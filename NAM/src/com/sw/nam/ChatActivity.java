package com.sw.nam;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sw.nam.DataProvider.MessageType;
import com.sw.nam.client.ServerUtilities;

public class ChatActivity extends ActionBarActivity implements MessagesFragment.OnFragmentInteractionListener, 
OnClickListener {

	private EditText msgEdit;
	private Button sendBtn;
	private String profileId;
	private String profileName;
	private String profileEmail;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_activity);
		
		msgEdit = (EditText) findViewById(R.id.msg_edit);
		sendBtn = (Button) findViewById(R.id.send_btn);
		sendBtn.setOnClickListener(this);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		Cursor c;
		
		if(getIntent().getBooleanExtra(Common.IS_NOTIF, false)) {
			String senderEmail = getIntent().getStringExtra(Common.PROFILE_NAME);
			c = getContentResolver().query(DataProvider.CONTENT_URI_PROFILE, 
					null, DataProvider.COL_EMAIL + " LIKE ?", new String[]{senderEmail}, null);

		} else {
			profileId = getIntent().getStringExtra(Common.PROFILE_ID);
			c = getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), 
				null, null, null, null);
		}
		if (c.moveToFirst()) {
			profileName = c.getString(c.getColumnIndex(DataProvider.COL_NAME));
			profileEmail = c.getString(c.getColumnIndex(DataProvider.COL_EMAIL));
			profileId = c.getString(c.getColumnIndex(DataProvider.COL_ID));
			actionBar.setTitle(profileName);
		}
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;			
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.send_btn:
			send(msgEdit.getText().toString());
			msgEdit.setText(null);
			break;
		}
	}	

	@Override
	public String getProfileEmail() {
		return profileEmail;
	}	

	private void send(final String txt) {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					ServerUtilities.send(txt, profileEmail);
					ContentValues values = new ContentValues(2);
					values.put(DataProvider.COL_TYPE,  MessageType.OUTGOING.ordinal());
					values.put(DataProvider.COL_MESSAGE, txt);
					values.put(DataProvider.COL_RECEIVER_EMAIL, profileEmail);
					values.put(DataProvider.COL_SENDER_EMAIL, Common.getPreferredEmail());		
					getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);

				} catch (IOException ex) {
					msg = "Message could not be sent";
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				if (!TextUtils.isEmpty(msg)) {
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				}
			}
		}.execute(null, null, null);		
	}	

	@Override
	protected void onPause() {
		ContentValues values = new ContentValues(1);
		values.put(DataProvider.COL_COUNT, 0);
		getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
