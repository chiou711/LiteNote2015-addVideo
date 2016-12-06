package com.cwc.litenote;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.cwc.litenote.config.Config;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.lib.DragSortController;
import com.cwc.litenote.lib.DragSortListView;
import com.cwc.litenote.lib.SimpleDragSortCursorAdapter;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.GalleryGridAct;
import com.cwc.litenote.media.image.SlideshowInfo;
import com.cwc.litenote.media.image.SlideshowPlayer;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.note.Note_addAudio;
import com.cwc.litenote.note.Note_addCameraImage;
import com.cwc.litenote.note.Note_addCameraVideo;
import com.cwc.litenote.note.Note_addNewText;
import com.cwc.litenote.note.Note_addNew_optional;
import com.cwc.litenote.note.Note_addNew_optional_for_multiple;
import com.cwc.litenote.note.Note_addReadyImage;
import com.cwc.litenote.note.Note_addReadyVideo;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.note.Note_view_pager_buttons_controller;
import com.cwc.litenote.util.SendMailAct;
import com.cwc.litenote.util.Util;

import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class DrawerActivity extends FragmentActivity implements OnBackStackChangedListener 
{
    private DrawerLayout mDrawerLayout;
    private DragSortController mController;
    private DragSortListView mDrawerListView;
    
    static ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mAppTitle;
    static Context mContext;
	static Config mConfigFragment;
	static boolean bEnableConfig;
    static Menu mMenu;
    static DB mDb;
    static DrawerInfoAdapter drawerInfoAdapter;
    static List<String> mDrawerTitles;
    public static int mFocusDrawerPos;
    
    SharedPreferences mPref_add_new_note_option;
    private static SharedPreferences mPref_delete_warn;
	static NoisyAudioStreamReceiver noisyAudioStreamReceiver;
	static IntentFilter intentFilter;
	static FragmentActivity mDrawerActivity;
	
	int mFirstExist_DrawerId = 0;		
	int mSecondExist_DrawerId = 0;		
	int mLastExist_DrawerId = 0;
	private static Cursor mDrawerCursor;
	static int mDrawerCount;
	static FragmentManager fragmentManager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	///
//    	 StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//
//    	   .detectDiskReads()
//    	   .detectDiskWrites()
//    	   .detectNetwork() 
//    	   .penaltyLog()
//    	   .build());
//
//    	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
////    	   .detectLeakedSqlLiteObjects() //??? unmark this line will cause strict mode error
//    	   .penaltyLog() 
//    	   .penaltyDeath()
//    	   .build());     	
    	///
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_activity_main);
        
        if(Util.CODE_MODE == Util.RELEASE_MODE) 
        {
        	OutputStream nullDev = new OutputStream() 
            {
                public  void    close() {}
                public  void    flush() {}
                public  void    write(byte[] b) {}
                public  void    write(byte[] b, int off, int len) {}
                public  void    write(int b) {}
            }; 
            System.setOut( new PrintStream(nullDev));
        }
        
        //Log.d below can be disabled by applying proguard
        //1. enable proguard-android-optimize.txt in project.properties
        //2. be sure to use newest version to avoid build error
        //3. add the following in proguard-project.txt
        /*-assumenosideeffects class android.util.Log {
        public static boolean isLoggable(java.lang.String, int);
        public static int v(...);
        public static int i(...);
        public static int w(...);
        public static int d(...);
        public static int e(...);
    	}
        */
        Log.d("test log tag","start app");         
        
        System.out.println("================start application ==================");
        System.out.println("DrawerActivity / onCreate");

        UtilImage.getDefaultSacleInPercent(DrawerActivity.this);
        
        mAppTitle = getTitle();
        
        mDrawerTitles = new ArrayList<String>();

        // get last time drawer number, default drawer number: 1
        int iLastView_DrawerTabsTableId = Util.getPref_lastTimeView_DrawerTabsTableId(this);
        System.out.println("iDrawerTabsTableId = " + iLastView_DrawerTabsTableId);

		Context context = getApplicationContext();
        DB.setFocus_DrawerTabsTableId(iLastView_DrawerTabsTableId);
        DB.setFocus_NotesTableId(Util.getPref_lastTimeView_NotesTableId(this)); 

        mDb = new DB(context);  
		mDb.doOpenDrawer();
        
        
        if (savedInstanceState == null)
        {
        	for(int i=0;i<DB.getDrawersCount();i++)
        	{
	        	if(	DB.getDrawerTabsTableId(i)== iLastView_DrawerTabsTableId)
	        	{
	        		mFocusDrawerPos =  i;
	    			System.out.println("DrawerActivity / onCreate /  mFocusDrawerId = " + mFocusDrawerPos);
	        	}
        	}
        	AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_STOP;
        	mIsCalledWhilePlayingAudio = false;
        }

		if(DB.getDrawersCount() == 0)
		{
			String drawerPrefix = "D";
	        for(int i=0;i< DB.DEFAULT_TABS_TABLE_COUNT;i++)
	        {
	        	String drawerTitle = drawerPrefix.concat(String.valueOf(i+1));
	        	mDrawerTitles.add(drawerTitle);
	        	mDb.insertDrawer(i+1, drawerTitle );
	        }
		}
		else
		{
		    for(int i=0;i< DB.getDrawersCount();i++)
	        {
	        	mDrawerTitles.add(""); // init only
	        	mDrawerTitles.set(i, mDb.getDrawerTitle(i)); 
	        }
		}
		mDb.doClose();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView = (DragSortListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        // set adapter
    	mDb.doOpenDrawer();
    	Cursor cursor = DB.mDrawerCursor;
        
        String[] from = new String[] { DB.KEY_DRAWER_TITLE};
        int[] to = new int[] { R.id.drawerText };
        
        drawerInfoAdapter = new DrawerInfoAdapter(
				this,
				R.layout.drawer_list_item,
				cursor,
				from,
				to,
				0
				);
        
        mDb.doClose();
        mDrawerListView.setAdapter(drawerInfoAdapter);
   
        // set up click listener
        mDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
        // set up long click listener
        mDrawerListView.setOnItemLongClickListener(new DrawerItemLongClickListener());
        
        mController = buildController(mDrawerListView);
        mDrawerListView.setFloatViewManager(mController);
        mDrawerListView.setOnTouchListener(mController);
        mDrawerListView.setDragEnabled(true);
        mDrawerListView.setDragListener(onDrag);
        mDrawerListView.setDropListener(onDrop);
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
	                this,                  /* host Activity */
	                mDrawerLayout,         /* DrawerLayout object */
	                R.drawable.ic_drawer,  /* navigation drawer image to replace 'Up' caret */
	                R.string.drawer_open,  /* "open drawer" description for accessibility */
	                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) 
        {
            public void onDrawerClosed(View view) 
            {
        		System.out.println("mDrawerToggle onDrawerClosed ");
        		mDb.doOpenDrawer();
        		int pos = mDrawerListView.getCheckedItemPosition();
        		int tblId = DB.getDrawerTabsTableId(pos);
        		DB.setSelected_DrawerTabsTableId(tblId);        		
        		mDrawerTitle = mDb.getDrawerTitle(pos);
        		mDb.doClose();  
                setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) 
            {
        		System.out.println("mDrawerToggle onDrawerOpened ");
                setTitle(mAppTitle);
                drawerInfoAdapter.notifyDataSetChanged();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mContext = getBaseContext();
        bEnableConfig = false;
        
        mDrawerActivity = this;
        
        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);
        
		// register an audio stream receiver
		if(noisyAudioStreamReceiver == null)
		{
			noisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
			intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY); 
			registerReceiver(noisyAudioStreamReceiver, intentFilter);
		}
    }

    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv)
    {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);
        
        //drag //??? need to add for enabling Drag?
//	  	mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);
//	  	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
	  		controller.setDragInitMode(DragSortController.ON_DOWN); // click
