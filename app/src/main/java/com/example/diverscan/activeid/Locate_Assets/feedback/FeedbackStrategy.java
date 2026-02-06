package com.example.diverscan.activeid.Locate_Assets.feedback;

public interface FeedbackStrategy {
    /**
     * Inicia el feedback (ej. sonido, vibración).
     */
    void start();

    /**
     * Detiene el feedback.
     */
    void stop();

    /**
     * Actualiza la intensidad del feedback basado en la proximidad.
     * @param progress Valor de 0 (lejos) a 100 (cerca).
     */
    void update(int progress);

    /**
     * Libera recursos (ej. ToneGenerator).
     */
    void destroy();
}
