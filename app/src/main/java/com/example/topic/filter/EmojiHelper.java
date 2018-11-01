package com.example.topic.filter;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;

/**
 * @author zhuangsj
 * @created 2018/10/31
 */
public class EmojiHelper {

    private static final int LINE_FEED = 0x0A;
    private static final int CARRIAGE_RETURN = 0x0D;


    public static int getCharSequenceCount(CharSequence text) {
        int i = 0, count = 0;
        while (i < text.length()) {
            i += getOffsetForBackspaceKey(text, i);
            count++;
        }

        return count;
    }


    // Returns the start offset to be deleted by a backspace key from the given offset.
    public static int getOffsetForBackspaceKey(CharSequence text, int offset) {
//        if (offset <= 1) {
//            return 0;
//        }

        // Initial state
        final int STATE_START = 0;

        // The offset is immediately before line feed.
        final int STATE_LF = 1;

        // The offset is immediately before a KEYCAP.
        final int STATE_BEFORE_KEYCAP = 2;
        // The offset is immediately before a variation selector and a KEYCAP.
        final int STATE_BEFORE_VS_AND_KEYCAP = 3;

        // The offset is immediately before an emoji modifier.
        final int STATE_BEFORE_EMOJI_MODIFIER = 4;
        // The offset is immediately before a variation selector and an emoji modifier.
        final int STATE_BEFORE_VS_AND_EMOJI_MODIFIER = 5;

        // The offset is immediately before a variation selector.
        final int STATE_BEFORE_VS = 6;

        // The offset is immediately before an emoji.
        final int STATE_BEFORE_EMOJI = 7;
        // The offset is immediately before a ZWJ that were seen before a ZWJ emoji.
        final int STATE_BEFORE_ZWJ = 8;
        // The offset is immediately before a variation selector and a ZWJ that were seen before a
        // ZWJ emoji.
        final int STATE_BEFORE_VS_AND_ZWJ = 9;

        // The number of following RIS code points is odd.
        final int STATE_ODD_NUMBERED_RIS = 10;
        // The number of following RIS code points is even.
        final int STATE_EVEN_NUMBERED_RIS = 11;

        // The offset is in emoji tag sequence.
        final int STATE_IN_TAG_SEQUENCE = 12;

        // The state machine has been stopped.
        final int STATE_FINISHED = 13;

        int deleteCharCount = 0;  // Char count to be deleted by backspace.
        int lastSeenVSCharCount = 0;  // Char count of previous variation selector.

        int state = STATE_START;

        int tmpOffset = offset;
        do {
            final int codePoint = Character.codePointAt(text, tmpOffset);
            tmpOffset += Character.charCount(codePoint);

            switch (state) {
                case STATE_START:
                    deleteCharCount = Character.charCount(codePoint);
                    if (codePoint == LINE_FEED) {
                        state = STATE_LF;
                    } else if (isVariationSelector(codePoint)) {
                        state = STATE_BEFORE_VS;
                    } else if (Emoji.isRegionalIndicatorSymbol(codePoint)) {
                        state = STATE_ODD_NUMBERED_RIS;
                    } else if (Emoji.isEmojiModifier(codePoint)) {
                        state = STATE_BEFORE_EMOJI_MODIFIER;
                    } else if (codePoint == Emoji.COMBINING_ENCLOSING_KEYCAP) {
                        state = STATE_BEFORE_KEYCAP;
                    } else if (Emoji.isEmoji(codePoint)) {
                        state = STATE_BEFORE_EMOJI;
                    } else if (codePoint == Emoji.CANCEL_TAG) {
                        state = STATE_IN_TAG_SEQUENCE;
                    } else {
                        state = STATE_FINISHED;
                    }
                    break;
                case STATE_LF:
                    if (codePoint == CARRIAGE_RETURN) {
                        ++deleteCharCount;
                    }
                    state = STATE_FINISHED;
                    break;
                case STATE_ODD_NUMBERED_RIS:
                    if (Emoji.isRegionalIndicatorSymbol(codePoint)) {
                        deleteCharCount += 2; /* Char count of RIS */
                        state = STATE_EVEN_NUMBERED_RIS;
                    } else {
                        state = STATE_FINISHED;
                    }
                    break;
                case STATE_EVEN_NUMBERED_RIS:
                    if (Emoji.isRegionalIndicatorSymbol(codePoint)) {
                        deleteCharCount -= 2; /* Char count of RIS */
                        state = STATE_ODD_NUMBERED_RIS;
                    } else {
                        state = STATE_FINISHED;
                    }
                    break;
                case STATE_BEFORE_KEYCAP:
                    if (isVariationSelector(codePoint)) {
                        lastSeenVSCharCount = Character.charCount(codePoint);
                        state = STATE_BEFORE_VS_AND_KEYCAP;
                        break;
                    }

                    if (Emoji.isKeycapBase(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint);
                    }
                    state = STATE_FINISHED;
                    break;
                case STATE_BEFORE_VS_AND_KEYCAP:
                    if (Emoji.isKeycapBase(codePoint)) {
                        deleteCharCount += lastSeenVSCharCount + Character.charCount(codePoint);
                    }
                    state = STATE_FINISHED;
                    break;
                case STATE_BEFORE_EMOJI_MODIFIER:
                    if (isVariationSelector(codePoint)) {
                        lastSeenVSCharCount = Character.charCount(codePoint);
                        state = STATE_BEFORE_VS_AND_EMOJI_MODIFIER;
                        break;
                    } else if (Emoji.isEmojiModifierBase(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint);
                    }
                    state = STATE_FINISHED;
                    break;
                case STATE_BEFORE_VS_AND_EMOJI_MODIFIER:
                    if (Emoji.isEmojiModifierBase(codePoint)) {
                        deleteCharCount += lastSeenVSCharCount + Character.charCount(codePoint);
                    }
                    state = STATE_FINISHED;
                    break;
                case STATE_BEFORE_VS:
                    if (Emoji.isEmoji(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint);
                        state = STATE_BEFORE_EMOJI;
                        break;
                    }

                    if (!isVariationSelector(codePoint) &&
                            UCharacter.getCombiningClass(codePoint) == 0) {
                        deleteCharCount += Character.charCount(codePoint);
                    }
                    state = STATE_FINISHED;
                    break;
                case STATE_BEFORE_EMOJI:
                    if (codePoint == Emoji.ZERO_WIDTH_JOINER) {
                        state = STATE_BEFORE_ZWJ;
                    } else {
                        state = STATE_FINISHED;
                    }
                    break;
                case STATE_BEFORE_ZWJ:
                    if (Emoji.isEmoji(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint) + 1;  // +1 for ZWJ.
                        state = Emoji.isEmojiModifier(codePoint) ?
                                STATE_BEFORE_EMOJI_MODIFIER : STATE_BEFORE_EMOJI;
                    } else if (isVariationSelector(codePoint)) {
                        lastSeenVSCharCount = Character.charCount(codePoint);
                        state = STATE_BEFORE_VS_AND_ZWJ;
                    } else {
                        state = STATE_FINISHED;
                    }
                    break;
                case STATE_BEFORE_VS_AND_ZWJ:
                    if (Emoji.isEmoji(codePoint)) {
                        // +1 for ZWJ.
                        deleteCharCount += lastSeenVSCharCount + 1 + Character.charCount(codePoint);
                        lastSeenVSCharCount = 0;
                        state = STATE_BEFORE_EMOJI;
                    } else {
                        state = STATE_FINISHED;
                    }
                    break;
                case STATE_IN_TAG_SEQUENCE:
                    if (Emoji.isTagSpecChar(codePoint)) {
                        deleteCharCount += 2; /* Char count of emoji tag spec character. */
                        // Keep the same state.
                    } else if (Emoji.isEmoji(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint);
                        state = STATE_FINISHED;
                    } else {
                        // Couldn't find tag_base character. Delete the last tag_term character.
                        deleteCharCount = 2;  // for U+E007F
                        state = STATE_FINISHED;
                    }
                    // TODO: Need handle emoji variation selectors. Issue 35224297
                    break;
                default:
                    throw new IllegalArgumentException("state " + state + " is unknown");
            }
        } while (tmpOffset < text.length() && state != STATE_FINISHED);

        return deleteCharCount;
    }

    public static boolean isVariationSelector(int codepoint) {
        return UCharacter.hasBinaryProperty(codepoint, UProperty.VARIATION_SELECTOR);
    }
}
