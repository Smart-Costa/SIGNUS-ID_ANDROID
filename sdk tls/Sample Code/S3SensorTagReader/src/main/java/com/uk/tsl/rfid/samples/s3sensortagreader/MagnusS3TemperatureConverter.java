package com.uk.tsl.rfid.samples.s3sensortagreader;

import com.crccalc.CrcCalculator;
import com.uk.tsl.rfid.ModelException;
import com.uk.tsl.utils.HexEncoding;

import java.util.Locale;

import static com.crccalc.Crc16.Crc16Genibus;

//
// Calibrator for Temperature Sensor tags
class MagnusS3TemperatureConverter implements ISensorConverter
{
    public float valueFromCode(float code)
    {
        float temperature;

        temperature = (gradient * (code - code1) + temp1 - 800)/10;

        return temperature;
    }

    public String getName()
    {
        return "Magnus S3 Temperature";
    }
    public String getUnitDescription() { return "C"; }

    public int code1() { return code1; };
    public int code2() { return code2; };

    public int temp1() { return temp1; };
    public int temp2() { return temp2; };

    /**
     *
     * Creates a new convertor for the given calibration
     *
     * @param calibrationData the 4 words of calibration data
     */
    public MagnusS3TemperatureConverter(byte[] calibrationData ) throws ModelException
    {
        // Extract CRC
        int b0 = (int)calibrationData[0] & 0xff;
        int b1 = (int)calibrationData[1] & 0xff;
        int crc = ((b0 << 8) + b1)  & 0xffff;

        // Verify data
        int calculatedCrc = (int)crc16.Calc(calibrationData, 2, 6);
        if( crc != calculatedCrc)
        {
            String msg = String.format(Locale.US,
                    "CRC Error - Data: %s, Expected: %4X, Calculated: %4X",
                    HexEncoding.bytesToString(calibrationData),
                    crc,
                    calculatedCrc);
            throw new ModelException(msg);
        }

        // Extract conversion parameters
        int b2 = (int)calibrationData[2] & 0xff;
        int b3 = (int)calibrationData[3] & 0xff;
        int b4 = (int)calibrationData[4] & 0xff;
        int b5 = (int)calibrationData[5] & 0xff;
        int b6 = (int)calibrationData[6] & 0xff;
        int b7 = (int)calibrationData[7] & 0xff;

        code1 = (b2 << 4) + ((b3 >> 4) & 0x0f);
        temp1 = ((b3 & 0x0f) << 7) + ((b4 & 0xfe) >> 1);
        code2 = ((b4 & 0x01) << 11) + (b5 << 3) + ((b6 & 0xe0) >> 5);
        temp2 = ((b6 & 0x1f) << 6) + ((b7 & 0xfc) >> 2);

        // Pre-calculate linear gradient
        gradient = (temp2 - temp1) / (float)(code2 -code1);
    }

    private int code1;
    private int code2;

    private int temp1;
    private int temp2;

    private float gradient;
    private final CrcCalculator crc16 = new CrcCalculator(Crc16Genibus);
}
