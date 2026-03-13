//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.uk.tsl.rfid.samples.inventory;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Looper;
import android.util.Log;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.ModelException;
import com.uk.tsl.rfid.asciiprotocol.commands.AbortCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.AlertCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.InventoryCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.LinkProfileCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadLogFileCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.AlertDuration;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.IBarcodeReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;

public class InventoryModel extends ModelBase
{
    private static String TAG = "IM";

	// Control 
	private boolean mAnyTagSeen;
    private boolean mEnabled;
    private boolean mContinuousScanEnabled;
    private boolean mUniquesOnly;
    private int mTagsSeen = 0;
    private long alertLastIssueTime = System.nanoTime();
    private final static long sAlertRepeatDelayMs = 400 * 1000 * 1000;

    public boolean enabled() { return mEnabled; }

	public void setEnabled(boolean state)
	{
		boolean oldState = mEnabled;
		mEnabled = state;

		// Update the commander for state changes
		if(oldState != state) {
			if( mEnabled ) {
				// Listen for transponders
				getCommander().addResponder(mInventoryResponder);
				// Listen for barcodes
				getCommander().addResponder(mBarcodeResponder);
			} else {
			    if (mContinuousScanEnabled)
                {
                    scanStop();
                }
				// Stop listening for transponders
				getCommander().removeResponder(mInventoryResponder);
				// Stop listening for barcodes
				getCommander().removeResponder(mBarcodeResponder);
			}
			
		}
	}

	public boolean uniquesOnly() { return mUniquesOnly; }

	public void setUniquesOnly(boolean value)
    {
        mUniquesOnly = value;
    }

    /**
     * @return true if the tag info (CRC, etc...) will be requested
     */
    public boolean isInfoRequested() { return IsInfoRequested; }

    /**
     * Controls the tag info requested
     * @param infoRequested true to request the tag info (CRC, etc...)
     */
    public void setInfoRequested(boolean infoRequested) { IsInfoRequested = infoRequested; }
    private boolean IsInfoRequested = false;

    /**
     * The current link profile
     * @return the link profile number
     */
    public int getLinkProfile() { return mLinkProfile; }

    /**
     * Set the link profile
     * @param linkProfile the link profile number
     */
    public void setLinkProfile(int linkProfile) { mLinkProfile = linkProfile; }
    private int mLinkProfile;

    /**
     * @return the maximum number of tags per inventory action
     */
    public int getMaximumTagsPerInventory() { return mMaximumTagsPerInventory; }

    /**
     * Set the maximum number of tags per inventory action
     * @param maximumTagsPerInventory the maximum tags to set
     */
    public void setMaximumTagsPerInventory(int maximumTagsPerInventory) { mMaximumTagsPerInventory = maximumTagsPerInventory; }
    private int mMaximumTagsPerInventory = 0;

	// The command to use as a responder to capture incoming inventory responses
	private InventoryCommand mInventoryResponder;
	// The command used to issue commands
	private InventoryCommand mInventoryCommand;

	// The command to use as a responder to capture incoming barcode responses
	private BarcodeCommand mBarcodeResponder;

	// A 'Dictionary' lookup for the unique transponders seen
	private HashMap<String, TransponderData> mUniqueTransponders = new HashMap<>();

	// The inventory command configuration
	public InventoryCommand getCommand() { return mInventoryCommand; }

	// Used to indicate tags seen in continuous inventory mode
	private AlertCommand mAlertCommand;

    private long mStartTime;
    private long mFinishTime;
    private long mFirstTagTime;
    private long mLastTagTime;
    private long mInventoryTagCount;

