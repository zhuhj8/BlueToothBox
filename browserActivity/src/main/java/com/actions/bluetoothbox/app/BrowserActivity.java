package com.actions.bluetoothbox.app;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.fragment.A2DPMusicFragment;
import com.actions.bluetoothbox.fragment.AlarmClockFragment;
import com.actions.bluetoothbox.fragment.ConnectionFragment;
import com.actions.bluetoothbox.fragment.LineInFragment;
import com.actions.bluetoothbox.fragment.RadioFragment;
import com.actions.bluetoothbox.fragment.RemoteMusicFragment;
import com.actions.bluetoothbox.fragment.SlideoutMenuFragment;
import com.actions.bluetoothbox.log.LogcatThread;
import com.actions.bluetoothbox.util.Constant;
import com.actions.bluetoothbox.util.Preferences;
import com.actions.bluetoothbox.util.Utils;
import com.actions.bluetoothbox.util.VerticalSeekBar;
import com.actions.ibluz.factory.BluzDeviceFactory;
import com.actions.ibluz.factory.IBluzDevice;
import com.actions.ibluz.factory.IBluzDevice.OnConnectionListener;
import com.actions.ibluz.manager.BluzManager;
import com.actions.ibluz.manager.BluzManagerData.CallbackListener;
import com.actions.ibluz.manager.BluzManagerData.CommandType;
import com.actions.ibluz.manager.BluzManagerData.DAEMode;
import com.actions.ibluz.manager.BluzManagerData.DAEOption;
import com.actions.ibluz.manager.BluzManagerData.EQMode;
import com.actions.ibluz.manager.BluzManagerData.FuncMode;
import com.actions.ibluz.manager.BluzManagerData.OnCustomCommandListener;
import com.actions.ibluz.manager.BluzManagerData.OnDAEChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnGlobalUIChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnHotplugChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnManagerReadyListener;
import com.actions.ibluz.manager.BluzManagerData.OnMessageListener;
import com.actions.ibluz.manager.IBluzManager;
import com.actions.ibluz.manager.IGlobalManager;
import com.iflytek.cloud.SpeechUtility;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class BrowserActivity extends BaseActivity implements OnAudioFocusChangeListener {
    private static final String TAG = "BrowserActivity";

    private IBluzDevice mBluzConnector;
    private BluzManager mBluzManager;
    private View mGlobalInfoLayout;
    private TextView mDeviceNameText;
    private SeekBar mVolumeSeekBar;
    private ImageView mBatteryImageView;
    private AudioManager mAudioManager;
    private ImageButton mSoundImageButton;
    private int mSeekBarVolume;
    private String mFragmentTag;
    private boolean mForeground = false;
    public boolean mMediaFree = true;
    private File mFile;
    private Fragment mComingFragment = null;
    private Context mContext;
    private int mMode = FuncMode.UNKNOWN;
    private boolean mFragmentStacked = false;
    private A2DPMusicFragment mA2DPMusicFragment;//音乐播放界面
    private View mEQDialogView;
    private View mEqSettingLayout;
    private Spinner mEqTypeSpinner;
    public int mEQMode = 0;
    private VerticalSeekBar[] mEqSeekBar;
    private List<int[]> mEqBandLevel = new ArrayList<int[]>();
    private String[] mEqTypes;
    private boolean mDaeChoose[] = new boolean[2];
    private String data = null;
    private LogcatThread mLogcatThread;
    private static final int[] STATE_CHARGE = {R.attr.state_incharge};
    private static final int[] STATE_NONE = {};
    private static final int[] mDialogRes = new int[]{R.array.array_dialog_normal, 0, 0, 0, 0, R.array.array_dialog_usbinsert, 0};
    private AlertDialog mUSBPlugDialog;
    private AlertDialog mAlarmDialog = null;
    private boolean mPausedByTransientLossOfFocus = false;
    private ComponentName mBluetoothBoxControl;
    private boolean isStopEQTrackingTouch = false;

    //暂停系统自带的播放器
    private void pauseMusic() {
        Intent freshIntent = new Intent();
        freshIntent.setAction("com.android.music.musicservicecommand.pause");
        freshIntent.putExtra("command", "pause");
        sendBroadcast(freshIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        pauseMusic(); //暂停系统自带的播放器
        Log.v(TAG, "===================shine=============================");
        //获取屏幕参数
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        Log.v(TAG, "density=" + displayMetrics.density + ", densityDpi=" + displayMetrics.densityDpi);
        //初始语音识别
        Log.v(TAG, "===================初始语音识别Start=============================");
        SpeechUtility.createUtility(BrowserActivity.this, "appid=" + getString(R.string.app_id));
        Log.v(TAG, "===================初始语音识别End=============================");
        //蓝牙主界面
        setContentView(R.layout.fragment_main);
        mContext = this;
        //获取默认协议蓝牙连接设备
        mBluzConnector = getBluzConnector();
        Log.v(TAG, "===================shine===1==========================");
        File cardFile = Environment.getExternalStorageDirectory();
        Log.e(TAG, "=======total========================" + cardFile.getTotalSpace());
        Log.e(TAG, "=======free========================" + cardFile.getFreeSpace());

        mLogcatThread = new LogcatThread();
        if (!cardFile.canWrite()) {
            String[] paths;
            String extSdCard = null;
            try {
                StorageManager sm = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
                paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
                String esd = Environment.getExternalStorageDirectory().getPath();

                for (int i = 0; i < paths.length; i++) {
                    if (paths[i].equals(esd)) {
                        continue;
                    }
                    File sdFile = new File(paths[i]);
                    if (sdFile.canWrite()) {
                        extSdCard = paths[i];
                        Log.i(TAG, "extsdcard:" + extSdCard);
                    }
                }
                data = extSdCard + "/" + this.getPackageName().toString() + "/" + this.getPackageName().toString() + ".log";
                mFile = new File(extSdCard + "/" + this.getPackageName().toString());
                mLogcatThread.setLogFilePath(data);
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                data = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName().toString() + "/"
                        + this.getPackageName().toString() + ".log";
                mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName().toString());
                mLogcatThread.setLogFilePath(data);
            }
        } else {
            data = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName().toString() + "/"
                    + this.getPackageName().toString() + ".log";
            mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName().toString());
            mLogcatThread.setLogFilePath(data);
        }

        Log.i(TAG, "data:" + data);

        if (!mFile.isDirectory()) {
            mFile.mkdir();
        }
        if (mBluzConnector == null) {//判断是否支持蓝牙
            showNotSupportedDialog();
        }

        if (mFile.canWrite() && mFile.canRead()) {
            mLogcatThread.start();
        }

        mGlobalInfoLayout = findViewById(R.id.globalInfoLayout);//
        mDeviceNameText = (TextView) findViewById(R.id.deviceName_tv);//设备名称
        mSoundImageButton = (ImageButton) findViewById(R.id.mute);//无声按钮
        mSoundImageButton.setImageResource(R.drawable.selector_muteon_button);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.volume);//进度条
        mBatteryImageView = (ImageView) findViewById(R.id.battery);//电池图标

        Utils.setContext(mContext);
        Utils.setAlphaForView(mGlobalInfoLayout, 0.5f);
        mVolumeSeekBar.setEnabled(false);
        mSoundImageButton.setEnabled(false);

        mForeground = false;
        initFragment();
        registerExternalStorageListener();
        actionBarSetup();
        avrcpSetup();
        Log.v(TAG, "===================shine==设置回调监听==========================");
        //设置连接回调监听
        mBluzConnector.setOnConnectionListener(mOnConnectionListener);
    }

    private void avrcpSetup() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);//获得系统播放管理器
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(Constant.MusicPlayControl.SERVICECMD);
        commandFilter.addAction(Constant.MusicPlayControl.TOGGLEPAUSE_ACTION);
        commandFilter.addAction(Constant.MusicPlayControl.PAUSE_ACTION);
        commandFilter.addAction(Constant.MusicPlayControl.NEXT_ACTION);
        commandFilter.addAction(Constant.MusicPlayControl.PREVIOUS_ACTION);
        registerReceiver(mIntentReceiver, commandFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        stopBackgroundMusic();
        if (mLogcatThread != null && mLogcatThread.getThreadState() == LogcatThread.STATE_DONE) {
            if (mFile.canWrite() && mFile.canRead() && mMediaFree) {
                mLogcatThread = null;
                mLogcatThread = new LogcatThread();
                mLogcatThread.setAppend();
                mLogcatThread.setLogFilePath(data);
                mLogcatThread.start();
            }
        }
    }

    @Override
    public void onResumeFragments() {
        super.onResumeFragments();
        Log.v(TAG, "onResumeFragments");

        mForeground = true;
        if (mComingFragment != null) {
            initFragment(mComingFragment, false, 0);
        }

        setBluzManagerForeground();
    }

    @Override
    public void finish() {
        super.finish();
        Log.v(TAG, "finish");
        /** android NOT guarantee that onDestroy() follows finish() */
        releaseAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        Log.v(TAG, "==================hu=============================");
        mForeground = false;
        if (mLogcatThread != null && mLogcatThread.isAlive()) {
            mLogcatThread.setState(LogcatThread.STATE_DONE);
            Log.v(TAG, "STATE_DONE!");
        }

        setBluzManagerForeground();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        if (!isFinishing()) {
            Log.i(TAG, "not isFinishing");
            /**
             * consider the situation the normal destroy without finish(), when
             * the app is in background
             */
            releaseAll();
        }
    }

    private void setBluzManagerForeground() {
        if (mBluzManager != null) {
            mBluzManager.setForeground(mForeground);
        }
    }

    private void showLowElectricityRemindDialog() { //音响电量不足
        Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);// ic_dialog_alert_holo_light
        builder.setTitle(R.string.charge_warm);
        builder.setMessage(R.string.charge_tip);
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void replaceFragment(Fragment fragment, String tag) {
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP) != null
                    && tag.equals(SlideoutMenuFragment.FRAGMENT_TAG_ALARM)) {
                ft.replace(R.id.main_fragment, fragment, tag);
                ft.addToBackStack(tag);
            } else {
                if (fragment != mA2DPMusicFragment && !tag.equals(SlideoutMenuFragment.FRAGMENT_TAG_ALARM)) {
                    if (mA2DPMusicFragment != null) {
                        mA2DPMusicFragment.pause();
                    }
                }

                ft.replace(R.id.main_fragment, fragment, tag);
            }

            ft.commit();
            showContent();

            mFragmentStacked = false;
        }
    }

    public void addFragmentToStack(Fragment fragment, String tag) {
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_fragment, fragment, tag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(tag);
            ft.commit();
            showContent();

            mFragmentStacked = true;
        }
    }

    public void setMode(int mode) {
        if (mode == mMode) {
            toggle();
            if (mFragmentStacked) {
                getSupportFragmentManager().popBackStack();
                mFragmentStacked = false;
            }
        } else {
            mBluzManager.setMode(mode);
        }
    }

    private void releaseAll() {
        releaseReceiver();
        releaseManager();
        releaseDevice();
    }

    private void releaseReceiver() {
        mAudioManager.abandonAudioFocus(this);
        mAudioManager.unregisterMediaButtonEventReceiver(mBluetoothBoxControl);
        unregisterReceiver(mIntentReceiver);
        unregisterReceiver(mUnmountReceiver);
    }

    private void releaseDevice() {
        if (mBluzConnector != null) {
            mBluzConnector.setOnConnectionListener(null);
            mBluzConnector.release();
            mBluzConnector = null;
        }
    }

    private void releaseManager() {
        if (mBluzManager != null) {
            mBluzManager.setOnGlobalUIChangedListener(null);
            mBluzManager.release();
            mBluzManager = null;
        }
    }

    private long exitTime = 0;

    @Override
    public void onBackPressed() {//返回按钮处理
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, R.string.message_press_quit, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public SlidingMenu getSlidingMenu() {
        return super.getSlidingMenu();
    }

    public void setBluzDeviceChanged() {
        Log.i(TAG, "setBluzDeviceChanged");
        stopMusicPlayer();
        toggleGlobalInfo(true);
        createBluzManager();
    }

    private void createBluzManager() {
        if (mBluzConnector == null) {
            mBluzManager = null;
        } else {
            mBluzManager = new BluzManager(mContext, mBluzConnector, new OnManagerReadyListener() {

                @Override
                public void onReady() {
                    mBluzManager.setSystemTime();
                    // fix when auto-connect in background, frequently
                    // data-exchange
                    // will interfere with phone call
                    mBluzManager.setForeground(mForeground);
                    mBluzManager.setOnMessageListener(new OnMessageListener() {

                        @Override
                        public void onToast(int messageId) {
                            Toast.makeText(mContext, 0, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onDialog(int id, int messageId, CallbackListener listener) {
                            Log.v(TAG, "onDialog show");
                            final CallbackListener callback = listener;
                            callback.onReceive(5);
                            String[] res = getResources().getStringArray(mDialogRes[id]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle(res[0]);
                            builder.setPositiveButton(res[1], new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    callback.onPositive();
                                }
                            });
                            builder.setNegativeButton(res[2], new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    callback.onNegative();
                                }
                            });

                            builder.setMessage(getResources().getStringArray(R.array.array_message_body)[messageId]);
                            builder.setCancelable(false);
                            mUSBPlugDialog = builder.create();
                            mUSBPlugDialog.show();
                        }

                        @Override
                        public void onCancel() {
                            Log.v(TAG, "onCancel");
                            if (mUSBPlugDialog != null && mUSBPlugDialog.isShowing()) {
                                mUSBPlugDialog.dismiss();
                            }
                        }
                    });

                    //热插拨
                    mBluzManager.setOnHotplugChangedListener(new OnHotplugChangedListener() {

                        @Override
                        public void onUhostChanged(boolean visibility) {
                            mSlideoutMenuFragment.uhostMenuChanged(visibility);
                        }

                        @Override
                        public void onLineinChanged(boolean visibility) {
                            mSlideoutMenuFragment.lineinMenuChanged(visibility);
                        }

                        @Override
                        public void onCardChanged(boolean visibility) {
                            mSlideoutMenuFragment.cardMenuChanged(visibility);
                        }

                    });

                    mBluzManager.setOnGlobalUIChangedListener(new OnGlobalUIChangedListener() {

                        @Override
                        public void onVolumeChanged(int volume, boolean mute) {
                            mVolumeSeekBar.setProgress(volume);
                            if (mute) {
                                mSoundImageButton.setImageResource(R.drawable.selector_muteoff_button);
                                mVolumeSeekBar.setEnabled(false);
                            } else {
                                mSoundImageButton.setImageResource(R.drawable.selector_muteon_button);
                                mVolumeSeekBar.setEnabled(true);
                            }
                        }

                        @Override
                        public void onModeChanged(int mode) {
                            mMode = mode;
                            // Log.v(TAG, "onModeChanged = " + mode);
                            Fragment newContentFragment = null;
                            mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_A2DP;
                            boolean specialCatalogSelected = false;
                            switch (mode) {
                                case FuncMode.A2DP:
                                    if (mA2DPMusicFragment == null) {
                                        mA2DPMusicFragment = new A2DPMusicFragment();
                                    }
                                    newContentFragment = mA2DPMusicFragment;
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_A2DP;
                                    break;

                                case FuncMode.USB:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_UHOST;
                                    break;

                                case FuncMode.RADIO:
                                    newContentFragment = new RadioFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_RADIO;
                                    break;

                                case FuncMode.LINEIN:
                                    newContentFragment = new LineInFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_LINEIN;
                                    break;

                                case FuncMode.ALARM:
                                    newContentFragment = new AlarmClockFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_ALARM;
                                    break;

                                case FuncMode.CARD:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_CARD;
                                    break;

                                case FuncMode.CRECORD:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_REC_CARDPLAYBACK;
                                    break;

                                case FuncMode.URECORD:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_REC_UHOSTPLAYBACK;
                                    break;

                                default:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_CARD;
                                    specialCatalogSelected = true;
                                    break;
                            }
                            initFragment(newContentFragment, specialCatalogSelected, mode);
                        }

                        @Override
                        public void onEQChanged(int eq) {
                            if (eq != EQMode.UNKNOWN) {
                                mEQMode = eq;
                                Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQUALIZER_TYPE, mEQMode);
                            }
                        }

                        @Override
                        public void onBatteryChanged(int battery, boolean incharge) {
                            if (battery == 0 && !incharge) {
                                showLowElectricityRemindDialog();
                            }
                            mBatteryImageView.setImageLevel(battery);
                            mBatteryImageView.setImageResource(R.drawable.battery);
                            mBatteryImageView.setImageState(incharge ? STATE_CHARGE : STATE_NONE, true);// incharge
                        }
                    });
                    mBluzManager.setOnDAEChangedListener(new OnDAEChangedListener() {

                        @Override
                        public void onDAEModeChanged(int daeMode) {
                            if (daeMode != DAEMode.UNKNOWN) {
                                Preferences.setPreferences(mContext, Preferences.KEY_DAE_MODE, daeMode);
                            }
                        }

                        @Override
                        public void onDAEOptionChanged(int daeOption) {
                            if (daeOption != DAEOption.UNKNOWN) {
                                Preferences.setPreferences(mContext, Preferences.KEY_DAE_OPTION, daeOption);
                            }
                        }

                    });

                    // Initializes the state of whether to support the feature
                    mSlideoutMenuFragment.setFeatureFilter();
                    getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                }
            });
        }
    }

    /**
     * 状态布局开关
     *
     * @param turnOn
     */
    private void toggleGlobalInfo(boolean turnOn) {
        if (turnOn) {
            Utils.setAlphaForView(mGlobalInfoLayout, 1.0f);
            mVolumeSeekBar.setEnabled(true);
            mSoundImageButton.setEnabled(true);
            mSoundImageButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mBluzManager.switchMute();
                }
            });

            mVolumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    System.out.println("============vol========" + mSeekBarVolume);
                    mBluzManager.setVolume(mSeekBarVolume);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        // We're not interested in programmatically generated
                        // changes to
                        // the progress bar's position.
                        return;
                    }

                    mSeekBarVolume = progress;
                }
            });
        } else {
            Utils.setAlphaForView(mGlobalInfoLayout, 0.5f);
            mVolumeSeekBar.setProgress(0);
            mVolumeSeekBar.setOnSeekBarChangeListener(null);
            mVolumeSeekBar.setEnabled(false);
            mSoundImageButton.setImageResource(R.drawable.selector_muteon_button);
            mSoundImageButton.setEnabled(false);
            mSoundImageButton.setOnClickListener(null);
            mBatteryImageView.setImageLevel(0);
            mBatteryImageView.setImageResource(R.drawable.battery);
            mBatteryImageView.setImageState(STATE_NONE, true);
        }
    }

    private void initFragment() {
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_CONNECTION;
        initFragment(new ConnectionFragment(), false, 0);
    }

    private void initFragment(Fragment fragment, boolean specialCatalogSelected, int selectedMode) {
        if (mForeground) {
            mSlideoutMenuFragment.setMenuSelected(mFragmentTag, specialCatalogSelected, selectedMode);
            replaceFragment(fragment, mFragmentTag);
            mComingFragment = null;
        } else {
            mComingFragment = fragment;
        }
    }

    private void setBluzDeviceDisconnected() {//蓝牙断开连接
        Log.i(TAG, "setBluzDeviceDisconnected");
        stopMusicPlayer();//停止播放
        toggleGlobalInfo(false);
        releaseManager();
        initFragment();
    }

    public String getFragmentTag() {
        return mFragmentTag;
    }

    public IBluzManager getIBluzManager() {
        return mBluzManager;
    }

    public IGlobalManager getIGlobalManager() {
        return mBluzManager;
    }

    /**
     * EQ调节界面
     */
    public void showEQSettingDialog() {
        initEQDialogView();
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.menu_title_sound).setView(mEQDialogView)
                .setPositiveButton(R.string.action_submit, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQUALIZER_TYPE, mEQMode);
                if (mEQMode == EQMode.USER) {
                    int value = mEqBandLevel.get(mEQMode - 1)[0];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_80, value);
                    value = mEqBandLevel.get(mEQMode - 1)[1];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_200, value);
                    value = mEqBandLevel.get(mEQMode - 1)[2];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_500, value);
                    value = mEqBandLevel.get(mEQMode - 1)[3];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_1K, value);
                    value = mEqBandLevel.get(mEQMode - 1)[4];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_4K, value);
                    value = mEqBandLevel.get(mEQMode - 1)[5];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_8K, value);
                    value = mEqBandLevel.get(mEQMode - 1)[6];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_16K, value);
                }
            }
        });
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = Utils.screenSize(mContext).x;
        // params.height = 600 ;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    public void showDAEDialog() {
        int daeOption = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_DAE_OPTION, 0).toString());
        switch (daeOption) {
            case DAEOption.VBASS:
                mDaeChoose[0] = true;
                mDaeChoose[1] = false;
                break;
            case DAEOption.TREBLE:
                mDaeChoose[0] = false;
                mDaeChoose[1] = true;
                break;
            case DAEOption.BOTH:
                mDaeChoose[0] = true;
                mDaeChoose[1] = true;
                break;
            default:
                mDaeChoose[0] = false;
                mDaeChoose[1] = false;
                break;
        }
        AlertDialog daeDialog = new AlertDialog.Builder(this).setTitle(R.string.menu_item_daesound)
                .setMultiChoiceItems(R.array.array_dae_type, mDaeChoose, new OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (which) {
                            case 0:
                                mDaeChoose[0] = isChecked;
                                break;
                            case 1:
                                mDaeChoose[1] = isChecked;
                                break;
                        }
                    }
                }).setPositiveButton(R.string.action_submit, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int daeOption = DAEOption.UNKNOWN;
                        if (mDaeChoose[0] && mDaeChoose[1]) {
                            daeOption = DAEOption.BOTH;
                        } else if (mDaeChoose[0]) {
                            daeOption = DAEOption.VBASS;
                        } else if (mDaeChoose[1]) {
                            daeOption = DAEOption.TREBLE;
                        } else {
                            daeOption = DAEOption.NONE;
                        }
                        Preferences.setPreferences(mContext, Preferences.KEY_DAE_OPTION, String.valueOf(daeOption));
                        mBluzManager.setDAEOption(daeOption);
                        dialog.cancel();
                    }
                }).create();
        daeDialog.show();
    }

    private void initEQDialogView() {
        LayoutInflater factory = LayoutInflater.from(BrowserActivity.this);
        mEQDialogView = factory.inflate(R.layout.dialog_eqsetting, null);
        mEqTypeSpinner = (Spinner) mEQDialogView.findViewById(R.id.eqTypeSpinner);
        mEqSettingLayout = mEQDialogView.findViewById(R.id.eqSettingLayout);
        mEqSeekBar = new VerticalSeekBar[7];
        mEqSeekBar[0] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency80HzBar);
        mEqSeekBar[1] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency200HzBar);
        mEqSeekBar[2] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency500HzBar);
        mEqSeekBar[3] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency1KHzBar);
        mEqSeekBar[4] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency4KHzBar);
        mEqSeekBar[5] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency8KHzBar);
        mEqSeekBar[6] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency16KHzBar);
        for (int i = 0; i < mEqSeekBar.length; i++) {
            mEqSeekBar[i].setOnSeekBarChangeListener(mSeekBarChangeListener);//EQ调节监听
        }
        mEqBandLevel.clear();
        // int[] normalLevel =
        // getResources().getIntArray(R.array.array_eq_normal);
        // mEqBandLevel.add(normalLevel);
        int[] jazzLevel = getResources().getIntArray(R.array.array_eq_jazz);
        mEqBandLevel.add(jazzLevel);
        int[] popLevel = getResources().getIntArray(R.array.array_eq_pop);
        mEqBandLevel.add(popLevel);
        int[] classicLevel = getResources().getIntArray(R.array.array_eq_classic);
        mEqBandLevel.add(classicLevel);
        int[] softLevel = getResources().getIntArray(R.array.array_eq_soft);
        mEqBandLevel.add(softLevel);
        int[] dbbLevel = getResources().getIntArray(R.array.array_eq_dbb);
        mEqBandLevel.add(dbbLevel);
        int[] rockLevel = getResources().getIntArray(R.array.array_eq_rock);
        mEqBandLevel.add(rockLevel);

        int[] userLevel = new int[7];
        userLevel[0] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_80, 0).toString());
        userLevel[1] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_200, 0).toString());
        userLevel[2] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_500, 0).toString());
        userLevel[3] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_1K, 0).toString());
        userLevel[4] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_4K, 0).toString());
        userLevel[5] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_8K, 0).toString());
        userLevel[6] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_16K, 0).toString());
        mEqBandLevel.add(userLevel);

        mEqTypes = getResources().getStringArray(R.array.array_eq_type);
        ArrayAdapter<String> eqTypeAdapter = new ArrayAdapter<String>(mContext, R.layout.eq_spinner_item, mEqTypes);
        eqTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEqTypeSpinner.setAdapter(eqTypeAdapter);
        mEqTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEQMode = position + 1;// position
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                // mBluzManager.setEQMode(mEQMode);
                if (mEQMode == EQMode.USER) {
                    System.out.println("================EQ=============");
                    mBluzManager.setEQParam(mEqBandLevel.get(mEQMode - 1));
                } else {
                    mBluzManager.setDAEEQMode(mEQMode);
                }
                equalizerUpdateDisplay();//更新均衡器显示
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mEQMode = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQUALIZER_TYPE, 0).toString());
        mEqTypeSpinner.setSelection(mEQMode - 1);// mEQMode
    }

    private void equalizerUpdateDisplay() {
        int[] level = mEqBandLevel.get(mEQMode - 1);// mEQMode

        for (int i = 0; i < level.length; i++) {
            mEqSeekBar[i].setProgressAndThumb(level[i] + 12);
        }

        if (mEQMode == EQMode.USER) {
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

    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar verticalSeekBar, int progress, boolean fromUser) {
            if (mEQMode != EQMode.USER)
                return;
            int value = verticalSeekBar.getProgress() - 12;
            switch (verticalSeekBar.getId()) {
                case R.id.frequency80HzBar:
                    mEqBandLevel.get(mEQMode - 1)[0] = value;// EQMode.USER
                    System.out.println("=====80==EQ====" + value);
                    break;
                case R.id.frequency200HzBar:
                    mEqBandLevel.get(mEQMode - 1)[1] = value;
                    System.out.println("=====200==EQ====" + value);
                    break;
                case R.id.frequency500HzBar:
                    mEqBandLevel.get(mEQMode - 1)[2] = value;
                    System.out.println("=====500==EQ====" + value);
                    break;
                case R.id.frequency1KHzBar:
                    mEqBandLevel.get(mEQMode - 1)[3] = value;
                    System.out.println("=====1k==EQ====" + value);
                    break;
                case R.id.frequency4KHzBar:
                    mEqBandLevel.get(mEQMode - 1)[4] = value;
                    System.out.println("=====4k==EQ====" + value);
                    break;
                case R.id.frequency8KHzBar:
                    mEqBandLevel.get(mEQMode - 1)[5] = value;
                    System.out.println("=====8k==EQ====" + value);
                    break;
                case R.id.frequency16KHzBar:
                    mEqBandLevel.get(mEQMode - 1)[6] = value;
                    System.out.println("=====16k==EQ====" + value);
                    break;
            }
            /**
             * EQ调节代码***************
             */
            if (mEQMode == EQMode.USER && isStopEQTrackingTouch) {
                System.out.println("=====mogogogo====");
                int arrEq[] = new int[6];
                arrEq = mEqBandLevel.get(mEQMode - 1);
                for (int i = 0; i < arrEq.length; i++)
                    System.out.println("=====eqV====" + arrEq[i]);
                mBluzManager.setEQParam(mEqBandLevel.get(mEQMode - 1));//发送EQ参数
                //mBluzManager.setDAEEQParam(mEqBandLevel.get(mEQMode - 1));
                isStopEQTrackingTouch = false;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar verticalSeekBar) {
            // TODO Auto-generated method stub
            Log.v(TAG, "onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar verticalSeekBar) {
            Log.v(TAG, "onStopTrackingTouch");
            isStopEQTrackingTouch = true;
        }
    };

    //快进，快退操作
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("command");
            A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
            if (fragment != null) {
                if (cmd.equals("next")) {
                    fragment.playNextMusic();
                } else if (cmd.equals("pre")) {
                    fragment.playPreviousMusic();
                } else if (cmd.equals("play") || cmd.equals("pause") || cmd.equals("play-pause")) {
                    fragment.controlPauseResume();
                } else if (cmd.equals("fastforward")) {
                    fragment.doFastForward();
                } else if (cmd.equals("rewind")) {
                    fragment.doRewind();
                }
            }
        }
    };

    private void stopBackgroundMusic() {
        int focus = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (focus != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.i(TAG, "Audio focus request failed!");
        }

        mBluetoothBoxControl = new ComponentName(getPackageName(), BluetoothBoxControl.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mBluetoothBoxControl);
    }

    //实现
    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.v(TAG, " onAudioFocusChange: " + focusChange);
        A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS");
                if (fragment != null) {
                    if (fragment.isPlaying()) {
                        mPausedByTransientLossOfFocus = false;
                    }
                    fragment.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                if (fragment != null) {
                    if (fragment.isPlaying()) {
                        mPausedByTransientLossOfFocus = true;
                    }
                    fragment.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (fragment != null) {
                    // fragment.setFadeUpToDown(true);
                    if (fragment.isPlaying()) {
                        mPausedByTransientLossOfFocus = true;
                    }
                    fragment.pause();
                }

                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                if (fragment != null) {
                    if (!fragment.isPlaying() && mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        fragment.doPauseResume();
                    }
                    // else {
                    // fragment.setFadeUpToDown(false);
                    // }
                }
                break;
            default:
                Log.e(TAG, "Unknown audio focus change code");
        }
    }

    private OnConnectionListener mOnConnectionListener = new OnConnectionListener() {
        @Override
        public void onConnected(BluetoothDevice device) {
            //获取蓝牙名
            mDeviceNameText.setText((device == null) ? null : device.getName());
            setBluzDeviceChanged(); //蓝牙设备变化
            stopBackgroundMusic();
        }

        @Override
        public void onDisconnected(BluetoothDevice device) {
            mDeviceNameText.setText(null);
            setBluzDeviceDisconnected();
        }
    };

    private void showNotSupportedDialog() { //蓝牙不支持
        Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.dialog_title_warning);
        builder.setMessage(R.string.notice_bluetooth_not_supported);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        builder.create().show();
    }

    private void stopMusicPlayer() {
        A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
        if (fragment != null) {
            fragment.release();
        }
    }

    private BroadcastReceiver mUnmountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "actions :" + action);
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                if (mLogcatThread.isAlive()) {
                    mLogcatThread.setState(LogcatThread.STATE_DONE);
                    Log.v(TAG, "STATE_DONE!");
                }
                mMediaFree = false;
                A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
                if (fragment != null) {
                    fragment.doPauseResume();
                    fragment.release();
                    Constant.MusicPlayData.myMusicList.clear();
                    fragment.initTextShow();
                    fragment.updateListView();
                    Log.i(TAG, "i call fragment list clear ");
                    Toast.makeText(context, R.string.music_storge_busy, Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mMediaFree = true;
                A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
                if (fragment != null) {
                    // fragment.getMusicList();
                    try {
                        Thread.sleep(2000); // waitting 2s,for auto
                        // refresh music list .
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    fragment.updateList();
                    Log.i(TAG, "i call fragment updateList");
                }
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
                if (fragment != null) {
                    Log.i(TAG, " i get ACTION_MEDIA_SCANNER_FINISHED");
                    fragment.updateList();
                }
            }

        }
    };

    private void registerExternalStorageListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter.addDataScheme("file");
        registerReceiver(mUnmountReceiver, filter);
    }

    public int getCurrentMode() {
        return mMode;
    }

    public void showDisconncetDialog() {
        Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.dialog_title_warning);
        builder.setMessage(getResources().getString(R.string.dialog_message_disconncect));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mBluzConnector.disconnect(mBluzConnector.getConnectedDevice());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public int getCurrentDAEMode() {
        return Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_DAE_MODE, 0).toString());
    }

    //menu选择事件
    public void menuItemSelected(Menu menu, int itemIdSelected) {
        switch (itemIdSelected) {
            case R.id.nodigitalsoundeffect:
                mBluzManager.setDAENoDigitalSound();
                // mBluzManager.setEQMode(0);
                break;
            case R.id.eqsoundeffect:
                // mBluzManager.setDAEEQMode(mEQMode);
                showEQSettingDialog();
                break;
            case R.id.daesoundeffect:
                showDAEDialog();
                break;
        }
        if (menu != null) {
            com.actionbarsherlock.view.MenuItem noneItem, eqItem, daeItem;
            noneItem = menu.findItem(R.id.nodigitalsoundeffect);
            eqItem = menu.findItem(R.id.eqsoundeffect);
            daeItem = menu.findItem(R.id.daesoundeffect);
            noneItem.setCheckable(false);
            eqItem.setCheckable(false);
            daeItem.setCheckable(false);
            int currentDAEMode = getCurrentDAEMode();
            switch (currentDAEMode) {
                case DAEMode.NONE:
                    noneItem.setCheckable(true);
                    noneItem.setChecked(true);
                    break;
                case DAEMode.EQ:
                    eqItem.setCheckable(true);
                    eqItem.setChecked(true);
                    break;
                case DAEMode.DAE:
                    daeItem.setCheckable(true);
                    daeItem.setChecked(true);
                    break;
                default:
                    break;
            }
        }
    }

    public void showAlarmDialog(AlertDialog adg) {
        this.mAlarmDialog = adg;
        if (mAlarmDialog != null) {
            mAlarmDialog.show();
        }
    }

    public void dismissAlarmDialog() {
        if (mAlarmDialog != null && mAlarmDialog.isShowing()) {
            mAlarmDialog.dismiss();
        }
    }

    //获取默认协议蓝牙连接设备
    public IBluzDevice getBluzConnector() {
        if (mBluzConnector == null) {
            mBluzConnector = BluzDeviceFactory.getDevice(this);
        }

        return mBluzConnector;
    }
}
