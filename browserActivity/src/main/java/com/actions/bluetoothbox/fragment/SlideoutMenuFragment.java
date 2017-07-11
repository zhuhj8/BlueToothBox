package com.actions.bluetoothbox.fragment;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.app.BrowserActivity;
import com.actions.ibluz.manager.BluzManagerData.FeatureFlag;
import com.actions.ibluz.manager.BluzManagerData.FolderEntry;
import com.actions.ibluz.manager.BluzManagerData.FuncMode;
import com.actions.ibluz.manager.IGlobalManager;

public class SlideoutMenuFragment extends Fragment implements OnClickListener {
    private static final String TAG = "SlideoutMenuFragment";
    public static final String FRAGMENT_TAG_A2DP = "a2dp";
    public static final String FRAGMENT_TAG_CARD = "card";
    public static final String FRAGMENT_TAG_UHOST = "uhost";
    public static final String FRAGMENT_TAG_RADIO = "radio";
    public static final String FRAGMENT_TAG_LINEIN = "linein";
    public static final String FRAGMENT_TAG_ALARM = "alarm";
    public static final String FRAGMENT_TAG_REC_CARDPLAYBACK = "rec_cardplayback";
    public static final String FRAGMENT_TAG_REC_UHOSTPLAYBACK = "rec_uhostplayback";
    public static final String FRAGMENT_TAG_CONNECTION = "connection";

    private static SlideoutMenuFragment mSlideoutMenuFragment;
    private BrowserActivity mActivity;

    private View mA2DPBtnLayout;
    private View mMusicPlayBtnLayout;
    private View mRadioBtnLayout;
    private View mAlarmClockBtnLayout;
    private View mConnectionBtnLayout;
    private View mUhostBtnLayout;
    private View mLineinBtnLayout;
    private View mCardPlayBackBtnLayout;
    private View mUhostPlayBackBtnLayout;
    // Expand
    private static int sFeature[] = new int[]{FeatureFlag.SDCARD, FeatureFlag.UHOST, FeatureFlag.FMRADIO, FeatureFlag.LINEIN, FeatureFlag.REC_PLAYBACK,
            FeatureFlag.ALARM, FeatureFlag.FOLDER};
    private static int[] sSpecialCatalogID = new int[]{R.id.specialCatalog1BtnLayout, R.id.specialCatalog2BtnLayout, R.id.specialCatalog3BtnLayout,
            R.id.specialCatalog4BtnLayout, R.id.specialCatalog5BtnLayout, R.id.specialCatalog6BtnLayout, R.id.specialCatalog7BtnLayout,
            R.id.specialCatalog8BtnLayout, R.id.specialCatalog9BtnLayout, R.id.specialCatalog10BtnLayout};
    private View mSpecialCatalog1BtnLayout;
    private View mSpecialCatalog2BtnLayout;
    private View mSpecialCatalog3BtnLayout;
    private View mSpecialCatalog4BtnLayout;
    private View mSpecialCatalog5BtnLayout;
    private View mSpecialCatalog6BtnLayout;
    private View mSpecialCatalog7BtnLayout;
    private View mSpecialCatalog8BtnLayout;
    private View mSpecialCatalog9BtnLayout;
    private View mSpecialCatalog10BtnLayout;
    private TextView mSpecialCatalog1Name;
    private TextView mSpecialCatalog2Name;
    private TextView mSpecialCatalog3Name;
    private TextView mSpecialCatalog4Name;
    private TextView mSpecialCatalog5Name;
    private TextView mSpecialCatalog6Name;
    private TextView mSpecialCatalog7Name;
    private TextView mSpecialCatalog8Name;
    private TextView mSpecialCatalog9Name;
    private TextView mSpecialCatalog10Name;
    private TextView[] mTextView;
    private View[] mView;
    private List<FolderEntry> mFolderEntryList;
    private boolean mSpecialCatalogSelected = false;

    private IGlobalManager mBluzManager;

    private String mFragmentTag = "";

    public static SlideoutMenuFragment getInstance() {
        if (mSlideoutMenuFragment == null) {
            return new SlideoutMenuFragment();
        }
        return mSlideoutMenuFragment;
    }

    public SlideoutMenuFragment() {
        mSlideoutMenuFragment = this;
    }

    public void cardMenuChanged(boolean visibility) {
        int visible = visibility ? View.VISIBLE : View.GONE;
        mMusicPlayBtnLayout.setVisibility(visible);
        boolean recVisible = mBluzManager.isFeatureSupport(FeatureFlag.REC_PLAYBACK);
        boolean folderVisible = mBluzManager.isFeatureSupport(FeatureFlag.FOLDER);
        int recShow = (recVisible && visibility) ? View.VISIBLE : View.GONE;
        int folderShow = (folderVisible && visibility) ? View.VISIBLE : View.GONE;
        mCardPlayBackBtnLayout.setVisibility(recShow);
        for (int i = 0; i < mFolderEntryList.size(); i++) {
            mView[i].setVisibility(folderShow);
        }

    }

