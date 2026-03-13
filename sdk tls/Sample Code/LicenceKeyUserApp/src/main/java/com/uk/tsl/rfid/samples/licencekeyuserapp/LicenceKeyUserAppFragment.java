package com.uk.tsl.rfid.samples.licencekeyuserapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.WeakHandler;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.samples.licencekeyuserapp.databinding.FragmentLicenceKeyUserAppBinding;
import com.uk.tsl.utils.StringHelper;

public class LicenceKeyUserAppFragment extends Fragment
{

    private FragmentLicenceKeyUserAppBinding binding;

    // Debug control
    private static final String TAG = "LKUserAppFragment";
    private static final boolean D = BuildConfig.DEBUG;

    // The model that performs all the actions
    private LicenceKeyUserAppModel mModel;

    // The list of results from actions
    private ArrayAdapter<String> mResultsArrayAdapter;


    /**
     * @return the current AsciiCommander
     */
    protected AsciiCommander getCommander()
    {
        return AsciiCommander.sharedInstance();
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragmentLicenceKeyUserAppBinding.inflate(inflater, container, false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mResultsArrayAdapter = new ArrayAdapter<String>(this.getContext(),R.layout.result_item);


        // Find and set up the results ListView
        binding.resultListView.setAdapter(mResultsArrayAdapter);
        binding.resultListView.setFastScrollEnabled(true);

        // Hook up the reader action buttons
        binding.inventoryButton.setOnClickListener(mInventoryButtonListener);
        binding.clearButton.setOnClickListener(mClearButtonListener);
        binding.barcodeButton.setOnClickListener(mBarcodeButtonListener);

        // Connect the respond to authorised reader control
        binding.onlyRespondToAuthorisedReadersCheckBox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CheckBox cb = (CheckBox)v;
                if( mModel != null )
                {
                    mModel.setOnlyAuthorisedReaderAllowed(cb.isChecked());
                }
                UpdateUI();
            }
        });

        //Create a (custom) model and configure its commander and handler
        mModel = new LicenceKeyUserAppModel(getContext());
        mModel.setCommander(getCommander());
        // The handler for model messages
        GenericHandler mGenericModelHandler = new GenericHandler(this);
        mModel.setHandler(mGenericModelHandler);

        // Set the starting values for the model
        if( mModel != null )
        {
            mModel.setOnlyAuthorisedReaderAllowed(binding.onlyRespondToAuthorisedReadersCheckBox.isChecked());
        }
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
                mModel.updateConfiguration();
            }

            else
            {
                mModel.validateReader();
            }
            UpdateUI();
        }
    };

    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------

    private static class GenericHandler extends WeakHandler<LicenceKeyUserAppFragment>
    {
        public GenericHandler(LicenceKeyUserAppFragment t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, LicenceKeyUserAppFragment t)
        {
            try
            {
                switch (msg.what)
                {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        //TODO: process change in model busy state
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        // Examine the message for prefix
                        String message = (String)msg.obj;
                        if( message.startsWith("ER:")) {
                            t.mResultsArrayAdapter.add( message.substring(3));
                        }
                        else if( message.startsWith("BC:")) {
                            t.mResultsArrayAdapter.add(message.substring(3));
                        } else {
                            t.mResultsArrayAdapter.add(message);
                        }
                        t.scrollResultsListViewToBottom();
                        t.UpdateUI();
                        break;

                    case LicenceKeyUserAppModel.AUTHORISATION_STATE_CHANGED_NOTIFICATION:
                        // Show the message
                        String aMessage = (String)msg.obj;
                        if( !StringHelper.isNullOrEmpty(aMessage))
                        {
                            t.mResultsArrayAdapter.add(aMessage);
                            t.scrollResultsListViewToBottom();
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

    // The handler for model messages
    private static GenericHandler mGenericModelHandler;

    //----------------------------------------------------------------------------------------------
    // UI state and display update
    //----------------------------------------------------------------------------------------------

    //
    // Set the state for the UI controls
    //
    private void UpdateUI()
    {
        boolean isAuthorised = mModel != null && mModel.isReaderAuthorised();
        binding.authorisationBannerTextView.setText(isAuthorised ? getString(R.string.banner_title_authorised) : getString(R.string.banner_title_not_authorised));
        binding.authorisationBannerTextView.setBackgroundColor(isAuthorised ? Color.parseColor("#00A000") : Color.parseColor("#9080a0"));
    }


    private void scrollResultsListViewToBottom() {
        binding.resultListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                binding.resultListView.setSelection(mResultsArrayAdapter.getCount() - 1);
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    // Button event handlers
    //----------------------------------------------------------------------------------------------

    // Scan action
    private View.OnClickListener mInventoryButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                // Perform a transponder scan
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
                mResultsArrayAdapter.clear();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // Barcode scan action
    private View.OnClickListener mBarcodeButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                // Perform a transponder scan
                mModel.barcodeScan();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


}