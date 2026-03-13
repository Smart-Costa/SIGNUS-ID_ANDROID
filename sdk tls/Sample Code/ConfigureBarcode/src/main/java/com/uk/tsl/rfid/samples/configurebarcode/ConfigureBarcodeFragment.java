package com.uk.tsl.rfid.samples.configurebarcode;

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
import android.widget.ArrayAdapter;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.WeakHandler;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.samples.configurebarcode.databinding.FragmentConfigureBarcodeBinding;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ConfigureBarcodeFragment extends Fragment
{

    private FragmentConfigureBarcodeBinding binding;

    // Debug control
    private static final String TAG = "BarcodeConfFrg";
    private static final boolean D = BuildConfig.DEBUG;

    //
    private ArrayAdapter<String> mBarcodeResultsArrayAdapter;

    // All of the reader tasks are handled by this class
    private ConfigureBarcodeModel mModel;


    /**
     * @return the current AsciiCommander
     */
    protected AsciiCommander getCommander()
    {
        return AsciiCommander.sharedInstance();
    }

    //----------------------------------------------------------------------------------------------
    // OnCreate life cycle
    //----------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {
        binding = FragmentConfigureBarcodeBinding.inflate(inflater, container, false);

        mBarcodeResultsArrayAdapter = new ArrayAdapter<String>(this.getContext(),R.layout.result_item);
        binding.barcodeListView.setAdapter(mBarcodeResultsArrayAdapter);
        binding.barcodeListView.setFastScrollEnabled(true);

        // Hook up the button actions
        binding.persistButton.setOnClickListener(mPersistButtonListener);
        binding.defaultButton.setOnClickListener(mRestoreButtonListener);
        binding.scanButton.setOnClickListener(mScanButtonListener);
        binding.clearButton.setOnClickListener(mClearButtonListener);

        // Set up Symbology check box listeners
        binding.checkBox.setOnClickListener(mCheckBox1Listener);

        //Create a (custom) model and configure its commander and handler
        mModel = new ConfigureBarcodeModel();
        mModel.setCommander(getCommander());
        // The handler for model messages
        GenericHandler mGenericModelHandler = new GenericHandler(this);
        mModel.setHandler(mGenericModelHandler);


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
    public void onResume()
    {
        super.onResume();

        mModel.setEnabled(true);

        // Register to receive notifications from the AsciiCommander
        try
        {
            LocalBroadcastManager.getInstance(this.requireContext()).registerReceiver(mMessageReceiver, new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));
        }
        catch(Exception ignored) {}
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mModel.setEnabled(false);

        // Register to receive notifications from the AsciiCommander
        try
        {
            LocalBroadcastManager.getInstance(this.requireContext()).unregisterReceiver(mMessageReceiver);
        }
        catch(Exception ignored) {}
    }


    //----------------------------------------------------------------------------------------------
    // AsciiCommander message handling
    //----------------------------------------------------------------------------------------------

    //
    // Handle the messages broadcast from the AsciiCommander
    //
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) { Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + getCommander().isConnected()); }

            if( getCommander().isConnected() )
            {
                mModel.resetDevice();

                if( mModel.isImagerSupported())
                {
                    // Enable controls
                    binding.checkBox.setEnabled(true);
                    binding.editTextLengthOne.setEnabled(true);
                    binding.editTextLengthTwo.setEnabled(true);

                }
                else
                {
                    // Disable controls
                    binding.checkBox.setEnabled(false);
                    binding.editTextLengthOne.setEnabled(false);
                    binding.editTextLengthTwo.setEnabled(false);

                    mBarcodeResultsArrayAdapter.add("!!! Imager not supported !!!");
                    scrollBarcodeListViewToBottom();
                }
            }

            UpdateUI();
        }
    };

    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------

    private static class GenericHandler extends WeakHandler<ConfigureBarcodeFragment>
    {
        public GenericHandler(ConfigureBarcodeFragment t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, ConfigureBarcodeFragment t) {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        //TODO: process change in model busy state
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        // Examine the message for prefix
                        String message = (String)msg.obj;
                        if( message.startsWith("ER:")) {
                            t.binding.resultTextView.setText( message.substring(3));
                        }
                        else {
                            t.mBarcodeResultsArrayAdapter.add(message);
                            t.scrollBarcodeListViewToBottom();
                        }
                        t.UpdateUI();
                        break;

                    default:
                        break;
                }
            } catch (Exception ignored) {}

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

        // Set up current Symbology setting state
        binding.checkBox.setChecked(mModel.isCode128Enabled());

        // Remove listeners for lengths change
        binding.editTextLengthOne.removeTextChangedListener(mValue1EditTextChangedListener);
        binding.editTextLengthTwo.removeTextChangedListener(mValue2EditTextChangedListener);

        // Set up lengths
        String lengthOne = String.format(Locale.US, "%d", mModel.getLengthOne());
        binding.editTextLengthOne.setText(lengthOne);
        String lengthTwo = String.format(Locale.US, "%d", mModel.getLengthTwo());
        binding.editTextLengthTwo.setText(lengthTwo);

        // Add listeners for lengths change
        binding.editTextLengthOne.addTextChangedListener(mValue1EditTextChangedListener);
        binding.editTextLengthTwo.addTextChangedListener(mValue2EditTextChangedListener);
    }


    private void scrollBarcodeListViewToBottom() {
        binding.barcodeListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                binding.barcodeListView.setSelection(mBarcodeResultsArrayAdapter.getCount() - 1);
            }
        });
    }


    //----------------------------------------------------------------------------------------------
    // Button event handlers
    //----------------------------------------------------------------------------------------------

    // Scan action
    private View.OnClickListener mPersistButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                mModel.makePersistent();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // Scan action
    private View.OnClickListener mRestoreButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                mModel.restoreDefaults();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // Scan action
    private View.OnClickListener mScanButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                binding.resultTextView.setText("");
                mModel.scan();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // Clear action
    private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                // Clear the list
                mBarcodeResultsArrayAdapter.clear();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in Symbology selections
    //----------------------------------------------------------------------------------------------

    private View.OnClickListener mCheckBox1Listener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                if( getCommander() != null )
                {
                    mModel.toggleCode128(binding.checkBox.isChecked());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in Symbology lengths
    //
    // Note: Could be improved as this makes calls to the reader each time the text changes
    //----------------------------------------------------------------------------------------------

    private TextWatcher mValue1EditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            try {
                value = Integer.parseInt(s.toString());
            } catch (Exception e) {}

            mModel.setLengthOne(value);
        }
    };

    private TextWatcher mValue2EditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            try {
                value = Integer.parseInt(s.toString());
            } catch (Exception e) {}

            mModel.setLengthTwo(value);
        }
    };



}