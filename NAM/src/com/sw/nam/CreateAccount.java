package com.sw.nam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class CreateAccount extends AsyncTask<Object, Object, String>{

	@Override
	protected String doInBackground(Object... arg0) {
		URL url = null;
		try {
			url = new URL("http://1-dot-sw-xp-06.appspot.com/register");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	    HttpURLConnection conn = null;
	    String msg = null;
	    int status = 0;
	    try {
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	        // post the request
	        OutputStreamWriter writer = new OutputStreamWriter(
	                conn.getOutputStream());
	        writer.write("chatId=fjkah@gmail.com&regId=regidwhichever");
	        writer.close();
	        // handle the response
	        status = conn.getResponseCode();
	        
	        String temp = "";

	        BufferedReader buff = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        StringBuffer result = new StringBuffer();
	    	String line = "";
	    	while ((line = buff.readLine()) != null) {
	    		result.append(line);
	        if (status != 200) {
	          throw new IOException("Post failed with error code " + status);
	        }
	    	}
	    	msg  = result.toString();
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }

		return msg;
	}

	@Override
	protected void onPostExecute(String result) {
		Log.v("MSG", result);
    }

	
}
