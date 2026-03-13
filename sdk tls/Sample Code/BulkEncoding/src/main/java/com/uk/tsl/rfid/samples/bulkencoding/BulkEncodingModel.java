//----------------------------------------------------------------------------------------------
// Copyright (c) 2026 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.uk.tsl.rfid.samples.bulkencoding;

import java.util.ArrayList;
import java.util.Locale;

import android.util.Log;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.ModelException;
import com.uk.tsl.rfid.asciiprotocol.commands.BlockPermalockTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.InventoryCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.KillCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.LockCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.TransponderSelectCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.WriteTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.storm.AccessCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.storm.BankCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.storm.BankConfigurationGroup;
import com.uk.tsl.rfid.asciiprotocol.commands.storm.DatastoreCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySelect;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySession;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QueryTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.enumerations.WriteMode;
import com.uk.tsl.rfid.asciiprotocol.enumerations.storm.DatastoreClearStatusMode;
import com.uk.tsl.rfid.asciiprotocol.enumerations.storm.DatastoreRead;
import com.uk.tsl.rfid.asciiprotocol.enumerations.storm.TagReports;
import com.uk.tsl.rfid.asciiprotocol.responders.storm.BankSummary;
import com.uk.tsl.rfid.asciiprotocol.responders.storm.BankTransponderData;
import com.uk.tsl.rfid.asciiprotocol.responders.storm.DatastoreData;
import com.uk.tsl.rfid.asciiprotocol.responders.storm.DatastoreSummary;
import com.uk.tsl.rfid.asciiprotocol.responders.storm.IBankSummaryReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.storm.IBankTransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.storm.IDatastoreDataReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.storm.IDatastoreSummaryReceivedDelegate;

import androidx.annotation.NonNull;

public class BulkEncodingModel extends ModelBase
{
    private static String TAG = "BEM";
    private static final boolean D = BuildConfig.DEBUG;

    public static class Data
    {
        public String getSource()
        {
            return mSource;
        }
        public String mSource;

        public String getIdentifier()
        {
            return mIdentifier;
        }
        private String mIdentifier;

        public String getNewEPC()
        {
            return mNewEPC;
        }
        private String mNewEPC;

        public String getPassword()
        {
            return mPassword;
        }
        private String mPassword;

        public String getUserData()
        {
            return mUserData;
        }
        private String mUserData;

        public Data(String source, String id, String epc, String password, String data)
        {
            mSource = source;
            mIdentifier = id;
            mNewEPC = epc;
            mPassword = password;
            mUserData = data;
        }

        @NonNull
        @Override
        public String toString()
        {
            return mSource;
        }
    }


    final static ArrayList<String> MESSAGES = new ArrayList<>();

	// Control 
    private boolean mEnabled;


    public boolean enabled() { return mEnabled; }

	public void setEnabled(boolean state)
	{
		boolean oldState = mEnabled;
		mEnabled = state;

		// Update the commander for state changes
		if(oldState != state) {
			if( mEnabled ) {
				// Listen for transponders
			} else {
				// Stop listening for transponders
			}
			
		}
	}

    /**
     * @return true if the Bulk Encoding identifier is the tag TID
     */
    public boolean useTidIdentifier()
    {
        return useTidIdentifier;
    }
    /**
     * Modify the Identifier used
     * @param useTidIdentifier true if the Bulk Encoding identifier is the tag TID otherwise EPC is used
     */
    public void setUseTidIdentifier(boolean useTidIdentifier)
    {
        this.useTidIdentifier = useTidIdentifier;
    }
    private boolean useTidIdentifier = true;

    /**
     * @return the word offset into the EPC/TID where the identifier matching begins
     */
    public int getMatchOffset()
    {
        return matchOffset;
    }
    /**
     * Modify where the tag identifier (EPC/TID) is matched from
     * @param matchOffset the word offset into the EPC/TID where the identifier matching begins
     */
    public void setMatchOffset(int matchOffset)
    {
        this.matchOffset = matchOffset;
    }
    private int matchOffset = 0;

    /**
     * @return true if the password data is to be used
     */
    public boolean usePassword()
    {
        return usePassword;
    }
    /**
     * @param usePassword true if the password data is to be used
     */
    public void setUsePassword(boolean usePassword)
    {
        this.usePassword = usePassword;
    }
    private boolean usePassword = false;