	public InventoryModel()
	{
        mContinuousScanEnabled = false;
        mUniquesOnly = false;

        mAlertCommand = new AlertCommand();
        mAlertCommand.setDuration(AlertDuration.SHORT);

		// This is the command that will be used to perform configuration changes and inventories
		mInventoryCommand = new InventoryCommand();
        mInventoryCommand.setResetParameters(TriState.YES);

        // Handle the alerts in the App
        mInventoryCommand.setUseAlert(TriState.NO);

        // Use an InventoryCommand as a responder to capture all incoming inventory responses
		mInventoryResponder = new InventoryCommand();

		// Also capture the responses that were not from App commands 
		mInventoryResponder.setCaptureNonLibraryResponses(true);

		// Notify when each transponder is seen
		mInventoryResponder.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

			@Override
			public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                if(!mAnyTagSeen) {
                    mFirstTagTime = System.nanoTime();
                }
                mInventoryTagCount += 1;

				if( transponder.getEpc() != null) {
                    mAnyTagSeen = true;

                    if( !(mUniquesOnly && mUniqueTransponders.containsKey(transponder.getEpc())) ) {

                        String msg = "EPC: " + transponder.getEpc();
                        if (transponder.getTidData() != null) {
                            String tidValue = HexEncoding.bytesToString(transponder.getTidData());
                            String tidMsg = String.format(Locale.US, "\nTID: %s", tidValue);
                            msg += tidMsg;
                        }
                        if (isInfoRequested()) {
                            String infoMsg = String.format(Locale.US, "\nRSSI: %d  PC: %04X  CRC: %04X", transponder.getRssi(), transponder.getPc(), transponder.getCrc());
                            msg += infoMsg;
                        }
                        msg += "\n# " + mTagsSeen;

                        sendMessageNotification(msg);
                        mTagsSeen++;

                        // Remember this transponder as it has not been seen before
                        if (mUniquesOnly) {
                            mUniqueTransponders.put(transponder.getEpc(), transponder);
                        }
                    }
                }
				if( !moreAvailable) {
                    mLastTagTime = System.nanoTime();
					Log.d("TagCount",String.format("Tags seen: %s", mTagsSeen));
				}
			}
		});

		mInventoryResponder.setResponseLifecycleDelegate( new ICommandResponseLifecycleDelegate() {
			
			@Override
			public void responseEnded() {
                mFinishTime = System.nanoTime();
			    // Only play sound when tags were seen
                if(mAnyTagSeen)
                {
                    // To avoid continuously running the buzzer on 11xx series Readers
                    // Ensure no new sound until after least (short) tone has finished
                    // Note: 21xx series readers do not need this
                    if( System.nanoTime() - alertLastIssueTime > sAlertRepeatDelayMs)
                    {
                        getCommander().executeCommand(mAlertCommand);
                        alertLastIssueTime = System.nanoTime();
                    }
                }
                else
                {
                    // No tags were seen but a value is needed to calculate read rate
                    mLastTagTime = System.nanoTime();
                }
                // Send on any messages
                for ( String msg : mInventoryResponder.getMessages())
                {
                    sendMessageNotification(msg);
                }
                if( mContinuousScanEnabled)
                {
                    // Issue another asynchronous scan
                    getCommander().executeCommand(mInventoryCommand);
                }
                else
                {
                    if (!mAnyTagSeen && mInventoryCommand.getTakeNoAction() != TriState.YES)
                    {
                        sendMessageNotification("No transponders seen");
                    }
                    mInventoryCommand.setTakeNoAction(TriState.NO);
                }

                if( mInventoryTagCount > 1)
                {
                    double tagDuration = mLastTagTime - mFirstTagTime;
                    double tagDurationNs = tagDuration /1.0e9;
                    double tagReadRate = mInventoryTagCount / tagDurationNs;

                    sendMessageNotification(String.format("RR:Host Read Rate: %.0f tps", tagReadRate));
                }
                else
                {
                    sendMessageNotification(String.format("RR:Host Read Rate: --- tps"));
                }
            }
			
			@Override
			public void responseBegan() {
				mAnyTagSeen = false;
                mStartTime = System.nanoTime();
                mFirstTagTime = System.nanoTime(); // Default to inventory start time until a Tag is actually seen
                mInventoryTagCount = 0;
			}
		});

		// This command is used to capture barcode responses
		mBarcodeResponder = new BarcodeCommand();
		mBarcodeResponder.setCaptureNonLibraryResponses(true);
		mBarcodeResponder.setUseEscapeCharacter(TriState.YES);
		mBarcodeResponder.setBarcodeReceivedDelegate(new IBarcodeReceivedDelegate() {
			@Override
			public void barcodeReceived(String barcode)
            {
                String msg = "BC: " + mBarcodeResponder.getData();
                sendMessageNotification(msg);

                // Include additional information when present
                if( mBarcodeResponder.getSymbology() != null )
                {
                    msg = String.format(Locale.US, "BC: %s (%s%s)", mBarcodeResponder.getSymbology().getName(), mBarcodeResponder.getSymbology().getCode(), mBarcodeResponder.getSymbology().getModifier());
                    sendMessageNotification(msg);
                }
			}
		});
	}

	//
	// Reset the reader configuration to default command values
	//
	public void resetDevice()
	{
        if(getCommander().isConnected()) {
            FactoryDefaultsCommand fdCommand = new FactoryDefaultsCommand();
            fdCommand.setResetParameters(TriState.YES);
            getCommander().executeCommand(fdCommand);
        }
	}

    public void resetAndUpdateConfig()
    {
//        try
//        {
            serialTaskRunner(new Runnable() {
                @Override
                public void run()
                {
                    Log.d(TAG, "resetAndUpdateConfig");
                    resetDevice();
                    Log.d(TAG, String.format("resetAndUpdateConfig Thread: %s", Thread.currentThread().getName()));
                    updateConfiguration();
                }
            });
//        }
//        catch (ModelException e)
//        {
//            throw new RuntimeException(e);
//        }
    }


	//
	// Update the reader configuration from the command
	// Call this after each change to the model's command
	//
	public void updateConfiguration()
    {
        if (getCommander().isConnected() )
        {
            // Execute off the main thread, when necessary
            runOffUIThread(() -> {
                try
                {
                    Log.d(TAG, String.format("updateConfiguration Thread: %s", Thread.currentThread().getName()));

                    mInventoryCommand.setTakeNoAction(TriState.YES);

                    // Configure the type of inventory reports
                    mInventoryCommand.setIncludeTransponderRssi(TriState.from(isInfoRequested()));
                    mInventoryCommand.setIncludeChecksum(TriState.from(isInfoRequested()));
                    mInventoryCommand.setIncludePC(TriState.from(isInfoRequested()));
                    mInventoryCommand.setIncludeDateTime(TriState.from(isInfoRequested()));

                    // Only Series 3 Readers - older Readers will ignore this parameter
                    mInventoryCommand.setHaltOnTags(mMaximumTagsPerInventory);


                    getCommander().executeCommand(mInventoryCommand);

                    // Configure the link profile if needed
                    if (getCommander().getDeviceProperties().getLinkProfile() != mLinkProfile)
                    {
                        LinkProfileCommand lpCommand = LinkProfileCommand.synchronousCommand();
                        lpCommand.setProfile(mLinkProfile);
                        getCommander().executeCommand(lpCommand);

                        // Refresh the device properties after Link Profile change
                        getCommander().updateDeviceProperties();
                    }


                    if (getCommander().getDeviceProperties().getInformationCommand().getAsciiProtocol().startsWith(("3")))
                    {
                        // Todo: Workaround for slow performance due to SD-Card logging issues in early Series 3 firmware
                        // Turn off the logging!
                        ReadLogFileCommand rlCommand = ReadLogFileCommand.synchronousCommand();
                        rlCommand.setTakeNoAction(TriState.YES);
                        rlCommand.setCommandLoggingEnabled(TriState.NO);
                        getCommander().executeCommand(rlCommand);
                    }
                }
                catch (Exception e)
                {
                    sendMessageNotification(String.format(Locale.US,
                            "Exception: %s",
                            e.getMessage()
                    ));
                    e.printStackTrace();
                }
                finally
                {
                }
            }
            );
        }
	}

    //
    // Perform an inventory scan with the current command parameters
    //
