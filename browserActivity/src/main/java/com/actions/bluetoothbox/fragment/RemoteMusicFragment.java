package com.actions.bluetoothbox.fragment;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.app.BrowserActivity;
import com.actions.bluetoothbox.lyric.LyricView;
import com.actions.bluetoothbox.util.Preferences;
import com.actions.bluetoothbox.util.Utils;
import com.actions.bluetoothbox.util.VerticalSeekBar;
import com.actions.ibluz.factory.IBluzDevice;
import com.actions.ibluz.manager.BluzManagerData.EQMode;
import com.actions.ibluz.manager.BluzManagerData.FeatureFlag;
import com.actions.ibluz.manager.BluzManagerData.FolderEntry;
import com.actions.ibluz.manager.BluzManagerData.FuncMode;
import com.actions.ibluz.manager.BluzManagerData.LoopMode;
import com.actions.ibluz.manager.BluzManagerData.MusicEntry;
import com.actions.ibluz.manager.BluzManagerData.OnLyricEntryReadyListener;
import com.actions.ibluz.manager.BluzManagerData.OnManagerReadyListener;
import com.actions.ibluz.manager.BluzManagerData.OnMusicEntryChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnMusicUIChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnPListEntryReadyListener;
import com.actions.ibluz.manager.BluzManagerData.PListEntry;
import com.actions.ibluz.manager.BluzManagerData.PlayState;
import com.actions.ibluz.manager.IGlobalManager;
import com.actions.ibluz.manager.IMusicManager;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.sunflower.FlowerCollector;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class RemoteMusicFragment extends SherlockFragment {
	private static final String TAG = "RemoteMusicFragment";

	private static final int MESSAGE_REFRESH_UI = 1;
	private static final int MESSAGE_REFRESH_LYRIC = 2;
	private static final int MESSAGE_SET_LYRIC = 3;
	private static final int MESSAGE_GET_PLISTENTRY = 4;

	private BrowserActivity mActivity;
	private View mMainView;
	private ViewPager mViewPager;
	private View mListPager;
	private View mInfoPager;
	private ImageButton mSendSpeechButton;
	private View mLyricPager;
	private ImageButton mLoopModeButton;
	private ImageButton mPlayPauseButton;
	private ImageButton mPreviousButton;
	private ImageButton mNextButton;
	private ImageButton mEQImageButton;
	private SeekBar mSeekBar;
	private ListView mListView;
	private TextView mCurrentText;
	private TextView mDurationText;
	private TextView mMusicNameText;
	private TextView mArtistNameText;
	private TextView mMusicTitleText;
	private TextView mMusicArtistText;
	private TextView mMusicAblumText;
	private TextView mMusicGenreText;
	private TextView mMusicMimeTypeText;
	private View mEqSettingLayout;
	private VerticalSeekBar[] mEqSeekBar;
	private LyricView mLyricView;
	private String mLrcFilePath;

	private SlidingMenu mSlidingMenu;
	private List<View> mPagerList;
	private MusicPagerAdapter mPagerAdapter;
	private MusicListAdapter mMusicListAdapter;
	private int mEqPreset = EQMode.NORMAL;
	private int mLoopPreset = LoopMode.UNKNOWN;
	private int mPlayStatePreset = PlayState.UNKNOWN;
	private List<int[]> mEqBandLevel = new ArrayList<int[]>();
	private List<PListEntry> mPListEntryList = new ArrayList<PListEntry>();

	private IMusicManager mMusicManager;
	private MusicEntry mCurrentMusicEntry;
	private ProgressDialog mProgressDialog;
	private IBluzDevice mBluzConnector;
	private List<FolderEntry> mFolderEntryList;
	private IGlobalManager mBluzManager;

	private boolean isChanged = false;
	private int mSelectedMode = FuncMode.UNKNOWN;;
	private boolean isSDCardPListUpdate = false;
	private boolean isUhostPListUpdate = false;
	private boolean isCRecordPListUpdate = false;
	private boolean isURecordPListUpdate = false;
	private boolean isSpecialcatalogUpdate = false;
	private boolean isESSShowed = true;
	private Menu mMenu = null;
/***************************语音识别****************************************/
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				//showTip("初始化失败，错误码：" + code);
				System.out.println("===初始化失败，错误码====="+code);
			}
		}
	};
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		String lag = "mandarin";
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);
		}

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		//mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		//mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		//mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
		mIat.setParameter(SpeechConstant.ASR_PTT,"0");
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
		
		// 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
		// 注：该参数暂时只对在线听写有效
		mIat.setParameter(SpeechConstant.ASR_DWA,"0");
	}
	
	
	/**
	 * 听写监听器。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			System.out.println("开始说话");
		}

		@Override
		public void onError(SpeechError error) {
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			System.out.println(error.getPlainDescription(true));
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			System.out.println("结束说话");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, results.getResultString());
			printResult(results);

			if (isLast) {
			}
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			System.out.println("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据："+data.length);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};
	
	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());
		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}
		for(int i = 0;i < mPListEntryList.size();i++)
		{
			if(mPListEntryList.get(i).name.contains(resultBuffer.toString()))
			{
				System.out.println("=========change=ID=Name========="+i+mPListEntryList.get(i).name);
				mMusicManager.select(i+1);
				break;
			}
			else
			{
				System.out.println(mPListEntryList.get(i).name);
			}
		}
		System.out.println("================speechChoseMusicEnd============="+resultBuffer.toString());
		

	}
/***********************************************************/
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		mActivity = (BrowserActivity) getActivity();
		mSlidingMenu = mActivity.getSlidingMenu();
		mProgressDialog = new ProgressDialog(mActivity);
		mProgressDialog.setMessage(getText(R.string.music_preparation_message));
		mProgressDialog.show();
		mEngineType = SpeechConstant.TYPE_CLOUD;
		// 初始化识别无UI识别对象
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = SpeechRecognizer.createRecognizer(mActivity, mInitListener);
		mMusicManager = mActivity.getIBluzManager().getMusicManager(new OnManagerReadyListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onReady() {
				mBluzConnector = mActivity.getBluzConnector();
				mBluzManager = mActivity.getIGlobalManager();
				mSelectedMode = mActivity.getCurrentMode();
				mFolderEntryList = mBluzManager.getMusicFolderList();
			
				
				
				isChanged = mBluzManager.isContentChanged();
				String currentConnectDeviceAddress = mBluzConnector.getConnectedDevice().getAddress();
				String preConnectDevcieAddress = (String) Preferences.getPreferences(mActivity, Preferences.KEY_DEVICE_ADDRESS, "");
				boolean isDeviceChanged = currentConnectDeviceAddress.equalsIgnoreCase(preConnectDevcieAddress);
			
				switch (mSelectedMode) {
				case FuncMode.CARD:
					String sdCardPlist = (String) Preferences.getPreferences(mActivity, Preferences.KEY_MUSIC_PLIST, "");
					if (sdCardPlist.length() == 0) {
						isSDCardPListUpdate = true;
					}
					break;
				case FuncMode.USB:
					String uHostCardPlist = (String) Preferences.getPreferences(mActivity, Preferences.KEY_UHOST_PLIST, "");
					if (uHostCardPlist.length() == 0) {
						isUhostPListUpdate = true;
					}
					break;
				case FuncMode.CRECORD:
					String cRecordPlist = (String) Preferences.getPreferences(mActivity, Preferences.KEY_CRECORD_PLIST, "");
					if (cRecordPlist.length() == 0) {
						isCRecordPListUpdate = true;
					}
					break;
				case FuncMode.URECORD:
					String uRecordPlist = (String) Preferences.getPreferences(mActivity, Preferences.KEY_URECORD_PLIST, "");
					if (uRecordPlist.length() == 0) {
						isURecordPListUpdate = true;
					}
					break;
				default:
					for (int i = 0; i < mFolderEntryList.size(); i++) {
						if (mSelectedMode == mFolderEntryList.get(i).value) {
							String specialCatalogPlist = (String) Preferences.getPreferences(mActivity, mFolderEntryList.get(i).name, "");
							if (specialCatalogPlist.length() == 0) {
								isSpecialcatalogUpdate = true;
							}
						}
					}
					break;
				}
				if (!isDeviceChanged || isChanged || isSpecialcatalogUpdate || isSDCardPListUpdate || isUhostPListUpdate || isCRecordPListUpdate
						|| isURecordPListUpdate) {
					Preferences.setPreferences(mActivity, Preferences.KEY_DEVICE_ADDRESS, currentConnectDeviceAddress);
					if (isChanged || !isDeviceChanged) {
						// Reset
						Preferences.setPreferences(mActivity, Preferences.KEY_MUSIC_PLIST, "");
						Preferences.setPreferences(mActivity, Preferences.KEY_UHOST_PLIST, "");
						Preferences.setPreferences(mActivity, Preferences.KEY_CRECORD_PLIST, "");
						Preferences.setPreferences(mActivity, Preferences.KEY_URECORD_PLIST, "");
						for (int i = 0; i < mFolderEntryList.size(); i++) {
							Preferences.setPreferences(mActivity, mFolderEntryList.get(i).name, "");
						}

					}

					mHandler.sendEmptyMessage(MESSAGE_GET_PLISTENTRY);
				} else {
					
					List<PListEntry> mTempPListList = new ArrayList<PListEntry>();
					if (mSelectedMode == FeatureFlag.SDCARD) {
						mTempPListList = (List<PListEntry>) Preferences.getComplexDataInPreference(mActivity, Preferences.KEY_MUSIC_PLIST);
					} else if (mSelectedMode == FeatureFlag.UHOST) {
						mTempPListList = (List<PListEntry>) Preferences.getComplexDataInPreference(mActivity, Preferences.KEY_UHOST_PLIST);
					} else if (mSelectedMode == FuncMode.CRECORD) {
						mTempPListList = (List<PListEntry>) Preferences.getComplexDataInPreference(mActivity, Preferences.KEY_CRECORD_PLIST);
					} else if (mSelectedMode == FuncMode.URECORD) {
						mTempPListList = (List<PListEntry>) Preferences.getComplexDataInPreference(mActivity, Preferences.KEY_URECORD_PLIST);
					} else {
						for (int i = 0; i < mFolderEntryList.size(); i++) {
							if (mSelectedMode == mFolderEntryList.get(i).value) {
								mTempPListList = (List<PListEntry>) Preferences.getComplexDataInPreference(mActivity, mFolderEntryList.get(i).name);
							}
						}
					}
					mPListEntryList.clear();
					
					for (PListEntry pe : mTempPListList) {
						mPListEntryList.add(pe);
						mMusicListAdapter.notifyDataSetChanged();
					}
					if (mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}
					if (mCurrentMusicEntry != null) {
						mListView.setSelectionFromTop(mCurrentMusicEntry.index - 1, 200);
					}
				}
			}
		});
		
		mMusicManager.setOnMusicUIChangedListener(mMusicUIChangedListener);
		mMusicManager.setOnMusicEntryChangedListener(mMusicEntryChangedListener);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView!");
		setHasOptionsMenu(true);
		mMainView = inflater.inflate(R.layout.fragment_remotemusic, container, false);
		mLoopModeButton = (ImageButton) mMainView.findViewById(R.id.musicLoopModeButton);
		mPlayPauseButton = (ImageButton) mMainView.findViewById(R.id.musicPlayPauseButton);
		mPreviousButton = (ImageButton) mMainView.findViewById(R.id.musicPreviousButton);
		mNextButton = (ImageButton) mMainView.findViewById(R.id.musicNextButton);
		//mEQImageButton = (ImageButton) mMainView.findViewById(R.id.musicPlaceholderButton);
		mSeekBar = (SeekBar) mMainView.findViewById(R.id.musicSeekBar);
		mCurrentText = (TextView) mMainView.findViewById(R.id.musicCurrentText);
		mDurationText = (TextView) mMainView.findViewById(R.id.musicDurationText);
		mMusicNameText = (TextView) mMainView.findViewById(R.id.musicNameText);
		mArtistNameText = (TextView) mMainView.findViewById(R.id.artistNameText);
		mViewPager = (ViewPager) mMainView.findViewById(R.id.advancedViewPager);
	
		mSendSpeechButton = (ImageButton) mMainView.findViewById(R.id.iat_recognize);
		mSendSpeechButton.setOnClickListener(mOnClickListener);
		
		mLoopModeButton.setOnClickListener(mOnClickListener);
		mPlayPauseButton.setOnClickListener(mOnClickListener);
		mPreviousButton.setOnClickListener(mOnClickListener);
		mNextButton.setOnClickListener(mOnClickListener);
		//mEQImageButton.setOnClickListener(mOnClickListener);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 0) {
					mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
				} else {
					mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
				}

				if (arg0 == 2) {
					mHandler.sendEmptyMessage(MESSAGE_SET_LYRIC);
					mHandler.sendEmptyMessage(MESSAGE_REFRESH_LYRIC);
				} else {
					mHandler.removeMessages(MESSAGE_REFRESH_LYRIC);
				}

				if (arg0 == 3) {
					equalizerUpdateDisplay();
				}
			}
		});

		mListPager = inflater.inflate(R.layout.pager_music_list, null);
		mInfoPager = inflater.inflate(R.layout.pager_music_info, null);
		mLyricPager = inflater.inflate(R.layout.pager_music_lyric, null);

		mListView = (ListView) mListPager.findViewById(R.id.music_list);
		mMusicListAdapter = new MusicListAdapter(this.getActivity(), mPListEntryList);
		mListView.setAdapter(mMusicListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Log.v(TAG, "===================shine===changesongItem==========================");
				
				mMusicManager.select(position + 1);
			}
		});

		mMusicTitleText = (TextView) mInfoPager.findViewById(R.id.musicTitleText);
		mMusicArtistText = (TextView) mInfoPager.findViewById(R.id.musicArtistText);
		mMusicAblumText = (TextView) mInfoPager.findViewById(R.id.musicAblumText);
		mMusicGenreText = (TextView) mInfoPager.findViewById(R.id.musicGenreText);
		mMusicMimeTypeText = (TextView) mInfoPager.findViewById(R.id.musicMimeTypeText);

		mLyricView = (LyricView) mLyricPager.findViewById(R.id.musicLyricView);

		mPagerList = new ArrayList<View>();
		mPagerList.add(mListPager);
		mPagerList.add(mInfoPager);
		mPagerList.add(mLyricPager);
		mPagerAdapter = new MusicPagerAdapter(mPagerList);
		mViewPager.setAdapter(mPagerAdapter);

		return mMainView;
	}

	@Override
	public void onResume() {
		super.onResume();
		FlowerCollector.onResume(mActivity);
		FlowerCollector.onPageStart(TAG);
		Log.v(TAG, "onResume!");
		// review current slidingmenu state
		if (mViewPager.getCurrentItem() != 0) {
			mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		}
		if (mCurrentMusicEntry == null) {
			String unknown = mActivity.getResources().getString(R.string.unknown);
			mCurrentMusicEntry = new MusicEntry();
			mCurrentMusicEntry.title = unknown;
			mCurrentMusicEntry.album = unknown;
			mCurrentMusicEntry.artist = unknown;
			mCurrentMusicEntry.genre = unknown;
			mCurrentMusicEntry.name = unknown;
		}
		refreshUIWidget(mCurrentMusicEntry);
		mHandler.sendEmptyMessage(MESSAGE_REFRESH_UI);
		initLyric();

	}

	@Override
	public void onPause() {
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(mActivity);
		super.onPause();
		Log.v(TAG, "onPause!");
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mHandler.removeMessages(MESSAGE_SET_LYRIC);
		mHandler.removeMessages(MESSAGE_REFRESH_LYRIC);
		mHandler.removeMessages(MESSAGE_REFRESH_UI);
	}

	@Override
	public void onDestroyView() {
		Log.v(TAG, "onDestroyView!");
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mHandler.removeMessages(MESSAGE_GET_PLISTENTRY);
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy!");
		super.onDestroy();
	}

	private void equalizerUpdateDisplay() {
		int[] level = mEqBandLevel.get(mEqPreset);

		for (int i = 0; i < level.length; i++) {
			mEqSeekBar[i].setProgressAndThumb(level[i] + 12);
		}

		if (mEqPreset == EQMode.USER) {
			Utils.setAlphaForView(mEqSettingLayout, 1.0f);
			for (int i = 0; i < mEqSeekBar.length; i++) {
				mEqSeekBar[i].setEnabled(true);
			}
		} else {
			Utils.setAlphaForView(mEqSettingLayout, 0.5f);
			for (int i = 0; i < mEqSeekBar.length; i++) {
				mEqSeekBar[i].setEnabled(false);
			}
		}
	}

	private void updateLoopChanged(int loop) {
		if (loop == LoopMode.ALL) {
			mLoopModeButton.setImageResource(R.drawable.selector_loop_all_button);
		} else if (loop == LoopMode.SINGLE) {
			mLoopModeButton.setImageResource(R.drawable.selector_loop_single_button);
		} else if (loop == LoopMode.SHUFFLE) {
			mLoopModeButton.setImageResource(R.drawable.selector_loop_shuffle_button);
		} else {
			mMusicManager.setLoopMode(LoopMode.ALL);
			mLoopModeButton.setImageResource(R.drawable.selector_loop_all_button);
		}
	}

	private void updateStateChanged(int state) {
		if (state == PlayState.PLAYING) {
			mPlayPauseButton.setImageResource(R.drawable.selector_pause_button);
		} else {
			mPlayPauseButton.setImageResource(R.drawable.selector_play_button);
		}
	}

	private OnMusicEntryChangedListener mMusicEntryChangedListener = new OnMusicEntryChangedListener() {

		@Override
		public void onChanged(MusicEntry entry) {
			//这里就有详细信息了
			mCurrentMusicEntry = entry;
			mMusicListAdapter.notifyDataSetChanged();
			mListView.setSelectionFromTop(mCurrentMusicEntry.index - 1, 100);
			Log.v(TAG, "===================shine===changesongrefreshUI=========================");
			refreshUIWidget(entry);
			initLyric();

		}
	};

	private OnPListEntryReadyListener mOnPListEntryReadyListener = new OnPListEntryReadyListener() {

		@Override
		public void onReady(List<PListEntry> entry) {
			mPListEntryList.addAll(entry);
		
			mMusicListAdapter.notifyDataSetChanged();
			if (mProgressDialog.isShowing()) {
				mProgressDialog.setMessage(getText(R.string.notice_loadingnum) + String.valueOf(mMusicManager.getPListSize()) + "\n"
						+ getText(R.string.notice_loadingcurrentnum) + mPListEntryList.size());
				// loading view
				// if (mPListEntryList.size() > 20 &&
				// mProgressDialog.isShowing()) {
				// mProgressDialog.dismiss();
				// }
			}
			mHandler.sendEmptyMessage(MESSAGE_GET_PLISTENTRY);
		}
	};

	private OnLyricEntryReadyListener mOnLyricEntryReadyListener = new OnLyricEntryReadyListener() {

		@Override
		public void onReady(byte[] buffer) {
			Utils.createExternalStoragePrivateFile(mCurrentMusicEntry.title + ".lrc", buffer);
			mHandler.sendEmptyMessage(MESSAGE_SET_LYRIC);
			mHandler.sendEmptyMessage(MESSAGE_REFRESH_LYRIC);
		}
	};

	private OnMusicUIChangedListener mMusicUIChangedListener = new OnMusicUIChangedListener() {

		@Override
		public void onLoopChanged(int loop) {
			mLoopPreset = loop;
			updateLoopChanged(mLoopPreset);
		}

		@Override
		public void onStateChanged(int state) {
			mPlayStatePreset = state;
			updateStateChanged(mPlayStatePreset);
		}

	};

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.musicLoopModeButton:
				int loop = mLoopPreset;// mMusicManager.getLoopMode()
				switch (loop) {
				case LoopMode.ALL:
					loop = LoopMode.SINGLE;
					break;
				case LoopMode.SINGLE:
					loop = LoopMode.SHUFFLE;
					break;
				case LoopMode.SHUFFLE:
					loop = LoopMode.ALL;
					break;
				default:
					loop = LoopMode.ALL;
					break;
				}
				mMusicManager.setLoopMode(loop);
				break;
			case R.id.musicPlayPauseButton:
				if (mPlayStatePreset == PlayState.PAUSED) {
					mMusicManager.play();
				} else {
					mMusicManager.pause();
				}
				break;
			case R.id.musicPreviousButton:
				mMusicManager.previous();
				break;
			case R.id.musicNextButton:
				mMusicManager.next();
				break;
			//case R.id.musicPlaceholderButton:
				//break;
			case R.id.iat_recognize:
				mMusicManager.pause();
				System.out.println("======================点击说话按钮==========================");
				mIatResults.clear();
				// 设置参数
				setParam();
				System.out.println("================设置参数结束==============");
				int ret = mIat.startListening(mRecognizerListener);
				if (ret != ErrorCode.SUCCESS) {

					System.out.println("===============监听开始失败==============错误码="+ret);
				} else {

					System.out.println("===============监听开始==============");
				}
				break;
			default:
				break;
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_REFRESH_UI:
				mCurrentText.setText(Utils.showTime(mMusicManager.getCurrentPosition()));
				mSeekBar.setProgress(mMusicManager.getCurrentPosition());
				mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_UI, 300);
				break;
			case MESSAGE_REFRESH_LYRIC:
				long delay = mLyricView.updateIndex(mMusicManager.getCurrentPosition());
				if (delay == 0) {
					delay = 200;
				}
				mHandler.removeMessages(MESSAGE_REFRESH_LYRIC);
				mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_LYRIC, delay);
				break;
			case MESSAGE_SET_LYRIC:
				mLrcFilePath = mActivity.getExternalFilesDir(null) + "/" + mCurrentMusicEntry.title + ".lrc";
				mLyricView.setLyric(mLrcFilePath, mCurrentMusicEntry.title);
				break;
			case MESSAGE_GET_PLISTENTRY:
				
				if (mPListEntryList.size() < mMusicManager.getPListSize()) {
					int left = mMusicManager.getPListSize() - mPListEntryList.size();
					mMusicManager.getPList(mPListEntryList.size() + 1, left >= 5 ? 5 : left, mOnPListEntryReadyListener);
				} else if (mPListEntryList.size() == mMusicManager.getPListSize()) {
					// sortMusicPList();
					storeMusicPList();
					mListView.setSelectionFromTop(mCurrentMusicEntry.index - 1, 200);
					if (mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}
				}
				break;
			}
		}
	};

	private void refreshUIWidget(MusicEntry entry) {
		Log.v(TAG, "===================shine===changeSongInfor==========================");
		System.out.println(entry.title);
		System.out.println(entry.artist);
		System.out.println(entry.album);
		System.out.println(entry.genre);
		mMusicTitleText.setText(entry.title);
		mMusicArtistText.setText(entry.artist);
		mMusicAblumText.setText(entry.album);
		mMusicGenreText.setText(entry.genre);
		mMusicMimeTypeText.setText(entry.mimeType);
		mMusicNameText.setText(entry.title);
		mArtistNameText.setText(entry.artist);
		mCurrentText.setText(Utils.showTime(mMusicManager.getCurrentPosition()));
		mDurationText.setText(Utils.showTime(mMusicManager.getDuration()));
		mSeekBar.setMax(mMusicManager.getDuration());
	}

	public class MusicPagerAdapter extends PagerAdapter {
		private List<View> mListViews;

		public MusicPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position), 0);
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	private class MusicListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<PListEntry> mList;

		private MusicListAdapter(Context context, List<PListEntry> list) {
			this.mList = list;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.remotemusiclist_item, null);
				holder = new ViewHolder();
				holder.sName = (TextView) convertView.findViewById(R.id.musicName);
				holder.sArtist = (TextView) convertView.findViewById(R.id.musicAritst);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.sName.setText(mList.get(position).name);
			holder.sArtist.setText(mList.get(position).artist);
		
			if ((mCurrentMusicEntry.index - 1) == position) {
				holder.sName.setSingleLine(true);
				holder.sName.setSelected(true);
				holder.sName.setEllipsize(TruncateAt.MARQUEE);
				convertView.setBackgroundColor(R.drawable.list_bg_selected);
			} else {
				holder.sName.setEllipsize(TruncateAt.END);
				convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			return convertView;
		}

		private final class ViewHolder {
			public TextView sName = null;
			public TextView sArtist = null;
		}
	}

	private void initLyric() {
		mHandler.removeMessages(MESSAGE_SET_LYRIC);
		mHandler.removeMessages(MESSAGE_REFRESH_LYRIC);
		if (Utils.hasExternalStoragePrivateFile(mCurrentMusicEntry.title + ".lrc")) {
			mHandler.sendEmptyMessage(MESSAGE_SET_LYRIC);
			mHandler.sendEmptyMessage(MESSAGE_REFRESH_LYRIC);
		} else {
			if (mCurrentMusicEntry.lyric) {
				if ((!Utils.checkExternalStorageAvailable()[0] || !Utils.checkExternalStorageAvailable()[1]) && isESSShowed) {
					isESSShowed = false;
					Utils.displayToast(R.string.notice_lyric_warn);
				}
				mMusicManager.getLyric(mOnLyricEntryReadyListener);
			} else {
				mHandler.sendEmptyMessage(MESSAGE_SET_LYRIC);
			}
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

	private class SortName implements Comparator<PListEntry> {
		Collator cmp = Collator.getInstance(java.util.Locale.CHINA);

		@Override
		public int compare(PListEntry o1, PListEntry o2) {
			if (cmp.compare(o1.name, o2.name) > 0) {
				return 1;
			} else if (cmp.compare(o1.name, o2.name) < 0) {
				return -1;
			}
			return 0;
		}
	}

	private void sortMusicPList() {
		List<PListEntry> mTempPListList = new ArrayList<PListEntry>();
		SortName comparator = new SortName();
		Collections.sort(mPListEntryList, comparator);
		short[] list = new short[mPListEntryList.size()];
		for (int i = 0; i < mPListEntryList.size(); i++) {
			list[i] = (short) mPListEntryList.get(i).index;
		}
		mMusicManager.setPList(list);
		for (short i : list) {
			for (PListEntry pe : mPListEntryList) {
				if (i == (short) pe.index) {
					mTempPListList.add(pe);
				}
			}
		}
		mPListEntryList.clear();
		for (PListEntry pe : mTempPListList) {
			mPListEntryList.add(pe);
			mMusicListAdapter.notifyDataSetChanged();
		}
		storeMusicPList();
		mListView.setSelectionFromTop(mCurrentMusicEntry.index - 1, 200);
	}

	private void storeMusicPList() {
		if (mSelectedMode == FeatureFlag.SDCARD) {
			/*
			Log.v(TAG, "===================shine===storeMusicPListStart====11======================");
			for(int i = 0;i<mPListEntryList.size();i++)
			{
				System.out.println("专辑："+mPListEntryList.get(i).album);
				System.out.println("歌手："+mPListEntryList.get(i).artist);
				System.out.println("流派:"+mPListEntryList.get(i).genre);
				System.out.println("格式:"+mPListEntryList.get(i).mimeType);
				System.out.println("文件名:"+mPListEntryList.get(i).name);
				System.out.println("显示名称:"+mPListEntryList.get(i).title);
				System.out.println("曲目号:"+mPListEntryList.get(i).index);
				System.out.println("------------------------------------");
			}
			Log.v(TAG, "===================shine===storeMusicPListEnd========111==================");
			*/
			Preferences.storeComplexDataInPreference(mActivity, Preferences.KEY_MUSIC_PLIST, mPListEntryList);
			
			
			
		} else if (mSelectedMode == FeatureFlag.UHOST) {
			Preferences.storeComplexDataInPreference(mActivity, Preferences.KEY_UHOST_PLIST, mPListEntryList);
		} else if (mSelectedMode == FuncMode.CRECORD) {
			Preferences.storeComplexDataInPreference(mActivity, Preferences.KEY_CRECORD_PLIST, mPListEntryList);
		} else if (mSelectedMode == FuncMode.URECORD) {
			Preferences.storeComplexDataInPreference(mActivity, Preferences.KEY_URECORD_PLIST, mPListEntryList);
		} else {
			
			mFolderEntryList = mBluzManager.getMusicFolderList();
			for (int i = 0; i < mFolderEntryList.size(); i++) {
				if (mSelectedMode == mFolderEntryList.get(i).value) {
					Preferences.storeComplexDataInPreference(mActivity, mFolderEntryList.get(i).name, mPListEntryList);
				}
			}
		}
	}
}
