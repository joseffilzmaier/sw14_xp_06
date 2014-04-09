package com.sw.nam;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sw.nam.EmailListDialog;

public class MainMenu extends Activity implements EmailListDialog.EmailDialogListener {

	Account[] accounts_;
    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "83287728691";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
 		Context context = getApplicationContext();
		accounts_ = AccountManager.get(context).getAccounts();
		String[] account_names = new String[accounts_.length];
		
		for (int i=0 ; i < accounts_.length ; i++){
			account_names[i] = accounts_[i].name;
		}
		
		setContentView(R.layout.activity_main_menu);
		DialogFragment email_dialog = EmailListDialog.newInstance(account_names); 
		email_dialog.show(getFragmentManager(), "dialog");
		
		
	}
	
	@Override
	public void onEmailSelected(int which){
		getRegId();
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