//	  	else
//	        controller.setDragInitMode(DragSortController.MISS); 
//
	  	controller.setDragHandleId(R.id.drawer_dragger);// handler
	  	controller.setBackgroundColor(Color.argb(128,128,64,0));// background color when dragging

        return controller;
    }        

    // list view listener: on drag
    private DragSortListView.DragListener onDrag = new DragSortListView.DragListener() 
    {
                @Override
                public void drag(int startPosition, int endPosition) {
//                	System.out.println("DrawerActivity / onDrag");
                }
    };    

    // list view listener: on drop
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() 
    {
        @Override
        public void drop(int startPosition, int endPosition) {
//        	System.out.println("DrawerActivity / onDrop / startPosition = " + startPosition);    
//        	System.out.println("DrawerActivity / onDrop / endPosition = " + endPosition);    
        	
			//reorder data base storage
			int loop = Math.abs(startPosition-endPosition);
			for(int i=0;i< loop;i++)
			{
				swapDrawerRows(startPosition,endPosition);
				if((startPosition-endPosition) >0)
					endPosition++;
				else
					endPosition--;
			}
			
			drawerInfoAdapter.notifyDataSetChanged();
			
			updateFocusPosition();
        }
    };    
    
    // Update focus position
    void updateFocusPosition()
    {
		//update focus position
		int iLastView_DrawerTabsTableId = Util.getPref_lastTimeView_DrawerTabsTableId(DrawerActivity.this);
    	for(int i=0;i<DB.getDrawersCount();i++)
    	{
        	if(	DB.getDrawerTabsTableId(i)== iLastView_DrawerTabsTableId)
        	{
        		mFocusDrawerPos =  i;
        		mDrawerListView.setItemChecked(mFocusDrawerPos, true); 		
        	}
    	}
    }

    private static Long mDrawerId1 = (long) 1;
    private static Long mDrawerId2 = (long) 1;
	private static int mDrawerTabsTableId1;
	private static int mDrawerTabsTableId2;
	private static String mDrawerTitle1;
	private static String mDrawerTitle2;
	
    // swap rows
	protected static void swapDrawerRows(int startPosition, int endPosition) 
	{
		mDb.doOpenDrawer();

		mDrawerId1 = DB.getDrawerId(startPosition);
		mDrawerTabsTableId1 = DB.getDrawerTabsTableId(startPosition);
		mDrawerTitle1 = mDb.getDrawerTitle(startPosition);

		mDrawerId2 = DB.getDrawerId(endPosition);
		mDrawerTabsTableId2 = DB.getDrawerTabsTableId(endPosition);
		mDrawerTitle2 = mDb.getDrawerTitle(endPosition);

        mDb.updateDrawer(mDrawerId1,
        		mDrawerTabsTableId2,
        		mDrawerTitle2);		        
		
		mDb.updateDrawer(mDrawerId2,
				mDrawerTabsTableId1,
				mDrawerTitle1);	
    	mDb.doClose();
	}    
    
    /*
     * Life cycle
     * 
     */
    // for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState) 
    {
       super.onSaveInstanceState(outState);
//  	   System.out.println("DrawerActivity / onSaveInstanceState / mFocusDrawerPos = " + mFocusDrawerPos);
       outState.putInt("CurrentDrawerIndex",mFocusDrawerPos);
       outState.putInt("CurrentPlaying_TabIndex",mCurrentPlaying_TabIndex);
       outState.putInt("CurrentPlaying_DrawerIndex",mCurrentPlaying_DrawerIndex);
       outState.putInt("SeekBarProgress",NoteFragment.mProgress);
       outState.putInt("PlayerState",AudioPlayer.mPlayerState);
       outState.putBoolean("CalledWhilePlayingAudio", mIsCalledWhilePlayingAudio);
       mHandler.removeCallbacks(mTabsHostRun);
       mHandler = null;
    }
    
    // for After Rotate
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
//		System.out.println("DrawerActivity / _onRestoreInstanceState ");
    	if(savedInstanceState != null)
    	{
    		mFocusDrawerPos = savedInstanceState.getInt("CurrentDrawerIndex");
    		mCurrentPlaying_TabIndex = savedInstanceState.getInt("CurrentPlaying_TabIndex");
    		mCurrentPlaying_DrawerIndex = savedInstanceState.getInt("CurrentPlaying_DrawerIndex");
    		AudioPlayer.mPlayerState = savedInstanceState.getInt("PlayerState");
    		NoteFragment.mProgress = savedInstanceState.getInt("SeekBarProgress");
//    		System.out.println("DrawerActivity / onRestoreInstanceState / AudioPlayer.mPlayerState = " + AudioPlayer.mPlayerState);
    		mIsCalledWhilePlayingAudio = savedInstanceState.getBoolean("CalledWhilePlayingAudio");
    	}    
    	
    }

    @Override
    protected void onResume() 
    {
//    	System.out.println("DrawerActivity / _onResume"); 
      	// To Registers a listener object to receive notification when incoming call 
     	TelephonyManager telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
     	if(telMgr != null) 
     	{
     		telMgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
     	}         	 	
        super.onResume();
    }
	
    @Override
    protected void onPostResume() {
//    	System.out.println("DrawerActivity / _onPostResume ");
    	super.onPostResume();
    }    
    
    @Override
    protected void onResumeFragments() {
//    	System.out.println("DrawerActivity / _onResumeFragments ");
    	super.onResumeFragments();
    	selectDrawerItem(mFocusDrawerPos);
    }
    
	// for finish(), for Rotate screen
    @Override
    protected void onPause() {
        super.onPause();
// 	   System.out.println("DrawerActivity / onPause");
    }

    @Override
    protected void onStop() {
//  	    System.out.println("DrawerActivity / onStop");    
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() 
    {
//    	System.out.println("DrawerActivity / onDestroy");
    	//unregister TelephonyManager listener 
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        
		// unregister an audio stream receiver
		if(noisyAudioStreamReceiver != null)
		{
			try
			{
				unregisterReceiver(noisyAudioStreamReceiver);//??? unregister here? 
			}
			catch (Exception e)
			{
				
			}
			noisyAudioStreamReceiver = null;
		}        
        
		super.onDestroy();
    }
    
    /*
     * Listeners
     * 
     */
    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements OnItemClickListener 
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
        	System.out.println("DrawerActivity / DrawerItemClickListener");
        	mFocusDrawerPos = position;
            selectDrawerItem(position);

            Util.setPref_lastTimeView_DrawerTabsTableId(DrawerActivity.this,
            									  DB.getDrawerTabsTableId(mFocusDrawerPos) );
        }
    }

    // select drawer item
    private void selectDrawerItem(final int position) 
    {
		mDb.doOpenDrawer();
        DB.setFocus_DrawerTabsTableId((int) DB.getDrawerTabsTableId(position)); 
    	System.out.println("DrawerActivity / _selectDrawerItem / position = " + position);
		mDrawerTitle = mDb.getDrawerTitle(position);
		mDb.doClose();  

		// update selected item and title, then close the drawer
        mDrawerListView.setItemChecked(position, true);		
        mDrawerLayout.closeDrawer(mDrawerListView);    	
        setTitle(mDrawerTitle);
        
        // use Runnable to make sure only one drawer background is seen
        mHandler = new Handler();
        mHandler.post(mTabsHostRun);
    }
    
    Handler mHandler;
    static Runnable mTabsHostRun =  new Runnable() 
    {
        @Override
        public void run() 
        {
        	System.out.println("DrawerActivity / mTabsHostRun");
            Fragment mTabsHostFragment = new TabsHostFragment();
        	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, mTabsHostFragment).commit(); 
            fragmentManager.executePendingTransactions();
        } 
    };
    
    
    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemLongClickListener implements DragSortListView.OnItemLongClickListener 
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
        {
        	editDrawerItem(position);
			return true;
        }
    }
    
	void editDrawerItem(final int position)
	{
		// insert when table is empty, activated only for the first time 
		mDb.doOpenDrawer();
		final String drawerName = mDb.getDrawerTitle(position);
		mDb.doClose();

		final EditText editText = new EditText(this);
	    editText.setText(drawerName);
	    editText.setSelection(drawerName.length()); // set edit text start position
	    //update tab info
	    Builder builder = new Builder(this);
	    builder.setTitle(R.string.edit_drawer_title)
	    	.setMessage(R.string.edit_drawer_message)
	    	.setView(editText)   
	    	.setNegativeButton(R.string.btn_Cancel, new OnClickListener()
	    	{   @Override
	    		public void onClick(DialogInterface dialog, int which)
	    		{/*cancel*/}
	    	})
            .setNeutralButton(R.string.edit_page_button_delete, new OnClickListener()
            {   @Override
                public void onClick(DialogInterface dialog, int which)
            	{
            		// delete
					//warning:start
                	mPref_delete_warn = DrawerActivity.this.getSharedPreferences("delete_warn", 0);
                	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
 	                   mPref_delete_warn.getString("KEY_DELETE_DRAWER_WARN","yes").equalsIgnoreCase("yes")) 
                	{
            			Util util = new Util(DrawerActivity.this);
        				util.vibrate();
        				
                		Builder builder1 = new Builder(DrawerActivity.this); 
                		builder1.setTitle(R.string.confirm_dialog_title)
                        .setMessage(R.string.confirm_dialog_message_drawer)
                        .setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener(){
                        	@Override
                            public void onClick(DialogInterface dialog1, int which1){
                        		/*nothing to do*/}})
                        .setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener(){
                        	@Override
                            public void onClick(DialogInterface dialog1, int which1){
                        		deleteSelectedDrawer(position, DrawerActivity.this);
                        	}})
                        .show();
                	} //warning:end
                	else
                	{
                		deleteSelectedDrawer(position, DrawerActivity.this);
                	}
                	
                }
            })		    	
	    	.setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
	    	{   @Override
	    		public void onClick(DialogInterface dialog, int which)
	    		{
	    			// save
    				mDb.doOpenDrawer();
	    			int drawerId =  (int) DB.getDrawerId(position);
	    			int drawerTabInfoTableId =  DB.getDrawerTabsTableId(position);
					mDb.updateDrawer(drawerId,
							drawerTabInfoTableId,
							editText.getText().toString());
                    mDb.doClose();
                    drawerInfoAdapter.notifyDataSetChanged();
                    setTitle(editText.getText().toString());
	            }
            })	
            .setIcon(android.R.drawable.ic_menu_edit);
	        
        AlertDialog d1 = builder.create();
        d1.show();
        // android.R.id.button1 for positive: save
        ((Button)d1.findViewById(android.R.id.button1))
        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
        
        // android.R.id.button2 for negative: cancel 
        ((Button)d1.findViewById(android.R.id.button2))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        
        // android.R.id.button3 for neutral: delete
        ((Button)d1.findViewById(android.R.id.button3))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
	}
    
	/**
	 * delete drawer
	 * 
	 */
	public void deleteSelectedDrawer(int position, final Activity activity) 
	{

		// set selected drawer tabs table Id
		DB.setSelected_DrawerTabsTableId(DB.getDrawerTabsTableId(position));
		// set selected notes table Id
		DB.setSelected_NotesTableId(Util.getPref_lastTimeView_NotesTableId(this)); 
		
		// Before delete
		// renew first DrawerTabsTableId and last DrawerTabsTableId
		renewFirstAndLastDrawerId();
		
		// keep one drawer at least
		mDb.doOpenDrawer();
		int drawerCount = DB.getDrawersCount();
		mDb.doClose();
		if(drawerCount == 1)
		{
			 // show toast for only one drawer
             Toast.makeText(activity, R.string.toast_keep_one_drawer , Toast.LENGTH_SHORT).show();
             return;
		}

		// get drawer tabs table Id
		int drawerTabsTableId = DB.getDrawerTabsTableId(position);
		// get drawer Id
		mDb.doOpenByDrawerTabsTableId(drawerTabsTableId);
		int drawerId =  (int) DB.getDrawerId(position);
		
		// 1) delete related notes table
		for(int i=0;i< DB.getTabsCount();i++)
		{
			int notesTableId = DB.getTab_NotesTableId(i);
			mDb.dropNotesTable(drawerTabsTableId, notesTableId);
		}
		
		// 2) delete tabs table
		mDb.dropTabsTable(drawerTabsTableId);
		
		// 3) delete tabs info in drawer table
		mDb.deleteDrawerId(drawerId);		
		mDb.doClose();
		
		renewFirstAndLastDrawerId();

		// After Delete
        // - update mFocusDrawerPos
        // - select first existing drawer item 
		mDb.doOpenDrawer();
		
		// get new focus position
		// if focus item is deleted, set focus to new first existing drawer
        if(mFocusDrawerPos == position)
        {
	        for(int item = 0; item<DB.getDrawersCount(); item++)
	        {
	        	if(	DB.getDrawerId(item)== mFirstExist_DrawerId)
	        		mFocusDrawerPos = item; 
	        }
        }
        else if(position < mFocusDrawerPos)
       		mFocusDrawerPos--;

        // set new focus position
        mDrawerListView.setItemChecked(mFocusDrawerPos, true); 
        mDb.doClose();
        
        // update playing highlight if needed
        if(AudioPlayer.mMediaPlayer != null)
        {
           if( mCurrentPlaying_DrawerIndex > position)
        	   mCurrentPlaying_DrawerIndex--;
           else if(mCurrentPlaying_DrawerIndex == position)
           {
        	   UtilAudio.stopAudioPlayer();
        	   selectDrawerItem(mFocusDrawerPos); // select drawer to clear old playing view 
           }
        }
		
        // refresh drawer list view
        drawerInfoAdapter.notifyDataSetChanged();
	}

	// Renew first and last drawer Id
	void renewFirstAndLastDrawerId()
	{
		int i = 0;
		mDb.doOpenDrawer();
		mDrawerCursor = DB.mDrawerCursor;
		while(i < DB.getDrawersCount())
    	{
			mDrawerCursor.moveToPosition(i);
			
			if(mDrawerCursor.isFirst())
				mFirstExist_DrawerId = (int) DB.getDrawerId(i) ;
			
			if(mDrawerCursor.isLast())
				mLastExist_DrawerId = (int) DB.getDrawerId(i) ;
			
			i++;
    	} 
		mDb.doClose();	
//		System.out.println("mFirstExist_DrawerId = " + mFirstExist_DrawerId);
//		System.out.println("mLastExist_DrawerId = " + mLastExist_DrawerId);
	}
	
    @Override
    public void setTitle(CharSequence title) {
    	super.setTitle(title);
        if(title == null)
        {
        	title = mDrawerTitle;
        	fragmentManager.popBackStack();
        	initActionBar();
            mDrawerLayout.closeDrawer(mDrawerListView);
        }
        getActionBar().setTitle(title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        System.out.println("DrawerActivity / onPostCreate");
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        System.out.println("DrawerActivity / onConfigurationChanged");
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        System.out.println("DrawerActivity / onPrepareOptionsMenu");
        // If the navigation drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListView);
        if(drawerOpen)
        {
        	mMenu.setGroupVisible(0, false); 
    		mMenu.setGroupVisible(1, true); 
        }
        else
        {
            setTitle(mDrawerTitle);
    		mMenu.setGroupVisible(1, false);             
        }
        return super.onPrepareOptionsMenu(menu);
    }

	private static class ViewHolder
	{
		TextView drawerTitle; // refers to ListView item's ImageView
	}
    
	class DrawerInfoAdapter extends SimpleDragSortCursorAdapter
	{
		public DrawerInfoAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) 
		{
			super(context, layout, c, from, to, flags);
		}

		@Override
		public int getCount() {
			mDb.doOpenDrawer();
			int count = DB.getDrawersCount();
			mDb.doClose();
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
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder viewHolder; // holds references to current item's GUI
         
			// if convertView is null, inflate GUI and create ViewHolder;
			// otherwise, get existing ViewHolder
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(R.layout.drawer_list_item, parent, false);
				
				// set up ViewHolder for this ListView item
				viewHolder = new ViewHolder();
				viewHolder.drawerTitle = (TextView) convertView.findViewById(R.id.drawerText);
				convertView.setTag(viewHolder); // store as View's tag
			}
			else // get the ViewHolder from the convertView's tag
				viewHolder = (ViewHolder) convertView.getTag();

			// set highlight of selected drawer
            if((AudioPlayer.mMediaPlayer != null) &&
           		(mCurrentPlaying_DrawerIndex == position) )
            	viewHolder.drawerTitle.setTextColor(Color.argb(0xff, 0xff, 0x80, 0x00));
            else
            	viewHolder.drawerTitle.setTextColor(Color.argb(0xff, 0xff, 0xff, 0xff));
			
			
			mDb.doOpenDrawer();
			viewHolder.drawerTitle.setText(mDb.getDrawerTitle(position));
  		    mDb.doClose();

			return convertView;
		}
	}
    
    
	/******************************************************
	 * Menu
	 * 
	 */
    // Menu identifiers
	private static SharedPreferences mPref_show_note_attribute;
	static final int ADD_NEW_FOLDER = R.id.ADD_NEW_FOLDER;
	
    static final int ADD_TEXT = R.id.ADD_TEXT;
    static final int ADD_CAMERA_PICTURE = R.id.ADD_NEW_IMAGE;
    static final int ADD_READY_PICTURE = R.id.ADD_OLD_PICTURE;
    static final int ADD_CAMERA_VIDEO = R.id.ADD_NEW_VIDEO;
    static final int ADD_READY_VIDEO = R.id.ADD_OLD_VIDEO;
    static final int ADD_AUDIO = R.id.ADD_AUDIO;
    static final int OPEN_PLAY_SUBMENU = R.id.PLAY;
    static final int PLAY_OR_STOP_AUDIO = R.id.PLAY_OR_STOP_MUSIC;
