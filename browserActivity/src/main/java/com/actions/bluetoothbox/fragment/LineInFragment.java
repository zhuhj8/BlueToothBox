package com.actions.bluetoothbox.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.app.BrowserActivity;
import com.actions.ibluz.manager.BluzManagerData.OnAuxUIChangedListener;
import com.actions.ibluz.manager.BluzManagerData.PlayState;
import com.actions.ibluz.manager.IAuxManager;

public class LineInFragment extends SherlockFragment {
	private static final String TAG = "LineInFragment";

	private BrowserActivity mActivity;
	private View mMainView;
	private ImageButton mPlayStopButton;

	private IAuxManager mAuxManager;
	private int mMuteState = 1;
	private Menu mMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		setHasOptionsMenu(true);
		mActivity = (BrowserActivity) getActivity();

		mAuxManager = mActivity.getIBluzManager().getAuxManager(null);
		mAuxManager.setOnAuxUIChangedListener(mOnAuxUIChangedListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView");
		mMainView = inflater.inflate(R.layout.fragment_linein, container, false);
		mPlayStopButton = (ImageButton) mMainView.findViewById(R.id.lineinPlayStopButton);
		mPlayStopButton.setOnClickListener(mOnClickListener);
		return mMainView;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateStateChanged(mMuteState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	private OnAuxUIChangedListener mOnAuxUIChangedListener = new OnAuxUIChangedListener() {

		@Override
		public void onStateChanged(int state) {
			updateStateChanged(state);
		}

	};

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.lineinPlayStopButton:
				mAuxManager.mute();
				break;
			default:
				break;
			}
		}

	};

	private void updateStateChanged(int state) {
		mMuteState = state;
		if (mMuteState == PlayState.PAUSED) {
			mPlayStopButton.setImageResource(R.drawable.selector_play_button);
		} else {
			mPlayStopButton.setImageResource(R.drawable.selector_pause_button);
		}
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		inflater.inflate(R.menu.soundsetting_menu, menu);
		mMenu = menu;
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		mActivity.menuItemSelected(mMenu, item.getItemId());
		return super.onOptionsItemSelected(item);
	}
}
