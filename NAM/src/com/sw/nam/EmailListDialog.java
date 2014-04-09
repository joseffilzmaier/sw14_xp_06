package com.sw.nam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

public class EmailListDialog extends DialogFragment {
	
	static EmailListDialog newInstance(String[] account_names) {
		EmailListDialog f = new EmailListDialog();

	    // Supply num input as an argument.
	    Bundle args = new Bundle();
	    args.putStringArray("accname", account_names);
	    f.setArguments(args);

	    return f;
	}
	
	// Container Activity must implement this interface
    public interface OnArticleSelectedListener {
        public void onArticleSelected(Uri articleUri);
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.choose_account)
	    	   .setItems(getArguments().getStringArray("accname"), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   mListener.onEmailSelected(which);
	           }
	    });
	    return builder.create();
	}
	
	public interface EmailDialogListener {
        public void onEmailSelected(int which);
	}
	
	 // Use this instance of the interface to deliver action events
    EmailDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (EmailDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	
}
