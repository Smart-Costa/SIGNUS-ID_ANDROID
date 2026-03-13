//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.uk.tsl.rfid.samples.s3sensortagreader;


import android.os.Message;
import android.util.Log;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.ModelException;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.SwitchActionCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.TransponderMemoryCommandBase;
import com.uk.tsl.rfid.asciiprotocol.commands.TransponderSelectCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchState;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ISwitchStateReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.SwitchResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.uk.tsl.rfid.samples.s3sensortagreader.TagReading.INVALID_TEMPERATURE;

public class SensorTagModel extends ModelBase
{
    // Debugging
    private static final String TAG = "SensorTagModel";
    private static final boolean D = BuildConfig.DEBUG;

    // Sent when there is an update to the current TagReadings
    public static final int READING_UPDATE_NOTIFICATION = 3;
    // Sent when there is an update to the power level being used
    public static final int POWER_CHANGE_NOTIFICATION = 4;


    private volatile Boolean mIsRunning = false;

    // The tag readings indexed by EPC
    private HashMap<String, Integer> mTagReadingIndex = new HashMap<>();

    // The transponders that have on-chip RSSI within the specified range (from UI)
    HashMap<String, TransponderData> mInRangeTransponders = new HashMap<>();

    private Boolean mTagReadingsModified = false;
    private Boolean mDidReceiveData = false;
    private byte[] mDataReceived = null;
    private int mCurrentPower = 0;
    private boolean mIsSweepIncreasing;
    private int mTotalTagsSeen = 0;
    private int mValidTagsSeenLastPass = 0;
    private int mUnderPoweredTagsSeen = 0;
    private int mOverPoweredTagsSeen = 0;


    // The switch state responder
    private SwitchResponder mSwitchResponder;

    // The collection of Tag Readings
    public ArrayList<TagReading> getTagReadings()
    {
        return mTagReadings;
    }
    private ArrayList<TagReading> mTagReadings = new ArrayList<>();

    public int getMinPower()
    {
        return mMinPower;
    }
    public void setMinPower(int minPower)
    {
        mMinPower = minPower;
    }
    private int mMinPower;

    public int getMaxPower()
    {
        return mMaxPower;
    }
    public void setMaxPower(int maxPower)
    {
        mMaxPower = maxPower;
    }
    private int mMaxPower;

    public int getMinRSSI()
    {
        return mMinRSSI;
    }
    public void setMinRSSI(int minRSSI)
    {
        mMinRSSI = minRSSI;
    }
    private int mMinRSSI;

    public int getMaxRSSI()
    {
        return mMaxRSSI;
    }
    public void setMaxRSSI(int maxRSSI)
    {
        mMaxRSSI = maxRSSI;
    }
    private int mMaxRSSI;

    public int getSamplesPerTag()
    {
        return mSamplesPerTag;
    }
    public void setSamplesPerTag(int samplesPerTag)
    {
        mSamplesPerTag = samplesPerTag;
    }
    private int mSamplesPerTag;

    // True if the User is scanning
    public boolean isScanning() { return mScanning;}
    public void setScanning(boolean scanning) { mScanning = scanning; }
    private boolean mScanning = false;

    // True if Temperature tag reading is required
    public boolean isTemperatureRequired() { return mTemperatureRequired; }
    public void setTemperatureRequired(boolean required) { mTemperatureRequired = required; }
    private boolean mTemperatureRequired = true;

    private static SensorTagModel sSharedModel;

    public static SensorTagModel sharedInstance()
    {
        if( sSharedModel == null ) { sSharedModel = new SensorTagModel(); }

        return sSharedModel;
    }

    // Control
    public boolean isEnabled() { return mEnabled; }

    public void setEnabled(boolean state)
    {
        boolean oldState = mEnabled;
        mEnabled = state;

        // Update the commander for state changes
        if(oldState != state)
        {
            if( mEnabled )
            {
                // Listen for trigger
                getCommander().addResponder( mSwitchResponder);

            }
            else
            {
                // Stop Scanning
                setScanning(false);
                stopContinuousSensorReading();

                // Stop listening for trigger
                getCommander().addResponder( mSwitchResponder);
            }
        }
    }
    private boolean mEnabled;

