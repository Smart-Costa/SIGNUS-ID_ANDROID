package com.uk.tsl.rfid.samples.s3sensortagreader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.uk.tsl.rfid.devicelist.BuildConfig;
import com.uk.tsl.rfid.samples.s3sensortagreader.databinding.FragmentConfigureBinding;

import java.util.Locale;

public class ConfigureFragment extends Fragment
{
    // Debugging
    private static final String TAG = "ConfigureFragment";
    private static final boolean D = BuildConfig.DEBUG;

    private FragmentConfigureBinding binding;
    private NavController.OnDestinationChangedListener mDestinationListener;

    // All of theSensor Tag tasks are handled by this class
    private SensorTagModel mModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {
        binding = FragmentConfigureBinding.inflate(inflater, container, false);

        mModel = SensorTagModel.sharedInstance();


        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

//        binding.buttonSecond.setOnClickListener(v ->
//                NavHostFragment.findNavController(ConfigureFragment.this)
//                               .navigate(R.id.action_ConfigureFragment_to_SensorTagFragment)
//        );

        mDestinationListener = (navController, navDestination, bundle) ->
        {
            if (D) { Log.d(TAG, "Destination: " + navDestination.getLabel()); }
            if( navDestination.getLabel() == getResources().getString(R.string.configure_fragment_label) )
            {
                if (D) { Log.d(TAG, "Arriving... "); }

                // Set display elements to current model values
                boolean isTR = mModel.isTemperatureRequired();
                binding.switchSensorType.setChecked(isTR);
                binding.editviewMinRssi.setText( String.format(Locale.US, "%d", mModel.getMinRSSI()));
                binding.editviewMaxRssi.setText( String.format(Locale.US, "%d", mModel.getMaxRSSI()));

                binding.editviewMinPower.setText( String.format(Locale.US, "%d", mModel.getMinPower()));
                binding.editviewMaxPower.setText( String.format(Locale.US, "%d", mModel.getMaxPower()));

                binding.editviewSamplesPerReading.setText( String.format(Locale.US, "%d", mModel.getSamplesPerTag()));
            }
            else
            {
                if (D) { Log.d(TAG, "Leaving. "); }
                // Clear all readings
                //
                // Changes in RSSI or SamplesPerTag invalidate previous readings
                // For simplicity in the sample code, we assume values are always modified on this screen
                // In production code check for changes and only clear when necessary
                //
                boolean isTR = binding.switchSensorType.isChecked();
                mModel.setTemperatureRequired(isTR);
                mModel.clearTagReadings();
                mModel.setMinRSSI(extractIntValue(binding.editviewMinRssi,mModel.getMinRSSI()));
                mModel.setMaxRSSI(extractIntValue(binding.editviewMaxRssi,mModel.getMaxRSSI()));
                mModel.setSamplesPerTag(extractIntValue(binding.editviewSamplesPerReading,mModel.getSamplesPerTag()));

                // Power limit changes should not affect existing readings
                mModel.setMinPower(extractIntValue(binding.editviewMinPower,mModel.getMinPower()));
                mModel.setMaxPower(extractIntValue(binding.editviewMaxPower,mModel.getMaxPower()));
            }
        };
        NavHostFragment.findNavController(ConfigureFragment.this).addOnDestinationChangedListener(mDestinationListener);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        NavHostFragment.findNavController(ConfigureFragment.this).removeOnDestinationChangedListener(mDestinationListener);
        binding = null;
    }

    // Returns the value in the EditText or the defaultValue if value cannot be parsed
    private int extractIntValue(EditText editText, int defaultValue)
    {
        int value = defaultValue;
        try
        {
            value = Integer.parseInt(editText.getText().toString());
        }
        catch(Exception ignored) {}
        return value;
    }
}