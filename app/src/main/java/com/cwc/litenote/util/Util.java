package com.cwc.litenote.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.media.video.UtilVideo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

public class Util 
{
    static boolean DEBUG_MODE = false; 
    public static boolean RELEASE_MODE = !DEBUG_MODE;
    //set mode
//    static boolean CODE_MODE = RELEASE_MODE;
    public static boolean CODE_MODE = DEBUG_MODE;
    static boolean PICTURE_PATH_BY_SYSTEM_DEFAULT = true;
    
    SharedPreferences mPref_vibration;
    Context mContext;
    Activity mActivity;
    String mEMailString;
    private static DB mDb;
    public static String NEW_LINE = "\r" + System.getProperty("line.separator");

	static int STYLE_DEFAULT = 1;
    
	static int ACTIVITY_CREATE = 0;
    static int ACTIVITY_VIEW_NOTE = 1;
    static int ACTIVITY_EDIT_NOTE = 2;
    public static int ACTIVITY_TAKE_PICTURE = 3;
    public static int CHOOSER_SET_PICTURE = 4;
    public static int CHOOSER_SET_AUDIO = 5;
    
    int defaultBgClr;
    int defaultTextClr;

    // style
    // 0,2,4,6,8: dark background, 1,3,5,7,9: light background
	public static int[] mBG_ColorArray = new int[]{Color.rgb(34,34,34), //#222222
											Color.rgb(255,255,255),
											Color.rgb(38,87,51), //#265733
											Color.rgb(186,249,142),
											Color.rgb(87,38,51),//#572633
											Color.rgb(249,186,142),
											Color.rgb(38,51,87),//#263357
											Color.rgb(142,186,249),
											Color.rgb(87,87,51),//#575733
											Color.rgb(249,249,140)};
	public static int[] mText_ColorArray = new int[]{Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0)};

    
    public Util(){};
    
	public Util(FragmentActivity activity) {
		mContext = activity;
		mActivity = activity;
	}
	
	public Util(Context context) {
		mContext = context;
	}
	
	// set vibration time
	public void vibrate()
	{
		mPref_vibration = mContext.getSharedPreferences("vibration", 0);
    	if(mPref_vibration.getString("KEY_ENABLE_VIBRATION","yes").equalsIgnoreCase("yes"))
    	{
			Vibrator mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			if(mPref_vibration.getString("KEY_VIBRATION_TIME","25") != "")
			{
				int vibLen = Integer.valueOf(mPref_vibration.getString("KEY_VIBRATION_TIME","25"));
				mVibrator.vibrate(vibLen); //length unit is milliseconds
				System.out.println("vibration len = " + vibLen);
			}
    	}
	}
	
	// export to SD card: for checked pages
	public String exportToSdCard(String filename, List<Boolean> checkedArr,boolean enableToast)
	{   
		//first row text
		String data ="";
		//get data from DB
		if(checkedArr == null)
			data = queryDB(data,null);// all pages
		else
			data = queryDB(data,checkedArr);
		
		// sent data
		data = addXmlTag(data);
		mEMailString = data;
		
		exportToSdCardFile(data,filename);
		
		return mEMailString;
	}
	
	// save to SD card: for NoteView class
	public String exportStringToSdCard(String filename, String curString)
	{   
		//sent data
		String data = "";
		data = data.concat(curString);
		
		mEMailString = data;
		
		exportToSdCardFile(data,filename);
		
		return mEMailString;
	}
	
	// Export data to be SD Card file
	void exportToSdCardFile(String data,String filename)
	{
	    // SD card path + "/" + directory path
	    String dirString = Environment.getExternalStorageDirectory().toString() + 
	    		              "/" + 
	    		              Util.getAppName(mContext);
	    
		File dir = new File(dirString);
		if(!dir.isDirectory())
			dir.mkdir();
		File file = new File(dir, filename);
		file.setReadOnly();
		
//		FileWriter fw = null;
//		try {
//			fw = new FileWriter(file);
//		} catch (IOException e1) {
//			System.out.println("_FileWriter error");
//			e1.printStackTrace();
//		}
//		BufferedWriter bw = new BufferedWriter(fw);
		
		BufferedWriter bw = null;
		OutputStreamWriter osw = null;
		
		int BUFFER_SIZE = 8192;
		try {
			osw = new OutputStreamWriter(new FileOutputStream(file.getPath()), "UTF-8");
			bw = new BufferedWriter(osw,BUFFER_SIZE);
			
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			bw.write(data);
			bw.flush();
			osw.close();
			bw.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
    /**
     * Query current data base
     * @param checkedArr 
     * 
     */
    String queryDB(String data, List<Boolean> checkedArr)
    {
    	String curData = data;
    	
		String strFinalPageViewed_tableId = Util.getPref_lastTimeView_NotesTableId((Activity) mContext);
        DB.setFocus_NotesTableId(strFinalPageViewed_tableId);
    	
    	mDb = new DB(mContext);
    	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
    	int tabCount = DB.getTabsCount();
    	mDb.doClose();
    	for(int i=0;i<tabCount;i++)
    		
    	{
    		// null: all pages
        	if((checkedArr == null ) || ( checkedArr.get(i) == true  ))
    		{
	        	// set Sent string Id
				List<Long> rowArray = new ArrayList<Long>();
        		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
				DB.setFocus_NotesTableId(String.valueOf(DB.getTab_NotesTableId(i)));
				mDb.doClose();
				
        		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
	    		for(int k=0;k<mDb.getNotesCount();k++)
	    		{
    				rowArray.add(k,(long) mDb.getNoteId(k));
	    		}
	    		mDb.doClose();
	    		curData = curData.concat(getStringWithXmlTag(rowArray));
    		}
    	}
    	return curData;
    	
    }
    
    // get current time string
    public static String getCurrentTimeString()
    {
		// set time
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
	
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONDAY)+ 1; //month starts from 0
		int date = cal.get(Calendar.DATE);
		
//		int hour = cal.get(Calendar.HOUR);//12h 
		int hour = cal.get(Calendar.HOUR_OF_DAY);//24h
//		String am_pm = (cal.get(Calendar.AM_PM)== 0) ?"AM":"PM"; // 0 AM, 1 PM
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		int mSec = cal.get(Calendar.MILLISECOND);
		
		String strTime = year 
				+ "" + String.format(Locale.US,"%02d", month)
				+ "" + String.format(Locale.US,"%02d", date)
//				+ "_" + am_pm
				+ "_" + String.format(Locale.US,"%02d", hour)
				+ "" + String.format(Locale.US,"%02d", min)
				+ "" + String.format(Locale.US,"%02d", sec) 
				+ "_" + String.format(Locale.US,"%03d", mSec);
//		System.out.println("time = "+  strTime );
		return strTime;
    }
    
    // get time string
    public static String getTimeString(Long time)
    {
		// set time
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
	
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONDAY)+ 1; //month starts from 0
		int date = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);//24h
