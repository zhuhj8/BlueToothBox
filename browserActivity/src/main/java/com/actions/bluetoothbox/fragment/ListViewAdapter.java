package com.actions.bluetoothbox.fragment;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.util.Utils;

public class ListViewAdapter extends BaseAdapter {

	// local music list
	private List<MusicData> mList;
	// get a LayoutInfalter object to import layout
	private LayoutInflater mInflater;
	// music play state
	private int mPlayState;
	// play position
	private int mPlayIndex;
	// which one is selected
	private int selectItem = -1;

	public ListViewAdapter(Context context, List<MusicData> list) {
		this.mInflater = LayoutInflater.from(context);
		this.mList = list;
	}

	/*
	 * set playindex and playstate
	 */
	public void setPlayState(int playindex, int playstate) {
		this.mPlayIndex = playindex;
		this.mPlayState = playstate;
		notifyDataSetChanged();
	}

	// update song list
	public void setListAdapter(List<MusicData> list) {
		this.mList = list;
		notifyDataSetChanged();
	}

	// get current playstate
	public int getPlayState() {
		return this.mPlayState;
	}

	// get current playindex
	public int getPlayIndex() {
		return this.mPlayIndex;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ListViewHolder holder;
		if (convertView == null) {
			// get listview_item layout
			convertView = mInflater.inflate(R.layout.listview_item, null);
			holder = new ListViewHolder();

			// music file name
			holder.sName = (TextView) convertView.findViewById(R.id.musicName);
			// music aritst
			holder.sArtist = (TextView) convertView.findViewById(R.id.musicAritst);
			// music total time
			holder.sTime = (TextView) convertView.findViewById(R.id.musicTime);
			// index
			/*
			 * holder.sPos = (TextView) convertView
			 * .findViewById(R.id.musiclistPos);
			 */
			convertView.setTag(holder);
		} else {
			// get ViewHolder object
			holder = (ListViewHolder) convertView.getTag();
			convertView.setBackgroundColor(Color.WHITE);
		}

		// set TextView's content
		holder.sName.setText((CharSequence) mList.get(position).getFileName());
		holder.sArtist.setText((CharSequence) mList.get(position).getMusicArtist());
		holder.sTime.setText((CharSequence) Utils.showTime(mList.get(position).getMusicDuration()));
		/*
		 * holder.sPos.setText((CharSequence) String.format("%d. ",
		 * mList.get(position).getMusicID()));
		 */
		if (position == selectItem) {
			convertView.setBackgroundColor(R.drawable.list_bg_selected);
			holder.sName.setSingleLine(true);
			holder.sName.setSelected(true);
			holder.sName.setEllipsize(TruncateAt.MARQUEE);
		} else {
			holder.sName.setEllipsize(TruncateAt.END);
		}
		return convertView;
	}

	public void setSelectItem(int selectItem) {
		this.selectItem = selectItem;
	}

}
