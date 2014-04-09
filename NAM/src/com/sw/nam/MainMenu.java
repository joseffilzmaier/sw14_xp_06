package com.sw.nam;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainMenu extends Activity {

	Account[] accounts_;
	Account google_account;
	String regid;
    GoogleCloudMessaging gcm;
    String PROJECT_NUMBER = "83287728691";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
 		Context context = getApplicationContext();
		accounts_ = AccountManager.get(context).getAccounts();

		if (accounts_.length == 0){
			//TODO No Google Account error Message
			System.exit(0);
		}
		
		google_account = accounts_[0];
		
		getRegId();
		
		setContentView(R.layout.activity_main_menu);
		
	}
	
	public void getRegId(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM",  msg);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                Register.registerandsendtest("maaki.kozissnik@gmail.com", regid);
                return msg;
            }
            
            @Override
            protected void onPostExecute(String msg) {
        		TextView t = (TextView)findViewById(R.id.textView1);
        		t.setText(msg + '\n');
        		
        		
            }
        }.execute(null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
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
