package com.sw.nam;

import java.io.IOException;

import com.sw.nam.client.ServerUtilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
		et.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		et.setHint("abc@example.com");
		alertDialog = new AlertDialog.Builder(getActivity())
				.setTitle("Add Contact").setMessage("Add Contact")
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
							String resp = friendrequest(email);
							if (resp.equals("true")) {
								ContentValues values = new ContentValues(2);
								values.put(DataProvider.COL_NAME,
										email.substring(0, email.indexOf('@')));
								values.put(DataProvider.COL_EMAIL, email);
								getActivity().getContentResolver().insert(
										DataProvider.CONTENT_URI_PROFILE,
										values);
							} else {
								AlertDialog alert = new AlertDialog.Builder(v
										.getContext()).create();
								alert.setMessage("Contact is not registered!!!");
								alert.show();
							}
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

	private String friendrequest(final String email) {
		String result = " ";
		try {
			result = new AsyncTask<Void, Void, String>() {
				@Override
				protected String doInBackground(Void... params) {
					String resp = "NOTHING";
					try {
						resp = ServerUtilities.contactRequest(email);
					} catch (IOException ex) {
						Log.d("test", "Exception: " + ex);
						resp = "Contact request could not be sent";
					}
					return resp;
				}

				@Override
				protected void onPostExecute(String result) {
					// finalResult.setText(result);
					Log.d("test", "Response: " + result);
				}
			}.execute(null, null, null).get();
		} catch (Exception e) {
			Log.d("test", "Exception");
		}
		Log.d("test", "Response: " + result);
		return result;
	}
}