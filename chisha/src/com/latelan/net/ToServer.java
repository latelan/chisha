package com.latelan.net;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ToServer {
	
	private String server_url = "http://3.xyguide.sinaapp.com/todaymeal.php";
	private String strResponse;
	private Handler mHandler;
	private int msgWhat;
	
	private  int msgWhatFetchMenu = 0x123;
	private  int msgWhatFetchVotes = 0x124;
	private  int msgWhatVote = 0x125;
	private  int msgWhatCancelVote = 0x126;
	
	
	// 投票
	public void vote(int dish_id, Handler handler){
		JSONObject msgJson = new JSONObject();
		
		try {
			msgJson.put("mode", "vote");
			msgJson.put("dish_id", dish_id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String msgStr = msgJson.toString();
		msgWhat = msgWhatVote;
		postRequest(server_url, msgStr, handler);
	}
	
	public void cancelVote(int dish_id, Handler handler){
		JSONObject msgJson = new JSONObject();
		
		try {
			msgJson.put("mode", "cancel_vote");
			msgJson.put("dish_id", dish_id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String msgStr = msgJson.toString();
		msgWhat = msgWhatCancelVote;
		postRequest(server_url, msgStr, handler);
		
	}
	
	// 获取投票信息
	public void fetchVotes(int dish_id, Handler handler){
		JSONObject msgJson = new JSONObject();
		
		try {
			msgJson.put("mode", "fetch_votes");
			msgJson.put("dish_id", dish_id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String msgStr = msgJson.toString();
		msgWhat = msgWhatFetchVotes;
		postRequest(server_url, msgStr, handler);
	}
	
	// 获取菜单
	public void fetchMenu(int timestamp, Handler handler){
		
//		int time = (int)(System.currentTimeMillis() / 1000);
		JSONObject msgJson = new JSONObject();
		
		try {
			msgJson.put("mode", "fetch_menu");
			msgJson.put("timestamp", timestamp);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String msgString = msgJson.toString();
		msgWhat = msgWhatFetchMenu;
		postRequest(server_url, msgString, handler);
	}

	public void postRequest(String url, String data, Handler handler) {
		this.mHandler = handler;
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(8000);
//		client.addHeader("Content-Type", "application/x-www-form-urlencoded");
		RequestParams params = new RequestParams();
		params.put("contents", data);

		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] arg1,
					byte[] responseBody) {
				// TODO Auto-generated method stub
				if (statusCode == 200) {
					// 发送成功
					
					strResponse = new String(responseBody);
					Log.d("response", strResponse);
					Message msg = mHandler.obtainMessage();
					msg.what = msgWhat;

					msg.obj = strResponse;
					mHandler.sendMessage(msg);

				}
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				// TODO Auto-generated method stub
				// 发送失败
				
			}
		});
	}
}
