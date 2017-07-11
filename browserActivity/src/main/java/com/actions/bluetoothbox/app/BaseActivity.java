package com.actions.bluetoothbox.app;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.fragment.SlideoutMenuFragment;
import com.actions.bluetoothbox.util.Utils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public abstract class BaseActivity extends SlidingSherlockFragmentActivity implements ActionBar.OnNavigationListener {
    private static final String TAG = "BaseActivity";
    protected SlideoutMenuFragment mSlideoutMenuFragment = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlideMenu();
        actionBarSetup();
    }

    protected void addSlideMenu() {
        setBehindContentView(R.layout.fragment_slideoutmenu);
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        mSlideoutMenuFragment = new SlideoutMenuFragment();
        fragmentTransaction.replace(R.id.fragment_slideoutmenu, mSlideoutMenuFragment);
        fragmentTransaction.commit();

        // Point screenSize = AppHelper.screenSize(getApplicationContext());
        SlidingMenu mSlidingMenu = getSlidingMenu();
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setBehindWidth((int) (230 * Utils.screenDensity(getApplicationContext())));// god
        // bless
        // gesture mode
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        setSlidingActionBarEnabled(false);
    }

    protected void actionBarSetup() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        // actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
        // actionBar.setSubtitle(R.string.ab_subtitle);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        return true;
    }

    public int getDpAsPxFromResource(int res) {
        return (int) getResources().getDimension(res);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();
    }

}
