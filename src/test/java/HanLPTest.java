import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.junit.Test;

import com.appleyk.core.CoreProcessor;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

public class HanLPTest {

	@Test
	public void TestA(){
		String lineStr = "明天虽然会下雨，但是我还是会看周杰伦的演唱会。";
        customDict(lineStr,"虽然会","ng 0");
    }

	@Test
	public void unsetNatureAndFrequency(){
		String lineStr = "建国大业这部电影真的很好看啊！";
        customDict(lineStr,"建国大业","nb 0");
    }

    private void customDict(String lineStr ,String word,String natureAndFrequency ) {
        try{
            Segment segment = HanLP.newSegment();
            segment.enableCustomDictionary(true);
            CustomDictionary.add(word,natureAndFrequency);
            List<Term> seg = segment.seg(lineStr);
            for (Term term : seg) {
                System.out.println(term.toString());
            }
        }catch(Exception ex){
            System.out.println(ex.getClass()+","+ex.getMessage());
        }
    }

    @Test
	public void TestB(){
		HanLP.Config.Normalization = true;
		CustomDictionary.insert("爱听4G", "nz 1000");
		System.out.println(HanLP.segment("爱听4g"));
		System.out.println(HanLP.segment("爱听4G"));
		System.out.println(HanLP.segment("爱听４G"));
		System.out.println(HanLP.segment("爱听４Ｇ"));
		System.out.println(HanLP.segment("愛聽４Ｇ"));
	}

	@Test
	public void TestC() throws Exception{
		CoreProcessor query = new CoreProcessor("D:/HanLP/data");
		String[] questionArr = new String[] {"卧虎藏龙的分数是多少"};
		for(String que: questionArr){
			ArrayList<String> question = query.analysis(que);
			System.err.println(question);
		}
	}

	@Test
	public void TestRDD(){

		SparkConf conf = new SparkConf().setAppName("NaiveBayesTest").setMaster("local[*]");
		JavaSparkContext sc = new JavaSparkContext(conf);


		/**
		 * MLlib的本地向量主要分为两种，DenseVector和SparseVector
		 * 前者是用来保存稠密向量，后者是用来保存稀疏向量		 
		 */

		/**
		 * 两种方式分别创建向量  == 其实创建稀疏向量的方式有两种，本文只讲一种
		 * (1.0, 0.0, 2.0）
		 * (2.0, 3.0, 0.0）
		 */

		//稠密向量 == 连续的
		Vector dense = Vectors.dense(1.0,0.0,2.0);
		System.out.println(dense);


		//稀疏向量 == 间隔的、指定的，未指定位置的向量值默认 = 0.0
		int len = 3;
		int[] index = new int[]{0,1};
		double[] values = new double[]{2.0,3.0};
		Vector sparse = Vectors.sparse(len, index, values);

		/**
		 * labeled point 是一个局部向量，要么是密集型的要么是稀疏型的
		 * 用一个label/response进行关联
		 * 在MLlib里，labeled points 被用来监督学习算法
		 * 我们使用一个double数来存储一个label，因此我们能够使用labeled points进行回归和分类
		 * 在二进制分类里，一个label可以是 0（负数）或者 1（正数）
		 * 在多级分类中，labels可以是class的索引，从0开始：0,1,2,......
		 */

		//训练集生成 ，规定数据结构为LabeledPoint == 构建方式:稠密向量模式  ，1.0:类别编号
		LabeledPoint train_one = new LabeledPoint(1.0,dense);  //(1.0, 0.0, 2.0）
		//训练集生成 ，规定数据结构为LabeledPoint == 构建方式:稀疏向量模式  ，2.0:类别编号
		LabeledPoint train_two = new LabeledPoint(2.0,sparse); //(2.0, 3.0, 0.0）
		//训练集生成 ，规定数据结构为LabeledPoint == 构建方式:稠密向量模式  ，3.0:类别编号
		LabeledPoint train_three = new LabeledPoint(3.0,Vectors.dense(1,1,2)); //(1.0, 1.0, 2.0）

		//List存放训练集【三个训练样本数据】
		List<LabeledPoint> trains = new ArrayList<>();
		trains.add(train_one);
		trains.add(train_two);
		trains.add(train_three);

		//获得弹性分布式数据集JavaRDD，数据类型为LabeledPoint
		JavaRDD<LabeledPoint> trainingRDD = sc.parallelize(trains);

		/**
		 * 利用Spark进行数据分析时，数据一般要转化为RDD
		 * JavaRDD转Spark的RDD
		 */
		NaiveBayesModel nb_model = NaiveBayes.train(trainingRDD.rdd());

		//测试集生成
		double []  dTest = {2,1,0};
		Vector vTest =  Vectors.dense(dTest);//测试对象为单个vector，或者是RDD化后的vector

		//朴素贝叶斯用法
		System.err.println(nb_model.predict(vTest));// 分类结果 == 返回分类的标签值
		/**
		 * 计算测试目标向量与训练样本数据集里面对应的各个分类标签匹配的概率结果
		 */
		System.err.println(nb_model.predictProbabilities(vTest));

		//最后不要忘了释放资源
		sc.close();
	}

}
