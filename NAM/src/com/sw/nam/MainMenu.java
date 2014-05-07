package com.sw.nam;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
    public static final String PREFS_NAME = "nam_prefs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    boolean is_registered = settings.getBoolean("registered", false);
		
	 		Context context = getApplicationContext();
			accounts_ = AccountManager.get(context).getAccounts();
	
			if (accounts_.length == 0){
				//TODO No Google Account error Message
				System.exit(0);
			}
			google_account = accounts_[0];
			
		if(!is_registered){
			getRegId();
			SharedPreferences.Editor editor = settings.edit();
			//editor.putBoolean("registered", true);
			//editor.commit();
	    }
	    else
	    {
		    String load_reg_id = settings.getString("registration_id", "Error");
	    	regid = load_reg_id;
	    	Log.d("GCM", "Account already registered on this device!");
	    }
		Log.d("GCM", "Registration ID: " + regid);
		setContentView(R.layout.activity_main_menu);
		TextView t = (TextView)findViewById(R.id.textView1);
		t.setText("Registered as: " + google_account.name);
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
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        			SharedPreferences.Editor editor = settings.edit();
        			editor.putString("registration_id", regid);
        			editor.putBoolean("registered", true);
        			editor.commit();
                    Log.i("GCM",  msg);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                Log.i("GCM","Register Response:" + Register.registerandsendtest(google_account.name, regid));
                
                return msg;
            }
            
            @Override
            protected void onPostExecute(String msg) {
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
