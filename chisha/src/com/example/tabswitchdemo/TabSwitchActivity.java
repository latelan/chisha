package com.example.tabswitchdemo;

import com.example.tabswitchdemo.tabswitch.TabSwitchFactory;

import com.example.tabswitchdemo.tabswitch.TabSwitch;

import java.io.File;
import java.io.IOException;

import com.example.tabswitchdemo.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.smart.clock.SmartClock;
import com.smart.impl.*;

public class TabSwitchActivity extends Activity {

	private Dialog mTabSwitchDialog;
	private FrameLayout mTabSwitchViewShim;
	private TabSwitch mTabSwitch;
	private ImageView setup_image;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		setup_image = (ImageView) this.findViewById(R.id.setup_image); 
		AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);  
        anima.setDuration(1000);// 设置动画显示时间  
        setup_image.startAnimation(anima);  
        anima.setAnimationListener(new AnimationImpl());  
  
		try {
			if(makeAppDir("chisha")) {
//				Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "启动失败，路径加载有问题", Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MyTimeController timeCon = new MyTimeController();

		// 10:00之前
	/*	if (!timeCon.doesRequest(10, 0)) {
			Intent intent = new Intent(TabSwitchActivity.this, SmartClock.class);
			startActivity(intent);
		} else {
			
		}*/

		
		if (mTabSwitch == null) {
			createTabSwitch();
		}
		showTabSwitch(true);
	

	}
	
	
	private class AnimationImpl implements AnimationListener {  
		  
        @Override  
        public void onAnimationStart(Animation animation) {  
        	setup_image.setBackgroundResource(R.drawable.setup_bg);  
        }  
  
        @Override  
        public void onAnimationEnd(Animation animation) {  
            skip(); // 动画结束后跳转到别的页面  
        }  
  
        @Override  
        public void onAnimationRepeat(Animation animation) {  
  
        }  
  
    }  
  
    private void skip() {  
    	mTabSwitchDialog.show();

		mTabSwitch.onShow(true);
		Toast.makeText(this, "欢迎你使用吃啥", Toast.LENGTH_SHORT).show();
    }  
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createTabSwitch() {

		mTabSwitch = TabSwitchFactory.createTabSwitch(this, "rotate");
	}

	public void showTabSwitch(boolean retainScene) {
		View view = mTabSwitch.getView();
		if (view != null)
			view.setVisibility(View.VISIBLE);

		mTabSwitchViewShim = new FrameLayout(this);
		FrameLayout.LayoutParams layoutparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		mTabSwitchViewShim.setLayoutParams(layoutparams);
		mTabSwitchViewShim.addView(view);

		mTabSwitchDialog = new Dialog(this, R.style.simple_bubble_message_dialog);
		mTabSwitchDialog.setContentView(mTabSwitchViewShim);

		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		Window win = mTabSwitchDialog.getWindow();
		win.setGravity(Gravity.LEFT | Gravity.TOP);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		WindowManager.LayoutParams origin_params = win.getAttributes();
		params.copyFrom(origin_params);
		params.width = dm.widthPixels;
		params.height = dm.heightPixels;

		Rect frame = new Rect();
		this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		params.height = params.height - statusBarHeight;

		win.setAttributes(params);
		win.setWindowAnimations(0);

		mTabSwitchDialog.setCancelable(true);

		mTabSwitchDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (event.getAction() == KeyEvent.ACTION_UP) {
						if (mTabSwitch.processKeyBack())
							return true;
						hideTabSwitch();
					}

					return true;
				}
				return false;
			}
		});

		Rect frame2 = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame2);
		int width = frame2.width();
		int height = frame2.height();
		float ratio = (float) height / (float) width;
		mTabSwitch.setAspectRatio(ratio);

//		mTabSwitchDialog.show();
//
//		mTabSwitch.onShow(retainScene);
	}

	public void hideTabSwitch() {
		if (null == mTabSwitch)
			return;

		View view = mTabSwitch.getView();
		if (null != view)
			view.setVisibility(View.GONE);

		if (null != mTabSwitchViewShim) {
			mTabSwitchViewShim.removeAllViews();
			mTabSwitchViewShim = null;
		}

		if (null != mTabSwitchDialog) {
			// mTabSwitchDialog.setContentView(null);
			mTabSwitchDialog.dismiss();
			mTabSwitchDialog = null;
		}
	}
	public boolean makeAppDir(String appHome) throws IOException {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// 获取SD卡路径
			String root = Environment.getExternalStorageDirectory().getCanonicalPath();
			// 构建应用程序路径
			File app_dir = new File(root + "/" + appHome);
			boolean  ean = app_dir.mkdir();
			boolean  can = app_dir.exists();
			if (!ean && !can) {
				Log.e("创建应用文件夹", "失败");
				return false;
			}

			// 午餐文件夹
			File wucan_dir = new File(app_dir.getCanonicalPath() + "/" + "wucan");
			if (!wucan_dir.mkdir() && !wucan_dir.exists()) {
				Log.e("创建午餐文件夹", "失败");
				return false;
			}

			// 晚餐文件夹
			File wancan_dir = new File(app_dir.getCanonicalPath() + "/" + "wancan");
			if (!wancan_dir.mkdir() && !wancan_dir.exists()) {
				Log.e("创建晚餐文件夹", "失败");
				return false;
			}
			
			//夜宵文件夹
			File yexiao_dir = new File(app_dir.getCanonicalPath() + "/" + "yexiao");
			if (!yexiao_dir.mkdir() && !yexiao_dir.exists()) {
				Log.e("创建夜宵文件夹", "失败");
				return false;
			}
			
			//饮食日志文件夹
			File log = new File(app_dir.getCanonicalPath() + "/" + "log");
			if (!yexiao_dir.mkdir() && !yexiao_dir.exists()) {
				Log.e("创建夜宵文件夹", "失败");
				return false;
			}
		}
		return true;
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
//		Toast.makeText(this, "helloabc", Toast.LENGTH_SHORT).show();
		finish();
	}
}
