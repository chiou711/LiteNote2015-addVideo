package com.cwc.litenote.media.audio;

import java.io.File;
import java.util.List;
import java.util.Locale;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UtilAudio {
	
	static AudioPlayer mAudioPlayer;
	// Play audio
	public static void startAudioPlayer(FragmentActivity fragAct)
	{
 	   	System.out.println("Util / startAudioPlayer ");
 	   	// if media player is null, set new fragment
		if(AudioPlayer.mMediaPlayer == null)
		{
			// prepare audio info when media player is null
			AudioPlayer.prepareAudioInfo(fragAct);
			
		 	// show toast if Audio file is not found or No selection of audio file
			if( (AudioInfo.getAudioFilesSize() == 0) &&
				(AudioPlayer.mPlayMode == AudioPlayer.CONTINUE_MODE)        )
			{
				Toast.makeText(fragAct,R.string.audio_file_not_found,Toast.LENGTH_SHORT).show();
			}
			else
			{
				// fix: progress dialog can not disappear soon when double click of list view item 
				if(mAudioPlayer != null) 
				{
					AudioPlayer.stopAudio();
				}
				AudioPlayer.mPlaybackTime = 0;
				AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_PLAY;
				mAudioPlayer = new AudioPlayer(fragAct);
			}
		}
		else
		{
			// from play to pause
			if(AudioPlayer.mMediaPlayer.isPlaying())
			{
				System.out.println("play -> pause");
				AudioPlayer.mMediaPlayer.pause();
				AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunOneTimeMode); 
				AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunContinueMode); 
				AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_PAUSE;
			}
			else // from pause to play
			{
				System.out.println("pause -> play");
				AudioPlayer.mMediaPlayer.start();
				AudioPlayer.mAudioHandler.post(AudioPlayer.mRunOneTimeMode);  
				AudioPlayer.mAudioHandler.post(AudioPlayer.mRunContinueMode);  
				AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_PLAY;
			}
		}
	} 
	
	// Stop audio media player and audio handler
    public static void stopAudioPlayer()
    {
    	System.out.println("_stopAudioPlayer");
        if(AudioPlayer.mMediaPlayer != null)
    	{
			if(AudioPlayer.mMediaPlayer.isPlaying())
				AudioPlayer.mMediaPlayer.pause();
    		AudioPlayer.mMediaPlayer.release();
    		AudioPlayer.mMediaPlayer = null;
    		AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunOneTimeMode); 
    		AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunContinueMode); 
    		AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_STOP;
    	}
    }
    
    public static void stopAudioIfNeeded()
    {
		if( (AudioPlayer.mMediaPlayer != null)    &&
			(TabsHostFragment.mCurrentTabIndex == DrawerActivity.mCurrentPlaying_TabIndex)&&
			(DrawerActivity.mCurrentPlaying_DrawerIndex == DrawerActivity.mFocusDrawerPos)&&
			(AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP)      )
		{
			UtilAudio.stopAudioPlayer();
			AudioPlayer.mAudioIndex = 0;
			AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_STOP;
			TabsHostFragment.setPlayingTab_WithHighlight(false);
			DrawerActivity.mSubMenuItemAudio.setIcon(R.drawable.ic_menu_slideshow);
			NoteFragment.mItemAdapter.notifyDataSetChanged(); // disable focus
		}     	
    }
	
    public static Intent chooseAudioIntent(Activity act)
    {
	    // set multiple actions in Intent 
	    // Refer to: http://stackoverflow.com/questions/11021021/how-to-make-an-intent-with-multiple-actions
        Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getContentIntent.setType("audio/*");
        PackageManager pm = act.getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(getContentIntent, 0);
        System.out.println("resInfo size = " + resInfo.size());
        Intent openInChooser = Intent.createChooser(getContentIntent, act.getResources().getText(R.string.add_new_chooser_audio));
        
        // SAF support starts from Kitkat
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
			// BEGIN_INCLUDE (use_open_document_intent)
	        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
	        Intent openDocumentIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
	        
	        // Filter to only show results that can be "opened", such as a file (as opposed to a list
	        // of contacts or time zones)
	        openDocumentIntent.addCategory(Intent.CATEGORY_OPENABLE);	        
	        openDocumentIntent.setType("audio/*");

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
	            intent.setType("audio/*");
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
	            intent.setType("audio/*");
	            extraIntents[resInfoSaf.size()+i] = new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon);
	        }
	        
        	openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        }

        return openInChooser;
    }
    
    // update footer audio state
    public static void updateFooterAudioState(ImageView footerAudioImage, TextView textView)
    {
		if(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PLAY)
		{
//			footerTextView.setEnabled(true);
			textView.setTextColor(Color.rgb(255,128,0));
			textView.setSelected(true);
//			footerAudioImage.setImageResource(R.drawable.ic_audio_selected); //highlight
			footerAudioImage.setImageResource(R.drawable.ic_media_pause);
		}
		else if( (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PAUSE) ||
				 (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_STOP)    )
		{
//			footerTextView.setEnabled(false); // gray out effect if no setting text color
			textView.setSelected(false);
			textView.setTextColor(Color.rgb(64,64,64));
//			footerAudioImage.setImageResource(R.drawable.ic_lock_ringer_on);
			footerAudioImage.setImageResource(R.drawable.ic_media_play);
		}
    }
    
    // check if file has audio extension
    // refer to http://developer.android.com/intl/zh-tw/guide/appendix/media-formats.html
    public static boolean hasAudioExtention(File file)
    {
    	boolean isAudio = false;
    	String fn = file.getName().toLowerCase(Locale.getDefault());
    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||	fn.endsWith("m4a") || fn.endsWith("aac") ||
       		fn.endsWith("ts") || fn.endsWith("flac") ||	fn.endsWith("mp3") || fn.endsWith("mid") ||  
       		fn.endsWith("xmf") || fn.endsWith("mxmf")|| fn.endsWith("rtttl") || fn.endsWith("rtx") ||  
       		fn.endsWith("ota") || fn.endsWith("imy")|| fn.endsWith("ogg") || fn.endsWith("mkv") ||
       		fn.endsWith("wav") || fn.endsWith("wma")
    		) 
	    	isAudio = true;
	    
    	return isAudio;
    }    
}
