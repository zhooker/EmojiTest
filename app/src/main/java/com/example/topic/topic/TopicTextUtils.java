package com.example.topic.topic;

import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 话题类
 *
 * @author zhuangsj
 */
public class TopicTextUtils {
    // 最多输入的话题数
    public static final int MAX_TOPIC_COUNT = 10;
    // 话题最长字数，中文20，英文40
    public static final int MAX_TOPIC_NAME_LENGTH = 20;

    // 正则匹配
    public static final String REG_TOPIC_TAG = "#";
    public static final String REG_EMOJI = "[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]";
    public static final String REG_TOPIC_TITLE = "[a-zA-Z0-9\\u4e00-\\u9fa5]|" + REG_EMOJI;
    public static final String REG_TOPIC = "#(" + REG_TOPIC_TITLE + "){1,40}\\[[0-9]{1,10}\\]#";

    /**
     * 根据内容获取话题标签列表
     *
     * @param content
     * @param pattern
     * @param excludeMentionCharacter
     * @return
     */
    public static List<String> getTopicList(String content, Pattern pattern, boolean excludeMentionCharacter) {
        List<String> mentionList = new ArrayList<>();
        if (TextUtils.isEmpty(content)) {
            return mentionList;
        }
        Matcher matcher = pattern == null ? Pattern.compile(REG_TOPIC).matcher(content) : pattern.matcher(content);
        while (matcher.find()) {
            String mentionText = matcher.group();
            if (!TopicTextUtils.checkTopicTitleLength(mentionText)) {
                continue;
            }
            if (excludeMentionCharacter) {
                mentionText = getTopicRealName(mentionText);
            }

            //if (!mentionList.contains(mentionText)) {
            mentionList.add(mentionText);
            //}
            if (mentionList.size() >= MAX_TOPIC_COUNT) {
                break;
            }
        }
        return mentionList;
    }

