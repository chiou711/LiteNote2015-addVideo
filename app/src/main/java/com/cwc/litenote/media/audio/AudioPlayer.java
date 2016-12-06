package com.cwc.litenote.media.audio;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.note.Note_view_pager_buttons_controller;
import com.cwc.litenote.util.Util;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public class AudioPlayer 
{
	private static final String TAG = "AUDIO_PLAYER"; // error logging tag
	static final int DURATION_1S = 1000; // 1 seconds per slide
	static AudioInfo mAudioInfo; // slide show being played
	public static Handler mAudioHandler; // used to update the slide show
	public static int mAudioIndex; // index of current media to play
	public static int mPlaybackTime; // time in miniSeconds from which media should play 
	public static MediaPlayer mMediaPlayer; // plays the background music, if any
	static Activity mAct;
	public static int mPlayerState;
	public static int PLAYER_AT_STOP = 0;
	public static int PLAYER_AT_PLAY = 1;
	public static int PLAYER_AT_PAUSE = 2;
	public static int mPlayMode;
	static int mTryTimes; // use to avoid useless looping in Continue mode
	public final static int ONE_TIME_MODE = 0;
	public final static int CONTINUE_MODE = 1;
   
	public AudioPlayer(FragmentActivity fa)  
	{
		System.out.println("AudioPlayer constructor");
		
		mTryTimes = 0;
		mAct = fa;
		
		if(mPlayerState == PLAYER_AT_PLAY) 
			startAudio();
	};

	//
	// One time mode
	//
	public static Runnable mRunOneTimeMode = new Runnable()
	{   @Override
		public void run()
		{
	   		if(mMediaPlayer == null)
	   		{
	   			String audioStr = mAudioInfo.getAudioAt(mAudioIndex);
	   			if(AsyncTaskAudioUrlVerify.mIsOkUrl)
	   			{
				    System.out.println("Runnable updateMediaPlay / play mode: OneTime");
	   				
				    //create a MediaPlayer
				    mMediaPlayer = new MediaPlayer();
	   				mMediaPlayer.reset();
	   				
	   				//set audio player listeners
	   				setAudioPlayerListeners();
	   				
	   				try
	   				{
	   					mMediaPlayer.setDataSource(mAct, Uri.parse(audioStr));
	   					
					    // prepare the MediaPlayer to play, this will delay system response 
   						mMediaPlayer.prepare();
   						
	   					//Note: below
	   					//Set 1 second will cause Media player abnormal on Power key short click
	   					mAudioHandler.postDelayed(mRunOneTimeMode,DURATION_1S * 2);
	   				}
	   				catch(Exception e)
	   				{
	   					Toast.makeText(mAct,R.string.audio_message_could_not_open_file,Toast.LENGTH_SHORT).show();
	   					stopAudio();
	   				}
	   			}
	   			else
	   			{
	   				Toast.makeText(mAct,R.string.audio_message_no_media_file_is_found,Toast.LENGTH_SHORT).show();
   					stopAudio();
	   			}
	   		}
	   		else if(mMediaPlayer != null)
	   		{
				mAudioHandler.postDelayed(mRunOneTimeMode,DURATION_1S);
	   		}		    		
		} 
	};

	//
	// Continue mode
	//
	public static String mAudioStr;
	public static Runnable mRunContinueMode = new Runnable()
	{   @Override
		public void run()
		{
	   		if( AudioInfo.getAudioMarking(mAudioIndex) == 1 )
	   		{ 
	   			if(mMediaPlayer == null)
	   			{
		    		// check if audio file exists or not
   					mAudioStr = mAudioInfo.getAudioAt(mAudioIndex);

					if(!AsyncTaskAudioUrlVerify.mIsOkUrl)
					{
						mTryTimes++;
						playNextAudio();
					}
					else
   					{
   						System.out.println("* Runnable updateMediaPlay / play mode: continue");
	   					
   						//create a MediaPlayer 
   						mMediaPlayer = new MediaPlayer(); 
	   					mMediaPlayer.reset();
	   					willPlayNext = true; // default: play next
	   					NoteFragment.mProgress = 0;
	   					
	   					// for network stream buffer change
	   					mMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener()
	   					{
	   						@Override
	   						public void onBufferingUpdate(MediaPlayer mp, int percent) {
	   							NoteFragment.seekBarProgress.setSecondaryProgress(percent);
	   						}
	   					});
   						
	   					// set listeners
   						setAudioPlayerListeners();
   						
   						try
   						{
   							// set data source
							mMediaPlayer.setDataSource(mAct, Uri.parse(mAudioStr));
   							
   							// prepare the MediaPlayer to play, could delay system response
   							mMediaPlayer.prepare();
   						}
   						catch(Exception e)
   						{
   							System.out.println("on Exception");
   							Log.e(TAG, e.toString());
							mTryTimes++;
   							playNextAudio();
   						}
   					}
	   			}
	   			else if(mMediaPlayer != null )
	   			{
	   				// keep looping, do not set post() here, it will affect slide show timing
	   				if(mTryTimes < AudioInfo.getAudioFilesSize())
	   				{
						// update seek bar
	   					NoteFragment.primarySeekBarProgressUpdater();
						
						if(mTryTimes == 0)
							mAudioHandler.postDelayed(mRunContinueMode,DURATION_1S);
						else
							mAudioHandler.postDelayed(mRunContinueMode,DURATION_1S/10);
	   				}
	   			}
	   		}
	   		else if( AudioInfo.getAudioMarking(mAudioIndex) == 0 )// for non-marking item
	   		{
	   			System.out.println("--- for non-marking item");
	   			// get next index
	   			if(willPlayNext)
	   				mAudioIndex++;
	   			else
	   				mAudioIndex--;
	   			
	   			if( mAudioIndex >= AudioInfo.getAudioList().size())
	   				mAudioIndex = 0; //back to first index
	   			else if( mAudioIndex < 0)
	   			{
	   				mAudioIndex ++;
	   				willPlayNext = true;
	   			}
	   			
	   			startAudio();
	   		}
		} 
	};	
	
	static boolean mIsPrepared;
	static void setAudioPlayerListeners()	
	{
			// - on completion listener
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener()
			{	@Override
				public void onCompletion(MediaPlayer mp) 
				{
					System.out.println("onCompletion");
					
					if(mMediaPlayer != null)
								mMediaPlayer.release();
	
					mMediaPlayer = null;
					mPlaybackTime = 0;
					
					// get next index
					if(mPlayMode == CONTINUE_MODE)
					{
						mTryTimes = 0; //reset try times 
						mAudioIndex++;
						if(mAudioIndex == AudioInfo.getAudioList().size())
							mAudioIndex = 0;	// back to first index
						
						startAudio();
			    		NoteFragment.mItemAdapter.notifyDataSetChanged();
					}
					else // one time mode
					{
	   					stopAudio();
					}
				}
			});
			
			// - on prepared listener
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener()
			{	@Override
				public void onPrepared(MediaPlayer mp) 
				{
					System.out.println("onPrepared");

					// set seek bar progress
					NoteFragment.mediaFileLengthInMilliseconds = mMediaPlayer.getDuration(); // gets the song length in milliseconds from URL
					NoteFragment.primarySeekBarProgressUpdater();
  						
					// set footer message: media name
					NoteFragment.setFooterAudioControl(mAudioStr);//??? not for pager mode, add getMode?
					
					if(mMediaPlayer!= null)
					{
						mIsPrepared = true;
						mMediaPlayer.start();
						mMediaPlayer.getDuration();
						mMediaPlayer.seekTo(mPlaybackTime);	
						
						// set highlight of playing tab
						if((mPlayMode == CONTINUE_MODE) &&
						   (DrawerActivity.mCurrentPlaying_DrawerIndex == DrawerActivity.mFocusDrawerPos) )
							TabsHostFragment.setPlayingTab_WithHighlight(true);
						else
							TabsHostFragment.setPlayingTab_WithHighlight(false);
						
						NoteFragment.mItemAdapter.notifyDataSetChanged();
						
						// add for calling runnable
						if(mPlayMode == CONTINUE_MODE )
							mAudioHandler.postDelayed(mRunContinueMode,Util.oneSecond/4);
					}						
				}
				
			});	 
			
			// - on error listener
			mMediaPlayer.setOnErrorListener(new OnErrorListener()
			{	@Override
				public boolean onError(MediaPlayer mp,int what,int extra) 
				{
					// more than one error when playing an index 
					System.out.println("on Error: what = " + what + " , extra = " + extra);
					return false;
				}
			});
	}
	
	public static AsyncTaskAudioUrlVerify mAudioUrlVerifyTask;
	static void startAudio()
	{
		// remove call backs to make sure next toast will appear soon
		if(mAudioHandler != null)
		{
			mAudioHandler.removeCallbacks(mRunOneTimeMode); 
			mAudioHandler.removeCallbacks(mRunContinueMode);
		}
		
		// start a new handler
		mAudioHandler = new Handler();
		
		if( (mPlayMode == CONTINUE_MODE) && (AudioInfo.getAudioMarking(mAudioIndex) == 0))
		{
			mAudioHandler.postDelayed(mRunContinueMode,Util.oneSecond/4);		}
		else
		{
			mAudioUrlVerifyTask = new AsyncTaskAudioUrlVerify(mAct); 
			mAudioUrlVerifyTask.execute("Searching media ...");
		}
	}
	
	public static boolean willPlayNext;
	public static void playNextAudio()
	{		
//		Toast.makeText(mAct,"Can not open file, try next one.",Toast.LENGTH_SHORT).show();
		System.out.println("_playNextAudio");
		if(mMediaPlayer != null)
		{
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mPlaybackTime = 0;
   
		// new audio index
		mAudioIndex++;
		
		if(mAudioIndex >= AudioInfo.getAudioList().size())
			mAudioIndex = 0; //back to first index

		// check try times,had tried or not tried yet, anyway the audio file is found
		System.out.println("check mTryTimes = " + mTryTimes);
		if(mTryTimes < AudioInfo.getAudioFilesSize())
		{
			startAudio();
		}
		else // try enough times: still no audio file is found 
		{
			Toast.makeText(mAct,R.string.audio_message_no_media_file_is_found,Toast.LENGTH_SHORT).show();
			
			// do not show highlight
			DrawerActivity.mSubMenuItemAudio.setIcon(R.drawable.ic_menu_slideshow);
			TabsHostFragment.setPlayingTab_WithHighlight(false);
			NoteFragment.mItemAdapter.notifyDataSetChanged();

			// stop media player
			stopAudio();
		}		
		System.out.println("Next mAudioIndex = " + mAudioIndex);
	}

	public static void playPreviousAudio()
	{		
		System.out.println("_playPreviousAudio");
		if(mMediaPlayer != null)
		{
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mPlaybackTime = 0;
   
		// new audio index
		mAudioIndex--;
		
		if(mAudioIndex < 0)
			mAudioIndex++; //back to first index

		startAudio();
		System.out.println("Previous mAudioIndex = " + mAudioIndex);
	}
	
	
	public static void stopAudio()
	{
		System.out.println("_stopAudio");
		if(mMediaPlayer != null)
			mMediaPlayer.release();
		mMediaPlayer = null;
		mAudioHandler.removeCallbacks(mRunOneTimeMode); 
		mAudioHandler.removeCallbacks(mRunContinueMode); 
		mPlayerState = PLAYER_AT_STOP;
		
		// set highlight off
		if(Note_view_pager_buttons_controller.imageViewAudioButton != null)
			Note_view_pager_buttons_controller.imageViewAudioButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_ringer_on, 0, 0, 0);

		if(Note_view_pager.mMenuItemAudio != null)
			Note_view_pager.mMenuItemAudio.setIcon(R.drawable.ic_lock_ringer_on);
		
		// make sure progress dialog will disappear
	 	if( !mAudioUrlVerifyTask.isCancelled() )
	 	{
	 		mAudioUrlVerifyTask.cancel(true);
	 		
	 		if( (mAudioUrlVerifyTask.mUrlVerifyDialog != null) &&
	 		 	mAudioUrlVerifyTask.mUrlVerifyDialog.isShowing()	)
	 		{
	 			mAudioUrlVerifyTask.mUrlVerifyDialog.dismiss();
	 		}

	 		if( (mAudioUrlVerifyTask.mAudioPrepareTask != null) &&
	 			(mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog != null) &&
	 			mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog.isShowing()	)
	 		{
	 			mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog.dismiss();
	 		}
	 	}
	}
	
//	static boolean isMediaEndWasMet()
//	{
//		mPlaybackTime = mMediaPlayer.getCurrentPosition();
//		mAudioDuration = mMediaPlayer.getDuration();
//		System.out.println("-- mAudioIndex -- = " + mAudioIndex);
//		System.out.println("- mAudioDuration = " + mAudioDuration);
//		System.out.println("- mPlaybackTime = " + mPlaybackTime);
//		 System.out.println("= mPlaybackTime / mAudioDuration = " + (int)((mPlaybackTime * 100.0f) /mAudioDuration) + "%" );
//		 System.out.println("= mPlaybackTime - mAudioDuration = " + Math.abs(mPlaybackTime - mAudioDuration) );
//		return Math.abs(mPlaybackTime - mAudioDuration) < 1500; // toleration
//	}
   
	public static void prepareAudioInfo(Context context)
	{
		mAudioInfo = new AudioInfo(); 
		mAudioInfo.updateAudioInfo(context);
	}   

}