package com.uk.tsl.rfid.samples.blockpermalock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.WeakHandler;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;
import com.uk.tsl.rfid.asciiprotocol.device.ConnectionState;
import com.uk.tsl.rfid.asciiprotocol.device.IAsciiTransport;
import com.uk.tsl.rfid.asciiprotocol.device.ObservableReaderList;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;
import com.uk.tsl.rfid.asciiprotocol.device.ReaderManager;
import com.uk.tsl.rfid.asciiprotocol.device.TransportType;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.utils.HexEncoding;
import com.uk.tsl.utils.Observable;

import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_ACTION;
import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_INDEX;

public class BlockPermalockActivity extends AppCompatActivity
{
    // The Reader currently in use
    private Reader mReader = null;

    private boolean mIsSelectingReader = false;

    private MenuItem mConnectMenuItem;
    private MenuItem mDisconnectMenuItem;

    // Debugging
    private static final String TAG = "BlockPermalockActivity";
    private static final boolean D = BuildConfig.DEBUG;

    // The text view to display the RF Output Power used in RFID commands
    private TextView mPowerLevelTextView;
    // The seek bar used to adjust the RF Output Power for RFID commands
    private SeekBar mPowerSeekBar;
    // The current setting of the power level
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    // The buttons that invoke actions
    private Button mReadActionButton;
    private Button mWriteActionButton;

    // The text-based parameters
    private EditText mTargetTagEditText;
    private EditText mWordAddressEditText;
    private EditText mWordCountEditText;
    private EditText mDataEditText;

    private TextView mResultTextView;
    private ScrollView mResultScrollView;

    //Create model class derived from ModelBase
    private BlockPermalockModel mModel;


