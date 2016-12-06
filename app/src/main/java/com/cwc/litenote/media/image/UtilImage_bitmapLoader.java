package com.cwc.litenote.media.image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.R;
import com.cwc.litenote.media.video.AsyncTaskVideoBitmap;
import com.cwc.litenote.media.video.UtilVideo;
import com.cwc.litenote.util.UilCommon;
import com.cwc.litenote.util.Util;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class UtilImage_bitmapLoader 
{
  Bitmap thumbnail;
  AsyncTaskVideoBitmap mVideoAsyncTask;
  SimpleImageLoadingListener mSimpleImageLoadingListener, mSimpleImageLoadingListenerForVideo;
  ImageLoadingProgressListener mImageLoadingProgressListener;
  ProgressBar mProgressBar;
  ImageView mPicImageView;
  
  public UtilImage_bitmapLoader(){}
  
  public UtilImage_bitmapLoader(ImageView picImageView, String mPictureUriInDB,final ProgressBar progressBar,Activity mAct )
  {
	  	setLoadingListerners();
	    mPicImageView = picImageView;
	    mProgressBar = progressBar;
	    
		Bitmap bmVideoIcon = BitmapFactory.decodeResource(mAct.getResources(), R.drawable.ic_media_play);
		Uri imageUri = Uri.parse(mPictureUriInDB);
		String pictureUri = imageUri.toString();
		String scheme = Uri.parse(pictureUri).getScheme();
		
		// 1 for image: scheme includes file and content
		if (UtilImage.hasImageExtension(pictureUri)) 
		{
			UilCommon.imageLoader
					 .displayImage(	pictureUri,
									mPicImageView,
									NoteFragment.mStyle % 2 == 1 ? UilCommon.optionsForRounded_light
											: UilCommon.optionsForRounded_dark,
									mSimpleImageLoadingListener,
									mImageLoadingProgressListener);
		}
		// 2 for video: scheme is file or content
		else if (UtilVideo.hasVideoExtension(pictureUri)) 
		{
			Uri uri = Uri.parse(pictureUri);
			String path = uri.getPath();
			thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);

			// check if video thumb mail exists
			if (thumbnail != null) 
			{
				// // add video play icon overlay
				thumbnail = UtilImage.setIconOnThumbnail(thumbnail,	bmVideoIcon, 50);
				UilCommon.imageLoader
						 .displayImage( "drawable://" + R.drawable.ic_media_play,
								 mPicImageView,
								 NoteFragment.mStyle % 2 == 1 ? UilCommon.optionsForRounded_light
										: UilCommon.optionsForRounded_dark,
								 mSimpleImageLoadingListenerForVideo,
								 mImageLoadingProgressListener);
			}
			// video file is not found
			else 
			{
				UilCommon.imageLoader
			 			 .displayImage( "drawable://" + R.drawable.ic_cab_done_holo,
			 					 mPicImageView,
								 NoteFragment.mStyle % 2 == 1 ? UilCommon.optionsForRounded_light
										 : UilCommon.optionsForRounded_dark,
								 mSimpleImageLoadingListener,
								 mImageLoadingProgressListener);

			}
		}
		// 3 for scheme is content: try video and then try image
		else if (!Util.isEmptyString(scheme) && scheme.equalsIgnoreCase("content")) 
		{
			Uri uri = Uri.parse(pictureUri);
			String path = uri.getPath();
			thumbnail = ThumbnailUtils.createVideoThumbnail(path,
					MediaStore.Video.Thumbnails.MICRO_KIND);

			if (thumbnail == null) 
			{
				// refer to
				// http://android-developers.blogspot.tw/2009/05/painless-threading.html
				mVideoAsyncTask = new AsyncTaskVideoBitmap(mAct, pictureUri, mPicImageView);
				mVideoAsyncTask.execute("Searching media ...");

			}

			// if video exists
			if (thumbnail != null) 
			{
				System.out.println("populateFields / scheme is content / could be local video");
				System.out.println("path = " + path);
				// add video play icon overlay
				thumbnail = UtilImage.setIconOnThumbnail(thumbnail,	bmVideoIcon, 50);

				UilCommon.imageLoader
						 .displayImage(	"drawable://" + R.drawable.ic_media_play,
								 		mPicImageView,
								 		NoteFragment.mStyle % 2 == 1 ? UilCommon.optionsForRounded_light
								 				: UilCommon.optionsForRounded_dark,
								 		mSimpleImageLoadingListenerForVideo,
								 		mImageLoadingProgressListener);
			}
			// not exists, so it could be remote image or remote video
			else 
			{
				System.out.println("populateFields / scheme is content / could be image or remote video");
				System.out.println("path = " + path);

				// try remote image or and then try video
				UilCommon.imageLoader
						 .displayImage( pictureUri,
								 		mPicImageView,
								 		NoteFragment.mStyle % 2 == 1 ? UilCommon.optionsForRounded_light_playIcon
								 				: UilCommon.optionsForRounded_dark_playIcon,
								 		mSimpleImageLoadingListener,
								 		mImageLoadingProgressListener);
			}
		} 
		else 
		{
			mPicImageView.setVisibility(View.GONE);
			System.out.println("populateFields / can not decide image and video");
		}
	}
  
    public void setLoadingListerners()
    {
        // set image loading listener
        mSimpleImageLoadingListener = new SimpleImageLoadingListener() 
        {
      	  @Override
      	  public void onLoadingStarted(String imageUri, View view) 
      	  {
//      		  System.out.println("----------------onLoadingStarted 1");
      		  mPicImageView.setVisibility(View.GONE);
      		  mProgressBar.setProgress(0);
      		  mProgressBar.setVisibility(View.VISIBLE);
      	  }

      	  @Override
      	  public void onLoadingFailed(String imageUri, View view, FailReason failReason) 
      	  {
//      		  System.out.println("----------------onLoadingFailed 1");
      		  mProgressBar.setVisibility(View.GONE);
      		  mPicImageView.setVisibility(View.VISIBLE);
      	  }

      	  @Override
      	  public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) 
      	  {
      		  super.onLoadingComplete(imageUri, view, loadedImage);
//      		  System.out.println("----------------onLoadingComplete 1");
      		  mProgressBar.setVisibility(View.GONE);
      		  mPicImageView.setVisibility(View.VISIBLE);
      	  }
  		};

  		// set image loading listener for video
  		mSimpleImageLoadingListenerForVideo = new SimpleImageLoadingListener() 
  		{
  			@Override
  			public void onLoadingStarted(String imageUri, View view) 
  			{
//  				System.out.println("----------------onLoadingStarted 2");
  				mPicImageView.setVisibility(View.GONE);
  				mProgressBar.setProgress(0);
  				mProgressBar.setVisibility(View.VISIBLE);
  			}

  			@Override
  			public void onLoadingFailed(String imageUri, View view, FailReason failReason) 
  			{
//  				System.out.println("----------------onLoadingFailed 2");
  				mProgressBar.setVisibility(View.GONE);
  				mPicImageView.setVisibility(View.VISIBLE);

  			}

  			@Override
  			public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) 
  			{
  				super.onLoadingComplete(imageUri, view, loadedImage);
//  				System.out.println("----------------onLoadingComplete 2");
  				mProgressBar.setVisibility(View.GONE);
  				mPicImageView.setVisibility(View.VISIBLE);
  				// set thumb nail bitmap instead of video play icon
  				mPicImageView.setImageBitmap(thumbnail);
  			}
  		};

  		// Set image loading process listener
  		mImageLoadingProgressListener = new ImageLoadingProgressListener() 
  		{
  			@Override
  			public void onProgressUpdate(String imageUri, View view, int current, int total) 
  			{
  				mProgressBar.setProgress(Math.round(100.0f * current / total));
  			}
  		};
    	
    }
}
