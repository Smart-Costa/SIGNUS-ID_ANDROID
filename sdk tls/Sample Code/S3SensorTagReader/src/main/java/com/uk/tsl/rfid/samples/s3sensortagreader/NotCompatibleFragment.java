package com.uk.tsl.rfid.samples.s3sensortagreader;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.devicelist.BuildConfig;
import com.uk.tsl.rfid.samples.s3sensortagreader.databinding.FragmentNotCompatibleBinding;
import com.uk.tsl.utils.Observable;

public class NotCompatibleFragment extends Fragment
{
    // Debugging
    private static final String TAG = "NotCompatibleFragment";
    private static final boolean D = BuildConfig.DEBUG;

    private FragmentNotCompatibleBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragmentNotCompatibleBinding.inflate(inflater, container, false);
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

//        mModel.setEnabled(false);

        // Stop observing events from the AsciiCommander
        getCommander().stateChangedEvent().removeObserver(mConnectionStateObserver);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

//        mModel.setEnabled(true);

        // Observe events from the AsciiCommander
        getCommander().stateChangedEvent().addObserver(mConnectionStateObserver);

//        UpdateUI();
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
            if( getCommander().getDeviceProperties().getInformationCommand().getAsciiProtocol().startsWith("3"))
            {
                NavHostFragment.findNavController(NotCompatibleFragment.this)
                               .navigate(R.id.action_NotCompatibleFragment_to_SensorTagFragment);
            }
        }

        //UpdateUI();
    };
}