//    static final int STOP_AUDIO = R.id.STOP_MUSIC;
    static final int SLIDE_SHOW = R.id.SLIDE_SHOW;
    
    static final int CHECK_ALL = R.id.CHECK_ALL;
    static final int UNCHECK_ALL = R.id.UNCHECK_ALL;
    static final int INVERT_SELECTED = R.id.INVERT_SELECTED;
    static final int MOVE_CHECKED_NOTE = R.id.MOVE_CHECKED_NOTE;
    static final int COPY_CHECKED_NOTE = R.id.COPY_CHECKED_NOTE;
    static final int MAIL_CHECKED_NOTE = R.id.MAIL_CHECKED_NOTE;
    static final int DELETE_CHECKED_NOTE = R.id.DELETE_CHECKED_NOTE;
    static final int ADD_NEW_PAGE = R.id.ADD_NEW_PAGE;
    static final int CHANGE_PAGE_COLOR = R.id.CHANGE_PAGE_COLOR;
    static final int SHIFT_PAGE = R.id.SHIFT_PAGE;
    static final int SHOW_BODY = R.id.SHOW_BODY;
    static final int ENABLE_DRAGGABLE = R.id.ENABLE_DND;
    static final int SEND_PAGES = R.id.SEND_PAGES;
    static final int GALLERY = R.id.GALLERY;
	static final int CONFIG_PREFERENCE = R.id.CONFIG_PREF;    
	
	/*
	 * onCreate Options Menu
	 */
	public static MenuItem mSubMenuItemAudio;
	MenuItem playOrStopMusicButton;