    /**
     * 匹配原始话题文本（" #XXX[type]# "), 转换为（" #XXX "）,同时记录所有话题的起点和终点位置，话题内容
     * before   : #XXX[type]#
     * after    : #XXX
     *
     * @param content    匹配的文本
     * @param topicList  匹配到的所有纯话题文本,只包含文本信息 ： XXX
     * @param startRange 所有话题的起点位置
     * @param endRange   所有话题的终点位置
     * @return 返回话题转换后的正常文本
     */
    public static String replaceTopicPlaceHolder(String content, List<String> topicList, List<Integer> startRange, List<Integer> endRange) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        StringBuilder resultBuilder = new StringBuilder(content);
        Pattern pattern = Pattern.compile(REG_TOPIC);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String matchStr = matcher.group();
            if (!TextUtils.isEmpty(matchStr)) {
                String realName = getTopicRealName(matchStr);
                String displayName = getTopicDisplayName(matchStr);
                int start = resultBuilder.indexOf(matchStr);
                int end = start + displayName.length();
                resultBuilder.replace(start, start + matchStr.length(), " " + displayName + " ");
                topicList.add(displayName);
                startRange.add(start);
                endRange.add(end + 2);
            }
        }
        return resultBuilder.toString();
    }

    public static void print(TextView tv) {
        Layout layout = tv.getLayout();
        String text = tv.getText().toString();
        int start = 0;
        int end = 0;


        for (int i = 0; i < tv.getLineCount(); i++) {
            end = layout.getLineEnd(i);
            String line = text.substring(start, end);
            Log.d("zsj", "print: start=" + start + ", end=" + end + " line " + line);
            start = end;
        }
    }


    public static CharSequence getTopicSpannable(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }

        List<Integer> startRange = new ArrayList<>();
        List<Integer> endRange = new ArrayList<>();

        StringBuilder resultBuilder = new StringBuilder(content);
        Pattern pattern = Pattern.compile(REG_TOPIC);
        Matcher matcher = pattern.matcher(content);
        int lastPos = 0;
        while (matcher.find()) {
            String matchStr = matcher.group();
            if (!TextUtils.isEmpty(matchStr)) {
                String displayName = getTopicDisplayName(matchStr);
                int start = resultBuilder.indexOf(matchStr, lastPos);
                int end = start + displayName.length();
                resultBuilder.replace(start, start + matchStr.length(), " " + displayName + " ");
                startRange.add(start);
                endRange.add(end + 2);
                lastPos = end;
            }
        }

        SpannableString spannableString = new SpannableString(resultBuilder.toString());
        for (int i = 0; i < endRange.size(); i++) {
            if (endRange.get(i) <= startRange.get(i)) {
                continue;
            }

            spannableString.setSpan(new ForegroundColorSpan(0xFFff277A)
                    , startRange.get(i)
                    , endRange.get(i)
                    , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }


    /**
     * 时间是上一个的2倍？？？ 约4~8ms
     * SpannableStringBuilder时间消耗比较大
     *
     * @param content
     * @return
     */
    public static CharSequence getTopicSpannable2(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }

        SpannableStringBuilder resultBuilder = new SpannableStringBuilder(content);
        Pattern pattern = Pattern.compile(REG_TOPIC);
        Matcher matcher = pattern.matcher(content);
        int posDiff = 0;
        while (matcher.find()) {
            String matchStr = matcher.group();
            if (!TextUtils.isEmpty(matchStr)) {
                String displayName = " " + getTopicDisplayName(matchStr) + " ";
                int start = matcher.start() + posDiff;
                resultBuilder.replace(start, start + matchStr.length(), displayName);

                resultBuilder.setSpan(new ForegroundColorSpan(0xFFff277A)
                        , start
                        , start + displayName.length()
                        , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                posDiff += displayName.length() - matchStr.length();
            }
        }

        return resultBuilder;
    }

    /**
     * 最终方案
     *
     * @param content
     * @return
     */
    public static CharSequence getTopicSpannable3(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }

        List<Integer> startRange = new ArrayList<>();

        StringBuilder resultBuilder = new StringBuilder(content);
        Pattern pattern = Pattern.compile(REG_TOPIC);
        Matcher matcher = pattern.matcher(content);

        int posDiff = 0;
        while (matcher.find()) {
            String matchStr = matcher.group();
            if (!TextUtils.isEmpty(matchStr)) {
                String displayName = " " + getTopicDisplayName(matchStr) + " ";
                int start = matcher.start() + posDiff;
                startRange.add(start);
                resultBuilder.replace(start, start + matchStr.length(), displayName);
                posDiff += displayName.length() - matchStr.length();
            }
        }

        SpannableString spannableString = new SpannableString(resultBuilder.toString());
        for (int i = 0; i < startRange.size(); i++) {
            spannableString.setSpan(new ForegroundColorSpan(0xFFff277A)
                    , startRange.get(i)
                    , startRange.get(i) + 2
                    , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }


    public static void setTopicPlaceHolder(TextView tv, String content) {
        List<String> topicList = new ArrayList<>();
        List<Integer> startRange = new ArrayList<>();
        List<Integer> endRange = new ArrayList<>();

        String covertText = TopicTextUtils.replaceTopicPlaceHolder(content, topicList, startRange, endRange);

        SpannableStringBuilder spannableString = new SpannableStringBuilder(covertText);
        for (int i = 0; i < topicList.size(); i++) {
            if (endRange.get(i) <= startRange.get(i)) {
                continue;
            }

            Log.d("zsj", "replaceTopicPlaceHolder2: start=" + startRange.get(i) + ", end=" + endRange.get(i));

            spannableString.setSpan(new ForegroundColorSpan(0xFFff277A)
                    , startRange.get(i)
                    , endRange.get(i)
                    , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        tv.setText(spannableString);


        Layout layout = tv.getLayout();
        String text = tv.getText().toString();
        int start = 0;
        int end = 0;

        for (int i = 0; i < tv.getLineCount(); i++) {
            end = layout.getLineEnd(i);
            String line = text.substring(start, end);
            Log.d("zsj", "print: start=" + start + ", end=" + end + " line " + line);

            int s = layout.getEllipsisStart(i);
            int c = layout.getEllipsisCount(i);
            Log.d("zsj", "print: i=" + i + ", s=" + s + " c " + c);
            if (c > 0 && s > 0) {
                for (int j = 0; j < topicList.size(); j++) {
                    Log.d("zsj", "print: startRange=" + (startRange.get(j) - start) + ", endRange=" + (endRange.get(j) - start));
                    if (startRange.get(j) - start <= s && endRange.get(j) - start >= s) {
                        if (s - startRange.get(j) + start <= 2) {
                            //spannableString.removeSpan(spannableString.getSpans(startRange.get(j) - 1, endRange.get(j), ForegroundColorSpan.class));
                            spannableString.replace(startRange.get(j) + 1, startRange.get(j) + 2, ".");
                            tv.setText(spannableString);
                            return;
                        }
                    }
                }
            }

            start = end;
        }
    }

    public static String replaceTopicPlaceHolder3(TextView tv, String content) {
        List<String> topicList = new ArrayList<>();
        List<Integer> startRange = new ArrayList<>();
        List<Integer> endRange = new ArrayList<>();

        String covertText = TopicTextUtils.replaceTopicPlaceHolder(content, topicList, startRange, endRange);
        return covertText;
    }

    /**
     * 检测话题标签的长度，中文20个，英文40个
     * todo 也许能用正则表达式直接匹配？
     *
     * @param title
     * @return
     */
    public static boolean checkTopicTitleLength(String title) {
        title = getTopicRealName(title);
        if (title.length() <= MAX_TOPIC_NAME_LENGTH) {
            return true;
        }

        int count = 0;
        for (int i = 0; i < title.length(); i++) {
            char c = title.charAt(i);
            if (isChinese(c)) {
                count += 2;
            } else {
                count++;
            }
            if (count > MAX_TOPIC_NAME_LENGTH * 2) {
                return false;
            }
        }

        return true;
    }

    /**
     * 是否中文字符
     *
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }


    /**
     * 获取话题标签名字
     * before   : #XXX[type]#
     * after    : XXX
     *
     * @param title
     * @return
     */
    public static String getTopicRealName(String title) {
        return title.substring(1, title.lastIndexOf("["));
    }

    /**
     * 获取话题标签显示的名字
     * before   : #XXX[type]#
     * after    : #XXX
     *
     * @param title 话题标签名
     * @return
     */
    public static String getTopicDisplayName(String title) {
        return REG_TOPIC_TAG + getTopicRealName(title);
    }

    /**
     * 话题标签名称包装
     * before   : XXX
     * after    : #XXX[type]#
     *
     * @param name 话题名，不包含#
     * @return
     */
    public static String wrapTopicName(String name, int type, boolean prefix) {
        return new StringBuilder(prefix ? REG_TOPIC_TAG : "")
                .append(name)
                .append(1)
                .append(REG_TOPIC_TAG)
                .toString();
    }

    /**
     * 获取话题类型，默认自定义
     *
     * @param type
     * @return
     */
    public static String getType(int type) {
        return "[" + type + "]";
    }

    /**
     * 通过话题名获取话题类型
     *
     * @param content
     * @return
     */
    public static int getType(String content) {
        return Integer.parseInt(content.substring(content.lastIndexOf('[') + 1, content.lastIndexOf(']')));
    }
}
