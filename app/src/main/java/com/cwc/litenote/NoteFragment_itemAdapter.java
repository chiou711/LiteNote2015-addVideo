package com.cwc.litenote;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cwc.litenote.db.DB;
import com.cwc.litenote.lib.SimpleDragSortCursorAdapter;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.image.UtilImage_bitmapLoader;
import com.cwc.litenote.media.video.AsyncTaskVideoBitmap;
import com.cwc.litenote.util.Util;

public class NoteFragment_itemAdapter extends SimpleDragSortCursorAdapter 
{
	public NoteFragment_itemAdapter(Context context, int layout, Cursor c,
						String[] from, int[] to, int flags) 
	{
		super(context, layout, c, from, to, flags);
	}

	public class ViewHolder {
		public ImageView imageCheck;
		public TextView rowId;
		public View audioBlock;
		public ImageView imageAudio;
		public TextView audioName;
		public View textTitleBlock;
		public TextView textTitle;
		public View textBodyBlock;
		public TextView textBody;
		public TextView textTime;
		public ImageView imageDragger;
		public View pictureBlock;
		public ImageView imagePicture;
		public Bitmap bmThumbnail;
		public AsyncTaskVideoBitmap mVideoAsyncTask;
		public ProgressBar progressBar;
	}
	
	@Override
	public int getCount() {
		NoteFragment.mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		int count = NoteFragment.mDb.getNotesCount();
		NoteFragment.mDb.doClose();
		return count;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		System.out.println("- _getView");
		System.out.println("NoteFragment / getView / position = " +  position);
		View view = convertView;
		final ViewHolder holder;
		
		if (convertView == null) 
		{
			view = NoteFragment.mAct.getLayoutInflater().inflate(R.layout.activity_main_list_row, parent, false);
			//04-23 23:00:02.226: E/AndroidRuntime(27214): java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.LayoutInflater android.support.v4.app.FragmentActivity.getLayoutInflater()' on a null object reference

			// set rectangular background
//				view.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
			
			//set round corner and background color
    		switch(NoteFragment.mStyle)
    		{
    			case 0:
    				view.setBackgroundResource(R.drawable.bg_0);
    				break;
    			case 1:
    				view.setBackgroundResource(R.drawable.bg_1);
    				break;
    			case 2:
    				view.setBackgroundResource(R.drawable.bg_2);
    				break;
    			case 3:
    				view.setBackgroundResource(R.drawable.bg_3);
    				break;
    			case 4:
    				view.setBackgroundResource(R.drawable.bg_4);
    				break;
    			case 5:
    				view.setBackgroundResource(R.drawable.bg_5);
    				break;
    			case 6:
    				view.setBackgroundResource(R.drawable.bg_6);
    				break;
    			case 7:
    				view.setBackgroundResource(R.drawable.bg_7);
    				break;
    			case 8:
    				view.setBackgroundResource(R.drawable.bg_8);
    				break;
    			case 9:
    				view.setBackgroundResource(R.drawable.bg_9);
    				break;
    			default:
    				break;
    		}
    		
			holder = new ViewHolder();
			holder.audioName = (TextView) view.findViewById(R.id.row_audio_name);
			holder.textTitleBlock = view.findViewById(R.id.row_title_block);
			holder.textTitle = (TextView) view.findViewById(R.id.row_title);
			holder.textBodyBlock = view.findViewById(R.id.row_body);
			holder.textBody = (TextView) view.findViewById(R.id.row_body_text_view);
			holder.textTime = (TextView) view.findViewById(R.id.row_time);
			holder.imageCheck= (ImageView) view.findViewById(R.id.img_check);
			holder.rowId= (TextView) view.findViewById(R.id.row_id);
			holder.audioBlock = view.findViewById(R.id.audio_block);
			holder.imageAudio = (ImageView) view.findViewById(R.id.img_audio);
			holder.pictureBlock = view.findViewById(R.id.image_view_block);
			holder.imagePicture = (ImageView) view.findViewById(R.id.image_view_thumb);
			holder.imageDragger = (ImageView) view.findViewById(R.id.img_dragger);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.img_progress);
			view.setTag(holder);
		} 
		else 
		{
			holder = (ViewHolder) view.getTag();
		}
		
		NoteFragment.mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		
		// show row Id
		holder.rowId.setText(String.valueOf(position+1));
		holder.rowId.setTextColor(Util.mText_ColorArray[NoteFragment.mStyle]);
		
		// show check box, title , picture
		if(Util.isEmptyString(NoteFragment.mDb.getNoteTitle(position)))
			holder.textTitleBlock.setVisibility(View.GONE);
		else
		{
			holder.textTitleBlock.setVisibility(View.VISIBLE);
			holder.textTitle.setText(NoteFragment.mDb.getNoteTitle(position));
		}
		
		holder.textTitle.setTextColor(Util.mText_ColorArray[NoteFragment.mStyle]);

		// set audio name
		String audio_name = null;
		if(!Util.isEmptyString(NoteFragment.mDb.getNoteAudioUri(position)))
		{
			String uriString = NoteFragment.mDb.getNoteAudioUri(position);
			audio_name = Util.getDisplayNameByUriString(uriString, NoteFragment.mAct);
		}
		
//			System.out.println("    audio_name = " + audio_name);
		holder.audioName.setText(audio_name);
//			holder.audioName.setTextSize(12.0f);
		
