package com.xiaoyou.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 基于DFA算法的屏蔽词工具
 * Created by qiyubin on 2016/5/30 0030.
 */
public class StopWordsTools {
    // 字符编码
    private static String ENCODING = "UTF-8";
    private static String fileName = "stopWord.txt";
    private static Map stopWordMap = initStopWord();


    // 最小匹配规则,匹配后停止寻找,abc 如ab、abc都为屏蔽词 找到ab停止
    public static int minMatchTYpe = 1;
    // 最大匹配规则，匹配后继续寻找,abc 如ab、abc都为屏蔽词 找到ab、继续寻找，可以找到屏蔽词abc
    public static int maxMatchType = 2;

    public static void main(String args[]) {
        String test = "肏你妈的";
        StopWordsTools stopWordsTools = new StopWordsTools();
        Set<String> s = stopWordsTools.getStopWords(test, minMatchTYpe);
        System.out.print(s);
        boolean i = stopWordsTools.containStopWord(test, minMatchTYpe);
        System.out.print(i);
    }

    /**
     * 获取文字中的敏感词
     *
     * @param txt
     * @param matchType
     * @return
     */
    public static Set<String> getStopWords(String txt, int matchType) {
        Set<String> stopWordList = new LinkedHashSet<String>();

        for (int i = 0; i < txt.length(); i++) {

            // 判断是否包含敏感字符
            int length = checkStopWord(txt, i, matchType);

            // 存在,加入list中
            if (length > 0) {
                stopWordList.add(txt.substring(i, i + length));

                // 减1的原因，是因为for会自增
                i = i + length - 1;
            }
        }

        return stopWordList;
    }


    /**
     * 判断文字是否包含敏感字符
     *
     * @param txt
     * @param matchType
     * @return
     */
    public static boolean containStopWord(String txt, int matchType) {
        boolean flag = false;
        for (int i = 0; i < txt.length(); i++) {

            // 判断是否包含敏感字符
            int matchFlag = checkStopWord(txt, i, matchType);

            // 大于0存在，返回true
            if (matchFlag > 0) {
                flag = true;
            }
        }
        return flag;
    }


    /**
     * 检查文字中是否包含敏感字符，检查规则如下：<br>
     * 如果存在，则返回敏感词字符的长度，不存在返回0
     *
     * @param txt
     * @param beginIndex
     * @param matchType
     * @return
     */
    private static int checkStopWord(String txt, int beginIndex, int matchType) {

        // 敏感词结束标识位：用于敏感词只有1位的情况
        boolean flag = false;

        // 匹配标识数默认为0
        int matchFlag = 0;
        Map nowMap = stopWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            char word = txt.charAt(i);

            // 获取指定key
            nowMap = (Map) nowMap.get(word);

            // 存在，则判断是否为最后一个
            if (nowMap != null) {

                // 找到相应key，匹配标识+1
                matchFlag++;

                // 如果为最后一个匹配规则,结束循环，返回匹配标识数
                if ("1".equals(nowMap.get("isEnd"))) {

                    // 结束标志位为true
                    flag = true;

                    // 最小规则，直接返回,最大规则还需继续查找
                    if (minMatchTYpe == matchType) {
                        break;
                    }
                }
            }

            // 不存在，直接返回
            else {
                break;
            }
        }
        if(flag){
            return matchFlag;
        }
        // 长度必须大于等于1，为词
        if (matchFlag < 2 ) {
            matchFlag = 0;
        }
        return matchFlag;
    }

    //-----------------------------------------------------------------初始化-------------------------------------------

    /**
     * 初始化敏感字库
     *
     * @return
     */
    private static Map initStopWord() {

        // 读取敏感词库
        Set<String> wordSet = readStopWordFile();

        // 将敏感词库加入到HashMap中
        return addStopWordToHashMap(wordSet);
    }


    /**
     * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br>
     * 中 = { isEnd = 0 国 = {<br>
     * isEnd = 1 人 = {isEnd = 0 民 = {isEnd = 1} } 男 = { isEnd = 0 人 = { isEnd =
     * 1 } } } } 五 = { isEnd = 0 星 = { isEnd = 0 红 = { isEnd = 0 旗 = { isEnd = 1
     * } } } }
     */
    private static Map addStopWordToHashMap(Set<String> wordSet) {

        // 初始化敏感词容器，减少扩容操作
        Map wordMap = new HashMap(wordSet.size());

        for (String word : wordSet) {
            Map nowMap = wordMap;
            for (int i = 0; i < word.length(); i++) {

                // 转换成char型
                char keyChar = word.charAt(i);

                // 获取
                Object tempMap = nowMap.get(keyChar);

                // 如果存在该key，直接赋值
                if (tempMap != null) {
                    nowMap = (Map) tempMap;
                }

                // 不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                else {

                    // 设置标志位
                    Map<String, String> newMap = new HashMap<String, String>();
                    newMap.put("isEnd", "0");

                    // 添加到集合
                    nowMap.put(keyChar, newMap);
                    nowMap = newMap;
                }

                // 最后一个
                if (i == word.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }

        return wordMap;
    }

    /**
     * 读取敏感词库中的内容，将内容添加到set集合中
     *
     * @return
     * @throws Exception
     */
    private static Set<String> readStopWordFile() {

        Set<String> wordSet = null;

        // 读取文件
        try {
            InputStreamReader read = new InputStreamReader(StopWordsTools.class.getClassLoader().getResourceAsStream(fileName)
                    , ENCODING);
            // 文件流是否存在
            wordSet = new HashSet<String>();
            StringBuffer sb = new StringBuffer();
            BufferedReader bufferedReader = new BufferedReader(read);
            String txt = null;
            // 读取文件，将文件内容放入到set中
            while ((txt = bufferedReader.readLine()) != null) {
                sb.append(txt);
            }
            bufferedReader.close();
            String str = sb.toString();
            String[] ss = str.split(",");
            for (String s : ss) {
                wordSet.add(s);
            }
            // 关闭文件流
            read.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return wordSet;
    }

}
