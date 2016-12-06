package com.cwc.litenote.note;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.TouchImageView;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.util.UilCommon;
import com.cwc.litenote.util.Util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Note_edit extends Activity 
{

    private Long mRowId, mCreatedTime;
    private String mTitle, mPictureUri, mAudioUri, mCameraPictureUri, mBody;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    Note_common note_common;
    private boolean mEnSaveDb = true;
    boolean bUseCameraImage;
    DB mDb;
    static TouchImageView mEnlargedImage;
    int mPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note_title);// set title
    	
        System.out.println("Note_edit / onCreate");
        
		mEnlargedImage = (TouchImageView)findViewById(R.id.expanded_image);

		UilCommon.init();
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        
    	Bundle extras = getIntent().getExtras();
    	mPosition = extras.getInt("list_view_position");
    	mRowId = extras.getLong(DB.KEY_NOTE_ID);
    	mPictureUri = extras.getString(DB.KEY_NOTE_PICTURE_URI);
    	mAudioUri = extras.getString(DB.KEY_NOTE_AUDIO_URI);
    	mTitle = extras.getString(DB.KEY_NOTE_TITLE);
    	mBody = extras.getString(DB.KEY_NOTE_BODY);
    	mCreatedTime = extras.getLong(DB.KEY_NOTE_CREATED);
        
    	//initialization
        note_common = new Note_common(this, mRowId, mTitle, mPictureUri, mAudioUri, "", mBody, mCreatedTime);
        note_common.UI_init();
        mCameraPictureUri = "";
        bUseCameraImage = false;
        
        // get picture Uri in DB if instance is not null
        mDb = new DB(Note_edit.this);
        if(savedInstanceState != null)
        {
	        mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
	        System.out.println("Note_edit / onCreate / mRowId =  " + mRowId);
	        if(mRowId != null)
	        {
	        	mPictureUri = mDb.getNotePictureUriById(mRowId);
	       		Note_common.mCurrentPictureUri = mPictureUri;
	        	mAudioUri = mDb.getNoteAudioUriById(mRowId);
	        	Note_common.mCurrentAudioUri = mAudioUri;
	        }
	        mDb.doClose();
        }
        
    	// show view
        Note_common.populateFields(mRowId);
		
		// OK button: edit OK, save
        Button okButton = (Button) findViewById(R.id.note_edit_ok);
        okButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
		// OK
        okButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
				if(Note_common.bRemovePictureUri)
				{
					mPictureUri = "";
				}
				if(Note_common.bRemoveAudioUri)
				{
					mAudioUri = "";
				}				
                mEnSaveDb = true;
                finish();
            }

        });
        
        // delete button: delete note
        Button delButton = (Button) findViewById(R.id.note_edit_delete);
        delButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
        // delete
        delButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
				//warning :start
        		mPref_delete_warn = getSharedPreferences("delete_warn", 0);
            	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
            	   mPref_delete_warn.getString("KEY_DELETE_NOTE_WARN","yes").equalsIgnoreCase("yes")) 
            	{
        			Util util = new Util(Note_edit.this);
    				util.vibrate();
            		
            		Builder builder1 = new Builder(Note_edit.this ); 
            		builder1.setTitle(R.string.confirm_dialog_title)
                        .setMessage(R.string.confirm_dialog_message)
                        .setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener()
                        {   @Override
                            public void onClick(DialogInterface dialog1, int which1)
                        	{/*nothing to do*/}
                        })
                        .setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener()
                        {   @Override
                            public void onClick(DialogInterface dialog1, int which1)
                        	{
                        		note_common.deleteNote(mRowId);
                        		
                        		
                                if( (DrawerActivity.mCurrentPlaying_NotesTblId == 
                           			 TabsHostFragment.mCurrentNotesTableId        ) &&
                           			(DrawerActivity.mCurrentPlaying_TabIndex == 
                           			 TabsHostFragment.mCurrentTabIndex            )      )
                                {
                                	AudioPlayer.prepareAudioInfo(Note_edit.this);
                                }                        		
                        		
                        		// Stop Play/Pause if current edit item is played and is not at Stop state
                        		if(NoteFragment.mHighlightPosition == mPosition) 
                        			UtilAudio.stopAudioIfNeeded();
                        		
                        		// update highlight position
                        		if(mPosition < NoteFragment.mHighlightPosition )
                        			AudioPlayer.mAudioIndex--;
                        		
                            	finish();
                        	}
                        })
                        .show();//warning:end
            	}
            	else{
            	    //no warning:start
	                setResult(RESULT_CANCELED);
	                note_common.deleteNote(mRowId);
	                finish();
            	}
            }
        });
        
        // cancel button: leave, do not save current modification
        Button cancelButton = (Button) findViewById(R.id.note_edit_cancel);
        cancelButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        // cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                
                // check if note content is modified
               	if(note_common.isNoteModified())
            	{
               		// show confirmation dialog
            		confirmToUpdateDlg();
            	}
            	else
            	{
            		mEnSaveDb = false;
                    finish();
            	}
            }
        });
    }
    
    // confirm to update change or not
    void confirmToUpdateDlg()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(Note_edit.this);
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.edit_note_confirm_update)
	           // Yes, to update
			   .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						if(Note_common.bRemovePictureUri)
						{
							mPictureUri = "";
						}
						if(Note_common.bRemoveAudioUri)
						{
							mAudioUri = "";
						}						
					    mEnSaveDb = true;
					    finish();
					}})
			   // cancel
			   .setNeutralButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{   // do nothing
					}})
			   // no, roll back to original status		
			   .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						Bundle extras = getIntent().getExtras();
						String originalPictureFileName = extras.getString(DB.KEY_NOTE_PICTURE_URI);

						if(originalPictureFileName.isEmpty())
						{   // no picture at first
							note_common.removePictureStringFromOriginalNote(mRowId);
		                    mEnSaveDb = false;
						}
						else
						{	// roll back existing picture
							Note_common.bRollBackData = true;
							mPictureUri = originalPictureFileName;
							mEnSaveDb = true;
						}	
						
						String originalAudioFileName = extras.getString(DB.KEY_NOTE_AUDIO_URI);

						if(originalAudioFileName.isEmpty())
						{   // no picture at first
							note_common.removeAudioStringFromOriginalNote(mRowId);
		                    mEnSaveDb = false;
						}
						else
						{	// roll back existing picture
							Note_common.bRollBackData = true;
							mAudioUri = originalAudioFileName;
							mEnSaveDb = true;
						}							
						
	                    finish();
					}})
			   .show();
    }
    

    // for finish(), for Rotate screen
    @Override
    protected void onPause() {
        super.onPause();
        
        System.out.println("Note_edit / onPause / mEnSaveDb = " + mEnSaveDb);
        System.out.println("Note_edit / onPause / mPictureUri = " + mPictureUri);
        System.out.println("Note_edit / onPause / mAudioUri = " + mAudioUri);
        mRowId = Note_common.saveStateInDB(mRowId,mEnSaveDb,mPictureUri, mAudioUri, ""); 
    }

    // for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        System.out.println("Note_edit / onSaveInstanceState / mEnSaveDb = " + mEnSaveDb);
        System.out.println("Note_edit / onSaveInstanceState / bUseCameraImage = " + bUseCameraImage);
        System.out.println("Note_edit / onSaveInstanceState / mCameraPictureUri = " + mCameraPictureUri);
        
        if(Note_common.bRemovePictureUri)
    	    outState.putBoolean("removeOriginalPictureUri",true);

        if(Note_common.bRemoveAudioUri)
    	    outState.putBoolean("removeOriginalAudioUri",true);
        
        
        if(bUseCameraImage)
        {
        	outState.putBoolean("UseCameraImage",true);
        	outState.putString("showCameraImageUri", mPictureUri);
        }
        else
        {
        	outState.putBoolean("UseCameraImage",false);
        	outState.putString("showCameraImageUri", "");
        }
        
        mRowId = Note_common.saveStateInDB(mRowId,mEnSaveDb,mPictureUri, mAudioUri, ""); 
        outState.putSerializable(DB.KEY_NOTE_ID, mRowId);
        
    }
    
    // for After Rotate
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	if(savedInstanceState.getBoolean("UseCameraImage"))
    		bUseCameraImage = true;
    	else
    		bUseCameraImage = false;
    	
    	mCameraPictureUri = savedInstanceState.getString("showCameraImageUri");
    	
    	System.out.println("Note_edit / onRestoreInstanceState / savedInstanceState.getBoolean removeOriginalPictureUri =" +
    							savedInstanceState.getBoolean("removeOriginalPictureUri"));
        if(savedInstanceState.getBoolean("removeOriginalPictureUri"))
        {
        	mCameraPictureUri = "";
        	Note_common.mOriginalPictureUri="";
        	Note_common.mCurrentPictureUri="";
        	note_common.removePictureStringFromOriginalNote(mRowId);
        	Note_common.populateFields(mRowId);
        	Note_common.bRemovePictureUri = true;
        }
        if(savedInstanceState.getBoolean("removeOriginalAudioUri"))
        {
        	Note_common.mOriginalAudioUri="";
        	Note_common.mCurrentAudioUri="";
        	note_common.removeAudioStringFromOriginalNote(mRowId);
        	Note_common.populateFields(mRowId);
        	Note_common.bRemoveAudioUri = true;
        }        
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public void onBackPressed() {
	    if(Note_common.bShowEnlargedImage == true)
	    {
	    	Note_common.closeEnlargedImage();
	    }
	    else
	    {
	    	if(note_common.isNoteModified())
	    	{
	    		confirmToUpdateDlg();
	    	}
	    	else
	    	{
	            mEnSaveDb = false;
	            finish();
	    	}
	    }
    }
    
    static final int CHANGE_AUDIO = R.id.ADD_AUDIO;
    static final int CHANGE_IMAGE = R.id.ADD_NEW_IMAGE;
    static final int CHANGE_VIDEO = R.id.ADD_NEW_VIDEO;
	private Uri pictureUri;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, CHANGE_AUDIO, 0, R.string.note_audio )
	    .setIcon(R.drawable.ic_lock_ringer_on)
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

