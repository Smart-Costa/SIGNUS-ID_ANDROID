//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.uk.tsl.rfid.samples.tagkill;

import java.util.HashMap;
import java.util.Locale;

import android.util.Log;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.ModelException;
import com.uk.tsl.rfid.asciiprotocol.commands.AbortCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.AlertCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.InventoryCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.KillCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.LinkProfileCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadLogFileCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.SwitchActionCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.AlertDuration;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchState;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TransponderAccessErrorCode;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ISwitchStateReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.SwitchResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;

public class TagKillModel extends ModelBase
{
    private static String devBoardTagEPC = "FFFF33B2DDD9014000000000463C";
    private static String devBoardTagtid = "E2801150200015721FF71809";
    private static String devBoardTagKillPassword = "DEADC0DE";

    private static String sUpdateUIRequest = "UI:";
	// Control 
	private boolean mAnyTagSeen;
    private boolean mEnabled;
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
                getCommander().addResponder(mSwitchResponder);
			}
            else {

                getCommander().removeResponder(mSwitchResponder);
				// Stop listening for transponders
                getCommander().removeResponder(mInventoryResponder);
			}
			
		}
	}


    public String getTargetEPC() { return mTargetEPC; }
    public void setTargetEPC(String targetEPC) {mTargetEPC = targetEPC;}
    private String mTargetEPC = null;

    public String getTargetTID() { return mTargetTID; }
    public void setTargetTID(String targetTID) {mTargetTID = targetTID;}
    private String mTargetTID = null;

    public boolean isArmed() { return mArmingLevel >= 0; }
    public int getArmingLevel() { return mArmingLevel; }
    public void setArmingLevel(int level) { mArmingLevel = level; }
    private int mArmingLevel = -1;

    private static int sArmingResetLevel = 0;
    public int getArmingThreshold() { return sArmingThreshold; }
    // The level at which the action is executed e.g. kill tag
    private static int sArmingThreshold = 3;


    public boolean lastKillSucceeded()
    {
        return mLastKillSuccessState;
    }
    public void setLastKillState(boolean lastKillSucceeded)
    {
        mLastKillSuccessState = lastKillSucceeded;
    }
    private boolean mLastKillSuccessState = false;

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

	// A 'Dictionary' lookup for the unique transponders seen
	private HashMap<String, TransponderData> mUniqueTransponders = new HashMap<>();

	// The inventory command configuration
	public InventoryCommand getCommand() { return mInventoryCommand; }

    private SwitchActionCommand mSwitchCommand;
    private SwitchResponder mSwitchResponder;
    private SwitchState mLastSwitchState = SwitchState.OFF;



	// Used to indicate tags seen in continuous inventory mode
	private AlertCommand mAlertCommand;

    private long mStartTime;
    private long mFinishTime;
    private long mFirstTagTime;
    private long mLastTagTime;
    private long mInventoryTagCount;

	public TagKillModel()
	{
        mUniquesOnly = true;

        mAlertCommand = new AlertCommand();
        mAlertCommand.setDuration(AlertDuration.SHORT);

		// This is the command that will be used to perform configuration changes and inventories
		mInventoryCommand = new InventoryCommand();
        mInventoryCommand.setResetParameters(TriState.YES);

        // Handle the alerts in the App
        mInventoryCommand.setUseAlert(TriState.NO);

        mInventoryCommand.setUsefastId(TriState.YES);

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

                        // Only list tags with TID data (i.e. allow to be killed)
                        if (transponder.getTidData() != null) {
                            sendTagFoundNotification(transponder);
                            mTagsSeen++;
                        }
                        else {
                            // Some Reader/tag combinations using FastId give rise to EPCs that are concatenated EPC and TID
                            // Currently only interested in Impinj (M700 series) tags
                            // Try to recognise a concatenated FastId response - 96-bit EPC only
                            final String epc = transponder.getEpc();
                            if( epc.length() > 24 ) {
                                // Fast TID data should be (at least) 96-bits
                                // Only look for Impinj tags
                                if( epc.length() >= 48 && epc.substring(24, 29).equals("E2801")) {
                                    // Create new transponder
                                    TransponderData tag = new TransponderData(
                                            null,
                                            epc.substring(0,24),
                                            null, false, false, null, null,
                                            transponder.getRssi(),
                                            null, null, null,
                                            HexEncoding.stringToBytes(transponder.getEpc().substring(24)),
                                            -1
                                            );
                                    transponder = tag;
                                    sendTagFoundNotification(transponder);
                                    mTagsSeen++;
                                }
                            }
                        }
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
                        // Within a Responder so issue alert after this method has finished
                        // Alternatively, could use getCommander().send(mAlertCommand.getCommandLine())
                        mHandler.post(new Runnable() {
                            @Override
                            public void run()
                            {
                                getCommander().executeCommand(mAlertCommand);
                                alertLastIssueTime = System.nanoTime();
                            }
                        });
                    }
                }
                else
                {
                    // No tags were seen but a value is needed to calculate read rate
                    mLastTagTime = System.nanoTime();
                }

                if (!mAnyTagSeen && mInventoryCommand.getTakeNoAction() != TriState.YES)
                {
                    //sendMessageNotification("No transponders seen");
                }
                mInventoryCommand.setTakeNoAction(TriState.NO);

                if( mInventoryTagCount > 0)
                {
                    double tagDuration = mLastTagTime - mFirstTagTime;
                    double tagDurationNs = tagDuration /1.0e9;
                    double tagReadRate = mInventoryTagCount / tagDurationNs;

                    //sendMessageNotification(String.format("RR:Read Rate: %.0f tps", tagReadRate));
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

        mSwitchCommand = SwitchActionCommand.synchronousCommand();

        mSwitchResponder = new SwitchResponder();
        mSwitchResponder.setSwitchStateReceivedDelegate(new ISwitchStateReceivedDelegate() {
            @Override
            public void switchStateReceived(SwitchState switchState)
            {
                if( mArmingLevel >= 0 ) {
                    if( mLastSwitchState.equals(SwitchState.SINGLE)){
                        if( switchState.equals(SwitchState.OFF)) {
                            mArmingLevel++;

                            if( mArmingLevel >= sArmingThreshold ) {
                                // Try to kill the tag
                                // Allow the responder to complete before executing the kill
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        killTargetTag();
                                    }
                                });
                            }
                            sendMessageNotification(sUpdateUIRequest);
                        } else {
                            // Any other switch event cancels
                            mArmingLevel = sArmingResetLevel;
                        }
                        sendTriggerPullNotification();
                    }
                    else if( mLastSwitchState.equals(SwitchState.OFF)) {
                        if( !switchState.equals(SwitchState.SINGLE) ) {
                            mArmingLevel = sArmingResetLevel;
                            sendMessageNotification(sUpdateUIRequest);
                        }
                    }
                }
                mLastSwitchState = switchState;
            }
        });
	}


    private void sendTriggerPullNotification()
    {
        int triggerPullsNeeded = sArmingThreshold - mArmingLevel;
        sendMessageNotification(String.format("KI:Pull trigger %d time%s to kill tag", triggerPullsNeeded, (triggerPullsNeeded == 1 ? "" : "s") ));
    }


    private void sendTagFoundNotification(TransponderData transponder)
    {
        String msg = "EPC: " + transponder.getEpc();
        String tidValue = HexEncoding.bytesToString(transponder.getTidData());
        String tidMsg = String.format(Locale.US, "\nTID: %s", tidValue);
        msg += tidMsg;
        // Always include RSSI
        String infoMsg = String.format(Locale.US, "\nRSSI: %d", transponder.getRssi());
        msg += infoMsg;

        sendMessageNotification(msg);
    }


    //
    // Reset the target tag
    //
    public void clearTarget()
    {
        setTargetEPC(null);
        setTargetTID(null);
        setArmingLevel(-1);
        setLastKillState(false);
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
	
	//
	// Update the reader configuration from the command
	// Call this after each change to the model's command
	//
	public void updateConfiguration()
	{
		if(getCommander().isConnected()) {
            try
            {
                // Configure the link profile if needed
                if( getCommander().getDeviceProperties().getLinkProfile() != mLinkProfile)
                {
                    LinkProfileCommand lpCommand = LinkProfileCommand.synchronousCommand();
                    lpCommand.setProfile(mLinkProfile);
                    getCommander().executeCommand(lpCommand);

                    // Refresh the device properties after Link Profile change
                    getCommander().updateDeviceProperties();
                }

                // Configure the Inventory
                mInventoryCommand.setTakeNoAction(TriState.YES);

                // Configure the type of inventory reports
                mInventoryCommand.setIncludeTransponderRssi(TriState.YES);

                getCommander().executeCommand(mInventoryCommand);
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
	}

    public void updateSwitchConfiguration()
    {
        if(getCommander().isConnected()) {
            try
            {
                if( getArmingLevel() >= 0 ) {
                    mSwitchCommand.setSinglePressAction(SwitchAction.OFF);
                    mSwitchCommand.setDoublePressAction(SwitchAction.OFF);
                    mSwitchCommand.setAsynchronousReportingEnabled(TriState.YES);
                } else {
                    mSwitchCommand = SwitchActionCommand.synchronousCommand();
                    mSwitchCommand.setResetParameters(TriState.YES);
                }

                getCommander().executeCommand(mSwitchCommand);

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
    }


	// Reset the unique transponder list
	public void clearUniques()
    {
        mTagsSeen = 0;
        mUniqueTransponders.clear();
    }


    //
    // Note: Currently, Tags to be killed have to have Kill Password "DEADC0DE"
    //
    public void killTargetTag()
    {
        try {
            performTask(new Runnable() {
                @Override
                public void run() {

                    sendMessageNotification("KI:Attempting to kill the Tag...");

                    KillCommand kCommand = KillCommand.synchronousCommand();
                    kCommand.setResetParameters(TriState.YES);

                    // target tag using TID
                    kCommand.setSelectBank(Databank.TRANSPONDER_IDENTIFIER);
                    kCommand.setSelectOffset(0);
                    kCommand.setSelectData(getTargetTID());
                    kCommand.setSelectLength(getTargetTID().length() * 4);

                    kCommand.setKillPassword(devBoardTagKillPassword);

                    kCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
                        @Override
                        public void transponderReceived(TransponderData transponderData, boolean b)
                        {
                            Log.d("TKM", String.format("Tag Seen: %s", transponderData.getEpc()) );
                            // Test for success - includes workaround for IndyMac bugs
                            if(transponderData.didKill()
                                    || getCommander().getDeviceProperties().getModel().startsWith("1128")
                                        && transponderData.getAccessErrorCode() != null && transponderData.getAccessErrorCode().equals(TransponderAccessErrorCode.OUT_OF_RETRIES)
                            )
                            {
                                Log.d("TKM", "..was killed" );

                                // Monza-X returns EPC with combined TID (quirk of powered tag)
                                // Check EPC section only
                                if( transponderData.getEpc().startsWith(getTargetEPC()))
                                {
                                    mTagKilled = true;
                                }
                                else {
                                    sendMessageNotification( String.format("ER:!!! the wrong tag has been killed !!!\n%s", transponderData.getEpc()) );
                                }
                            }
                        }
                    });

                    mTagKilled = false;

                    getCommander().executeCommand(kCommand);
                    sendMessageNotification( String.format("KI:Time taken: %.2fs", getTaskExecutionDuration()) );

                    // Double check that 1128 really did kill the tag...
                    if( mTagKilled
                            && getCommander().getDeviceProperties().getModel().startsWith("1128")
                    ) {
                        // See if the tag was really killed
                        ReadTransponderCommand rCommand = ReadTransponderCommand.synchronousCommand();
                        // target tag using TID
                        rCommand.setSelectBank(Databank.TRANSPONDER_IDENTIFIER);
                        rCommand.setSelectOffset(0);
                        rCommand.setSelectData(getTargetTID());
                        rCommand.setSelectLength(getTargetTID().length() * 4);

                        rCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
                            @Override
                            public void transponderReceived(TransponderData transponderData, boolean b)
                            {
                                if( transponderData.getEpc() != null && transponderData.getEpc().startsWith(getTargetEPC())) {
                                    // Tag is still alive
                                    mTagKilled = false;
                                }
                            }
                        });
                        getCommander().executeCommand(rCommand);
                    }

                    if( mTagKilled )
                    {
                        setLastKillState(true);
                        sendMessageNotification("KS:");
                        sendMessageNotification("KI: Tag Killed!");
                        pause(2000);

                        clearTarget();
                        clearUniques();
                        sendMessageNotification("CL:");

                        // Rescan for tags using existing inventory configuration
                        getCommander().executeCommand(mInventoryCommand);
                    } else {
                        setLastKillState(false);
                        sendMessageNotification("KI: Failed to kill tag...");
                        sendMessageNotification("KF:");
                        pause(2000);
                        sendMessageNotification("KI: Try Again...");
                        // Reset to allow another try
                        setArmingLevel(sArmingResetLevel);
                        pause(1000);
                        sendTriggerPullNotification();
                    }

                    updateSwitchConfiguration();
                    sendMessageNotification("UI:");
                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }

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


    private boolean mTagKilled = false;
}