//    public void scan()
//    {
//        testForAntenna();
//        if(getCommander().isConnected()) {
//            mInventoryCommand.setTakeNoAction(TriState.NO);
//            getCommander().executeCommand(mInventoryCommand);
//        }
//    }


    //
    // Start the continuous inventory scan with the current command parameters
    //
    public void scanStart()
    {
//        testForAntenna();
        if(getCommander().isConnected()) {
            mContinuousScanEnabled = true;
            mInventoryCommand.setTakeNoAction(TriState.NO);
            getCommander().executeCommand(mInventoryCommand);
        }
    }


    //
    // Stop the continuous inventory scan
    //
    public void scanStop()
    {
        mContinuousScanEnabled = false;
        mInventoryCommand.setTakeNoAction(TriState.YES);

        if(getCommander().isConnected()) {
            // Cancel any running inventory
            getCommander().executeCommand(new AbortCommand());
        }
    }


    //
	// Test for the presence of the antenna
	//
//	public void testForAntenna()
//	{
//		if(getCommander().isConnected()) {
//			InventoryCommand testCommand = InventoryCommand.synchronousCommand();
//			testCommand.setTakeNoAction(TriState.YES);
//			getCommander().executeCommand(testCommand);
//			if( !testCommand.isSuccessful() ) {
//				sendMessageNotification("ER:Error! Code: " + testCommand.getErrorCode() + " " + testCommand.getMessages().toString());
//			}
//		}
//	}

	// Reset the unique transponder list
	public void clearUniques()
    {
        mTagsSeen = 0;
        mUniqueTransponders.clear();
    }

}


