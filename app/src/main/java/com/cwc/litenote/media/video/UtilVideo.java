package com.cwc.litenote.media.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

import com.cwc.litenote.R;
import com.cwc.litenote.media.image.TouchImageView;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.util.UilCommon;
import com.cwc.litenote.util.Util;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;

public class UtilVideo 
{
	public static FragmentActivity mFragAct;
	private static final String TAG_VIDEO = "Video View";
	public final static int VIDEO_AT_STOP = 1;
	public final static int VIDEO_AT_PLAY = 2;
	public final static int VIDEO_AT_PAUSE = 3;

	public static VideoViewCustom mVideoView;
	public static int mVideoState;
	public static int mPlayVideoPosition;
	static String mPictureString;
	public static String currentPicturePath;
	public static View mCurrentPagerView;
	public static Button mVideoPlayButton;
	
	UtilVideo()	{}
	
	/***********************************************
	* init video view
	* 
	initVideoView
		setVideoViewLayout
				getBitmapDrawableByPath
				branch __ new PagerVideoAsyncTask
					   |_____ setVideoViewDimensions()
						   |_ setBitmapDrawableToVideoView
		setVideoViewUI
	*/
	public static void initVideoView(final String strPicture, FragmentActivity fmAct, final LayoutInflater inflater, final ViewGroup container)
    {
    	System.out.println("UtilVideo / initVideoView");
		mFragAct = fmAct;
		mPictureString = strPicture;
    	String scheme = Uri.parse(mPictureString).getScheme();
    	if( hasVideoExtension(mPictureString))		
	  	{
        	System.out.println("UtilVideo / initVideoView / has video extenion");
        	setVideoViewLayout();
   			setVideoViewUI();
   			
			if(mPlayVideoPosition > 0)
				playOrPauseVideo();
  		}
    	else if( !UtilImage.hasImageExtension(mPictureString) &&
    			 (!Util.isEmptyString(scheme) && scheme.equalsIgnoreCase("content")) )
  		{
			Uri uri = Uri.parse(mPictureString);
			String path = uri.getPath();
			Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
			
			if(bmThumbnail == null)
			{
				int style = Note_view_pager.getStyle();
				// load image
				final TouchImageView imageView = (TouchImageView) mCurrentPagerView.findViewById(R.id.image_view);
				UilCommon.imageLoader
				 		 .displayImage( strPicture ,
				 				 		imageView,
				 				 		style%2 == 1 ? UilCommon.optionsForRounded_light:
				 				 					   UilCommon.optionsForRounded_dark,
					  new SimpleImageLoadingListener() 
				 	  {     
				      	   @Override
				      	   public void onLoadingStarted(String imageUri, View view) {
				      	   }

				      	   @Override
				      	   public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				      		   // algorithm:
				      		   // check extension: not video
				      		   // check extension: not image
				      		   // check content: here, not remote image since loading failed
				      		   // so it could be a remote video
			      			   System.out.println("initVideoView / scheme is content / comes from remote video");
			      			   // load video
			      			   setVideoViewLayout();
			      			   setVideoViewUI();
			      	        	if(mPlayVideoPosition > 0)
			      	        		playOrPauseVideo();
				      	   }
				      	   
				      	   @Override
				      	   public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			      			   System.out.println("initVideoView / scheme is content / onLoadingComplete / so this is an image");
			      			   // remote content could be image or video
			      			   // - at failure: show video
			      			   // - at complete: show image again by calling showImageByTouchImageView(spinner, imageView, strPicture) again
			      			   if(mVideoView != null)
			      				   mVideoView.setVisibility(View.GONE);
			      			   
			      			   View pagerView = (ViewGroup) inflater.inflate(R.layout.note_view_pager, container, false);
			      			   final ProgressBar spinner = (ProgressBar) pagerView.findViewById(R.id.loading);
			      			   Note_view_pager.showImageByTouchImageView(spinner, imageView, strPicture);
				      	   }
				      	   
				 	  }, 
				 	  new ImageLoadingProgressListener() 
				 	  {
				 		  @Override
				 		  public void onProgressUpdate(String imageUri, View view, int current, int total) {
				 		  }
				 	  }	
				 );	
			}
  		}
    } // handle video entry
	
    public static void setVideoViewUI()
    {
    	System.out.println("UtilVideo / _setVideoViewUI");

		if(mPlayVideoPosition == 0)
		{
	    	System.out.println("UtilVideo / _setVideoViewUI / mPlayVideoPosition  == 0 ");
			mVideoState = VIDEO_AT_STOP;
	        if(hasVideoExtension(Note_view_pager.getCurrentPictureString()))
	        	showVideoPlayButtonState();
		}
		else // for Continue play
		{
	    	System.out.println("UtilVideo / _setVideoViewUI / mPlayVideoPosition = " + mPlayVideoPosition);
			mVideoState = VIDEO_AT_PAUSE;
		}
      	mVideoPlayButton.setOnClickListener(videoPlayBtnListener); 
    }
    
    public static BitmapDrawable mBitmapDrawable;
	
    // set video view layout
    public static void setVideoViewLayout()
    {
    	System.out.println("UtilVideo / _setVideoViewLayout");
		// set video view
		ViewGroup viewGroup = (ViewGroup) mCurrentPagerView.findViewById(R.id.imageContent);
	    mVideoPlayButton = (Button) (viewGroup.findViewById(R.id.video_view_play_video));
      	mVideoView = (VideoViewCustom) mCurrentPagerView.findViewById(R.id.video_view);
		mVideoView.setVisibility(View.VISIBLE);
		
		System.out.println("setVideoViewLayout / video view h = " + mVideoView.getHeight());
		System.out.println("setVideoViewLayout / video view w = " + mVideoView.getWidth());
		
		// get bitmap by path
      	mBitmapDrawable = getBitmapDrawableByPath(mFragAct,mPictureString);
      	
      	// if bitmap drawable is null, start an Async task
	  	if(mBitmapDrawable.getBitmap() == null)
	  	{
	      	AsyncTaskVideoBitmapPager mPagerVideoAsyncTask = null;
			mPagerVideoAsyncTask = new AsyncTaskVideoBitmapPager(mFragAct,mPictureString,mVideoView); 
			mPagerVideoAsyncTask.execute("Searching media ...");
	  	}
	  	else // if bitmap is not null, set bitmap drawable to video view directly
	  	{
	  		setVideoViewDimensions(mBitmapDrawable);
	      	// set bitmap drawable to video view
	  		if(UtilVideo.mVideoView.getCurrentPosition() == 0)
	  			setBitmapDrawableToVideoView(mBitmapDrawable,mVideoView);
	  	}
		
//		if(mVideoView != null)
//		{
//	    	System.out.println("UtilVideo / _setVideoViewLayout / mVideoView != null");
//	      	mVideoView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
//		}
		
    }    
    
    // Set video view dimensions
    public static void setVideoViewDimensions(BitmapDrawable bitmapDrawable)
    {
  		System.out.println("setVideoViewDimensions");
    	int screenHeight = UtilImage.getScreenHeight(mFragAct);
	    int screenWidth = UtilImage.getScreenWidth(mFragAct);
  		System.out.println("setVideoViewDimensions / screenHeight = " + screenHeight);
  		System.out.println("setVideoViewDimensions / screenWidth = " + screenWidth);
	    
      	int bitmapHeight = 0,bitmapWidth = 0;
      	int config_orientation = mFragAct.getResources().getConfiguration().orientation;

      	Bitmap bitmap = bitmapDrawable.getBitmap();
      	boolean bitmapIsLanscape = false;
      	boolean	bitmapIsPortrait = false;
      	
      	if(bitmap != null)
      	{
      		System.out.println("setVideoViewDimensions / bitmap != null");
      		bitmapHeight = bitmap.getHeight();
      		bitmapWidth = bitmap.getWidth();
      		System.out.println("setVideoViewDimensions / bitmapHeight = " + bitmapHeight);
      		System.out.println("setVideoViewDimensions / bitmapWidth = " + bitmapWidth);
          	bitmapIsLanscape = ( bitmapWidth > bitmapHeight )?true:false;
          	bitmapIsPortrait = ( bitmapHeight > bitmapWidth )?true:false;
          	System.out.println("setVideoViewDimensions / bitmapIsLanscape 1 = " + bitmapIsLanscape);
          	System.out.println("setVideoViewDimensions / bitmapIsPortrait 1 = " + bitmapIsPortrait);
      	}
      	
      	String rotDeg = AsyncTaskVideoBitmapPager.mRotationStr;
      	if(rotDeg != null)
      	{
      		System.out.println("setVideoViewDimensions / rotDeg = " + rotDeg);
      		if( rotDeg.equalsIgnoreCase("0"))
      		{
      	       	bitmapIsLanscape = true;
      	       	bitmapIsPortrait = false;
      	    }
      		else if( rotDeg.equalsIgnoreCase("90"))
      		{
      			bitmapIsLanscape = false;
      			bitmapIsPortrait = true;
      		}
      	}
      	System.out.println("setVideoViewDimensions / bitmapIsLanscape 2 = " + bitmapIsLanscape);
      	System.out.println("setVideoViewDimensions / bitmapIsPortrait 2 = " + bitmapIsPortrait);
      		
      	// for landscape screen
  		if (config_orientation == Configuration.ORIENTATION_LANDSCAPE)
  		{
  			// for landscape bitmap
  			if(bitmapIsLanscape)
  			{
  	          	System.out.println("setVideoViewDimensions / L_scr L_bmp");
  	          	
  	          	if(UtilVideo.mVideoView != null)
  	          	{
  	          		UtilVideo.mVideoView.setDimensions(screenWidth, screenHeight);
  	          		UtilVideo.mVideoView.getHolder().setFixedSize(screenWidth, screenHeight);
  	          	}
  			}// for portrait bitmap
  			else if (bitmapIsPortrait)
  			{
  	          	System.out.println("setVideoViewDimensions / L_scr P_bmp");
  				// set screen height to be constant, and set screen width by proportional
  	          	int propotionalWidth = 0;
  	          	if(bitmap != null)
  	          	{
  	          		propotionalWidth = (bitmapWidth > bitmapHeight)?
  							  		   Math.round(screenHeight * bitmapHeight/bitmapWidth) : 
  							  		   Math.round(screenHeight * bitmapWidth/bitmapHeight);
  	          	}
  	          	else
  	          		propotionalWidth = Math.round(screenHeight * screenHeight/screenWidth);
  	          	
  	          	if(UtilVideo.mVideoView != null)
  	          	{
  	          		UtilVideo.mVideoView.setDimensions(propotionalWidth, screenHeight);
  	          		UtilVideo.mVideoView.getHolder().setFixedSize(propotionalWidth, screenHeight);
  	          	}
  			}
  		}// for portrait screen
  		else if (config_orientation == Configuration.ORIENTATION_PORTRAIT)
  		{
  			// for landscape bitmap
  			if(bitmapIsLanscape)
  			{
  	          	System.out.println("setVideoViewDimensions / P_scr L_bmp");
	    		// set screen width to be constant, and set screen height by proportional
  	          	int propotiaonalHeight = 0;
  	          	if(bitmap != null)
  	          	{
  	          		
  	          		propotiaonalHeight = (bitmapWidth > bitmapHeight)?
  	          							  Math.round(screenWidth * bitmapHeight/bitmapWidth) : 
  	          							  Math.round(screenWidth * bitmapWidth/bitmapHeight);
  	          	}
  	          	else
  	          		propotiaonalHeight = Math.round(screenWidth * screenWidth/screenHeight );
  	          	
  	          	if(UtilVideo.mVideoView != null)
  	          	{
  	          		UtilVideo.mVideoView.setDimensions(screenWidth, propotiaonalHeight);
  	          		UtilVideo.mVideoView.getHolder().setFixedSize(screenWidth, propotiaonalHeight);
  	          	}
  			}// for portrait bitmap
  			else if (bitmapIsPortrait)
  			{
  	          	System.out.println("setVideoViewDimensions / P_scr P_bmp");
  	          	
  	          	if(UtilVideo.mVideoView != null)
  	          	{
  	          		UtilVideo.mVideoView.setDimensions(screenWidth, screenHeight);
  	          		UtilVideo.mVideoView.getHolder().setFixedSize(screenWidth, screenHeight);
  	          	}
  			}
  		}
  	} //setVideoViewDimensions
    
    
    // on global layout listener
