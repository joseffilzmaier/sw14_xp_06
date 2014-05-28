package com.sw.nam;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sw.nam.client.ServerUtilities;

public class AddContactDialog extends DialogFragment {
	private AlertDialog alertDialog;
	private EditText et;

	public static AddContactDialog newInstance() {
		AddContactDialog fragment = new AddContactDialog();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		et = new EditText(getActivity());
		et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		et.setHint("abc@example.com");
		alertDialog = new AlertDialog.Builder(getActivity())
				.setTitle("Add Contact")
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(android.R.string.cancel, null).setView(et)
				.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button okBtn = alertDialog
						.getButton(AlertDialog.BUTTON_POSITIVE);
				okBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String email = et.getText().toString();
						if (!isEmailValid(email)) {
							et.setError("Invalid email!");
							return;
						}
						try {
							new AddContactTask(getActivity().getApplicationContext()).execute(email);

						} catch (Exception e) {
						}
						alertDialog.dismiss();
					}
				});
			}
		});
		return alertDialog;
	}

	private boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	private class AddContactTask extends AsyncTask<String, Void, String> {
	  private String email;
	  private Context context;
	  
    public AddContactTask(Context context) {
      this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
      email = params[0];
    
      String resp = "";
      try {
        resp = ServerUtilities.contactRequest(email);
      } catch (IOException ex) {
      }
      return resp;
    }

    @Override
    protected void onPostExecute(String result) {
      if (result.equals("true")) {
        ContentValues values = new ContentValues(2);
        values.put(DataProvider.COL_NAME,
            email.substring(0, email.indexOf('@')));
        values.put(DataProvider.COL_EMAIL, email);
        context.getContentResolver().insert(
            DataProvider.CONTENT_URI_PROFILE,
            values);
        
        Toast.makeText(context, email + " added!", Toast.LENGTH_SHORT).show();
        
      }
      else
      {
        Toast.makeText(context, email + " is not registered!", Toast.LENGTH_LONG).show();
      }
    }
	}
}