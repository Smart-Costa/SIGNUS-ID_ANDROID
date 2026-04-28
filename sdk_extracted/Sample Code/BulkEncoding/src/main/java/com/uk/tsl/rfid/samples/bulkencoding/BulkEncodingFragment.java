package com.uk.tsl.rfid.samples.bulkencoding;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.uk.tsl.rfid.ModelBase;
import com.uk.tsl.rfid.WeakHandler;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.samples.bulkencoding.BuildConfig;
import com.uk.tsl.rfid.samples.bulkencoding.databinding.FragmentBulkEncodingBinding;
import com.uk.tsl.utils.Observable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class BulkEncodingFragment extends Fragment
{
    // Debugging
    private static final String TAG = "BulkEncodingFragment";
    private static final boolean D = BuildConfig.DEBUG;

    private FragmentBulkEncodingBinding binding;

    // The list of source data
    private ArrayAdapter<String> mSourceDataArrayAdapter;
    private ListView mSourceDataListView;

    // The list of results from actions
    private ArrayAdapter<String> mResultsArrayAdapter;
    private ListView mResultsListView;

    private TextView mResultTextView;

    // The identifier matching types
    // Order important - pos 1 must be TID
    private String[] mMatchTypes = new String[]
            {
                "EPC", // 0
                "TID", // 1
            };
    // The list of Matching Types
    private ArrayAdapter<String> mMatchTypeArrayAdapter;

    Spinner mMatchTypeS;
    EditText mMatchOffsetET;
    CheckBox mUsePasswordsCB;
    CheckBox mUseUserDataCB;

    Button mLoadButton;
    Button mEncodeButton;
    Button mRetryButton;
    Button mClearButton;


    private BulkEncodingModel mModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragmentBulkEncodingBinding.inflate(inflater, container, false);


        //Create a (custom) model and configure its commander and handler
        mModel = new BulkEncodingModel();
        mModel.setCommander(getCommander());
        // The handler for model messages
        GenericHandler mGenericModelHandler = new GenericHandler(this);
        mModel.setHandler(mGenericModelHandler);


        mLoadButton = binding.loadButton;
        mLoadButton.setOnClickListener(mLoadButtonListener);

        mEncodeButton = binding.encodeButton;
        mEncodeButton.setOnClickListener(mEncodeButtonListener);

        mRetryButton = binding.retryButton;
        mRetryButton.setOnClickListener(mRetryButtonListener);

        mClearButton = binding.clearButton;
        mClearButton.setOnClickListener(mClearButtonListener);

        mSourceDataArrayAdapter = new ArrayAdapter<>(this.getContext(),R.layout.result_item);
        mSourceDataListView = binding.sourceDataListView;
        mSourceDataListView.setAdapter(mSourceDataArrayAdapter);

        mResultsArrayAdapter = new ArrayAdapter<>(this.getContext(),R.layout.result_item);
        mResultsListView = binding.resultListView;
        mResultsListView.setAdapter(mResultsArrayAdapter);

        mResultTextView = binding.resultTextView;

        mMatchTypeArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, mMatchTypes);
        mMatchTypeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMatchTypeS = binding.matchTypeSpinner;
        mMatchTypeS.setAdapter(mMatchTypeArrayAdapter);
        mMatchTypeS.setSelection(mModel.useTidIdentifier() ? 1 : 0);
        mMatchTypeS.setOnItemSelectedListener(mMatchTypeSelectedListener);

        mMatchOffsetET = binding.matchOffsetEditText;
        mMatchOffsetET.setText("" + mModel.getMatchOffset());
        mMatchOffsetET.setOnEditorActionListener(mMatchOffsetEditorActionListener);

        mUsePasswordsCB = binding.usePasswordCheckBox;
        mUsePasswordsCB.setOnClickListener(mUsePasswordsCheckBoxListener);

        mUseUserDataCB = binding.useUserDataCheckBox;
        mUseUserDataCB.setOnClickListener(mUseUserDataCheckBoxListener);


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

    private static class GenericHandler extends WeakHandler<BulkEncodingFragment>
    {
        public GenericHandler(BulkEncodingFragment t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, BulkEncodingFragment t)
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
                            t.showError(message.substring(3));
                        }
                        else if( message.startsWith("ME:")) {
                            t.showMessage(message.substring(3));
                        }
                        else if( message.startsWith("SD:")) {
                            t.mSourceDataArrayAdapter.add(message);
                            t.scrollSourceDataListViewToBottom();
                        }
                        else {
                            t.mResultsArrayAdapter.add(message);

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

    private void showError(String message)
    {
        mResultTextView.setText( message );
        mResultTextView.setBackgroundColor(0xD0CC0000);
        mResultTextView.setTextColor(Color.WHITE);
    }

    private void showMessage(String message)
    {
        mResultTextView.setText( message );
        mResultTextView.setBackgroundColor(0x00FFFFFF);
        mResultTextView.setTextColor(Color.DKGRAY);
    }

    //
    // Set the state for the UI controls
    //
    private void UpdateUI() {
        boolean isConnected = getCommander().isConnected();
        //TODO: configure UI control state
        boolean isSeries3Reader = isConnected && getCommander().getDeviceProperties().getInformationCommand().getAsciiProtocol().startsWith(("3"));

        mLoadButton.setEnabled(isSeries3Reader);

        mMatchTypeS.setEnabled(isSeries3Reader);
        mMatchOffsetET.setEnabled(isSeries3Reader);
        mUsePasswordsCB.setEnabled(isSeries3Reader);
        mUseUserDataCB.setEnabled(isSeries3Reader);

        mModel.setEnabled(isSeries3Reader);

        boolean isListLoaded = mModel.getDataset() != null && mModel.getDataset().size() > 0;
        mEncodeButton.setEnabled(isSeries3Reader && isListLoaded);
        mRetryButton.setEnabled(isSeries3Reader && isListLoaded);
        mClearButton.setEnabled(isSeries3Reader && isListLoaded);

    }

    private void scrollSourceDataListViewToBottom() {
        mSourceDataListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mSourceDataListView.setSelection(mSourceDataArrayAdapter.getCount() - 1);
            }
        });
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
        if (D) { Log.d(getClass().getName(), "...Thread: " + Thread.currentThread().getName()); }

        if( getCommander().isConnected() )
        {
            mModel.resetAndUpdateConfig();
            if( getCommander().getDeviceProperties().getInformationCommand().getAsciiProtocol().startsWith(("3")))
            {
                // Only Series 3 Readers support these options
                showMessage("");
            }
            else
            {
            }

        }

        UpdateUI();
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in Match Type
    //----------------------------------------------------------------------------------------------

    private AdapterView.OnItemSelectedListener mMatchTypeSelectedListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mModel.setUseTidIdentifier(pos == 1);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes to the Match offset
    //----------------------------------------------------------------------------------------------

    private TextView.OnEditorActionListener mMatchOffsetEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
        {
            int result = actionId & EditorInfo.IME_MASK_ACTION;
            switch (result)
            {
                case EditorInfo.IME_ACTION_DONE:
                    // done stuff
                    int value = 0;
                    try {
                        value = Integer.parseInt(mMatchOffsetET.getText().toString());

                        mModel.setMatchOffset(value);
                    } catch (Exception e) {
                        mModel.setMatchOffset(0);
                    }
                    textView.clearFocus();
                    break;
            }
            UpdateUI();
            return false;
        }
    };

    //----------------------------------------------------------------------------------------------
    // Handler for changes in UsePasswords
    //----------------------------------------------------------------------------------------------

    private final View.OnClickListener mUsePasswordsCheckBoxListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                CheckBox infoCheckBox = (CheckBox)v;

                mModel.setUsePassword(infoCheckBox.isChecked());

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in UseUserData
    //----------------------------------------------------------------------------------------------

    private final View.OnClickListener mUseUserDataCheckBoxListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                CheckBox infoCheckBox = (CheckBox)v;

                mModel.setUseUserData(infoCheckBox.isChecked());

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // Button event handlers
    //----------------------------------------------------------------------------------------------

    // Load source data action
    private View.OnClickListener mLoadButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            ((BulkEncodingActivity)getActivity()).setDisconnectionSuspended(true);
            mGetDocument.launch(new String[]{ "text/plain" });
        }
    };



    // Encode action
    private View.OnClickListener mEncodeButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                mResultTextView.setText("");

                mModel.encode();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    // Retry action
    private View.OnClickListener mRetryButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                mResultTextView.setText("");

                mModel.retry();

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
                showMessage("");

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // File Selection
    //----------------------------------------------------------------------------------------------

    // GetContent creates an ActivityResultLauncher<String> to let you pass
// in the mime type you want to let the user select
    ActivityResultLauncher<String[]> mGetDocument = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    ((BulkEncodingActivity)getActivity()).setDisconnectionSuspended(false);
                    // Handle the returned Uri
                    try
                    {
                        ArrayList<BulkEncodingModel.Data> dataset = BulkEncodingDataLoader.loadFromUri(BulkEncodingFragment.this.getContext(), uri);
                        mModel.setDataset(dataset);
                        mSourceDataArrayAdapter.clear();
                        for (BulkEncodingModel.Data data : dataset)
                        {
                            mSourceDataArrayAdapter.add(data.toString());
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        showError("Unable to load file:\n" + uri.getPath());
                    }
                }
            });

}