//	    menu.add(0, ADD_NEW_PICTURE, 1, R.string.note_take_picture )
	    menu.add(0, CHANGE_IMAGE, 1, R.string.note_camera_image )
	    .setIcon(android.R.drawable.ic_menu_camera)
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

	    menu.add(0, CHANGE_VIDEO, 2, R.string.note_camera_video )
	    .setIcon(android.R.drawable.presence_video_online)
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	    
	    
		return super.onCreateOptionsMenu(menu);
	}
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) 
        {
		    case android.R.id.home:
		    	if(note_common.isNoteModified())
		    	{
		    		confirmToUpdateDlg();
		    	}
		    	else
		    	{
		            mEnSaveDb = false;
		            finish();
		    	}
		        return true;
	        
            case CHANGE_AUDIO:
            	Note_common.bRemoveAudioUri = false; // reset
            	setAudioSource();
			    return true;
			    
            case CHANGE_IMAGE:
            	Intent intentImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            	// new picture Uri with current time stamp
            	pictureUri = UtilImage.getPictureUri("IMG_" + Util.getCurrentTimeString() + ".jpg",
						   						   Note_edit.this); 
            	mPictureUri = pictureUri.toString();
			    intentImage.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
			    startActivityForResult(intentImage, Util.ACTIVITY_TAKE_PICTURE); 
			    mEnSaveDb = true;
			    Note_common.bRemovePictureUri = false; // reset
			    
			    if(UtilImage.mExpandedImageView != null)
			    	UtilImage.closeExpandedImage();
		        
			    return true;
            
            case CHANGE_VIDEO:
            	Intent intentVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            	// new picture Uri with current time stamp
            	pictureUri = UtilImage.getPictureUri("VID_" + Util.getCurrentTimeString() + ".mp4",
						   						   Note_edit.this); 
            	mPictureUri = pictureUri.toString();
			    intentVideo.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
			    startActivityForResult(intentVideo, Util.ACTIVITY_TAKE_PICTURE); 
			    mEnSaveDb = true;
			    Note_common.bRemovePictureUri = false; // reset
			    
			    return true;			    
			    
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    
    void setAudioSource() 
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.edit_note_set_audio_dlg_title);
		// Cancel
		builder.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener()
		   	   {
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{// cancel
				}});
		// Set
		builder.setNeutralButton(R.string.btn_Select, new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) 
		{
		    mEnSaveDb = true;
	        startActivityForResult(UtilAudio.chooseAudioIntent(Note_edit.this),
	        					   Util.CHOOSER_SET_AUDIO);
		}});
		// None
		if(!mAudioUri.isEmpty())
		{
			builder.setPositiveButton(R.string.btn_None, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						Note_common.bRemoveAudioUri = true;
						Note_common.mOriginalAudioUri = "";
						mAudioUri = "";
						Note_common.removeAudioStringFromCurrentEditNote(mRowId);
						Note_common.populateFields(mRowId);
					}});		
		}
		
		Dialog dialog = builder.create();
		dialog.show();
    }
    
