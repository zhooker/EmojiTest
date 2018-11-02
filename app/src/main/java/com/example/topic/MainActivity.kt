package com.example.topic

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import com.example.topic.filter.CharFilter3
import com.example.topic.filter.EmojiHelper
import kotlinx.android.synthetic.main.activity_main.*

const val TAG = "zsj"
const val TIMEOUT = 100L
const val MAX_COUNT = 10

class MainActivity : AppCompatActivity() {

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
//                    var start = System.currentTimeMillis()
//                    val str1 = TopicTextUtils.getTopicSpannable(msg.obj.toString())
//                    Log.d(TAG, "time1 = ${System.currentTimeMillis() - start}")
//
//                    start = System.currentTimeMillis()
//                    val str2 = TopicTextUtils.getTopicSpannable2(msg.obj.toString())
//                    Log.d(TAG, "time2 = ${System.currentTimeMillis() - start}")
//
//                    start = System.currentTimeMillis()
//                    val str3 = TopicTextUtils.getTopicSpannable3(msg.obj.toString())
//                    Log.d(TAG, "time3 = ${System.currentTimeMillis() - start}")


                    val charSequence = msg.obj as CharSequence
                    val stringBuilder = StringBuilder()
                    var count = 0
                    var i = 0
                    while (i < charSequence.length) {
                        val num = EmojiHelper.getOffsetForBackspaceKey(charSequence, i)
                        val subSequence = charSequence.subSequence(i, i + num)

                        stringBuilder.append(subSequence)
                        stringBuilder.append("=")
                        for (c in subSequence.toString().toCharArray()) {
                            stringBuilder.append("0x" + Integer.toHexString(c.toInt()).toUpperCase() + " ")
                        }

                        stringBuilder.append("\n")

                        count++
                        i += num
                    }


                    if (count >= MAX_COUNT) {
                        tv_text_1.setText(String.format("字数达到上限%d个", MAX_COUNT))
                    } else {
                        tv_text_1.setText(String.format("字数%d个", count))
                    }

                    tv_text_2.setText(stringBuilder.toString())
                }
            }
        }
    };


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initEditText()
    }


    private fun initEditText() {
        ev_text_input.filters = arrayOf<InputFilter>(CharFilter3(MainActivity@ this, MAX_COUNT, object : CharFilter3.TextFilterListener {
            override fun onTextLengthOutOfLimit() {
                Log.d(TAG, "***onTextLengthOutOfLimit***")
            }
        }).ignoreExtraSpace(true))

        ev_text_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mHandler.removeMessages(1)
                mHandler.sendMessageDelayed(mHandler.obtainMessage(1, ev_text_input.editableText), TIMEOUT)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        ev_text_input.post {
            val text = "\uD83D\uDC81\u200D\uD83D\uDC82\u200D\uD83D\uDC88\u200D\uD83D\uDC75\u200D\uD83D\uDC49"
            ev_text_input.setText(text)
        }

        mHandler.removeMessages(1)
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1, ""), TIMEOUT)
    }
}