    public void uhostMenuChanged(boolean visibility) {
        int visible = visibility ? View.VISIBLE : View.GONE;
        mUhostBtnLayout.setVisibility(visible);
        boolean recVisible = mBluzManager.isFeatureSupport(FeatureFlag.REC_PLAYBACK);
        int recShow = (recVisible && visibility) ? View.VISIBLE : View.GONE;
        mUhostPlayBackBtnLayout.setVisibility(recShow);
    }

    public void lineinMenuChanged(boolean visibility) {
        int visible = visibility ? View.VISIBLE : View.GONE;
        mLineinBtnLayout.setVisibility(visible);
    }

    public void setFeatureFilter() {
        mBluzManager = mActivity.getIGlobalManager();
        mFolderEntryList = mBluzManager.getMusicFolderList();
        int listSize = mFolderEntryList.size();
        for (int i = 0; i < listSize; i++) {
            mTextView[i].setText(mFolderEntryList.get(i).name);
        }
        for (int fe : sFeature) {
            switch (fe) {
                case FeatureFlag.SDCARD:
                    mMusicPlayBtnLayout.setVisibility(mBluzManager.isFeatureSupport(FeatureFlag.SDCARD) ? View.VISIBLE : View.GONE);
                    break;
                case FeatureFlag.UHOST:
                    mUhostBtnLayout.setVisibility(mBluzManager.isFeatureSupport(FeatureFlag.UHOST) ? View.VISIBLE : View.GONE);
                    break;
                case FeatureFlag.FMRADIO:
                    mRadioBtnLayout.setVisibility(mBluzManager.isFeatureSupport(FeatureFlag.FMRADIO) ? View.VISIBLE : View.GONE);
                    break;
                case FeatureFlag.LINEIN:
                    mLineinBtnLayout.setVisibility(mBluzManager.isFeatureSupport(FeatureFlag.LINEIN) ? View.VISIBLE : View.GONE);
                    break;
                case FeatureFlag.REC_PLAYBACK:
                    int recVisible = mBluzManager.isFeatureSupport(FeatureFlag.REC_PLAYBACK) ? View.VISIBLE : View.GONE;
                    mCardPlayBackBtnLayout.setVisibility(recVisible);
                    mUhostPlayBackBtnLayout.setVisibility(recVisible);
                    break;
                case FeatureFlag.ALARM:
                    mAlarmClockBtnLayout.setVisibility(mBluzManager.isFeatureSupport(FeatureFlag.ALARM) ? View.VISIBLE : View.GONE);
                    break;
                case FeatureFlag.FOLDER:
                    int folderVisible = mBluzManager.isFeatureSupport(FeatureFlag.FOLDER) ? View.VISIBLE : View.GONE;
                    for (int i = 0; i < listSize; i++) {
                        mView[i].setVisibility(folderVisible);
                    }
                    break;
            }
        }
    }

    /**
     * @param tag                    it point to normal mode,like music,radio,line in..
     * @param specialCataLogSelected while true,mean special catalog was been selected.
     * @param selectedMode           if parameter SpecialCataLog was selected,it become valid and
     *                               point to special catalog mode.
     */
    public void setMenuSelected(String tag, boolean specialCataLogSelected, int selectedMode) {
        if (!mSpecialCatalogSelected) {
            setMenuUnselected();
        }
        if (!specialCataLogSelected) {
            if (tag.equalsIgnoreCase(FRAGMENT_TAG_A2DP)) {
                mA2DPBtnLayout.setSelected(true);
            } else if (tag.equalsIgnoreCase(FRAGMENT_TAG_CARD) && !mSpecialCatalogSelected) {
                mMusicPlayBtnLayout.setSelected(true);
            } else if (tag.equalsIgnoreCase(FRAGMENT_TAG_UHOST)) {
                mUhostBtnLayout.setSelected(true);
            } else if (tag.equalsIgnoreCase(FRAGMENT_TAG_RADIO)) {
                mRadioBtnLayout.setSelected(true);
            } else if (tag.equalsIgnoreCase(FRAGMENT_TAG_LINEIN)) {
                mLineinBtnLayout.setSelected(true);
            } else if (tag.equalsIgnoreCase(FRAGMENT_TAG_ALARM)) {
                mAlarmClockBtnLayout.setSelected(true);
            } else if (tag.equalsIgnoreCase(FRAGMENT_TAG_CONNECTION)) {
                mConnectionBtnLayout.setSelected(true);
            } else if (tag.equalsIgnoreCase(FRAGMENT_TAG_REC_CARDPLAYBACK)) {
                mCardPlayBackBtnLayout.setSelected(true);
            } else if (tag.equalsIgnoreCase(FRAGMENT_TAG_REC_UHOSTPLAYBACK)) {
                mUhostPlayBackBtnLayout.setSelected(true);
            }
        } else {
            mFolderEntryList = mBluzManager.getMusicFolderList();
            for (int i = 0; i < mFolderEntryList.size(); i++) {
                if (selectedMode == mFolderEntryList.get(i).value) {
                    mView[i].setSelected(true);
                }
            }
        }
        mSpecialCatalogSelected = false;
    }

