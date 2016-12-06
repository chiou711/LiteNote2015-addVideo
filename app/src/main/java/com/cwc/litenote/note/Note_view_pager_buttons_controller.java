package com.cwc.litenote.note;

import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.video.UtilVideo;
import com.cwc.litenote.media.video.VideoPlayer;
import com.cwc.litenote.util.Util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Note_view_pager_buttons_controller extends BroadcastReceiver 
{
	public static boolean showControl;	
	public static Button imageViewBackButton;
	public static TextView imageTitle;
	public static Button imageViewAudioButton;
	public static Button imageViewModeButton;
	public static Button imagePreviousButton;
	public static Button imageNextButton;
	public static TextView videoCurrPos;
	public static SeekBar videoSeekBar;
	public static TextView videoFileLength;
    public static int videoFileLengthInMilliseconds;
    public static int videoProgress;
    
	public Note_view_pager_buttons_controller(){};
   
	public void SetControllerListener(ViewGroup viewGroup) 		
	{
        imageViewBackButton = (Button) (viewGroup.findViewById(R.id.image_view_back));
        imageViewAudioButton = (Button) (viewGroup.findViewById(R.id.image_view_audio));
        imageViewModeButton = (Button) (viewGroup.findViewById(R.id.image_view_mode));
        imagePreviousButton = (Button) (viewGroup.findViewById(R.id.image_view_previous));
        imageNextButton = (Button) (viewGroup.findViewById(R.id.image_view_next));
        
        videoCurrPos = (TextView) (viewGroup.findViewById(R.id.video_current_pos));
        videoSeekBar = (SeekBar)(viewGroup.findViewById(R.id.video_seek_bar));
        videoFileLength = (TextView) (viewGroup.findViewById(R.id.video_file_length));

        // view mode 
    	// picture only
	  	if(Note_view_pager.isPictureMode())
	  	{
			// image: view back
	  		imageViewBackButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back /*android.R.drawable.ic_menu_revert*/, 0, 0, 0);
			// click to finish Note_view_pager
	  		imageViewBackButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) {
	            	Note_view_pager_adapter.stopAV();
	            	Note_view_pager_adapter.mAct.finish();
	            }
	        });   
			
			// click to play audio 
	  		imageViewAudioButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_ringer_on, 0, 0, 0);
	  		imageViewAudioButton.setOnClickListener(new View.OnClickListener() {

	            public void onClick(View view) {
	            	TabsHostFragment.setPlayingTab_WithHighlight(false);// in case playing audio in pager
	            	Note_view_pager.playAudioInPager();
	            }
	        });       			
			
			// image: view mode
	  		imageViewModeButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_view, 0, 0, 0);
			// click to select view mode 
	  		imageViewModeButton.setOnClickListener(new View.OnClickListener() {

	            public void onClick(View view) {
//	            	Note_view_pager.mAct.invalidateOptionsMenu();
            		Note_view_pager.mMenu.performIdentifierAction(R.id.VIEW_NOTE_MODE, 0);
            		//fix: update current video position will cause view mode option menu disappear
            		showControl = false; 
	            }
	        });       			
			
			// image: previous button
	  		imagePreviousButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_previous, 0, 0, 0);
			// click to previous 
	  		imagePreviousButton.setOnClickListener(new View.OnClickListener() 
	        {
	            public void onClick(View view) {
	            	// since onPageChanged is not called fast enough, add stop functions below
	            	Note_view_pager_adapter.stopAV();
	            	Note_view_pager.mPager.setCurrentItem(Note_view_pager.mPager.getCurrentItem() - 1);
	            }
	        });   
	        
			// image: next button
	  		imageNextButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_next, 0, 0, 0);
			// click to next 
	  		imageNextButton.setOnClickListener(new View.OnClickListener()
	        {
	            public void onClick(View view) {
	            	// since onPageChanged is not called fast enough, add stop functions below
	            	Note_view_pager_adapter.stopAV();
	            	Note_view_pager.mPager.setCurrentItem(Note_view_pager.mPager.getCurrentItem() + 1);
	            }
	        }); 
	  		
	  	}
	  	
	  	if(Note_view_pager.isPictureMode()|| Note_view_pager.isViewAllMode())
	  	{
			// set video seek bar listener
			videoSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
			{
				// onStartTrackingTouch
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					System.out.println("Note_view_pager_buttons_controller / _onStartTrackingTouch");
					if( (UtilVideo.mVideoPlayer == null)  && (UtilVideo.mVideoView != null))
					{
						if(Build.VERSION.SDK_INT >= 16)
							UtilVideo.mVideoView.setBackground(null);
						else
							UtilVideo.mVideoView.setBackgroundDrawable(null);
						
						UtilVideo.mVideoView.setVisibility(View.VISIBLE);
						UtilVideo.mVideoPlayer = new VideoPlayer(UtilVideo.mFragAct);
//						UtilVideo.mVideoView.seekTo(mPlayVideoPosition);
					}
				}
				
				// onProgressChanged
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
				{
					System.out.println("Note_view_pager_buttons_controller / _onProgressChanged");
					if(fromUser)
					{	
						// show progress change
				    	int currentPos = videoFileLengthInMilliseconds*progress/(seekBar.getMax()+1);
				    	// update current play time
				     	videoCurrPos.setText(Util.getTimeFormatString(currentPos));
					}
				}
				
				// onStopTrackingTouch
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) 
				{
					System.out.println("Note_view_pager_buttons_controller / _onStopTrackingTouch");
					if( UtilVideo.mVideoView != null  )
					{
						int mPlayVideoPosition = (int) (((float)(videoFileLengthInMilliseconds / 100)) * seekBar.getProgress());
						
						if(UtilVideo.mVideoPlayer != null)
						{
							UtilVideo.mVideoView.seekTo(mPlayVideoPosition);
						}
					}
				}	
				
			});	
	  	}
   }

	public static void delayPictureControl(Context context, long timeMilliSec) 
	{
		 Intent intent = new Intent(context, Note_view_pager_buttons_controller.class);
		 AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		 PendingIntent pendIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		 alarmMgr.set(AlarmManager.RTC_WAKEUP, timeMilliSec, pendIntent);
	}

	@Override
	public void onReceive(final Context context, Intent intent) 
	{
		System.out.println("Note_view_pager_buttons_controller / _onReceive");
		// add for fixing exception after App is not alive, but PendingInetent still run as plan
		if(Note_view_pager.mPager != null)  
		{
			String tagImageStr = "current"+ Note_view_pager.mPager.getCurrentItem() +"imageView";
			ViewGroup imageGroup = (ViewGroup) Note_view_pager.mPager.findViewWithTag(tagImageStr);
	       
			if(imageGroup != null)
			{
				Button videoViewPlayButton = (Button) (imageGroup.findViewById(R.id.video_view_play_video));
		       
				imageViewBackButton = (Button) (imageGroup.findViewById(R.id.image_view_back));
				imageTitle = (TextView) (imageGroup.findViewById(R.id.image_title));
				imageViewAudioButton = (Button) (imageGroup.findViewById(R.id.image_view_audio));
				imageViewModeButton = (Button) (imageGroup.findViewById(R.id.image_view_mode));

				imagePreviousButton = (Button) (imageGroup.findViewById(R.id.image_view_previous));
				videoCurrPos = (TextView) (imageGroup.findViewById(R.id.video_current_pos));
				videoSeekBar = (SeekBar)(imageGroup.findViewById(R.id.video_seek_bar));
				videoFileLength = (TextView) (imageGroup.findViewById(R.id.video_file_length));
				imageNextButton = (Button) (imageGroup.findViewById(R.id.image_view_next));
		       
				// to distinguish image and video, does not show video play icon 
				// only when video is playing
				if(UtilVideo.mVideoState == UtilVideo.VIDEO_AT_PLAY)
					videoViewPlayButton.setVisibility(View.GONE);
		       
				imageViewBackButton.setVisibility(View.GONE);
				imageTitle.setVisibility(View.GONE);
				imageViewAudioButton.setVisibility(View.GONE);
				imageViewModeButton.setVisibility(View.GONE);
				imagePreviousButton.setVisibility(View.GONE);
				videoCurrPos.setVisibility(View.GONE);
				videoSeekBar.setVisibility(View.GONE);
				videoFileLength.setVisibility(View.GONE);
				imageNextButton.setVisibility(View.GONE);
			   
				showControl = false;
			   
				// set Full screen when buttons are off
				if(Note_view_pager.isPictureMode())
				{
					Window win = Note_view_pager.mAct.getWindow();
					win.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);		
					win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								 WindowManager.LayoutParams.FLAG_FULLSCREEN);	
				}
			}
		}
	}
   
	static void showControlButtons(int position)
	{
        String tagImageStr = "current"+ position +"imageView";
        System.out.println("tagImageStr = " + tagImageStr);
        ViewGroup imageGroup = (ViewGroup) Note_view_pager.mPager.findViewWithTag(tagImageStr);
        
        if(imageGroup != null)
        {
	        imageViewBackButton = (Button) (imageGroup.findViewById(R.id.image_view_back));
	        imageTitle = (TextView) (imageGroup.findViewById(R.id.image_title));
	        imageViewAudioButton = (Button) (imageGroup.findViewById(R.id.image_view_audio));
	        imageViewModeButton = (Button) (imageGroup.findViewById(R.id.image_view_mode));
	        
	        imagePreviousButton = (Button) (imageGroup.findViewById(R.id.image_view_previous));
	        videoCurrPos = (TextView) (imageGroup.findViewById(R.id.video_current_pos));
	        videoSeekBar = (SeekBar)(imageGroup.findViewById(R.id.video_seek_bar));
	        videoFileLength = (TextView) (imageGroup.findViewById(R.id.video_file_length));
	        imageNextButton = (Button) (imageGroup.findViewById(R.id.image_view_next));

        	if(Note_view_pager.isPictureMode())
        		imageViewBackButton.setVisibility(View.VISIBLE);
        	else
        		imageViewBackButton.setVisibility(View.GONE);
        	
	        // set image title
		    Note_view_pager.mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
        	String strPicture = Note_view_pager.mDb.getNotePictureUri(position);
        	Note_view_pager.mDb.doClose();

			if(!Util.isEmptyString(strPicture))
			{
				strPicture = Util.getDisplayNameByUriString(strPicture, Note_view_pager.mAct);
			}
			else
				strPicture = "";		        
	        
			if(!Util.isEmptyString(strPicture))
			{
				imageTitle.setVisibility(View.VISIBLE);
				imageTitle.setText(strPicture);
			}
			else
				imageTitle.setVisibility(View.INVISIBLE);
			
	        if(UtilVideo.hasVideoExtension(strPicture))
	        	UtilVideo.showVideoPlayButtonState();
	        
	        // audio playing state for one time mode
	        if((AudioPlayer.mPlayMode == AudioPlayer.ONE_TIME_MODE) &&
	           (position == AudioPlayer.mAudioIndex))
	        {
        		if(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PLAY)
        			imageViewAudioButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audio_selected, 0, 0, 0);
        		else if((AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PAUSE) ||
        				(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_STOP)    )
        			imageViewAudioButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_ringer_on, 0, 0, 0);
	        }
	        
	        
	        if(Note_view_pager.currentNoteHasAudioUri())
	        	imageViewAudioButton.setVisibility(View.VISIBLE);
	        else
	        	imageViewAudioButton.setVisibility(View.GONE);
	        
	        if(Note_view_pager.isPictureMode())
	        {
	        	imageViewModeButton.setVisibility(View.VISIBLE);
	        	
	        	imagePreviousButton.setVisibility(View.VISIBLE);
		        imagePreviousButton.setEnabled(position==0? false:true);
		        imagePreviousButton.setAlpha(position==0? 0.1f:1f);

		        imageNextButton.setVisibility(View.VISIBLE);
		        imageNextButton.setAlpha(position == (Note_view_pager.mPagerAdapter.getCount()-1 )? 0.1f:1f);
		        imageNextButton.setEnabled(position == (Note_view_pager.mPagerAdapter.getCount()-1 )? false:true);
	        }
	        else
	        {
	        	imageViewModeButton.setVisibility(View.GONE);
	        	imagePreviousButton.setVisibility(View.GONE);		        	
				imageNextButton.setVisibility(View.GONE);
	        }
			
			// show seek bar for video only
			if(Note_view_pager.currentNoteHasVideoUri())
			{
				primaryVideoSeekBarProgressUpdater();
			}
			else
			{
				videoCurrPos.setVisibility(View.GONE);
				videoSeekBar.setVisibility(View.GONE);
				videoFileLength.setVisibility(View.GONE);
			}
		    
	        // set Not full screen
	        if(Note_view_pager.isPictureMode())
	        	Note_view_pager.mAct.getActionBar().hide();
	        else
	        	Note_view_pager.mAct.getActionBar().show();
	        
	        Window win = Note_view_pager.mAct.getWindow();
	        win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        win.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
	  	    			 WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	        
	        showControl = true;
	    	delayPictureControl(Note_view_pager.mAct, System.currentTimeMillis() + 1000 * 5); // for 5 seconds		        
        }
        
        //To show buttons or not is dependent on view mode
	  	if(Note_view_pager.isViewAllMode()|| Note_view_pager.isTextMode())
	  	{
	  		Note_view_pager.editButton.setVisibility(View.VISIBLE);
	  		Note_view_pager.sendButton.setVisibility(View.VISIBLE);
	  		Note_view_pager.backButton.setVisibility(View.VISIBLE);
	  	    
	    	if(!Util.isEmptyString(Note_view_pager.mAudioTextView.getText().toString()) )
	    		Note_view_pager.mAudioTextView.setVisibility(View.VISIBLE);
	    	else
	    		Note_view_pager.mAudioTextView.setVisibility(View.GONE);
	  	}
	  	else if(Note_view_pager.isPictureMode() )
	  	{	
	  		Note_view_pager.editButton.setVisibility(View.GONE);
	  		Note_view_pager.sendButton.setVisibility(View.GONE);
	  		Note_view_pager.backButton.setVisibility(View.GONE);
	  		Note_view_pager.mAudioTextView.setVisibility(View.GONE);
	  	}	        
   	} //showImageControlButtons
   
   	public static void primaryVideoSeekBarProgressUpdater() 
   	{
	   	System.out.println("Note_view_pager_buttons_controller / _primaryVideoSeekBarProgressUpdater ");

	   	// get current position
	   	int currentPos = 0;
	   	if( UtilVideo.mVideoView != null)
	   		currentPos = UtilVideo.mVideoView.getCurrentPosition();

	    // show current play position
	   	videoCurrPos.setText(Util.getTimeFormatString(currentPos));
		videoCurrPos.setVisibility(View.VISIBLE);
//	   	int curHour = Math.round((float)(currentPos / 1000 / 60 / 60));
//	   	int curMin = Math.round((float)((currentPos - curHour * 60 * 60 * 1000) / 1000 / 60));
//	    int curSec = Math.round((float)((currentPos - curHour * 60 * 60 * 1000 - curMin * 60 * 1000)/ 1000));
//    	videoCurrPos.setText(String.format("%2d", curHour)+":" +
//   							 String.format("%02d", curMin)+":" +
//   							 String.format("%02d", curSec) );
	   	
	   	// show seek bar progress
		videoSeekBar.setVisibility(View.VISIBLE);
		videoSeekBar.setMax(99);
	   	videoProgress = (int)(((float)currentPos/videoFileLengthInMilliseconds)*100);
	   	videoSeekBar.setProgress(videoProgress);
   	
		// show file length
		String curPicStr = Note_view_pager.getCurrentPictureString();
		if(!Util.isEmptyString(curPicStr))
		{
			MediaPlayer mp = MediaPlayer.create(Note_view_pager_adapter.mAct, Uri.parse(curPicStr));
			videoFileLengthInMilliseconds = mp.getDuration();
			mp.release();
			
			// set file length
			videoFileLength.setText(Util.getTimeFormatString(videoFileLengthInMilliseconds));
			videoFileLength.setVisibility(View.VISIBLE);
		}
   }   
}