    /**
     * @return true if the User Data is to be used
     */
    public boolean useUserData()
    {
        return useUserData;
    }
    /**
     * @param useUserData true if the User Data is to be used
     */
    public void setUseUserData(boolean useUserData)
    {
        this.useUserData = useUserData;
    }
    private boolean useUserData = false;

    /**
     * @return the dataset to use for encoding
     */
    public ArrayList<Data> getDataset()
    {
        return dataset;
    }
    /**
     * @param dataset the dataset to use for encoding
     */
    public void setDataset(ArrayList<Data> dataset)
    {
        this.dataset = dataset;
    }
    private ArrayList<Data> dataset;




    private long mStartTime;
    private long mFinishTime;
    private long mFirstTagTime;
    private long mLastTagTime;
    private long mInventoryTagCount;

	public BulkEncodingModel()
	{
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

                    if (getCommander().getDeviceProperties().getInformationCommand().getAsciiProtocol().startsWith(("3")))
                    {
                        // Todo: Workaround for slow performance due to SD-Card logging issues in early Series 3 firmware
                        // Turn off the logging!
                        //ReadLogFileCommand rlCommand = ReadLogFileCommand.synchronousCommand();
                        //rlCommand.setTakeNoAction(TriState.YES);
                        //rlCommand.setCommandLoggingEnabled(TriState.NO);
                        //getCommander().executeCommand(rlCommand);


                    }
                    else
                    {
                        sendMessageNotification("ER: !!! This requires a Series 3 Reader !!!");
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


    private void showDatastoreSize()
    {
        DatastoreCommand command = DatastoreCommand.synchronousCommand();
        command = DatastoreCommand.synchronousCommand();
        command.setShowAvailableSize(TriState.YES);
        getCommander().executeCommand(command);
        if( command.isSuccessful() )
        {
            sendMessageNotification(String.format(Locale.US, "SZ: %d,  IC: %d", command.getSize(), command.getItemCount()));
        }
        else
        {
            sendMessageNotification("!!! Unable to get Datastore size !!!");
        }
    }

    public void encode() throws ModelException
    {
        performTask(new Runnable()
        {
            @Override
            public void run()
            {
                // The STORM Bank to Use - Reader's allow Banks 1-8
                int bankNumber = 1;

                // The RF Mode to use
                int rfMode = 241;

                BankCommand baCommand;
                DatastoreCommand dsCommand;

                // Enable ASCII STORM
                sendMessageNotification("\nEnabling ASCII STORM mode...");
                baCommand = new BankCommand();
                baCommand.setReset(TriState.YES);
                baCommand.setBankNumber(bankNumber);
                getCommander().executeCommand(baCommand);

                sendMessageNotification("\nResetting Datastore");

                long startTime = System.nanoTime();

                // Prepare the Reader's Datastore
                dsCommand = DatastoreCommand.synchronousCommand();
                dsCommand.setReset(TriState.YES);
                dsCommand.setMatchOffset(getMatchOffset());
                getCommander().executeCommand(dsCommand);

                showDatastoreSize();

                // Prepare the Reader's Datastore
                if( loadDatastore() )
                {
                    showDatastoreSize();

                    TransponderSelectCommand tsCommand;
                    InventoryCommand ivCommand;
                    ReadTransponderCommand rdCommand;
                    WriteTransponderCommand wrCommand;
                    AccessCommand acCommand;
                    LockCommand loCommand;
                    KillCommand kiCommand;
                    BlockPermalockTransponderCommand bpCommand;

                    startTime = System.nanoTime();

                    // In STORM mode the Bank can have up to 10 actions per tag
                    // The configuration for each action is supplied by executing ASCII commands.
                    // The commands are allocated to a Slot, in the current Bank, when executed
                    // In STORM mode, most commands ONLY CONFIGURE the Reader's Bank parameters
                    // Use the execute
                    // Note new ASCII commands were introduced to support operations that do not have an existing ASCII protocol equivalent.
                    //
                    // A BankConfigurationGroup is used for efficiency
                    // Multiple commands can be added to the group.
                    // When executed, all the commands are issued as one send
                    // The group command waits until the last command response has been received
                    //
                    BankConfigurationGroup bcgCommand = BankConfigurationGroup.synchronousCommand();

                    // Configure the Bank operations.
                    bcgCommand = BankConfigurationGroup.synchronousCommand();

                    baCommand = BankCommand.synchronousCommand();
                    baCommand.setBankNumber(bankNumber);

                    bcgCommand.append(baCommand);


                    // Configure the inventory
                    // This will be repeated via Multibank so just do one Round at a time
                    ivCommand = new InventoryCommand();
                    ivCommand.setResetParameters(TriState.YES);

                    ivCommand.setQueryTarget(QueryTarget.TARGET_A);
                    ivCommand.setQuerySelect(QuerySelect.ALL);
                    ivCommand.setQuerySession(QuerySession.SESSION_1);

                    // TID is used to identify the tag so use the FastId operation
                    // This works for most recent tags from Impinj and NXP
                    ivCommand.setUsefastId(TriState.YES);

                    ivCommand.setTagReports(TagReports.NONE | TagReports.EPC );//| TagReports.TID | TagReports.BANK_HEADER | TagReports.SUMMARY);
                    ivCommand.setQValue(8);
                    ivCommand.setMaxQValue(15);
                    ivCommand.setMinQValue(0);
                    ivCommand.setNumberOfMinQCycles(1);
                    ivCommand.setMaxQueriesSinceValidEPC(4);
                    ivCommand.setDualTarget(TriState.NO);

                    ivCommand.setRFMode(rfMode);

                    ivCommand.setHaltOnTags(0);
                    ivCommand.setHaltOnRounds(1);
                    ivCommand.setHaltOnDuration(0);

                    bcgCommand.append(ivCommand);

                    // Use an Access password if requested
                    // The access password is parameter 2 in the Datastore
                    // Note: the Access action occupies 2 (TWO) slots in the Bank but the Reader handles this
                    // (the Kill action also uses 2 slots)
                    if( usePassword() )
                    {
                        acCommand = new AccessCommand();
                        acCommand.setBulkEncodeIndex(2);

                        bcgCommand.append(acCommand);
                    }

                    // Write a new EPC using the value from the Datastore
                    // The new EPC is parameter 1 in the Datastore
                    //
                    // This example assumes that the length of the EPC is currently correct and just writes the new EPC identifier value
                    // Adjust the PC Word length bits to adjust the tag's EPC length
                    //(this is left as an exercise for the developer ;-)
                    wrCommand = new WriteTransponderCommand();
                    wrCommand.setBank(Databank.ELECTRONIC_PRODUCT_CODE);
                    wrCommand.setOffset(2);
                    wrCommand.setBulkEncodeIndex(1);
                    // Make this a block write
                    wrCommand.setWriteMode(WriteMode.BLOCK);

                    bcgCommand.append(wrCommand);

                    if( useUserData())
                    {
                        // Write data to the start of User memory using the value from the Datastore
                        // The UserData is parameter 4 in the Datastore
                        wrCommand = new WriteTransponderCommand();
                        wrCommand.setBank(Databank.USER);
                        wrCommand.setOffset(0);
                        wrCommand.setBulkEncodeIndex(4);
                        // Make this a block write
                        wrCommand.setWriteMode(WriteMode.BLOCK);

                        bcgCommand.append(wrCommand);
                    }



                    // REVIEW AND (UN)COMMENT AS NEEDED
                    //
                    // The Kill and Access passwords could also be set
                    // The command below will clear both (assuming the Reserved bank is not locked or that the Access password is being used )
                    // !!! Note that the BulkEncodeIndex needs to be explicitly cleared here as the previous Write set it and the Reader will retain that value
                    //
                    //String dataToWrite = "0000000000000000"; // Or use your new passwords here
                    //wrCommand = new WriteTransponderCommand();
                    //wrCommand.setBank(Databank.RESERVED);
                    //wrCommand.setBulkEncodeIndex(0);
                    //wrCommand.setOffset(0);
                    //wrCommand.setData(HexEncoding.stringToBytes(dataToWrite));
                    //wrCommand.setLength(dataToWrite.length() / 4);
                    //wrCommand.setWriteMode(WriteMode.BLOCK);

                    //bcgCommand.append(wrCommand);

                    // REVIEW AND (UN)COMMENT AS NEEDED
                    //
                    // The command below will Read the current Kill/Access passwords
                    // (assuming the Reserved bank is not locked or that the Access password is being used )
                    //
                    //rdCommand = new ReadTransponderCommand();
                    //rdCommand.setBank(Databank.RESERVED);
                    //rdCommand.setOffset(0);
                    //rdCommand.setLength(4);

                    //bcgCommand.append(rdCommand);


                    // REVIEW AND (UN)COMMENT AS NEEDED
                    //
                    // The command below will Lock the tag using the given payload
                    // This example actually does not set any lock bits at all
                    // !!! Ensure that any payload used is correct for your use-case as it could permanently lock a tag
                    //
                    //loCommand = new LockCommand();
                    //loCommand.setLockPayload("00000");
                    //
                    //bcgCommand.append(loCommand);


                    // Configure the Multibank
                    // The single bank in use is repeated until all tags in the Datastore have been fully processed
                    // This will typically be that the tag has been fully written as specified above but
                    // in the case of errors each tag is only retried a certain number of times before being ignored
                    // The only stop condition for the multibank is a duration assuming that all tags will be written before then
                    // Adjust this value in accordance with the number of tags to be encoded.
                    baCommand = new BankCommand();
                    baCommand.setMultibankSequence(1);
                    baCommand.setRepeatMultibank(TriState.YES);
                    baCommand.setIncludeMultibankSummary(TriState.YES);
                    baCommand.setMultibankHaltOnTags(0);
                    baCommand.setMultibankHaltOnRounds(0);
                    baCommand.setMultibankHaltOnDuration(5000);

                    bcgCommand.append(baCommand);


                    getCommander().executeCommand(bcgCommand);

                    if( !bcgCommand.getGroupErrors().isEmpty() )
                    {
                        for (BankConfigurationGroup.Error e : bcgCommand.getGroupErrors())
                        {
                            sendMessageNotification("\n!!! Configuration Errors:");
                            sendMessageNotification(e.toString());
                        }
                    }
                    else
                    {
                        // Prepare to execute the Multibank
                        baCommand = BankCommand.synchronousCommand();
                        baCommand.setMultibankExecute(TriState.YES);

                        //
                        // Set the delegates to receive responses to the Bank command
                        //
                        baCommand.setTransponderReceivedDelegate(new IBankTransponderReceivedDelegate<BankTransponderData>()
                        {
                            @Override
                            public void transponderReceived(BankTransponderData transponder)
                            {
                                sendMessageNotification(transponder.getEpc());
                            }
                        });

                        baCommand.setDatastoreSummaryReceivedDelegate(new IDatastoreSummaryReceivedDelegate()
                        {
                            @Override
                            public void summaryReceived(DatastoreSummary summary)
                            {
                                sendMessageNotification("Datastore Summary = " + summary.toString());
                            }
                        });

                        baCommand.setBankSummaryReceivedDelegate(new IBankSummaryReceivedDelegate()
                        {
                            @Override
                            public void summaryReceived(BankSummary summary)
                            {
                                sendMessageNotification("Summary = " + summary.toString());
                            }
                        });

                        baCommand.setMultibankSummaryReceivedDelegate(new IBankSummaryReceivedDelegate()
                        {
                            @Override
                            public void summaryReceived(BankSummary summary)
                            {
                                sendMessageNotification("Multibank Summary = " + summary.toString());
                            }
                        });

                        sendMessageNotification("BankCommand: " + baCommand.getCommandLine());

                        getCommander().executeCommand(baCommand);

                        long finishTime = System.nanoTime();
                        double configurationDuration = (finishTime - startTime)/1000000000.0;
                        sendMessageNotification(String.format(Locale.US, "Execution Complete (in %.2fs)\n", configurationDuration));

                        if (!baCommand.isSuccessful())
                        {
                            sendMessageListNotification(baCommand.getMessages());
                        }
                        sendMessageNotification(baCommand.isSuccessful() ? "Succeeded" : "Failed!");

                        // Inspect the Datastore for the failed tags
                        sendMessageNotification("\nDatastore Records");

                        sendMessageNotification("\nFailed:");
                        MESSAGES.clear();
                        dsCommand = DatastoreCommand.synchronousCommand();
                        dsCommand.setRead(DatastoreRead.SHOW_FAILED);
                        dsCommand.setDatastoreDataReceivedDelegate(new IDatastoreDataReceivedDelegate() {
                            @Override
                            public void dataReceived(DatastoreData data)
                            {
                                MESSAGES.add(data.toString());
                            }
                        });

                        getCommander().executeCommand(dsCommand);
                        sendMessageListNotification(MESSAGES);


                        sendMessageNotification("\nUnprocessed:");
                        MESSAGES.clear();
                        dsCommand = DatastoreCommand.synchronousCommand();
                        dsCommand.setRead(DatastoreRead.SHOW_UNPROCESSED);
                        dsCommand.setDatastoreDataReceivedDelegate(new IDatastoreDataReceivedDelegate() {
                            @Override
                            public void dataReceived(DatastoreData data)
                            {
                                MESSAGES.add(data.toString());
                            }
                        });

                        getCommander().executeCommand(dsCommand);
                        sendMessageListNotification(MESSAGES);

                    }
                }


                // Disable ASCII STORM
                sendMessageNotification("\nDisabling ASCII STORM mode...");
                baCommand = new BankCommand();
                baCommand.setBankNumber(0);
                getCommander().executeCommand(baCommand);
            }

        });
    }

    public void retry() throws ModelException
    {
        performTask(new Runnable()
        {
            @Override
            public void run()
            {
                int bankNumber = 1;

                BankCommand baCommand;
                DatastoreCommand dsCommand;

                // Enable ASCII STORM
                sendMessageNotification("\nEnabling ASCII STORM mode...");
                baCommand = new BankCommand();
                baCommand.setBankNumber(bankNumber);
                getCommander().executeCommand(baCommand);

                sendMessageNotification("\nResetting Datastore");

                long startTime = System.nanoTime();

                // Only reset the Reader's Datastore Status Bytes to allow failed tags to be retried
                dsCommand = DatastoreCommand.synchronousCommand();
                dsCommand.setClearStatusBytes(DatastoreClearStatusMode.RESET_FAILED_RECORDS);
                getCommander().executeCommand(dsCommand);

                // Prepare to execute the Multibank
                baCommand = BankCommand.synchronousCommand();
                baCommand.setMultibankExecute(TriState.YES);

                //
                // Set the delegates to receive responses to the Bank command
                //
                baCommand.setTransponderReceivedDelegate(new IBankTransponderReceivedDelegate<BankTransponderData>()
                {
                    @Override
                    public void transponderReceived(BankTransponderData transponder)
                    {
                        sendMessageNotification(transponder.getEpc());
                    }
                });

                baCommand.setDatastoreSummaryReceivedDelegate(new IDatastoreSummaryReceivedDelegate()
                {
                    @Override
                    public void summaryReceived(DatastoreSummary summary)
                    {
                        sendMessageNotification("Datastore Summary = " + summary.toString());
                    }
                });

                baCommand.setBankSummaryReceivedDelegate(new IBankSummaryReceivedDelegate()
                {
                    @Override
                    public void summaryReceived(BankSummary summary)
                    {
                        sendMessageNotification("Summary = " + summary.toString());
                    }
                });

                baCommand.setMultibankSummaryReceivedDelegate(new IBankSummaryReceivedDelegate()
                {
                    @Override
                    public void summaryReceived(BankSummary summary)
                    {
                        sendMessageNotification("Multibank Summary = " + summary.toString());
                    }
                });

                sendMessageNotification("BankCommand: " + baCommand.getCommandLine());

                getCommander().executeCommand(baCommand);

                long finishTime = System.nanoTime();
                double configurationDuration = (finishTime - startTime)/1000000000.0;
                sendMessageNotification(String.format(Locale.US, "Execution Complete (in %.2fs)\n", configurationDuration));

                if (!baCommand.isSuccessful())
                {
                    sendMessageListNotification(baCommand.getMessages());
                }
                sendMessageNotification(baCommand.isSuccessful() ? "Succeeded" : "Failed!");

                // Inspect the Datastore for the failed tags
                sendMessageNotification("\nDatastore Records");

                sendMessageNotification("\nFailed:");
                MESSAGES.clear();
                dsCommand = DatastoreCommand.synchronousCommand();
                dsCommand.setRead(DatastoreRead.SHOW_FAILED);
                dsCommand.setDatastoreDataReceivedDelegate(new IDatastoreDataReceivedDelegate() {
                    @Override
                    public void dataReceived(DatastoreData data)
                    {
                        MESSAGES.add(data.toString());
                    }
                });

                getCommander().executeCommand(dsCommand);
                sendMessageListNotification(MESSAGES);


                sendMessageNotification("\nUnprocessed:");
                MESSAGES.clear();
                dsCommand = DatastoreCommand.synchronousCommand();
                dsCommand.setRead(DatastoreRead.SHOW_UNPROCESSED);
                dsCommand.setDatastoreDataReceivedDelegate(new IDatastoreDataReceivedDelegate() {
                    @Override
                    public void dataReceived(DatastoreData data)
                    {
                        MESSAGES.add(data.toString());
                    }
                });

                getCommander().executeCommand(dsCommand);
                sendMessageListNotification(MESSAGES);



                // Disable ASCII STORM
                sendMessageNotification("\nDisabling ASCII STORM mode...");
                baCommand = new BankCommand();
                baCommand.setBankNumber(0);
                getCommander().executeCommand(baCommand);

            }

        });
    }


    // Use the current dataset to load the Datastore

    /**
     * Loads the current dataset into the Reader's Datastore
     * @return true if load was successful
     */
    private boolean loadDatastore()
    {
        boolean isSuccessful = true;
        long startTime = System.nanoTime();

        ArrayList<Data> dataset = getDataset();

        BankConfigurationGroup bcgCommand = BankConfigurationGroup.synchronousCommand();

        sendMessageNotification(String.format(Locale.US, "\nLoading Datastore...\nSource: %d items", dataset.size()));

        // Load Datastore
        DatastoreCommand dsCommand;
        for(Data data : dataset)
        {
            dsCommand = new DatastoreCommand();

            String id = data.getIdentifier();
            if( useTidIdentifier() )
            {
                dsCommand.setMatchTID(id);
            }
            else
            {
                dsCommand.setMatchEPC(id);
            }

            dsCommand.setParameter1(data.getNewEPC());

            if( usePassword() )
            {
                // Passwords (Access and Kill) need to be split into 2 16-bit values when used from the Datastore
                // These two values have to be consecutive Datastore parameters
                // and are referenced in the BulkEncoding parameter using the lower parameter index
                dsCommand.setParameter2(data.getPassword().substring(0,4));
                dsCommand.setParameter3(data.getPassword().substring(4,8));
            }

            dsCommand.setParameter4(data.getUserData());


            boolean wasCommandAdded = bcgCommand.append(dsCommand);

            if( !wasCommandAdded )
            {
                sendMessageNotification("Splitting group...");
                // Execute current group
                getCommander().executeCommand(bcgCommand);
                isSuccessful = bcgCommand.getGroupErrors().isEmpty();
                if( isSuccessful )
                {
                    // Create a new group starting with the unsent command
                    bcgCommand = BankConfigurationGroup.synchronousCommand();
                    bcgCommand.append(dsCommand);
                }
                else
                {
                    break;
                }
            }
        }

        if( isSuccessful )
        {
            getCommander().executeCommand(bcgCommand);
            isSuccessful = bcgCommand.getGroupErrors().isEmpty();
        }

        reportDatastoreErrors(bcgCommand);

        long finishTime = System.nanoTime();
        double configurationDuration = (finishTime - startTime)/1000000000.0;
        sendMessageNotification(String.format(Locale.US, "Loading Done (in %.2fs)\n", configurationDuration));

        return isSuccessful;
    }

    private void reportDatastoreErrors(BankConfigurationGroup bcgCommand)
    {
        if( !bcgCommand.getGroupErrors().isEmpty() )
        {
            for (BankConfigurationGroup.Error e : bcgCommand.getGroupErrors())
            {
                sendMessageNotification("\n!!! Configuration Errors:");
                sendMessageNotification(e.toString());
            }
        }
    }

}


