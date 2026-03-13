//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.uk.tsl.rfid.samples.hf_lf_support;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.ModelException;
import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.BatteryStatusCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.hflf.InventoryTagCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.hflf.MifareKey;
import com.uk.tsl.rfid.asciiprotocol.commands.hflf.ReadTagCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.hflf.RfPassThroughCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.IBarcodeReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.hflf.HfLfTransponderData;
import com.uk.tsl.rfid.asciiprotocol.responders.hflf.IHfLfTransponderReceivedDelegate;
import com.uk.tsl.utils.HexEncoding;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;

import static com.uk.tsl.rfid.asciiprotocol.commands.hflf.MifareUtilities.SectorFromBlockOffset;

public class HFLFSupportModel extends ModelBase
{
    // Gets the Read Tag offset
    public int getOffset() { return mOffset; }

    // Sets the Read Tag offset
    public void setOffset(int offset) { this.mOffset = offset; }

    // Gets the Read Tag number of blocks
    public int getNumberOfBlocks() { return mNumberOfBlocks; }

    // Sets the Read Tag number of blocks
    public void setNumberOfBlocks(int numberOfBlocks) { this.mNumberOfBlocks = numberOfBlocks; }


    public boolean enabled() { return mEnabled; }

    public void setEnabled(boolean state)
    {
        boolean oldState = mEnabled;
        mEnabled = state;

        // Update the commander for state changes
        if(oldState != state) {
            if( mEnabled ) {
                // Listen for transponders
                getCommander().addResponder(mHfLfTagResponder);
                // Listen for barcodes
                getCommander().addResponder(mBarcodeResponder);
            } else {
                // Stop listening for transponders
                getCommander().removeResponder(mHfLfTagResponder);
                // Stop listening for barcodes
                getCommander().removeResponder(mBarcodeResponder);
            }

        }
    }


    // Backing field for the Offset
    private int mOffset;
    // Backing field for the NumberOfBlocks
    private int mNumberOfBlocks;

    // The number of transponders that responded (typically this will be one but multi-read option can return more)
    private int mTransponderCount;

    // The command for performing memory reads of (supported) HF/LF Tags
    private ReadTagCommand mReadCommand;

    // The command for inventorying HF/LF Tags
    private InventoryTagCommand mInventoryTagCommand;

    // The responder for inventorying HF/LF Tags
    private final InventoryTagCommand mHfLfTagResponder;

    // The command to use as a responder to capture incoming barcode responses
    private final BarcodeCommand mBarcodeResponder;

    // The command for performing memory reads of (supported) HF/LF Tags
    private RfPassThroughCommand mPassThroughCommand;

    // Backing field for Enabled property
    private boolean mEnabled;



    /**
     * A class to demonstrate the use of the AsciiProtocol library to read and write to HF/LF transponders
     */
    public HFLFSupportModel()
    {
        mOffset = 0;
        mNumberOfBlocks = 1;

        // This command is used to capture barcode responses
        mBarcodeResponder = new BarcodeCommand();
        mBarcodeResponder.setCaptureNonLibraryResponses(true);
        mBarcodeResponder.setUseEscapeCharacter(TriState.YES);
        mBarcodeResponder.setBarcodeReceivedDelegate(new IBarcodeReceivedDelegate() {
            @Override
            public void barcodeReceived(String barcode) {
                sendMessageNotification(barcode);
            }
        });


        // Create a responder to capture responses from triggered HF/LF inventory
        mHfLfTagResponder = new InventoryTagCommand();
        mHfLfTagResponder.setCaptureNonLibraryResponses(true);
        mHfLfTagResponder.setTransponderReceivedDelegate(new IHfLfTransponderReceivedDelegate() {
            @Override
            public void transponderReceived(HfLfTransponderData tagData, boolean b)
            {
                String msg = String.format(Locale.US, "Tag ID: %s\nType: %s\nDescription: %s\n",
                        tagData.getHexIdentity(),
                        tagData.getTagType(),
                        tagData.getTagTypeDescription()
                );

                sendMessageNotification(msg);
            }
        });

    }

