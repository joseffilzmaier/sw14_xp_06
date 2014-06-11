package com.sw.nam;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sw.nam.EditContactDialog.OnFragmentInteractionListener;
import com.sw.nam.client.GcmUtil;

public class MainActivity extends ActionBarActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, OnFragmentInteractionListener {
	private AlertDialog disclaimer;
	ListView listView;
	private ActionBar actionBar;
	private ContactCursorAdapter ContactCursorAdapter;
	private GcmUtil gcmUtil;
	private int profileID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.contactslist);
		listView.setOnItemClickListener(this);
		ContactCursorAdapter = new ContactCursorAdapter(this, null);
		listView.setAdapter(ContactCursorAdapter);
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.show();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setTitle(Common.getPreferredEmail());

		getSupportLoaderManager().initLoader(0, null, this);
		registerForContextMenu(listView);
		
		actionBar.setSubtitle("connecting ...");

		registerReceiver(registrationStatusReceiver, new IntentFilter(Common.ACTION_REGISTER));
		gcmUtil = new GcmUtil(getApplicationContext());
	}

	@SuppressLint("NewApi")
  @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
	  super.onCreateContextMenu(menu, v, menuInfo);
	  menu.add(0, 0, 0, "Rename");
	  menu.add(0, 1, 1, "Delete");
	  menu.add(0, 2, 2, "Add Picture");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{	  
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	  Cursor cursor = (Cursor)listView.getItemAtPosition(info.position);
	  switch (item.getItemId()) 
	  {
    case 0:
      String profileId = cursor.getString(cursor.getColumnIndex(DataProvider.COL_ID));
      String name = cursor.getString(cursor.getColumnIndex(DataProvider.COL_NAME));
      
      EditContactDialog dialog = new EditContactDialog();
      Bundle args = new Bundle();
      args.putString(Common.PROFILE_ID, profileId);
      args.putString(DataProvider.COL_NAME, name);
      dialog.setArguments(args);
      dialog.show(getSupportFragmentManager(), "EditContactDialog");
      break;
      
    case 1:
      String email = cursor.getString(cursor.getColumnIndex(DataProvider.COL_EMAIL));
      getContentResolver().delete(DataProvider.CONTENT_URI_PROFILE, DataProvider.COL_EMAIL + " LIKE ?", new String[]{email});
      getContentResolver().delete(DataProvider.CONTENT_URI_MESSAGES, DataProvider.COL_SENDER_EMAIL + " LIKE ? OR " 
      + DataProvider.COL_RECEIVER_EMAIL + " LIKE ?", new String[]{email, email});
      Toast.makeText(getApplicationContext(), "Contact deleted", Toast.LENGTH_SHORT).show();
      break;
      
    case 2:
    	profileID = cursor.getInt(cursor.getColumnIndex(DataProvider.COL_ID));
    	Log.v("tag", Integer.toString(profileID));
    	Intent i = new Intent(
    			Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    	startActivityForResult(i, 1);
    	break;
      
    default:
      break;
    }
	  return true;
	}
	
	@Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	      
	     if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
	         Uri selectedImage = data.getData();
	         String[] filePathColumn = { MediaStore.Images.Media.DATA };
	 
	         Cursor cursor = getContentResolver().query(selectedImage,
	                 filePathColumn, null, null, null);
	         cursor.moveToFirst();
	 
	         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	         String picturePath = cursor.getString(columnIndex);
	         cursor.close();

	         Log.v("tag", "profile: " + profileID);
	         ContentValues values = new ContentValues(1);
			 values.put(DataProvider.COL_PICTURE, picturePath);
			 
			 getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, Integer.toString(profileID)), values, null, null);
			 
			 //drawable_ic_contact_picture
	     
	     }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			AddContactDialog newFragment = AddContactDialog.newInstance();
			newFragment.show(getSupportFragmentManager(), "AddContactDialog");
			return true;
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(Common.PROFILE_ID, String.valueOf(arg3));
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		if (disclaimer != null)
			disclaimer.dismiss();
		unregisterReceiver(registrationStatusReceiver);
		gcmUtil.cleanup();
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader loader = new CursorLoader(this,
				DataProvider.CONTENT_URI_PROFILE, new String[] {
						DataProvider.COL_ID, DataProvider.COL_NAME,
						DataProvider.COL_EMAIL, DataProvider.COL_COUNT, DataProvider.COL_PICTURE }, null,
				null, DataProvider.COL_ID + " DESC");
		return loader;
	}

	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> arg0,
			Cursor arg1) {
		ContactCursorAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0) {
		ContactCursorAdapter.swapCursor(null);
	}

	public class ContactCursorAdapter extends CursorAdapter {

		private LayoutInflater mInflater;

		public ContactCursorAdapter(Context context, Cursor c) {
			super(context, c, 0);
			this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return getCursor() == null ? 0 : super.getCount();
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View itemLayout = mInflater.inflate(R.layout.main_list_item,
					parent, false);
			ViewHolder holder = new ViewHolder();
			itemLayout.setTag(holder);
			holder.text1 = (TextView) itemLayout.findViewById(R.id.text1);
			holder.text2 = (TextView) itemLayout.findViewById(R.id.text2);
			holder.textEmail = (TextView) itemLayout
					.findViewById(R.id.textEmail);
			holder.avatar = (ImageView) itemLayout.findViewById(R.id.avatar);
			return itemLayout;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.text1.setText(cursor.getString(cursor
					.getColumnIndex(DataProvider.COL_NAME)));
			holder.textEmail.setText(cursor.getString(cursor
					.getColumnIndex(DataProvider.COL_EMAIL)));
			int count = cursor.getInt(cursor
					.getColumnIndex(DataProvider.COL_COUNT));
			if (count > 0) {
				holder.text2.setVisibility(View.VISIBLE);
				holder.text2.setText(String.format("%d new message%s", count,
						count == 1 ? "" : "s"));
			} else
				holder.text2.setVisibility(View.GONE);
			
			String file = cursor.getString(cursor.getColumnIndex(DataProvider.COL_PICTURE));

			if (file != "")
			{
				File imgFile = new  File(file);
				if(imgFile.exists()){
					//ExifInterface exif = new ExifInterface(file);
					//if (exif )
				    Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				 
				    holder.avatar.setImageBitmap(Bitmap.createScaledBitmap(bm, 32, 32, false));
				}
				else
					holder.avatar.setImageResource(R.drawable.ic_contact_picture);
			}
			else
				holder.avatar.setImageResource(R.drawable.ic_contact_picture);
			
			//photoCache.DisplayBitmap(requestPhoto(cursor.getString(cursor
			//		.getColumnIndex(DataProvider.COL_EMAIL))), holder.avatar);

		}
	}

	private static class ViewHolder {
		TextView text1;
		TextView text2;
		TextView textEmail;
		ImageView avatar;
	}

