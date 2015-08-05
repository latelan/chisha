package com.example.tabswitchdemo.tabswitch;

import com.example.tabswitchdemo.R;

import android.content.Context;
import android.view.LayoutInflater;

public class TabSwitchFactory {
	public static TabSwitch createTabSwitch(Context context,String type)
	{
		TabSwitch v = null;
		if(type.equals("rotate"))
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			v = (TabSwitchRotate)inflater.inflate(R.layout.tab_switch_rotate, null);
		}
		
		return v;
	}
}
