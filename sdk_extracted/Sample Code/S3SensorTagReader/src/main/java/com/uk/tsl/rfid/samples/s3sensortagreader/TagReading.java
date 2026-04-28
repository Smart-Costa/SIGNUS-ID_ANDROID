package com.uk.tsl.rfid.samples.s3sensortagreader;

import android.util.Log;

import java.util.Locale;

/**
 * Stores the reading from a temperature sensor tag
 * Supports averaging multiple readings
 * A converter can be specified to provide temperature in Celsius from the code value.
 */
public class TagReading
{
    //public static float MINIMUM_TEMPERATURE = -273.15f;
    public static float INVALID_TEMPERATURE = -999.0f;
    public static float INVALID_SENSOR = -999.0f;

    public String getEPC()
    {
        return mEPC;
    }

    // The current number of samples for this reading
    public int getSampleCount() { return mSampleCount; }

    // The number of samples averaged for a temperature reading
    public int getSampleSize() { return mSampleSize; }

    // The last complete averaged temperature code, rounded to the nearest integer
    public int getTemperatureCode()
    {
        return mTemperatureCode;
    }

    // The last recorded Sensor Code value
    public int getSensorCode()
    {
        return mSensorCode;
    }

    // The RSSI for the last reading added
    public int getRSSI()
    {
        return mRSSI;
    }

    // The channel frequency for the last reading added
    public int getChannelFrequency()
    {
        return mChannelFrequency;
    }

    // The power level for the last reading added
    public int getPower()
    {
        return mPower;
    }

    // Returns the converted sensor value if a sensor converter has  been supplied
    // otherwise returns INVALID_TEMPERATURE
    public double getSensorValue()
    {
        return mSensorValue;
    }

    // Returns the converted temperature value if a temeperature converter has  been supplied
    // otherwise returns INVALID_TEMPERATURE
    public double getTemperatureValue()
    {
        return mTemperatureValue;
    }

    // An instance of a class for converting temperature codes into Celsius values
    public ISensorConverter getTemperatureConverter()
    {
        return mTemperatureConverter;
    }

    public void setTemperatureConverter(ISensorConverter converter)
    {
        mTemperatureConverter = converter;
    }

    // An instance of a class for converting sensor codes into real-world values
    public ICompensatedSensorConverter getSensorCodeConverter()
    {
        return mSensorConverter;
    }

    public void setSensorCodeConverter(ICompensatedSensorConverter converter)
    {
        mSensorConverter = converter;
    }


    public int getScansSinceLastResponse()
    {
        return mScansSinceLastResponse;
    }


    // The lower power limit recorded
    public int getMinimumRecordedPower() { return mMinimumRecordedPower; }
    // The lower power limit recorded
    public int getMaximumRecordedPower() { return mMaximumRecordedPower; }


    /**
     * Construct an instance identified by the given EPC
     * @param EPC the EPC identifier for this reading
     * @param samplesToAverage the number of samples to average the Code over
     */
    public TagReading(String EPC, int samplesToAverage)
    {
        mEPC = EPC;
        mSampleSize = samplesToAverage;

        mTemperatureCode = 0;
        mRSSI = 0;
        mChannelFrequency = 0;
        mPower = 0;

        mSensorValue = INVALID_SENSOR;
        mTemperatureValue = INVALID_TEMPERATURE;

        mAccumulatedSensorCode = 0;
        mAccumulatedTemperatureCode = 0;

        mSampleCount = 0;
        mTemperatureConverter = null;
        mSensorConverter = null;
        mScansSinceLastResponse = 0;

        mMinimumRecordedPower = Integer.MAX_VALUE;
        mMaximumRecordedPower = Integer.MIN_VALUE;
    }


    /**
     *
     * @param rssi the tag's returned RSSI value
     */
    public void update(int rssi)
    {
        mRSSI = rssi;
    }

    /**
     *
     * @param rssi the tag's returned RSSI value
     * @param frequency the channel frequency used when scanning the tag
     */
    public void update(int rssi, int frequency, int power)
    {
        mRSSI = rssi;
        mChannelFrequency = frequency;
        mPower = power;
    }

