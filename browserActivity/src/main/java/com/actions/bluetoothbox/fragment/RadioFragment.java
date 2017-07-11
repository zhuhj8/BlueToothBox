package com.actions.bluetoothbox.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.app.BrowserActivity;
import com.actions.bluetoothbox.util.Preferences;
import com.actions.bluetoothbox.util.RadioDial;
import com.actions.bluetoothbox.util.RadioDial.OnChannelChangeListener;
import com.actions.ibluz.manager.BluzManagerData.OnManagerReadyListener;
import com.actions.ibluz.manager.BluzManagerData.OnRadioUIChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnScanCompletionListener;
import com.actions.ibluz.manager.BluzManagerData.PlayState;
import com.actions.ibluz.manager.BluzManagerData.RadioEntry;
import com.actions.ibluz.manager.IRadioManager;

public class RadioFragment extends SherlockFragment {
	private static final String TAG = "RadioFragment";
	private View mView;
	private IRadioManager mRadioManager;
	private BrowserActivity mActivity;

	private static List<Integer> mChannelSearchedList = new ArrayList<Integer>();
	private ArrayList<HashMap<String, String>> mStoreChannelArrayList;
	private RadioStoreChannelAdapter mRadioStoreChannelAdapter;
	private ActionMode mActionMode = null;

	private ProgressDialog mProgressDialog;
	private RadioDial mRadioDial;
	private ListView mStoreChannelListView;
	private ImageButton mSwitchImageButton;
	private ImageButton mSeekBackwardImageButton;
	private ImageButton mReduceChannelImageButton;
	private ImageButton mAddChannelImageButton;
	private ImageButton mSeekforwardImageButton;
	private TextView mStoreFMTextView;
	private View mAddStoreView;

