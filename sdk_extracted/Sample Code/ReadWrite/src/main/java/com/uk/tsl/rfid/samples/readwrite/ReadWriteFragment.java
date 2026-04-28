package com.uk.tsl.rfid.samples.readwrite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.WeakHandler;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;
import com.uk.tsl.rfid.asciiprotocol.device.ConnectionState;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.EnumerationBase;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.samples.readwrite.databinding.FragmentReadWriteBinding;
import com.uk.tsl.utils.HexEncoding;

public class ReadWriteFragment extends Fragment
{
    // Debug control
    private static final boolean D = BuildConfig.DEBUG;

    private FragmentReadWriteBinding binding;

    // The text view to display the RF Output Power used in RFID commands
    private TextView mPowerLevelTextView;
    // The seek bar used to adjust the RF Output Power for RFID commands
    private SeekBar mPowerSeekBar;
    // The current setting of the power level
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    // Custom adapter for the Ascii command enumerated parameter values to display the description rather than the toString() value
    public class ParameterEnumerationArrayAdapter<T extends EnumerationBase> extends ArrayAdapter<T>
    {
        private final T[] mValues;

        public ParameterEnumerationArrayAdapter(Context context, int textViewResourceId, T[] objects) {
            super(context, textViewResourceId, objects);
            mValues = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView)super.getView(position, convertView, parent);
            view.setText(mValues[position].getDescription());
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView)super.getDropDownView(position, convertView, parent);
            view.setText(mValues[position].getDescription());
            return view;
        }
    }

    private Databank[] mDatabanks = new Databank[] {
            Databank.ELECTRONIC_PRODUCT_CODE,
            Databank.TRANSPONDER_IDENTIFIER,
            Databank.RESERVED,
            Databank.USER
    };
    private ParameterEnumerationArrayAdapter<Databank> mDatabankArrayAdapter;

    // The buttons that invoke actions
    private Button mReadActionButton;
    private Button mWriteActionButton;
    private Button mClearActionButton;

    //Create model class derived from ModelBase
    private ReadWriteModel mModel;

    // The text-based parameters
    private EditText mTargetTagEditText;
    private EditText mWordAddressEditText;
    private EditText mWordCountEditText;
    private EditText mDataEditText;

    private TextView mResultTextView;
    private ScrollView mResultScrollView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragmentReadWriteBinding.inflate(inflater, container, false);

        // The SeekBar provides an integer value for the antenna power
        mPowerLevelTextView = binding.powerTextView;
        mPowerSeekBar = binding.powerSeekBar;
        mPowerSeekBar.setOnSeekBarChangeListener(mPowerSeekBarListener);

        // Set up the spinner with the memory bank selections
        mDatabankArrayAdapter = new ParameterEnumerationArrayAdapter<Databank>(this.getContext(), android.R.layout.simple_spinner_item, mDatabanks);
        // Find and set up the sessions spinner
        Spinner spinner = binding.bankSpinner;
        mDatabankArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mDatabankArrayAdapter);
        spinner.setOnItemSelectedListener(mBankSelectedListener);
        spinner.setSelection(mDatabanks.length - 1);


        // Set up the action buttons
        mReadActionButton = binding.readButton;
        mReadActionButton.setOnClickListener(mAction1ButtonListener);

        mWriteActionButton = binding.writeButton;
        mWriteActionButton.setOnClickListener(mAction2ButtonListener);

        mClearActionButton = binding.clearButton;
        mClearActionButton.setOnClickListener(mAction3ButtonListener);

        // Set up the target EPC EditText
        mTargetTagEditText = binding.targetTagEditText;
        mTargetTagEditText.addTextChangedListener(mTargetTagEditTextChangedListener);

        mWordAddressEditText = binding.wordAddressEditText;
        mWordAddressEditText.addTextChangedListener(mWordAddressEditTextChangedListener);

        mWordCountEditText = binding.wordCountEditText;
        mWordCountEditText.addTextChangedListener(mWordCountEditTextChangedListener);

        mDataEditText = binding.dataEditText;
        mDataEditText.addTextChangedListener(mDataEditTextChangedListener);

        // Set up the results area
        mResultTextView = binding.resultTextView;
        mResultScrollView = binding.resultScrollView;



        //Create a (custom) model and configure its commander and handler
        mModel = new ReadWriteModel();
        mModel.setCommander(getCommander());
        // The handler for model messages
        GenericHandler mGenericModelHandler = new GenericHandler(this);
        mModel.setHandler(mGenericModelHandler);

        // Use the model's values for the offset and length
        // Display the initial values
        int offset = mModel.getReadCommand().getOffset();
        int length = mModel.getReadCommand().getLength();
        mWordAddressEditText.setText(String.format("%d", offset));
        mWordCountEditText.setText(String.format("%d", length));

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    //----------------------------------------------------------------------------------------------
    // Pause & Resume life cycle
    //----------------------------------------------------------------------------------------------

    @Override
    public synchronized void onPause() {
        super.onPause();

        // Unregister to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(mCommanderMessageReceiver);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(mCommanderMessageReceiver, new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));

        UpdateUI();
    }


    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------

    private static class GenericHandler extends WeakHandler<ReadWriteFragment>
    {
        public GenericHandler(ReadWriteFragment t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, ReadWriteFragment t)
        {
            try {
                switch (msg.what)
                {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        if (t.mModel.error() != null)
                        {
                            t.mResultTextView.append("\n Task failed:\n" + t.mModel.error().getMessage() + "\n\n");
                            t.mResultScrollView.post(new Runnable()
                            {
                                public void run() {t.mResultScrollView.fullScroll(View.FOCUS_DOWN);}
                            });

                        }
                        t.UpdateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String) msg.obj;
                        t.mResultTextView.append(message);
                        t.mResultScrollView.post(new Runnable()
                        {
                            public void run() {t.mResultScrollView.fullScroll(View.FOCUS_DOWN);}
                        });
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };


    //----------------------------------------------------------------------------------------------
    // UI state and display update
    //----------------------------------------------------------------------------------------------

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

    /**
     * @return the current AsciiCommander
     */
    protected AsciiCommander getCommander()
    {
        return AsciiCommander.sharedInstance();
    }

    //
    // Handle the messages broadcast from the AsciiCommander
    //
    private BroadcastReceiver mCommanderMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) { Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + getCommander().isConnected()); }

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
            }

            UpdateUI();
        }
    };

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
        public void onStartTrackingTouch(SeekBar seekBar)
        {
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
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + progress);
        }
    };

    private void updatePowerSetting(int level)
    {
        mPowerLevel = level;
        mPowerLevelTextView.setText( mPowerLevel + " dBm");
    }

    //----------------------------------------------------------------------------------------------
    // Handler for changes in databank
    //----------------------------------------------------------------------------------------------

    private AdapterView.OnItemSelectedListener mBankSelectedListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Databank targetBank = (Databank)parent.getItemAtPosition(pos);
            if( mModel.getReadCommand() != null ) {
                mModel.getReadCommand().setBank(targetBank);
            }
            if( mModel.getWriteCommand() != null ) {
                mModel.getWriteCommand().setBank(targetBank);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
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
            mModel.write();
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
                    mModel.getWriteCommand().setData(data);
                } catch (Exception e) {
                    // Ignore if invalid
                }
            }
            UpdateUI();
        }
    };

}