package com.actions.bluetoothbox.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.actions.bluetoothbox.util.Utils;

import android.util.Log;

public class LogcatThread extends Thread {
	private static final String TAG = "LogcatThread";
	public final static int STATE_DONE = 0;
	public final static int STATE_RUNNING = 1;
	private boolean cleanFlag = true;
	int mState;
	private String mLogPath = null;
	private int LOGFILELENGTH = 1024 * 1024 * 50;
	private Process process;

	public void run() {
		Log.i(TAG, "log start!");
		mState = STATE_RUNNING;
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		FileWriter logcatFileWriter = null;
		Log.i(TAG, "" + mLogPath);
		File file = new File(mLogPath);
		if (file.exists() && cleanFlag) {
			File mBackupFile = new File(mLogPath + ".bak");
			try {
				if (mBackupFile.exists()) {
					mBackupFile.delete();
				}
				Utils.copyFile(file, mBackupFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			file.delete();
			Log.v(TAG, "Log file Clean!");
		}
		try {
			Process clear = Runtime.getRuntime().exec("logcat -c");
			clear.waitFor();
			clear.destroy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> commandLine = new ArrayList<String>();
		commandLine.add("logcat");
		commandLine.add("-v");
		commandLine.add("time");
		String line;
		try {
			process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
			inputStream = process.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader, 1024);

			logcatFileWriter = new FileWriter(mLogPath, true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (mState == STATE_RUNNING) {
			if (file.length() > LOGFILELENGTH) {
				Log.i(TAG, "Logcat File is > 50M .");
				break;
			}
			try {
				while (mState == STATE_RUNNING && (line = bufferedReader.readLine()) != null) {
					logcatFileWriter.append(line);
					logcatFileWriter.append("\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, "I get the writer file exception,stop itself.");
				break;
			} finally {

			}
		}
		Log.i(TAG, "log over");
		try {
			if (logcatFileWriter != null)
				logcatFileWriter.close();
			if (bufferedReader != null)
				bufferedReader.close();
			if (inputStreamReader != null)
				inputStreamReader.close();
			if (inputStream != null)
				inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (process != null) {
			process.destroy();
		}
	}

	public void setState(int state) {
		mState = state;
	}

	public void setAppend() {
		cleanFlag = false;
	}

	public int getThreadState() {
		return mState;

	}

	public void setLogFilePath(String path) {
		this.mLogPath = path;
	}
}
