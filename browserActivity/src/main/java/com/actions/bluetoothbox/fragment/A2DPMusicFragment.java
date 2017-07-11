package com.actions.bluetoothbox.fragment;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.app.BrowserActivity;
import com.actions.bluetoothbox.util.Constant;
import com.actions.bluetoothbox.util.Preferences;
import com.actions.bluetoothbox.util.Utils;

public class A2DPMusicFragment extends SherlockFragment {
	private static final String LOGTAG = "A2DPMusicFragment";
	private static final int AUTO_UPDATE = 1;
	private static final int FADEDOWN = 2;
	private static final int FADEUP = 3;

	private BrowserActivity mActivity;
	private ListView mListView;
	private static ListViewAdapter mListViewAdapter;
	private int mMusicListLength = 0;
	private String mCurrentName = null;
	private boolean mResmueFlag = false;
	private MediaPlayer mMediaPlayer;
	private View mView;
	private ImageButton mImgBtnPlayAndPause;
	private ImageButton mImgBtnPlayNext;
	private ImageButton mImgBtnPlayPre;
	private SeekBar mSeekBar;
	private TextView mTxtCurrPlayTime;
	private TextView mTxtPlayTotalTime;
	private TextView mTxtPlayName;
	private TextView mTxtAuthorName;
	private Timer mTimer = new Timer();
	private static MessageHandler messageHandler;
	private Cursor cur;
	private Menu mMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(LOGTAG, "onCreate");
		mActivity = (BrowserActivity) getActivity();
		mActivity.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, mContentObserver);
	}

	private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Log.i(LOGTAG, "ContentObserver onChange");
			if (mActivity != null && mActivity.mMediaFree) {
				updateList();
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(LOGTAG, "onCreateView");
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_a2dpmusic, container, false);
		try {
			getMusicList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mListView = (ListView) mView.findViewById(R.id.music_list);
		mListViewAdapter = new ListViewAdapter(this.getActivity(), Constant.MusicPlayData.myMusicList);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//
		Constant.MusicPlayData.CURRENT_PLAY_INDEX = (Integer) (Preferences.getPreferences(mActivity, "songId", 0));
		Constant.MusicPlayData.CURRENT_PLAY_POSITION = (Integer) (Preferences.getPreferences(mActivity, "currentTime", 0));
		Constant.MusicPlayData.TOTAL_TIME = (Integer) (Preferences.getPreferences(mActivity, "totalTime", 0));
		String playName = (String) Preferences.getPreferences(mActivity, "songName", "song");
		if (Constant.MusicPlayData.myMusicList.size() > 0) {
			for (int i = 0; i < Constant.MusicPlayData.myMusicList.size(); i++) {
				if (Constant.MusicPlayData.myMusicList.get(i).getFileName().equalsIgnoreCase(playName)) {
					Constant.MusicPlayData.CURRENT_PLAY_INDEX = i;
					break;
				}
			}
			if (Constant.MusicPlayData.myMusicList.size() > Constant.MusicPlayData.CURRENT_PLAY_INDEX) {
				if (!Constant.MusicPlayData.myMusicList.get(Constant.MusicPlayData.CURRENT_PLAY_INDEX).getFileName().equalsIgnoreCase(playName)) {
					Constant.MusicPlayData.CURRENT_PLAY_INDEX = 0;
					Constant.MusicPlayData.CURRENT_PLAY_POSITION = 0;
					Constant.MusicPlayData.TOTAL_TIME = 0;
				}
			} else {
				Constant.MusicPlayData.CURRENT_PLAY_INDEX = 0;
				Constant.MusicPlayData.CURRENT_PLAY_POSITION = 0;
				Constant.MusicPlayData.TOTAL_TIME = 0;
			}
		}
		mListView.setAdapter(mListViewAdapter);
		initUIManager();
		messageHandler = new MessageHandler();
		setTimerTask();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Constant.MusicPlayData.IS_PLAY_NEW = true;
				Constant.MusicPlayData.CURRENT_PLAY_POSITION = 0;
				Constant.MusicPlayData.CURRENT_PLAY_INDEX = position;
				mCurrentName = Constant.MusicPlayData.myMusicList.get(Constant.MusicPlayData.CURRENT_PLAY_INDEX).getFileName();
				playMusic(false);
			}
		});
		return mView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(LOGTAG, "onDestroy ()");
		messageHandler.removeMessages(AUTO_UPDATE);
		mActivity.getContentResolver().unregisterContentObserver(mContentObserver);
		release();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v(LOGTAG, "onDestroyView");
		getPlayerInfo();
	}

	public boolean isPlaying() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				return true;
			}
		}
		return false;
	}

	public void release() {
		Log.v(LOGTAG, "release ");
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			Constant.MusicPlayData.IS_PLAY_NEW = true;
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(LOGTAG, "onResume ()");
		updateList();
	}

	public void updateList() {
		Log.i(LOGTAG, "updateList");
		if (mResmueFlag) {
			try {
				getMusicList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 再获取一次列表以便后台播放回来后更新列表
		}
		mResmueFlag = true;
		mListViewAdapter.notifyDataSetChanged();
		mMusicListLength = Constant.MusicPlayData.myMusicList.size();
		int index = Constant.MusicPlayData.CURRENT_PLAY_INDEX;
		if ((mMusicListLength > 0) && (mMusicListLength > Constant.MusicPlayData.CURRENT_PLAY_INDEX)) {
			mCurrentName = (String) Preferences.getPreferences(mActivity, "songName", "song");
		}
		Log.i(LOGTAG, "mCurrentName:" + mCurrentName);
		boolean checksongExit = false;
		for (int i = 0; i < Constant.MusicPlayData.myMusicList.size(); i++) {
			if (mCurrentName.equalsIgnoreCase(Constant.MusicPlayData.myMusicList.get(i).getFileName())) {
				checksongExit = true;
				Log.v(LOGTAG, "checksongExit:" + checksongExit);
				Constant.MusicPlayData.CURRENT_PLAY_INDEX = i;
				break;
			}
		}
		if (!checksongExit) {
			// 查询不到当前歌曲，进行下一曲
			Constant.MusicPlayData.IS_PLAY_NEW = true;
			Constant.MusicPlayData.CURRENT_PLAY_POSITION = 0;
			Constant.MusicPlayData.CURRENT_PLAY_INDEX = index;
			playMusic(false);
		}
		mListView.setSelection(Constant.MusicPlayData.CURRENT_PLAY_INDEX);
		setDurationAndCurrentTime();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(LOGTAG, "onPause ()");
	}

	private void initUIManager() {
		mImgBtnPlayAndPause = (ImageButton) mView.findViewById(R.id.list_music_btn_playAndPause);
		mImgBtnPlayPre = (ImageButton) mView.findViewById(R.id.list_music_btn_playPre);
		mImgBtnPlayNext = (ImageButton) mView.findViewById(R.id.list_music_btn_playNext);

		mSeekBar = (SeekBar) mView.findViewById(R.id.list_music_seekBar);
		mTxtCurrPlayTime = (TextView) mView.findViewById(R.id.list_music_txt_currTime);
		mTxtPlayTotalTime = (TextView) mView.findViewById(R.id.list_music_txt_totalTime);
		mTxtPlayName = (TextView) mView.findViewById(R.id.list_music_name);
		mTxtAuthorName = (TextView) mView.findViewById(R.id.list_music_aithor);

		mImgBtnPlayAndPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mMediaPlayer != null) {
					doPauseResume();
				} else {
					playMusic(false);
				}
			}
		});
		mImgBtnPlayPre.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				playPreviousMusic();
			}
		});
		mImgBtnPlayNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				playNextMusic();
			}
		});
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
				if (arg2 == true) {
					Constant.MusicPlayData.CURRENT_PLAY_POSITION = position;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				if (mMediaPlayer != null) {
					mMediaPlayer.seekTo(Constant.MusicPlayData.CURRENT_PLAY_POSITION);
				}

				sendUpdateMessage(AUTO_UPDATE);
			}

		});

	}

	private void setTimerTask() {
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendUpdateMessage(AUTO_UPDATE);
			}
		}, 0, 1000);
	}

	public void setFadeUpToDown(boolean flag) {
		if (messageHandler != null) {
			if (flag) {
				messageHandler.removeMessages(FADEUP);
				messageHandler.sendEmptyMessage(FADEDOWN);
			} else {
				messageHandler.removeMessages(FADEDOWN);
				messageHandler.sendEmptyMessage(FADEUP);
			}
		}
	}

	private void sendUpdateMessage(int type) {
		Message message = Message.obtain();
		message.what = type;
		messageHandler.sendMessage(message);
	}

	@SuppressLint("HandlerLeak")
	private class MessageHandler extends Handler {
		float mCurrentVolume = 1.0f;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AUTO_UPDATE:
				if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
					int position = mMediaPlayer.getCurrentPosition();
					Constant.MusicPlayData.CURRENT_PLAY_POSITION = position;
					mSeekBar.setProgress(position);
					mTxtCurrPlayTime.setText(Utils.showTime(position));
				}
				break;
			case FADEDOWN:
				mCurrentVolume -= .05f;
				if (mCurrentVolume > .2f) {
					messageHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
				} else {
					mCurrentVolume = .2f;
				}
				if (mMediaPlayer != null)
					mMediaPlayer.setVolume(mCurrentVolume, mCurrentVolume);
				break;
			case FADEUP:
				mCurrentVolume += .01f;
				if (mCurrentVolume < 1.0f) {
					messageHandler.sendEmptyMessageDelayed(FADEUP, 10);
				} else {
					mCurrentVolume = 1.0f;
				}
				if (mMediaPlayer != null)
					mMediaPlayer.setVolume(mCurrentVolume, mCurrentVolume);
				break;

			}
		}
	};

	private void getPlayerInfo() {
		if (Constant.MusicPlayData.myMusicList.size() > 0) {
			Preferences.setPreferences(mActivity, "songId", Constant.MusicPlayData.CURRENT_PLAY_INDEX);
			Preferences.setPreferences(mActivity, "songName", Constant.MusicPlayData.myMusicList.get(Constant.MusicPlayData.CURRENT_PLAY_INDEX).getFileName());
			if (mMediaPlayer != null) {
				Preferences.setPreferences(mActivity, "currentTime", mMediaPlayer.getCurrentPosition());
				Preferences.setPreferences(mActivity, "totalTime", mMediaPlayer.getDuration());
			}
			Preferences.setPreferences(mActivity, "songArtist", Constant.MusicPlayData.myMusicList.get(Constant.MusicPlayData.CURRENT_PLAY_INDEX)
					.getMusicArtist());
		}
	}

	public synchronized void getMusicList() throws Exception {
		Constant.MusicPlayData.myMusicList.clear();
		if (cur != null) {
			cur.close();
			cur = null;
		}
		cur = this
				.getActivity()
				.getContentResolver()
				.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.TITLE,
								MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.YEAR,
								MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DATA }, null, null, null);
		if (cur == null) {
			Utils.displayToast(R.string.notice_list_null);
			return;
		}
		int index = 1;
		while (cur.moveToNext()) {
			MusicData song = new MusicData();
			song.setMusicID(index);
			song.setFileName(cur.getString(1));
			song.setMusicName(cur.getString(2));
			song.setMusicArtist(cur.getString(4));
			song.setMusicDuration(cur.getInt(3));
			song.setMusicAlbum(cur.getString(5));
			if (cur.getString(6) != null) {
				song.setMusicYear(cur.getString(6));
			} else {
				song.setMusicYear("undefine");
			}
			if ("audio/mpeg".equals(cur.getString(7).trim())) {// file type
				song.setFileType("mp3");
			} else if ("audio/x-ms-wma".equals(cur.getString(7).trim())) {
				song.setFileType("wma");
			}
			song.setFileType(cur.getString(7));
			if (cur.getString(8) != null) {// fileSize
			} else {
				song.setFileSize("undefine");
			}
			song.setFileSize(cur.getString(8));
			if (cur.getString(9) != null) {
				song.setFilePath(cur.getString(9));
			}
			index++;
			Constant.MusicPlayData.myMusicList.add(song);
		}
		cur.close();

	}

	public void playPreviousMusic() {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			// return;
		}
		Constant.MusicPlayData.CURRENT_PLAY_POSITION = 0;
		int checkIndex = checkNowSong();
		if (checkIndex == 0) {
			return;
		} else if (checkIndex == 1) {
			Constant.MusicPlayData.CURRENT_PLAY_INDEX = setCurrIndexByPlayType(Constant.MusicPlayState.CURRENT_PLAY_STATE,
					Constant.MusicPlayControl.MUSIC_CONTROL_PREVIOUS, Constant.MusicPlayData.CURRENT_PLAY_INDEX, Constant.MusicPlayData.myMusicList.size());
		} else if (checkIndex == 2) {
		} else {
			return;
		}
		Constant.MusicPlayData.IS_PLAY_NEW = true;
		Constant.MusicPlayState.CURRENT_PLAY_STATE = Constant.MusicPlayState.PLAY_STATE_PLAYING;
		mImgBtnPlayAndPause.setImageResource(R.drawable.ic_music_pause_focused);
		playMusic(true);
	}

	public void playNextMusic() {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			// return;
		}
		Constant.MusicPlayData.CURRENT_PLAY_POSITION = 0;
		int checkIndex = checkNowSong();
		if (checkIndex == 0) {
			Toast.makeText(mActivity, R.string.music_info_error, Toast.LENGTH_SHORT).show();
			return;
		} else if (checkIndex == 1) {
			Constant.MusicPlayData.CURRENT_PLAY_INDEX = setCurrIndexByPlayType(Constant.MusicPlayState.CURRENT_PLAY_STATE,
					Constant.MusicPlayControl.MUSIC_CONTROL_NEXT, Constant.MusicPlayData.CURRENT_PLAY_INDEX, Constant.MusicPlayData.myMusicList.size());
		} else if (checkIndex == 2) {
		} else {
			return;
		}
		Constant.MusicPlayData.IS_PLAY_NEW = true;
		Constant.MusicPlayState.CURRENT_PLAY_STATE = Constant.MusicPlayState.PLAY_STATE_PLAYING;
		mImgBtnPlayAndPause.setImageResource(R.drawable.ic_music_pause_focused);
		playMusic(false);

	}

	private int checkNowSong() {
		int isCorrect = 0;
		try {
			if (!mActivity.mMediaFree) {
				isCorrect = 0;
				return isCorrect;
			}
			if (Constant.MusicPlayData.CURRENT_PLAY_INDEX >= 0 && Constant.MusicPlayData.CURRENT_PLAY_INDEX < Constant.MusicPlayData.myMusicList.size()) {
				isCorrect = 1;
			} else {
				if (Constant.MusicPlayData.myMusicList.size() > 0) {
					Constant.MusicPlayData.CURRENT_PLAY_INDEX = 0;
					isCorrect = 2;
				} else {
					Constant.MusicPlayData.CURRENT_PLAY_INDEX = -1;
					isCorrect = 0;
				}
			}
			return isCorrect;
		} catch (Exception e) {
			isCorrect = 0;
			return isCorrect;
		}
	}

	private int setCurrIndexByPlayType(int playmode, int playDirection, int index, int allCount) {
		int newIndex = index;
		switch (playmode) {
		case Constant.MusicPlayMode.PLAY_MODE_ORDER:
			if (playDirection == Constant.MusicPlayControl.MUSIC_CONTROL_PREVIOUS) {
				if (index == 0) {
					newIndex = index;
				} else {
					newIndex = index - 1;
				}
			} else if (playDirection == Constant.MusicPlayControl.MUSIC_CONTROL_NEXT) {
				if (index == allCount - 1) {
					newIndex = index;
				} else {
					newIndex = index + 1;
				}
			}
			break;
		case Constant.MusicPlayMode.PLAY_MODE_LIST_LOOP:
			if (playDirection == Constant.MusicPlayControl.MUSIC_CONTROL_PREVIOUS) {
				if (index == 0) {
					newIndex = allCount - 1;
				} else {
					newIndex = index - 1;
				}
			} else if (playDirection == Constant.MusicPlayControl.MUSIC_CONTROL_NEXT) {
				if (index == allCount - 1) {
					newIndex = 0;
				} else {
					newIndex = index + 1;
				}
			}
			break;
		case Constant.MusicPlayMode.PLAY_MODE_RANDOM:
			newIndex = Utils.generateRandom(allCount - 1, index);
			break;
		case Constant.MusicPlayMode.PLAY_MODE_SINGLE:
			newIndex = index;
			break;
		default:
			break;
		}
		return newIndex;
	}

	public void playMusic(boolean pre) {
		try {
			if (mMediaPlayer != null) {
				Log.i(LOGTAG, "before play release.");
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
			setCurrentShow();
			if (Constant.MusicPlayData.myMusicList.size() > 0) {
				mMediaPlayer = new MediaPlayer();
				if (Constant.MusicPlayData.IS_PLAY_NEW) {
					mMediaPlayer.reset();

					mMediaPlayer.setDataSource(Constant.MusicPlayData.myMusicList.get(Constant.MusicPlayData.CURRENT_PLAY_INDEX).getFilePath());

					mMediaPlayer.prepare();
					Constant.MusicPlayData.IS_PLAY_NEW = false;
				} else {
					if (Constant.MusicPlayState.CURRENT_PLAY_STATE == Constant.MusicPlayState.PLAY_STATE_PAUSE) {
						Utils.displayToast(R.string.notice_playmode_continue);
					}
				}
				Constant.MusicPlayState.CURRENT_PLAY_STATE = Constant.MusicPlayState.PLAY_STATE_PLAYING;
				mMediaPlayer.seekTo(Constant.MusicPlayData.CURRENT_PLAY_POSITION);
				mMediaPlayer.start();
				setFadeUpToDown(false);
				setDurationAndCurrentTime();
				mMediaPlayer.setOnCompletionListener(new MediaPlayerOnCompletionListener());
				mMediaPlayer.setOnErrorListener(new MediaPlayerOnErrorListener());
			}
		} catch (Exception e) {
			Log.e(LOGTAG, "exception: " + e);
			Toast.makeText(mActivity, R.string.music_info_error, Toast.LENGTH_SHORT).show();
			if (pre) {
				initTextShow();
			} else {
				playNextMusic();
			}
		}
		getPlayerInfo();
	}

	private class MediaPlayerOnCompletionListener implements OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer arg0) {
			Log.v(LOGTAG, "onCompletion");
			if (Constant.MusicPlayData.CURRENT_PLAY_INDEX == Constant.MusicPlayData.myMusicList.size() - 1
					&& Constant.MusicPlayMode.CURRENT_PLAY_MODE == Constant.MusicPlayMode.PLAY_MODE_ORDER) {
				Utils.displayToast(R.string.notice_playmode_lastsong);
				mMediaPlayer.pause();
				Constant.MusicPlayState.CURRENT_PLAY_STATE = Constant.MusicPlayState.PLAY_STATE_PAUSE;
				Constant.MusicPlayData.IS_PLAY_NEW = false;
			} else if (Constant.MusicPlayMode.CURRENT_PLAY_MODE == Constant.MusicPlayMode.PLAY_MODE_SINGLE) {
				playMusic(false);
			} else {
				playNextMusic();
			}
		}
	}

	private class MediaPlayerOnErrorListener implements OnErrorListener {
		@Override
		public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
			Log.v(LOGTAG, "onError");
			playNextMusic();
			return false;
		}

	}

	private void updatePausePlay() {
		if (mImgBtnPlayAndPause == null || mMediaPlayer == null)
			return;
		if (mMediaPlayer.isPlaying()) {
			mImgBtnPlayAndPause.setImageResource(R.drawable.selector_pause_button);
		} else {
			mImgBtnPlayAndPause.setImageResource(R.drawable.selector_play_button);
		}
	}

	public void pause() {
		synchronized (this) {
			messageHandler.removeMessages(FADEUP);
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
			}
			updatePausePlay();
		}
	}

	public void controlPauseResume() {
		if (mMediaPlayer == null) {
			playMusic(true);
			return;
		}
		doPauseResume();
	}

	public void doPauseResume() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				messageHandler.removeMessages(FADEUP);
			} else {
				mMediaPlayer.start();
				setFadeUpToDown(false);
			}
		}
		updatePausePlay();
	}

	public void doFastForward() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			int forward = mMediaPlayer.getCurrentPosition();
			forward = forward + 5000;
			mMediaPlayer.seekTo(forward);
		}
	}

	public void doRewind() {
		if (mMediaPlayer != null) {
			int rewind = mMediaPlayer.getCurrentPosition();
			rewind = rewind - 5000;
			if (rewind < 0) {
				rewind = 0;
			}
			mMediaPlayer.seekTo(rewind);
		}
	}

	private void setDurationAndCurrentTime() {
		// if (mMediaPlayer != null) {
		int index = Constant.MusicPlayData.CURRENT_PLAY_INDEX;

		if ((mMusicListLength > 0) && (index >= 0) && index < Constant.MusicPlayData.myMusicList.size()) {
			mTxtPlayName.setText(Constant.MusicPlayData.myMusicList.get(index).getFileName());
			mTxtAuthorName.setText(Constant.MusicPlayData.myMusicList.get(index).getMusicArtist());
			mTxtCurrPlayTime.setText(Utils.showTime(Constant.MusicPlayData.CURRENT_PLAY_POSITION));
			mSeekBar.setMax(Constant.MusicPlayData.TOTAL_TIME);
			mSeekBar.setProgress(Constant.MusicPlayData.CURRENT_PLAY_POSITION);
			setCurrentShow();
			mTxtPlayName.setVisibility(View.VISIBLE);
			mTxtAuthorName.setVisibility(View.VISIBLE);
			mTxtPlayTotalTime.setText(Utils.showTime(Constant.MusicPlayData.TOTAL_TIME));
			if (mMediaPlayer != null) {
				mTxtPlayTotalTime.setText(Utils.showTime(mMediaPlayer.getDuration()));
				Constant.MusicPlayData.TOTAL_TIME = mMediaPlayer.getDuration();
				mSeekBar.setMax(mMediaPlayer.getDuration());
				// if (!mMediaPlayer.isPlaying()) {
				// mMediaPlayer.start();
				// }
			}
		} else {
			initTextShow();
		}
		// }
		updatePausePlay();
	}

	public void initTextShow() {
		mTxtPlayName.setVisibility(View.INVISIBLE);
		mTxtAuthorName.setVisibility(View.INVISIBLE);
		mTxtPlayTotalTime.setText("00:00");
		mTxtCurrPlayTime.setText("00:00");
		mSeekBar.setProgress(0);
	}

	public void updateListView() {
		mListViewAdapter.notifyDataSetChanged();
	}

	private void setCurrentShow() {
		if (Constant.MusicPlayData.CURRENT_PLAY_INDEX > -1) {
			mListViewAdapter.setSelectItem(Constant.MusicPlayData.CURRENT_PLAY_INDEX);
			mListViewAdapter.notifyDataSetChanged();
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