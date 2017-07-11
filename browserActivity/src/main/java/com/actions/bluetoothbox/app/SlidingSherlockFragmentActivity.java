package com.actions.bluetoothbox.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityHelper;

public class SlidingSherlockFragmentActivity extends SherlockFragmentActivity implements SlidingActivityBase {

	private SlidingActivityHelper mHelper;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new SlidingActivityHelper(this);
		mHelper.onCreate(savedInstanceState);
	}

	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHelper.onPostCreate(savedInstanceState);
	}

	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v != null)
			return v;
		return mHelper.findViewById(id);
	}

	public void setContentView(int id) {
		setContentView(getLayoutInflater().inflate(id, null));
	}

	public void setContentView(View v) {
		setContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	public void setContentView(View v, LayoutParams params) {
		super.setContentView(v, params);
		mHelper.registerAboveContentView(v, params);
	}

	public void setBehindContentView(int id) {
		setBehindContentView(getLayoutInflater().inflate(id, null));
	}

	public void setBehindContentView(View v) {
		setBehindContentView(v, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	public void setBehindContentView(View v, LayoutParams params) {
		mHelper.setBehindContentView(v, params);
	}

	public SlidingMenu getSlidingMenu() {
		return mHelper.getSlidingMenu();
	}

	public void toggle() {
		mHelper.toggle();
	}

	public void setSlidingActionBarEnabled(boolean b) {
		mHelper.setSlidingActionBarEnabled(b);
	}

	@Override
	public void showContent() {
		mHelper.showContent();
	}

	@Override
	public void showMenu() {
		mHelper.showMenu();
	}

	@Override
	public void showSecondaryMenu() {
		mHelper.showSecondaryMenu();
	}
	
	
}