package com.example.tabswitchdemo;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.latelan.net.ToServer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
	private boolean  donate = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setTitle("hello");
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.activity_singlepageshow);
		
		Intent intent = getIntent();
		cur_index = intent.getIntExtra("cur_index", 0);
		icon_path = intent.getStringExtra("icon_path");
		cur_des = intent.getStringExtra("cur_des");
		
		server = new ToServer();
		server.fetchVotes(cur_index+1, handler);
		
		single_page_show_img = (ImageView) findViewById(R.id.single_page_show_img);
		single_page_show_view = (TextView) findViewById(R.id.single_page_show_view);
		single_page_show_zan = (ImageButton) findViewById(R.id.single_page_show_zan);
		
//		
		single_page_show_img.setImageBitmap(BitmapFactory.decodeFile(icon_path));
		Random random = new Random();
		random_int = Math.abs(random.nextInt())%100;
		random_int = 0;
		String chooseMan = "<br/><h5>当前已选择此餐的人数:"+random_int+"</h5>";
//		String chooseMan = "<br/><h5>当前已选择此餐的人数:0</h5>";
		single_page_show_view.setText(cur_des+chooseMan);
		single_page_show_view.setText(Html.fromHtml(cur_des+chooseMan));
//		single_page_show_zan.set  setText("就吃你啦");
		
		
		single_page_show_zan.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(donate) {
					server.vote(cur_index+1, handler);
//					random_int += 1;
//					String chooseMan = "<br/><h5>当前已选择此餐的人数:"+random_int+"</h5>";
//					single_page_show_view.setText(Html.fromHtml(cur_des+chooseMan));
//					Toast.makeText(getBaseContext(), "vote", Toast.LENGTH_SHORT).show();
					}
					break;
				case MotionEvent.ACTION_UP:
					donate = false;
					((ImageButton)findViewById(R.id.single_page_show_zan)).setBackgroundResource(R.drawable.zan1);
				default:
					break;
				}
				return false;
			}
		});
		
	}
	
	 Handler handler = new Handler() {
	    	public void handleMessage(Message msg){
	    		JSONObject dataJson;
	    		if(msg.what == 0x124){
	    			
	    			try {
						dataJson = new JSONObject(msg.obj.toString());
						if(dataJson.getInt("code") == 0) {
			    			int voteNum = dataJson.getInt("detail");
			    			String chooseMan = "<br/><h5>当前已选择此餐的人数:"+voteNum+"</h5>";
			    			single_page_show_view.setText(Html.fromHtml(cur_des+chooseMan));
			    		} else {
			    			Toast.makeText(getBaseContext(), "取消", Toast.LENGTH_SHORT).show();
			    		}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		
	    		} else if (msg.what == 0x125) {
	    			try {
						dataJson = new JSONObject(msg.obj.toString());
						server.fetchVotes(cur_index+1, handler);
						
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
}
