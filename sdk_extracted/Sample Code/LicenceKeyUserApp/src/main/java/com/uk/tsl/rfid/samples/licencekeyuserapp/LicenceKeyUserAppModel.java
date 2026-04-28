package com.uk.tsl.rfid.samples.licencekeyuserapp;

import android.content.Context;
import android.os.Message;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.InventoryCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.LicenceKeyCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.VersionInformationCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.DeleteConfirmation;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.IBarcodeReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

// !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!!
//
// This sample demonstrates how the Public Key can be used to verify that a Reader has been authorised.
// The Public key is loaded from the App's resources. Since knowledge of the public key does not allow the
// generation of the signature required to authorise a reader, distribution of this key is safe.
//
// See the LicenceKey sample project for details of the keys used.
//
// !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!!


public class LicenceKeyUserAppModel extends ModelBase
{
	// Model busy state changed message
	public static final int AUTHORISATION_STATE_CHANGED_NOTIFICATION = 100;

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
				// Stop listening for transponders
				getCommander().removeResponder(mInventoryResponder);
				// Stop listening for barcodes
				getCommander().removeResponder(mBarcodeResponder);
			}
		}
	}

	/**
	 * @return the readerAuthorised
	 */
	public final boolean isReaderAuthorised() {
		return mReaderAuthorised;
	}

	/**
	 * @param readerAuthorised the readerAuthorised to set
	 */
	public final void setReaderAuthorised(boolean readerAuthorised) {
		mReaderAuthorised = readerAuthorised;
	}

	/**
	 * @return the onlyAuthorisedReaderAllowed
	 */
	public final boolean isOnlyAuthorisedReaderAllowed() {
		return mOnlyAuthorisedReaderAllowed;
	}

	/**
	 * @param onlyAuthorisedReaderAllowed the onlyAuthorisedReaderAllowed to set
	 */
	public final void setOnlyAuthorisedReaderAllowed(
			boolean onlyAuthorisedReaderAllowed) {
		mOnlyAuthorisedReaderAllowed = onlyAuthorisedReaderAllowed;
	}

	/**
	 * @return the secret
	 */
	public final String getSecret() {
		return mSecret;
	}

	/**
	 * @param secret the secret to set
	 */
	public final void setSecret(String secret) {
		mSecret = secret;
	}

	//	 The minimum ASCII protocol version that suppports the licence key command
	public static final String MINIMUM_ASCII_PROTOCOL_VERSION_FOR_LICENCE_KEY_COMMAND = "2.2";

	// Backing fields for properties
	private boolean mEnabled;
	private boolean mReaderAuthorised;
	private boolean mOnlyAuthorisedReaderAllowed;
	private String mSecret;

    public android.content.Context getContext()
    {
        return mContext;
    }
    public void setContext(android.content.Context mContext)
    {
        this.mContext = mContext;
    }

    private Context mContext;

    // This key can be shared
    private PublicKey mPublicKey;

    // The type of signature used for the LicenceKey
    private final String signatureType = "SHA256withECDSA";




    // Used to issue message for inventory commands that receive no tags
	private boolean mAnyTagSeen;

	// The command to use as a responder to capture incoming inventory responses
	private InventoryCommand mInventoryResponder;
	// The command used to issue commands
	private InventoryCommand mInventoryCommand;

	// The command to use as a responder to capture incoming barcode responses
	private BarcodeCommand mBarcodeResponder;
	// The command used to issue barcode commands
	private BarcodeCommand mBarcodeCommand;

	// The command used to extract version information from the reader
	private VersionInformationCommand mVersionCommand; 
	
	public LicenceKeyUserAppModel(Context context)
	{
        mContext = context;

		// This command is used to obtain information about the reader
		mVersionCommand = VersionInformationCommand.synchronousCommand();

		// This is the command that will be used to perform configuration changes and inventories
		mInventoryCommand = new InventoryCommand();

		// Configure the type of inventory
		mInventoryCommand.setIncludeTransponderRssi(TriState.YES);
		mInventoryCommand.setIncludeChecksum(TriState.YES);
		mInventoryCommand.setIncludePC(TriState.YES);
		
		// Use an InventoryCommand as a responder to capture all incoming inventory responses
		mInventoryResponder = new InventoryCommand();

		// Also capture the responses that were not from App commands 
		mInventoryResponder.setCaptureNonLibraryResponses(true);

		// Notify when each transponder is seen
		mInventoryResponder.setTransponderReceivedDelegate(new ITransponderReceivedDelegate()
		{

			int mTagsSeen = 0;
			@Override
			public void transponderReceived(TransponderData transponder, boolean moreAvailable)
			{
				if( isReaderAuthorised() || !isOnlyAuthorisedReaderAllowed() )
				{
					mAnyTagSeen = true;

					String infoMsg = String.format(Locale.US, "\nRSSI: %d  PC: %04X  CRC: %04X", transponder.getRssi(), transponder.getPc(), transponder.getCrc());
					sendMessageNotification("EPC: " + transponder.getEpc() + infoMsg );
					mTagsSeen++;
					if( !moreAvailable) {
						sendMessageNotification("");
						Log.d("TagCount",String.format("Tags seen: %s", mTagsSeen));
					}
				}
			}
		});

		mInventoryResponder.setResponseLifecycleDelegate( new ICommandResponseLifecycleDelegate() {
			
			@Override
			public void responseEnded()
			{
				if( isReaderAuthorised() || !isOnlyAuthorisedReaderAllowed() )
				{
					if( !mAnyTagSeen && mInventoryCommand.getTakeNoAction() != TriState.YES)
					{
						sendMessageNotification("No transponders seen");
					}
					mInventoryCommand.setTakeNoAction(TriState.NO);
				}
			}
			
			@Override
			public void responseBegan() { mAnyTagSeen = false; }
		});

		// This is the command that will be used to issue barcode scans (with default parameters)
		mBarcodeCommand = new BarcodeCommand();
		
		// This command is used to capture barcode responses
		mBarcodeResponder = new BarcodeCommand();
		mBarcodeResponder.setCaptureNonLibraryResponses(true);
		mBarcodeResponder.setUseEscapeCharacter(TriState.YES);
		mBarcodeResponder.setBarcodeReceivedDelegate(new IBarcodeReceivedDelegate()
		{
			@Override
			public void barcodeReceived(String barcode)
			{
				if( isReaderAuthorised() || !isOnlyAuthorisedReaderAllowed() )
				{
					sendMessageNotification("BC: " + barcode);
				}
			}
		});

        mPublicKey = (PublicKey) loadKey(R.raw.ec_public_key_sample);
	}

	
	//
	// Reset the reader configuration for the switch actions, inventory and barcode commands
	//
	public void resetDevice()
	{
		if(getCommander().isConnected()) {
			getCommander().executeCommand(new FactoryDefaultsCommand());
		}
	}

	/**
	 * @param versionString The string form of the version number
	 * @return a numeric value that allows version number strings to be compared
	 */
	private static int comparableVersionValue(String versionString)
	{
		String[] parts = versionString.split("\\.");
		if( parts.length == 0 || parts.length > 3) return -1;

		try
		{
			int scale = 1 << 16;
			int value = 0;
			for( String part : parts)
			{
				int digitValue = Integer.parseInt(part);
				value += digitValue * scale;
				scale >>= 8;
			}
			return value;
		}
		catch( Exception e)
		{
			return -1;
		}
		
	}
	
	//
	// Update the reader configuration from the command
	// Call this after each change to the model's command
	//
	public void updateConfiguration()
	{
		if(getCommander().isConnected())
		{
			// Configure the inventory operations
			mInventoryCommand.setTakeNoAction(TriState.YES);
			getCommander().executeCommand(mInventoryCommand);

			// Update the connected reader version information
			getCommander().executeCommand(mVersionCommand);
			
			if( comparableVersionValue(mVersionCommand.getAsciiProtocol())
					< comparableVersionValue(MINIMUM_ASCII_PROTOCOL_VERSION_FOR_LICENCE_KEY_COMMAND))
			{
				sendMessageNotification(String.format("Reader does not support licence keys\nRequires ASCII protocol: %s\nReader ASCII protocol: %s\n",
						MINIMUM_ASCII_PROTOCOL_VERSION_FOR_LICENCE_KEY_COMMAND,
						mVersionCommand.getAsciiProtocol()));
			}

			validateReader();
		}
	}

	
	//
	// Perform an inventory scan with the current command parameters
	//
	public void scan()
	{
		if( mCommander.isConnected())
		{
			if( isReaderAuthorised() || !isOnlyAuthorisedReaderAllowed() )
			{
				if(getCommander().isConnected())
				{
					mInventoryCommand.setTakeNoAction(TriState.NO);
					getCommander().executeCommand(mInventoryCommand);
				}
				else
				{
					sendMessageNotification("Reader not connected!");
				}
			}
			else
			{
				sendMessageNotification("Reader NOT authorised!");
			}
		}
		else
		{
			 sendMessageNotification("Reader not connected!\n");
		}
	}


	//
	// Perform a barcode scan with the current command parameters
	//
	public void barcodeScan()
	{
		if( mCommander.isConnected())
		{
			if( isReaderAuthorised() || !isOnlyAuthorisedReaderAllowed() )
			{
				if(getCommander().isConnected())
				{
					mBarcodeCommand.setTakeNoAction(TriState.NO);
					getCommander().executeCommand(mBarcodeCommand);
				}
			}
			else
			{
				sendMessageNotification("Reader NOT authorised!");
			}
		}
		else
		{
			 sendMessageNotification("Reader not connected!\n");
		}
	}


	/**
	 * Send a message to the client using the current Handler
	 * 
	 * @param message The message to send as String
	 */
	protected void sendAuthorisationStateNotification(String message)
	{
		if( mHandler != null )
		{
			Message msg = mHandler.obtainMessage(AUTHORISATION_STATE_CHANGED_NOTIFICATION, message);
			mHandler.sendMessage(msg);
		}
	}

    /**
     * Check that the given licence key contains the digital signature for the given Reader properties
     *
     * @param serialNumber the serial number to include in the licence key
     * @param bluetoothAddress the bluetooth address to include in the licence key
     * @return true if the
     */
    private boolean validateLicenceKey(String licenceKey, String serialNumber, String bluetoothAddress )
    {
        boolean isValid = false;

        String licenceKeySourceValue = String.format("%s_%s", serialNumber, bluetoothAddress);
        try
        {
            // Convert signature to bytes
            byte[] signature = Base64.decode(licenceKey,Base64.DEFAULT);

            Signature s = Signature.getInstance(signatureType);
            s.initVerify((PublicKey) mPublicKey);
            s.update(licenceKeySourceValue.getBytes());
            isValid = s.verify(signature);
        }
        catch (Exception ignored){}

        return isValid;
    }



    //
    // Check to see if the reader contains a valid licence key
    //
    public void validateReader()
    {
        boolean isAuthorisedReader = false;
        String message = null;

        if( mCommander.isConnected() )
        {
            // Retrieve the current licence key
            LicenceKeyCommand licenceKeyCommand = LicenceKeyCommand.synchronousCommand();
            mCommander.executeCommand(licenceKeyCommand);

            if( licenceKeyCommand.isSuccessful() )
            {
                message = String.format("Reader:\n%s\n\n", mVersionCommand.getSerialNumber());
                message = String.format("%sReader Licence Key:\n%s\n\n", message, licenceKeyCommand.getLicenceKey());

                isAuthorisedReader = validateLicenceKey(licenceKeyCommand.getLicenceKey(), mVersionCommand.getSerialNumber(), mVersionCommand.getBluetoothAddress());

                message = String.format("%sLicence Key Valid: : %s\n\n", message, isAuthorisedReader);
            }
            else
            {
                message = "Unable to validate reader!\n";
            }
        }

        setReaderAuthorised(isAuthorisedReader);
        sendAuthorisationStateNotification(message);
    }

    //
    // Loads the keys from resource files
    //
    private Key loadKey(int resourceId)
    {
        InputStream inStream = null;
        InputStreamReader sr = null;
        BufferedReader br = null;
        Key key = null;
        try
        {
            // PEM markers
            final String keyMarker = "KEY-----";
            final String beginMarker = "-----BEGIN";
            final String endMarker = "-----END";

            inStream = getContext().getResources().openRawResource(resourceId);

            sr = new InputStreamReader(inStream);
            br = new BufferedReader(sr);

            // Find the start of the key
            String line = br.readLine();
            while(line != null && !(line.startsWith(beginMarker) && line.endsWith(keyMarker)))
            {
                line = br.readLine();
            }

            // If cannot read resource then abort

            if( line == null ) return key;
            // Preserve the key type
            int keyPos = line.indexOf(keyMarker);
            String keyType = line.substring(beginMarker.length(), keyPos).trim();

            // Now read in the Base64 key data
            StringBuilder sb = new StringBuilder();
            line = br.readLine();
            while(line != null && !(line.startsWith(endMarker) && line.endsWith(keyMarker)))
            {
                sb.append(line);
                line = br.readLine();
            }

            // Get the raw bytes
            byte[] rawBytes  = Base64.decode(sb.toString(), Base64.DEFAULT);

            // Determine the key type
            KeyFactory kf = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC);
            if( keyType.contains("PUBLIC" ))
            {
                key = kf.generatePublic(new X509EncodedKeySpec(rawBytes));

            } else if( keyType.contains("PRIVATE" ))
            {
                key = kf.generatePrivate(new PKCS8EncodedKeySpec(rawBytes));
            } else
            {
                return key;
            }


            return key;

        }
        catch (Exception e )
        {
            // Report error
            Log.e("LKM", e.getMessage());
        } finally
        {
            try
            {
                // Close streams
                if (br != null) br.close();
                if (sr != null) sr.close();
                if (inStream != null) inStream.close();
            }
            catch (Exception e)
            {
                // Report error
                Log.e("LKM", e.getMessage());
            }
        }

        return key;
    }
}
