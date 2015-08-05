package com.smart.clock;

import com.example.tabswitchdemo.R;
import com.example.tabswitchdemo.TabSwitchActivity;
import com.smart.impl.MyTimeController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SmartClock extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smart);

		// 使用后台进程去检测时间失败。不清楚原因

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		MyTimeController timeConn = new MyTimeController();

		if (timeConn.doesRequest(10, 1)) {
			finish();
		}

	}
}
