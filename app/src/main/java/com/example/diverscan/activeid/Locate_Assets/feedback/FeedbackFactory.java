package com.example.diverscan.activeid.Locate_Assets.feedback;

import android.content.Context;

public class FeedbackFactory {
    public enum FeedbackType {
        GEIGER_SOUND
        // Futuros tipos: VIBRATION, VISUAL_BLINK, etc.
    }

    public static FeedbackStrategy getFeedback(FeedbackType type, Context context) {
        switch (type) {
            case GEIGER_SOUND:
                return new GeigerSoundFeedback();
            default:
                return new GeigerSoundFeedback();
        }
    }
}