    /**
	 * A class to demonstrate the Series 3 Reader's support for temperature sensor tags based on the Magnus S3 sensor
	 */
	public SensorTagModel()
	{
        mMinPower = 15;
        mMaxPower = 30;

        // These defaults are derived from the Axzon Application Note AN006 (Rev 3) Sensor and Temperature Measurements
        // Chosen values are optimal for both Temperature and Sensor Code on the Magnus-S3 chips
        // We are using a slightly tighter range than that recommended for maximum accuracy (13,17)
        mMinRSSI = 14;
        mMaxRSSI = 16;

        mSamplesPerTag = 1;

        mSwitchResponder = new SwitchResponder();
        mSwitchResponder.setSwitchStateReceivedDelegate(new ISwitchStateReceivedDelegate() {
            @Override
            public void switchStateReceived(SwitchState switchState)
            {
                try
                {
                    // When trigger released
                    if (switchState == SwitchState.OFF)
                    {
                    }
                    else if (switchState == SwitchState.SINGLE)
                    {
                        setScanning(!isScanning());
                        if (isScanning())
                        {
                            startContinuousSensorReading();
                        }
                        else
                        {
                            stopContinuousSensorReading();
                        }
                    }
                }
                catch( Exception e)
                {
                    if (D) Log.d(TAG, String.format("Exception: %s", e.getMessage()));
                }
            }
        });
	}


    /**
     * Pause for the given duration
     * @param duration (ms)
     * @return the given duration (ms)
     */
    public long pause(long duration)
    {
        try { Thread.sleep(duration); } catch (InterruptedException e) { e.printStackTrace(); }
        return duration;
    }



    /**
	 * Check the given command for errors and report them via the model message system
	 * @param command The command to check
	 */
	private void reportErrors(AsciiSelfResponderCommandBase command)
	{
		if( !command.isSuccessful() ) {
			sendMessageNotification(String.format(
					"%s failed!\nError code: %s\n", command.getClass().getSimpleName(), command.getErrorCode()));
			for (String message : command.getMessages()) {
				sendMessageNotification(message + "\n");
			}
		}

	}


