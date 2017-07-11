package com.actions.bluetoothbox.app;

import com.actions.bluetoothbox.util.Constant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class BluetoothBoxControl extends BroadcastReceiver {
	private static String TAG = "BluetoothBoxControl";
	

	public static final String CMDNAME = "command";

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.i(TAG, "BluetoothBoxControl onReceive");
		// 获得Action
		String intentAction = intent.getAction();
		// 获得KeyEvent对象
		KeyEvent keyEvent = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			// 获得按键字节码
			int keyCode = keyEvent.getKeyCode();
			Log.i(TAG, "keycode :" +keyCode);
			// 按下 / 松开 按钮
			int keyAction = keyEvent.getAction();
			// 获得事件的时间
			long downtime = keyEvent.getEventTime();
			String command = null;
			switch (keyCode) {
			case KeyEvent.KEYCODE_MEDIA_STOP:
				command = "stop";
				break;
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				command = "play-pause";
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				command = "next";
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				command = "pre";
				break;
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				command = "pause";
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
				command = "play";
				break;
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				command = "fastforward";
				break;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				command = "rewind";
				break;
			}
			Log.i(TAG, "BluetoothBoxControl onReceive:" +command);
			if (command != null) {
				if (keyAction == KeyEvent.ACTION_DOWN) {
					Log.i(TAG, "KeyEvent.ACTION_DOWN");
					Log.i(TAG, "RepeatCount:"+keyEvent.getRepeatCount());
					if (keyEvent.getRepeatCount() == 0) {
						// 发一个广播出去
						Intent i = new Intent();
						i.setAction(Constant.MusicPlayControl.SERVICECMD);
						i.putExtra(CMDNAME, command);
						context.sendBroadcast(i);
					} else if (command.equals("fastforward") || command.equals("rewind")) {
						Intent i = new Intent();
						i.setAction(Constant.MusicPlayControl.SERVICECMD);
						i.putExtra(CMDNAME, command);
						context.sendBroadcast(i);
					}
				} else if (keyAction == KeyEvent.ACTION_UP  || command.equals("fastforward") || command.equals("rewind")){
					Log.i(TAG, "KeyEvent.ACTION_UP");
					 abortBroadcast();
				}
			}
		}

	}
}
