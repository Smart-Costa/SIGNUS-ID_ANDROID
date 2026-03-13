package com.uk.tsl.rfid.samples.s3sensortagreader;

/**
 * This class provides a converter that simply returns the raw value
 */
public class NullSensorConverter implements ICompensatedSensorConverter
{
    @Override
    public float normalisedCodeFrom(int code, int frequency, int power)
    {
        return (float)code;
    }

    @Override
    public float valueFromCode(float code, int frequency)
    {
        return (float)code;
    }

    @Override
    public float valueFromCode(float code)
    {
        return (float)code;
    }

    @Override
    public String getName()
    {
        return "Raw Sensor Code";
    }

    @Override
    public String getUnitDescription()
    {
        return "";
    }
}