    //
    // Reset the reader configuration to default command values
    //
    public void resetReader()
    {
        if(getCommander().isConnected())
        {
            try
            {
                performTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getCommander().executeCommand(new FactoryDefaultsCommand());

                        sendMessageNotification("\nReader Reset.\n");
                    }
                });

            }
            catch (ModelException e)
            {
                sendMessageNotification("Unable to perform action: " + e.getMessage());
            }
        }
    }

    //
    // Set the reader configuration
    //
    public void configureReader()
    {
        if(getCommander().isConnected() && isEnabled())
        {
            try
            {
                sendMessageNotification("\nInitialising reader...\n");

                performTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getCommander().executeCommand(new FactoryDefaultsCommand());

                        // Configure the switch actions
                        SwitchActionCommand switchActionCommand = SwitchActionCommand.synchronousCommand();
                        switchActionCommand.setResetParameters(TriState.YES);

                        switchActionCommand.setSinglePressAction(SwitchAction.OFF);
                        switchActionCommand.setDoublePressAction(SwitchAction.OFF);

                        switchActionCommand.setAsynchronousReportingEnabled(TriState.YES);

                        mCommander.executeCommand(switchActionCommand);

                        sendMessageNotification("\nDone.\n");
                    }
                });

            }
            catch (ModelException e)
            {
                sendMessageNotification("Unable to perform action: " + e.getMessage());
            }
        }
    }




    // Target using TID
    private void targetForCalibrationData(TransponderMemoryCommandBase command)
    {
        // Select for Axzon tag types
        command.setSelectData("82403D");
        command.setSelectOffset(0x08);
        command.setSelectLength(20);// Ignore last digit
        command.setSelectBank(Databank.TRANSPONDER_IDENTIFIER);

        command.setBank(Databank.USER);
        command.setOffset(0x08);

        // Reading 4 words of calibration data
        command.setLength(4);
    }


    // Target using EPC
    private void targetForCalibrationData(TransponderMemoryCommandBase command, String epc)
    {
        // Select for specific tag
        command.setSelectData(epc);
        command.setSelectOffset(0x20);
        command.setSelectLength(epc.length() * 4);
        command.setSelectBank(Databank.ELECTRONIC_PRODUCT_CODE);

        command.setBank(Databank.USER);
        command.setOffset(0x08);

        // Reading 4 words of calibration data
        command.setLength(4);
    }

    /**
     * Reads S3 sensor tags using two separate read commands:
     *  - First to read the RSSI and SensorCode
     *  - Second to red the Temperature Code, if required
     * @throws ModelException
     */
    private void continuousSensorReading() throws ModelException
    {
        //
        // Use select to filter tags and obtain on-tag RSSI and Sensor code simultaneously
        //
        // Select for tags below the maximum possible RSSI level
        //
        // This (mostly) restricts the responses to Magnus S3 tags.
        // By allowing all RSSI levels, it is possible to respond to both too low and too high RSSI
        //
        // Refer to Axzon Application Note AN002 for more details
        //

        // Select for RSSI <= 31
//        int rssiMaskLevel = 0x1f;
        int rssiMaskLevel = mMaxRSSI & 0x1f; // Swap this line for the above to restrict to tags below the Max RSSI limit

        String rssiMaskString = String.format(Locale.US, "%2x", rssiMaskLevel);

        ReadTransponderCommand readRSSICommand = ReadTransponderCommand.synchronousCommand();
        readRSSICommand.setResetParameters(TriState.YES);

        readRSSICommand.setIncludeChannelFrequency(TriState.YES);

        readRSSICommand.setUseAlert(TriState.NO);
        // Select for RFMicron sensor tag RSSI
        readRSSICommand.setSelectOffset(0x00d0);
        readRSSICommand.setSelectLength(8);
        readRSSICommand.setSelectData(rssiMaskString);
        readRSSICommand.setSelectBank(Databank.USER);

        readRSSICommand.setBank(Databank.RESERVED);
        // Offset for SensorCode, RSSI follows
        readRSSICommand.setOffset(0xC);
        readRSSICommand.setLength(2);

//        readRSSICommand.setQValue(1);

        readRSSICommand.setResponseLifecycleDelegate(new ICommandResponseLifecycleDelegate() {
            @Override
            public void responseBegan() {}

            @Override
            public void responseEnded(){}
        });

        readRSSICommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable)
            {
                String epc = transponder.getEpc();

                Integer frequency = transponder.getChannelFrequency();
                if( epc != null )
                {
                    byte[] data = transponder.getReadData();
                    if (data != null && data.length > 0)
                    {
                        mTotalTagsSeen += 1;

                        // Extract RSSI
                        int d3 = (int) data[3];
                        int rssi = (data[3] & 0x1f);

                        // Ensure the changes will be seen
                        mTagReadingsModified = true;

                        // Has the tag been seen before
                        TagReading reading;
                        if (mTagReadingIndex.containsKey(epc))
                        {
                            reading = mTagReadings.get(mTagReadingIndex.get(epc));
                        }
                        else
                        {
                            // Create new reading and append to existing
                            reading = appendReading(epc, mSamplesPerTag);
                        }


                        // Is the RSSI in the valid range
                        if (rssi < mMinRSSI)
                        {
                            mUnderPoweredTagsSeen += 1;
                            // Mark as too low
                            reading.setMessage("RSSI too low!");
                        }
                        else if (rssi > mMaxRSSI)
                        {
                            mOverPoweredTagsSeen += 1;
                            // Mark as too high
                            reading.setMessage("RSSI too high!");
                        }
                        else
                        {
                            // tag's RSSI is in the valid range -
                            mInRangeTransponders.put(epc, transponder);
                            reading.setMessage("");
                        }
                        if( isTemperatureRequired() )
                        {
                            // Record the RSSI and frequency, later commands will retrieve additional data
                            reading.update(rssi, frequency != null ? frequency : 0, mCurrentPower);
                        }
                        else
                        {
                            // Only interetsed in the Sensor Code
                            // Get the Sensor code data collected
                            String sensorCodeString = "";
                            int sensorCode = 0;
                            String srData = HexEncoding.bytesToString(data);
                            sensorCodeString = srData.substring(0, 4);

                            sensorCode = Integer.parseInt(sensorCodeString, 16);
                            //rssi = Integer.parseInt(srData.substring(4), 16);

                            // Record Sensor Code, RSSI and frequency
                            reading.update(sensorCode, rssi, frequency != null ? frequency : 0, mCurrentPower);
                        }
                    }
                }
            }
        });


        // Read command to access the Temperature code
        ReadTransponderCommand readTemperatureCommand = ReadTransponderCommand.synchronousCommand();
        readTemperatureCommand.setResetParameters(TriState.YES);


        // Select for RFMicron sensor tag RSSI
        readTemperatureCommand.setSelectOffset(0x00E0);
        readTemperatureCommand.setSelectLength(0);
        readTemperatureCommand.setSelectBank(Databank.USER);

        readTemperatureCommand.setUseAlert(TriState.NO);

        readTemperatureCommand.setBank(Databank.RESERVED);
        // Offset for RSSI, Temperature follows
        readTemperatureCommand.setOffset(0xE);
        readTemperatureCommand.setLength(1);

        // The pause after select command is only available on the 3-series readers
        // A value of 2 (ms) may be sufficient due to existing delays between Select and the first Query Rep
        // but using 3 (ms) guarantees that the AN006 (Rev 3) command timing requirement is met.
        readTemperatureCommand.setSelectPause(3);

        readTemperatureCommand.setResponseLifecycleDelegate(new ICommandResponseLifecycleDelegate() {
            @Override
            public void responseBegan() { mDidReceiveData = false; }

            @Override
            public void responseEnded(){}
        });

        readTemperatureCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable)
            {
                String epc = transponder.getEpc();

                // Ignore tags that are out of the requested RSSI range
                if (epc == null || !mInRangeTransponders.containsKey(epc))
                {
                    return;
                }

                String eaMsg = transponder.getAccessErrorCode() == null ? "" : transponder.getAccessErrorCode().getDescription();
                String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : transponder.getBackscatterErrorCode().getDescription();

                if(!eaMsg.isEmpty() || !ebMsg.isEmpty())
                {
                    // Ignore any errors - assume other scan will work
                    return;
                }

                // Interpret data
                byte[] data = transponder.getReadData();
                String dataString = null;
                float temperature = INVALID_TEMPERATURE;
                String temperatureString;
                if( data != null && data.length > 0)
                {
                    dataString = HexEncoding.bytesToString(data);

                    int b0 = (int)data[0] & 0xff;
                    int b1 = (int)data[1] & 0xff;
                    int temperatureCode = (b0 << 8) + b1;

                    int readingIndex = mTagReadingIndex.get(epc);
                    TagReading reading = mTagReadings.get(readingIndex);

                    ISensorConverter converter = reading.getTemperatureConverter();
                    if( converter != null)
                    {
                        // Convert Temperature Code into temperature
                        MagnusS3TemperatureConverter tc = (MagnusS3TemperatureConverter) converter;
                        temperature = tc.valueFromCode(temperatureCode);

                        // Sanity check the value (based on Axzon AZN3200-AAR datasheet)
                        if( temperature < -40.0f || temperature > 125.0f)
                        {
                            // Use a special value to erroneous reading
                            temperature = INVALID_TEMPERATURE;
                            temperatureString = "<!>";
                        }
                        else
                        {
                            temperatureString = String.format(Locale.US, "%6.2f", temperature);
                        }
                    }
                    else
                    {
                        temperatureString = "?";
                    }

                    // Get the RSSI data, previously collected
                    String sensorCodeString = "";
                    int rssi = 0;
                    int sensorCode = 0;
                    TransponderData td = mInRangeTransponders.get(epc);
                    String srData = HexEncoding.bytesToString(td.getReadData());
                    sensorCodeString = srData.substring(0, 4);

                    sensorCode = Integer.parseInt(sensorCodeString, 16);
                    rssi = Integer.parseInt(srData.substring(4), 16);

                    reading.update(temperatureCode, sensorCode, rssi, reading.getChannelFrequency(), mCurrentPower);
                }
            }
        });


        resetPowerSweep(true);
        readRSSICommand.setOutputPower((mCurrentPower));
        readTemperatureCommand.setOutputPower(mCurrentPower);

        mIsRunning = true;

        int emptyScanCount = 0;
        int maxPowerEmptyScanCount = 0;

        // Adjust power after this many scans without valid tags
        final int emptyScanLimit = 1;

        // Reset the scanning after this many scans, at full power, without valid tags
        final int emptyScanLimitAtMaxPower = 8;

        mValidTagsSeenLastPass = 0;

        do
        {
            mTagReadingsModified = false;
            mInRangeTransponders.clear();
            mTotalTagsSeen = 0;
            mUnderPoweredTagsSeen = 0;
            mOverPoweredTagsSeen = 0;

            // Assume tags will not be seen
            // If they are then the update() method will correct this assumption
            for (TagReading reading : mTagReadings )
            {
                reading.notSeen();
            }

            // Scan for transponders with on-chip RSSI in correct range
            getCommander().executeCommand(readRSSICommand);

            String status = String.format("Valid: %d, Under: %d, Over: %d",
                    mInRangeTransponders.keySet().size(),
                    mUnderPoweredTagsSeen,
                    mOverPoweredTagsSeen
            );
            Log.d(TAG, status);

            // Simple continuous sweep from low to high
            mCurrentPower++;
            if( mCurrentPower >= getMaxPower() )
            {
                mCurrentPower = getMinPower();
            }
            sendPowerChangeNotification(mCurrentPower);

            readRSSICommand.setOutputPower((mCurrentPower));
            readTemperatureCommand.setOutputPower(mCurrentPower);

            if(mInRangeTransponders.isEmpty())
            {
                emptyScanCount += 1;

//                // Adjust the power to bring tags into the valid RSSI range
//                if( emptyScanCount >= emptyScanLimit)
//                {
//                    emptyScanCount = 0;
//
//                    // Adjust power based on RSSI response of tags seen, if any.
//                    if( mUnderPoweredTagsSeen == 0 && mOverPoweredTagsSeen > 0 && mCurrentPower > getMinPower() )
//                    {
//                        mCurrentPower -= 1;
//                    }
//                    else if((mTotalTagsSeen == 0 || mOverPoweredTagsSeen == 0 && mUnderPoweredTagsSeen > 0) && mCurrentPower < getMaxPower())
//                    {
//                        mCurrentPower += 1;
//                    }
//                    else if( mCurrentPower >= getMaxPower())
//                    {
//                        maxPowerEmptyScanCount += 1;
//                        if( maxPowerEmptyScanCount >= emptyScanLimitAtMaxPower )
//                        {
//                            // Reset sweep
//                            resetPowerSweep(false);
//                            maxPowerEmptyScanCount = 0;
//                        }
//                    }
//                    sendPowerChangeNotification(mCurrentPower);
//
//                    readRSSICommand.setOutputPower((mCurrentPower));
//                    readTemperatureCommand.setOutputPower(mCurrentPower);
//                }
            }
            else
            {
//                // Try to maximise the power when seeing valid tags
//                if(mOverPoweredTagsSeen == 0 && mUnderPoweredTagsSeen > 0 && mCurrentPower < getMaxPower())
//                {
//                    mCurrentPower += 1;
//                }
//                sendPowerChangeNotification(mCurrentPower);
//
//                readRSSICommand.setOutputPower((mCurrentPower));
//                readTemperatureCommand.setOutputPower(mCurrentPower);


                if( emptyScanCount > 0 || mValidTagsSeenLastPass != 0)
                {
                    sendMessageNotification("Tags detected...\n\nRSSI 'Too High' => move away.\nRSSI 'Too Low' => move nearer.");
                }
                emptyScanCount = 0;
                maxPowerEmptyScanCount = 0;

                if( isTemperatureRequired() )
                {
                    // Check to see if a temperature convertor is needed for any of the discovered transponders
                    for (String tagEPC : mInRangeTransponders.keySet())
                    {
                        int readingIndex = mTagReadingIndex.get(tagEPC);
                        TagReading reading = mTagReadings.get(readingIndex);

                        ISensorConverter converter = reading.getTemperatureConverter();
                        if (converter == null)
                        {
                            //sendMessageNotification(String.format( Locale.US, "\nE: %s - reading calibration data\n", tagEPC ));
                            reading.setMessage("Reading calibration data...");

                            // Use an 'open' read of all tags to obtain calibration data for as many tags as are in range
                            ReadTransponderCommand readCalCommand = ReadTransponderCommand.synchronousCommand();
                            readCalCommand.setResetParameters(TriState.YES);

                            readCalCommand.setUseAlert(TriState.NO);
                            targetForCalibrationData(readCalCommand,tagEPC);
                            //readCalCommand.setBank(Databank.USER);
                            //readCalCommand.setOffset(8);
                            //readCalCommand.setLength(4);

                            readCalCommand.setResponseLifecycleDelegate(new ICommandResponseLifecycleDelegate()
                            {
                                @Override
                                public void responseBegan() {mDidReceiveData = false;}

                                @Override
                                public void responseEnded() {}
                            });

                            readCalCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate()
                            {
                                @Override
                                public void transponderReceived(TransponderData transponder, boolean moreAvailable)
                                {
                                    if (!transponder.getEpc().contentEquals(tagEPC)) return;

                                    byte[] data = transponder.getReadData();
                                    if (data != null && data.length == 8)
                                    {
                                        mDidReceiveData = true;
                                        mDataReceived = data;
                                    }
                                }
                            });

                            int attempts = 0;
                            do
                            {
                                getCommander().executeCommand(readCalCommand);
                                if (mDidReceiveData)
                                {
                                    MagnusS3TemperatureConverter tc = null;
                                    try
                                    {
                                        tc = new MagnusS3TemperatureConverter(mDataReceived);
                                        // Add convertor to the cache
                                        reading.setTemperatureConverter(tc);
                                    }
                                    catch (ModelException e)
                                    {
                                        Log.d(TAG, "Failed to create temperature convertor: " + e.getMessage());
                                    }
                                }
                                else
                                {
                                    attempts++;
                                    //pause(50);
                                }
                            }
                            while (!mDidReceiveData && attempts < 4);
                            if( attempts >= 4 )
                            {
                                reading.setMessage("Calibration not read!");
                            }
                        }
                    }

                    // Now request Temperature Codes
                    getCommander().executeCommand(readTemperatureCommand);
                }
            }

            mValidTagsSeenLastPass = mInRangeTransponders.size();

