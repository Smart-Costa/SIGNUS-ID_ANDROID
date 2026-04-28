//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.uk.tsl.rfid;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.samples.bulkencoding.BuildConfig;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelBase {

	// Debugging
	private static final boolean D = BuildConfig.DEBUG;

	// Model busy state changed message
	public static final int BUSY_STATE_CHANGED_NOTIFICATION = 1;
	public static final int MESSAGE_NOTIFICATION = 2;

	// 
	protected Handler mHandler;
	protected boolean mBusy;
	private Exception mException;
	protected AsciiCommander mCommander;
	protected AsyncTask<Void, Void, Void> mTaskRunner;
	protected double mLastTaskExecutionDuration;

	private Date mTaskStartTime;

	/**
	 * @return true if the model is currently performing a task
	 */
	public boolean isBusy() { return mBusy; }

	/**
	 * Set the task busy state
	 * @param isBusy true when the model is busy
	 */
	protected void setBusy(boolean isBusy)
	{
		if( mBusy != isBusy  )
		{
			mBusy = isBusy;

			if( mHandler != null )
			{
				Message msg = mHandler.obtainMessage(BUSY_STATE_CHANGED_NOTIFICATION, isBusy);
	        	mHandler.sendMessage(msg);
			}
		}
	}


	/**
	 * Send a message to the client using the current Handler
	 * 
	 * @param message The message to send as String
	 */
	protected void sendMessageNotification(String message)
	{
		if( mHandler != null )
		{
			Message msg = mHandler.obtainMessage(MESSAGE_NOTIFICATION, message);
			mHandler.sendMessage(msg);
		}
	}

    /**
     *  Send the given list of messages to the client using the current handler
     * @param messages the list of messages
     */
    protected void sendMessageListNotification(List<String> messages)
    {
        if( mHandler != null && messages != null)
        {
            String message = "";
            for( String line : messages )
            {
                if( line != null )
                {
                    message += line + "\n";
                }
            }
            Message msg = mHandler.obtainMessage(MESSAGE_NOTIFICATION, message);
            mHandler.sendMessage(msg);
        }
    }

    public boolean isTaskRunning() { return mTaskRunner != null; }
	
	public ModelBase()
	{
		mCommander = null;
		mHandler = null;
		mBusy = false;
		mLastTaskExecutionDuration = -1.00;
	}

	/**
	 * @return the commander the model uses
	 */
	public AsciiCommander getCommander() { return mCommander; }
	/**
	 * @param commander the commander the model uses
	 */
	public void setCommander(AsciiCommander commander) { mCommander = commander; }

	/**
	 * @return the handler for model notifications
	 */
	public Handler getHandler() { return mHandler; }
	/**
	 * @param handler the handler for model notifications
	 */
	public void setHandler(Handler handler) { mHandler = handler; }

	
	/**
	 * @return the error as an exception or null if none
	 */
	public Exception error() { return mException; }

	/**
	 * @param e the error as an exception
	 */
	protected void setError(Exception e) { mException = e; }

	/**
	 * @return the current execution duration if a task is running otherwise the duration of the last task
	 */
	public final double getTaskExecutionDuration() {
		if( mLastTaskExecutionDuration >= 0.0 ) {
			return mLastTaskExecutionDuration;
		} else {
			Date now = new Date();
			return (now.getTime() - mTaskStartTime.getTime()) / 1000.0;
		}
	}

	/**
	 * Execute the given task
	 * 
	 * The busy state is notified to the client
	 * 
	 * Tasks should throw an exception to indicate (and return) error
	 * 
	 * @param task the Runnable task to be performed 
	 */
	public void performTask(Runnable task) throws ModelException
	{
        if( mCommander == null ) {
			throw( new ModelException("There is no AsciiCommander set for this model!") );
		} else {
			if( mTaskRunner != null ) {
				throw( new ModelException("Task is already running!"));
			} else {
				mTaskRunner = new ModelTask(this, task);

				try {
    				mTaskRunner.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[])null);
				} catch( Exception e ) {
					mException = e;
				}
			}
		}
	}


    private static class ModelTask extends AsyncTask<Void, Void, Void>
    {
        private final Runnable rTask;
        private final ModelBase mModel;

        public ModelTask(ModelBase model, Runnable task)
        {
            super();
            rTask = task;
            mModel = model;
        }

        @Override
        protected void onPreExecute()
        {
            mModel.mLastTaskExecutionDuration = -1.0;
            mModel.mTaskStartTime = new Date();
        }

        protected Void doInBackground(Void... voids)
        {
            try {
                mModel.setBusy(true);
                mModel.mException = null;

                rTask.run();

            } catch (Exception e) {
                mModel.mException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mModel.mTaskRunner = null;
            mModel.setBusy(false);

            // Update the time taken
            Date finishTime = new Date();
            mModel.mLastTaskExecutionDuration = (finishTime.getTime() - mModel.mTaskStartTime.getTime()) / 1000.0;

            if(D) Log.i(getClass().getName(), String.format("Time taken (ms): %d %.2f", finishTime.getTime() - mModel.mTaskStartTime.getTime(), mModel.mLastTaskExecutionDuration));
        }
    };


    /**
     * Serial task runner
     * Executes given Runnable on a Serial Queue
     * @param task
     */
    protected void serialTaskRunner(Runnable task)
    {
        String tName = Thread.currentThread().getName();
        if(D) Log.d(getClass().getName(), "serialTaskRunner launch Thread: " + tName );

        if( mSerialExecutor == null )
        {
            mSerialExecutor = Executors.newSingleThreadExecutor();
        }
        mSerialExecutor.execute( task );
    }
    private ExecutorService mSerialExecutor;

    /**
     * Executes the given task on a non-UI thread
     * if the method is invoked from a non-UI thread the task just runs
     * otherwise the task is executed by the serialTaskRunner()
     * @param task the Runnable task
     */
    protected void runOffUIThread(Runnable task)
    {
        // Execute off the main thread, when necessary
        if (Looper.getMainLooper().isCurrentThread())
        {
            if(D) Log.d(getClass().getName(), "runOffUIThread => serialTaskRunner");

            serialTaskRunner(task);
        }
        else
        {
            task.run();
        }
    }
}
