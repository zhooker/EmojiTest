package com.example.topic.filter;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

/**
 * 发布页面标题、内容输入过滤
 * 最终版本，可以完美检测emoji表情
 *
 * @author zhuangsj
 */
public class CharFilter3 implements InputFilter {

    public static final String emojiReg = "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]";
    public static final String returnReg = "[\\n]";

    public interface TextFilterListener {
        void onTextLengthOutOfLimit();
    }

    protected int MAX_LEN;
    protected Pattern mPattern;
    protected char[] ignores;
    protected TextFilterListener mListener;
    protected boolean ignoreExtraSpace = false;


    public CharFilter3(Context context, int maxlen, TextFilterListener listener) {
        this.MAX_LEN = maxlen;
        this.mListener = listener;

    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        if (isIncompatibleChar(source)) {
            //if (mListener != null) {
            //    mListener.onTextIncompatible(source);
            //}
            return "";
        }

        int destCount = ignoreExtraSpace ? getRealCharSequenceLength(dest, source, dstart, dend) : getRealCharSequenceLength(dest, dstart, dend);


        int count = 0;
        int i = 0;
        int blankCount = -1;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (ignoreExtraSpace && destCount < MAX_LEN) {
                if (blankCount < 0 && c == ' ') {
                    blankCount--;
                    i++;
                    continue;
                }
            }

            if (!isIgnoreChar(c)) {
                if (destCount < MAX_LEN) {
                    if (count + destCount + 1 <= MAX_LEN) {
                        i = checkEmoji(i, source);
                    } else {
                        break;
                    }
                    count += 1;
                } else {
                    // 此时字符已达限制，但是输入的是不能忽略字符，所以break。
                    break;
                }
            } else if (c == 0x200D && count + destCount >= MAX_LEN) {
                break;
            }

            i++;
        }

        if (i >= source.length()) {
            return source;
        } else {
            if (mListener != null) {
                mListener.onTextLengthOutOfLimit();
            }
            // 如果输入字符太长，超过限制将会被截取
            return i == 0 ? "" : source.subSequence(0, i).toString();
        }
    }

    public void setTextFilterListener(TextFilterListener mListener) {
        this.mListener = mListener;
    }

    protected int getRealCharSequenceLength(CharSequence source, int dstart, int dend) {
        int i = 0;
        int count = 0;
        while (i < source.length()) {
            if (dend - dstart > 0) {
                if (i >= dstart && i < dend) {
                    i++;
                    continue;
                }
            }

            char c = source.charAt(i);
            if (!isIgnoreChar(c)) {
                i = checkEmoji(i, source);
                count++;
            }
            i++;
        }

        return count;
    }

    protected int getRealCharSequenceLength(CharSequence dest, CharSequence source, int dstart, int dend) {
        int i = 0;

        int firstBlankCount = 0;
        while (i < dest.length()) {
            char c = dest.charAt(i);
            if (c == ' ') {
                firstBlankCount++;
            } else {
                break;
            }
            i++;
        }

        int lastBlankCount = 0;
        if (firstBlankCount < dest.length() - 2) {
            i = dest.length() - 1;
            while (i >= 0) {
                char c = dest.charAt(i);
                if (c == ' ') {
                    lastBlankCount++;
                } else {
                    break;
                }
                i--;
            }
        }

        int count = 0;
        if (firstBlankCount + lastBlankCount < dest.length() - 1) {
            i = firstBlankCount;
            while (i < dest.length() - lastBlankCount) {
                if (dend - dstart > 0) {
                    if (i >= dstart && i < dend) {
                        i++;
                        continue;
                    }
                }

                char c = dest.charAt(i);
                if (!isIgnoreChar(c)) {
                    i = checkEmoji(i, dest);
                    count++;
                }
                i++;
            }
        }

        if (source.toString().trim().length() > 0) {
            if (dstart <= firstBlankCount) {
                count += firstBlankCount - dstart;
            }

            if (dstart > dest.length() - lastBlankCount) {
                count += dstart - (dest.length() - lastBlankCount);
            }
        }
        return count;
    }

    protected boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    /**
     * 判断是否忽略字符，如果字符忽略，那就不会计算到最大字符数上
     */
    protected boolean isIgnoreChar(char c) {
        if (c == 0x200D || c == 0xFE0F || c == 0xFE0E) {
            return true;
        }

        if (ignores != null) {
            for (char temp : ignores) {
                if (temp == c) {
                    return true;
                }
            }
        }
        return false;
    }

    protected int checkEmoji(int i, CharSequence source) {
        if (i < source.length() - 1) {
//            if (Character.isHighSurrogate(source.charAt(i)) && Character.isLowSurrogate(source.charAt(i + 1))) {
//                return i + 1;
//            }
            int count = EmojiHelper.getOffsetForBackspaceKey(source,i);
            return count + i - 1;
        }

        return i;
    }

    /**
     * 判断输入是否有不支持的字符，包含不支持字符将不能输入
     */
    protected boolean isIncompatibleChar(CharSequence source) {
        if (mPattern != null) {
            if (mPattern.matcher(source).find()) {
                return true;
            }
        }
        return false;
    }

    public CharFilter3 reject(String... regex) {
        StringBuilder sb = new StringBuilder(regex[0]);
        for (int i = 1; i < regex.length; i++) {
            sb.append("|");
            sb.append(regex[i]);
        }
        mPattern = Pattern.compile(sb.toString(), Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        return CharFilter3.this;
    }


    public CharFilter3 ignore(char[] ignores) {
        this.ignores = ignores;
        return CharFilter3.this;
    }

    public CharFilter3 ignoreExtraSpace(boolean ignore) {
        this.ignoreExtraSpace = ignore;
        return CharFilter3.this;
    }
}