package com.example.topic.paragraph;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuangsj
 * @created 2018/11/6
 */
public class ParagraphHelper {
    public static void setParagraphSpacing(TextView tv, String content, float paragraphSpacing) {
        if (!content.contains("\n")) {
            tv.setText(content);
            return;
        }
        content = content.replace("\n", "\n\r");

        int previousIndex = -2;
        List<Integer> nextParagraphBeginIndexes = new ArrayList<>();

        do {
            previousIndex = content.indexOf("\n\r", previousIndex + 2);
            if (previousIndex != -1) {
                nextParagraphBeginIndexes.add(previousIndex);
            }
        } while (previousIndex != -1);

        SpannableString spanString = new SpannableString(content);
        Drawable d = new ColorDrawable(Resources.getSystem().getColor(android.R.color.transparent, null));
        d.setBounds(0, 0, 1, (int) (tv.getLineHeight() * paragraphSpacing / 1.2f));

        for (int index : nextParagraphBeginIndexes) {
            spanString.setSpan(new ImageSpan(d), index + 1, index + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(spanString);
    }
}
