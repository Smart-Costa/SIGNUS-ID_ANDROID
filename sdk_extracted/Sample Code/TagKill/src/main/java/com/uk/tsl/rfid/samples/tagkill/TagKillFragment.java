package com.uk.tsl.rfid.samples.tagkill;

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
import com.uk.tsl.rfid.samples.tagkill.databinding.FragmentTagkillBinding;
import com.uk.tsl.utils.Observable;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class TagKillFragment extends Fragment
{
    // Debugging
    private static final String TAG = "TagKillFragment";
    private static final boolean D = BuildConfig.DEBUG;

    private FragmentTagkillBinding binding;


    // The list of results from actions
    private ArrayAdapter<String> mResultsArrayAdapter;
    private ListView mResultsListView;

    // The text view to display the RF Output Power used in RFID commands
    private TextView mPowerLevelTextView;
    // The seek bar used to adjust the RF Output Power for RFID commands
    private SeekBar mPowerSeekBar;
    // The current setting of the power level
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    // Error report
    private TextView mResultTextView;

    private TextView mTotalTagsTextView;

    // Target tag
    private TextView mtargetTagTextView;
    private TextView mtargetTagTIDTextView;
    private TextView mArmed3TextView;
    private TextView mArmed2TextView;
    private TextView mArmed1TextView;
    private TextView mArmedTextView;
    private TextView mKillSuccessTextView;
    private TextView mKillFailedTextView;


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

    // All of the reader tagkill tasks are handled by this class
    private TagKillModel mModel;

    // Start stop buttons
    Button mStartButton;
    Button mStopButton;


    private static String sEPCTag = "EPC:";
    private static String sTIDTag = "TID:";
    private static String sRSSITag = "RSSI:";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragmentTagkillBinding.inflate(inflater, container, false);

        mTotalTagsTextView = binding.totalTagsTextView;

        mResultsArrayAdapter = new ArrayAdapter<String>(this.getContext(),R.layout.result_item);

        mResultTextView = binding.resultTextView;

        mtargetTagTextView = binding.targetTagTextView;
        mtargetTagTIDTextView = binding.targetTagTIDTextView;
        mArmedTextView = binding.armedTextView;
        mArmed1TextView = binding.armed1;
        mArmed2TextView = binding.armed2;
        mArmed3TextView = binding.armed3;
        mKillSuccessTextView = binding.killSuccessTextView;
        mKillFailedTextView = binding.killFailedTextView;

        // Find and set up the results ListView
        mResultsListView = binding.resultListView;
        mResultsListView.setAdapter(mResultsArrayAdapter);
        mResultsListView.setFastScrollEnabled(true);

        mResultsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {

                Log.v("long clicked","pos: " + pos);
                String info = mResultsArrayAdapter.getItem(pos);
                if( info.contains(sTIDTag))
                {
                    // Extract EPC and TID
                    int EPCPos = info.indexOf(sEPCTag);
                    int TIDPos = info.indexOf(sTIDTag);
                    int RSSIPos = info.indexOf(sRSSITag);
                    String epc = info.substring(EPCPos + sEPCTag.length(), TIDPos).trim();
                    String tid = info.substring(TIDPos + sTIDTag.length(), RSSIPos).trim();

                    if (mModel != null)
                    {
                        if( mModel.getTargetEPC() != null)
                        {
                            // Reset target
                        }

                        mModel.setTargetEPC(epc);
                        mtargetTagTextView.setText(epc);

                        mModel.setTargetTID(tid);
                        mtargetTagTIDTextView.setText(tid);

                        mModel.setArmingLevel(0);
                        mModel.updateSwitchConfiguration();
                        mResultTextView.setText(String.format("Pull trigger %d time%s to kill tag", mModel.getArmingThreshold(), (mModel.getArmingThreshold() == 1 ? "" : "s") ));

                        updateUI();
                    }
                }

                return true;
            }
        });

        // Hook up the button actions
        Button cButton = binding.clearButton;
        cButton.setOnClickListener(mClearButtonListener);

        // The SeekBar provides an integer value for the antenna power
        mPowerLevelTextView = binding.powerTextView;
        mPowerSeekBar = binding.powerSeekBar;
        mPowerSeekBar.setOnSeekBarChangeListener(mPowerSeekBarListener);

        mRFModesArrayAdapter = new ArrayAdapter<RFModeItem>(this.getContext(), android.R.layout.simple_spinner_item, mRFModes);
        // Find and set up the sessions spinner
        Spinner spinnerRF = binding.rfModeSpinner;
        mRFModesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRF.setAdapter(mRFModesArrayAdapter);
        spinnerRF.setOnItemSelectedListener(mRFModeSelectedListener);
        spinnerRF.setSelection(0);

        // Set up the "Uniques Only" Id check box listener
        CheckBox ucb = binding.uniquesCheckBox;
        ucb.setOnClickListener(mUniquesCheckBoxListener);

        //Create a (custom) model and configure its commander and handler
        mModel = new TagKillModel();
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

    @Override
    public void onStop()
    {
        super.onStop();

        if( getCommander().isConnected() ) {
            mModel.clearTarget();
            mModel.updateSwitchConfiguration();
        }
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

        updateUI();
    }


    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------

    private static class GenericHandler extends WeakHandler<TagKillFragment>
    {
        public GenericHandler(TagKillFragment t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, TagKillFragment t)
        {
            try {
                switch (msg.what)
                {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        //TODO: process change in model busy state
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        // Examine the message for prefix
                        String message = (String) msg.obj;
                        if (message.startsWith("KF:"))
                        {
                            t.showFailedMessage();
                        }
                        else
                        {
                            if (message.startsWith("ER:"))
                            {
                                t.mResultTextView.setText(message.substring(3));
                                t.mResultTextView.setBackgroundColor(0xD0FFFFFF);
                            }
                            else if (message.startsWith("CL:"))
                            {
                                t.mResultsArrayAdapter.clear();
                                t.mTotalTagsTextView.setText("");
                                t.mResultTextView.setText("");
                            }
                            else if (message.startsWith("KS:"))
                            {
                                t.mKillSuccessTextView.setVisibility(View.VISIBLE);
                                t.mArmedTextView.setVisibility(View.GONE);
                                break;
                            }
                            else if (message.startsWith("KI:"))
                            {
                                t.mResultTextView.setText(message.substring(3));
                            }
                            else if (message.startsWith("UI:"))
                            {
                                // Model requested UI update
                            }
                            else
                            {
                                t.mResultsArrayAdapter.add(message);
                                t.scrollResultsListViewToBottom();
                                t.mTotalTagsTextView.setText(String.format(Locale.US, "%d", t.mResultsArrayAdapter.getCount()));
                            }
                            t.updateUI();
                        }
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };

    private void showFailedMessage()
    {
        mKillFailedTextView.setVisibility(View.VISIBLE);
        mKillSuccessTextView.setVisibility(View.GONE);
        mArmedTextView.setVisibility(View.GONE);
    }

    //
    // Set the state for the UI controls
    //
    private void updateUI() {
        //boolean isConnected = getCommander().isConnected();
        //TODO: configure UI control state

        mKillFailedTextView.setVisibility(View.GONE);
        mKillSuccessTextView.setVisibility(mModel.lastKillSucceeded() ? View.VISIBLE : View.GONE);
        mArmedTextView.setVisibility(mModel.lastKillSucceeded() ? View.GONE : View.VISIBLE);

        // TargetTag
        if(mModel.getTargetEPC() != null) {
            mtargetTagTextView.setText(mModel.getTargetEPC());
            mtargetTagTIDTextView.setText((mModel.getTargetTID()));

            mArmedTextView.setTextColor(getResources().getColor(R.color.armed_enabled_text, null));
            mArmedTextView.setBackgroundColor(getResources().getColor(R.color.armed_enabled_background, null));

            mArmed1TextView.setVisibility(mModel.getArmingLevel() > 0 ? View.VISIBLE : View.INVISIBLE);
            mArmed2TextView.setVisibility(mModel.getArmingLevel() > 1 ? View.VISIBLE : View.INVISIBLE);
            mArmed3TextView.setVisibility(mModel.getArmingLevel() > 2 ? View.VISIBLE : View.INVISIBLE);

        } else {
            mtargetTagTextView.setText(getText(R.string.dash_placeholder));
            mtargetTagTIDTextView.setText(getText(R.string.dash_placeholder));

            mArmedTextView.setTextColor(getResources().getColor(R.color.armed_disabled_text, null));
            mArmedTextView.setBackgroundColor(getResources().getColor(R.color.armed_disabled_background, null));

            mArmed1TextView.setVisibility(View.INVISIBLE);
            mArmed2TextView.setVisibility(View.INVISIBLE);
            mArmed3TextView.setVisibility(View.INVISIBLE);

            if( getCommander().isConnected())
            {
                if (mResultsArrayAdapter.getCount() > 0)
                {
                    mResultTextView.setText("Long press on Tag to select target.");
                }
                else
                {
                    mResultTextView.setText("Pull trigger to scan for Impinj tags.");
                }
            } else {
                mResultTextView.setText("Connect a TSL Reader.");
            }
        }
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
            }
            else
            {
                binding.rfModeSpinner.setEnabled(false);
            }

            // Update for any change in power limits
            setPowerBarLimits();

            // This may have changed the current power level setting if the new range is smaller than the old range
            // so update the model's tagkill command for the new power value
            mModel.getCommand().setOutputPower(mPowerLevel);

            mModel.resetDevice();
            mModel.updateConfiguration();
        }

        updateUI();
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
        //mPowerLevel = deviceProperties.getMaximumCarrierPower();
        mPowerLevel = 16;
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

    // Clear action
    private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                // Clear the list
                mResultsArrayAdapter.clear();
                mResultTextView.setText("");
                mResultTextView.setBackgroundColor(0x00FFFFFF);
                mModel.clearUniques();
                mTotalTagsTextView.setText("");

                mModel.clearTarget();
                mModel.updateSwitchConfiguration();

                updateUI();

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
    // Handler for changes in Tag Info requested
    //----------------------------------------------------------------------------------------------

    private final View.OnClickListener mInfoCheckBoxListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                CheckBox infoCheckBox = (CheckBox)v;

                mModel.setInfoRequested(infoCheckBox.isChecked());
                mModel.updateConfiguration();

                updateUI();

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

                updateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
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