    //
    // Reset the reader configuration to default command values
    //
    public void resetDevice()
    {
        if(getCommander().isConnected()) {
            getCommander().executeCommand(new FactoryDefaultsCommand());

            // Report the battery level when Reader connects
            BatteryStatusCommand bCommand = BatteryStatusCommand.synchronousCommand();
            getCommander().executeCommand(bCommand);
            int batteryLevel = bCommand.getBatteryLevel();
            sendMessageNotification(String.format(Locale.US, "Battery level: %d%%", batteryLevel));

            InventoryTagCommand itCommand = InventoryTagCommand.synchronousCommand();
            itCommand.setTakeNoAction(TriState.YES);
            itCommand.setIncludeTagType(TriState.YES);

            // Scan for multiple ISO 14443A Tags
            // Warning: No other tag types will be recognised while in this mode
            //
            // Uncomment line below to enable option for Trigger scans
            //itCommand.setSearchForMultipleIso14443ATags(TriState.YES);

            getCommander().executeCommand(itCommand);
            if (itCommand.isSuccessful())
            {
                sendMessageNotification("Reader configured to return tag types\n");
            }
            else
            {
                sendMessageNotification("\n\n!!! Error: unable to configure reader !!!\n\n");
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // Read
    //----------------------------------------------------------------------------------------------
    //
    // Uses the ReadTagCommand to read a number of blocks from a given location on the tag in front of the reader
    //
    // This uses Synchronous (blocking) commands so they are executed on a background thread
    //
    public void read()
    {
        try {
            performTask(new Runnable() {
                @Override
                public void run() {

                    executeReadTagCommand();

                    sendMessageNotification( String.format(Locale.US,"Time taken: %.2fs\n", getTaskExecutionDuration()) );
                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }
    }


    private void executeReadTagCommand()
    {
        // This example uses hard-coded values that are typical for new MIFARE tags
        MifareKey mifareKey = MifareKey.A;
        String mifarePasswordA = "ffffffffffff";
        String mifarePasswordB = "ffffffffffff";
        String mifarePassword = mifareKey == MifareKey.A ? mifarePasswordA : mifarePasswordB;

        int mifareSector = SectorFromBlockOffset(getOffset());

        sendMessageNotification("");
        sendMessageNotification(String.format(Locale.US, "Reading %d blocks from offset %d ...",
                getNumberOfBlocks(),
                getOffset()
        ));

        ReadTagCommand rtCommand = ReadTagCommand.synchronousCommand();
        rtCommand.setResetParameters(TriState.YES);

        // These parameters are not needed unless reading MIFARE tags
        rtCommand.setAccessPassword(mifarePassword);
        rtCommand.setAccessKey(mifareKey);
        rtCommand.setAccessSector(mifareSector);

        // Specify where and how many blocks to read
        rtCommand.setOffset(getOffset());
        rtCommand.setNumberOfBlocks(getNumberOfBlocks());

        // Handle tag responses (there will be only one!)
        rtCommand.setTransponderReceivedDelegate(new IHfLfTransponderReceivedDelegate() {
            @Override
            public void transponderReceived(HfLfTransponderData transponder, boolean moreAvailable)
            {
                sendMessageNotification(String.format(Locale.US, "Identifier: %s\nData:\n%s\n",
                        transponder.getHexIdentity(),
                        transponder.getData()
                ));

            }
        });


        getCommander().executeCommand(rtCommand);

        // Display any messages
        sendMessageListNotification(rtCommand.getMessages());

        if( !rtCommand.isSuccessful() )
        {
            sendMessageNotification("Error occurred !!!");
        }

        sendMessageNotification("");

    }


    //----------------------------------------------------------------------------------------------
    // PassThroughCommand Examples
    //
    // See the HF/LF module protocol document from the Reader downloads page
    // e.g. https://www.tsl.com/downloads/tsl-products/2173-downloads/
    //
    //----------------------------------------------------------------------------------------------

    //
    // Scan for multiple ISO tags using the RF pass-through command
    //
    // This uses Synchronous (blocking) commands so they are executed on a background thread
    //
    public void isoMultiScan()
    {
        try {
            performTask(new Runnable() {
                @Override
                public void run() {

                    executeIsoMultiScanCommand();

                    sendMessageNotification( String.format(Locale.US,"Time taken: %.2fs\n", getTaskExecutionDuration()) );
                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }
    }

    private void executeIsoMultiScanCommand()
    {
        sendMessageNotification("\nSearching for multiple ISO14443A\n(using passthrough command)...");
        sendMessageNotification("");

        // ISO14443A_SearchMultiTag - limit to 255 tags
        final String rfCommand = "1208FF";

        sendMessageNotification("Sending command: " + rfCommand );

        RfPassThroughCommand ptCommand = RfPassThroughCommand.synchronousCommand();

        ptCommand.setCommandData(HexEncoding.stringToBytes(rfCommand));
        getCommander().executeCommand(ptCommand);

        // Display result
        byte[] response = ptCommand.getResponseData();
        if(response.length > 0)
        {
            String hexResponse = HexEncoding.bytesToString(response);
            sendMessageNotification("\nResponse Received (Hex):\n" + hexResponse );

            // split out each returned id
            if( response[1] == 0)
            {
                sendMessageNotification("\nResult: false (No tags found)");
            }
            else
            {
                sendMessageNotification("\nResult: true");
                int tagCount = response[2];
                sendMessageNotification("Tag count: " + tagCount );

                int idByteCount = response[3];
                sendMessageNotification("Total ID Byte count: " + idByteCount );

                int offset = 4;

                for( int i = 0; i < tagCount; i++)
                {
                    int idLength = response[offset];
                    byte[] tagIdBytes = Arrays.copyOfRange(response, offset + 1, offset + 1 + idLength);
                    sendMessageNotification("ID: " + HexEncoding.bytesToString(tagIdBytes) );
                    offset += idLength + 1;
                }

            }
        }
        else
        {
            sendMessageNotification("Unexpected error: maybe no response from reader !??");
        }

    }


    //
    // Obtain the RF module version using the RF pass-through command
    //
    //
    // This uses Synchronous (blocking) commands so they are executed on a background thread
    //
    public void rfModuleVersion()
    {
        try {
            performTask(new Runnable() {
                @Override
                public void run() {

                    executeVersionCommand();

                    sendMessageNotification( String.format(Locale.US,"Time taken: %.2fs\n", getTaskExecutionDuration()) );
                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }
    }

    private void executeVersionCommand()
    {
        sendMessageNotification("\nIssuing RF Module GetVersion command\n(using passthrough command)...");
        sendMessageNotification("");

        // Module GetVersionString command - max 255 (FF) bytes
        final String rfCommand = "0004FF";

        sendMessageNotification("Sending command: " + rfCommand );

        RfPassThroughCommand ptCommand = RfPassThroughCommand.synchronousCommand();
        // Ask for engine version string
        ptCommand.setCommandData(HexEncoding.stringToBytes(rfCommand));
        getCommander().executeCommand(ptCommand);

        // Display result
        byte[] response = ptCommand.getResponseData();
        if(response.length > 0)
        {
            String version = HexEncoding.bytesToString(response);
            sendMessageNotification("\nResponse Received (Hex):\n" + version );

            try
            {
                byte[] responseBytes = Arrays.copyOfRange(response, 2, response.length);
                String versionAscii = new String(responseBytes, "UTF-8");
                sendMessageNotification("\nVersion:\n" + versionAscii );
            }
            catch (UnsupportedEncodingException e)
            {
                sendMessageNotification("\nError: unable to convert to ASCII");
                e.printStackTrace();
            }
        }
        else
        {
            sendMessageNotification("\nUnexpected error: maybe no response from reader !??");
        }

    }
    


}
