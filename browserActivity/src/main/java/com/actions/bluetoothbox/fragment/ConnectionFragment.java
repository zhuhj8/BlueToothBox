package com.actions.bluetoothbox.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.app.BrowserActivity;
import com.actions.bluetoothbox.app.DeviceListAdapter;
import com.actions.bluetoothbox.app.DeviceListAdapter.DeviceEntry;
import com.actions.ibluz.factory.BluzDeviceFactory.ConnectionState;
import com.actions.ibluz.factory.IBluzDevice;
import com.actions.ibluz.factory.IBluzDevice.OnDiscoveryListener;

public class ConnectionFragment extends SherlockFragment {
	private final static String TAG = "ConnectionFragment";

	private final static int REQUEST_BLUETOOTH_ON = 100;

	private final static int MAX_RETRY_TIMES = 5;

	private BrowserActivity mActivity;
	private View mMainView;
	private ProgressBar mSearchProgressBar;
	private ListView mDeviceListView;
	private DeviceListAdapter mDeviceAdapter;
	private List<DeviceEntry> mDeviceEntries;
	private IBluzDevice mBluzConnector;
	/* Avoid special cases：Some phone like Nexus4 start discovery twice */
	private boolean mDiscoveryStarted;
	private int mConnectRetryTimes;
	private AlertDialog mAlertDialog = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView");
		setHasOptionsMenu(true);
		mMainView = inflater.inflate(R.layout.fragment_connection, container, false);
		mDeviceListView = (ListView) mMainView.findViewById(R.id.deviceListView);
		mSearchProgressBar = (ProgressBar) mMainView.findViewById(R.id.progressBar);

		mDeviceEntries = new ArrayList<DeviceEntry>();
		mDeviceAdapter = new DeviceListAdapter(getActivity(), mDeviceEntries);
		mDeviceListView.setOnItemClickListener(mOnItemClickListener);
		mDeviceListView.setOnItemLongClickListener(mOnItemLongClickListener);
		mDeviceListView.setAdapter(mDeviceAdapter);

		mActivity = (BrowserActivity) getActivity();
		mBluzConnector = mActivity.getBluzConnector();
		mBluzConnector.setOnDiscoveryListener(mOnDiscoveryListener);

