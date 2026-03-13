package com.uk.tsl.rfid.samples.s3sensortagreader;

import android.util.Log;

/**
 * An example class for Linear conversions of raw sensor code, compensating for both power and frequency variations 
 */
public class LinearSensorConverter implements ICompensatedSensorConverter
{
    // Debug tag
    private static String TAG = "LSC";

    @Override
    public String getName()
    {
        return "Linear Example Converter";
    }

    public String getUnitDescription() { return "%"; }


    @Override
    public float normalisedCodeFrom(int code, int frequency, int power)
    {
        // Check for raw sensor code in valid range e.g. 5 - 490
        if( code < 5 || code > 490 || frequency == 0)
        {
            return TagReading.INVALID_SENSOR;
        }

        String region = regionFrom(frequency);

        float normalisedSensorValue = code;

        // Normalisation (linear)
        // Derived from empirical testing
        if( region == etsiRegion )
        {
            // Power normalisation
            normalisedSensorValue = code - (power - REF_POWER_ETSI) * POWER_GRADIENT_ETSI;

            // Frequency normalisation
            normalisedSensorValue = normalisedSensorValue - (frequency - REF_FREQUENCY_ETSI) * FREQUENCY_GRADIENT_ETSI;
        }
        else if( region == fccRegion )
        {
            // Power normalisation
            normalisedSensorValue = code - (power - REF_POWER_FCC) * POWER_GRADIENT_FCC;

            // Frequency normalisation
            normalisedSensorValue = normalisedSensorValue - (frequency - REF_FREQUENCY_FCC) * FREQUENCY_GRADIENT_FCC;
        }
        else
        {
            return TagReading.INVALID_SENSOR;
        }

        Log.d(TAG, String.format("Code: %5d, Normalised: %3.1f, Frequency: %6d, Power: %2d", code, normalisedSensorValue, frequency, power));

        return normalisedSensorValue;
    }


    /**
     * !!! Conversion is frequency dependent !!!
     * @param code the sensor code
     * @return humidity or TagReading.INVALID_SENSOR when out of valid range
     */
    public float valueFromCode(float code)
    {
        return TagReading.INVALID_SENSOR;
    }

    /**
     * Returns value 0.0 - 100.0 representing %age humidity
     * @param code the sensor code
     * @return humidity or TagReading.INVALID_SENSOR when out of valid range
     */
    public float valueFromCode(float code, int frequency)
    {
        String region = regionFrom(frequency);

        float value;

        // Different linear mappings for ETSI & FCC
        // Derived from empirical testing
        if( region == etsiRegion )
        {
            // Enforce valid range
            if( code < SENSOR_CONVERTER_LOWER_LIMIT_ETSI )
            {
                Log.d(TAG, String.format("!!! Capping Code: %3.1f, Frequency: %6d", code, frequency));

                code = SENSOR_CONVERTER_LOWER_LIMIT_ETSI;
            }
            else if( code > SENSOR_CONVERTER_UPPER_LIMIT_ETSI)
            {
                Log.d(TAG, String.format("!!! Capping Code: %3.1f, Frequency: %6d", code, frequency));

                code = SENSOR_CONVERTER_UPPER_LIMIT_ETSI;
            }

            value = (code - SENSOR_CONVERTER_INTERCEPT_ETSI) / (SENSOR_CONVERTER_GRADIENT_ETSI);
        }
        else if( region == fccRegion )
        {
            // Enforce valid range
            if( code < SENSOR_CONVERTER_LOWER_LIMIT_FCC )
            {
                Log.d(TAG, String.format("!!! Capping Code: %3.1f, Frequency: %6d", code, frequency));

                code = SENSOR_CONVERTER_LOWER_LIMIT_FCC;
            }
            else if( code > SENSOR_CONVERTER_UPPER_LIMIT_FCC)
            {
                Log.d(TAG, String.format("!!! Capping Code: %3.1f, Frequency: %6d", code, frequency));

                code = SENSOR_CONVERTER_UPPER_LIMIT_FCC;
            }

            value = (code - SENSOR_CONVERTER_INTERCEPT_FCC) / (SENSOR_CONVERTER_GRADIENT_FCC);
        }
        else
        {
            value = TagReading.INVALID_SENSOR;
        }

        return value;
    }

    //
    // Coefficients to convert the normalised sensor values to real world values
    //

    // ETSI coefficients
    // These are example values.
    // Make real-world measurements to determine the correct linear coefficients for your sensor
    static private final float SENSOR_CONVERTER_INTERCEPT_ETSI = 288.0f;
    static private final float SENSOR_CONVERTER_GRADIENT_ETSI = -2.83f;
    static private final float SENSOR_CONVERTER_LOWER_LIMIT_ETSI = 5.0f;
    static private final float SENSOR_CONVERTER_UPPER_LIMIT_ETSI = 288.0f;

    // FCC coefficients
    // These are example values.
    // Make real-world measurements to determine the correct linear coefficients for your sensor
    static private final float SENSOR_CONVERTER_INTERCEPT_FCC = 300.0f;
    static private final float SENSOR_CONVERTER_GRADIENT_FCC = -4.0f;
    static private final float SENSOR_CONVERTER_LOWER_LIMIT_FCC = 5.0f;
    static private final float SENSOR_CONVERTER_UPPER_LIMIT_FCC = 290.0f;


    //
    // Coefficients used to adjust sensor values to a known, fixed frequency and power
    //

    // ETSI coefficients
    // Constants used to adjust for variations across the power range
    // Example values for flat response - derive actual values from empirical testing
    static private final int REF_POWER_ETSI = 16;
    static private final float POWER_GRADIENT_ETSI = 0.0f;

    // Constants used to adjust for variations across the frequency range
    // Example values for linear response - derive actual values from empirical testing
    static private final int REF_FREQUENCY_ETSI = 866600;
    static private final float FREQUENCY_GRADIENT_ETSI = -0.003f;

    // FCC coefficients
    // Constants used to adjust for variations across the power range
    // Example values for flat response - derive actual values from empirical testing
    static private final int  REF_POWER_FCC = 16;
    static private final float POWER_GRADIENT_FCC = 0.0f;

    // Constants used to adjust for variations across the frequency range
    // Example values for linear response - derive actual values from empirical testing
    static private final int REF_FREQUENCY_FCC = 914500;
    static private float FREQUENCY_GRADIENT_FCC = -0.003f;





    private static String regionFrom(int frequency)
    {
        if( frequency < etsiMinimumFrequency || frequency > fccMaximumFrequency) return null;

        if( frequency <= etsiMaximumFrequency ) return etsiRegion;
        else if(frequency >= fccMinimumFrequency ) return fccRegion;
        else return null;
    }

    private static String etsiRegion = "EU";
    private static int etsiMinimumFrequency = 865700;
    private static int etsiMaximumFrequency = 867500;

    private static String fccRegion = "FCC";
    private static int fccMinimumFrequency = 902750;
    private static int fccMaximumFrequency = 927250;
}
