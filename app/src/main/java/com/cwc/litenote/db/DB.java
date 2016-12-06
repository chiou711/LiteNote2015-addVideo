package com.cwc.litenote.db;

import java.util.Date;

import com.cwc.litenote.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.widget.Toast;


public class DB   
{

    private static Context mContext = null;
    private static DatabaseHelper mDbHelper ;
    public static SQLiteDatabase mSqlDb;

    
    private static String mNotesTableId;
    // default count
	public static int DEFAULT_NOTES_TABLE_COUNT = 5; // Notes1_1, Notes1_2, Notes1_3, Notes1_4, Notes1_5  
	public static int DEFAULT_TABS_TABLE_COUNT = 3;  // Tabs1, Tabs2, Tabs3
    
    // Table name format: Drawer, Tabs1, Notes1_2 
    static String DB_DRAWER_TABLE_NAME = "Drawer";
    private static String DB_TABS_TABLE_PREFIX = "Tabs";
    private static String DB_NOTES_TABLE_PREFIX = "Notes";
    private static String DB_NOTES_TABLE_NAME; // Note: name = prefix + id
    
    public static final String KEY_NOTE_ID = "_id"; //do not rename _id for using CursorAdapter 
    public static final String KEY_NOTE_TITLE = "note_title";
    public static final String KEY_NOTE_BODY = "note_body";
    public static final String KEY_NOTE_MARKING = "note_marking";
    public static final String KEY_NOTE_PICTURE_URI = "note_picture_uri";
    public static final String KEY_NOTE_AUDIO_URI = "note_audio_uri";
    static final String KEY_NOTE_DRAWING_URI = "note_drawing_uri";
    public static final String KEY_NOTE_CREATED = "note_created";
    
    static final String KEY_TAB_ID = "tab_id"; //can rename _id for using BaseAdapter
    static final String KEY_TAB_TITLE = "tab_title";
    static final String KEY_TAB_NOTES_TABLE_ID = "tab_notes_table_id";
    static final String KEY_TAB_STYLE = "tab_style";
    static final String KEY_TAB_CREATED = "tab_created";
    
    static final String KEY_DRAWER_ID = "drawer_id"; //can rename _id for using BaseAdapter
//    static final String KEY_DRAWER_ID = "_id"; //BaseColumns._ID
    static final String KEY_DRAWER_TABS_TABLE_ID = "drawer_tabs_table_id"; //can rename _id for using BaseAdapter
    public static final String KEY_DRAWER_TITLE = "drawer_title";
    static final String KEY_DRAWER_CREATED = "drawer_created";
    
	

    /** Constructor */
    public DB(Context context) 
    {
        DB.mContext = context;
    }

    public DB open() throws SQLException 
    {
        mDbHelper = new DatabaseHelper(mContext); 
        
        // will call DatabaseHelper.onCreate()first time when database is not created yet
        mSqlDb = mDbHelper.getWritableDatabase();
        return this;  
    }

    public void close() 
    {
        mDbHelper.close(); 
    }
    
    /*
     * DB functions
     * 
     */
	public static Cursor mDrawerCursor;
	public static Cursor mTabCursor;
	public Cursor mNoteCursor;

	public void doOpenByDrawerTabsTableId(int i) 
	{
//		System.out.println("doOpenByDrawerTabsTableId / i =" + i);
		this.open();
		mDrawerCursor = this.getDrawerCursor();
		mTabCursor = this.getTabsCursorByDrawerTabsTableId(i);
		
		//try to get notes cursor 
		//??? unknown reason, last view notes table id could be changed and then cause 
		// an exception when getting this cursor
		try
		{
			mNoteCursor = this.getNotesCursor();
		}
		catch(Exception e)
		{
			// if notes able dose not exist
			int firstExist_TabId = 1;
			int firstExist_NotesTableId = 1;
			int position = 0;
			int mTabCount = DB.getTabsCount();
			System.out.println("Warning: mTabCount = " + mTabCount);
			boolean dGotFirst = false;
			while((position <= mTabCount) && (dGotFirst == false))
	    	{
				// check if request destination is reachable
				if(mTabCursor.moveToPosition(i)) 
				{
					firstExist_TabId = getTabId(position) ;
					System.out.println("mFirstExist_TabId = " + firstExist_TabId);
					firstExist_NotesTableId = getTab_NotesTableId(position);
					System.out.println("firstExist_NotesTableId = " + firstExist_NotesTableId);
					dGotFirst = true;
				}
				position++;
	    	}
			// change table Id to first existed table Id
			setFocus_NotesTableId(String.valueOf(firstExist_NotesTableId));
			// get note cursor again
			mNoteCursor = this.getNotesCursor();
		}
	}

