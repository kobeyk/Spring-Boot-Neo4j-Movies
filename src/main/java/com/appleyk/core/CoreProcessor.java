package com.appleyk.core;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import scala.reflect.internal.Trees;

/**
 * Spark贝叶斯分类器 + HanLP分词器 + 实现问题语句的抽象+模板匹配+关键性语句还原
 * @auhtor Appleyk
 * @blob   http://blog.csdn.net/appleyk
 * @date   updated on 2020年4月5日-下午21:00:00
 */
public class CoreProcessor {

    /**指定问题question及字典的txt模板所在的根目录*/
    private String rootDirPath;

    /**Spark贝叶斯分类器*/
    private NaiveBayesModel nbModel;

    /**分类标签号和问句模板对应表*/
    private Map<Double, String> questionsPattern;

    /**词语和下标的对应表   == 词汇表*/
    private Map<String, Integer> vocabulary;

    /**关键字与其词性的map键值对集合 == 句子抽象*/
    private Map<String, String> abstractMap;

    /** 分类模板索引*/
    int modelIndex = 0;

    public CoreProcessor(String rootDirPath) throws Exception{
        this.rootDirPath = rootDirPath+'/';
        // 加载问题模板
        questionsPattern = loadQuestionTemplates();
        // 加载词汇表
        vocabulary = loadVocabulary();
        // 加载分类模型，初始化贝叶斯分类器对象
        nbModel = loadClassifierModel();
    }

    /**
     * 问句拆解，套用模板，得到关键word，核心方法实现
     * @param querySentence 问句
     * @return 结果集合（问题模板索引、关键word数组）
     * @throws Exception
     */
    public List<String> analysis(String querySentence) throws Exception {

        /**原始问句*/
        System.out.println("原始句子："+querySentence);
        System.out.println("========HanLP开始分词========");

        /**抽象句子，利用HanPL分词，将关键字进行词性抽象*/
        String abstractStr = queryAbstract(querySentence);
        System.out.println("句子抽象化结果："+abstractStr);

        /**将抽象的句子与Spark训练集中的模板进行匹配，拿到句子对应的模板*/
        String strPattern = queryClassify(abstractStr);
        System.out.println("句子套用模板结果："+strPattern);

        /**模板还原成句子，此时问题已转换为我们熟悉的操作*/
        String finalPattern = sentenceReduction(strPattern);
        System.out.println("原始句子替换成系统可识别的结果："+finalPattern);

        List<String> resultList = new ArrayList<>();
        resultList.add(String.valueOf(modelIndex));
        String[] finalPatternArr = finalPattern.split(" ");
        for (String word : finalPatternArr)
            resultList.add(word);
        return resultList;

    }

    /**
     * 将HanLp分词后的关键word，用抽象词性xx替换
     * @param querySentence 查询句子
     * @return
     */
    public  String queryAbstract(String querySentence) {

        // 句子抽象化
        Segment segment = HanLP.newSegment().enableCustomDictionary(true);
        List<Term> terms = segment.seg(querySentence);
        String abstractQuery = "";
        abstractMap = new HashMap<>();
        // nr 人名词性这个 词语出现的频率
        int nrCount = 0;
        for (Term term : terms) {
            String word = term.word;
            String termStr = term.toString();
            System.out.println(termStr);
            if (termStr.contains("nm")) {        //nm 电影名
                abstractQuery += "nm ";
                abstractMap.put("nm", word);
            } else if (termStr.contains("nr") && nrCount == 0) { //nr 人名
                abstractQuery += "nnt ";
                abstractMap.put("nnt", word);
                nrCount++;
            }else if (termStr.contains("nr") && nrCount == 1) { //nr 人名 再出现一次，改成nnr
                abstractQuery += "nnr ";
                abstractMap.put("nnr", word);
                nrCount++;
            }else if (termStr.contains("x")) {  //x  评分
                abstractQuery += "x ";
                abstractMap.put("x", word);
            } else if (termStr.contains("ng")) { //ng 类型
                abstractQuery += "ng ";
                abstractMap.put("ng", word);
            }
            else {
                abstractQuery += word + " ";
            }
        }
        System.out.println("========HanLP分词结束========");
        return abstractQuery;
    }