//            if( mTagReadingsModified )
//            {
                sendReadingUpdateNotification();
//            }
        }
        while(mIsRunning);


//        for(Statistics stats : results.values())
//        {
//            String temperatureString = String.format(Locale.US, "%6.2f", stats.averageTemperature());
//        }

    }


    /**
     * Reads S3 sensor tags using Dual Select and a single read command:
     *  - TransponderSelect command used to initiate the temperature conversion
     *  - ReadCommand to select on RSSI and retrieve SensorCode, on-chip RSSI and the Temperature Code, if required
     *  For temperature reading, requires series 3 Reader
     * @throws ModelException
     */
    private void continuousDualSelectSensorReading() throws ModelException
    {
        //
        // Use select to initiate the temperature conversion
        //
        TransponderSelectCommand tsCommand = TransponderSelectCommand.synchronousCommand();
        tsCommand.setResetParameters(TriState.YES);

        // Select for RFMicron sensor tag Temperature
        tsCommand.setSelectOffset(0x00E0);
        tsCommand.setSelectLength(0);
        tsCommand.setSelectBank(Databank.USER);
        tsCommand.setSelectAction(SelectAction.NOTHING_NOTHING_NOT_ASSERT_SET_A);

        // On Series 2xxx and 3xxx Readers this simply configures the additional select parameters
        getCommander().executeCommand(tsCommand);



        //
        // Use select to filter tags and obtain on-tag RSSI and Sensor code simultaneously
        // Select for tags below the maximum possible RSSI level
        //
        // This (mostly) restricts the responses to Magnus S3 tags.
        // By allowing all RSSI levels, it is possible to respond to both too low and too high RSSI
        //
        // Refer to Axzon Application Note AN002 for more details
        //
        // TransponderSelect will have started Temperature conversion
        // so access all three values
        //
        // Note: This code only works for Magnus S3 sensor tags, not S2
        //

        // Select for RSSI <= 31
//        int rssiMaskLevel = 0x1f;
        int rssiMaskLevel = mMaxRSSI & 0x1f; // Swap this line for the above to restrict to tags below the Max RSSI limit

        String rssiMaskString = String.format(Locale.US, "%2x", rssiMaskLevel);

        ReadTransponderCommand readRSSICommand = ReadTransponderCommand.synchronousCommand();
        readRSSICommand.setResetParameters(TriState.YES);

        readRSSICommand.setIncludeChannelFrequency(TriState.YES);

        readRSSICommand.setUseAlert(TriState.NO);
        // Select for RFMicron sensor tag RSSI
        readRSSICommand.setSelectOffset(0x00d0);
        readRSSICommand.setSelectLength(8);
        readRSSICommand.setSelectData(rssiMaskString);
        readRSSICommand.setSelectBank(Databank.USER);

        // Use the additional select parameters
        readRSSICommand.setAdditionalSelectEnabled(TriState.YES);

        readRSSICommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A); //

        readRSSICommand.setBank(Databank.RESERVED);
        // Offset for SensorCode, RSSI follows, then Temperature Code
        readRSSICommand.setOffset(0xC);
        readRSSICommand.setLength(3);

        readRSSICommand.setSelectPause(2);

