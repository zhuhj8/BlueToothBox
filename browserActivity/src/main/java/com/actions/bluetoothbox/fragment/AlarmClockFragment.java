package com.actions.bluetoothbox.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.app.BrowserActivity;
import com.actions.bluetoothbox.util.Constant.IPCKey;
import com.actions.bluetoothbox.util.Utils;
import com.actions.ibluz.manager.BluzManagerData.AlarmEntry;
import com.actions.ibluz.manager.BluzManagerData.OnAlarmUIChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnManagerReadyListener;
import com.actions.ibluz.manager.IAlarmManager;

public class AlarmClockFragment extends SherlockFragment {
	private static final String TAG = "AlarmClockFragment";

	private BrowserActivity mActivity;
	private View mView;
	private ListView mAlarmClockListView;
	private IAlarmManager mAlarmManager;
	private AlarmClockAdapter mAlarmClockAdapter;
	private ArrayList<AlarmClockNode> mAlarmEntriesList;
	private ActionMode mActionMode = null;
	private Menu mMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView");
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_alarmclock, container, false);
		mActivity = (BrowserActivity) getActivity();
		mAlarmManager = mActivity.getIBluzManager().getAlarmManager(new OnManagerReadyListener() {

			@Override
			public void onReady() {
				refreshAlarmEntries();
				notifyDataChanged();
			}

		});

		mAlarmManager.setOnAlarmUIChangedListener(new OnAlarmUIChangedListener() {

			@Override
			public void onStateChanged(int state) {
				if (state == 1) {
					mActivity.showAlarmDialog(createAlarmDialog());
				} else {
					mActivity.dismissAlarmDialog();
				}
			}

		});

		mAlarmEntriesList = new ArrayList<AlarmClockNode>();
		init();
		return mView;
	}

	private AlertDialog createAlarmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.alarmclock);
		builder.setPositiveButton(R.string.alarmclock_snooze, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mAlarmManager.snooze();
				dialog.dismiss();
				Toast.makeText(mActivity, R.string.alarmclock_snoozing, Toast.LENGTH_LONG).show();
			}
		});
		builder.setNegativeButton(R.string.alarmclock_turnoff, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mAlarmManager.off();
				dialog.dismiss();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mAlarmManager.off();
			}
		});
		AlertDialog dg = builder.create();
		return dg;
	}

	@Override
	public void onPause() {
		Log.v(TAG, "onPause!");
		super.onPause();
		if (mActionMode != null) {
			mActionMode.finish();
		}
	}

	public void init() {
		final View addAlarmView = mView.findViewById(R.id.add_alarm);
		addAlarmView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mAlarmEntriesList.size() < 4) {
					changeToAlarmSetting(creatNewAlarmEntry());
				} else {
					Utils.displayToast(R.string.alarmclock_too_many_entries);
				}
			}
		});
		// refreshAlarmEntries();
		mAlarmClockListView = (ListView) mView.findViewById(R.id.alarms_list);
		mAlarmClockAdapter = new AlarmClockAdapter(mAlarmEntriesList, mActivity);

		mAlarmClockListView.setAdapter(mAlarmClockAdapter);
		mAlarmClockListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AlarmClockAdapter.ViewHolder holder = (AlarmClockAdapter.ViewHolder) view.getTag();
				holder.onOff.toggle();

				if (mActionMode != null) {
					if (holder.onOff.isChecked()) {
						mAlarmEntriesList.get(position).setFlag(true);
					} else {
						mAlarmEntriesList.get(position).setFlag(false);
					}
				} else {
					changeToAlarmSetting(mAlarmEntriesList.get(position).getAlarmEntry());
				}
			}

		});
		mAlarmClockListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (mActionMode == null) {
					mActionMode = getSherlockActivity().startActionMode(new ActionMode.Callback() {

						@Override
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							// TODO Auto-generated method stub
							return false;
						}

						@Override
						public void onDestroyActionMode(ActionMode mode) {
							mActionMode = null;
							notifyDataChanged();
						}

						@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							MenuInflater inflater = mode.getMenuInflater();
							inflater.inflate(R.menu.listsetting4alarmclock_menu, menu);
							notifyDataChanged();
							return true;
						}

						@Override
						public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
							switch (item.getItemId()) {
							case R.id.action_delete:
								List<AlarmEntry> tempList = new ArrayList<AlarmEntry>();
								ArrayList<AlarmClockNode> tempNode = new ArrayList<AlarmClockNode>();
								for (int i = 0; i < mAlarmEntriesList.size(); i++) {
									if (mAlarmEntriesList.get(i).getFlag()) {
										tempList.add(mAlarmEntriesList.get(i).getAlarmEntry());
										tempNode.add(mAlarmEntriesList.get(i));
									}
								}
								mAlarmEntriesList.removeAll(tempNode);
								for (int j = 0; j < tempList.size(); j++) {
									mAlarmManager.remove(tempList.get(j));
								}

								notifyDataChanged();
								mode.finish();
								return true;
							default:
								return true;
							}
						}
					});
				}
				return true;
			}
		});
	}

	public void refreshAlarmEntries() {
		mAlarmEntriesList.clear();
		for (AlarmEntry entry : mAlarmManager.getList()) {
			final AlarmClockNode alarmClockNode = new AlarmClockNode(entry, false);
			mAlarmEntriesList.add(alarmClockNode);
		}
	}

	private class AlarmClockAdapter extends BaseAdapter {
		public ArrayList<AlarmClockNode> list;

		public AlarmClockAdapter(ArrayList<AlarmClockNode> list, Context context) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		final class ViewHolder {
			TextView time;
			TextView description;
			TextView repeat;
			CheckBox onOff;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.alarmclock_item, null);

				holder = new ViewHolder();
				holder.time = (TextView) convertView.findViewById(R.id.alarmTimeText);
				holder.description = (TextView) convertView.findViewById(R.id.alarmDescriptionText);
				holder.repeat = (TextView) convertView.findViewById(R.id.alarmRepeatText);
				holder.onOff = (CheckBox) convertView.findViewById(R.id.alarmSwitchBox);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			AlarmClockNode clock = list.get(position);
			AlarmEntry entry = clock.alarmEntry;
			if (mActionMode != null) {
				holder.onOff.setButtonDrawable(R.drawable.checkbox_switch_style);
				holder.onOff.setChecked(list.get(position).flag);
			} else {
				holder.onOff.setButtonDrawable(R.drawable.alarm_on_off_style);
				holder.onOff.setChecked(list.get(position).getAlarmEntry().state);
			}

			holder.onOff.setTag(clock);
			holder.onOff.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlarmClockNode clock = (AlarmClockNode) v.getTag();
					CheckBox checkbox = (CheckBox) v;
					if (mActionMode != null) {
						if (checkbox.isChecked()) {
							clock.setFlag(true);
						} else {
							clock.setFlag(false);
						}
					} else {
						AlarmEntry entry = clock.getAlarmEntry();
						entry.state = checkbox.isChecked();
						mAlarmManager.set(entry);
					}
				}
			});

			String sunday = getResources().getString(R.string.sun);
			String monday = getResources().getString(R.string.mon);
			String tuesday = getResources().getString(R.string.tue);
			String wednesday = getResources().getString(R.string.wed);
			String thursday = getResources().getString(R.string.thu);
			String friday = getResources().getString(R.string.fri);
			String saturday = getResources().getString(R.string.sat);

			String repeat = "";
			if (entry.repeat[0])
				repeat = (sunday + "  ");
			if (entry.repeat[1])
				repeat += (monday + "  ");
			if (entry.repeat[2])
				repeat += (tuesday + "  ");
			if (entry.repeat[3])
				repeat += (wednesday + "  ");
			if (entry.repeat[4])
				repeat += (thursday + "  ");
			if (entry.repeat[5])
				repeat += (friday + "  ");
			if (entry.repeat[6])
				repeat += saturday;
			if (!repeat.equalsIgnoreCase("")) {
				holder.repeat.setText(repeat);
				holder.repeat.setTextColor(Color.rgb(0x33, 0xB5, 0xE5));
			} else {
				String def = getResources().getString(R.string.alarmclock_repeat_default);
				holder.repeat.setText(def);
				holder.repeat.setTextColor(Color.rgb(0x1F, 0x1F, 0x1F));
			}

			String format = String.format("%02d:%02d", entry.hour, entry.minute);
			holder.time.setText(format);

			if (entry.title != null && !entry.title.equalsIgnoreCase("")) {
				holder.description.setText(entry.title);
			} else {
				String def = getResources().getString(R.string.alarmclock_description_hint);
				holder.description.setText(def);
			}

			convertView.setTag(holder);
			return convertView;
		}
	}

	private void notifyDataChanged() {
		mAlarmClockAdapter.notifyDataSetChanged();
	}

	private AlarmEntry creatNewAlarmEntry() {
		boolean isHolded;
		int index = 1;
		for (int i = 1; i < 256; i++) {
			isHolded = false;
			for (AlarmEntry entry : mAlarmManager.getList()) {
				if (entry.index == i) {
					isHolded = true;
					break;
				}
			}

			if (!isHolded) {
				index = i;
				break;
			}
		}
		AlarmEntry entry = new AlarmEntry();
		entry.index = index;
		entry.ringType = 0;
		Calendar calendar = Calendar.getInstance();
		entry.hour = calendar.get(Calendar.HOUR_OF_DAY);
		entry.minute = calendar.get(Calendar.MINUTE);
		return entry;
	}

	private void changeToAlarmSetting(AlarmEntry entry) {
		AlarmClockSettingFragment fragment = new AlarmClockSettingFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(IPCKey.ALARM_ENTRY, entry);
		fragment.setArguments(bundle);
		mActivity.replaceFragment(fragment, SlideoutMenuFragment.FRAGMENT_TAG_ALARM);
	}

	private class AlarmClockNode {
		public AlarmEntry alarmEntry;
		public Boolean flag;

		AlarmClockNode(AlarmEntry alarmEntry, Boolean flag) {
			this.alarmEntry = alarmEntry;
			this.flag = flag;
		}

		public AlarmEntry getAlarmEntry() {
			return alarmEntry;
		}

		public Boolean getFlag() {
			return flag;
		}

		public void setFlag(Boolean flag) {
			this.flag = flag;
		}

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
