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

public class SinglePageShow extends Activity {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* 生成点赞列表 */
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

		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
		setContentView(R.layout.activity_singlepageshow); // 设置布局

		/* 获取食物图片、食物营养价值、食物点赞状态容器 */
		single_page_show_img = (ImageView) findViewById(R.id.single_page_show_img);
		single_page_show_view = (TextView) findViewById(R.id.single_page_show_view);
		single_page_show_zan = (ImageButton) findViewById(R.id.single_page_show_zan);

		/* 获取上一个活动传过来的参数：包括食物ID、食物图片路径、食物说明 */
		Intent intent = getIntent();
		cur_index = intent.getIntExtra("cur_index", 0);
		icon_path = intent.getStringExtra("icon_path");
		cur_des = intent.getStringExtra("cur_des");

		/* 请求服务器，更新点赞数据 */
		server = new ToServer();
		server.fetchVotes(cur_index + 1, handler);

		/* 更新本地点赞背景 */
		if (zanList.get(cur_index).get("zan").equals("true")) {
			single_page_show_zan.setBackgroundResource(R.drawable.zan);
		} else {
			single_page_show_zan.setBackgroundResource(R.drawable.unzan);
		}

		/* 更新食物图片 */
		single_page_show_img
				.setImageBitmap(BitmapFactory.decodeFile(icon_path));

		/* 初始化当前选餐人数 */
		String chooseMan = "<br/><h5>当前已选择此餐的人数:0</h5>";
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
	 * 如果已经点赞，那么取消点赞 如果处于取消点赞状态，那么点赞
	 */
	private void zanOrNoZan(int curId) {
		int drawId;
		boolean zanbuzan;
		String doesZan = (String) zanList.get(curId).get("zan");

		if (doesZan.equals("true")) {
			drawId = R.drawable.unzan; // 已经点赞过了，不点赞
			zanbuzan = false;
			server.cancelVote(cur_index + 1, handler);
			// Toast.makeText(getBaseContext(), "取消点赞",
			// Toast.LENGTH_SHORT).show();
		} else {
			drawId = R.drawable.zan;
			zanbuzan = true;
			server.vote(cur_index + 1, handler); // 点赞
			// Toast.makeText(getBaseContext(), "点赞",
			// Toast.LENGTH_SHORT).show();
		}

		((ImageButton) findViewById(R.id.single_page_show_zan))
				.setBackgroundResource(drawId);
		zanList.get(curId).put("zan", Boolean.valueOf(zanbuzan).toString());
	}

	/* 处理服务器返回数据的handler函数 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			JSONObject dataJson;
			if (msg.what == 0x124) { // 如果收到的点赞类型数据，处理它

				try {
					dataJson = new JSONObject(msg.obj.toString());
					if (dataJson.getInt("code") == 0) { // 假如code为0，表示成功
						int voteNum = dataJson.getInt("detail");
						if (voteNum < 0) // 当服务器传递数据为负数，则显示为 0
							voteNum = 0;
						String chooseMan = "<br/><h5>当前已选择此餐的人数:" + voteNum
								+ "</h5>";
						single_page_show_view.setText(Html.fromHtml(cur_des
								+ chooseMan));
					} else { // 数据异常
						Toast.makeText(getBaseContext(), "服务器返回数据异常",
								Toast.LENGTH_SHORT).show();
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
						Toast.makeText(getBaseContext(), "服务器返回数据异常",
								Toast.LENGTH_SHORT).show();
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
						Toast.makeText(getBaseContext(), "服务器返回数据异常",
								Toast.LENGTH_SHORT).show();
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		dianzan.saxCreateXML(zanList);
		super.onBackPressed();

	}
}
