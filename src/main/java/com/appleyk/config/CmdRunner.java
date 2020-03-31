package com.appleyk.config;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import java.io.*;

/**
 * <p>初始化项目时，执行命令，将相关额外的自定义词典加载下</p>
 *
 * @author Appleyk
 * @version V.0.1.1
 * @blob https://blog.csdn.net/Appleyk
 * @date created on 21:25 2020/3/31
 */
public class CmdRunner implements CommandLineRunner {

    @Value("${HanLP.CustomDictionary.path.movieDict}")
    private String movieDictPath;

    @Value("${HanLP.CustomDictionary.path.genreDict}")
    private String genreDictPath;

    @Value("${HanLP.CustomDictionary.path.scoreDict}")
    private String scoreDictPath;

    @Override
    public void run(String... args) throws Exception {

        /**
         * 加载自定义的电影字典 == 设置词性 nm 0
         */
        loadMovieDict(movieDictPath);

        /**
         * 加载自定义的类型字典 == 设置词性 ng 0
         */
        loadGenreDict(genreDictPath);

        /**
         * 加载自定义的评分字典 == 设置词性 x 0
         */
        loadScoreDict(scoreDictPath);
    }

    /**
     * 加载自定义电影字典
     *
     * @param path
     */
    public void loadMovieDict(String path) {

        File file = new File(path);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            addCustomDictionary(br, 0);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

    }

    /**
     * 加载自定义电影类别字典
     *
     * @param path
     */
    public void loadGenreDict(String path) {

        File file = new File(path);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            addCustomDictionary(br, 1);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 加载自定义电影评分字典
     *
     * @param path
     */
    public void loadScoreDict(String path) {

        File file = new File(path);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            addCustomDictionary(br, 2);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 添加自定义分词及其词性，注意数字0表示频率，不能没有
     *
     * @param br
     * @param type
     */
    public void addCustomDictionary(BufferedReader br, int type) {

        String word;
        try {
            while ((word = br.readLine()) != null) {
                switch (type) {
                    /**
                     * 设置电影名词词性 == nm 0
                     */
                    case 0:
                        CustomDictionary.add(word, "nm 0");
                        break;
                    /**
                     * 设置电影类型名词 词性 == ng 0
                     */
                    case 1:
                        CustomDictionary.add(word, "ng 0");
                        break;
                    /**
                     * 设置电影评分数词 词性 == x 0
                     */
                    case 2:
                        CustomDictionary.add(word, "x 0");
                        break;
                    default:
                        break;
                }
            }
            br.close();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
