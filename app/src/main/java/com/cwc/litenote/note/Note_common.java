/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cwc.litenote.note;

import java.util.Date;

import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.media.image.UtilImage_bitmapLoader;
import com.cwc.litenote.util.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Note_common {

	static TextView mAudioTextView;
    
    static ImageView mPicImageView;
    static String mPictureUriInDB;
    static String mAudioUriInDB;
    static String mOriginalPictureUri;
    static String mCurrentPictureUri;
    static String mCurrentAudioUri;

    static String mOriginalAudioUri;
    static String mOriginalDrawingUri;

    static EditText mTitleEditText;
    static EditText mBodyEditText;
    static String mOriginalTitle;
    static String mOriginalBody;
    
    Long mRowId;
	static Long mOriginalCreatedTime;
	static Long mOriginalMarking;
    
    static boolean bRollBackData;
    static boolean bRemovePictureUri = false;
    static boolean bRemoveAudioUri = false;
    boolean bEditPicture = false;

    private static DB mDb;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    static Activity mAct;
    static int mStyle;
    static ProgressBar progressBar;
    static ProgressBar progressBarExpand;
    
    public Note_common(Activity act,Long rowId,String strTitle, String pictureUri, String audioUri, String drawingUri, String strBody, Long createdTime)
    {
    	mAct = act;
    	mRowId = rowId;
    			
    	mOriginalTitle = strTitle;
	    mOriginalBody = strBody;
	    mOriginalPictureUri = pictureUri;
	    mOriginalAudioUri = audioUri;
	    mOriginalDrawingUri = drawingUri;
	    
	    mOriginalCreatedTime = createdTime;
	    mCurrentPictureUri = pictureUri;
	    mCurrentAudioUri = audioUri;
	    
    	DB.setFocus_NotesTableId(DB.getNotes_TableId()); 
        mDb = new DB(mAct);
    	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
	    mOriginalMarking = mDb.getNoteMarkingById(rowId);
		mDb.doClose();

		bRollBackData = false;
		bEditPicture = true;
		bShowEnlargedImage = false;
    }
    
    public Note_common(Activity act)
    {
    	mAct = act;
    	DB.setFocus_NotesTableId(DB.getNotes_TableId()); 
        mDb = new DB(mAct);
    }
    
    void UI_init()
    {
    	mAudioTextView = (TextView) mAct.findViewById(R.id.edit_audio);
        mTitleEditText = (EditText) mAct.findViewById(R.id.edit_title);
        mPicImageView = (ImageView) mAct.findViewById(R.id.edit_picture);
        mBodyEditText = (EditText) mAct.findViewById(R.id.edit_body);
        progressBar = (ProgressBar) mAct.findViewById(R.id.edit_progress_bar);
        progressBarExpand = (ProgressBar) mAct.findViewById(R.id.edit_progress_bar_expand);
        		
        mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		mStyle = mDb.getTabStyle(TabsHostFragment.mCurrentTabIndex);
		mDb.doClose();

		//set audio color
//		mAudioTextView.setTextColor(Util.mText_ColorArray[style]);
//		mAudioTextView.setBackgroundColor(Util.mBG_ColorArray[style]);
		
		//set title color
		mTitleEditText.setTextColor(Util.mText_ColorArray[mStyle]);
		mTitleEditText.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
		
		mPicImageView.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
		
		//set body color 
		mBodyEditText.setTextColor(Util.mText_ColorArray[mStyle]);
		mBodyEditText.setBackgroundColor(Util.mBG_ColorArray[mStyle]);	
		
		// set thumb nail listener
        mPicImageView.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View view) {
            	if(bShowEnlargedImage == true)
            		closeEnlargedImage();
            	else
                {
                	System.out.println("Note_common / mPictureUriInDB = " + mPictureUriInDB);
                	if(!Util.isEmptyString(mPictureUriInDB))
                	{
                		bRemovePictureUri = false;
                		System.out.println("mPicImageView.setOnClickListener / mPictureUriInDB = " + mPictureUriInDB);
                		
                		// check if pictureUri has scheme
                		if(Util.isUriExisted(mPictureUriInDB, mAct))
                		{
	                		if(Uri.parse(mPictureUriInDB).isAbsolute())
	                		{
	                			new UtilImage_bitmapLoader(Note_edit.mEnlargedImage, mPictureUriInDB, progressBarExpand, mAct);
	                			bShowEnlargedImage = true;
	                		}
	                		else
	                		{
	                			System.out.println("mPictureUriInDB is not Uri format");
	                		}
                		}
                		else
                			Toast.makeText(mAct,R.string.file_not_found,Toast.LENGTH_SHORT).show();
                	}
                	else
            			Toast.makeText(mAct,R.string.file_is_not_created,Toast.LENGTH_SHORT).show();

				} 
            }
        });
        
		// set thumb nail long click listener
        mPicImageView.setOnLongClickListener(new View.OnLongClickListener() 
        {
            @Override
            public boolean onLongClick(View view) {
            	if(bEditPicture)
            		setPictureImage();
                return false;
            }
        });
    }
    
    static boolean bShowEnlargedImage;
    public static void closeEnlargedImage()
    {
    	System.out.println("closeExpandImage");
		Note_edit.mEnlargedImage.setVisibility(View.GONE);
		bShowEnlargedImage = false;
    }
    
    void setPictureImage() 
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
		builder.setTitle(R.string.edit_note_set_picture_dlg_title)
			   .setNeutralButton(R.string.btn_Select, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						bRemovePictureUri = false; // reset
						// For selecting local gallery
//						Intent intent = new Intent(mAct, PictureGridAct.class);
//						intent.putExtra("gallery", false);
//						mAct.startActivityForResult(intent, Util.ACTIVITY_SELECT_PICTURE);
						
						// select global
						mAct.startActivityForResult(UtilImage.chooseImageIntent(mAct),
								   					Util.CHOOSER_SET_PICTURE);						
						
						
					}})					
			   .setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{// cancel
					}});

		if(!mPictureUriInDB.isEmpty())
		{
				builder.setPositiveButton(R.string.btn_None, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					//just delete picture file name
					mCurrentPictureUri = "";
					mOriginalPictureUri = "";
			    	removePictureStringFromCurrentEditNote(mRowId);
			    	populateFields(mRowId);
			    	bRemovePictureUri = true;
				}});
		}
		
		Dialog dialog = builder.create();
		dialog.show();
    }
    
    void deleteNote(Long rowId)
    {
    	System.out.println("Note_common / deleteNote");
    	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
        // for Add new note (mRowId is null first), but decide to cancel 
        if(rowId != null)
        	mDb.deleteNote(rowId);
        mDb.close();
    }
    

    
    static void populateFields(Long rowId) 
    {
    	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
    	if (rowId != null) 
    	{
    		// for picture block
    		mPictureUriInDB = mDb.getNotePictureUriById(rowId);
			System.out.println("populateFields / mPictureFileNameInDB = " + mPictureUriInDB);
    		
			// load bitmap to image view
			if(!Util.isEmptyString(mPictureUriInDB))
			{
				new UtilImage_bitmapLoader(mPicImageView, mPictureUriInDB, progressBar, mAct);
			}
			else
			{
	    		mPicImageView.setImageResource(mStyle%2 == 1 ?
		    			R.drawable.btn_radio_off_holo_light:
		    			R.drawable.btn_radio_off_holo_dark);
			}
	    	
    		// audio
			mAudioUriInDB = mDb.getNoteAudioUriById(rowId);
        	if(!Util.isEmptyString(mAudioUriInDB))
    		{
    			String audio_name = Util.getDisplayNameByUriString(mAudioUriInDB,mAct);
				System.out.println("populateFields / set audio name / audio_name = " + audio_name);
				mAudioTextView.setText(mAct.getResources().getText(R.string.note_audio) + ": " + audio_name);
    		}
        	else
				mAudioTextView.setText("");
        		
    		
			String strTitleEdit = mDb.getNoteTitleById(rowId);
            mTitleEditText.setText(strTitleEdit);
            mTitleEditText.setSelection(strTitleEdit.length());

            String strBodyEdit = mDb.getNoteBodyById(rowId);
            mBodyEditText.setText(strBodyEdit);
            mBodyEditText.setSelection(strBodyEdit.length());
        }
    	else
    	{
            // renew title
			String strTitleEdit = "";
            mTitleEditText.setText(strTitleEdit);
            mTitleEditText.setSelection(strTitleEdit.length());
            mTitleEditText.requestFocus();
            
            // renew body
            String strBodyEdit = "";
            mBodyEditText.setText(strBodyEdit);
            mBodyEditText.setSelection(strBodyEdit.length());
    	}
        mDb.close();
    }
    
    boolean isTitleModified()
    {
    	return !mOriginalTitle.equals(mTitleEditText.getText().toString());
    }
    
    boolean isPictureModified()
    {
    	return !mOriginalPictureUri.equals(mPictureUriInDB);
    }
    
    boolean isAudioModified()
    {
    	if(mOriginalAudioUri == null)
    		return false;
    	else
    		return !mOriginalAudioUri.equals(mAudioUriInDB);
    }    
    
    boolean isBodyModified()
    {
    	return !mOriginalBody.equals(mBodyEditText.getText().toString());
    }
    
    boolean isTimeCreatedModified()
    {
    	return false; 
    }
    
    boolean isNoteModified()
    {
    	boolean bModified = false;
    	if( isTitleModified() || isPictureModified() || isAudioModified() ||
    		isBodyModified() || bRemovePictureUri || bRemoveAudioUri)
    	{
    		bModified = true;
    	}
    	
    	return bModified;
    }
    
    boolean isTextAdded()
    {
    	boolean bEdit = false;
    	String curTitle = mTitleEditText.getText().toString();
    	String curBody = mBodyEditText.getText().toString();
       	if(!Util.isEmptyString(curTitle) ||
       	   !Util.isEmptyString(curBody) || 
       	   Util.isUriExisted(mPictureUriInDB, mAct)) 	
    		bEdit = true;
    	
    	return bEdit;
    }

	public static Long saveStateInDB(Long rowId,boolean enSaveDb, String pictureUri, String audioUri, String drawingUri) 
	{
		boolean mEnSaveDb = enSaveDb;
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
    	String title = mTitleEditText.getText().toString();
    	String body = mBodyEditText.getText().toString();
    	
        if(mEnSaveDb)
        {
	        if (rowId == null) // for Add new
	        {
	        	if( (!title.isEmpty()) || (!body.isEmpty()) ||(!pictureUri.isEmpty()) || (!audioUri.isEmpty()))
	        	{
	        		// insert
	        		System.out.println("Note_common / saveState / insert");
	        		rowId = mDb.insertNote(title, pictureUri, audioUri, drawingUri, body, 0, (long) 0);// add new note, get return row Id
	        	}
        		mCurrentPictureUri = pictureUri; // update file name
        		mCurrentAudioUri = audioUri; // update file name
	        } 
	        else // for Edit
	        {
    	        Date now = new Date(); 
//	        	if( (!title.isEmpty()) || (!body.isEmpty()) || (!pictureUri.isEmpty()) || (!audioUri.isEmpty()) )
	        	if( !Util.isEmptyString(title) || !Util.isEmptyString(body) ||
	        		!Util.isEmptyString(pictureUri) || !Util.isEmptyString(audioUri) )
	        	{
	        		// update
	        		if(bRollBackData) //roll back
	        		{
			        	System.out.println("Note_common / saveState / update: roll back");
	        			title = mOriginalTitle;
	        			body = mOriginalBody;
	        			Long time = mOriginalCreatedTime;
	        			mDb.updateNote(rowId, title, pictureUri, audioUri, drawingUri, body, mOriginalMarking, time);
	        		}
	        		else // update new
	        		{
	        			System.out.println("Note_common / saveState / update new");
	        			mDb.updateNote(rowId, title, pictureUri, audioUri, drawingUri, body, mOriginalMarking, now.getTime()); // update note
	        		}
	        		mCurrentPictureUri = pictureUri; // update file name
	        		mCurrentAudioUri = audioUri; // update file name
	        	}
	        	else if( Util.isEmptyString(title) && Util.isEmptyString(body) &&
			        	 Util.isEmptyString(pictureUri) && Util.isEmptyString(audioUri) )
	        	{
	        		// delete
	        		System.out.println("Note_common / saveState / delete");
	        		mDb.deleteNote(rowId);
	        	}
	        }
        }
        mDb.close();
        
		return rowId;
	}

	public static Long savePictureStateInDB(Long rowId,boolean enSaveDb, String pictureUri, String audioUri, String drawingUri) 
	{
		boolean mEnSaveDb = enSaveDb;
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
        if(mEnSaveDb)
        {
	        if (rowId == null) // for Add new
	        {
	        	if( !pictureUri.isEmpty())
	        	{
	        		// insert
	        		System.out.println("Note_common / saveState / insert");
	        		rowId = mDb.insertNote("", pictureUri, audioUri, drawingUri, "", 1, (long) 0);// add new note, get return row Id
	        	}
        		mCurrentPictureUri = pictureUri; // update file name
	        } 
	        else // for Edit
	        {
    	        Date now = new Date(); 
	        	if( !pictureUri.isEmpty())
	        	{
	        		// update
	        		if(bRollBackData) //roll back
	        		{
			        	System.out.println("Note_common / saveState / update: roll back");
	        			Long time = mOriginalCreatedTime;
	        			mDb.updateNote(rowId, "", pictureUri, audioUri, drawingUri, "", mOriginalMarking, time);
	        		}
	        		else // update new
	        		{
	        			System.out.println("Note_common / saveState / update new");
	        			mDb.updateNote(rowId, "", pictureUri, audioUri, drawingUri, "", 1, now.getTime()); // update note
	        		}
	        		mCurrentPictureUri = pictureUri; // update file name
	        	}
	        	else if(pictureUri.isEmpty())
	        	{
	        		// delete
	        		System.out.println("Note_common / saveState / delete");
	        		mDb.deleteNote(rowId);
	        	}
	        }
        }
        mDb.close();
        
		return rowId;
	}
	
	// for confirmation condition
	public void removePictureStringFromOriginalNote(Long rowId) {
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
    	mDb.updateNote(rowId, 
    				   mOriginalTitle,
    				   "", 
    				   mOriginalAudioUri,
    				   mOriginalDrawingUri,
    				   mOriginalBody,
    				   mOriginalMarking,
    				   mOriginalCreatedTime );
        mDb.close();
	}
	
	public void removePictureStringFromCurrentEditNote(Long rowId) {
        String title = mTitleEditText.getText().toString();
        String body = mBodyEditText.getText().toString();
        
        mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
    	mDb.updateNote(rowId, 
    				   title,
    				   "", 
    				   mOriginalAudioUri,
    				   mOriginalDrawingUri,
    				   body,
    				   mOriginalMarking,
    				   mOriginalCreatedTime );
        mDb.close();
	}
	
	public void removeAudioStringFromOriginalNote(Long rowId) {
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
    	mDb.updateNote(rowId, 
    				   mOriginalTitle,
    				   mOriginalPictureUri, 
    				   "",
    				   mOriginalDrawingUri,
    				   mOriginalBody,
    				   mOriginalMarking,
    				   mOriginalCreatedTime );
        mDb.close();
	}	
	
	public static void removeAudioStringFromCurrentEditNote(Long rowId) {
        String title = mTitleEditText.getText().toString();
        String body = mBodyEditText.getText().toString();
        mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
        mDb.updateNote(rowId, 
    				   title,
    				   mOriginalPictureUri, 
    				   "",
    				   mOriginalDrawingUri,
    				   body,
    				   mOriginalMarking,
    				   mOriginalCreatedTime );
        mDb.close();
	}	
	
	static int getCount()
	{
    	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		int noteCount = mDb.getNotesCount();
		mDb.doClose();
		return noteCount;
	}
	
	// for audio
	public static Long insertAudioToDB(String audioUri) 
	{
		Long rowId = null;
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
       	if( !Util.isEmptyString(audioUri))
    	{
    		// insert
    		System.out.println("Note_common / insertAudioToDB / insert");
    		// set marking to 1 for default
    		rowId = mDb.insertNote("", "", audioUri, "", "", 1, (long) 0);// add new note, get return row Id
    	}
        mDb.close();
		return rowId;
	}
	
}