package com.example.diverscan.activeid.Locate_Assets.feedback;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;

/**
 * Implementación del feedback tipo Geiger (sonido más rápido al acercarse).
 */
public class GeigerSoundFeedback implements FeedbackStrategy {
    private ToneGenerator toneGenerator;
    private Handler soundHandler;
    private Runnable soundRunnable;
    private boolean isRunning = false;
    private volatile int soundInterval = 1000; // Intervalo en ms

    public GeigerSoundFeedback() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        soundHandler = new Handler(Looper.getMainLooper());
        
        soundRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && toneGenerator != null) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50); // Duración del beep: 50ms
                    soundHandler.postDelayed(this, soundInterval);
                }
            }
        };
    }

    @Override
    public void start() {
        if (!isRunning && toneGenerator != null) {
            isRunning = true;
            soundHandler.post(soundRunnable);
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        if (soundHandler != null) {
            soundHandler.removeCallbacks(soundRunnable);
        }
    }

    @Override
    public void update(int progress) {
        // Lógica Geiger: Mayor progreso (cerca) -> Menor intervalo (más rápido)
        // Progress 0 -> 1000ms
        // Progress 100 -> 100ms
        
        int newInterval = 1000 - (progress * 9);
        if (newInterval < 50) newInterval = 50; // Límite inferior para evitar solapamiento
        
        this.soundInterval = newInterval;
    }

    @Override
    public void destroy() {
        stop();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
