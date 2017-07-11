package com.actions.bluetoothbox.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.app.BrowserActivity;
import com.actions.bluetoothbox.util.Constant.IPCKey;
import com.actions.bluetoothbox.util.Preferences;
import com.actions.ibluz.manager.BluzManagerData.AlarmEntry;
import com.actions.ibluz.manager.BluzManagerData.FolderEntry;
import com.actions.ibluz.manager.BluzManagerData.RingEntry;
import com.actions.ibluz.manager.BluzManagerData.RingSource;
import com.actions.ibluz.manager.IAlarmManager;

public class AlarmClockSettingFragment extends SherlockFragment {
	private static final String TAG = "AlarmClockSettingFragment";

	private BrowserActivity mActivity;
	private View mView;
	private IAlarmManager mAlarmManager;

	private AlarmEntry mAlarmEntry;
	private TimePicker mTimePicker;
	private EditText mTitleEditText;

	private CheckBox[] mRepeatButtons = new CheckBox[7];
	private Spinner mAlarmTypeSpinner;
	private Spinner mAlarmMusicSpinner;
	private List<String> mAlarmType = new ArrayList<String>();
	private List<RingEntry> mUhostEntries = new ArrayList<RingEntry>();
	private List<RingEntry> mCardEntries = new ArrayList<RingEntry>();
	private List<RingEntry> mInternalEntries = new ArrayList<RingEntry>();
	private List<RingEntry> mRadioEntries = new ArrayList<RingEntry>();
	private List<RingEntry> mCurrentEntries;

	private ArrayAdapter<String> mAlarmTypeSpinnerAdapter;
	private ArrayAdapter<String> mAlarmMusicSpinnerAdapter;

	private Button mCommitButton;
	private Button mCancelButton;