	public void doOpenDrawer() 
	{
		this.open();
		mDrawerCursor = this.getDrawerCursor();
	}	
	
	public void doClose()	
	{
		this.close();
	}
	
    // delete DB
	public static void deleteDB()
	{
        mSqlDb = mDbHelper.getWritableDatabase();
        try {
	    	mSqlDb.beginTransaction();
	        mContext.deleteDatabase(DatabaseHelper.DB_NAME);
	        mSqlDb.setTransactionSuccessful();
	    }
	    catch (Exception e) {
	    }
	    finally {
	    	Toast.makeText(mContext,R.string.config_delete_DB_toast,Toast.LENGTH_SHORT).show();
	    	mSqlDb.endTransaction();
	    }
	}         
	
	public static boolean isTableExisted(String tableName) 
	{
	    Cursor cursor = mSqlDb.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
	    if(cursor!=null) 
	    {
	        if(cursor.getCount()>0) 
	        {
	        	cursor.close();
	            return true;
	        }
	        cursor.close();
	    }
	    return false;
	}		
	
    /**
     *  Notes table
     * 
     */
    // table columns: for note
    String[] strNoteColumns = new String[] {
          KEY_NOTE_ID,
          KEY_NOTE_TITLE,
          KEY_NOTE_PICTURE_URI,
          KEY_NOTE_AUDIO_URI,
          KEY_NOTE_DRAWING_URI,
          KEY_NOTE_BODY,
          KEY_NOTE_MARKING,
          KEY_NOTE_CREATED
      };

