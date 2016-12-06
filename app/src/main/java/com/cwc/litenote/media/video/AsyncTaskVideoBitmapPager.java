package com.cwc.litenote.media.video;


import com.cwc.litenote.media.image.UtilImage;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

// pager video async task
public class AsyncTaskVideoBitmapPager extends AsyncTask<String,Integer,String>
{
	 FragmentActivity mFragAct;
	 String mPictureUri;
	 VideoViewCustom mVideoView;
	 MediaMetadataRetriever mmr;
	 Bitmap bitmap;
	 public static String mRotationStr = null;
	 
	 public AsyncTaskVideoBitmapPager(FragmentActivity fragAct, String mPictureString,VideoViewCustom view) 
	 {
		 mFragAct = fragAct;
		 mPictureUri = mPictureString;
		 mVideoView = view;
	 }

	@Override
	 protected void onPreExecute() 
	 {
	 	super.onPreExecute();
	 } 
	 
	 @Override
	 protected String doInBackground(String... params) 
	 {
		 mmr = new MediaMetadataRetriever();
		 try
		 {
			 System.out.println("PagerVideoAsyncTask / setDataSource start / mPictureUri = " + mPictureUri);
			 mmr.setDataSource(mFragAct,Uri.parse(mPictureUri));//??? why hang up?
			 System.out.println("PagerVideoAsyncTask / setDataSource done / mPictureUri = " + mPictureUri );
			 bitmap = mmr.getFrameAtTime(-1);
			 bitmap = Bitmap.createScaledBitmap(bitmap, UtilImage.getScreenWidth(mFragAct), UtilImage.getScreenHeight(mFragAct), true);
			 
			 if (Build.VERSION.SDK_INT >= 17) {
				 mRotationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
				 Log.d("PagerVideoAsyncTask / Rotation = ", mRotationStr);
			 }
			 
			 mmr.release();
		 }
		 catch(Exception e)
		 { }

		 return null;
	 }
	
	 @Override
	 protected void onProgressUpdate(Integer... progress) 
	 { 
	     super.onProgressUpdate(progress);
	 }
	 
	 // This is executed in the context of the main GUI thread
	 protected void onPostExecute(String result)
	 {
		 BitmapDrawable bitmapDrawable = new BitmapDrawable(mFragAct.getResources(),bitmap);
		 
		 if( (mVideoView != null) && (bitmapDrawable != null) )
		 {
			 UtilVideo.setVideoViewDimensions(bitmapDrawable);
			 
			 if(UtilVideo.mVideoView.getCurrentPosition() == 0)
				 UtilVideo.setBitmapDrawableToVideoView(bitmapDrawable,mVideoView);
		 }
		 else
			 return;
	 }
}
