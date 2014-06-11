/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sw.nam.client;

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
import java.util.Random;

import com.sw.nam.Common;
import com.sw.nam.DataProvider;

public final class ServerUtilities {

	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	public static void register(final String email, final String regId)
			throws IOException {

		String serverUrl = Common.getServerUrl() + "/register";
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataProvider.SENDER_EMAIL, email);
		params.put(DataProvider.REG_ID, regId);

		try {
			post(serverUrl, params, MAX_ATTEMPTS);
		} catch (IOException e) {
			throw e;
		}
	}

	public static void unregister(final String email) {

		String serverUrl = Common.getServerUrl() + "/unregister";
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataProvider.SENDER_EMAIL, email);
		try {
			post(serverUrl, params, MAX_ATTEMPTS);
		} catch (IOException e) {

		}
	}

	public static void send(String msg, String to) throws IOException {

		String serverUrl = Common.getServerUrl() + "/send";
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataProvider.MESSAGE, msg);
		params.put(DataProvider.SENDER_EMAIL, Common.getPreferredEmail());
		params.put(DataProvider.RECEIVER_EMAIL, to);
		post(serverUrl, params, MAX_ATTEMPTS);
	}

	public static String contactRequest(String email) throws IOException {

		String response = " ";
		String serverUrl = Common.getServerUrl() + "/contactRequest";
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataProvider.COL_EMAIL, email);
		response = post(serverUrl, params, MAX_ATTEMPTS);
		return response;
	}

	private static String post(String endpoint, Map<String, String> params,
			int maxAttempts) throws IOException {
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		String response = " ";
		for (int i = 1; i <= maxAttempts; i++) {

			try {
				response = post(endpoint, params);
				return response;
			} catch (IOException e) {

				if (i == maxAttempts) {
					throw e;
				}
				try {
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					return response;
				}
				backoff *= 2;
			} catch (IllegalArgumentException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
		return response;
	}

	private static String post(String endpoint, Map<String, String> params)
			throws IOException {
		URL url;
		StringBuffer response = new StringBuffer();
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("Post failed with error code " + status);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return response.toString();
	}

}
