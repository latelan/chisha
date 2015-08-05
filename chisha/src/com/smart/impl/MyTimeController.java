package com.smart.impl;

import java.util.Date;

import android.util.Log;



public class MyTimeController extends Date {

	public boolean doesRequest(int dead_hour, int dead_minute) {
		int curHour = getHours();
		int curMin = getMinutes();
		
		if(curHour >= dead_hour && curMin > dead_minute) {
			return true;
		} else 
			return false;
		
	}
	
}
