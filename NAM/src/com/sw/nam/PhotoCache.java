package com.sw.nam;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Profile;
import android.util.Log;
import android.widget.ImageView;

public class PhotoCache {

	private static final String TAG = PhotoCache.class.getName();

	CacheMemory cacheMemory = new CacheMemory();

	private Map<ImageView, Uri> imageViewsMap = Collections.synchronizedMap(new WeakHashMap<ImageView, Uri>());

	ExecutorService executorService;

	private Context context; 

	final int stub_id = R.drawable.ic_contact_picture;

	public PhotoCache(Context context){
		executorService = Executors.newFixedThreadPool(5);
		this.context = context;
	}

	public void DisplayBitmap(Uri uri, ImageView imageView){
		if(uri == null){
			imageView.setImageResource(stub_id);
			return;
		}
		imageViewsMap.put(imageView, uri);
		Bitmap bitmap = cacheMemory.get(uri);
		if(bitmap != null)
			imageView.setImageBitmap(bitmap);
		else{
			String[] mProjection = new String[]
				    {
				        Profile._ID,
				        Profile.DISPLAY_NAME_PRIMARY,
				        Profile.LOOKUP_KEY,
				        Profile.PHOTO_THUMBNAIL_URI
				    };

				// Retrieves the profile from the Contacts Provider
				Cursor mProfileCursor =
				       context.getContentResolver().query(
				                Profile.CONTENT_URI,
				                mProjection ,
				                null,
				                null,
				                null);
				String pictureUri = "";
				if (mProfileCursor.moveToFirst()) {
					uri = Uri.parse(mProfileCursor.getString(mProfileCursor.getColumnIndex(Profile.PHOTO_THUMBNAIL_URI)));
				}
				InputStream in = null;
				
				if (uri != null) {
			        try{
			            in = context.getContentResolver().openInputStream(uri);
			        }catch(FileNotFoundException e){
			            e.printStackTrace();
			        }
			    }
			imageView.setImageBitmap(BitmapFactory.decodeStream(in));
//			PhotoStub p = new PhotoStub(uri, imageView);
//			executorService.submit(new PhotoStubLoader(p));			
//			imageView.setImageResource(stub_id);
		}
	}

	private class PhotoStub {
		public Uri url;
		public ImageView imageView;

		public PhotoStub(Uri u, ImageView i){
			url=u; 
			imageView = i;
		}
	}

	class PhotoStubLoader implements Runnable {
		PhotoStub photoToLoad;
		PhotoStubLoader(PhotoStub photoToLoad){
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if(imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			cacheMemory.put(photoToLoad.url, bmp);
			if(imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			((Activity)context).runOnUiThread(bd);
		}
	}

	private Bitmap getBitmap(Uri uri) {
		Bitmap b;
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getContentResolver().openInputStream(uri));
			b = decodeRecourse(bufferedInputStream);
		} catch (Exception e) {
			b = null;
		}
		if(b != null)
			return b;
		return null;
	}

	private Bitmap decodeRecourse(InputStream inputStream) {
		try {

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream, null, o);
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while(true){
				if(width_tmp /2 < REQUIRED_SIZE || height_tmp /2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			inputStream.reset(); 
			o.inJustDecodeBounds = false;
			o.inSampleSize = scale;
			return BitmapFactory.decodeStream(inputStream, null, o);
		} catch (Exception e) {
			Log.w(TAG, "decodeRecourse failed"+ e.getMessage());
		}finally{
			if(inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	boolean imageViewReused(PhotoStub photoToLoad) {
		Uri tag = imageViewsMap.get(photoToLoad.imageView);
		if(tag == null || !tag.equals(photoToLoad.url)){
			Log.w(TAG, "ViewHolder is reused no need to load photo");
			return true;
		}else{
			return false;
		}
	}

	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoStub photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoStub p){
			bitmap = b;
			photoToLoad = p;
		}
		public void run(){
			if(imageViewReused(photoToLoad))
				return;
			if(bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageResource(stub_id);
		}
	}

	public void clearCache() {
		cacheMemory.clear();
	}

	class CacheMemory {
		private static final String TAG = "CacheMemory";
		private Map<Uri, Bitmap> cache = Collections.synchronizedMap( new LinkedHashMap<Uri, Bitmap>(10, 1.5f, true));
		private long size = 0;
		private long limit = 1000000;

		public CacheMemory(){
			setLimit(Runtime.getRuntime().maxMemory()/4);
		}

		public void setLimit(long new_limit){
			limit = new_limit;
			Log.i(TAG, "CacheMemory will use up to " + limit/1024./1024. + "MB");
		}

		public Bitmap get(Uri id){
			try{
				if(!cache.containsKey(id))
					return null;
				return cache.get(id);
			}catch(NullPointerException ex){
				ex.printStackTrace();
				return null;
			}
		}

		public void put(Uri id, Bitmap bitmap){
			try{
				if(cache.containsKey(id))
					size -= getSizeInBytes(cache.get(id));
				cache.put(id, bitmap);
				size += getSizeInBytes(bitmap);
				checkSize();
			}catch(Throwable th){
				th.printStackTrace();
			}
		}

		private void checkSize() {
			if(size > limit){ 
				Iterator<Entry<Uri, Bitmap>> iter = cache.entrySet().iterator(); 
				while(iter.hasNext()){
					Entry<Uri, Bitmap> entry = iter.next();
					size -= getSizeInBytes(entry.getValue());
					iter.remove();
					if(size <= limit)
						break;
				}
			}
		}

		public void clear() {
			try{ 
				cache.clear();
				size = 0;
			}catch(NullPointerException ex){
				ex.printStackTrace();
			}
		}

		long getSizeInBytes(Bitmap bitmap) {
			if(bitmap == null)
				return 0;
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
	}	

}