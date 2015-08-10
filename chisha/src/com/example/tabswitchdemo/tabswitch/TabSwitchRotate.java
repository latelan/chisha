package com.example.tabswitchdemo.tabswitch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.w3c.dom.Notation;

import com.example.tabswitchdemo.R;
import com.example.tabswitchdemo.SinglePageShow;
import com.example.tabswitchdemo.TabSwitchActivity;

import android.app.Activity;
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

		//���ҡһҡ��ִ�е��л�����
		findViewById(R.id.create_new_tab).setOnTouchListener(new OnTouchListener(	) {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				Random random;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					random = new Random();
					 
					mCtrl.setCurrentIndex(Math.abs(random.nextInt())%8);
					break;
				case MotionEvent.ACTION_MOVE:
					random = new Random();
					 
					mCtrl.setCurrentIndex(Math.abs(random.nextInt())%8);
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

	//����ͼƬ
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
		String[] icons_name={"����������","���з�","��ն����","��ţ��֭��","���ţ��", "����","������","ʯ���跹"};
		String[] des = {
				"<h3>��������</h3><br/>��ζ��ɫ��<br/>��������ѡ�ü���Ϊ���ϣ����Ի����ס��ƹϡ������ȸ������ƶ��ɡ� ����������������͡�����ζŨ�����ʻ��ࡣ",
		"<h3>���з�</h3><br/>���з���������ϵ�� ���з��ķ�ζ�������֣�����ζ�����������������Źǡ���Ρ���Ѽ�����м��ȡ���ʵ���з�Ҳ�����ҷ���",
		"<h3>��ն����</h3><br/>�Ϸ���ϵ���ˣ���״���ۣ�Ƥ����ף�������������ζ�쳣������ʮ�ֿɿڡ����м������˼���������ͨ��һ�֣����ڽ����ࡣ�����������ף����첻�ã����������ұ���ԭζΪ�ص㡣���м�Ƥˬ�⻬���嵭������ɫ��״��ͻƣ����д�����ζ���жδ���ߣ�ʳʱ����ĩ����ʳ֮���з�ζ�������˼����������ԭ֭ԭζ",
		"<h3>��ţ��֭��</h3><br/>һ��ð�������ķ�ţ���˵���ǰ���������Ǹ�ζ���Ϳ����ü����ꤵĶ�Ƥ�����������Ҩһ�׹�����֭���׷�������У�����һ�ڱ������ķ�ţƬ������Ĺ�������ȫ����й�����뷹�ϣ�����ٳԼ���Ҭ�˻�����β������Ĵ��ű������ȥ�ˡ�",
		"<h3>���ţ�� </h3><br/>\"���\"һ����Դ��̹�׶���ǡ��������ϼ���һ���󡱵���˼��",
		"<h3>���� </h3><br/>���������Դ�ڰ�����������Ĵ�������ȵط�������ζ�ں϶������������Դ�����ҷ�ζ���ǵ����ϰ��յļҳ����������顢�����ʡ��㡢�͡����Ϊ�ص㡣",
		"<h3>������ </h3><br/>��������һ�ֵ��͵�����ʳƷ������Ƭ����м�Ƭ������ҡ����ֵ����������ɣ��Է���㣬�㷺����������������",
		"<h3>ʯ���跹 </h3><br/>\"ʯ���跹\"�ֳ� \"�����跹\"��\"ʯ��跹\"���Ǻ����Լ��й��������������������֡��������е��׷����� ���ķ�Դ��Ϊ����ȫ�ݣ������ݱ�Ϊ�����Ĵ�����ʳ��ں�����\"ʯ���跹\"Ҳ�ǰ���ı������������һ���ϲ͹ݵ㡱ʯ���跹���Ļ�����ʿ���������Ů�ѽ��ð跹����Ů���޷������˳Ը����⣬����ʿ�͵ý�ʣ�µķ���ȫ�Ըɾ����Դ����Ů�ѵİ��顣"
		};
//		ArrayList<String> icons = new ArrayList<String>();//�ƶ������
		
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
		
		//����ҡһҡ�ļ��
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
//			Toast.makeText(this.getContext(), "��������?", Toast.LENGTH_SHORT).show();
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

	
	//���ص��ֻ�����
	public boolean processKeyBack() {
//		return mCtrl.processKeyBack();
//		((Activity) this.getContext()).finish();
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);

        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        ((Activity) this.getContext()).startActivity(mHomeIntent);

		return true;
//		return true;
	}

	public void setAspectRatio(float ratio) {
		mAspectRatio = ratio;
	}
}