    /**
     * Update when only Sensor Code is required
     * @param sensorCode the tag's Sensor Code value
     * @param rssi the tag's returned RSSI value
     * @param frequency the channel frequency used when scanning the tag
     */
    public void update(int sensorCode, int rssi, int frequency, int power)
    {
//        mRSSI = rssi;
//        mChannelFrequency = frequency;
//        mPower = power;
//        mSensorCode = sensorCode;
//        // Convert, if possible
//        if( mSensorConverter != null )
//        {
//            mSensorValue = mSensorConverter.valueFromCode(mSensorCode, mChannelFrequency, mPower);
//        }
//        mScansSinceLastResponse = 0;

        update((int) INVALID_TEMPERATURE, sensorCode, rssi, frequency, power);
    }


    /**
     * Updates the reading with a new valid temperature sample
     * @param temperatureCode the tag's Temperature Code value
     * @param sensorCode the tag's Sensor Code value
     * @param rssi the tag's returned RSSI value
     * @param frequency the channel frequency used when scanning the tag
     * @return true if this updated the Temperature property
     */
    public boolean update(int temperatureCode, int sensorCode, int rssi, int frequency, int power)
    {
        boolean isNewValueReady = false;

        mScansSinceLastResponse = 0;
        mPower = power;
        if( mMinimumRecordedPower > mPower ) mMinimumRecordedPower = mPower;
        if( mMaximumRecordedPower < mPower ) mMaximumRecordedPower = mPower;
        Log.d("TR", String.format(Locale.US, "Power Range( %d, %d)", mMinimumRecordedPower, mMaximumRecordedPower));
        if(mChannelFrequency == 0 ) mChannelFrequency = frequency;

        mSensorCode = sensorCode;
        mRSSI = rssi;
        mChannelFrequency = frequency;

        float sCode = sensorCode;
        if( mSensorConverter != null )
        {
            sCode = mSensorConverter.normalisedCodeFrom(mSensorCode, mChannelFrequency, mPower);
        }
        mAccumulatedSensorCode += sCode;
        mAccumulatedTemperatureCode += temperatureCode;

        // Adjust the averaged code values
        mSampleCount += 1;
        if( mSampleCount >= mSampleSize)
        {
            isNewValueReady = true;

            // Calculate the averaged code value
            float sAverage = mAccumulatedSensorCode / (float) mSampleCount;
            mSensorValue = (int)(sAverage + 0.5f);
            if( mSensorConverter != null )
            {
                mSensorValue = mSensorConverter.valueFromCode(mSensorValue, mChannelFrequency);
            }



            // Calculate the averaged code value
            if( temperatureCode != (int)INVALID_TEMPERATURE )
            {
                float tAverage = mAccumulatedTemperatureCode / (float) mSampleCount;
                mTemperatureCode = (int) (tAverage + 0.5f);

                // Convert, if possible
                if (mTemperatureConverter != null)
                {
                    mTemperatureValue = mTemperatureConverter.valueFromCode(mTemperatureCode);
                }
            }

            mSampleCount = 0;
            mAccumulatedSensorCode = 0;
            mAccumulatedTemperatureCode = 0;
        }
        return isNewValueReady;
    }

    /**
     *  Call this to indicate that the tag was not seen this scan
     */
    public void notSeen()
    {
        mScansSinceLastResponse += 1;
    }

    //
    // Private variables
    //
    private String mEPC;

    /**
     * @return the TagReading's current message
     */
    public String getMessage()
    {
        return mMessage;
    }

    /**
     * Sets the TagReading's message
     * @param message an arbitrary message
     */
    public void setMessage(String message)
    {
        mMessage = message;
    }

    private String mMessage;
    private int mTemperatureCode;
    private int mSensorCode;
    private int mRSSI;
    private int mChannelFrequency;
    private int mPower;
    private float mSensorValue = INVALID_SENSOR;
    private float mTemperatureValue = INVALID_TEMPERATURE;
    private float mAccumulatedSensorCode;
    private float mAccumulatedTemperatureCode;

    private int mSampleSize = 1;
    private int mSampleCount = 1;
    private ISensorConverter mTemperatureConverter = null;
    private ICompensatedSensorConverter mSensorConverter = null;

    private int mScansSinceLastResponse;

    private int mMinimumRecordedPower;
    private int mMaximumRecordedPower;

}