//		int hour = cal.get(Calendar.HOUR);//12h 
//		String am_pm = (cal.get(Calendar.AM_PM)== 0) ?"AM":"PM"; // 0 AM, 1 PM
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		
		String strTime = year 
				+ "-" + String.format(Locale.US,"%02d", month)
				+ "-" + String.format(Locale.US,"%02d", date)
//				+ "_" + am_pm
				+ "    " + String.format(Locale.US,"%02d", hour)
				+ ":" + String.format(Locale.US,"%02d", min)
				+ ":" + String.format(Locale.US,"%02d", sec) ;
//		System.out.println("time = "+  strTime );
		
		return strTime;
    }
    
//    void deleteAttachment(String mAttachmentFileName)
//    {
//		// delete file after sending
//		String attachmentPath_FileName = Environment.getExternalStorageDirectory().getPath() + "/" +
//										 mAttachmentFileName;
//		File file = new File(attachmentPath_FileName);
//		boolean deleted = file.delete();
//		if(deleted)
//			System.out.println("delete file is OK");
//		else
//			System.out.println("delete file is NG");
//    }
    
    // add mark to current page
	public void addMarkToCurrentPage(DialogInterface dialogInterface)
	{
		mDb = new DB(mActivity);
	    ListView listView = ((AlertDialog) dialogInterface).getListView();
	    final ListAdapter originalAdapter = listView.getAdapter();
	    final int style = Util.getCurrentPageStyle(mActivity);
        CheckedTextView textViewDefault = new CheckedTextView(mActivity) ;
        defaultBgClr = textViewDefault.getDrawingCacheBackgroundColor();
        defaultTextClr = textViewDefault.getCurrentTextColor();

	    listView.setAdapter(new ListAdapter()
	    {
	        @Override
	        public int getCount() {
	            return originalAdapter.getCount();
	        }
	
	        @Override
	        public Object getItem(int id) {
	            return originalAdapter.getItem(id);
	        }
	
	        @Override
	        public long getItemId(int id) {
	            return originalAdapter.getItemId(id);
	        }
	
	        @Override
	        public int getItemViewType(int id) {
	            return originalAdapter.getItemViewType(id);
	        }
	
	        @Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View view = originalAdapter.getView(position, convertView, parent);
	            //set CheckedTextView in order to change button color
	            CheckedTextView textView = (CheckedTextView)view;
	            mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
	            if(DB.getTab_NotesTableId(position) == Integer.valueOf(DB.getNotes_TableId()))
	            {
		            textView.setTypeface(null, Typeface.BOLD_ITALIC);
		            textView.setBackgroundColor(mBG_ColorArray[style]);
		            textView.setTextColor(mText_ColorArray[style]);
			        if(style%2 == 0)
			        	textView.setCheckMarkDrawable(R.drawable.btn_radio_off_holo_dark);
			        else
			        	textView.setCheckMarkDrawable(R.drawable.btn_radio_off_holo_light);
	            }
	            else
	            {
		        	textView.setTypeface(null, Typeface.NORMAL);
		            textView.setBackgroundColor(defaultBgClr);
		            textView.setTextColor(defaultTextClr);
		            textView.setCheckMarkDrawable(R.drawable.btn_radio_off_holo_dark);
	            }
				mDb.doClose();
	            return view;
	        }

	        @Override
	        public int getViewTypeCount() {
	            return originalAdapter.getViewTypeCount();
	        }

	        @Override
	        public boolean hasStableIds() {
	            return originalAdapter.hasStableIds();
	        }
	
	        @Override
	        public boolean isEmpty() {
	            return originalAdapter.isEmpty();
	        }

	        @Override
	        public void registerDataSetObserver(DataSetObserver observer) {
	            originalAdapter.registerDataSetObserver(observer);
	
	        }
	
	        @Override
	        public void unregisterDataSetObserver(DataSetObserver observer) {
	            originalAdapter.unregisterDataSetObserver(observer);
	
	        }
	
	        @Override
	        public boolean areAllItemsEnabled() {
	            return originalAdapter.areAllItemsEnabled();
	        }
	
	        @Override
	        public boolean isEnabled(int position) {
	            return originalAdapter.isEnabled(position);
	        }
	    });
	}
	
	// get App name
	static public String getAppName(Context context)
	{
		return context.getResources().getString(R.string.app_name);
	}
	
	// get style
	static public int getNewPageStyle(Context context)
	{
		SharedPreferences mPref_style;
		mPref_style = context.getSharedPreferences("style", 0);
		return mPref_style.getInt("KEY_STYLE",STYLE_DEFAULT);
	}
	
	
	// set button color
	static String[] mItemArray = new String[]{"1","2","3","4","5","6","7","8","9","10"};
    public static void setButtonColor(RadioButton rBtn,int iBtnId)
    {
    	if(iBtnId%2 == 0)
    		rBtn.setButtonDrawable(R.drawable.btn_radio_off_holo_dark);
    	else
    		rBtn.setButtonDrawable(R.drawable.btn_radio_off_holo_light);
		rBtn.setBackgroundColor(Util.mBG_ColorArray[iBtnId]);
		rBtn.setText(mItemArray[iBtnId]);
		rBtn.setTextColor(Util.mText_ColorArray[iBtnId]);
    }
	
    // get current page style
	static public int getCurrentPageStyle(Context context)
	{
		int style = 0;
		mDb = new DB(context);
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		style = mDb.getTabStyle(TabsHostFragment.mCurrentTabIndex);
		mDb.doClose();
		
		return style;
	}

	// get style count
	static public int getStyleCount()
	{
		return mBG_ColorArray.length;
	}
	
	///
	// set notes table id of last time view
	public static void setPref_lastTimeView_DrawerTabsTableId(Activity act, int tabsTableId )
	{
	  SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
	  String keyName = "KEY_LAST_TIME_VIEW_DRAWER_TABS_TABLE_ID";
      pref.edit().putInt(keyName, tabsTableId).commit();
	}
	
	// get notes table id of last time view
	public static int getPref_lastTimeView_DrawerTabsTableId(Context context)
	{
		SharedPreferences pref = context.getSharedPreferences("last_time_view", 0);
		String keyName = "KEY_LAST_TIME_VIEW_DRAWER_TABS_TABLE_ID";
		int tabsTableId = pref.getInt(keyName, 1); // notes table Id: default is 1
		return tabsTableId;
	}	
	///
	

	// set notes table id of last time view
	public static void setPref_lastTimeView_NotesTableId(Activity act, int notesTableId )
	{
	  SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
	  String keyPrefix = "KEY_DRAWER_NUMBER_";
	  int tableId = Util.getPref_lastTimeView_DrawerTabsTableId(act);
	  String keyName = keyPrefix.concat(String.valueOf(tableId));
      pref.edit().putInt(keyName, notesTableId).commit();
	}
	
	// get notes table id of last time view
	public static String getPref_lastTimeView_NotesTableId(Context context)
	{
		SharedPreferences pref = context.getSharedPreferences("last_time_view", 0);
		String keyPrefix = "KEY_DRAWER_NUMBER_";
		int tableId = Util.getPref_lastTimeView_DrawerTabsTableId(context);
		String keyName = keyPrefix.concat(String.valueOf(tableId));
		int notesTableId = pref.getInt(keyName, 1); // notes table Id: default is 1
		return String.valueOf(notesTableId);
	}
	
	// set scroll X of drawer of last time view
	public static void setPref_lastTimeView_scrollX_byDrawerNumber(Activity act, int scrollX )
	{
	  SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
	  String keyPrefix = "KEY_DRAWER_NUMBER_";
	  int tableId = Util.getPref_lastTimeView_DrawerTabsTableId(act);
	  String keyName = keyPrefix.concat(String.valueOf(tableId));
	  keyName = keyName.concat("_SCROLL_X");
      pref.edit().putInt(keyName, scrollX).commit();
	}
	
	// get scroll X of drawer of last time view
	public static Integer getPref_lastTimeView_scrollX_byDrawerNumber(Activity act)
	{
		SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
		String keyPrefix = "KEY_DRAWER_NUMBER_";
		int tableId = Util.getPref_lastTimeView_DrawerTabsTableId(act);
		String keyName = keyPrefix.concat(String.valueOf(tableId));
		keyName = keyName.concat("_SCROLL_X");
		int scrollX = pref.getInt(keyName, 0); // default scroll X is 0
		return scrollX;
	}	

	// Set list view first visible Index of last time view
	public static void setPref_lastTimeView_list_view_first_visible_index(Activity act, int index )
	{
	  SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
	  String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX";
	  String location = getCurrentListViewLocation(act);
	  keyName = keyName.concat(location);
      pref.edit().putInt(keyName, index).commit();
	}	
	
	// Get list view first visible Index of last time view
	public static Integer getPref_lastTimeView_list_view_first_visible_index(Activity act)
	{
		SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
		String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX";
		String location = getCurrentListViewLocation(act);
		keyName = keyName.concat(location);		
		int index = pref.getInt(keyName, 0); // default scroll X is 0
		return index;
	}	
	
	// Set list view first visible index Top of last time view
	public static void setPref_lastTimeView_list_view_first_visible_index_top(Activity act, int top )
	{
	  SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
	  String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX_TOP";
	  String location = getCurrentListViewLocation(act);
	  keyName = keyName.concat(location);		
      pref.edit().putInt(keyName, top).commit();
	}
	
	// Get list view first visible index Top of last time view
	public static Integer getPref_lastTimeView_list_view_first_visible_index_top(Activity act)
	{
		SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
		String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX_TOP";
		String location = getCurrentListViewLocation(act);
		keyName = keyName.concat(location);		
		int top = pref.getInt(keyName, 0); 
		return top;
	}
	
	// get Send String with XML tag
	public static String getStringWithXmlTag(List<Long> rowArray)
	{
        String PAGE_TAG_B = "<page>";
        String TAB_TAG_B = "<tabname>";
        String TAB_TAG_E = "</tabname>";
        String NOTEITEM_TAG_B = "<note>";
        String NOTEITEM_TAG_E = "</note>";
        String TITLE_TAG_B = "<title>";
        String TITLE_TAG_E = "</title>";
        String BODY_TAG_B = "<body>";
        String BODY_TAG_E = "</body>";
        String PICTURE_TAG_B = "<picture>";
        String PICTURE_TAG_E = "</picture>";
        String AUDIO_TAG_B = "<audio>";
        String AUDIO_TAG_E = "</audio>";
        String PAGE_TAG_E = "</page>";
        
        String sentString = NEW_LINE;

    	// when page has tab name only, no notes
    	if(rowArray.size() == 0)
    	{
        	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
        	sentString = sentString.concat(NEW_LINE + PAGE_TAG_B );
	        sentString = sentString.concat(NEW_LINE + TAB_TAG_B + DB.getCurrentTabTitle() + TAB_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + NOTEITEM_TAG_B);
	    	sentString = sentString.concat(NEW_LINE + TITLE_TAG_B + TITLE_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + BODY_TAG_B +  BODY_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + PICTURE_TAG_B + PICTURE_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + AUDIO_TAG_B + AUDIO_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + NOTEITEM_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + PAGE_TAG_E );
    		sentString = sentString.concat(NEW_LINE);
    		mDb.doClose();
    	}
    	else
    	{
	        for(int i=0;i< rowArray.size();i++)
	        {
	        	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		    	Cursor cursorNote = mDb.queryNote(rowArray.get(i));
		        String strTitleEdit = cursorNote.getString(
		        		cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_TITLE));
		        strTitleEdit = replaceEscapeCharacter(strTitleEdit);
		        
		        String strBodyEdit = cursorNote.getString(
		        		cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_BODY));
		        strBodyEdit = replaceEscapeCharacter(strBodyEdit);

		        String strPictureUriStr = cursorNote.getString(
		        		cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_PICTURE_URI));
		        strPictureUriStr = replaceEscapeCharacter(strPictureUriStr);

		        String strAudioUriStr = cursorNote.getString(
		        		cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_AUDIO_URI));
		        strAudioUriStr = replaceEscapeCharacter(strAudioUriStr);
		        
		        int mark = cursorNote.getInt(cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_MARKING));
		        String srtMark = (mark == 1)? "[s]":"[n]";
		        
		        if(i==0)
		        {
		        	sentString = sentString.concat(NEW_LINE + PAGE_TAG_B );
		        	sentString = sentString.concat(NEW_LINE + TAB_TAG_B + DB.getCurrentTabTitle() + TAB_TAG_E );
		        }
		        
		        sentString = sentString.concat(NEW_LINE + NOTEITEM_TAG_B); 
		        sentString = sentString.concat(NEW_LINE + TITLE_TAG_B + srtMark + strTitleEdit + TITLE_TAG_E);
		        sentString = sentString.concat(NEW_LINE + BODY_TAG_B + strBodyEdit + BODY_TAG_E);
		        sentString = sentString.concat(NEW_LINE + PICTURE_TAG_B + strPictureUriStr + PICTURE_TAG_E);
		        sentString = sentString.concat(NEW_LINE + AUDIO_TAG_B + strAudioUriStr + AUDIO_TAG_E);
		        sentString = sentString.concat(NEW_LINE + NOTEITEM_TAG_E); 
		        sentString = sentString.concat(NEW_LINE);
		    	if(i==rowArray.size()-1)
		        	sentString = sentString.concat(NEW_LINE +  PAGE_TAG_E);
		    		
		    	mDb.doClose();
	        }
    	}
    	return sentString;
	}

    // replace special character (e.q. amp sign) for avoiding XML paring exception 
	//      &   &amp;
	//      >   &gt;
	//      <   &lt;
	//      '   &apos;
	//      "   &quot;
	static String replaceEscapeCharacter(String str)
	{
        str = str.replaceAll("&", "&amp;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll("'", "&apos;");
        str = str.replaceAll("\"", "&quot;");
        return str;
	}
	
	// add XML tag
	public static String addXmlTag(String str)
	{
		String ENCODING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        String XML_TAG_B = NEW_LINE + "<LiteNote>";
        String XML_TAG_E = NEW_LINE + "</LiteNote>";
        
        String data = ENCODING + XML_TAG_B;
        
        data = data.concat(str);
		data = data.concat(XML_TAG_E);
		
		return data;
	}

	// trim XML tag
	public String trimXMLtag(String string) {
		string = string.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
		string = string.replace("<LiteNote>","");
		string = string.replace("<page>","");
		string = string.replace("<tabname>","=== Page: ");
		string = string.replace("</tabname>"," ===");
		string = string.replace("<note>","--- note ---");
		string = string.replace("<title>","Title: ");
		string = string.replace("</title>","");
		string = string.replace("<body>","Body: ");
		string = string.replace("</body>","");
		string = string.replace("<picture>","Picture: ");
		string = string.replace("</picture>","");		
		string = string.replace("<audio>","Audio: ");
		string = string.replace("</audio>","");		
		string = string.replace("[s]","");
		string = string.replace("[n]","");
		string = string.replace("</note>","");
		string = string.replace("</page>"," ");
		string = string.replace("</LiteNote>","");
		string = string.trim();
		return string;
	}
	
	
	// get real path from Uri
	public static String getRealPathByUri(Context context, Uri contentUri) {
		  Cursor cursor = null;
		  try { 
		    String[] proj = { MediaStore.Images.Media.DATA };
		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		  } 
		  catch (Exception e){
			return null;  
		  }
		  finally {
		    if (cursor != null) {
		      cursor.close();
		    }
		  }
	}
	
	// get display name by URI
	public static String getDisplayNameByUriString(String uriString, Activity activity)
	{
		String display_name = "";
		
		if(Util.isEmptyString(uriString))
			return display_name;
		
		Uri uri = Uri.parse(uriString);
		String scheme = getUriScheme(uriString);
		//System.out.println("Uri string = " + uri.toString());
		//System.out.println("Uri last segment = " + uri.getLastPathSegment());
		
		if(!Util.isEmptyString(scheme) && 
		   scheme.equalsIgnoreCase("content"))
		{
			//String audioID = null, title = null, artist = null;
	        String[] proj = 
	        	{ 
	        		//MediaStore.Audio.Media._ID, 
	        		//MediaStore.Audio.Media.ARTIST,
	        		//MediaStore.Audio.Media.TITLE,
	        		//MediaStore.Audio.Media.DATA, 
	                MediaStore.Audio.Media.DISPLAY_NAME, //??? add more for video display name?
	               	//MediaStore.Audio.Media.DURATION
	            };
	        
	        Cursor cursor = null;
	        try{
	        	cursor = activity.getContentResolver().query(uri, proj, null, null, null);
	        }
	        catch (Exception e)
	        {
//	        	Toast toast = Toast.makeText(activity, "File is not accessible", Toast.LENGTH_SHORT);
//				toast.show();
	        }
	        
            if((cursor != null) && cursor.moveToFirst()) //reset the cursor
            {
                int col_index=-1;
                do
                {
                	//col_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                	//title = cursor.getString(col_index);
                	//col_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                	//artist = cursor.getString(col_index);
                	col_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                	display_name = cursor.getString(col_index);
                }while(cursor.moveToNext());
                cursor.close();
            }
            //System.out.println("artist_name = " + title);
            //System.out.println("artist_band = " + artist);
            //System.out.println("display_name = " + display_name);
		}
		else if(!Util.isEmptyString(scheme) && 
				(scheme.equalsIgnoreCase("http")|| scheme.equalsIgnoreCase("https") ))
		{
            // if display name can not be displayed, then show last segment instead
			//if(isEmptyString(display_name))
          	display_name = uri.getLastPathSegment();
		}
		else if(!Util.isEmptyString(scheme) && 
				(scheme.equalsIgnoreCase("file") ))
		{
            // if display name can not be displayed, then show last segment instead
			//if(isEmptyString(display_name))
          	display_name = uri.getLastPathSegment();
		}		
		//System.out.println("display_name = " + display_name);
                	
        return display_name;
	}
	
	// get scheme by Uri string
	public static String getUriScheme(String string)
	{
 		Uri uri = Uri.parse(string);
		return uri.getScheme();
	}
	
	
	// is URI existed for Activity
	public static boolean isUriExisted(String uriString, Activity activity)
	{
		boolean bFileExist = false;
		if(!Util.isEmptyString(uriString))
		{
			Uri uri = Uri.parse(uriString);
			
			// when scheme is content and check local file
			File file = null;
			try
			{
				file = new File(uri.getPath()); 
			}
			catch(Exception e)
			{
				System.out.println("Util / _isUriExisted / local file not found exception");
			}
			
			if((file != null) && file.exists())
				bFileExist = true;
			else
				bFileExist = false;
			
			// when scheme is content and check remote file
			if(!bFileExist)
			{
				try
				{
					ContentResolver cr = activity.getContentResolver();
					cr.openInputStream(uri); //??? why this could hang up system?
					bFileExist = true;
				}
				catch (FileNotFoundException exception) 
				{
					System.out.println("Util / _isUriExisted / remote file not found exception");
			    }
				catch (SecurityException se)
				{
					System.out.println("Util / _isUriExisted / remote security exception");
				}
				catch (Exception e)
				{
					System.out.println("Util / _isUriExisted / remote exception");
				}
			}
			System.out.println("Util / _isUriExisted / bFileExist (content)= " + bFileExist);
			
			// when scheme is https or http
			try
			{
				if(Patterns.WEB_URL.matcher(uriString).matches())//??? URL can check URI string?
					bFileExist = true;
			}
			catch (Exception e) 
			{

		    }
			System.out.println("Util / _isUriExisted / bFileExist (web url)= " + bFileExist);
		}
		return bFileExist;
	}
	
	// is Empty string
	public static boolean isEmptyString(String str)
	{
		boolean empty = true;
		if( str != null )
		{
			if(str.length() > 0 )
				empty = false;
		}
		return empty;
	}
	
	/***
	 * pictures directory or gallery directory
	 * 
	 * get: storage/emulated/0/
	 * with: Environment.getExternalStorageDirectory();
	 * 
	 * get: storage/emulated/0/Pictures
	 * with: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	 * 
	 * get: storage/emulated/0/DCIM
	 * with: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
	 * or with: Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM";  
	 *  
	 * get: storage/emulated/0/Android/data/com.cwc.litenote/files
	 * with: storageDir[0] got from File[] storageDir = context.getExternalFilesDirs(null);
	 * 
	 * get: storage/ext_sd/Android/data/com.cwc.litenote/files
	 * with: storageDir[1] got from File[] storageDir = context.getExternalFilesDirs(null);
	 *   
	 */
	public static File getPicturesDir(Context context)
    {
    	if(PICTURE_PATH_BY_SYSTEM_DEFAULT)
    	{
    		// Notes: 
    		// 1 for Google Camera App: 
    		// 	 - default path is /storage/sdcard/DCIM/Camera
    		// 	 - Can not save file to external SD card
    		// 2 for hTC default camera App:
    		//   - default path is /storage/ext_sd/DCIM/100MEDIA
    		//   - Can save file to internal SD card and external SD card, it is decided by hTC App
    		
//    		// is saved to preference after taking picture
//    		SharedPreferences pref_takePicture = context.getSharedPreferences("takePicutre", 0);	
//    		String picDirPathPref = pref_takePicture.getString("KEY_SET_PICTURE_DIR","unknown");
//    		System.out.println("--- Util / _getPicturesDir / pictureDirPath = " + picDirPathPref);
    		
    		String dirString;
    		File dir = null;
    		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
        	{
    			dirString = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
        		// add App name for sub-directory
        		dirString = dirString.concat("/"+ Util.getAppName(context));
        		dir = new File(dirString);
        	}
    		return dir;
    	}
    	else
    	{
    		File[] storageDir = context.getExternalFilesDirs(null); 
    		for(File dir:storageDir)
    			System.out.println("storageDir[] = " + dir);
    		// for Kitkat: write permission is off for external SD card, 
    		// but App can freely access Android/data/com.example.foo/ 
    		// on external storage devices with no permissions. 
    		// i.e. 
        	//		storageDir[1] = file:///storage/ext_sd/Android/data/com.cwc.litenote/files
            File appPicturesDir = new File(storageDir[1]+"/"+"pictures");// 0: system 1:ext_sd    
            return appPicturesDir;
        }
    }
    
    static boolean isValid = false;
    static String mStringUrl;
    public static int mResponseCode;
    static String mResponseMessage;
	public static int oneSecond = 1000;
    
	// check network connection
    public static boolean isNetworkConnected(Activity act)
    {
    	final ConnectivityManager conMgr = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
    	final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
    	if (activeNetwork != null && activeNetwork.isConnected()) {
    		System.out.println("network is connected");
    		return true;
    	} else {
    		System.out.println("network is NOT connected");
    		return false;
    	} 
    }
    
    // try Url connection
    protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
    static public void tryUrlConnection(String strUrl, final Activity act) throws Exception 
    {
//    	mStringUrl = strUrl.replaceFirst("^https", "http");
    	mResponseCode = 0;
    	mStringUrl = strUrl;
    	Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() 
            {
        	    try
        	    {
        			String encodedUrl = Uri.encode(mStringUrl, ALLOWED_URI_CHARS);
        			HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
        			conn.setRequestMethod("HEAD");
        			conn.setConnectTimeout(oneSecond); // cause exception if connection error
        			conn.setReadTimeout(oneSecond*4);
        			mResponseCode = conn.getResponseCode();
        	        mResponseMessage = conn.getResponseMessage();
        	    } 
        	    catch (IOException exception) 
        	    {
        	    	mResponseCode = 0;
        	    	mResponseMessage = "io exception";
        	    	System.out.println("------------------ tryUrlConnection / io exception");
        	    	exception.printStackTrace();
				} 
        	    catch (Exception e) 
        	    {
        	    	System.out.println("------------------ tryUrlConnection / exception");
					e.printStackTrace();
				}
        	    System.out.println("Response Code : " + mResponseCode +
        	    				   " / Response Message: " + mResponseMessage );
            }
        });    	
    }    
    
    // location about drawer table Id and notes table Id
    static String getCurrentListViewLocation(Activity act)
    {
    	String strLocation = "";
    	// drawer
    	int tableId = getPref_lastTimeView_DrawerTabsTableId(act);
    	String strDrawer = String.valueOf(tableId);
    	// tab
    	String strTab = getPref_lastTimeView_NotesTableId(act);
    	strLocation = "_" + strDrawer + "_" + strTab;
    	return strLocation;
    }
    
    // check if playing tab is same as focus tab
    public static boolean isFocusTabSameAsPlayingTab(Activity act)
    {
    	if((DrawerActivity.mCurrentPlaying_DrawerIndex == DrawerActivity.mFocusDrawerPos) && 
		   (DrawerActivity.mCurrentPlaying_NotesTblId == 
		 	Integer.valueOf(Util.getPref_lastTimeView_NotesTableId(act))                )    )
		 {
			 return true;
		 }
    	 else
    		 return false;
    }
    
	// get Url array of directory files
    public final static int AUDIO = 0;
    public final static int IMAGE = 1;
    public final static int VIDEO = 2;
    public static String[] getUrlsByFiles(File[] files,int type)
    {
        if(files == null)
        {
        	return null;
        }
        else
        {
        	String path[] = new String[files.length];
            int i=0;
            
	        for(File file : files)
	        {
		        if( ( (type == AUDIO) && (UtilAudio.hasAudioExtention(file)) ) ||
		        	( (type == IMAGE) && (UtilImage.hasImageExtension(file)) ) ||
		        	( (type == VIDEO) && (UtilVideo.hasVideoExtension(file)) )  )	
	            {
		            if(i< files.length)
		            {
//		            	path[i] = "file:///" + file.getPath();
		            	path[i] = "file://" + file.getAbsolutePath();
		            	System.out.println("path[i] = " + path[i]);
		            	i++;
		            }
	            }
	        }
	        return path;
        }
    }		    
    
	// show saved file name
	public static void showSavedFileToast(String string,Activity act)
	{
		Toast.makeText(act,
						string,
						Toast.LENGTH_SHORT)
						.show();
	}
	
	static public void lockOrientation(Activity act) {
//	    if (act.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//	        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
//	    } else {
//	        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
//	    }
	    
	    int currentOrientation = act.getResources().getConfiguration().orientation;
	    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
//		       act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		       act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
	    }
	    else {
//		       act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		       act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
	    }	    
	    
	}
	
	

	static public void unlockOrientation(Activity act) {
	    act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	// get time format string
	static public String getTimeFormatString(long duration)
	{
		long hour = TimeUnit.MILLISECONDS.toHours(duration);
		long min = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hour);
		long sec = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(min);
		String str = String.format(Locale.US,"%2d:%02d:%02d", hour, min, sec);
		return str;
	}
	
	
	
}