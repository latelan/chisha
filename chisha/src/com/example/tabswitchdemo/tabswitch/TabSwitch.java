package com.example.tabswitchdemo.tabswitch;

import android.view.View;

public interface TabSwitch {
	boolean isShow();
	View getView();
	void onShow(boolean retainScene);
	boolean processKeyBack();
	public void setAspectRatio(float ratio);
	public void setCurrentPage();
}