    /**
     * 将句子模板还原成正常的语句（分词关键word的抽象词性替换成原有的word）
     * @param queryPattern
     * @return
     */
    public String sentenceReduction(String queryPattern) {
        Set<String> set = abstractMap.keySet();
        for (String key : set) {
            /**如果句子模板中含有抽象的词性*/
            if (queryPattern.contains(key)) {
                /**则替换抽象词性为具体的值*/
                String value = abstractMap.get(key);
                queryPattern = queryPattern.replace(key, value);
            }
        }
        String extendedQuery = queryPattern;
        /**当前句子处理完，抽象map清空释放空间并置空，等待下一个句子的处理*/
        abstractMap.clear();
        abstractMap = null;
        return extendedQuery;
    }


    /**
     * 加载词汇表 == 关键特征 == 与HanLP分词后的单词进行匹配
     * @return
     */
    public Map<String,Integer> loadVocabulary() {
        Map<String, Integer> vocabulary = new HashMap<String, Integer>();
        File file = new File(rootDirPath + "question/vocabulary.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(":");
                int index = Integer.parseInt(tokens[0].replace("\uFEFF",""));
                String word = tokens[1];
                vocabulary.put(word, index);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vocabulary;
    }


    /**
     * 句子分词后与词汇表进行key匹配转换为double向量数组
     * @param sentence 句子
     * @return 向量数组
     */
    public double[] sentenceToArrays(String sentence){
        double[] vector = new double[vocabulary.size()];
        /**模板对照词汇表的大小进行初始化，全部为0.0*/
        for (int i = 0; i < vocabulary.size(); i++) {
            vector[i] = 0;
        }

        /** HanLP分词，拿分词的结果和词汇表里面的关键特征进行匹配*/
        Segment segment = HanLP.newSegment();
        List<Term> terms = segment.seg(sentence);
        for (Term term : terms) {
            String word = term.word;
            /**如果命中，0.0 改为 1.0*/
            if (vocabulary.containsKey(word)) {
                int index = vocabulary.get(word);
                vector[index] = 1;
            }
        }
        return vector;
    }

    /**
     * Spark朴素贝叶斯(naiveBayes)
     * 对特定的模板进行加载并分类
     * 欲了解Spark朴素贝叶斯，可参考地址：https://blog.csdn.net/appleyk/article/details/80348912
     * @return
     * @throws Exception
     */
    public NaiveBayesModel loadClassifierModel() throws Exception {

        /**
         * 生成Spark对象
         * 一、Spark程序是通过SparkContext发布到Spark集群的
         * Spark程序的运行都是在SparkContext为核心的调度器的指挥下进行的
         * Spark程序的结束是以SparkContext结束作为结束
         * JavaSparkContext对象用来创建Spark的核心RDD的
         * 注意：第一个RDD,一定是由SparkContext来创建的
         *
         * 二、SparkContext的主构造器参数为 SparkConf
         * SparkConf必须设置appname和master，否则会报错
         * spark.master   用于设置部署模式
         * local[*] == 本地运行模式[也可以是集群的形式]，如果需要多个线程执行，可以设置为local[2],表示2个线程 ，*表示多个
         * spark.app.name 用于指定应用的程序名称  ==
         */

        /**
         * 题外话
         * 贝叶斯是谁？
         * 贝叶斯(约1701-1763) Thomas Bayes，英国数学家。
         * 1702年出生于伦敦，做过神甫。
         * 1742年成为英国皇家学会会员。
         * 1763年4月7日逝世。
         * 贝叶斯在数学方面主要研究概率论 == 贝叶斯公式是概率论中较为重要的公式
         */
        SparkConf conf = new SparkConf().setAppName("NaiveBayesTest").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        /**
         * 训练集生成
         * labeled point 是一个局部向量，要么是密集型的要么是稀疏型的
         * 用一个label/response进行关联。在MLlib里，labeled points 被用来监督学习算法
         * 我们使用一个double数来存储一个label，因此我们能够使用labeled points进行回归和分类
         */
        List<LabeledPoint> train_list = new LinkedList<>();
        String[] sentences;
        Map<Double, String> seqWithSamples = loadQuestionSamples("question");
        if(seqWithSamples == null || seqWithSamples.size() == 0){
            throw new Exception("缺少问题训练样本，请核查！");
        }

        for (Map.Entry<Double, String> entry : seqWithSamples.entrySet()) {
            Double seq = entry.getKey();
            String sampleContent = entry.getValue();
            sentences = sampleContent.split("`");
            for (String sentence : sentences) {
                double[] array = sentenceToArrays(sentence);
                LabeledPoint train = new LabeledPoint(seq, Vectors.dense(array));
                train_list.add(train);
            }
        }

        /**
         * SPARK的核心是RDD(弹性分布式数据集)
         * Spark是Scala写的,JavaRDD就是Spark为Java写的一套API
         * JavaSparkContext sc = new JavaSparkContext(sparkConf);    //对应JavaRDD
         * SparkContext	    sc = new SparkContext(sparkConf)    ;    //对应RDD
         */
        JavaRDD<LabeledPoint> trainingRDD = sc.parallelize(train_list);
        /**开始训练样本*/
        NaiveBayesModel nb_model = NaiveBayes.train(trainingRDD.rdd());
        /** 记得关闭资源*/
        sc.close();
        /** 返回贝叶斯分类器*/
        return nb_model;
    }

    /**
     * 加载问题样本数据，并返回每个样本的序号和内容键值对
     * @param path 路径（可以是文件夹，也可以是单个文件）
     * @return Map<Double,String>
     */
    public Map<Double,String> loadQuestionSamples(String path) throws IOException {

        File file = new File(rootDirPath+path);
        if(!file.exists()){
            throw new IOException("文件不存在！");
        }

        Map<Double,String> seqWithSamples = new HashMap<>(16);

        if(!file.isDirectory()){
            String content = getFileContent(file);
            seqWithSamples.put(0.0,content);
        }

        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.contains("【") && name.contains("】");
            }
        });

        if(files!=null && files.length>0){
            for (int i = 0; i <files.length ; i++) {
                File sampleFile = files[i];
                String fileName = sampleFile.getName();
                String subStr = fileName.substring(0, fileName.indexOf("】"));
                String seqStr = subStr.replace("【", "").replace("】", "");
                String content = getFileContent(sampleFile);
                seqWithSamples.put(Double.parseDouble(seqStr),content);
            }
        }

        return seqWithSamples;
    }

    /**
     * 获取文件内容
     * @param file 文件
     * @return String
     */
    private String getFileContent(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String content = "";
        String line;
        while ((line = br.readLine()) != null) {
            /**文本的换行符暂定用"`"代替*/
            content += line + "`";
        }
        /**关闭资源*/
        br.close();
        return content;
    }

    /**
     * 加载问题模板 == 分类器标签
     * @return Map<Double, String> == 序号，问题分类
     */
    public Map<Double,String> loadQuestionTemplates() {
        Map<Double, String> questionsPattern = new HashMap<>(16);
        File file = new File(rootDirPath + "question/question_classification.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(":");
                double index = Double.valueOf(tokens[0].replace("\uFEFF",""));
                String pattern = tokens[1];
                questionsPattern.put(index, pattern);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questionsPattern;
    }

    /**
     * 贝叶斯分类器分类的结果，拿到匹配的分类标签号，后续可根据标签号定位到指定的问题模板
     * @param sentence 句子
     * @return
     * @throws Exception
     */
    public String queryClassify(String sentence) throws Exception {

        double[] testArray = sentenceToArrays(sentence);
        Vector v = Vectors.dense(testArray);
        /**
         * 对数据进行预测predict
         * 句子模板在 spark贝叶斯分类器中的索引【位置】
         * 根据词汇使用的频率推断出句子对应哪一个模板
         * 原则：高频率的会被预测出
         */
        double index = nbModel.predict(v);
        modelIndex = (int)index;
        System.out.println("the model index is " + index);
        Vector vRes = nbModel.predictProbabilities(v);
        double[] probabilities = vRes.toArray();
        System.out.println("============ 问题模板分类概率 =============");
        for (int i = 0; i < probabilities.length; i++) {
            System.out.println("问题模板分类【"+i+"】概率："+String.format("%.5f", probabilities[i]));
        }
        System.out.println("============ 问题模板分类概率 =============");
        return questionsPattern.get(index);

    }

    public static void main(String[] agrs){
        System.out.println("Hello World !");
    }
}