	private static final String mPreFix[] = { "bd0_", "bd1_", "bd2_" };
	private List<FolderEntry> mFolderEntryList = new ArrayList<FolderEntry>();
	private String[] mFolderType;
	private List<RingEntry> mEXTERNAL1Entries = new ArrayList<RingEntry>();
	private List<RingEntry> mEXTERNAL2Entries = new ArrayList<RingEntry>();
	private List<RingEntry> mEXTERNAL3Entries = new ArrayList<RingEntry>();
	private List<RingEntry> mEXTERNAL4Entries = new ArrayList<RingEntry>();
	private HashMap<Integer, List<RingEntry>> mEXTERNALMap = new HashMap<Integer, List<RingEntry>>();
	private Menu mMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		if (bundle != null) {
			mAlarmEntry = bundle.getParcelable(IPCKey.ALARM_ENTRY);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.alarmclock_setting, container, false);
		mActivity = (BrowserActivity) getActivity();
		mAlarmManager = mActivity.getIBluzManager().getAlarmManager(null);
		init();
		return mView;
	}

	public void init() {
		mTimePicker = (TimePicker) mView.findViewById(R.id.alarmclock_time);
		mTitleEditText = (EditText) mView.findViewById(R.id.alarmclock_edittext);
		mRepeatButtons[0] = (CheckBox) mView.findViewById(R.id.Sunday);
		mRepeatButtons[1] = (CheckBox) mView.findViewById(R.id.Monday);
		mRepeatButtons[2] = (CheckBox) mView.findViewById(R.id.Tuesday);
		mRepeatButtons[3] = (CheckBox) mView.findViewById(R.id.Wednesday);
		mRepeatButtons[4] = (CheckBox) mView.findViewById(R.id.Thursday);
		mRepeatButtons[5] = (CheckBox) mView.findViewById(R.id.Friday);
		mRepeatButtons[6] = (CheckBox) mView.findViewById(R.id.Saturday);

		mAlarmTypeSpinner = (Spinner) mView.findViewById(R.id.alarmclock_type);
		mAlarmMusicSpinner = (Spinner) mView.findViewById(R.id.alarmclock_music);

		mUhostEntries.clear();
		mCardEntries.clear();
		mEXTERNAL1Entries.clear();
		mEXTERNAL2Entries.clear();
		mEXTERNAL3Entries.clear();
		mEXTERNAL4Entries.clear();
		mEXTERNALMap.clear();
		for (RingEntry entry : mAlarmManager.getRingList()) {
			switch (entry.source) {
			case RingSource.UHOST:
				mUhostEntries.add(entry);
				break;
			case RingSource.CARD:
				mCardEntries.add(entry);
				break;
			case RingSource.INTERNAL:
				mInternalEntries.add(entry);
				break;
			case RingSource.EXTERNAL1:
				mEXTERNAL1Entries.add(entry);
				break;
			case RingSource.EXTERNAL2:
				mEXTERNAL2Entries.add(entry);
				break;
			case RingSource.EXTERNAL3:
				mEXTERNAL3Entries.add(entry);
				break;
			case RingSource.EXTERNAL4:
				mEXTERNAL4Entries.add(entry);
				break;
			default:
				break;
			}
		}
		mEXTERNALMap.put(0, mEXTERNAL1Entries);
		mEXTERNALMap.put(1, mEXTERNAL2Entries);
		mEXTERNALMap.put(2, mEXTERNAL3Entries);
		mEXTERNALMap.put(3, mEXTERNAL4Entries);
		final List<String> typeString = new ArrayList<String>();
		typeString.add(getResources().getString(R.string.alarmclock_uhost));
		typeString.add(getResources().getString(R.string.alarmclock_cardsong));
		typeString.add(getResources().getString(R.string.alarmclock_fm));
		typeString.add(getResources().getString(R.string.alarmclock_bell));

		if (mInternalEntries.size() > 0) {
			mAlarmType.add(typeString.get(3));
		}
		mFolderEntryList = mAlarmManager.getRingFolderList();
		if (mFolderEntryList.size() > 0) {
			mFolderType = new String[mFolderEntryList.size()];
			for (int i = 0; i < mFolderEntryList.size(); i++) {
				mFolderType[i] = mFolderEntryList.get(i).name;
				typeString.add(mFolderEntryList.get(i).name);
				if (!mEXTERNALMap.get(i).isEmpty()) {
					mAlarmType.add(mFolderEntryList.get(i).name);
				}
			}
		} else {
			mFolderType = new String[] { "", "", "", "" };
		}
		if (mCardEntries.size() > 0) {
			mAlarmType.add(typeString.get(1));
		}
		if (mUhostEntries.size() > 0) {
			mAlarmType.add(typeString.get(0));
		}
		int mRadioBand = (Integer) Preferences.getPreferences(mActivity, Preferences.KEY_RADIO_BAND_SIGN, 0);
		int perferenceTotalChannel = getPerferenceTotalChannelNumber(mRadioBand);
		if (perferenceTotalChannel > 0) {
			mAlarmType.add(typeString.get(2));
			for (int i = 1; i <= perferenceTotalChannel; i++) {
				int channel = (Integer) Preferences.getPreferences(getActivity(), mPreFix[mRadioBand] + Preferences.KEY_RADIO_CHANNEL_PREFIX + i, 0);
				RingEntry entry = new RingEntry();
				entry.id = channel;
				entry.source = RingSource.FM;
				entry.name = String.valueOf(((float) channel) / 1000);
				mRadioEntries.add(entry);
			}
		}
		mAlarmTypeSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mAlarmType);
		mAlarmTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAlarmTypeSpinner.setAdapter(mAlarmTypeSpinnerAdapter);
		mAlarmTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Spinner spinner = (Spinner) parent;
				String type = (String) spinner.getItemAtPosition(position);

				if (type.equalsIgnoreCase(typeString.get(0))) {
					mAlarmEntry.ringType = RingSource.UHOST;
					mCurrentEntries = mUhostEntries;
				} else if (type.equalsIgnoreCase(typeString.get(1))) {
					mAlarmEntry.ringType = RingSource.CARD;
					mCurrentEntries = mCardEntries;
				} else if (type.equalsIgnoreCase(typeString.get(3))) {
					mAlarmEntry.ringType = RingSource.INTERNAL;
					mCurrentEntries = mInternalEntries;
				} else if (type.equalsIgnoreCase(mFolderType[0])) {
					mAlarmEntry.ringType = RingSource.EXTERNAL1;
					mCurrentEntries = mEXTERNAL1Entries;
				} else if (type.equalsIgnoreCase(mFolderType[1])) {
					mAlarmEntry.ringType = RingSource.EXTERNAL2;
					mCurrentEntries = mEXTERNAL2Entries;
				} else if (type.equalsIgnoreCase(mFolderType[2])) {
					mAlarmEntry.ringType = RingSource.EXTERNAL3;
					mCurrentEntries = mEXTERNAL3Entries;
				} else if (type.equalsIgnoreCase(mFolderType[3])) {
					mAlarmEntry.ringType = RingSource.EXTERNAL4;
					mCurrentEntries = mEXTERNAL4Entries;
				} else {
					mAlarmEntry.ringType = RingSource.FM;
					mCurrentEntries = mRadioEntries;
				}

				String[] mRingNames = new String[mCurrentEntries.size()];
				for (int i = 0; i < mCurrentEntries.size(); i++) {
					mRingNames[i] = mCurrentEntries.get(i).name;
					if (mAlarmEntry.ringType == RingSource.FM) {
						mRingNames[i] = mRingNames[i] + "MHz";
					}
				}
				mAlarmMusicSpinnerAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, mRingNames);
				mAlarmMusicSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				mAlarmMusicSpinner.setAdapter(mAlarmMusicSpinnerAdapter);
				int selection = 0;
				for (int i = 0; i < mCurrentEntries.size(); i++) {
					if (mAlarmEntry.ringType == RingSource.FM) {
						if (mAlarmEntry.ringId == mCurrentEntries.get(i).id) {
							selection = i;
							break;
						}
					} else {
						if (mAlarmEntry.ringId == mCurrentEntries.get(i).id) {
							selection = i;
							break;
						}
					}
				}
				mAlarmMusicSpinner.setSelection(selection);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		String ringType = typeString.get(mAlarmEntry.ringType);
		int position = 0;
		for (int i = 0; i < mAlarmType.size(); i++) {
			if (mAlarmType.get(i).equalsIgnoreCase(ringType)) {
				position = i;
				break;
			}
		}
		mAlarmTypeSpinner.setSelection(position);

		mCommitButton = (Button) mView.findViewById(R.id.commit);
		mCancelButton = (Button) mView.findViewById(R.id.cancel);

		mTimePicker.setCurrentHour(mAlarmEntry.hour);
		mTimePicker.setCurrentMinute(mAlarmEntry.minute);
		mTitleEditText.setText(mAlarmEntry.title);
		for (int i = 0; i < mAlarmEntry.repeat.length; i++) {
			mRepeatButtons[i].setSelected(mAlarmEntry.repeat[i]);
			mRepeatButtons[i].setChecked(mAlarmEntry.repeat[i]);
		}

		mCommitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < mRepeatButtons.length; i++) {
					mAlarmEntry.repeat[i] = mRepeatButtons[i].isChecked();
				}

				mAlarmEntry.title = mTitleEditText.getText().toString();
				mAlarmEntry.hour = mTimePicker.getCurrentHour();
				mAlarmEntry.minute = mTimePicker.getCurrentMinute();
				RingEntry entry = mCurrentEntries.get(mAlarmMusicSpinner.getSelectedItemPosition());
				if (mAlarmEntry.ringType == RingSource.FM) {
					float channel = Float.parseFloat(entry.name);
					mAlarmEntry.ringId = (int) (channel * 1000);
				} else {
					mAlarmEntry.ringId = entry.id;
				}
				mAlarmEntry.state = true;
				mAlarmManager.set(mAlarmEntry);

				switchFragment();
			}
		});

		mCancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switchFragment();
			}
		});

		Log.v(TAG, "init finish!");

	}

	private void switchFragment() {
		AlarmClockFragment fragment = new AlarmClockFragment();
		mActivity.replaceFragment(fragment, SlideoutMenuFragment.FRAGMENT_TAG_ALARM);
	}

	private int getPerferenceTotalChannelNumber(int bandnum) {
		return (Integer) Preferences.getPreferences(getActivity(), Preferences.KEY_RADIO_CHANNEL_NUM + String.valueOf(bandnum), 0);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		mMenu = menu;
		inflater.inflate(R.menu.soundsetting_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		mActivity.menuItemSelected(mMenu, item.getItemId());
		return super.onOptionsItemSelected(item);
	}
}