    /**
     * @return the current AsciiCommander
     */
    protected AsciiCommander getCommander() { return AsciiCommander.sharedInstance(); }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_permalock);

        mGenericModelHandler = new GenericHandler(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // The SeekBar provides an integer value for the antenna power
        mPowerLevelTextView = (TextView)findViewById(R.id.powerTextView);
        mPowerSeekBar = (SeekBar)findViewById(R.id.powerSeekBar);
        mPowerSeekBar.setOnSeekBarChangeListener(mPowerSeekBarListener);

        // Set up the action buttons
        Button clearActionButton;

        mReadActionButton = (Button)findViewById(R.id.readButton);
        mReadActionButton.setOnClickListener(mAction1ButtonListener);

        mWriteActionButton = (Button)findViewById(R.id.writeButton);
        mWriteActionButton.setOnClickListener(mAction2ButtonListener);

        clearActionButton = (Button)findViewById(R.id.clearButton);
        clearActionButton.setOnClickListener(mAction3ButtonListener);

        // Set up the target EPC EditText
        mTargetTagEditText = (EditText)findViewById(R.id.targetTagEditText);
        mTargetTagEditText.addTextChangedListener(mTargetTagEditTextChangedListener);

        mWordAddressEditText = (EditText)findViewById(R.id.wordAddressEditText);
        mWordAddressEditText.addTextChangedListener(mWordAddressEditTextChangedListener);

        mWordCountEditText = (EditText)findViewById(R.id.wordCountEditText);
        mWordCountEditText.addTextChangedListener(mWordCountEditTextChangedListener);

        mDataEditText = (EditText)findViewById(R.id.dataEditText);
        mDataEditText.addTextChangedListener(mDataEditTextChangedListener);

        // Set up the results area
        mResultTextView = (TextView)findViewById(R.id.resultTextView);
        mResultScrollView = (ScrollView)findViewById(R.id.resultScrollView);



        // Ensure the shared instance of AsciiCommander exists
        AsciiCommander.createSharedInstance(getApplicationContext());

        final AsciiCommander commander = getCommander();

        // Ensure that all existing responders are removed
        commander.clearResponders();

        // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
        // and passes the line onto the next responder
        // This is ADDED FIRST so that no other responder can consume received lines before they are logged.
        commander.addResponder(new LoggerResponder());

        // Add responder to enable the synchronous commands
        commander.addSynchronousResponder();

        // Configure the ReaderManager when necessary
        ReaderManager.create(getApplicationContext());

        // Add observers for changes
        ReaderManager.sharedInstance().getReaderList().readerAddedEvent().addObserver(mAddedObserver);
        ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().addObserver(mUpdatedObserver);
        ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().addObserver(mRemovedObserver);


        // Set the seek bar current value to maximum and to cover the range of the power settings
        setPowerBarLimits();

        //TODO: Create a (custom) model and configure its commander and handler
        mModel = new BlockPermalockModel();
        mModel.setCommander(getCommander());
        mModel.setHandler(mGenericModelHandler);

        // Use the model's values for the offset and length
        // Display the initial values
        int offset = mModel.getReadCommand().getOffset();
        int length = mModel.getReadCommand().getLength();
        mWordAddressEditText.setText(String.format("%d", offset));
        mWordCountEditText.setText(String.format("%d", length));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Remove observers for changes
        ReaderManager.sharedInstance().getReaderList().readerAddedEvent().removeObserver(mAddedObserver);
        ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().removeObserver(mUpdatedObserver);
        ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().removeObserver(mRemovedObserver);
    }

    //----------------------------------------------------------------------------------------------
    // Menu
    //----------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_block_permalock, menu);

        mConnectMenuItem = menu.findItem(R.id.connect_reader_menu_item);
        mDisconnectMenuItem= menu.findItem(R.id.disconnect_reader_menu_item);

        return true;
    }

    /**
     * Prepare the menu options
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        boolean isConnected = getCommander().isConnected();
        mDisconnectMenuItem.setEnabled(isConnected);

        mConnectMenuItem.setEnabled(true);
        mConnectMenuItem.setTitle( (mReader != null && mReader.isConnected() ? R.string.change_reader_menu_item_text : R.string.connect_reader_menu_item_text));

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.connect_reader_menu_item:
                // Launch the DeviceListActivity to see available Readers
                mIsSelectingReader = true;
                int index = -1;
                if( mReader != null )
                {
                    index = ReaderManager.sharedInstance().getReaderList().list().indexOf(mReader);
                }
                Intent selectIntent = new Intent(this, DeviceListActivity.class);
                if( index >= 0 )
                {
                    selectIntent.putExtra(EXTRA_DEVICE_INDEX, index);
                }
                startActivityForResult(selectIntent, DeviceListActivity.SELECT_DEVICE_REQUEST);
                return true;

            case R.id.disconnect_reader_menu_item:
                if( mReader != null )
                {
                    mReader.disconnect();
                    mReader = null;
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //----------------------------------------------------------------------------------------------
    // Start life cycle
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onStart()
    {
        super.onStart();

        checkForBluetoothPermission();
    }

    //----------------------------------------------------------------------------------------------
    // Pause & Resume life cycle
    //----------------------------------------------------------------------------------------------

    @Override
    public synchronized void onPause() {
        super.onPause();

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommanderMessageReceiver);

        // Disconnect from the reader to allow other Apps to use it
        // unless pausing when USB device attached or using the DeviceListActivity to select a Reader
        if( !mIsSelectingReader && !ReaderManager.sharedInstance().didCauseOnPause() && mReader != null )
        {
            mReader.disconnect();
        }

        ReaderManager.sharedInstance().onPause();
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommanderMessageReceiver, new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));

        // Remember if the pause/resume was caused by ReaderManager - this will be cleared when ReaderManager.onResume() is called
        boolean readerManagerDidCauseOnPause = ReaderManager.sharedInstance().didCauseOnPause();

        // The ReaderManager needs to know about Activity lifecycle changes
        ReaderManager.sharedInstance().onResume();

        // The Activity may start with a reader already connected (perhaps by another App)
        // Update the ReaderList which will add any unknown reader, firing events appropriately
        ReaderManager.sharedInstance().updateList();

        // Locate a Reader to use when necessary
        AutoSelectReader(!readerManagerDidCauseOnPause);

        mIsSelectingReader = false;

        displayReaderState();
        UpdateUI();
    }

    //
    // Automatically select the Reader to use
    //
    private void AutoSelectReader(boolean attemptReconnect)
    {
        ObservableReaderList readerList = ReaderManager.sharedInstance().getReaderList();
        Reader usbReader = null;
        if( readerList.list().size() >= 1)
        {
            // Currently only support a single USB connected device so we can safely take the
            // first CONNECTED reader if there is one
            for (Reader reader : readerList.list())
            {
                if (reader.hasTransportOfType(TransportType.USB))
                {
                    usbReader = reader;
                    break;
                }
            }
        }

        if( mReader == null )
        {
            if( usbReader != null )
            {
                // Use the Reader found, if any
                mReader = usbReader;
                getCommander().setReader(mReader);
            }
        }
        else
        {
            // If already connected to a Reader by anything other than USB then
            // switch to the USB Reader
            IAsciiTransport activeTransport = mReader.getActiveTransport();
            if ( activeTransport != null && activeTransport.type() != TransportType.USB && usbReader != null)
            {
                appendMessage("Disconnecting from: " + mReader.getDisplayName() + "\n");
                mReader.disconnect();

                mReader = usbReader;

                // Use the Reader found, if any
                getCommander().setReader(mReader);
            }
        }

        // Reconnect to the chosen Reader
        if( mReader != null
                && !mReader.isConnecting()
                && (mReader.getActiveTransport()== null || mReader.getActiveTransport().connectionStatus().value() == ConnectionState.DISCONNECTED))
        {
            // Attempt to reconnect on the last used transport unless the ReaderManager is cause of OnPause (USB device connecting)
            if( attemptReconnect )
            {
                if( mReader.allowMultipleTransports() || mReader.getLastTransportType() == null )
                {
                    // Reader allows multiple transports or has not yet been connected so connect to it over any available transport
                    if( mReader.connect() )
                    {
                        appendMessage("Connecting to: " + mReader.getDisplayName() +"\n");
                    }
                }
                else
                {
                    // Reader supports only a single active transport so connect to it over the transport that was last in use
                    if( mReader.connect(mReader.getLastTransportType()) )
                    {
                        appendMessage("Connecting (over last transport) to: " + mReader.getDisplayName() +"\n");
                    }
                }
            }
        }
    }


    // ReaderList Observers
    Observable.Observer<Reader> mAddedObserver = new Observable.Observer<Reader>()
    {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader)
        {
            // See if this newly added Reader should be used
            AutoSelectReader(true);
        }
    };

    Observable.Observer<Reader> mUpdatedObserver = new Observable.Observer<Reader>()
    {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader)
        {
        }
    };

    Observable.Observer<Reader> mRemovedObserver = new Observable.Observer<Reader>()
    {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader)
        {
            // Was the current Reader removed
            if( reader == mReader)
            {
                mReader = null;

                // Stop using the old Reader
                getCommander().setReader(mReader);
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------

    private static class GenericHandler extends WeakHandler<BlockPermalockActivity>
    {
        public GenericHandler(BlockPermalockActivity t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, BlockPermalockActivity t)
        {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        if( t.mModel.error() != null ) {
                            t.appendMessage("\n Task failed:\n" + t.mModel.error().getMessage() + "\n\n");
                        }
                        t.UpdateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String)msg.obj;
                        t.appendMessage(message);
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };

    // The handler for model messages
    private static GenericHandler mGenericModelHandler;

    //----------------------------------------------------------------------------------------------
    // UI state and display update
    //----------------------------------------------------------------------------------------------

    // Append the given message to the bottom of the results area
    private void appendMessage(String message)
    {
        final String msg = message;
        mResultScrollView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mResultTextView.append(msg);
                mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });
            }
        });
    }

    private void displayReaderState() {

        String connectionMsg = "Reader: ";
        switch( getCommander().getConnectionState())
        {
            case CONNECTED:
                connectionMsg += getCommander().getConnectedDeviceName();
                break;
            case CONNECTING:
                connectionMsg += "Connecting...";
                break;
            default:
                connectionMsg += "Disconnected";
        }
        setTitle(connectionMsg);
    }


    //
    // Set the state for the UI controls
    //
    private void UpdateUI()
    {
        boolean isConnected = getCommander().isConnected();
        boolean canIssueCommand = isConnected & !mModel.isBusy();
        mReadActionButton.setEnabled(canIssueCommand);
        // Only enable the write button when there is at least a partial EPC
        mWriteActionButton.setEnabled(canIssueCommand && mTargetTagEditText.getText().length() != 0);
    }


    //----------------------------------------------------------------------------------------------
    // AsciiCommander message handling
    //----------------------------------------------------------------------------------------------

    //
    // Handle the messages broadcast from the AsciiCommander
    //
    private BroadcastReceiver mCommanderMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String connectionStateMsg = getCommander().getConnectionState().toString();
            Log.d("", "AsciiCommander state changed - isConnected: " + getCommander().isConnected() + " (" + connectionStateMsg + ")");

            if(getCommander()!= null)
            {
                displayReaderState();

                if( getCommander().isConnected() )
                {
                    // Update for any change in power limits
                    setPowerBarLimits();
                    // This may have changed the current power level setting if the new range is smaller than the old range
                    // so update the model's inventory command for the new power value
                    mModel.getReadCommand().setOutputPower(mPowerLevel);
                    mModel.getWriteCommand().setOutputPower(mPowerLevel);

                    mModel.resetDevice();
                }
                else if(getCommander().getConnectionState() == ConnectionState.DISCONNECTED)
                {
                    // A manual disconnect will have cleared mReader
                    if( mReader != null )
                    {
                        // See if this is from a failed connection attempt
                        if (!mReader.wasLastConnectSuccessful())
                        {
                            // Unable to connect so have to choose reader again
                            mReader = null;
                        }
                    }
                }

                UpdateUI();
            }
        }
    };

    //----------------------------------------------------------------------------------------------
    // Handlers for the action buttons - invoke the currently selected actions
    //----------------------------------------------------------------------------------------------

    private View.OnClickListener mAction1ButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            mModel.read();
        }
    };

    private View.OnClickListener mAction2ButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BlockPermalockActivity.this);
            builder.setMessage("This action is PERMANENT" )
                   .setTitle("!! WARNING !!");

            builder.setPositiveButton("Apply Permalock", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mModel.write();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    private View.OnClickListener mAction3ButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            mResultTextView.setText("");
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for
    //----------------------------------------------------------------------------------------------

    private TextWatcher mTargetTagEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            String value = s.toString();
            if( mModel.getReadCommand() != null ) {
                mModel.getReadCommand().setSelectData(value);
            }
            if( mModel.getWriteCommand() != null ) {
                mModel.getWriteCommand().setSelectData(value);
            }
            UpdateUI();
        }
    };


    private TextWatcher mWordAddressEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            try {
                value = Integer.parseInt(s.toString());

                if( mModel.getReadCommand() != null ) {
                    mModel.getReadCommand().setOffset(value);
                }
                if( mModel.getWriteCommand() != null ) {
                    mModel.getWriteCommand().setOffset(value);
                }
            } catch (Exception e) {
            }
            UpdateUI();
        }
    };


    private TextWatcher mWordCountEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            try {
                value = Integer.parseInt(s.toString());

                if( mModel.getReadCommand() != null ) {
                    mModel.getReadCommand().setLength(value);
                }
                if( mModel.getWriteCommand() != null ) {
                    mModel.getWriteCommand().setLength(value);
                }
            } catch (Exception e) {
            }
            UpdateUI();
        }
    };


    private TextWatcher mDataEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            String value = s.toString();
            if( mModel.getWriteCommand() != null ) {
                byte[] data = null;
                try {
                    data = HexEncoding.stringToBytes(value);
                    mModel.getWriteCommand().setMask(data);
                } catch (Exception e) {
                    // Ignore if invalid
                }
            }
            UpdateUI();
        }
    };


    //
    // Handle Intent results
    //
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case DeviceListActivity.SELECT_DEVICE_REQUEST:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
                    int readerIndex = data.getExtras().getInt(EXTRA_DEVICE_INDEX);
                    Reader chosenReader = ReaderManager.sharedInstance().getReaderList().list().get(readerIndex);

                    int action = data.getExtras().getInt(EXTRA_DEVICE_ACTION);

                    // If already connected to a different reader then disconnect it
                    if( mReader != null )
                    {
                        if( action == DeviceListActivity.DEVICE_CHANGE || action == DeviceListActivity.DEVICE_DISCONNECT)
                        {
                            mReader.disconnect();
                            if(action == DeviceListActivity.DEVICE_DISCONNECT)
                            {
                                mReader = null;
                            }
                        }
                    }

                    // Use the Reader found
                    if( action == DeviceListActivity.DEVICE_CHANGE || action == DeviceListActivity.DEVICE_CONNECT)
                    {
                        mReader = chosenReader;
                        getCommander().setReader(mReader);
                    }
                }
                break;
        }
    }


    //----------------------------------------------------------------------------------------------
    // Power seek bar
    //----------------------------------------------------------------------------------------------

    //
    // Set the seek bar to cover the range of the currently connected device
    // The power level is set to the new maximum power
    //
    private void setPowerBarLimits()
    {
        DeviceProperties deviceProperties = getCommander().getDeviceProperties();

        mPowerSeekBar.setMax(deviceProperties.getMaximumCarrierPower() - deviceProperties.getMinimumCarrierPower());
        mPowerLevel = deviceProperties.getMaximumCarrierPower();
        mPowerSeekBar.setProgress(mPowerLevel - deviceProperties.getMinimumCarrierPower());
    }


    //
    // Handle events from the power level seek bar. Update the mPowerLevel member variable for use in other actions
    //
    private SeekBar.OnSeekBarChangeListener mPowerSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Nothing to do here
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            // Update the reader's setting only after the user has finished changing the value
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + seekBar.getProgress());
            mModel.getReadCommand().setOutputPower(mPowerLevel);
            mModel.getWriteCommand().setOutputPower(mPowerLevel);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + progress);
        }
    };

    private void updatePowerSetting(int level)	{
        mPowerLevel = level;
        mPowerLevelTextView.setText( mPowerLevel + " dBm");
    }


    //----------------------------------------------------------------------------------------------
    // Bluetooth permissions checking
    //----------------------------------------------------------------------------------------------
    //
    private TextView mBluetoothPermissionsTextView;
    private String mPermission1;
    private String mPermission2;
    //private String mPermission3;

    private void determineRequiredBluetoothPermissions()
    {
        //mPermission3 = null;
        if (Build.VERSION.SDK_INT < 29)
        {
            mPermission1 = Manifest.permission.BLUETOOTH;
            mPermission2 = Manifest.permission.ACCESS_COARSE_LOCATION;

            bluetoothPermissions = new String[]{mPermission1, mPermission2};
        }
        else if (Build.VERSION.SDK_INT < 31)
        {
            mPermission1 = Manifest.permission.BLUETOOTH;
            mPermission2 = Manifest.permission.ACCESS_FINE_LOCATION;

            bluetoothPermissions = new String[]{mPermission1, mPermission2};
        }
        else
        {
            mPermission1 = Manifest.permission.BLUETOOTH_CONNECT;
            mPermission2 = Manifest.permission.BLUETOOTH_SCAN;

            bluetoothPermissions = new String[]{mPermission1, mPermission2};
        }

    }

    private void checkForBluetoothPermission()
    {
        determineRequiredBluetoothPermissions();

        if (ContextCompat.checkSelfPermission(this, mPermission1) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, mPermission2) != PackageManager.PERMISSION_GRANTED
            //|| mPermission3 != null && ContextCompat.checkSelfPermission(this, mPermission3) != PackageManager.PERMISSION_GRANTED
        )
        {
            //binding.bluetoothPermissionsPrompt.setVisibility(View.VISIBLE);
            if (shouldShowRequestPermissionRationale(mPermission1))
            {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                offerBluetoothPermissionRationale();
            }
            else
            {
                requestPermissionLauncher.launch(bluetoothPermissions);
            }
        }
        else
        {
            //binding.bluetoothPermissionsPrompt.setVisibility(View.GONE);
        }
    }

    private String[] bluetoothPermissions = new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};

    void offerBluetoothPermissionRationale()
    {
        // Older permissions are granted at install time
        if (Build.VERSION.SDK_INT < 31) return;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("Permission is required to connect to TSL Readers over Bluetooth")
               .setTitle("Allow Bluetooth?");

        builder.setPositiveButton("Show Permission Dialog", new DialogInterface.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.S)
            public void onClick(DialogInterface dialog, int id)
            {
                requestPermissionLauncher.launch(bluetoothPermissions);
            }
        });


        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


    void showBluetoothPermissionDeniedConsequences()
    {
        // Note: When permissions have been denied, this will be invoked everytime checkForBluetoothPermission() is called
        // In your app, we suggest you limit the number of times the User is notified.
        Toast.makeText(this, "This app will not be able to connect to TSL Readers via Bluetooth.", Toast.LENGTH_LONG).show();
    }


    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionsGranted ->
            {
                //boolean allGranted = permissionsGranted.values().stream().reduce(true, Boolean::logicalAnd);
                boolean allGranted = true;
                for (boolean isGranted : permissionsGranted.values())
                {
                    allGranted = allGranted && isGranted;
                }

                if (allGranted)
                {
                    // Permission is granted. Continue the action or workflow in your
                    // app.

                    // Update the ReaderList which will add any unknown reader, firing events appropriately
                    ReaderManager.sharedInstance().updateList();
                    //binding.bluetoothPermissionsPrompt.setVisibility(View.GONE);
                }
                else
                {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    showBluetoothPermissionDeniedConsequences();
                }
            });

}