//	MenuItem stopMusicButton;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
//		System.out.println("DrawerActivity / onCreateOptionsMenu");
		mMenu = menu;
		//
		// set sub menu 0: add new note
		//
	    SubMenu subMenu0 = menu.addSubMenu(0, 0, 0, R.string.add_new_note);//order starts from 0
	    
	    // add item
	    subMenu0.add(0, ADD_TEXT, 1, R.string.note_text)
        		.setIcon(android.R.drawable.ic_menu_edit);
	    
	    // check camera feature
	    PackageManager packageManager = this.getPackageManager();
	    if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) 
	    {
	    	subMenu0.add(0, ADD_CAMERA_PICTURE, 2, R.string.note_camera_image)
					.setIcon(android.R.drawable.ic_menu_camera);
	    	subMenu0.add(0, ADD_CAMERA_VIDEO, 3,  R.string.note_camera_video)
					.setIcon(android.R.drawable.presence_video_online); //??? with better icon?
	    }
	    
	    subMenu0.add(0, ADD_READY_PICTURE, 3, R.string.note_ready_image)
		.setIcon(android.R.drawable.ic_menu_gallery);	    
	    subMenu0.add(0, ADD_READY_VIDEO, 4, R.string.note_ready_video)
		.setIcon(android.R.drawable.presence_video_online);	//??? with better icon?    
	    subMenu0.add(0, ADD_AUDIO, 5, R.string.note_audio)
        		.setIcon(R.drawable.ic_lock_ringer_on);
	    

	    SubMenu subMenuDrawer = menu.addSubMenu(1, 0, 0, R.string.options);//order starts from 0
	    
	    // add item
	    subMenuDrawer.add(0, ADD_NEW_FOLDER, 1, R.string.add_new_drawer)
        		.setIcon(android.R.drawable.ic_menu_add);
	    MenuItem subMenuItemDrawer = subMenuDrawer.getItem();
	    subMenuItemDrawer.setIcon(R.drawable.ic_menu_moreoverflow);
	    // set sub menu display
	    subMenuItemDrawer.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                    	  MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	    
	    // icon
	    MenuItem subMenuItem0 = subMenu0.getItem();
	    subMenuItem0.setIcon(R.drawable.ic_input_add);
		
	    // set sub menu display
		subMenuItem0.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);	 
		
		//
		// set sub menu 1: Play music & slide show
		//
	    SubMenu subMenu1 = menu.addSubMenu(0, OPEN_PLAY_SUBMENU, 1, R.string.menu_button_play);//order starts from 0
	    
	    // add item
	    subMenu1.add(0, PLAY_OR_STOP_AUDIO, 1, R.string.menu_button_play_audio)
   				.setIcon(R.drawable.ic_media_play);	    	
	    playOrStopMusicButton = subMenu1.getItem(0);
		  
	    subMenu1.add(0, SLIDE_SHOW, 3, R.string.menu_button_slide_show)
				.setIcon(R.drawable.ic_menu_play_clip);
	    
	    mSubMenuItemAudio = subMenu1.getItem();
		mSubMenuItemAudio.setIcon(R.drawable.ic_menu_slideshow);
		
	    // set sub menu display
		mSubMenuItemAudio.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                     MenuItem.SHOW_AS_ACTION_WITH_TEXT);	 
		
	    //
		// set sub menu 2: handle checked note
	    //
	    SubMenu subMenu2 = menu.addSubMenu(0, 0, 2, R.string.checked_notes);//order starts from 0
	    
	    // add item
	    subMenu2.add(0, CHECK_ALL, 1, R.string.checked_notes_check_all)
        		.setIcon(R.drawable.btn_check_on_holo_dark);
	    subMenu2.add(0, UNCHECK_ALL, 2, R.string.checked_notes_uncheck_all)
				.setIcon(R.drawable.btn_check_off_holo_dark);
	    subMenu2.add(0, INVERT_SELECTED, 3, R.string.checked_notes_invert_selected)
				.setIcon(R.drawable.btn_check_on_focused_holo_dark);
	    subMenu2.add(0, MOVE_CHECKED_NOTE, 4, R.string.checked_notes_move_to)
        		.setIcon(R.drawable.ic_menu_goto);	    
	    subMenu2.add(0, COPY_CHECKED_NOTE, 5, R.string.checked_notes_copy_to)
        		.setIcon(R.drawable.ic_menu_copy_holo_dark);
	    subMenu2.add(0, MAIL_CHECKED_NOTE, 6, R.string.mail_notes_btn)
        		.setIcon(android.R.drawable.ic_menu_send);
	    subMenu2.add(0, DELETE_CHECKED_NOTE, 7, R.string.checked_notes_delete)
        		.setIcon(R.drawable.ic_menu_clear_playlist);
	    // icon
	    MenuItem subMenuItem2 = subMenu2.getItem();
	    subMenuItem2.setIcon(R.drawable.ic_menu_mark);
	    
	    // set sub menu display
		subMenuItem2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		
		//
		// set sub menu 3: overflow
		//
	    SubMenu subMenu3 = menu.addSubMenu(0, 0, 3, R.string.options);//order starts from 0
	    // add item
	    subMenu3.add(0, ADD_NEW_PAGE, 1, R.string.add_new_page)
	            .setIcon(R.drawable.ic_menu_add_new_page);
	    
	    subMenu3.add(0, CHANGE_PAGE_COLOR, 2, R.string.change_page_color)
        	    .setIcon(R.drawable.ic_color_a);
	    
	    subMenu3.add(0, SHIFT_PAGE, 3, R.string.rearrange_page)
	            .setIcon(R.drawable.ic_dragger_h);
    	
	    // show body
	    mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);
    	if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
    		subMenu3.add(0, SHOW_BODY, 4, R.string.preview_note_body_no)
     	   		    .setIcon(R.drawable.ic_media_group_collapse);
    	else
    		subMenu3.add(0, SHOW_BODY, 4, R.string.preview_note_body_yes)
        	        .setIcon(R.drawable.ic_media_group_expand);
    	
    	// show draggable
	    mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);
    	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
    		subMenu3.add(0, ENABLE_DRAGGABLE, 5, getResources().getText(R.string.draggable_no))
		    				.setIcon(R.drawable.ic_dragger_off);
    	else
    		subMenu3.add(0, ENABLE_DRAGGABLE, 5, getResources().getText(R.string.draggable_yes))
    						.setIcon(R.drawable.ic_dragger_on);
    	
	    subMenu3.add(0, SEND_PAGES, 6, R.string.mail_notes_title)
 	   			.setIcon(android.R.drawable.ic_menu_send);

	    subMenu3.add(0, GALLERY, 7, R.string.gallery)
				.setIcon(android.R.drawable.ic_menu_gallery);	    
	    
	    subMenu3.add(0, CONFIG_PREFERENCE, 8, R.string.settings)
	    	   .setIcon(R.drawable.ic_menu_preferences);
	    
	    // set icon
	    MenuItem subMenuItem3 = subMenu3.getItem();
	    subMenuItem3.setIcon(R.drawable.ic_menu_moreoverflow);
	    
	    // set sub menu display
		subMenuItem3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}
	
	// set activity Enabled/Disabled
