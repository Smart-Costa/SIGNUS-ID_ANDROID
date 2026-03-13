package com.uk.tsl.rfid.samples.s3sensortagreader;

public interface ISensorConverter
{
    /**
     * Converts a code value into a real-world value
     * @param code the sensor code
     * @return the corresponding real world value
     */
    float valueFromCode(float code);

    /**
     * @return a User-facing representation of the converter
     */
    String getName();

    /**
     * @return a User-facing representation of the value's measurement unit
     */
    String getUnitDescription();
}
