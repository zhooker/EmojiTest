package com.example.topic.topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
import android.view.View;
import android.widget.EditText;

/**
 * @author zhuangsj
 * @created 2018/9/28
 */
public class TopicTextSpan extends ReplacementSpan {
    private String mText;  //文字
    private float mBgWidth;  //背景宽度
    private float mMargin; //边距
    private Paint mTextPaint; //文字画笔

    public TopicTextSpan(View editText, String text) {
        this(editText.getContext(), 0, text, editText.getWidth() - editText.getPaddingLeft() - editText.getPaddingRight());
    }

    public TopicTextSpan(Context context, int fgColorResId, String text, float maxWidth) {
        // 初始化文字画笔
        initPaint(0xFFff277A, dp2px(context, 14f));

        // 初始化尺寸
        initDefaultValue(text, maxWidth);
    }

    private void initPaint(int fgColorResId, float textSize) {
        mTextPaint = new TextPaint();
        mTextPaint.setColor(fgColorResId);
        mTextPaint.setTextSize(textSize);
        //mTextPaint.setTypeface(CmyFontHelper.getInstance().getTypeface());
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initDefaultValue(String text, float maxWidth) {
        this.mMargin = this.mTextPaint.measureText(" ") * 0;
        if (maxWidth <= 0) {
            this.mText = text;
            this.mBgWidth = this.mTextPaint.measureText(text);
        } else {
            float[] widths = new float[1];
            int index = this.mTextPaint.breakText(text, true, maxWidth - this.mMargin, widths);
            this.mText = text.substring(0, index);
            this.mBgWidth = widths[0];
        }
    }

    /**
     * dp to px
     *
     * @param context
     * @param dpValue
     * @return
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 设置宽度，宽度=背景宽度+右边距
     */
    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) (mBgWidth + mMargin);
    }

    /**
     * draw
     *
     * @param text   完整文本
     * @param start  setSpan里设置的start
     * @param end    setSpan里设置的start
     * @param x
     * @param top    当前span所在行的上方y
     * @param y      y其实就是metric里baseline的位置
     * @param bottom 当前span所在行的下方y(包含了行间距)，会和下一行的top重合
     * @param paint  使用此span的画笔
     */
    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        canvas.drawText(mText, x + (mBgWidth + mMargin) / 2, y, mTextPaint);
    }
}
