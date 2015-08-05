package com.example.tabswitchdemo.tabswitch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.w3c.dom.Notation;

import com.example.tabswitchdemo.R;
import com.example.tabswitchdemo.SinglePageShow;
import com.example.tabswitchdemo.TabSwitchActivity;
import android.app.usage.UsageEvents.Event;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo.State;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class TabSwitchRotate extends LinearLayout implements TabSwitch, OnClickListener, TabSwitchRotateCtrl.Observer {
	private static final String TAG = "TabSwitchRotate";
	private TabSwitchRotateCtrl mCtrl;
	private float mAspectRatio = 0;
	ArrayList<String> icons = new ArrayList<String>();
	public TabSwitchRotate(Context context) {
		super(context);
	}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   

	public TabSwitchRotate(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	public void setCurrentPage() {
		mCtrl.setCurrentIndex(mCtrl.getCurrentIndex());
	}
	protected void onFinishInflate() {
		mCtrl = (TabSwitchRotateCtrl) findViewById(R.id.tab_switch);
		mCtrl.setObserver(this);

		//点击摇一摇，执行的切换操作
		findViewById(R.id.create_new_tab).setOnTouchListener(new OnTouchListener(	) {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				Random random;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					random = new Random();
					 
					mCtrl.setCurrentIndex(Math.abs(random.nextInt())%4);
					break;
				case MotionEvent.ACTION_MOVE:
					random = new Random();
					 
					mCtrl.setCurrentIndex(Math.abs(random.nextInt())%4);
					break;
				case MotionEvent.ACTION_UP:
					mCtrl.setCurrentIndex(mCtrl.getCurrentIndex());
					
					Intent intent = new Intent(getContext(), SinglePageShow.class);
					intent.putExtra("cur_index", mCtrl.getCurrentIndex());
					intent.putExtra("icon_path", icons.get(mCtrl.getCurrentIndex()));
					intent.putExtra("cur_des", mCtrl.getData().get(mCtrl.getCurrentIndex()).des);
					getContext().startActivity(intent);
					
				default:
					break;
				}
				return false;
			}
		});
	}

	@Override
	public boolean isShow() {
		// TODO Auto-generated method stub
		if (getParent() == null)
			return false;
		return getVisibility() == View.VISIBLE;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return this;
	}

	//更新图片
	public void update(boolean retainScene) throws IOException {
		// if(!isDirty2())
		// return;
		Log.d(TAG, "TabSwitch update");

		// lizongyao: supply bitmap and string array
		int count = 4;// mTabModel.getTabCount();

		ArrayList<Bitmap> bmps = new ArrayList<Bitmap>();
		ArrayList<String> paths = new ArrayList<String>();
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<String> des_list = new ArrayList<String>();
		
		String icons_root_path = Environment.getExternalStorageDirectory().getCanonicalPath()+"/chisha/wucan";
		String[] icons_name={"宫保鸡丁饭","煲仔饭","白斩鸡饭","肥牛浇汁饭","咖喱牛腩", "拉面","三明治","石锅拌饭"};
		String[] des = {
				"<h3>宫保鸡丁</h3><br/>风味特色：<br/>宫保鸡丁选用鸡肉为主料，佐以花生米、黄瓜、辣椒等辅料烹制而成。 红而不辣、辣而不猛、香辣味浓、肉质滑脆。",
		"<h3>煲仔饭</h3><br/>煲仔饭属于粤菜系。 煲仔饭的风味多达百余种，如腊味、冬菇滑鸡、豆豉排骨、猪肝、烧鸭、白切鸡等。其实煲仔饭也称瓦煲饭。",
		"<h3>白斩鸡饭</h3><br/>南方菜系名菜，形状美观，皮黄肉白，肥嫩鲜美，滋味异常鲜美，十分可口。白切鸡是粤菜鸡肴中最普通的一种，属于浸鸡类。以其制作简易，刚熟不烂，不加配料且保持原味为特点。白切鸡皮爽肉滑，清淡鲜美。色洁白带油黄，具有葱油香味，葱段打花镶边，食时带芥末酱，食之别有风味，保持了鸡肉的鲜美、原汁原味",
		"<h3>肥牛浇汁饭</h3><br/>一碗冒着热气的肥牛饭端到面前，光是闻那个味道就可以让饥肠辘辘的肚皮大叫起来，当舀一勺裹着汤汁的米饭进入口中，再来一口薄如蝉翼的肥牛片，半天的工作憋屈全部发泄在这碗饭上，最后再吃几朵椰菜花，收尾，满足的打着饱嗝溜达去了。",
		"<h3>咖喱牛腩 </h3><br/>\"咖喱\"一词来源于坦米尔语，是“许多的香料加在一起煮”的意思。",
		"<h3>拉面 </h3><br/>麻辣香锅发源于巴蜀地区，以四川、重庆等地方麻辣风味融合而来，麻辣香锅源于土家风味，是当地老百姓的家常做法，以麻、辣、鲜、香、油、混搭为特点。",
		"<h3>三明治 </h3><br/>三明治是一种典型的西方食品，以两片面包夹几片肉和奶酪、各种调料制作而成，吃法简便，广泛流行于西方各国。",
		"<h3>石锅拌饭 </h3><br/>\"石锅拌饭\"又称 \"韩国拌饭\"、\"石碗拌饭\"，是韩国以及中国东北地区黑龙江、吉林、辽宁特有的米饭料理。 它的发源地为韩国全州，后来演变为韩国的代表性食物。在韩国吃\"石锅拌饭\"也是爱情的表征。如果情人一起上餐馆点”石锅拌饭“的话，男士必须得先替女友搅好拌饭，若女友无法将饭菜吃个精光，那男士就得将剩下的饭完全吃干净，以代表对女友的爱情。"
		};
//		ArrayList<String> icons = new ArrayList<String>();//移动到最顶端
		
		for (int i = 0; i < icons_name.length; i++) {
			
			icons.add(icons_root_path+"/"+icons_name[i]+".jpg");
			titles.add(icons_name[i]);
			des_list.add(des[i]);
		}
		

		Bitmap bmp = null;
		
		AssetManager am = getResources().getAssets();
		for (int i = 0; i < icons_name.length; i++) {
			bmp = BitmapFactory.decodeFile(icons.get(i));
			bmps.add(bmp);
			
		}
		ImageButton viewCreateTab = (ImageButton) findViewById(R.id.create_new_tab);
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewCreateTab.getLayoutParams();
		if (mAspectRatio < 1) {
			lp.bottomMargin = 0;
		} else {
			lp.bottomMargin = viewCreateTab.getHeight();
			;
		}
		
		//更改摇一摇的间距
		lp.bottomMargin = 0;
		viewCreateTab.setLayoutParams(lp);

	
		mCtrl.setData(bmps, icons, des_list, titles, bmps.size()/2, 1);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
//		if (id == R.id.create_new_tab) {
//			// if(mObserver != null)
//			// mObserver.onActionCreate();
//			Toast.makeText(this.getContext(), "你想死吗?", Toast.LENGTH_SHORT).show();
//			Random random = new Random();
//			 
//			mCtrl.setCurrentIndex(Math.abs(random.nextInt())%4);
////			onSwitch(mCtrl.getCurrentIndex());
//		} 
	}

	@Override
	public void onSwitch(int index) {
		// TODO Auto-generated method stub
		// if(mObserver != null)
		// mObserver.onActionSwitch(index);
	}

	@Override
	public void onClose(int index) {
		// TODO Auto-generated method stub
		// if(mObserver != null)
		// mObserver.onActionClose(index);
		// update();
	}

	@Override
	public void onViewSizeChanged() {
		// if(mObserver != null)
		// mObserver.onViewSizeChanged();
		// don't do layout during onlayout
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				try {
					update(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onCloseAll() {
		// if(mObserver != null)
		// mObserver.onActionCloseAll();
	}

	@Override
	public void onShow(boolean retainScene) {
		// TODO Auto-generated method stub
		mCtrl.setState(TabSwitchRotateCtrl.State.State_Normal);

		Log.i("main", "show"+retainScene);
		try {
			update(retainScene);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean processKeyBack() {
//		return mCtrl.processKeyBack();
		return true;
	}

	public void setAspectRatio(float ratio) {
		mAspectRatio = ratio;
	}
}
