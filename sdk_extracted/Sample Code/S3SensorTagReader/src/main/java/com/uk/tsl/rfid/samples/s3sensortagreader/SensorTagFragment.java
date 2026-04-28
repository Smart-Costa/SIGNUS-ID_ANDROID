package com.uk.tsl.rfid.samples.s3sensortagreader;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.TagReadingViewAdapter;
import com.uk.tsl.rfid.WeakHandler;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;
import com.uk.tsl.rfid.asciiprotocol.commands.VersionInformationCommand;
import com.uk.tsl.rfid.devicelist.BuildConfig;
import com.uk.tsl.rfid.samples.s3sensortagreader.databinding.FragmentSensorTagBinding;
import com.uk.tsl.utils.Observable;

import java.util.ArrayList;

public class SensorTagFragment extends Fragment
{
    // Debugging
    private static final String TAG = "SensorTagFragment";
    private static final boolean D = BuildConfig.DEBUG;

    private FragmentSensorTagBinding binding;
    private TagReadingViewAdapter mScannedTagsAdapter;

    // All of theSensor Tag tasks are handled by this class
    private SensorTagModel mModel;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {
        binding = FragmentSensorTagBinding.inflate(inflater, container, false);

        //Create a (custom) model and configure its commander and handler
        mModel = SensorTagModel.sharedInstance();
        mModel.setCommander(getCommander());

        // The handler for model messages
        GenericHandler genericModelHandler = new GenericHandler(this);
        mModel.setHandler(genericModelHandler);

        mScannedTagsAdapter = new TagReadingViewAdapter(getActivity(), R.id.scannedTagListView, mModel.getTagReadings());
        binding.scannedTagListView.setAdapter(mScannedTagsAdapter);

        return binding.getRoot();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonConfigure.setOnClickListener(v -> {

            NavHostFragment.findNavController(SensorTagFragment.this)
                           .navigate(R.id.action_SensorTagFragment_to_ConfigureFragment);
            mModel.setEnabled(false);
        } );

        binding.buttonClear.setOnClickListener(v -> {
            mModel.clearTagReadings();
            mScannedTagsAdapter.notifyDataSetChanged();
        } );

        mModel.setEnabled(isSupportedReader());
        updatePowerBar();
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

        // Disable
        mModel.setEnabled(false);
        mModel.resetReader();

        // Stop observing events from the AsciiCommander
        getCommander().stateChangedEvent().removeObserver(mConnectionStateObserver);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        // Observe events from the AsciiCommander
        getCommander().stateChangedEvent().addObserver(mConnectionStateObserver);

        // Ensure the current Reader state is displayed
        ((S3SensorTagReaderActivity)getActivity()).displayReaderState();

        // Disable
        mModel.setEnabled(isSupportedReader());
        mModel.configureReader();

        updateUI();
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


    private boolean isSupportedReader()
    {
        boolean isSupported = false;

        boolean isTR = mModel.isTemperatureRequired();
        boolean isA3 = isAscii3Reader();
        isSupported = !isTR || isA3;

        return isSupported;
    }


    private boolean isAscii3Reader()
    {
        boolean isAscii3 = false;

        if( getCommander().isConnected() )
        {
            DeviceProperties properties = getCommander().getDeviceProperties();
            if( properties != null )
            {
                VersionInformationCommand info = properties.getInformationCommand();
                if( info != null && info.getAsciiProtocol() != null)
                {
                    isAscii3 = info.getAsciiProtocol().startsWith("3");
                }
            }
        }

        return isAscii3;
    }


    //
    // Handle the connection state change events from the AsciiCommander
    //
    private final Observable.Observer<String> mConnectionStateObserver = (observable, reason) ->
    {
        if (D) { Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + getCommander().isConnected()); }

        if( getCommander().isConnected() )
        {
            mModel.setEnabled(isSupportedReader());
            if( !isSupportedReader() )
            {
                NavHostFragment.findNavController(SensorTagFragment.this)
                               .navigate(R.id.action_SensorTagFragment_to_NotCompatibleFragment);
            }
            else
            {
                mModel.configureReader();

                updatePowerBar();
            }
        }

        updateUI();
    };


    private void updatePowerBar()
    {
        if( getCommander().isConnected() )
        {
            // Update the power bar limits
            int min = getCommander().getDeviceProperties().getMinimumCarrierPower();
            int max = getCommander().getDeviceProperties().getMaximumCarrierPower();

            binding.powerProgressBar.setMin(min);
            binding.powerStartLabelTextView.setText("" + min);
            binding.powerProgressBar.setMax(max);
            binding.powerEndLabelTextView.setText("" + max);
        }
    }

    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------

    private static class GenericHandler extends WeakHandler<SensorTagFragment>
    {
        public GenericHandler(SensorTagFragment t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, SensorTagFragment t)
        {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        if( t.mModel.error() != null ) {
                            t.displayMessage("\n Task failed:\n" + t.mModel.error().getMessage() + "\n\n");
                        }
                        t.updateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String)msg.obj;
                        t.displayMessage(message);
                        break;

                    case SensorTagModel.POWER_CHANGE_NOTIFICATION:
                        int newPower = msg.arg1;
                        t.binding.powerProgressBar.setProgress(newPower, true);
                        t.binding.powerTitleTextView.setText(newPower + " dBm");
                        break;

                    case SensorTagModel.READING_UPDATE_NOTIFICATION:
                        t.mScannedTagsAdapter.notifyDataSetChanged();
                        if (D) { Log.d(getClass().getName(), "Data updated"); }
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

    // Display the given message
    // Ensures change happens on the UI thread
    private void displayMessage(String message)
    {
        final String msg = message;
        binding.messageTextView.post(new Runnable() {
            @Override
            public void run() {
                binding.messageTextView.setText(msg);
            }
        });
    }


    //
    // Set the state for the UI controls
    //
    private void updateUI()
    {
        boolean isConnected = getCommander().isConnected();
        boolean canIssueCommand = isConnected & !mModel.isBusy();

        String instructions = "";
        if( isConnected)
        {
            boolean isSupported = isSupportedReader();
            if( !isSupported )
            {
                instructions = getResources().getString(R.string.not_compatible2) + "\n\n" + getResources().getString(R.string.not_compatible3);
            }
            else
            {
                if (mModel.isScanning())
                {
                    instructions = "Pull trigger to stop";
                }
                else
                {
                    instructions = "Pull trigger to scan";
                }
            }

        }
        else
        {
            instructions = "Connect a TSL Reader";
        }
        binding.messageTextView.setText(instructions);

    }




}