package com.uk.tsl.rfid.samples.inventory;

import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.WeakHandler;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;
import com.uk.tsl.rfid.asciiprotocol.enumerations.EnumerationBase;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySession;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.devicelist.BuildConfig;
import com.uk.tsl.rfid.samples.inventory.databinding.FragmentInventoryBinding;
import com.uk.tsl.utils.Observable;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class InventoryFragment extends Fragment
{
    // Debugging
    private static final String TAG = "InventoryFragment";
    private static final boolean D = BuildConfig.DEBUG;

    private FragmentInventoryBinding binding;


    // The list of results from actions
    private ArrayAdapter<String> mResultsArrayAdapter;
    private ListView mResultsListView;
    private ArrayAdapter<String> mBarcodeResultsArrayAdapter;
    private ListView mBarcodeResultsListView;

    // The text view to display the RF Output Power used in RFID commands
    private TextView mPowerLevelTextView;
    // The seek bar used to adjust the RF Output Power for RFID commands
    private SeekBar mPowerSeekBar;
    // The current setting of the power level
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    // Error report
    private TextView mResultTextView;

    private TextView mTotalTagsTextView;
    private int mTotalTagsReceived;

    // Custom adapter to display the EnumerationBase.description() rather than the toString() value
    public class DescriptionArrayAdapter extends ArrayAdapter<EnumerationBase> {
        private final EnumerationBase[] mValues;

        public DescriptionArrayAdapter(Context context, int textViewResourceId, EnumerationBase[] objects) {
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

    // The session
    private QuerySession[] mSessions = new QuerySession[] {
            QuerySession.SESSION_0,
            QuerySession.SESSION_1,
            QuerySession.SESSION_2,
            QuerySession.SESSION_3
    };
    // The list of sessions that can be selected
    private DescriptionArrayAdapter mSessionArrayAdapter;


    public class RFModeItem {
        public int getValue() { return mValue;}
        private int mValue;

        public String getDescription() { return mDescription; }
        private String mDescription;

        public RFModeItem(int value, String description ) {
            mValue = value;
            mDescription = description;
        }

        @NonNull
        @Override
        public String toString()
        {
            return mValue + " " + mDescription;
        }
    }

    // The RF Modes
    private RFModeItem[] mRFModes = new RFModeItem[] {
            new RFModeItem(103, "Read Rate"),
            new RFModeItem(302, "Read Rate"),
            new RFModeItem(120, "Read Rate"),
            new RFModeItem(323, "Read Rate"),
            new RFModeItem(344, "Read Rate"),
            new RFModeItem(345, "Read Rate"),
            new RFModeItem(202, "Read Rate"),
            new RFModeItem(222, "ETSI LB"),
            new RFModeItem(223, "ETSI LB"),
            new RFModeItem(241, "ETSI DRM"),
            new RFModeItem(244, "FCC DRM"),
            new RFModeItem(285, "Sensitivity"),
    };
    // The list of RF Modes that can be selected
    private ArrayAdapter<RFModeItem> mRFModesArrayAdapter;

    // All of the reader inventory tasks are handled by this class
    private InventoryModel mModel;

    // Start stop buttons
    Button mStartButton;
    Button mStopButton;



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragmentInventoryBinding.inflate(inflater, container, false);

        mTotalTagsTextView = binding.totalTagsTextView;

        mResultsArrayAdapter = new ArrayAdapter<String>(this.getContext(),R.layout.result_item);
        mBarcodeResultsArrayAdapter = new ArrayAdapter<String>(this.getContext(),R.layout.result_item);

        mResultTextView = binding.resultTextView;


        // Find and set up the results ListView
        mResultsListView =binding.resultListView;
        mResultsListView.setAdapter(mResultsArrayAdapter);
        mResultsListView.setFastScrollEnabled(true);

        mBarcodeResultsListView = binding.barcodeListView;
        mBarcodeResultsListView.setAdapter(mBarcodeResultsArrayAdapter);
        mBarcodeResultsListView.setFastScrollEnabled(true);

        // Hook up the button actions
        mStartButton = binding.scanButton;
        mStartButton.setOnClickListener(mScanButtonListener);

        mStopButton = binding.scanStopButton;
        mStopButton.setOnClickListener(mScanStopButtonListener);
        mStopButton.setEnabled(false);

        Button cButton = binding.clearButton;
        cButton.setOnClickListener(mClearButtonListener);

        // The SeekBar provides an integer value for the antenna power
        mPowerLevelTextView = binding.powerTextView;
        mPowerSeekBar = binding.powerSeekBar;
        mPowerSeekBar.setOnSeekBarChangeListener(mPowerSeekBarListener);

        mSessionArrayAdapter = new DescriptionArrayAdapter(this.getContext(), android.R.layout.simple_spinner_item, mSessions);
        // Find and set up the sessions spinner
        Spinner spinner = binding.sessionSpinner;
        mSessionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mSessionArrayAdapter);
        spinner.setOnItemSelectedListener(mActionSelectedListener);
        spinner.setSelection(0);

        mRFModesArrayAdapter = new ArrayAdapter<RFModeItem>(this.getContext(), android.R.layout.simple_spinner_item, mRFModes);
        // Find and set up the sessions spinner
        Spinner spinnerRF = binding.rfModeSpinner;
        mRFModesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRF.setAdapter(mRFModesArrayAdapter);
        spinnerRF.setOnItemSelectedListener(mRFModeSelectedListener);
        spinnerRF.setSelection(0);

        // Set up the Tag info check box listener
        CheckBox ticb = binding.tagInfoCheckBox;
        ticb.setOnClickListener(mInfoCheckBoxListener);

        // Set up the "Uniques Only" Id check box listener
        CheckBox ucb = binding.uniquesCheckBox;
        ucb.setOnClickListener(mUniquesCheckBoxListener);

        // Set up Fast Id check box listener
        CheckBox cb =binding.fastIdCheckBox;
        cb.setOnClickListener(mFastIdCheckBoxListener);

        // Set up the Max Tags per inventory
        binding.maxTagsValueEditText.addTextChangedListener( mMaxTagsEditTextChangedListener );


        //Create a (custom) model and configure its commander and handler
        mModel = new InventoryModel();
        mModel.setCommander(getCommander());
        // The handler for model messages
        GenericHandler mGenericModelHandler = new GenericHandler(this);
        mModel.setHandler(mGenericModelHandler);

        binding.maxTagsValueEditText.setText(String.format("%d", mModel.getMaximumTagsPerInventory()));

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

        mModel.setEnabled(false);

        // Stop observing events from the AsciiCommander
        getCommander().stateChangedEvent().removeObserver(mConnectionStateObserver);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        mModel.setEnabled(true);

        // Observe events from the AsciiCommander
        getCommander().stateChangedEvent().addObserver(mConnectionStateObserver);

        UpdateUI();
    }


    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------

    private static class GenericHandler extends WeakHandler<InventoryFragment>
    {
        public GenericHandler(InventoryFragment t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, InventoryFragment t)
        {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        //TODO: process change in model busy state
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        // Examine the message for prefix
                        String message = (String)msg.obj;
                        if( message.startsWith("ER:")) {
                            t.mResultTextView.setText( message.substring(3));
                            t.mResultTextView.setBackgroundColor(0xD0FFFFFF);
                        }
                        else if( message.startsWith("BC:")) {
                            t.mBarcodeResultsListView.setVisibility(View.VISIBLE);
                            t.mBarcodeResultsArrayAdapter.add(message);
                            t.scrollBarcodeListViewToBottom();
                        }
                        else if( message.startsWith("RR:")) {
                            t.mResultTextView.setText(message.substring(3));
                        }
                        else {
                            t.mResultsArrayAdapter.add(message);
                            if( message.startsWith("EPC:")) t.mTotalTagsReceived += 1;
                            t.scrollResultsListViewToBottom();
                            t.mTotalTagsTextView.setText(String.format(Locale.US, "%d", t.mTotalTagsReceived));
                        }
                        t.UpdateUI();
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };


    //
    // Set the state for the UI controls
    //
    private void UpdateUI() {
        //boolean isConnected = getCommander().isConnected();
        //TODO: configure UI control state
    }


    private void scrollResultsListViewToBottom() {
        mResultsListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mResultsListView.setSelection(mResultsArrayAdapter.getCount() - 1);
            }
        });
    }

    private void scrollBarcodeListViewToBottom() {
        mBarcodeResultsListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mBarcodeResultsListView.setSelection(mBarcodeResultsArrayAdapter.getCount() - 1);
            }
        });
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
    // Handle the connection state change events from the AsciiCommander
    //
    private final Observable.Observer<String> mConnectionStateObserver = (observable, reason) ->
    {
        if (D) { Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + getCommander().isConnected()); }
        if (D) { Log.d(getClass().getName(), "...Thread: " + Thread.currentThread().getName()); }

        if( getCommander().isConnected() )
        {
            // Update the link profile
            int profile = getCommander().getDeviceProperties().getLinkProfile();
            mModel.setLinkProfile(profile);
            if( getCommander().getDeviceProperties().getInformationCommand().getAsciiProtocol().startsWith(("3")))
            {
                // Only Series 3 Readers support these options
                RFModeItem item = (RFModeItem) findIn(mRFModes, profile);
                int index = mRFModesArrayAdapter.getPosition(item);
                binding.rfModeSpinner.setSelection(index);
                binding.rfModeSpinner.setEnabled(true);

                binding.maxTagsValueEditText.setEnabled(true);
            }
            else
            {
                binding.rfModeSpinner.setEnabled(false);
                binding.maxTagsValueEditText.setEnabled(false);
            }

            // Update for any change in power limits
            setPowerBarLimits();

            // This may have changed the current power level setting if the new range is smaller than the old range
            // so update the model's inventory command for the new power value
            mModel.getCommand().setOutputPower(mPowerLevel);

//            mModel.resetDevice();
//            mModel.updateConfiguration();
            mModel.resetAndUpdateConfig();
        }

        UpdateUI();
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
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Nothing to do here
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            // Update the reader's setting only after the user has finished changing the value
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + seekBar.getProgress());
            mModel.getCommand().setOutputPower(mPowerLevel);
            mModel.updateConfiguration();
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
    // Button event handlers
    //----------------------------------------------------------------------------------------------

    // Scan (Start) action
    private View.OnClickListener mScanButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                mResultTextView.setText("");
                // Start the continuous inventory
                mTotalTagsReceived = 0;
                mModel.scanStart();

                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);

                mBarcodeResultsListView.setVisibility(View.GONE);
                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // Scan Stopaction
    private View.OnClickListener mScanStopButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                mResultTextView.setText("");
                // Stop the continuous inventory
                mModel.scanStop();

                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);

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
                mResultsArrayAdapter.clear();
                mResultTextView.setText("");
                mResultTextView.setBackgroundColor(0x00FFFFFF);
                mBarcodeResultsArrayAdapter.clear();
                mModel.clearUniques();
                mTotalTagsTextView.setText("");

                mBarcodeResultsListView.setVisibility(View.VISIBLE);
                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //----------------------------------------------------------------------------------------------
    // Handler for changes in RF Mode
    //----------------------------------------------------------------------------------------------

    private AdapterView.OnItemSelectedListener mRFModeSelectedListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            int profile = ((RFModeItem)parent.getItemAtPosition(pos)).getValue();
            mModel.setLinkProfile(profile);
            mModel.updateConfiguration();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in session
    //----------------------------------------------------------------------------------------------

    private AdapterView.OnItemSelectedListener mActionSelectedListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if( mModel.getCommand() != null ) {
                QuerySession targetSession = (QuerySession)parent.getItemAtPosition(pos);
                mModel.getCommand().setQuerySession(targetSession);
                mModel.updateConfiguration();
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in Tag Info requested
    //----------------------------------------------------------------------------------------------

    private final View.OnClickListener mInfoCheckBoxListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                CheckBox infoCheckBox = (CheckBox)v;

                mModel.setInfoRequested(infoCheckBox.isChecked());
                mModel.updateConfiguration();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in Uniques Only
    //----------------------------------------------------------------------------------------------

    private final View.OnClickListener mUniquesCheckBoxListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                CheckBox uniquesCheckBox = (CheckBox)v;

                mModel.setUniquesOnly(uniquesCheckBox.isChecked());

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in FastId
    //----------------------------------------------------------------------------------------------

    private final View.OnClickListener mFastIdCheckBoxListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                CheckBox fastIdCheckBox = (CheckBox)v;
                mModel.getCommand().setUsefastId(TriState.from(fastIdCheckBox.isChecked()));
                mModel.updateConfiguration();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private TextWatcher mMaxTagsEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            String vString = s.toString();
            try {
                value = Integer.parseInt(vString);


            } catch (Exception e) {
                e.printStackTrace();
            }

            mModel.setMaximumTagsPerInventory(value);
            mModel.updateConfiguration();

            UpdateUI();

        }
    };



    //----------------------------------------------------------------------------------------------
    // Helper for setting the RFMode spinner
    //----------------------------------------------------------------------------------------------

    /**
     * Find the given value in the set of items
     * @param items the items to search
     * @param value the value to find
     * @return the item found or null if none
     */
    public RFModeItem findIn(RFModeItem[] items, int value) {
        for (RFModeItem item : items) {
            if( item.getValue() == value ) return item;
        }
        return null;
    }

}