//	static OnGlobalLayoutListener onGlobalLayoutListener = new OnGlobalLayoutListener() 
//	{
//		@SuppressWarnings("deprecation")
//		@Override
//		public void onGlobalLayout() 
//		{
//			//action bar height is 120(landscape),144(portrait)
//			System.out.println("OnGlobalLayoutListener / getActionBar().getHeight() = " + mFragAct.getActionBar().getHeight());
//
//	      	// get bitmap by path
//	      	BitmapDrawable bitmapDrawable = getBitmapDrawableByPath(mFragAct,mPictureString);
//	      	
//		  	if(bitmapDrawable.getBitmap() == null)
//		  	{
//		      	PagerVideoAsyncTask mPagerVideoAsyncTask = null;
//				mPagerVideoAsyncTask = new PagerVideoAsyncTask(mFragAct,mPictureString,mVideoView); 
//				mPagerVideoAsyncTask.execute("Searching media ...");
//		  	}
//		  	else
//		      	// set bitmap drawable to video view
//		  		setBitmapDrawableToVideoView(bitmapDrawable,mVideoView);
//		} 
//	};	
	
	// Set Bitmap Drawable to Video View
	public static void setBitmapDrawableToVideoView(BitmapDrawable bitmapDrawable, VideoViewCustom videoView)
  	{
		//set bitmap drawable to video view
		System.out.println(" UtilVideo / setBitmapDrawableToVideoView / mPlayVideoPosition = " + mPlayVideoPosition);
		if(Build.VERSION.SDK_INT >= 16)
		{
			if(mPlayVideoPosition == 0)
				videoView.setBackground(bitmapDrawable);
		}
		else
		{
			if(mPlayVideoPosition == 0)
				videoView.setBackgroundDrawable(bitmapDrawable);
		}
		
		//??? add the following, why 720p video shows small bitmap?
		//this is an important step not to keep receiving callback
		//we should remove this listener
//		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
//			videoView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
//		else
//			videoView.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
  	}
	
	// get bitmap drawable by path
	static BitmapDrawable getBitmapDrawableByPath(Activity mAct,String picPathStr)
	{
		String path = Uri.parse(picPathStr).getPath();
		Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(mAct.getResources(),bmThumbnail);
		return bitmapDrawable;
	}
	
	public static VideoPlayer mVideoPlayer;
	// Play or Pause video
	public static void playOrPauseVideo()
	{
		System.out.println("UtilVideo / _playOrPauseVideo");
		
		if( mVideoView!= null)
			System.out.println("UtilVideo / _playOrPauseVideo / mVideoView != null");
		else
			System.out.println("UtilVideo / _playOrPauseVideo / mVideoView == null");

		if( mVideoView!= null)
		{
			if((!mVideoView.isPlaying()) && (mVideoState == VIDEO_AT_STOP) )
			{
				if(Build.VERSION.SDK_INT >= 16)
					mVideoView.setBackground(null);
				else
					mVideoView.setBackgroundDrawable(null);
			
				mVideoView.setVisibility(View.VISIBLE);
				
				//start a new Video player instance
				mVideoPlayer = new VideoPlayer(mFragAct);
			}
			else if(mVideoPlayer != null)
			{
				mVideoPlayer.goOnVideo();
			}
		}
	}
	
	// get video data source
	public static String getVideoDataSource(String path) throws IOException
	{
		if (!URLUtil.isNetworkUrl(path)) 
		{
			return path;
		} 
		else 
		{
			URL url = new URL(path);
			URLConnection cn = url.openConnection();
			cn.connect();
			InputStream stream = cn.getInputStream();
			
			if (stream == null)
				throw new RuntimeException("stream is null");
			
			File temp = File.createTempFile("mediaplayertmp", "dat");
			temp.deleteOnExit();
			String tempPath = temp.getAbsolutePath();
			FileOutputStream out = new FileOutputStream(temp);
			byte buf[] = new byte[128];
			
			do
			{
				int numread = stream.read(buf);
				if (numread <= 0)
					break;
				out.write(buf, 0, numread);
			} while (true);
			
			try 
			{
				stream.close();
				out.close();
			} catch (IOException ex) 
			{
				Log.e(TAG_VIDEO, "error: " + ex.getMessage(), ex);
			}
			return tempPath;
		}
	}	
	
    // check if file has video extension
    // refer to http://developer.android.com/intl/zh-tw/guide/appendix/media-formats.html
    public static boolean hasVideoExtension(File file)
    {
    	boolean isVideo = false;
    	String fn = file.getName().toLowerCase(Locale.getDefault());
    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||
    		fn.endsWith("ts") || fn.endsWith("webm") || fn.endsWith("mkv")  ) 
	    	isVideo = true;
	    
    	return isVideo;
    } 
    
    // check if string has video extension
    public static boolean hasVideoExtension(String string)
    {
    	boolean hasVideo = false;
    	if(!Util.isEmptyString(string))
    	{
	    	String fn = string.toLowerCase(Locale.getDefault());
	    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||
	    		fn.endsWith("ts") || fn.endsWith("webm") || fn.endsWith("mkv")  ) 
		    	hasVideo = true;
    	}
    	return hasVideo;
    } 
    
    // show video play button state
    public static void showVideoPlayButtonState()
    {
    	Button btn = mVideoPlayButton;
        // show video play button icon
    	
    	if(btn != null)
    	{
	        if(mVideoState == VIDEO_AT_PLAY)
	        {
	        	System.out.println("showVideoPlayButtonState / mVideoState == VIDEO_AT_PLAY");
	        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0, 0, 0);
	        	btn.setVisibility(View.VISIBLE);
	        }
	        else if(mVideoState == VIDEO_AT_PAUSE)
	        {
	        	System.out.println("showVideoPlayButtonState / mVideoState == VIDEO_AT_PAUSE");
	        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0, 0, 0);
	        	btn.setVisibility(View.VISIBLE);
	        }
	        else if(mVideoState == VIDEO_AT_STOP)
	        {
	        	System.out.println("showVideoPlayButtonState / mVideoState == VIDEO_AT_STOP");
	        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0, 0, 0);
	        	btn.setVisibility(View.VISIBLE);
	        }
	        else
	        	System.out.println("showVideoPlayButtonState / mVideoState == na");
    	}
    }    
    
    // update video play button state
    public static void updateVideoPlayButtonState()
    {
    	Button btn = mVideoPlayButton;
        // show video play button icon
        if(mVideoState == VIDEO_AT_PLAY)
        {
        	btn.setVisibility(View.GONE);
//        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0, 0, 0);
        }
        else if(mVideoState == VIDEO_AT_PAUSE)
        {
        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0, 0, 0);
        	btn.setVisibility(View.VISIBLE);
        }
        else if(mVideoState == VIDEO_AT_STOP)
        {
        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0, 0, 0);
        	btn.setVisibility(View.VISIBLE);
        }
    }
    
    public static boolean playWillPause = false;
    // video play button OnClickListener
    public static OnClickListener videoPlayBtnListener = new View.OnClickListener() 
    {
        public void onClick(View view) 
        {
        	if(mVideoState == VIDEO_AT_PLAY)
        	{
        		playWillPause = true;
            	System.out.println("videoPlayBtnListener / mVideoState = VIDEO_AT_PLAY");
        	}
        	else if(mVideoState == VIDEO_AT_PAUSE)
        	{
        		playWillPause = false;        		
            	System.out.println("videoPlayBtnListener / mVideoState = VIDEO_AT_PAUSE");
        	}
        	else if(mVideoState == VIDEO_AT_STOP)
        	{
        		playWillPause = false;          		
            	System.out.println("videoPlayBtnListener / mVideoState = VIDEO_AT_STOP");
        	}
        	
        	playOrPauseVideo();
        }
    };        
    
    // choose video intent
    public static Intent chooseVideoIntent(Activity act)
    {
	    // set multiple actions in Intent 
	    // Refer to: http://stackoverflow.com/questions/11021021/how-to-make-an-intent-with-multiple-actions
        Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getContentIntent.setType("video/*");
        PackageManager pm = act.getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(getContentIntent, 0);
        System.out.println("resInfo size = " + resInfo.size());
        Intent openInChooser = Intent.createChooser(getContentIntent, act.getResources().getText(R.string.add_new_chooser_image));
        
        // SAF support starts from Kitkat
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
			// BEGIN_INCLUDE (use_open_document_intent)
	        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
	        Intent openDocumentIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
	        
	        // Filter to only show results that can be "opened", such as a file (as opposed to a list
	        // of contacts or time zones)
	        openDocumentIntent.addCategory(Intent.CATEGORY_OPENABLE);	        
	        openDocumentIntent.setType("video/*");

	        List<ResolveInfo> resInfoSaf = null;
	        resInfoSaf = pm.queryIntentActivities(openDocumentIntent, 0);
	        System.out.println("resInfoSaf size = " + resInfoSaf.size());
	        
	        Intent[] extraIntents = null;
        	extraIntents = new Intent[resInfoSaf.size() + resInfo.size()];
	        
	        Spannable forSaf = new SpannableString(" (CLOUD)");
	        forSaf.setSpan(new ForegroundColorSpan(Color.RED), 0, forSaf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	 
	        for (int i = 0; i < resInfoSaf.size(); i++) {
	            // Extract the label, append it, and repackage it in a LabeledIntent
	            ResolveInfo ri = resInfoSaf.get(i);
	            String packageName = ri.activityInfo.packageName;
	            Intent intent = new Intent();
	            intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
	            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
	            intent.setType("video/*");
	            CharSequence label = TextUtils.concat(ri.loadLabel(pm), forSaf);
	            extraIntents[i] = new LabeledIntent(intent, packageName, label, ri.icon);
	        } 
	        for (int i = 0; i < resInfo.size(); i++) {
	            // Extract the label, append it, and repackage it in a LabeledIntent
	            ResolveInfo ri = resInfo.get(i);
	            String packageName = ri.activityInfo.packageName;
	            Intent intent = new Intent();
	            intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
	            intent.setAction(Intent.ACTION_GET_CONTENT);
	            intent.setType("video/*");
	            extraIntents[resInfoSaf.size()+i] = new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon);
	        }
	        
        	openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        }
        return openInChooser;
    }
}