    private void setMenuUnselected() {
        mA2DPBtnLayout.setSelected(false);
        mMusicPlayBtnLayout.setSelected(false);
        mUhostBtnLayout.setSelected(false);
        mRadioBtnLayout.setSelected(false);
        mLineinBtnLayout.setSelected(false);
        mAlarmClockBtnLayout.setSelected(false);
        mConnectionBtnLayout.setSelected(false);
        mCardPlayBackBtnLayout.setSelected(false);
        mUhostPlayBackBtnLayout.setSelected(false);
        mSpecialCatalog1BtnLayout.setSelected(false);
        mSpecialCatalog2BtnLayout.setSelected(false);
        mSpecialCatalog3BtnLayout.setSelected(false);
        mSpecialCatalog4BtnLayout.setSelected(false);
        mSpecialCatalog5BtnLayout.setSelected(false);
        mSpecialCatalog6BtnLayout.setSelected(false);
        mSpecialCatalog7BtnLayout.setSelected(false);
        mSpecialCatalog8BtnLayout.setSelected(false);
        mSpecialCatalog9BtnLayout.setSelected(false);
        mSpecialCatalog10BtnLayout.setSelected(false);
    }

    @Override
    public void onClick(View v) {
        setMenuUnselected();
        v.setSelected(true);
        mSpecialCatalogSelected = false;
        if (v.getId() == R.id.connectionBtnLayout) {
            mFragmentTag = FRAGMENT_TAG_CONNECTION;
            mActivity.addFragmentToStack(new ConnectionFragment(), mFragmentTag);
        } else {
            int mode = FuncMode.A2DP;

            switch (v.getId()) {
                case R.id.a2dpBtnLayout://蓝牙推送 模式 点击
                    mode = FuncMode.A2DP;
                    break;
                case R.id.musicplayBtnLayout://卡播放 模式 点击
                    mode = FuncMode.CARD;
                    break;
                case R.id.uhostBtnLayout:
                    mode = FuncMode.USB;
                    break;
                case R.id.radioBtnLayout:
                    mode = FuncMode.RADIO;
                    break;
                case R.id.lineinBtnLayout:
                    mode = FuncMode.LINEIN;
                    break;
                case R.id.alarmclockBtnLayout:
                    mode = FuncMode.ALARM;
                    break;
                case R.id.recCardPlayBackBtnLayout:
                    mode = FuncMode.CRECORD;
                    break;
                case R.id.recUhostPlayBackBtnLayout:
                    mode = FuncMode.URECORD;
                    break;
                default:
                    // Special Catalog
                    for (int i = 0; i < mFolderEntryList.size(); i++) {
                        if (v.getId() == sSpecialCatalogID[i]) {
                            mode = mFolderEntryList.get(i).modeCommand;
                            mSpecialCatalogSelected = true;
                        }
                    }
                    break;
            }
            // Log.v(TAG, "setMode = " + mode);
            mActivity.setMode(mode);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BrowserActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_left_fragment, container, false);
        mA2DPBtnLayout = view.findViewById(R.id.a2dpBtnLayout);//蓝牙推送 选项
        mA2DPBtnLayout.setOnClickListener(this);
        mMusicPlayBtnLayout = view.findViewById(R.id.musicplayBtnLayout);
        mMusicPlayBtnLayout.setOnClickListener(this);
        mMusicPlayBtnLayout.setVisibility(View.GONE);
        mRadioBtnLayout = view.findViewById(R.id.radioBtnLayout);
        mRadioBtnLayout.setOnClickListener(this);
        mRadioBtnLayout.setVisibility(View.GONE);
        mLineinBtnLayout = view.findViewById(R.id.lineinBtnLayout);
        mLineinBtnLayout.setOnClickListener(this);
        mAlarmClockBtnLayout = view.findViewById(R.id.alarmclockBtnLayout);
        mAlarmClockBtnLayout.setOnClickListener(this);
        mConnectionBtnLayout = view.findViewById(R.id.connectionBtnLayout);
        mConnectionBtnLayout.setOnClickListener(this);
        mUhostBtnLayout = view.findViewById(R.id.uhostBtnLayout);
        mUhostBtnLayout.setOnClickListener(this);
        mUhostBtnLayout.setVisibility(View.GONE);
        mCardPlayBackBtnLayout = view.findViewById(R.id.recCardPlayBackBtnLayout);
        mCardPlayBackBtnLayout.setOnClickListener(this);
        mCardPlayBackBtnLayout.setVisibility(View.GONE);
        mUhostPlayBackBtnLayout = view.findViewById(R.id.recUhostPlayBackBtnLayout);
        mUhostPlayBackBtnLayout.setOnClickListener(this);
        mUhostPlayBackBtnLayout.setVisibility(View.GONE);
        // Expand
        mSpecialCatalog1Name = (TextView) view.findViewById(R.id.sc1_toolbox_title);
        mSpecialCatalog2Name = (TextView) view.findViewById(R.id.sc2_toolbox_title);
        mSpecialCatalog3Name = (TextView) view.findViewById(R.id.sc3_toolbox_title);
        mSpecialCatalog4Name = (TextView) view.findViewById(R.id.sc4_toolbox_title);
        mSpecialCatalog5Name = (TextView) view.findViewById(R.id.sc5_toolbox_title);
        mSpecialCatalog6Name = (TextView) view.findViewById(R.id.sc6_toolbox_title);
        mSpecialCatalog7Name = (TextView) view.findViewById(R.id.sc7_toolbox_title);
        mSpecialCatalog8Name = (TextView) view.findViewById(R.id.sc8_toolbox_title);
        mSpecialCatalog9Name = (TextView) view.findViewById(R.id.sc9_toolbox_title);
        mSpecialCatalog10Name = (TextView) view.findViewById(R.id.sc10_toolbox_title);
        mTextView = new TextView[]{mSpecialCatalog1Name, mSpecialCatalog2Name, mSpecialCatalog3Name, mSpecialCatalog4Name, mSpecialCatalog5Name,
                mSpecialCatalog6Name, mSpecialCatalog7Name, mSpecialCatalog8Name, mSpecialCatalog9Name, mSpecialCatalog10Name};
        mSpecialCatalog1BtnLayout = view.findViewById(R.id.specialCatalog1BtnLayout);
        mSpecialCatalog1BtnLayout.setOnClickListener(this);
        mSpecialCatalog1Name = (TextView) view.findViewById(R.id.sc1_toolbox_title);
        mSpecialCatalog1BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog2BtnLayout = view.findViewById(R.id.specialCatalog2BtnLayout);
        mSpecialCatalog2BtnLayout.setOnClickListener(this);
        mSpecialCatalog2BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog3BtnLayout = view.findViewById(R.id.specialCatalog3BtnLayout);
        mSpecialCatalog3BtnLayout.setOnClickListener(this);
        mSpecialCatalog3BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog4BtnLayout = view.findViewById(R.id.specialCatalog4BtnLayout);
        mSpecialCatalog4BtnLayout.setOnClickListener(this);
        mSpecialCatalog4BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog5BtnLayout = view.findViewById(R.id.specialCatalog5BtnLayout);
        mSpecialCatalog5BtnLayout.setOnClickListener(this);
        mSpecialCatalog5BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog6BtnLayout = view.findViewById(R.id.specialCatalog6BtnLayout);
        mSpecialCatalog6BtnLayout.setOnClickListener(this);
        mSpecialCatalog6BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog7BtnLayout = view.findViewById(R.id.specialCatalog7BtnLayout);
        mSpecialCatalog7BtnLayout.setOnClickListener(this);
        mSpecialCatalog7BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog8BtnLayout = view.findViewById(R.id.specialCatalog8BtnLayout);
        mSpecialCatalog8BtnLayout.setOnClickListener(this);
        mSpecialCatalog8BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog9BtnLayout = view.findViewById(R.id.specialCatalog9BtnLayout);
        mSpecialCatalog9BtnLayout.setOnClickListener(this);
        mSpecialCatalog9BtnLayout.setVisibility(View.GONE);
        mSpecialCatalog10BtnLayout = view.findViewById(R.id.specialCatalog10BtnLayout);
        mSpecialCatalog10BtnLayout.setOnClickListener(this);
        mSpecialCatalog10BtnLayout.setVisibility(View.GONE);
        mView = new View[]{mSpecialCatalog1BtnLayout, mSpecialCatalog2BtnLayout, mSpecialCatalog3BtnLayout, mSpecialCatalog4BtnLayout,
                mSpecialCatalog5BtnLayout, mSpecialCatalog6BtnLayout, mSpecialCatalog7BtnLayout, mSpecialCatalog8BtnLayout, mSpecialCatalog9BtnLayout,
                mSpecialCatalog10BtnLayout};
        return view;
    }
}
