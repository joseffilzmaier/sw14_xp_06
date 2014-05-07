package com.sw.nam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

public class Register {
	public static String registerandsendtest(final String email, final String regId) {
	      
        String serverUrl = "http://1-dot-sw-xp-06.appspot.com" + "/register";
        Map<String, String> params = new HashMap<String, String>();
        params.put("chatId", email);
        params.put("regId", regId);
        
        String response = "";
        
        try {
        	Log.d("GCM", "ServerUrl: " + serverUrl + " " + "Params: " + params.get("chatId") + " ; " + params.get("regId"));
        	response = executePost(serverUrl, params);
        	
        } catch (IOException e) {
        }
        return response;
    }
	
	 private static String executePost(String endpoint, Map<String, String> params) throws IOException {
	        URL url;
	        StringBuffer response = new StringBuffer();
	        try {
	            url = new URL(endpoint);
	        } catch (MalformedURLException e) {
	            throw new IllegalArgumentException("invalid url: " + endpoint);
	        }
	        StringBuilder bodyBuilder = new StringBuilder();
	        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
	        // constructs the POST body using the parameters
	        while (iterator.hasNext()) {
	            Entry<String, String> param = iterator.next();
	            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
	            if (iterator.hasNext()) {
	                bodyBuilder.append('&');
	            }
	        }
	        String body = bodyBuilder.toString();
	        //Log.v(TAG, "Posting '" + body + "' to " + url);
	        byte[] bytes = body.getBytes();
	        HttpURLConnection conn = null;
	        try {
	            conn = (HttpURLConnection) url.openConnection();
	            conn.setDoOutput(true);
	            conn.setDoInput(true);
	            conn.setUseCaches(false);
	            conn.setFixedLengthStreamingMode(bytes.length);
	            conn.setRequestMethod("POST");
	            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	            
	            // post the request
	            OutputStream out = conn.getOutputStream();
	            out.write(bytes);
	            out.close();
	            
	            // handle the response
	            int status = conn.getResponseCode();
	            if (status != 200) {
	              throw new IOException("Post failed with error code " + status);
	              
	            } else {
	           
	            	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        		String inputLine;
	        		while ((inputLine = in.readLine()) != null) {
	        			response.append(inputLine);
	        		}
	        		in.close();
	            }
	        } finally {
	            if (conn != null) {
	                conn.disconnect();
	            }
	        }
	        return response.toString();
	      }

}
