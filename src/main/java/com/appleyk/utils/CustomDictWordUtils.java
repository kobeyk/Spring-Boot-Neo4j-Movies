package com.appleyk.utils;

import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;

/**
 * <p>自定义字典单词工具类（设置词性、词频）</p>
 *
 * @author Appleyk
 * @version V.0.1.1
 * @blob https://blog.csdn.net/Appleyk
 * @date created on 9:44 2020/4/1
 */
public class CustomDictWordUtils {

    /**
     * 重置默认word的词性和词频
     * @param word 单词
     * @param natureWithFrequency 词性+词频
     * @param isForce 是否强制执行
     */
    public static void setNatureAndFrequency(String word, String natureWithFrequency,boolean isForce){

        /**如果不强制的话，就普通添加*/
        if(!isForce){
            CustomDictionary.add(word,natureWithFrequency);
            return;
        }

        /**如果强制的话，先看自带的hanlp有没有这个单词的定义，就有重置，没有就普通添加*/
        if(CustomDictionary.contains(word)){
            unsetNatureAndFrequency(word,natureWithFrequency);
        }else{
            CustomDictionary.add(word,natureWithFrequency);
        }

    }

    /**
     * 重置默认word的词性和词频
     * @param word 单词
     * @param natureWithFrequency 词性+词频
     */
    public static void unsetNatureAndFrequency(String word, String natureWithFrequency){

        String[] natureWithFrequencyArr = natureWithFrequency.split(" ");
        /*新词性*/
        Nature natureNew = null;
        /*新词频*/
        int frequencyNew = 0 ;
        if(natureWithFrequencyArr!=null && natureWithFrequencyArr.length == 2){
            natureNew =  Nature.create(natureWithFrequencyArr[0]);
            frequencyNew = Integer.parseInt(natureWithFrequencyArr[1]);
        }
        DoubleArrayTrie<CoreDictionary.Attribute> dat = CustomDictionary.dat;
        CoreDictionary.Attribute attribute = dat.get(word);
        if(attribute == null){
            CustomDictionary.add(word,natureWithFrequency);
            return;
        }
        Nature[] nature = attribute.nature;
        int[] frequency = attribute.frequency;
        if(natureNew!=null && nature!=null && nature.length>1){
            nature[0] = natureNew;
        }
        if(frequency!=null && frequency.length>1){
            frequency[0] = frequencyNew;
        }

    }

}
