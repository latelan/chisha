package com.example.tabswitchdemo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.latelan.net.ToServer;
import com.mwumli.record.XMLOperation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SinglePageShow extends Activity  {

	private int cur_index;
	private String icon_path;
	private String cur_des;
	private int random_int;
	private ImageView single_page_show_img;
	private TextView single_page_show_view;
	private ImageButton single_page_show_zan;
	private ToServer server;
	private XMLOperation dianzan;
	private List<HashMap<String, Object>> zanList;

	private boolean doesExit = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* ���ɵ����б� */
		dianzan = new XMLOperation();
		File targetFile = null;
		try {
			targetFile = new File(Environment.getExternalStorageDirectory()
					.getCanonicalPath() + "/chisha/" + "dianzan.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		zanList = dianzan.parseXMLWithPull(targetFile);

		requestWindowFeature(Window.FEATURE_NO_TITLE); // �����ޱ���
		setContentView(R.layout.activity_singlepageshow); // ���ò���

		/* ��ȡʳ��ͼƬ��ʳ��Ӫ����ֵ��ʳ�����״̬���� */
		single_page_show_img = (ImageView) findViewById(R.id.single_page_show_img);
		single_page_show_view = (TextView) findViewById(R.id.single_page_show_view);
		single_page_show_zan = (ImageButton) findViewById(R.id.single_page_show_zan);

		/* ��ȡ��һ����������Ĳ���������ʳ��ID��ʳ��ͼƬ·����ʳ��˵�� */
		Intent intent = getIntent();
		cur_index = intent.getIntExtra("cur_index", 0);
		icon_path = intent.getStringExtra("icon_path");
		cur_des = intent.getStringExtra("cur_des");

		/* ��������������µ������� */
		server = new ToServer();
		server.fetchVotes(cur_index + 1, handler); 

	
		
		/* ���±��ص��ޱ��� */
		if (zanList.get(cur_index).get("zan").equals("true")) {
			single_page_show_zan.setBackgroundResource(R.drawable.zan);
		} else {
			single_page_show_zan.setBackgroundResource(R.drawable.unzan);
		}

		/* ����ʳ��ͼƬ */
		single_page_show_img
				.setImageBitmap(BitmapFactory.decodeFile(icon_path));

		/* ��ʼ����ǰѡ������ */
		String chooseMan = "<br/><h5>��ǰ��ѡ��˲͵�����:0</h5>";
		single_page_show_view.setText(cur_des + chooseMan);
		single_page_show_view.setText(Html.fromHtml(cur_des + chooseMan));

		single_page_show_zan.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:

					zanOrNoZan(cur_index);

					break;
				case MotionEvent.ACTION_UP:

					// zanOrNoZan(cur_index);
				default:
					break;
				}
				return false;
			}
		});

	}

	/*
	 * ����Ѿ����ޣ���ôȡ������ �������ȡ������״̬����ô����
	 */
	private void zanOrNoZan(int curId) {
		int drawId;
		boolean zanbuzan;
		String doesZan = (String) zanList.get(curId).get("zan");

		if (doesZan.equals("true")) {
			drawId = R.drawable.unzan; // �Ѿ����޹��ˣ�������
			zanbuzan = false;
			server.cancelVote(cur_index + 1, handler);
			// Toast.makeText(getBaseContext(), "ȡ������",
			// Toast.LENGTH_SHORT).show();
		} else {
			drawId = R.drawable.zan;
			zanbuzan = true;
			server.vote(cur_index + 1, handler); // ����
			// Toast.makeText(getBaseContext(), "����",
			// Toast.LENGTH_SHORT).show();
		}

		((ImageButton) findViewById(R.id.single_page_show_zan))
				.setBackgroundResource(drawId);
		zanList.get(curId).put("zan", Boolean.valueOf(zanbuzan).toString());
	}

	/* ����������������ݵ�handler���� */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			JSONObject dataJson;
			if (msg.what == 0x124) { // ����յ��ĵ����������ݣ�������

				try {
					dataJson = new JSONObject(msg.obj.toString());
					if (dataJson.getInt("code") == 0) { // ����codeΪ0����ʾ�ɹ�
						int voteNum = dataJson.getInt("detail");
						if (voteNum < 0) // ����������������Ϊ����������ʾΪ 0
							voteNum = 0;
						String chooseMan = "<br/><h5>��ǰ��ѡ��˲͵�����:" + voteNum
								+ "</h5>";
						single_page_show_view.setText(Html.fromHtml(cur_des
								+ chooseMan));
					} else { // �����쳣
//						Toast.makeText(getBaseContext(), "���������������쳣",
//								Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (msg.what == 0x125) {
				try {
					dataJson = new JSONObject(msg.obj.toString());
					if ((0 == dataJson.getInt("code"))
							&& dataJson.getString("detail")
									.equals("successful")) {

						server.fetchVotes(cur_index + 1, handler);
					} else {
			/*			Toast.makeText(getBaseContext(), "���������������쳣",
								Toast.LENGTH_SHORT).show();*/
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (msg.what == 0x126) {
				try {
					dataJson = new JSONObject(msg.obj.toString());
					if ((0 == dataJson.getInt("code"))
							&& dataJson.getString("detail")
									.equals("successful")) {

						server.fetchVotes(cur_index + 1, handler);
					} else {
				/*		Toast.makeText(getBaseContext(), "���������������쳣",
								Toast.LENGTH_SHORT).show();*/
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	};



	




	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		dianzan.saxCreateXML(zanList);
		doesExit = false;
		super.onBackPressed();

	}

	
}