//        readRSSICommand.setQValue(1);

        readRSSICommand.setResponseLifecycleDelegate(new ICommandResponseLifecycleDelegate() {
            @Override
            public void responseBegan() {}

            @Override
            public void responseEnded(){}
        });

        readRSSICommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable)
            {
                String epc = transponder.getEpc();

                Integer frequency = transponder.getChannelFrequency();
                frequency = frequency != null ? frequency : 0;

                if( epc != null )
                {
                    byte[] data = transponder.getReadData();
                    // Process if enough data received for at least the RSSI and Sensor Code
                    if (data != null && data.length >= 4 )
                    {
                        mTotalTagsSeen += 1;

                        // Extract RSSI
                        int d3 = (int) data[3];
                        int rssi = (data[3] & 0x1f);

                        // Ensure the changes will be seen
                        mTagReadingsModified = true;

                        // Has the tag been seen before
                        TagReading reading;
                        if (mTagReadingIndex.containsKey(epc))
                        {
                            reading = mTagReadings.get(mTagReadingIndex.get(epc));
                        }
                        else
                        {
                            // Create new reading and append to existing
                            reading = appendReading(epc, mSamplesPerTag);
                        }


                        // Is the RSSI in the valid range
                        if (rssi < mMinRSSI)
                        {
                            mUnderPoweredTagsSeen += 1;
                            // Mark as too low
                            reading.setMessage("RSSI too low!");
                        }
                        else if (rssi > mMaxRSSI)
                        {
                            mOverPoweredTagsSeen += 1;
                            // Mark as too high
                            reading.setMessage("RSSI too high!");
                        }
                        else
                        {
                            // tag's RSSI is in the valid range -
                            mInRangeTransponders.put(epc, transponder);
                            reading.setMessage("");

                            // Get the Sensor code data collected
                            String sensorCodeString = "";
                            int sensorCode = 0;
                            String srData = HexEncoding.bytesToString(data);
                            sensorCodeString = srData.substring(0, 4);
                            sensorCode = Integer.parseInt(sensorCodeString, 16);

                            if( !isTemperatureRequired() )
                            {
                                // Record Sensor Code, RSSI and frequency
                                reading.update(sensorCode, rssi, frequency, mCurrentPower);
                            }
                            else
                            {
                                // Interpret data
                                String dataString = null;
                                float temperature = INVALID_TEMPERATURE;
                                String temperatureString;
                                if( data != null && data.length >=6)
                                {
                                    dataString = HexEncoding.bytesToString(data);

                                    int b0 = (int)data[4] & 0xff;
                                    int b1 = (int)data[5] & 0xff;
                                    int temperatureCode = (b0 << 8) + b1;

                                    ISensorConverter converter = reading.getTemperatureConverter();
                                    if( converter != null)
                                    {
                                        // Convert Temperature Code into temperature
                                        MagnusS3TemperatureConverter tc = (MagnusS3TemperatureConverter) converter;
                                        temperature = tc.valueFromCode(temperatureCode);

                                        // Sanity check the value (based on Axzon AZN3200-AAR datasheet)
                                        if( temperature < -40.0f || temperature > 125.0f)
                                        {
                                            // Use a special value to erroneous reading
                                            temperature = INVALID_TEMPERATURE;
                                            temperatureString = "<!>";
                                        }
                                        else
                                        {
                                            temperatureString = String.format(Locale.US, "%6.2f", temperature);
                                        }
                                    }
                                    else
                                    {
                                        temperatureString = "?";
                                    }

                                    reading.update(temperatureCode, sensorCode, rssi, frequency, mCurrentPower);
                                }
                            }
                        }
                    }
                }
            }
        });


        resetPowerSweep(true);
        readRSSICommand.setOutputPower((mCurrentPower));

        mIsRunning = true;

        int emptyScanCount = 0;
        int maxPowerEmptyScanCount = 0;

        // Adjust power after this many scans without valid tags
        final int emptyScanLimit = 1;

        // Reset the scanning after this many scans, at full power, without valid tags
        final int emptyScanLimitAtMaxPower = 8;

        mValidTagsSeenLastPass = 0;

        do
        {
            mTagReadingsModified = false;
            mInRangeTransponders.clear();
            mTotalTagsSeen = 0;
            mUnderPoweredTagsSeen = 0;
            mOverPoweredTagsSeen = 0;

            // Assume tags will not be seen
            // If they are then the update() method will correct this assumption
            for (TagReading reading : mTagReadings )
            {
                reading.notSeen();
            }

            // Scan for transponders with on-chip RSSI in correct range
            getCommander().executeCommand(readRSSICommand);

            String status = String.format("Valid: %d, Under: %d, Over: %d",
                    mInRangeTransponders.keySet().size(),
                    mUnderPoweredTagsSeen,
                    mOverPoweredTagsSeen
            );
            Log.d(TAG, status);

            // Simple continuous sweep from low to high
            mCurrentPower++;
            if( mCurrentPower >= getMaxPower() )
            {
                mCurrentPower = getMinPower();
            }
            sendPowerChangeNotification(mCurrentPower);

            readRSSICommand.setOutputPower((mCurrentPower));

            if(mInRangeTransponders.isEmpty())
            {
                emptyScanCount += 1;
            }
            else
            {
                if( emptyScanCount > 0 || mValidTagsSeenLastPass != 0)
                {
                    sendMessageNotification("Tags detected...\n\nRSSI 'Too High' => move away.\nRSSI 'Too Low' => move nearer.");
                }
                emptyScanCount = 0;
                maxPowerEmptyScanCount = 0;

                if( isTemperatureRequired() )
                {
                    // Check to see if a temperature convertor is needed for any of the discovered transponders
                    for (String tagEPC : mInRangeTransponders.keySet())
                    {
                        int readingIndex = mTagReadingIndex.get(tagEPC);
                        TagReading reading = mTagReadings.get(readingIndex);

                        ISensorConverter converter = reading.getTemperatureConverter();
                        if (converter == null)
                        {
                            //sendMessageNotification(String.format( Locale.US, "\nE: %s - reading calibration data\n", tagEPC ));
                            reading.setMessage("Reading calibration data...");

                            // Use an 'open' read of all tags to obtain calibration data for as many tags as are in range
                            ReadTransponderCommand readCalCommand = ReadTransponderCommand.synchronousCommand();
                            readCalCommand.setResetParameters(TriState.YES);

                            readCalCommand.setUseAlert(TriState.NO);
                            targetForCalibrationData(readCalCommand,tagEPC);
                            //readCalCommand.setBank(Databank.USER);
                            //readCalCommand.setOffset(8);
                            //readCalCommand.setLength(4);

                            readCalCommand.setResponseLifecycleDelegate(new ICommandResponseLifecycleDelegate()
                            {
                                @Override
                                public void responseBegan() {mDidReceiveData = false;}

                                @Override
                                public void responseEnded() {}
                            });

                            readCalCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate()
                            {
                                @Override
                                public void transponderReceived(TransponderData transponder, boolean moreAvailable)
                                {
                                    if (!transponder.getEpc().contentEquals(tagEPC)) return;

                                    byte[] data = transponder.getReadData();
                                    if (data != null && data.length == 8)
                                    {
                                        mDidReceiveData = true;
                                        mDataReceived = data;
                                    }
                                }
                            });

                            int attempts = 0;
                            do
                            {
                                getCommander().executeCommand(readCalCommand);
                                if (mDidReceiveData)
                                {
                                    MagnusS3TemperatureConverter tc = null;
                                    try
                                    {
                                        tc = new MagnusS3TemperatureConverter(mDataReceived);
                                        // Add convertor to the cache
                                        reading.setTemperatureConverter(tc);
                                    }
                                    catch (ModelException e)
                                    {
                                        Log.d(TAG, "Failed to create temperature convertor: " + e.getMessage());
                                    }
                                }
                                else
                                {
                                    attempts++;
                                    //pause(50);
                                }
                            }
                            while (!mDidReceiveData && attempts < 4);
                            if( attempts >= 4 )
                            {
                                reading.setMessage("Calibration not read!");
                            }
                        }
                    }
                }
            }

            mValidTagsSeenLastPass = mInRangeTransponders.size();

            //if( mTagReadingsModified )
            //{
                sendReadingUpdateNotification();
            //}
        }
        while(mIsRunning);