//    static String mSelectedAudioUri;
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) 
	{
		// take picture
		if (requestCode == Util.ACTIVITY_TAKE_PICTURE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				pictureUri = Uri.parse(Note_common.mCurrentPictureUri);
//				String str = getResources().getText(R.string.note_take_picture_OK ).toString();
//	            Toast.makeText(Note_edit.this, str + " " + imageUri.toString(), Toast.LENGTH_SHORT).show();
	            Note_common.populateFields(mRowId);
	            bUseCameraImage = true;
	            mCameraPictureUri = Note_common.mCurrentPictureUri;
			} 
			else if (resultCode == RESULT_CANCELED)
			{
				bUseCameraImage = false;
				// to use captured picture or original picture
				if(!mCameraPictureUri.isEmpty())
				{
					// update
					Note_common.saveStateInDB(mRowId,mEnSaveDb,mCameraPictureUri, mAudioUri, "");// replace with existing picture
					Note_common.populateFields(mRowId);
		            
					// set for Rotate any times
		            bUseCameraImage = true;
		            mPictureUri = Note_common.mCurrentPictureUri; // for pause
		            mCameraPictureUri = Note_common.mCurrentPictureUri; // for save instance

				}
				else
				{
					// skip new Uri, roll back to original one
			    	Note_common.mCurrentPictureUri = Note_common.mOriginalPictureUri;
			    	mPictureUri = Note_common.mOriginalPictureUri;
					Toast.makeText(Note_edit.this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
				}
				
				mEnSaveDb = true;
				Note_common.saveStateInDB(mRowId,mEnSaveDb,mPictureUri, mAudioUri, "");
				Note_common.populateFields(mRowId);
			}
		}
		
		// choose picture
        if(requestCode == Util.CHOOSER_SET_PICTURE && resultCode == Activity.RESULT_OK)
        {
			Uri selectedUri = returnedIntent.getData(); 
			System.out.println("selected Uri = " + selectedUri.toString());
			String authority = selectedUri.getAuthority();
			// SAF support, take persistent Uri permission
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
		    	final int takeFlags = returnedIntent.getFlags()
		                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
		                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		    	// Check for the freshest data.
		    	if(authority.equalsIgnoreCase("com.google.android.apps.docs.storage")) //add other condition? 	
		    	{
		    		getContentResolver().takePersistableUriPermission(selectedUri, takeFlags);
		    	}
			}			
			
			
			String pictureUri = selectedUri.toString();
        	System.out.println("check onActivityResult / uriStr = " + pictureUri);
        	
        	mRowId = Note_common.saveStateInDB(mRowId,true,pictureUri, mAudioUri, ""); 
        	
            Note_common.populateFields(mRowId);
			
            // set for Rotate any times
            bUseCameraImage = true;
            mPictureUri = Note_common.mCurrentPictureUri; // for pause
            mCameraPictureUri = Note_common.mCurrentPictureUri; // for save instance

        }  
        
        // choose audio
		if(requestCode == Util.CHOOSER_SET_AUDIO)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				// for audio
				Uri auidoUri = returnedIntent.getData(); 
				
				String audioUriStr = auidoUri.toString();
//				System.out.println(" Note_edit / onActivityResult / Util.CHOOSER_SET_AUDIO / mPictureUri = " + mPictureUri);
	        	Note_common.saveStateInDB(mRowId,true,mPictureUri, audioUriStr, "");
	        	
	        	Note_common.populateFields(mRowId);
	        	mAudioUri = audioUriStr;
	    			
	        	showSavedFileToast(audioUriStr);
			} 
			else if (resultCode == RESULT_CANCELED)
			{
				Toast.makeText(Note_edit.this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
	            setResult(RESULT_CANCELED, getIntent());
	            finish();
	            return; // must add this
			}
		}
	}
	
	// show audio file name
	void showSavedFileToast(String audioUri)
	{
        String audioName = Util.getDisplayNameByUriString(audioUri, Note_edit.this);
		Toast.makeText(Note_edit.this,
						audioName,
						Toast.LENGTH_SHORT)
						.show();
	}
	
}
