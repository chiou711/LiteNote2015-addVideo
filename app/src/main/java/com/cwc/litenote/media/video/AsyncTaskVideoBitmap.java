package com.cwc.litenote.media.video;

import com.cwc.litenote.R;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.note.Note_view_pager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

//Video Async Task for applying MediaMetadataRetriever
//Note: setDataSource could hang up system for a long time when accessing remote content
public class AsyncTaskVideoBitmap extends AsyncTask<String,Integer,String>
{
	 Activity mAct;
	 String mPictureUri;
	 ImageView mImageView;
	 MediaMetadataRetriever mmr;
	 Bitmap bitmap;
//	 NoteFragment_itemAdapter.ViewHolder mViewHolder;
	 
//	 public AsyncTaskVideoBitmap(Activity act,String picString, ImageView view, NoteFragment_itemAdapter.ViewHolder holder)
	 public AsyncTaskVideoBitmap(Activity act,String picString, ImageView view)
	 {
		 mAct = act;
		 mPictureUri = picString;
		 mImageView = view;
//		 mViewHolder = holder;
	 }	 
	 
	@Override
	 protected void onPreExecute() 
	 {
	 	super.onPreExecute();
	 } 
	 
	 @Override
	 protected String doInBackground(String... params) 
	 {
		 
		 if(Note_view_pager.isPagerActive)
		 {
			if(this != null)
			{
				System.out.println("NoteFragment.mVideoAsyncTask != null");
				
				if(this.isCancelled())
					System.out.println("NoteFragment.mVideoAsyncTask.isCancelled()");
				else
					System.out.println("NoteFragment.mVideoAsyncTask is not Cancelled()");
				
				 if( (this != null) && (!this.isCancelled()) )
				 {
					 System.out.println("    NoteFragment.mVideoAsyncTask cancel");
					 this.cancel(true);
//					 this.cancel(true);
					 return "cancel";
				 }				
			}
			else
				System.out.println("NoteFragment.mVideoAsyncTask = null");
			
		 
		 }
		 
		 mmr = new MediaMetadataRetriever();
		 try
		 {
			 System.out.println("VideoAsyncTask / setDataSource start");
			 mmr.setDataSource(mAct,Uri.parse(mPictureUri));//??? why hang up?
			 System.out.println("VideoAsyncTask / setDataSource done");
			 bitmap = mmr.getFrameAtTime(-1);
			 bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
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
		 Bitmap bmVideoIcon = BitmapFactory.decodeResource(mAct.getResources(), R.drawable.ic_media_play);
		 
		 bitmap = UtilImage.setIconOnThumbnail(bitmap,bmVideoIcon,50);
		 
		 if(bitmap != null)
		 {
			 mImageView.setImageBitmap(bitmap);
			 System.out.println("VideoAsyncTask / set image bitmap");
		 }
	 }
}