    //insert new notes table by 
    // 1 SQLiteDatabase
    // 2 assigned drawer Id
    // 3 notes table Id
    public static void insertNotesTable(SQLiteDatabase sqlDb, int drawerId, int id)
    {   
    	{
    		//format "Notes1_2"
        	DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(drawerId)+"_"+String.valueOf(id));
            String dB_insert_table = "CREATE TABLE IF NOT EXISTS " + DB_NOTES_TABLE_NAME + "(" +
            							KEY_NOTE_ID + " INTEGER PRIMARY KEY," +
            							KEY_NOTE_TITLE + " TEXT," +  
            							KEY_NOTE_PICTURE_URI + " TEXT," +  
            							KEY_NOTE_AUDIO_URI + " TEXT," +  
            							KEY_NOTE_DRAWING_URI + " TEXT," +  
            							KEY_NOTE_BODY + " TEXT," +
            							KEY_NOTE_MARKING + " INTEGER," +
            							KEY_NOTE_CREATED + " INTEGER);";
            sqlDb.execSQL(dB_insert_table);         
    	}
    }

    //delete notes table
    public void dropNotesTable(int id)
    {   
    	{
    		//format "Notes1_2"
        	DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(getFocus_DrawerTabsTableId())+"_"+String.valueOf(id));
            String dB_drop_table = "DROP TABLE IF EXISTS " + DB_NOTES_TABLE_NAME + ";";
            mSqlDb.execSQL(dB_drop_table);         
    	}
    }   
    
    //delete notes table by drawer tabs table Id
    public void dropNotesTable(int drawerTabsTableId, int id)
    {   
    	{
    		//format "Notes1_2"
        	DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(drawerTabsTableId)+"_"+String.valueOf(id));
            String dB_drop_table = "DROP TABLE IF EXISTS " + DB_NOTES_TABLE_NAME + ";";
            mSqlDb.execSQL(dB_drop_table);         
    	}
    } 
    
    // select all notes
    public Cursor getNotesCursor() {
        return mSqlDb.query(DB_NOTES_TABLE_NAME, 
             strNoteColumns,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    //set note table id
    public static void setFocus_NotesTableId(String id)
    {
    	mNotesTableId = id;
    	
    	// table number initialization: name = prefix + id
        DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(getFocus_DrawerTabsTableId())+"_"+mNotesTableId);
    	System.out.println("DB / _setNoteTableId / mNoteTableId = " + mNotesTableId);
    }  
    
    //set note table id
    public static void setSelected_NotesTableId(String id)
    {
    	mNotesTableId = id;
    	
    	// table number initialization: name = prefix + id
        DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(getFocus_DrawerTabsTableId())+"_"+mNotesTableId);
    	System.out.println("DB / _setNoteTableId mNoteTableId=" + mNotesTableId);
    }  
    
    //get note table id
    public static String getNotes_TableId()
    {
    	return mNotesTableId;
    }  
    
    // Insert note
    // createTime: 0 will update time
    public long insertNote(String title,String pictureUri, String audioUri, String drawingUri, String body, int marking, Long createTime)
    { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_NOTE_TITLE, title);   
        args.put(KEY_NOTE_PICTURE_URI, pictureUri);
        args.put(KEY_NOTE_AUDIO_URI, audioUri);
        args.put(KEY_NOTE_DRAWING_URI, drawingUri);
        args.put(KEY_NOTE_BODY, body);
        if(createTime == 0)
        	args.put(KEY_NOTE_CREATED, now.getTime());
        else
        	args.put(KEY_NOTE_CREATED, createTime);
        	
        args.put(KEY_NOTE_MARKING,marking);
        return mSqlDb.insert(DB_NOTES_TABLE_NAME, null, args);  
    }  
    
    public boolean deleteNote(long rowId) {  
        return mSqlDb.delete(DB_NOTES_TABLE_NAME, KEY_NOTE_ID + "=" + rowId, null) > 0;
    }
    
    //query note
    public Cursor queryNote(long rowId) throws SQLException 
    {  
        Cursor mCursor = mSqlDb.query(true,
					                DB_NOTES_TABLE_NAME,
					                new String[] {KEY_NOTE_ID,
				  								  KEY_NOTE_TITLE,
				  								  KEY_NOTE_PICTURE_URI,
				  								  KEY_NOTE_AUDIO_URI,
				  								  KEY_NOTE_DRAWING_URI,
        										  KEY_NOTE_BODY,
        										  KEY_NOTE_MARKING,
        										  KEY_NOTE_CREATED},
					                KEY_NOTE_ID + "=" + rowId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // update note
    // 		createTime:  0 for Don't update time
    public boolean updateNote(long rowId, String title, String pictureUri, String audioUri, String drawingUri, String body, long marking, long createTime) { 
        ContentValues args = new ContentValues();
        args.put(KEY_NOTE_TITLE, title);
        args.put(KEY_NOTE_PICTURE_URI, pictureUri);
        args.put(KEY_NOTE_AUDIO_URI, audioUri);
        args.put(KEY_NOTE_DRAWING_URI, drawingUri);
        args.put(KEY_NOTE_BODY, body);
        args.put(KEY_NOTE_MARKING, marking);
        
        if(createTime == 0)
        	args.put(KEY_NOTE_CREATED, mNoteCursor.getLong(mNoteCursor.getColumnIndex(KEY_NOTE_CREATED)));
        else
        	args.put(KEY_NOTE_CREATED, createTime);

        int cUpdateItems = mSqlDb.update(DB_NOTES_TABLE_NAME, args, KEY_NOTE_ID + "=" + rowId, null);
        return cUpdateItems > 0;
    }    

	public int getNotesCount()
	{
		return mNoteCursor.getCount();
		//08-19 16:52:15.753: E/AndroidRuntime(6623): android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed. # Open Cursors=544 (# cursors opened by this proc=544)

	}

	public int getMaxNoteId() {
		Cursor cursor = this.getNotesCursor();
		int total = cursor.getColumnCount();
		int iMax =1;
		int iTemp = 1;
		for(int i=0;i< total;i++)
		{
			cursor.moveToPosition(i);
			iTemp = cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID));
			iMax = (iTemp >= iMax)? iTemp: iMax;
		}
		return iMax;
	}
	
	public int getCheckedNotesCount()
	{
		int cCheck =0;
		for(int i=0;i< getNotesCount() ;i++)
		{
			if(getNoteMarking(i) == 1)
				cCheck++;
		}
		return cCheck;
	}		
	
	
	// get note by Id
	public String getNoteTitleById(Long mRowId) 
	{
		return queryNote(mRowId).getString(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_TITLE));
	}
	
	public String getNoteBodyById(Long mRowId) 
	{
		return  queryNote(mRowId).getString(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_BODY));
	}

	public String getNotePictureUriById(Long mRowId)
	{
        Cursor noteCursor = queryNote(mRowId);
        String pictureFileName = noteCursor.getString(noteCursor
														.getColumnIndexOrThrow(DB.KEY_NOTE_PICTURE_URI));
        
		return pictureFileName;
	}
	
	public String getNoteAudioUriById(Long mRowId)
	{
        Cursor noteCursor = queryNote(mRowId);
		String pictureFileName = noteCursor.getString(noteCursor
														.getColumnIndexOrThrow(DB.KEY_NOTE_AUDIO_URI));
		return pictureFileName;
	}	
	
	public String getNoteDrawingUriById(Long mRowId)
	{
        Cursor noteCursor = queryNote(mRowId);
		String pictureFileName = noteCursor.getString(noteCursor
														.getColumnIndexOrThrow(DB.KEY_NOTE_DRAWING_URI));
		return pictureFileName;
	}		
	
	public Long getNoteMarkingById(Long mRowId) 
	{
		return  queryNote(mRowId).getLong(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_MARKING));
	}

	public Long getNoteCreatedTimeById(Long mRowId)
	{
		return  queryNote(mRowId).getLong(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_CREATED));
	}

	// get note by position
	public Long getNoteId(int position)
	{
		mNoteCursor.moveToPosition(position);
        return (long) mNoteCursor.getInt(mNoteCursor.getColumnIndex(KEY_NOTE_ID));
	}
	
	public String getNoteTitle(int position)
	{
		mNoteCursor.moveToPosition(position);
        return mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_TITLE));
	}
	
	public String getNoteBody(int position)
	{
		mNoteCursor.moveToPosition(position);
        return mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_BODY));
	}

	public String getNotePictureUri(int position)
	{
		mNoteCursor.moveToPosition(position);
        return mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_PICTURE_URI));
	}
	
	public String getNoteAudioUri(int position)
	{
		mNoteCursor.moveToPosition(position);
        return mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_AUDIO_URI));
	}	

	String getNoteDrawingUri(int position)
	{
		mNoteCursor.moveToPosition(position);
        return mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_DRAWING_URI));
	}	
	
	public int getNoteMarking(int position)
	{
		mNoteCursor.moveToPosition(position);
		return mNoteCursor.getInt(mNoteCursor.getColumnIndex(KEY_NOTE_MARKING));
	}
	
	public Long getNoteCreatedTime(int position)
	{
		mNoteCursor.moveToPosition(position);
		return mNoteCursor.getLong(mNoteCursor.getColumnIndex(KEY_NOTE_CREATED));
	}

    /*
     * Tab 
     * 
     */
	
    // table columns: for tab info
    String[] strTabColumns = new String[] {
            KEY_TAB_ID,
            KEY_TAB_TITLE,
            KEY_TAB_NOTES_TABLE_ID,
            KEY_TAB_STYLE,
            KEY_TAB_CREATED
        };   

    // select tabs cursor
    public Cursor getTabsCursorByDrawerTabsTableId(int i) {
        return mSqlDb.query(DB_TABS_TABLE_PREFIX + String.valueOf(i), 
             strTabColumns,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }     
    
    // insert tabs table
    public static void insertTabsTable(SQLiteDatabase sqlDb, int id)
    {
    	// table for Tabs
		String tableCreated = DB_TABS_TABLE_PREFIX.concat(String.valueOf(id));
        String DB_CREATE = "CREATE TABLE IF NOT EXISTS " + tableCreated + "(" + 
		            		KEY_TAB_ID + " INTEGER PRIMARY KEY," +
		            		KEY_TAB_TITLE + " TEXT," +
		            		KEY_TAB_NOTES_TABLE_ID + " INTEGER," +
		            		KEY_TAB_STYLE + " INTEGER," +
		            		KEY_TAB_CREATED + " INTEGER);";
        sqlDb.execSQL(DB_CREATE);  
        
        // set default tab info
		String tabs_table = DB_TABS_TABLE_PREFIX.concat(String.valueOf(id));
		insertTab(sqlDb,tabs_table,"N1",1,"N1",1,0);
		insertTab(sqlDb,tabs_table,"N2",2,"N2",2,1); 
		insertTab(sqlDb,tabs_table,"N3",3,"N3",3,2); 
		insertTab(sqlDb,tabs_table,"N4",4,"N4",4,3); 
		insertTab(sqlDb,tabs_table,"N5",5,"N5",5,4);         
    }
    
    // delete tabs table
    public void dropTabsTable(int tableId)
    {
		//format "Tabs1"
    	String DB_TABS_TABLE_NAME = DB_TABS_TABLE_PREFIX.concat(String.valueOf(tableId));
        String dB_drop_table = "DROP TABLE IF EXISTS " + DB_TABS_TABLE_NAME + ";";
        mSqlDb.execSQL(dB_drop_table);
    }
    
    // insert tab with SqlDb parameter
    public static long insertTab(SQLiteDatabase sqlDb, String intoTable,String title,long ntId, String plTitle, long plId, int style) 
    { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_TAB_TITLE, title);
        args.put(KEY_TAB_NOTES_TABLE_ID, ntId);
        args.put(KEY_TAB_STYLE, style);
        args.put(KEY_TAB_CREATED, now.getTime());
        return sqlDb.insert(intoTable, null, args);  
    }    
    
    // insert tab
    public static long insertTab(String intoTable,String title,long ntId, String plTitle, long plId, int style) 
    { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_TAB_TITLE, title);
        args.put(KEY_TAB_NOTES_TABLE_ID, ntId);
        args.put(KEY_TAB_STYLE, style);
        args.put(KEY_TAB_CREATED, now.getTime());
        return mSqlDb.insert(intoTable, null, args);  
    }        
    
    // delete tab
    public long deleteTab(String table,int id) 
    { 
        return mSqlDb.delete(table, KEY_TAB_ID + "='" + id +"'", null);  
    }

    //query single tab info
    public Cursor queryTab(String table, long id) throws SQLException 
    {  
        Cursor mCursor = mSqlDb.query(true,
        							table,
					                new String[] {KEY_TAB_ID,
        										  KEY_TAB_TITLE,
        										  KEY_TAB_NOTES_TABLE_ID,
        										  KEY_TAB_STYLE,
        										  KEY_TAB_CREATED},
					                KEY_TAB_ID + "=" + id,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    
    //update tab
    public boolean updateTab(long id, String title, long ntId, int style) 
    { 
        ContentValues args = new ContentValues();
        Date now = new Date(); 
        args.put(KEY_TAB_TITLE, title);
        args.put(KEY_TAB_NOTES_TABLE_ID, ntId);
        args.put(KEY_TAB_STYLE, style);
        args.put(KEY_TAB_CREATED, now.getTime());
        return mSqlDb.update(DB_TABS_TABLE_PREFIX+String.valueOf(getFocus_DrawerTabsTableId()), args, KEY_TAB_ID + "=" + id, null) > 0;
    }    
    
	public static int getTabsCount()	
	{
		return mTabCursor.getCount();
	}

	public int getTabId(int position) 
	{
		mTabCursor.moveToPosition(position);
        return mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_ID));
	}

    //get current tab info title
    public static String getCurrentTabTitle()
    {
    	String title = null;
    	for(int i=0;i< getTabsCount(); i++ )
    	{
    		if( Integer.valueOf(getNotes_TableId()) == getTab_NotesTableId(i))
    		{
    			title = getTabTitle(i);
    		}
    	}
    	return title;
    }     	

	public static int getTab_NotesTableId(int position)	
	{
		mTabCursor.moveToPosition(position);
        return mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_NOTES_TABLE_ID));
	}
	
	public static String getTabTitle(int position) 
	{
		mTabCursor.moveToPosition(position);
        return mTabCursor.getString(mTabCursor.getColumnIndex(KEY_TAB_TITLE));
	}
	
	public int getTabStyle(int position)	
	{
		mTabCursor.moveToPosition(position); //08-20 22:53:21.975: E/AndroidRuntime(18716): android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed. # Open Cursors=590 (# cursors opened by this proc=590)
        return mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_STYLE));
	}
    
	/*
	 * Drawer
	 * 
	 * 
	 */
    static int mDrawer_tabsTableId;
    
    // table columns: for drawer
    String[] strDrawerColumns = new String[] {
        KEY_DRAWER_ID + " AS " + BaseColumns._ID,
    	KEY_DRAWER_TABS_TABLE_ID,
    	KEY_DRAWER_TITLE,
    	KEY_DRAWER_CREATED
      };
    
    
    public Cursor getDrawerCursor() {
        return mSqlDb.query(DB_DRAWER_TABLE_NAME, 
        	 strDrawerColumns,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    //query note
    public Cursor queryDrawer(long rowId) throws SQLException 
    {  
        Cursor mCursor = mSqlDb.query(true,
					                DB_DRAWER_TABLE_NAME,
        							strDrawerColumns,
        							KEY_DRAWER_ID + "=" + rowId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public long insertDrawer(int tableId, String title) 
    { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_DRAWER_TABS_TABLE_ID, tableId);
        args.put(KEY_DRAWER_TITLE, title);
        args.put(KEY_DRAWER_CREATED, now.getTime());
        return mSqlDb.insert(DB_DRAWER_TABLE_NAME, null, args);  
    }  
    
    public long deleteDrawerId(int id) 
    { 
        return mSqlDb.delete(DB_DRAWER_TABLE_NAME, KEY_DRAWER_ID + "='" + id +"'", null);  
    }
    
    
    // update drawer
    public boolean updateDrawer(long rowId, int drawerTabsTableId, String drawerTitle) { 
        ContentValues args = new ContentValues();
        Date now = new Date();  
        args.put(KEY_DRAWER_TABS_TABLE_ID, drawerTabsTableId);
        args.put(KEY_DRAWER_TITLE, drawerTitle);
       	args.put(KEY_DRAWER_CREATED, now.getTime());

        int cUpdateItems = mSqlDb.update(DB_DRAWER_TABLE_NAME, args, KEY_DRAWER_ID + "=" + rowId, null);
        return cUpdateItems > 0;
    }    
    
    public static long getDrawerId(int position)
    {
    	mDrawerCursor.moveToPosition(position);
    	// note: KEY_DRAWER_ID + " AS " + BaseColumns._ID 
        return (long) mDrawerCursor.getInt(mDrawerCursor.getColumnIndex(BaseColumns._ID));
    }
    
    public static void setFocus_DrawerTabsTableId(int i)
    {
    	mDrawer_tabsTableId = i;
    }
    
    public static int getFocus_DrawerTabsTableId()
    {
    	return mDrawer_tabsTableId;
    }
    
    public static void setSelected_DrawerTabsTableId(int i)
    {
    	mDrawer_tabsTableId = i;
    }
    
    static int getSelected_DrawerTabsTableId()
    {
    	return mDrawer_tabsTableId;
    }    
    
    public static int getDrawersCount()
    {
    	return mDrawerCursor.getCount();
    }
    
    public static int getDrawerTabsTableId(int position)
    {
		mDrawerCursor.moveToPosition(position);
        return mDrawerCursor.getInt(mDrawerCursor.getColumnIndex(KEY_DRAWER_TABS_TABLE_ID));
    	
    }
    
	public String getDrawerTitle(int position)	
	{
		mDrawerCursor.moveToPosition(position);
        return mDrawerCursor.getString(mDrawerCursor.getColumnIndex(KEY_DRAWER_TITLE));
	}
	
	// get drawer title by Id
	public String getDrawerTitleById(Long mRowId) 
	{
		return queryDrawer(mRowId).getString(queryDrawer(mRowId)
											.getColumnIndexOrThrow(DB.KEY_DRAWER_TITLE));
	}
    
	// get current tabs table name
	public static String getFocusTabsTableName()
	{
		return DB.DB_TABS_TABLE_PREFIX + DB.getFocus_DrawerTabsTableId();
	}
}