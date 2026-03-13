package com.uk.tsl.rfid.samples.licencekey;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import android.content.Context;
import android.os.Message;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.LicenceKeyCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.VersionInformationCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.DeleteConfirmation;

// !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!!
//
// To simplify the sample project, the Public/Private keys are loaded from the App's resources
// The keys are ECDSA P-256 and encoded format is PEM PKCS#8.
// The keys were generated here: https://keytool.online/
//
// In "Real World" use, consider carefully how you will securely distribute the private key.
// Each Android device that can "authorise" Readers will need to use the same private key.
//
// The AndroidKeystore can import encrypted keys but demonstration of its use is outside the
// scope of this sample.
// https://developer.android.com/privacy-and-security/keystore#ImportingEncryptedKeys
//
// !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!! !!!


public class LicenceKeyModel extends ModelBase
{
	// Model busy state changed message
	public static final int AUTHORISATION_STATE_CHANGED_NOTIFICATION = 100;

	public boolean enabled() { return mEnabled; }

	public void setEnabled(boolean state)
	{
		mEnabled = state;
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


	//	 The minimum ASCII protocol version that suppports the licence key command
	public static final String MINIMUM_ASCII_PROTOCOL_VERSION_FOR_LICENCE_KEY_COMMAND = "2.2";

	// Backing fields for properties
	private boolean mEnabled;
	private boolean mReaderAuthorised;

    public android.content.Context getContext()
    {
        return mContext;
    }

    public void setContext(android.content.Context mContext)
    {
        this.mContext = mContext;
    }

    private Context mContext;

    // This key MUST remain private
    //
	private PrivateKey mPrivateKey;

    // This key can be shared
    private PublicKey mPublicKey;

    // The type of signature used for the LicenceKey
    private final String signatureType = "SHA256withECDSA";


    // The command used to extract version information from the reader
	private VersionInformationCommand mVersionCommand; 
	

	public LicenceKeyModel(Context context)
	{
        mContext = context;

		// This command is used to obtain information about the reader
		mVersionCommand = VersionInformationCommand.synchronousCommand();

        loadKeys();
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
     * Generate the licence key digital signature for the given Reader properties
     *
     * @param serialNumber the serial number to include in the licence key
     * @param bluetoothAddress the bluetooth address to include in the licence key
     * @return the licence key created from the input parameters
     */
    private String createLicenceKey(String serialNumber, String bluetoothAddress)
    {
        String licenceKey;
        String licenceKeySourceValue = String.format("%s_%s", serialNumber, bluetoothAddress);
        licenceKey = null;
        try
        {
            Signature s = Signature.getInstance(signatureType);
            s.initSign((PrivateKey) mPrivateKey);
            s.update(licenceKeySourceValue.getBytes());

            String b64Value = Base64.encodeToString(s.sign(), Base64.DEFAULT);
            // ASCII parameter cannot contain '\n'
            licenceKey = b64Value.replace("\n", "");
        }
        catch (Exception ignored){}

        return licenceKey;
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
				message = String.format("Reader Licence Key:\n%s\n\n", licenceKeyCommand.getLicenceKey());

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
	// Write the licence key generated for the currently connected reader to the reader
	//
	public void authoriseReader()
	{
		if( mCommander.isConnected())
		{
			// Calculate the licence key based on the reader properties and the current value of the 'secret'
			String licenceKey = createLicenceKey(mVersionCommand.getSerialNumber(), mVersionCommand.getBluetoothAddress());

			// Set the licence key on the reader
			LicenceKeyCommand licenceKeyCommand = LicenceKeyCommand.synchronousCommand();
			licenceKeyCommand.setLicenceKey(licenceKey);
			licenceKeyCommand.setDeleteLicenceKey(DeleteConfirmation.YES);
			mCommander.executeCommand(licenceKeyCommand);
			
			if( licenceKeyCommand.isSuccessful() )
			{
				validateReader();
			}
			else
			{
				sendMessageNotification("Unable to authorise reader!\n");
			}
		}
		else
		{
			 sendMessageNotification("Reader not connected!\n");
		}
	}


	//
	// Remove ANY licence key from the reader
	//
	public void deAuthoriseReader()
	{
		if( mCommander.isConnected())
		{
			// Delete the licence key from the reader
			LicenceKeyCommand licenceKeyCommand = LicenceKeyCommand.synchronousCommand();
			licenceKeyCommand.setDeleteLicenceKey(DeleteConfirmation.YES);
			mCommander.executeCommand(licenceKeyCommand);
			
			if( licenceKeyCommand.isSuccessful() )
			{
				validateReader();
			}
			else
			{
				sendMessageNotification("Unable to de-authorise reader!\n");
			}
		}
		else
		{
			 sendMessageNotification("Reader not connected!\n");
		}
	}


    // Load the public & private keys from the App resources.
    private void loadKeys()
    {
        mPrivateKey = (PrivateKey) loadKey(R.raw.ec_private_key_sample);
        mPublicKey = (PublicKey) loadKey(R.raw.ec_public_key_sample);
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