//	public static void setActivityEnabled(Context context,final Class<? extends Activity> activityClass,final boolean enable)
//    {
//	    final PackageManager pm=context.getPackageManager();
//	    final int enableFlag=enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
//	    pm.setComponentEnabledSetting(new ComponentName(context,activityClass),enableFlag,PackageManager.DONT_KILL_APP);
//    }
	
	/*
	 * on options item selected
	 * 
	 */
	public static SlideshowInfo slideshowInfo;
	static FragmentTransaction mFragmentTransaction;
	public static int mCurrentPlaying_NotesTblId;
	public static int mCurrentPlaying_TabIndex;
	public static int mCurrentPlaying_DrawerIndex;
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
		// Go back: check if Configure fragment now
    	if( (item.getItemId() == android.R.id.home) && bEnableConfig)
    	{
    		System.out.println("DrawerActivity / onOptionsItemSelected / Home key of Config is pressed ");
    		fragmentManager.popBackStack();
    		initActionBar();
			setTitle(mDrawerTitle);
	        mDrawerLayout.closeDrawer(mDrawerListView);
            return true;
    	}
    	
    	// The action bar home/up action should open or close the drawer.
    	// ActionBarDrawerToggle will take care of this.
    	if (mDrawerToggle.onOptionsItemSelected(item))
    	{
    		System.out.println("mDrawerToggle.onOptionsItemSelected(item) / ActionBarDrawerToggle");
    		return true;
    	}
    	
    	final Intent intent;
        switch (item.getItemId()) 
        {
	    	case ADD_NEW_FOLDER:
	    		renewFirstAndLastDrawerId();
	    		addNewFolder(mLastExist_DrawerId+1);
				return true;
			
        	case ADD_TEXT:
				intent = new Intent(this, Note_addNewText.class);
				new Note_addNew_optional(this, intent);
				return true;

        	case ADD_CAMERA_PICTURE:
//        		setActivityEnabled(this,Note_addCameraPicture.class,true);
				intent = new Intent(this, Note_addCameraImage.class);
				new Note_addNew_optional(this, intent);
	            return true;

        	case ADD_CAMERA_VIDEO:
				intent = new Intent(this, Note_addCameraVideo.class);
				new Note_addNew_optional(this, intent);
	            return true;	            
	            
        	case ADD_READY_PICTURE:
				intent = new Intent(this, Note_addReadyImage.class); 
				new Note_addNew_optional_for_multiple(this, intent);
				return true;

        	case ADD_READY_VIDEO:
				intent = new Intent(this, Note_addReadyVideo.class); 
				new Note_addNew_optional_for_multiple(this, intent);
				return true;
				
        	case ADD_AUDIO:
				intent = new Intent(this, Note_addAudio.class); 
				new Note_addNew_optional_for_multiple(this, intent);
				return true;
        	
        	case OPEN_PLAY_SUBMENU:
        		// new play instance: stop button is off
        	    if( (AudioPlayer.mMediaPlayer != null) && 
        	    	(AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP))
        		{
       		    	// show Stop
           			playOrStopMusicButton.setTitle(R.string.menu_button_stop_audio);
           			playOrStopMusicButton.setIcon(R.drawable.ic_media_stop);
        	    }
        	    else
        	    {
       		    	// show Play
           			playOrStopMusicButton.setTitle(R.string.menu_button_play_audio);
           			playOrStopMusicButton.setIcon(R.drawable.ic_media_play);        	    	
        	    }
        		return true;
        	
        	case PLAY_OR_STOP_AUDIO:
        		if( (AudioPlayer.mMediaPlayer != null) &&
        			(AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP))
        		{
					UtilAudio.stopAudioPlayer();
					TabsHostFragment.setPlayingTab_WithHighlight(false);
					NoteFragment.mItemAdapter.notifyDataSetChanged();
					NoteFragment.setFooter();
					return true; // just stop playing, wait for user action
        		}
        		else
        		{
        			AudioPlayer.mPlayMode = AudioPlayer.CONTINUE_MODE;

        			AudioPlayer.mAudioIndex = 0;
	        		UtilAudio.startAudioPlayer(this);
	        		NoteFragment.setFooter();
	        		
					// update notes table Id
					mCurrentPlaying_NotesTblId = TabsHostFragment.mCurrentNotesTableId;
					// update playing tab index
					mCurrentPlaying_TabIndex = TabsHostFragment.mCurrentTabIndex;
					// update playing drawer index
				    mCurrentPlaying_DrawerIndex = mFocusDrawerPos;	        		
        		}
        		return true;

        	case SLIDE_SHOW:
        		slideshowInfo = new SlideshowInfo();
        		// add images for slide show
        		mDb = new DB(this);
        		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
        		for(int i=0;i< mDb.getNotesCount() ;i++)
        		{
        			if(mDb.getNoteMarking(i) == 1)
        			{
        				String pictureUri = mDb.getNotePictureUri(i);
        				if((pictureUri.length() > 0) && UtilImage.hasImageExtension(pictureUri)) // skip empty
        					slideshowInfo.addImage(pictureUri);
        			}
        		}
        		mDb.doClose();
        		          		
        		if(slideshowInfo.imageSize() > 0)
        		{
					// create new Intent to launch the slideShow player Activity
					Intent playSlideshow = new Intent(this, SlideshowPlayer.class);
					startActivity(playSlideshow);  
        		}
        		else
        			Toast.makeText(mContext,R.string.file_not_found,Toast.LENGTH_SHORT).show();
        		return true;
				
            case ADD_NEW_PAGE:
                addNewPage(TabsHostFragment.mLastExist_TabId + 1);
                return true;
                
            case CHANGE_PAGE_COLOR:
            	changePageColor();
                return true;    
                
            case SHIFT_PAGE:
            	shiftPage();
                return true;  
                
            case SHOW_BODY:
            	mPref_show_note_attribute = mContext.getSharedPreferences("show_note_attribute", 0);
            	if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
            		mPref_show_note_attribute.edit().putString("KEY_SHOW_BODY","no").commit();
            	else
            		mPref_show_note_attribute.edit().putString("KEY_SHOW_BODY","yes").commit();
            	TabsHostFragment.updateChange(this);
                return true; 

            case ENABLE_DRAGGABLE:
            	mPref_show_note_attribute = mContext.getSharedPreferences("show_note_attribute", 0);
            	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
            		mPref_show_note_attribute.edit().putString("KEY_ENABLE_DRAGGABLE","no").commit();
            	else
            		mPref_show_note_attribute.edit().putString("KEY_ENABLE_DRAGGABLE","yes").commit();
            	TabsHostFragment.updateChange(this);
                return true;                 
                
            case SEND_PAGES:
				Intent intentSend = new Intent(this, SendMailAct.class);
				startActivity(intentSend);
				TabsHostFragment.updateChange(this);
            	return true;

            case GALLERY:
				Intent i_browsePic = new Intent(this, GalleryGridAct.class);
				i_browsePic.putExtra("gallery", true);
				startActivity(i_browsePic);
            	return true; 	

            case CONFIG_PREFERENCE:
            	mMenu.setGroupVisible(0, false); //hide the menu
        		setTitle(R.string.settings);
        		bEnableConfig = true;
        		
            	mConfigFragment = new Config();
            	mFragmentTransaction = fragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.content_frame, mConfigFragment).addToBackStack("config").commit();
                return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
    /*
     *  on Back button pressed
     *
     */
    @Override
    public void onBackPressed()
    {
        System.out.println("DrawerActivity / _onBackPressed");
        if(!bEnableConfig)
        {
        	super.onBackPressed();
            // stop audio player
            UtilAudio.stopAudioPlayer();        	
        }
        else
        {
        	fragmentManager.popBackStack();
        	initActionBar();
			setTitle(mDrawerTitle);
	        mDrawerLayout.closeDrawer(mDrawerListView);
		}
    }
    
    void initActionBar()
    {
		mConfigFragment = null;  
		bEnableConfig = false;
		mMenu.setGroupVisible(0, true);
		getActionBar().setDisplayShowHomeEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mDrawerToggle.setDrawerIndicatorEnabled(true); 
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		// Note: No need to keep AudioPlayer.audioIndex for NOT one-time-mode
//		//http://blog.shamanland.com/2014/01/nested-fragments-for-result.html
//        if( (requestCode & 0xffff /*to strip off the fragment index*/) 
//        	== Util.ACTIVITY_VIEW_NOTE ) 
//        {
//        	if (resultCode == Activity.RESULT_OK)
//        		AudioPlayer.audioIndex =  data.getIntExtra("audioIndexBack", AudioPlayer.audioIndex);
//        }  
	}	
	
    /**
     * Add new folder
     * 
     */
	public  void addNewFolder(final int newTableId) {
		// get tab name
		String tabName = "D".concat(String.valueOf(newTableId));
        
        final EditText editText1 = new EditText(getBaseContext());
        editText1.setText(tabName);
        editText1.setSelection(tabName.length()); // set edit text start position
        
        //update tab info
        Builder builder = new Builder(DrawerActivity.this);
        builder.setTitle(R.string.edit_drawer_title)
                .setMessage(R.string.edit_drawer_message)
                .setView(editText1)   
                .setNegativeButton(R.string.edit_page_button_ignore, new OnClickListener(){   
                	@Override
                    public void onClick(DialogInterface dialog, int which)
                    {/*nothing*/}
                })
                .setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
                {   @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                		
	    	            final String[] items = new String[]{
	    	            		getResources().getText(R.string.add_new_note_top).toString(),
	    	            		getResources().getText(R.string.add_new_note_bottom).toString() };
	    	            
						AlertDialog.Builder builder = new AlertDialog.Builder(DrawerActivity.this);
						  
						builder.setTitle(R.string.add_new_page_select_position)
						.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {
						

								
				        	String drawerTitle =  editText1.getText().toString();
				        	mDrawerTitles.add(drawerTitle);
				    		mDb.doOpenDrawer();
				    		// insert new drawer Id and Title
				        	mDb.insertDrawer(newTableId, drawerTitle ); 
				        	DB.insertTabsTable(DB.mSqlDb,newTableId);
				        	
				    		// insert notes table
				    		for(int i=1;i<= DB.DEFAULT_NOTES_TABLE_COUNT;i++)
				    			DB.insertNotesTable(DB.mSqlDb,newTableId, i);
				    		
				    		mDb.doClose();
				    		
				    		// add new drawer to the top
							if(which == 0)
							{
						        mDb.doOpenDrawer();
						        int startCursor = DB.getDrawersCount()-1;
						        mDb.doClose();
						        int endCursor = 0;
								
								//reorder data base storage for ADD_NEW_TO_TOP option
								int loop = Math.abs(startCursor-endCursor);
								for(int i=0;i< loop;i++)
								{
									swapDrawerRows(startCursor,endCursor);
									if((startCursor-endCursor) >0)
										endCursor++;
									else
										endCursor--;
								}
								
								// update playing highlight if needed
								if(AudioPlayer.mMediaPlayer != null) 
									mCurrentPlaying_DrawerIndex++;
							}				    		
				    		
				    		drawerInfoAdapter.notifyDataSetChanged();
				    		
				        	//end
							dialog.dismiss();
							
							updateFocusPosition();
						}})
						.setNegativeButton(R.string.btn_Cancel, null)
						.show();
                    }
                })	 
                .setIcon(android.R.drawable.ic_menu_edit);
        
	        final AlertDialog d = builder.create();
	        d.show();
	        // android.R.id.button1 for negative: cancel 
	        ((Button)d.findViewById(android.R.id.button1))
	        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
	        // android.R.id.button2 for positive: save
	        ((Button)d.findViewById(android.R.id.button2))
	        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
	}	
    
    /**
     * Add new page
     * 
     */
	public  void addNewPage(final int newTabId) {
		// get tab name
		String tabName = "N".concat(String.valueOf(newTabId));
        
        final EditText editText1 = new EditText(getBaseContext());
        editText1.setText(tabName);
        editText1.setSelection(tabName.length()); // set edit text start position
        
        //update tab info
        Builder builder = new Builder(DrawerActivity.this);
        builder.setTitle(R.string.edit_page_tab_title)
                .setMessage(R.string.edit_page_tab_message)
                .setView(editText1)   
                .setNegativeButton(R.string.edit_page_button_ignore, new OnClickListener(){   
                	@Override
                    public void onClick(DialogInterface dialog, int which)
                    {/*nothing*/}
                })
                .setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
                {   @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                		
	    	            final String[] items = new String[]{
	    	            		getResources().getText(R.string.add_new_page_leftmost).toString(),
	    	            		getResources().getText(R.string.add_new_page_rightmost).toString() };
	    	            
						AlertDialog.Builder builder = new AlertDialog.Builder(DrawerActivity.this);
						  
						builder.setTitle(R.string.add_new_page_select_position)
						.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {
						
							if(which ==0)
								insertTabLeftmost(newTabId, editText1.getText().toString());
							else
								insertTabRightmost(newTabId, editText1.getText().toString());
							//end
							dialog.dismiss();
						}})
						.setNegativeButton(R.string.btn_Cancel, null)
						.show();
                    }
                })	 
                .setIcon(android.R.drawable.ic_menu_edit);
        
	        final AlertDialog d = builder.create();
	        d.show();
	        // android.R.id.button1 for negative: cancel 
	        ((Button)d.findViewById(android.R.id.button1))
	        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
	        // android.R.id.button2 for positive: save
	        ((Button)d.findViewById(android.R.id.button2))
	        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
	}
	
	/* 
	 * Insert Tab to Leftmost
	 * 
	 */
	void insertTabLeftmost(int newTabId,String tabName)
	{
 	    // insert tab name
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		int style = Util.getNewPageStyle(mContext);
		DB.insertTab(DB.getFocusTabsTableName(),tabName, newTabId,tabName, newTabId,style );
		
		// insert table for new tab
		DB.insertNotesTable(DB.mSqlDb,DB.getFocus_DrawerTabsTableId(),newTabId);
		TabsHostFragment.mTabCount++;
		mDb.doClose();
		
		//change to leftmost tab Id
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		int tabTotalCount = DB.getTabsCount();
		mDb.doClose();
		for(int i=0;i <(tabTotalCount-1);i++)
		{
			int tabIndex = tabTotalCount -1 -i ;
			swapTabInfo(tabIndex,tabIndex-1);
			updateFinalPageViewed();
		}
		
        // set scroll X
		final int scrollX = 0; // leftmost
		
		// commit: scroll X
		TabsHostFragment.updateChange(this);
    	
		TabsHostFragment.mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
	        	TabsHostFragment.mHorScrollView.scrollTo(scrollX, 0);
	        	Util.setPref_lastTimeView_scrollX_byDrawerNumber(DrawerActivity.this, scrollX );
	        } 
	    });
		
		// update highlight tab
		if(mCurrentPlaying_DrawerIndex == mFocusDrawerPos)
			mCurrentPlaying_TabIndex++;
	}
	
	/*
	 * Update Final page which was viewed last time
	 * 
	 */
	protected void updateFinalPageViewed()
	{
        // get final viewed table Id
        String tblId = Util.getPref_lastTimeView_NotesTableId(this);
		Context context = getApplicationContext();

		DB.setFocus_NotesTableId(tblId);
		mDb = new DB(context);
		
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		// get final view tab index of last time
		for(int i =0;i<DB.getTabsCount();i++)
		{
			if(Integer.valueOf(tblId) == DB.getTab_NotesTableId(i))
				TabsHostFragment.mFinalPageViewed_TabIndex = i;	// starts from 0
			
        	if(	mDb.getTabId(i)== TabsHostFragment.mFirstExist_TabId)
        		Util.setPref_lastTimeView_NotesTableId(this, DB.getTab_NotesTableId(i) );
		}
		mDb.doClose();
	}
	
	/*
	 * Insert Tab to Rightmost
	 * 
	 */
	void insertTabRightmost(int newTblId,String tabName)
	{
 	    // insert tab name
		mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
		int style = Util.getNewPageStyle(mContext);
		DB.insertTab(DB.getFocusTabsTableName(),tabName,newTblId,tabName,newTblId,style );
		
		// insert table for new tab
		DB.insertNotesTable(DB.mSqlDb,DB.getFocus_DrawerTabsTableId(),newTblId);
		TabsHostFragment.mTabCount++;
		mDb.doClose();
		
		// commit: final page viewed
		Util.setPref_lastTimeView_NotesTableId(this, newTblId);
		
        // set scroll X
		final int scrollX = (TabsHostFragment.mTabCount) * 60 * 5; //over the last scroll X
		
		TabsHostFragment.updateChange(this);
    	
		TabsHostFragment.mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
	        	TabsHostFragment.mHorScrollView.scrollTo(scrollX, 0);
	        	Util.setPref_lastTimeView_scrollX_byDrawerNumber(DrawerActivity.this, scrollX );
	        } 
	    });
	}
	
	/*
	 * Change Page Color
	 * 
	 */
	void changePageColor()
	{
		// set color
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.edit_page_color_title)
	    	   .setPositiveButton(R.string.edit_page_button_ignore, new OnClickListener(){   
	            	@Override
	                public void onClick(DialogInterface dialog, int which)
	                {/*cancel*/}
	            	});
		// inflate select style layout
		LayoutInflater mInflator= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflator.inflate(R.layout.select_style, null);
		RadioGroup RG_view = (RadioGroup)view.findViewById(R.id.radioGroup1);
		
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio0),0);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio1),1);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio2),2);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio3),3);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio4),4);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio5),5);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio6),6);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio7),7);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio8),8);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio9),9);
		
		// set current selection
		for(int i=0;i< Util.getStyleCount();i++)
		{
			if(Util.getCurrentPageStyle(this) == i)
			{
				RadioButton buttton = (RadioButton) RG_view.getChildAt(i);
		    	if(i%2 == 0)
		    		buttton.setButtonDrawable(R.drawable.btn_radio_on_holo_dark);
		    	else
		    		buttton.setButtonDrawable(R.drawable.btn_radio_on_holo_light);		    		
			}
		}
		
		builder.setView(view);
		
		RadioGroup radioGroup = (RadioGroup) RG_view.findViewById(R.id.radioGroup1);
		    
		final AlertDialog dlg = builder.create();
	    dlg.show();
	    
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup RG, int id) {
				TabsHostFragment.mStyle = RG.indexOfChild(RG.findViewById(id));
				mDb = new DB(DrawerActivity.this);
				mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
				mDb.updateTab(mDb.getTabId(TabsHostFragment.mCurrentTabIndex),
	 							  DB.getTabTitle(TabsHostFragment.mCurrentTabIndex),
	 							  DB.getTab_NotesTableId(TabsHostFragment.mCurrentTabIndex),
	 							  TabsHostFragment.mStyle );
				mDb.doClose();
	 			dlg.dismiss();
	 			TabsHostFragment.updateChange(DrawerActivity.this);
		}});
	}

	
	
    /**
     * shift page right or left
     * 
     */
    void shiftPage()
    {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.rearrange_page_title)
          	   .setMessage(null)
               .setNegativeButton(R.string.rearrange_page_left, null)
               .setNeutralButton(R.string.edit_note_button_back, null)
               .setPositiveButton(R.string.rearrange_page_right,null)
               .setIcon(R.drawable.ic_dragger_h);
        final AlertDialog d = builder.create();
        
        // disable dim background 
    	d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    	d.show();
    	
    	
    	final int dividerWidth = getResources().getDrawable(R.drawable.ic_tab_divider).getMinimumWidth();
    	
    	// To left
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
        {  @Override
           public void onClick(View v)
           {
        		//change to OK
        		Button mButton=(Button)d.findViewById(android.R.id.button3);
    	        mButton.setText(R.string.btn_Finish);
    	        mButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_finish , 0, 0, 0);

    	        int[] leftMargin = {0,0};
    	        if(TabsHostFragment.mCurrentTabIndex == 0)
    	        	TabsHostFragment.mTabHost.getTabWidget().getChildAt(0).getLocationInWindow(leftMargin);
    	        else
    	        	TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrentTabIndex-1).getLocationInWindow(leftMargin);

    			int curTabWidth,nextTabWidth;
    			curTabWidth = TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrentTabIndex).getWidth();
    			if(TabsHostFragment.mCurrentTabIndex == 0)
    				nextTabWidth = curTabWidth;
    			else
    				nextTabWidth = TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrentTabIndex-1).getWidth(); 

    			// when leftmost tab margin over window border
           		if(leftMargin[0] < 0) 
           			TabsHostFragment.mHorScrollView.scrollBy(- (nextTabWidth + dividerWidth) , 0);
				
        		d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        	    if(TabsHostFragment.mCurrentTabIndex == 0)
        	    {
        	    	Toast.makeText(TabsHostFragment.mTabHost.getContext(), R.string.toast_leftmost ,Toast.LENGTH_SHORT).show();
        	    	d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);//avoid long time toast
        	    }
        	    else
        	    {
        	    	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
        	    	Util.setPref_lastTimeView_NotesTableId(DrawerActivity.this, DB.getTab_NotesTableId(TabsHostFragment.mCurrentTabIndex));
        	    	mDb.doClose();
					swapTabInfo(TabsHostFragment.mCurrentTabIndex,TabsHostFragment.mCurrentTabIndex-1);
					
					// shift left when audio playing
					// target is playing index 
					if(TabsHostFragment.mCurrentTabIndex == mCurrentPlaying_TabIndex)
						mCurrentPlaying_TabIndex--;
					// target is at right side of playing index
					else if((TabsHostFragment.mCurrentTabIndex - mCurrentPlaying_TabIndex)== 1 )
						mCurrentPlaying_TabIndex++;
					
					TabsHostFragment.updateChange(DrawerActivity.this);
        	    }
           }
        });
        
        // done
        d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
        {   @Override
           public void onClick(View v)
           {
               d.dismiss();
           }
        });
        
        // To right
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {  @Override
           public void onClick(View v)
           {
        		d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        		
        		// middle button text: change to OK
	    		Button mButton=(Button)d.findViewById(android.R.id.button3);
		        mButton.setText(R.string.btn_Finish);
		        mButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_finish , 0, 0, 0);
   	    		
		        mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
   	    		int count = DB.getTabsCount();
   	    		mDb.doClose();
                
    			int[] rightMargin = {0,0};
    			if(TabsHostFragment.mCurrentTabIndex == (count-1))
    				TabsHostFragment.mTabHost.getTabWidget().getChildAt(count-1).getLocationInWindow(rightMargin);
    			else
    				TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrentTabIndex+1).getLocationInWindow(rightMargin);

    			int curTabWidth, nextTabWidth;
    			curTabWidth = TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrentTabIndex).getWidth();
    			if(TabsHostFragment.mCurrentTabIndex == (count-1))
    				nextTabWidth = curTabWidth;
    			else
    				nextTabWidth = TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrentTabIndex+1).getWidth();
    			
	    		// when rightmost tab margin plus its tab width over screen border 
    			int screenWidth = UtilImage.getScreenWidth(DrawerActivity.this);
	    		if( screenWidth <= rightMargin[0] + nextTabWidth )
	    			TabsHostFragment.mHorScrollView.scrollBy(nextTabWidth + dividerWidth, 0);	
				
	    		d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
   	    		
       	    	if(TabsHostFragment.mCurrentTabIndex == (count-1))
       	    	{
       	    		// end of the right side
       	    		Toast.makeText(TabsHostFragment.mTabHost.getContext(),R.string.toast_rightmost,Toast.LENGTH_SHORT).show();
       	    		d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);//avoid long time toast
       	    	}
       	    	else
       	    	{
        	    	Util.setPref_lastTimeView_NotesTableId(DrawerActivity.this, DB.getTab_NotesTableId(TabsHostFragment.mCurrentTabIndex));
					swapTabInfo(TabsHostFragment.mCurrentTabIndex,TabsHostFragment.mCurrentTabIndex+1);
					
					// shift right when audio playing
					// target is playing index
					if(TabsHostFragment.mCurrentTabIndex == mCurrentPlaying_TabIndex)
						mCurrentPlaying_TabIndex++;
					// target is at left side of plying index
					else if((mCurrentPlaying_TabIndex - TabsHostFragment.mCurrentTabIndex)== 1 )
						mCurrentPlaying_TabIndex--;
						
					TabsHostFragment.updateChange(DrawerActivity.this);
       	    	}
           }
        });
        
        // android.R.id.button1 for positive: next 
        ((Button)d.findViewById(android.R.id.button1))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_forward, 0, 0, 0);
        // android.R.id.button2 for negative: previous
        ((Button)d.findViewById(android.R.id.button2))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
        // android.R.id.button3 for neutral: cancel
        ((Button)d.findViewById(android.R.id.button3))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
    }
    
    /**
     * swap tab info
     * 
     */
    void swapTabInfo(int start, int end)
    {
    	mDb.doOpenByDrawerTabsTableId(DB.getFocus_DrawerTabsTableId());
    	mDb.updateTab(mDb.getTabId(end),
        		DB.getTabTitle(start),
        		DB.getTab_NotesTableId(start),
        		mDb.getTabStyle(start));		        
		
        mDb.updateTab(mDb.getTabId(start),
				DB.getTabTitle(end),
				DB.getTab_NotesTableId(end),
				mDb.getTabStyle(end));
		mDb.doClose();
    }
    
    boolean mIsCalledWhilePlayingAudio;
    // for Pause audio player when incoming call
    // http://stackoverflow.com/questions/5610464/stopping-starting-music-on-incoming-calls
    PhoneStateListener phoneStateListener = new PhoneStateListener() 
    {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) 
        {
            if ( (state == TelephonyManager.CALL_STATE_RINGING) || 
                 (state == TelephonyManager.CALL_STATE_OFFHOOK )   ) 
            {
                //Incoming call or Call out: Pause music
            	System.out.println("Incoming call:");
            	if(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PLAY)
            	{
            		UtilAudio.startAudioPlayer(DrawerActivity.this);
            		mIsCalledWhilePlayingAudio = true;
            	}
            } 
            else if(state == TelephonyManager.CALL_STATE_IDLE) 
            {
                //Not in call: Play music
            	System.out.println("Not in call:");
            	if( (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PAUSE) && 
            		mIsCalledWhilePlayingAudio )	
            	{
            		UtilAudio.startAudioPlayer(DrawerActivity.this); // pause => play
            		mIsCalledWhilePlayingAudio = false;
            	}
            } 
            else if(state == TelephonyManager.CALL_STATE_OFFHOOK) 
            {
                //A call is dialing, active or on hold
            	System.out.println("A call is dialing, active or on hold:");
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };
    
    
	private class NoisyAudioStreamReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
				if((AudioPlayer.mMediaPlayer != null) &&
				    AudioPlayer.mMediaPlayer.isPlaying() )
				{
					System.out.println("NoisyAudioStreamReceiver / play -> pause");
					AudioPlayer.mMediaPlayer.pause();
					AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunOneTimeMode); 
					AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunContinueMode); 
					AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_PAUSE;
					//update audio control state
					UtilAudio.updateFooterAudioState(NoteFragment.footerAudioPlayAndPause,NoteFragment.footerAudioTextView);
					
		    		// update playing state in note view pager
					if( Note_view_pager.mPager != null)
					{
						if(Note_view_pager_buttons_controller.imageViewAudioButton != null) 
							Note_view_pager_buttons_controller.imageViewAudioButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_ringer_on, 0, 0, 0);
						
						if(Note_view_pager.mMenuItemAudio.isVisible())
							Note_view_pager.mMenuItemAudio.setIcon(R.drawable.ic_lock_ringer_on);
					}
		    		
				}        	
	        }
	    }
	}

	@Override
	public void onBackStackChanged() {
		int backStackEntryCount = fragmentManager.getBackStackEntryCount();
		System.out.println("--- DrawerActivity / _onBackStackChanged / backStackEntryCount = " + backStackEntryCount);
		 
		if(backStackEntryCount > 0){
			getActionBar().setDisplayShowHomeEnabled(false);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}else{
			initActionBar();
		}		
	}
}