package com.uk.tsl.rfid.samples.s3sensortagreader;

public interface ICompensatedSensorConverter extends ISensorConverter
{
    /**
     * Adjusts a code value compensating for the scan frequency and power
     * to return a value equivalent to a known, fixed frequency and power level
     * @param code the sensor code
     * @param frequency the RF frequency at which the sensor code was obtained
     * @param power the reader power at which the sensor code was obtained
     * @return the normalised code value
     */
    float normalisedCodeFrom(int code, int frequency, int power);

    /**
     * Converts a code value sampled at the given frequency
     * @param code the sensor code
     * @param frequency the RF frequency at which the sensor code was obtained
     * @return the corresponding real world value
     */
    float valueFromCode(float code, int frequency);

}