		return mMainView;
	}

	@Override
	public void onPause() {
		super.onPause();
		dismissDialogShowSetting();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		refresh(false);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v(TAG, "onDestroyView");
		mBluzConnector.setOnDiscoveryListener(null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
	}

	private void startDiscovery() {
		mDiscoveryStarted = false;
		mConnectRetryTimes = 0;
		/* in case some device(e.g. LT15i don't send onDiscoveryStarted */
		initEntry();
		mBluzConnector.startDiscovery();
	}

	private long refreshTime = 0;

	/**
	 * Base on Android 2.3 (Some phone like: G13) can not search
	 * device(BluetoothAdapter Unresponsive) possible
	 * 
	 * @param reset
	 *            while true re-enable
	 */
	public void refresh(boolean reset) {
		if (mBluzConnector.isEnabled()) {
			if (reset && mDeviceEntries.size() == 0) {
				long interval = System.currentTimeMillis() - refreshTime;
				if (interval > 2000) {
					refreshTime = System.currentTimeMillis();
					/*
					 * long time no operation(like 5seconds after disconnected)
					 * didn't need
					 */
					if (interval < 5000) {
						showSetting();
					}
				}
			}
			Log.v(TAG, "===================shine===进入页面搜索蓝牙========================");
			startDiscovery();
		} else {
			/**
			 * OOXX Compatible Galaxy Note3-SM-N9006(flash back)
			 */
			if (Build.MODEL.contains("SM-N9006")) {
				mBluzConnector.enable();
				startDiscovery();
			} else {
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, REQUEST_BLUETOOTH_ON);
			}

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_BLUETOOTH_ON) {
			if (resultCode == Activity.RESULT_OK) {
				startDiscovery();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				mActivity.finish();
			}
		}
	}

	// use getEntryState to keep the original state
	private int getEntryState(BluetoothDevice device, int defState) {
		for (DeviceEntry entry : mDeviceEntries) {
			if (entry.device.equals(device)) {
				return entry.state;
			}
		}

		return defState;
	}

	private void initEntry() {
		if (!mDiscoveryStarted) {
			mDeviceEntries.clear();
		}

		// device cannot be found while connected, so manually add it here
		int state = ConnectionState.A2DP_DISCONNECTED;
		//获取当前已连接的设备，含音频和数据
		BluetoothDevice device = mBluzConnector.getConnectedDevice();
		//如果SPP连上，返回SPP状态
		if (device != null) {
			state = getEntryState(device, ConnectionState.SPP_CONNECTED);
		} else {
			//如果SPP未连上，返回A2DP状态，获取当前已连接的音频设备
			device = mBluzConnector.getConnectedA2dpDevice();
			if (device != null) {
				state = getEntryState(device, ConnectionState.A2DP_CONNECTED);
			}
		}

		if (device != null && findEntry(device) == null) {
			//蓝牙显示列表添加设备
			mDeviceEntries.add(new DeviceEntry(device, state));
		}
		//页面更新蓝牙列表
		mDeviceAdapter.notifyDataSetChanged();
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DeviceEntry deviceEntry = (DeviceEntry) parent.getItemAtPosition(position);
			mSearchProgressBar.setVisibility(View.INVISIBLE);
			mBluzConnector.connect(deviceEntry.device);
		}
	};

	private OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			DeviceEntry deviceEntry = (DeviceEntry) parent.getItemAtPosition(position);
			if (deviceEntry.state == ConnectionState.SPP_CONNECTED) {
				mActivity.showDisconncetDialog();
			}

			return true;
		}
	};

	private boolean retry(BluetoothDevice device) {
		if (mConnectRetryTimes < MAX_RETRY_TIMES) {
			Log.i(TAG, "retry:" + mConnectRetryTimes);
			mBluzConnector.retry(device);
			mConnectRetryTimes++;
			return true;
		} else {
			mConnectRetryTimes = 0;
			return false;
		}
	}

	private synchronized DeviceEntry findEntry(BluetoothDevice device) {
		DeviceEntry deviceEntry = null;
		for (DeviceEntry entry : mDeviceEntries) {
			if (entry.device.getAddress().equals(device.getAddress())) {
				deviceEntry = entry;
				break;
			}
		}

		return deviceEntry;
	}

	private void dismissDialogShowSetting() {
		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
			mAlertDialog = null;
		}
	}

	private void showSetting() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.dialog_title_info);
		builder.setMessage(R.string.dialog_show_setting_message);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.dialog_show_setting_positive, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissDialogShowSetting();

				Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissDialogShowSetting();
			}
		});

		mAlertDialog = builder.create();
		mAlertDialog.show();
	}

	private OnDiscoveryListener mOnDiscoveryListener = new OnDiscoveryListener() {
		@Override
		public void onConnectionStateChanged(BluetoothDevice device, int state) {
			if (device != null) {
				DeviceEntry entry = findEntry(device);
				if (entry == null) {
					entry = new DeviceEntry(device, state);
					mDeviceEntries.add(entry);
				}

				Log.i(TAG, "onConnectionStateChanged:" + state + "@" + device.getName());
				if (state == ConnectionState.A2DP_FAILURE) {
					state = ConnectionState.A2DP_DISCONNECTED;
					if (!retry(device)) {
						Toast.makeText(mActivity, R.string.connection_connect_fail, Toast.LENGTH_SHORT).show();
						showSetting();
					}
				} else if (state == ConnectionState.SPP_FAILURE) {
					state = ConnectionState.A2DP_CONNECTED;
					if (!retry(device)) {
						Toast.makeText(mActivity, R.string.connection_connect_data_fail, Toast.LENGTH_SHORT).show();
					}
				}

				entry.state = state;
				mDeviceAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onDiscoveryStarted() {
			mSearchProgressBar.setVisibility(View.VISIBLE);
			initEntry();
		}

		@Override
		public void onDiscoveryFinished() {
			mSearchProgressBar.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onFound(BluetoothDevice device) {
			// device null for stub
			if ((device == null) || (findEntry(device) == null && device.getName() != null)) {
				mDeviceEntries.add(new DeviceEntry(device, ConnectionState.A2DP_DISCONNECTED));
				mDeviceAdapter.notifyDataSetChanged();
			}

			mDiscoveryStarted = true;
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			refresh(true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.connection_menu, menu);
	}
}