		// show audio highlight
		if( (DrawerActivity.mCurrentPlaying_DrawerIndex == DrawerActivity.mFocusDrawerPos) && 
			(DrawerActivity.mCurrentPlaying_NotesTblId == 
			 	Integer.valueOf(Util.getPref_lastTimeView_NotesTableId(NoteFragment.mAct)) ) &&
			(position == AudioPlayer.mAudioIndex)  &&
			(AudioPlayer.mMediaPlayer != null) &&
			(AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP) &&
			(AudioPlayer.mPlayMode == AudioPlayer.CONTINUE_MODE))
		{
			NoteFragment.mHighlightPosition = position;
//				holder.audioBlock.setBackgroundColor(Color.argb(0x80,0xff,0x80,0x00));
//				holder.audioName.setTextColor(Util.mText_ColorArray[mStyle]);
			holder.audioName.setTextColor(Color.argb(0xff,0xff,0x80,0x00));
			holder.audioBlock.setBackgroundResource(R.drawable.bg_highlight_border);
			holder.audioBlock.setVisibility(View.VISIBLE);
			holder.imageAudio.setVisibility(View.VISIBLE);
			holder.imageAudio.setImageResource(R.drawable.ic_audio_selected);
		}
		else
		{
			if(!Util.isEmptyString(NoteFragment.mDb.getNoteAudioUri(position)))
			{
//					holder.audioBlock.setBackgroundColor(Color.argb(0x80,0x80,0x80,0x80));
				holder.audioName.setTextColor(Util.mText_ColorArray[NoteFragment.mStyle]);
			}
			holder.audioBlock.setBackgroundResource(R.drawable.bg_gray_border);
			holder.audioBlock.setVisibility(View.VISIBLE);
			holder.imageAudio.setVisibility(View.VISIBLE);
			holder.imageAudio.setImageResource(R.drawable.ic_lock_ringer_on);
		}
		
		// audio icon and block
		if(Util.isEmptyString(NoteFragment.mDb.getNoteAudioUri(position)))
		{
			holder.imageAudio.setVisibility(View.INVISIBLE);
			holder.audioBlock.setVisibility(View.INVISIBLE);
		}
		
		
//		// Show image thumb nail
		final String pictureUri = NoteFragment.mDb.getNotePictureUri(position);
//		System.out.println("NoteFragment_itemAdapter / getView / pictureUri = " + pictureUri);
//		String scheme = Uri.parse(pictureUri).getScheme();
		
		
		if(Util.isEmptyString(pictureUri))
			holder.pictureBlock.setVisibility(View.GONE);
		else
		{
			holder.pictureBlock.setVisibility(View.VISIBLE);
			holder.mVideoAsyncTask = null;
			
			// load bitmap to image view
			new UtilImage_bitmapLoader(holder.imagePicture, pictureUri, holder.progressBar, NoteFragment.mAct);
		}
		
		// Show note body or not
		NoteFragment.mPref_show_note_attribute = NoteFragment.mAct.getSharedPreferences("show_note_attribute", 0);
	  	if(NoteFragment.mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
	  	{
//		  		holder.textBody.setText(mDb.getNoteBody(position));
	  		
	  		// test only: enabled for showing picture path
			if(!Util.isEmptyString(NoteFragment.mDb.getNoteBody(position)))
			{
				holder.textBody.setText(NoteFragment.mDb.getNoteBody(position));
			}
			else
			{
//				holder.textBody.setTextSize(12);
				holder.textBody.setText(NoteFragment.mDb.getNotePictureUri(position));
				System.out.println("- textBody / mDb.getNotePictureUri(position) = " + NoteFragment.mDb.getNotePictureUri(position));
			}
			
	  		// test only: enabled for showing audio path
//				if(!Util.isEmptyString(mDb.getNoteBody(position)))
//				{
//					holder.textBody.setText(mDb.getNoteBody(position));
//				}
//				else
//				{
//					holder.textBody.setTextSize(12);
//					holder.textBody.setText(mDb.getNoteAudioUri(position));
//				}
	  		
	  		// time stamp
			holder.textBody.setTextColor(Util.mText_ColorArray[NoteFragment.mStyle]);
			holder.textTime.setText(Util.getTimeString(NoteFragment.mDb.getNoteCreatedTime(position)));
			holder.textTime.setTextColor(Util.mText_ColorArray[NoteFragment.mStyle]);
	  	}
	  	else
	  	{
	  		holder.textBodyBlock.setVisibility(View.GONE);
	  	}			
		
		
	  	// dragger
	  	NoteFragment.mPref_show_note_attribute = NoteFragment.mAct.getSharedPreferences("show_note_attribute", 0);
	  	if(NoteFragment.mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
	  		holder.imageDragger.setVisibility(View.VISIBLE); 
	  	else
	  		holder.imageDragger.setVisibility(View.GONE); 
		
	  	// marking
		if( NoteFragment.mDb.getNoteMarking(position) == 1)
			holder.imageCheck.setBackgroundResource(NoteFragment.mStyle%2 == 1 ?
	    			R.drawable.btn_check_on_holo_light:
	    			R.drawable.btn_check_on_holo_dark);	
		else
			holder.imageCheck.setBackgroundResource(NoteFragment.mStyle%2 == 1 ?
					R.drawable.btn_check_off_holo_light:
					R.drawable.btn_check_off_holo_dark);
		
		NoteFragment.mDb.doClose();
		
		return view;
	}
}