	private int mStartSet;
	private int mEndSet;
	private int mStep;
	private int mBandnum = 0;
	private int mCurrentChannelNum;
	private static final String mPreFix[] = { "bd0_", "bd1_", "bd2_" };
	private static final float[] mCoefficient = { 0.1f, 0.001f };
	private String[] mBand;
	private final static DecimalFormat mDF[] = { new DecimalFormat("###.0"), new DecimalFormat("###.00") };
	private DecimalFormat mDecimalFormat = new DecimalFormat("###.0");
	private boolean mViewEnable = true;
	private boolean mScan = false;
	private Menu mMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		mActivity = (BrowserActivity) getActivity();
		mProgressDialog = new ProgressDialog(mActivity);
		mProgressDialog.setCanceledOnTouchOutside(true);
		mProgressDialog.setMessage(getText(R.string.notice_dialog_FM_preparation));
		mProgressDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mScan) {
					mRadioManager.cancelScan();
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView");
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_radio, container, false);
		mRadioManager = mActivity.getIBluzManager().getRadioManager(new OnManagerReadyListener() {
			@Override
			public void onReady() {
				reinstallChannelSearchedList();
				mRadioManager.setOnRadioUIChangedListener(mRadioUIChangedListener);
				setWidgetVisiable(true);
			}

		});
		init();
		return mView;
	}

	public void init() {
		mStoreChannelArrayList = new ArrayList<HashMap<String, String>>();
		mBand = new String[] { getResources().getString(R.string.radio_band_china_usa), getResources().getString(R.string.radio_band_japan),
				getResources().getString(R.string.radio_band_europe) };
		ImageButtonClickListener imagebuttonclicklistener = new ImageButtonClickListener();
		mReduceChannelImageButton = (ImageButton) mView.findViewById(R.id.btn_reduce);
		mReduceChannelImageButton.setOnClickListener(imagebuttonclicklistener);
		mAddChannelImageButton = (ImageButton) mView.findViewById(R.id.btn_add);
		mAddChannelImageButton.setOnClickListener(imagebuttonclicklistener);
		mSeekBackwardImageButton = (ImageButton) mView.findViewById(R.id.btn_seekbackward);
		mSeekBackwardImageButton.setOnClickListener(imagebuttonclicklistener);
		mSeekforwardImageButton = (ImageButton) mView.findViewById(R.id.btn_seekforward);
		mSeekforwardImageButton.setOnClickListener(imagebuttonclicklistener);

		mAddStoreView = mView.findViewById(R.id.storeView);
		mAddStoreView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				List<String> mlist = new ArrayList<String>();

				int sum = getPerferenceTotalChannelNumber(mBandnum);
				// if contain store channel,not add
				if (sum > 0) {
					for (int i = 1; i <= sum; i++) {
						String channel = Integer.toString((Integer) Preferences.getPreferences(getActivity(), mPreFix[mBandnum]
								+ Preferences.KEY_RADIO_CHANNEL_PREFIX + i, 0));
						mlist.add(channel);
					}
				}
				if (!mlist.contains(Integer.toString(mCurrentChannelNum))) {
					Preferences.setPreferences(mActivity, mPreFix[mBandnum] + Preferences.KEY_RADIO_CHANNEL_PREFIX + (sum + 1), mCurrentChannelNum);
					Preferences.setPreferences(mActivity, Preferences.KEY_RADIO_CHANNEL_NUM + String.valueOf(mBandnum), sum + 1);
				}
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("content", String.valueOf(mCurrentChannelNum));
				map.put("flag", "false");
				if (!mRadioStoreChannelAdapter.list.contains(map)) {
					mRadioStoreChannelAdapter.list.add(map);
					notifyDataChanged();
				}

			}
		});

		// initialize and sign current channel
		mCurrentChannelNum = mRadioManager.getCurrentChannel();
		mStoreFMTextView = (TextView) mView.findViewById(R.id.store);
		if (mCurrentChannelNum < mStartSet || mCurrentChannelNum > mEndSet) {
			mCurrentChannelNum = mStartSet;
		}

		mRadioDial = (RadioDial) mView.findViewById(R.id.radioDial);
		mRadioDial.setFrequence(mCurrentChannelNum);
		mRadioDial.setOnChannelListener(new OnChannelChangeListener() {

			@Override
			public void onStopTrackingTouch(RadioDial dial) {
				mCurrentChannelNum = dial.getFrequence();
				if (mCurrentChannelNum < mStartSet) {
					mCurrentChannelNum = mStartSet;
					mRadioDial.setFrequence(mCurrentChannelNum);
				} else if (mCurrentChannelNum > mEndSet) {
					mCurrentChannelNum = mEndSet;
					mRadioDial.setFrequence(mCurrentChannelNum);
				}
				if (mBandnum != RadioDial.RADIO_SYSTEM_EUROPE) {
					mCurrentChannelNum = mCurrentChannelNum / 100 * 100;
					mRadioDial.setFrequence(mCurrentChannelNum);
				}
				mStoreFMTextView.setText(String.valueOf(mDecimalFormat.format(mCurrentChannelNum * mCoefficient[1])) + "MHz");
				mRadioManager.select(mCurrentChannelNum);
			}

			@Override
			public void onStartTrackingTouch(RadioDial dial) {

			}

			@Override
			public void onChannelChanged(RadioDial dial, int frequence) {
				if (frequence < mStartSet) {
					frequence = mStartSet;
				} else if (frequence > mEndSet) {
					frequence = mEndSet;
				}
				mStoreFMTextView.setText(String.valueOf(mDecimalFormat.format(frequence * mCoefficient[1])) + "MHz");
				// mCurrentChannelNum = frequence;
			}
		});

		mSwitchImageButton = (ImageButton) mView.findViewById(R.id.radio_switch);
		mSwitchImageButton.setImageResource(R.drawable.selector_switchoff_button);
		mSwitchImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mRadioManager.switchMute();
			}
		});

		initListItemData();
		mStoreChannelListView = (ListView) mView.findViewById(R.id.radiostore_list);
		mRadioStoreChannelAdapter = new RadioStoreChannelAdapter(mStoreChannelArrayList);
		mStoreChannelListView.setAdapter(mRadioStoreChannelAdapter);
		mStoreChannelListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mActionMode == null) {
					mCurrentChannelNum = Integer.valueOf(mRadioStoreChannelAdapter.list.get(position).get("content").toString());
					mRadioManager.select(mCurrentChannelNum);
				} else {
					RadioStoreChannelAdapter.ViewHolder holder = (RadioStoreChannelAdapter.ViewHolder) view.getTag();
					holder.deleteTagCheckBox.toggle();
					if (holder.deleteTagCheckBox.isChecked() == true) {
						mStoreChannelArrayList.get(position).put("flag", "true");
					} else {
						mStoreChannelArrayList.get(position).put("flag", "false");
					}
				}
			}
		});

		mStoreChannelListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				if (mActionMode == null) {
					mActionMode = getSherlockActivity().startActionMode(new ActionMode.Callback() {
						@Override
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							Log.v(TAG, "onPrepareActionMode");
							return false;
						}

						@Override
						public void onDestroyActionMode(ActionMode mode) {
							Log.v(TAG, "onDestroyActionMode");
							mActionMode = null;
							notifyDataChanged();
						}

						@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							Log.v(TAG, "onCreateActionMode");
							MenuInflater inflater = mode.getMenuInflater();
							inflater.inflate(R.menu.listsetting4radio_menu, menu);
							notifyDataChanged();
							return true;
						}

						@Override
						public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
							Log.v(TAG, "onActionItemClicked");
							switch (item.getItemId()) {
							case R.id.action_delete:

								Iterator<HashMap<String, String>> iterator = mStoreChannelArrayList.iterator();
								// delete all
								if (getPerferenceTotalChannelNumber(mBandnum) > 0) {
									for (int i = 1; i <= getPerferenceTotalChannelNumber(mBandnum); i++) {
										Preferences.removePreferences(getActivity(), mPreFix[mBandnum] + Preferences.KEY_RADIO_CHANNEL_PREFIX + i);
									}
								}
								Preferences.setPreferences(mActivity, Preferences.KEY_RADIO_CHANNEL_NUM + String.valueOf(mBandnum), 0);

								// re-add
								int num = 1;
								while (iterator.hasNext()) {
									HashMap<String, String> temp = iterator.next();
									if (temp.get("flag").equals("false")) {
										Preferences.setPreferences(mActivity, mPreFix[mBandnum] + Preferences.KEY_RADIO_CHANNEL_PREFIX + num,
												Integer.valueOf(temp.get("content")));
										Preferences.setPreferences(mActivity, Preferences.KEY_RADIO_CHANNEL_NUM + String.valueOf(mBandnum), num);
										num++;
									} else {
										iterator.remove();
									}
								}

								notifyDataChanged();
								mode.finish(); // Action picked,
												// so close the
												// CAB
								return true;
							default:
								return true;
							}
						}
					});
					return true;
				}
				return true;
			}
		});
		setWidgetVisiable(false);
	}

	public void initListItemData() {
		mStoreChannelArrayList.clear();
		int perferenceTotalChannel = getPerferenceTotalChannelNumber(mBandnum);
		if (perferenceTotalChannel > 0) {
			for (int i = 1; i <= perferenceTotalChannel; i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				String channel = Integer.toString((Integer) Preferences.getPreferences(getActivity(), mPreFix[mBandnum] + Preferences.KEY_RADIO_CHANNEL_PREFIX
						+ i, 0));
				map.put("content", channel);
				map.put("flag", "false");
				mStoreChannelArrayList.add(map);
			}
		}
	}

	private void reinstallChannelSearchedList() {
		mChannelSearchedList.clear();
		for (RadioEntry re : mRadioManager.getList()) {
			if (!mChannelSearchedList.contains(re.channel)) {
				mChannelSearchedList.add(re.channel);
			}
		}
		// Sort
		Collections.sort(mChannelSearchedList);
		notifyDataChanged();
	}

	private class RadioStoreChannelAdapter extends BaseAdapter {
		private ArrayList<HashMap<String, String>> list;

		public RadioStoreChannelAdapter(ArrayList<HashMap<String, String>> list) {
			this.list = list;
		}

		@Override
		public Object getItem(int position) {

			return list.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public int getCount() {

			return list.size();
		}

		final class ViewHolder {
			TextView storedChannelTextView;
			CheckBox deleteTagCheckBox;
		}

		/**
		 * ListView Item
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.radiostore_item, null);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.storedChannelTextView = (TextView) convertView.findViewById(R.id.stored_name);
			holder.deleteTagCheckBox = (CheckBox) convertView.findViewById(R.id.stored_select);
			if (mActionMode != null) {
				holder.deleteTagCheckBox.setVisibility(View.VISIBLE);
			} else {
				holder.deleteTagCheckBox.setVisibility(View.GONE);
			}
			convertView.setTag(holder);
			int mChannel = Integer.valueOf(list.get(position).get("content").toString());
			holder.storedChannelTextView.setText("FM " + String.valueOf(mDecimalFormat.format((mChannel) * mCoefficient[1])) + "MHz");
			holder.deleteTagCheckBox.setChecked(list.get(position).get("flag").equals("true"));
			return convertView;
		}

	}

	/** ImageButton listener */
	class ImageButtonClickListener implements OnClickListener {
		public void onClick(View v) {
			mCurrentChannelNum = mRadioManager.getCurrentChannel();
			int total = mChannelSearchedList.size();
			switch (v.getId()) {
			case R.id.btn_seekbackward:
				if (mChannelSearchedList.size() != 0) {
					if (mCurrentChannelNum < mChannelSearchedList.get(0)) {
						mCurrentChannelNum = mChannelSearchedList.get(total - 1);
						setChannelChanged();
						return;
					} else {
						for (Integer channel : mChannelSearchedList) {
							int index = mChannelSearchedList.indexOf(channel);
							if (mCurrentChannelNum == mChannelSearchedList.get(0)) {
								mCurrentChannelNum = mChannelSearchedList.get(total - 1);
								setChannelChanged();
								return;
							} else if (mCurrentChannelNum == channel && index != 0) {
								mCurrentChannelNum = mChannelSearchedList.get(index - 1);
								setChannelChanged();
								return;
							} else if (mCurrentChannelNum > mChannelSearchedList.get(total - 1)) {
								mCurrentChannelNum = mChannelSearchedList.get(total - 1);
								setChannelChanged();
								return;
							} else if (mCurrentChannelNum < mChannelSearchedList.get(index + 1) && mCurrentChannelNum >= channel) {
								mCurrentChannelNum = channel;
								setChannelChanged();
								return;
							}
						}
					}
				}
				break;
			case R.id.btn_reduce:
				if (mCurrentChannelNum == mStartSet) {
					return;
				} else {
					mCurrentChannelNum = mCurrentChannelNum - mStep;
					setChannelChanged();
				}
				break;
			case R.id.btn_add:
				if (mCurrentChannelNum == mEndSet) {
					return;
				} else {
					mCurrentChannelNum = mCurrentChannelNum + mStep;
					setChannelChanged();
				}
				break;
			case R.id.btn_seekforward:
				if (mChannelSearchedList.size() != 0) {
					if (mCurrentChannelNum > mChannelSearchedList.get(mChannelSearchedList.size() - 1)) {
						mCurrentChannelNum = mChannelSearchedList.get(0);
						setChannelChanged();
						return;
					} else {
						for (Integer channel : mChannelSearchedList) {
							int index = mChannelSearchedList.indexOf(channel);
							if (mCurrentChannelNum == mChannelSearchedList.get(total - 1)) {
								mCurrentChannelNum = mChannelSearchedList.get(0);
								setChannelChanged();
								return;
							} else if (mCurrentChannelNum < mChannelSearchedList.get(0)) {
								mCurrentChannelNum = mChannelSearchedList.get(0);
								setChannelChanged();
								return;
							} else if (mCurrentChannelNum == channel && index != 0) {
								mCurrentChannelNum = mChannelSearchedList.get(index + 1);
								setChannelChanged();
								return;
							} else if (mCurrentChannelNum < mChannelSearchedList.get(index + 1) && mCurrentChannelNum >= channel) {
								mCurrentChannelNum = mChannelSearchedList.get(index + 1);
								setChannelChanged();
								return;
							}
						}
					}
				}
				break;
			default:
				break;
			}
		}
	}

	private OnRadioUIChangedListener mRadioUIChangedListener = new OnRadioUIChangedListener() {
		@Override
		public void onStateChanged(int arg0) {
			switch (arg0) {
			case PlayState.UNKNOWN:
				break;
			case PlayState.PLAYING:
				mSwitchImageButton.setImageResource(R.drawable.selector_switchoff_button);
				mViewEnable = true;
				setWidgetVisiable(mViewEnable);
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mScan = false;
					mProgressDialog.dismiss();
				}
				break;
			case PlayState.PAUSED:
				mSwitchImageButton.setImageResource(R.drawable.selector_switchon_button);
				mViewEnable = false;
				setWidgetVisiable(mViewEnable);
				break;
			case PlayState.WAITING:
				mScan = true;
				if (mProgressDialog != null && !mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
				break;
			default:
				break;
			}

		}

		@Override
		public void onChannelChanged(int arg0) {
			mCurrentChannelNum = arg0;
			mRadioDial.setFrequence(arg0);
			mStoreFMTextView.setText(String.valueOf(mDecimalFormat.format(arg0 * mCoefficient[1])) + "MHz");

		}

		@Override
		public void onBandChanged(int arg0) {
			mBandnum = arg0;
			// Band save in APP
			Preferences.setPreferences(mActivity, Preferences.KEY_RADIO_BAND_SIGN, arg0);
			switch (mBandnum) {
			// China-USA
			case 0:
				mRadioDial.setRadioSystem(RadioDial.RADIO_SYSTEM_CN_US);
				mStartSet = 87500;
				mEndSet = 108000;
				mStep = 100;
				mDecimalFormat = mDF[0];
				break;
			// Japan
			case 1:
				mRadioDial.setRadioSystem(RadioDial.RADIO_SYSTEM_JAPAN);
				mStartSet = 76000;
				mEndSet = 90000;
				mStep = 100;
				mDecimalFormat = mDF[0];
				break;
			// Europe
			case 2:
				mRadioDial.setRadioSystem(RadioDial.RADIO_SYSTEM_EUROPE);
				mStartSet = 87500;
				mEndSet = 108000;
				mStep = 50;
				mDecimalFormat = mDF[1];
				break;
			default:
				break;

			}
			reinstallChannelSearchedList();
			initListItemData();
			mRadioStoreChannelAdapter.notifyDataSetChanged();
		}
	};

	private OnScanCompletionListener mScanCompletionListener = new OnScanCompletionListener() {

		@Override
		public void onCompletion(List<RadioEntry> list) {
			mChannelSearchedList.clear();
			for (RadioEntry re : list) {
				if (!mChannelSearchedList.contains(re.channel)) {
					mChannelSearchedList.add(re.channel);
				}
			}
			// Sort
			Collections.sort(mChannelSearchedList);
			mScan = false;
			mProgressDialog.cancel();
		}
	};

	private void setWidgetVisiable(boolean enable) {
		mRadioDial.setEnabled(enable);
		mAddStoreView.setEnabled(enable);
		mStoreChannelListView.setEnabled(enable);
		mSeekBackwardImageButton.setEnabled(enable);
		mReduceChannelImageButton.setEnabled(enable);
		mAddChannelImageButton.setEnabled(enable);
		mSeekforwardImageButton.setEnabled(enable);
		mStoreFMTextView.setEnabled(enable);
	}

	public void showBandSelectDialog() {
		Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.action_band_settings);
		builder.setSingleChoiceItems(mBand, mBandnum, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mBandnum = which;
				mRadioManager.setBand(mBandnum);
				dialog.dismiss();

			}
		});
		builder.create().show();
	}

	private int getPerferenceTotalChannelNumber(int bandnum) {
		return (Integer) Preferences.getPreferences(getActivity(), Preferences.KEY_RADIO_CHANNEL_NUM + String.valueOf(bandnum), 0);
	}

	private void notifyDataChanged() {
		mRadioStoreChannelAdapter.notifyDataSetChanged();
	}

	private void setChannelChanged() {
		mRadioManager.select(mCurrentChannelNum);
	}

	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.v(TAG, "onPause");
		if (mActionMode != null) {
			mActionMode.finish();
			// this.onOptionsMenuClosed(null);
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.v(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu, inflater);
		mMenu = menu;
		inflater.inflate(R.menu.radio_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			if (mViewEnable) {
				mProgressDialog.show();
				mScan = true;
				mRadioManager.scan(mScanCompletionListener);
			}
			break;
		case R.id.bandsetting:
			if (mViewEnable) {
				showBandSelectDialog();
			}
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
}
