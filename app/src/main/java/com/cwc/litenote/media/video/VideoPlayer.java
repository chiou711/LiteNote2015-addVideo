package com.cwc.litenote.media.video;

import com.cwc.litenote.db.DB;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.note.Note_view_pager_buttons_controller;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public class VideoPlayer 
{
	private static final String TAG_VIDEO = "VIDEO_PLAYER"; // error logging tag
	static final int DURATION_1S = 1000; // 1000 = 1 second

	static Activity mFragAct;
	public static Handler mVideoHandler;
   
	public VideoPlayer(FragmentActivity fAct)  
	{
		System.out.println("VideoPlayer constructor");
		mFragAct = fAct;
		
		Note_view_pager.mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		Note_view_pager.mRowId = Note_view_pager.mDb.getNoteId(Note_view_pager.mCurrentPosition);
		Note_view_pager.mVideoUriInDB = Note_view_pager.mDb.getNotePictureUriById(Note_view_pager.mRowId);
		Note_view_pager.mDb.doClose(); 

		if(UtilVideo.mVideoState == UtilVideo.VIDEO_AT_STOP) 
			startVideo();
	};

	
	static void startVideo()
	{
		System.out.println("VideoPlayer / _startVideo");
		// remove call backs to make sure next toast will appear soon
		if(mVideoHandler != null)
		{
			mVideoHandler.removeCallbacks(mRunPlayVideo); 
		}
		
		// start a new handler
		mVideoHandler = new Handler();
		
		if(UtilVideo.mVideoView != null)
				setVideoPlayerListeners();
		
		mVideoHandler.post(mRunPlayVideo);
	}
	
	public static void stopVideo()
	{
		System.out.println("_stopVideo");
		
		if(mVideoHandler != null)
		{
			mVideoHandler.removeCallbacks(mRunPlayVideo); 
		}
		
		if((UtilVideo.mVideoView != null) && UtilVideo.mVideoView.isPlaying())
		{
			UtilVideo.mVideoView.stopPlayback();
			UtilVideo.mVideoView = null;
			UtilVideo.mVideoPlayer = null;
		}

//		UtilVideo.mPlayVideoPosition = 0;
		UtilVideo.mVideoState = UtilVideo.VIDEO_AT_STOP;
	}	
	
	public void goOnVideo()
	{
		mVideoHandler.post(mRunPlayVideo);
	}
	
	//
	// Runnable for play video
	//
	public static Runnable mRunPlayVideo = new Runnable()
	{   @Override
		public void run()
		{
			String path = Note_view_pager.mVideoUriInDB;
			
//			System.out.println("VideoPlayer / mRunPlayVideo");
			if(UtilVideo.mVideoView != null)
			{	
				try 
				{
					if (path == null || path.length() == 0) 
					{
						Toast.makeText(mFragAct, "Video file URL/path is empty",Toast.LENGTH_LONG).show();
					} 
					else 
					{
						if(Note_view_pager.isViewModeChanged)
							UtilVideo.mPlayVideoPosition = Note_view_pager.mPos;
						else	
							UtilVideo.mPlayVideoPosition = UtilVideo.mVideoView.getCurrentPosition();
						
						System.out.println("mRunPlayVideo / mPlayVideoPosition = " + UtilVideo.mPlayVideoPosition);
						
						// start video playing
						if(UtilVideo.mPlayVideoPosition == 0)
						{
							System.out.println("--- normal start");
							UtilVideo.currentPicturePath = path;
							UtilVideo.mVideoView.setVideoPath(UtilVideo.getVideoDataSource(path));
							UtilVideo.mVideoView.seekTo(UtilVideo.mPlayVideoPosition);
							UtilVideo.mVideoView.start();
							UtilVideo.mVideoView.requestFocus();
							UtilVideo.mVideoState = UtilVideo.VIDEO_AT_PLAY;
							UtilVideo.updateVideoPlayButtonState();
						}
						else if ( path.equals(UtilVideo.currentPicturePath) &&
								 (UtilVideo.mPlayVideoPosition > 0)) // If the path has not changed, just keep playing video until end
						{
							if(!UtilVideo.mVideoView.isPlaying())
							{
								// change state from Stop to Pause if position > 0
								if((UtilVideo.mPlayVideoPosition > 0) && (UtilVideo.mVideoState == UtilVideo.VIDEO_AT_STOP))
								{
									UtilVideo.mVideoState = UtilVideo.VIDEO_AT_PAUSE;
								}
								
								if(UtilVideo.mVideoState == UtilVideo.VIDEO_AT_PAUSE)
								{
									if(!UtilVideo.playWillPause ) 
									{
										System.out.println("--- continue Play");
										
										// when view change
										if(Note_view_pager.isViewModeChanged)
										{
											UtilVideo.currentPicturePath = path;
											UtilVideo.mVideoView.setVideoPath(UtilVideo.getVideoDataSource(path));
											UtilVideo.mVideoView.seekTo(UtilVideo.mPlayVideoPosition);
											Note_view_pager.isViewModeChanged = false;
										}
										
										UtilVideo.mVideoView.start();
										UtilVideo.mVideoView.requestFocus();
										UtilVideo.mVideoState = UtilVideo.VIDEO_AT_PLAY;
										UtilVideo.updateVideoPlayButtonState();
									}
								}
								else if(Math.abs(UtilVideo.mPlayVideoPosition - Note_view_pager_buttons_controller.videoFileLengthInMilliseconds) <= 1000)
								{
									System.out.println("--- stop Play");
									UtilVideo.mPlayVideoPosition = 0;
									UtilVideo.mVideoView.seekTo(0);
									stopVideo();
								}
							}
							else if(UtilVideo.mVideoView.isPlaying())
							{
								if(UtilVideo.playWillPause ) 
								{
									System.out.println("--- pause Play");
									UtilVideo.mVideoView.pause();
									UtilVideo.mVideoState = UtilVideo.VIDEO_AT_PAUSE;
									UtilVideo.updateVideoPlayButtonState();
								}
								else
								{
	//								System.out.println("--- keep Play");
									// do nothing, just keep playing
									if(Note_view_pager_buttons_controller.showControl == true)
										Note_view_pager_buttons_controller.primaryVideoSeekBarProgressUpdater();
								}
							}
						}
						
						// update video seek bar progress
						Note_view_pager_buttons_controller.videoProgress = (int)(((float)UtilVideo.mPlayVideoPosition/Note_view_pager_buttons_controller.videoFileLengthInMilliseconds)*100);				
						Note_view_pager_buttons_controller.videoSeekBar.setProgress(Note_view_pager_buttons_controller.videoProgress);						
	
						// delay and execute runnable
						if(UtilVideo.mVideoState == UtilVideo.VIDEO_AT_PLAY)
							mVideoHandler.postDelayed(mRunPlayVideo,DURATION_1S);
					}
				} 
				catch (Exception e) 
				{
					Log.e(TAG_VIDEO, "error: " + e.getMessage(), e);
					VideoPlayer.stopVideo();
				}
			
			}
		} 
	};

	
	static String mVideoStr;
	static boolean mIsPrepared;
	static void setVideoPlayerListeners()	
	{
		// on complete listener
		UtilVideo.mVideoView.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				System.out.println("UtilVideo.mVideoView.setOnCompletionListener / _onCompletion");
				UtilVideo.mPlayVideoPosition = 0;
				UtilVideo.mVideoState = UtilVideo.VIDEO_AT_STOP;
				UtilVideo.playWillPause = true;
				UtilVideo.updateVideoPlayButtonState();								
			}
		});					
		
		// on prepared listener
		UtilVideo.mVideoView.setOnPreparedListener(new OnPreparedListener() 
		{
			@Override
			public void onPrepared(MediaPlayer mp) 
			{
				System.out.println("----UtilVideo.mVideoView.setOnPreparedListener");
				Note_view_pager_buttons_controller.primaryVideoSeekBarProgressUpdater();
				
				
				mp.setOnSeekCompleteListener(new OnSeekCompleteListener() 
				{
					@Override
					public void onSeekComplete(MediaPlayer mp) 
					{}
				});

			}
		});
		
		
		// - on error listener
		UtilVideo.mVideoView.setOnErrorListener(new OnErrorListener()
		{	@Override
			public boolean onError(MediaPlayer mp,int what,int extra) 
			{
				// more than one error when playing an index 
				System.out.println("mVideoView.setOnErrorListener: what = " + what + " , extra = " + extra);
				return false;
			}
		});
	}

}