//	@SuppressLint("InlinedApi")
//	private Uri requestPhoto(String email) {
//		Cursor emailCur = null;
//		Uri uri = null;
//		try {
//			int SDK_INT = android.os.Build.VERSION.SDK_INT;
//			if (SDK_INT >= 11) {
//				String[] projection = { ContactsContract.CommonDataKinds.Email.PHOTO_URI };
//				ContentResolver cr = getContentResolver();
//				emailCur = cr
//						.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
//								projection,
//								ContactsContract.CommonDataKinds.Email.ADDRESS
//										+ " = ?", new String[] { email }, null);
//				if (emailCur != null && emailCur.getCount() > 0) {
//					if (emailCur.moveToNext()) {
//						String photoUri = emailCur
//								.getString(emailCur
//										.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_URI));
//						if (photoUri != null)
//							uri = Uri.parse(photoUri);
//					}
//				}
//			} else if (SDK_INT < 11) {
//				String[] projection = { ContactsContract.CommonDataKinds.Photo.CONTACT_ID };
//				ContentResolver cr = getContentResolver();
//				emailCur = cr
//						.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
//								projection,
//								ContactsContract.CommonDataKinds.Email.ADDRESS
//										+ " = ?", new String[] { email }, null);
//				if (emailCur.moveToNext()) {
//					int columnIndex = emailCur
//							.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
//					long contactId = emailCur.getLong(columnIndex);
//					uri = ContentUris.withAppendedId(
//							ContactsContract.Contacts.CONTENT_URI, contactId);
//					uri = Uri.withAppendedPath(uri,
//							ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (emailCur != null)
//					emailCur.close();
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//		return uri;
//	}
//	
	private BroadcastReceiver registrationStatusReceiver = new  BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && Common.ACTION_REGISTER.equals(intent.getAction())) {
				switch (intent.getIntExtra(Common.EXTRA_STATUS, 100)) {
				case Common.STATUS_SUCCESS:
					getSupportActionBar().setSubtitle("Online");
					break;

				case Common.STATUS_FAILED:
					getSupportActionBar().setSubtitle("Registration failed.");					
					break;					
				}
			}
		}
	};

  @Override
  public void onEditContact(String name) { 
  }
}