//        for(Statistics stats : results.values())
//        {
//            String temperatureString = String.format(Locale.US, "%6.2f", stats.averageTemperature());
//        }

    }


    private void resetPowerSweep(boolean isFirstSweep)
    {
        // Always start at preferred minimum power level
        mCurrentPower = getMinPower();
        mIsSweepIncreasing = true;

        sendPowerChangeNotification(mCurrentPower);
        if( isFirstSweep )
        {
            sendMessageNotification("Determining optimal power level\n\nMaintain a fixed distance from tag\nunless instructed otherwise.");
        }
        else
        {
            sendMessageNotification("Unable to determine power level\n\nAdjust distance from Reader to tag.\n");
        }
    }

    public void startContinuousSensorReading() throws ModelException
    {
        if(mIsRunning) return;

        performTask(new Runnable() {
            @Override
            public void run() {
                // Release the Busy flag to allow the Stop button to be active
                //mTaskRunner = null;
                //setBusy(false);

                try
                {
                    //continuousSensorReading();
                    continuousDualSelectSensorReading();
                }
                catch (ModelException e)
                {
                    sendMessageNotification(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopContinuousSensorReading()
    {
        if( mIsRunning )
        {
            sendMessageNotification("Stopping...");
        }
        mIsRunning = false;
    }

    private void sendPowerChangeNotification(int value)
    {
        if( mHandler != null )
        {
            Message msg = mHandler.obtainMessage(POWER_CHANGE_NOTIFICATION, value, 0 );
            mHandler.sendMessage(msg);
        }
    }

    private void sendReadingUpdateNotification()
    {
        if( mHandler != null )
        {
            Message msg = mHandler.obtainMessage(READING_UPDATE_NOTIFICATION);
            mHandler.sendMessage(msg);
        }
    }

    /**
     * Clear all the tag readings
     */
    public void clearTagReadings()
    {
        mTagReadings.clear();
        mTagReadingIndex.clear();
    }

    // Add a new TagReading to the collection and index
    private TagReading appendReading(String epc, int samples)
    {
        TagReading reading = new TagReading(epc, samples);
        reading.setSensorCodeConverter(sLinearConverter);
        mTagReadings.add(reading);
        mTagReadingIndex.put(epc, mTagReadings.size() - 1);
        return reading;
    }

    static ICompensatedSensorConverter sNullConverter = new NullSensorConverter();
    static ICompensatedSensorConverter sLinearConverter = new LinearSensorConverter();
}
