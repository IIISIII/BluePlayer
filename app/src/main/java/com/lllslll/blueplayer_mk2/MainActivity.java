package com.lllslll.blueplayer_mk2;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.media.session.*;
import android.os.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import java.util.*;

import com.lllslll.blueplayer_mk2.adapter.*;
import com.lllslll.blueplayer_mk2.file.*;
import com.lllslll.blueplayer_mk2.service.*;
import com.lllslll.blueplayer_mk2.util.*;


public class MainActivity extends Activity implements MusicPlayer.OnMusicPlayerListener, AdapterView.OnItemClickListener
{
	private boolean isAnimationPlaying = false;
	private boolean isListLoading = false;
	private boolean isPermissionDenied = false;
	private boolean isPlayerVisible = false;
	
	private ListType listType = ListType.FOLDER;
	
	private MusicPlayer player = null;
	
	private ListView listView = null;
	
	private FileDataAdapter currentAdapter = null;
	private FileDataAdapter folderAdapter = null;
	private FileDataAdapter libraryAdapter = null;
	private FileDataAdapter mediadbAdapter = null;
	private FileDataAdapter playlistAdapter = null;
	
	private FrameLayout menuBackgroundLayout = null;
	private LinearLayout menuLayout = null;
	
	private FrameLayout playerLayout = null;
	private RelativeLayout playerTouchLayout = null;
	private ImageView playerThumbnail = null;
	private TextView playerTitle = null;
	private ImageButton playerButton = null;
	
	private ImageButton openMenuBu = null;
	private ImageButton[] menuIcons = new ImageButton[6];
	
	private LinearLayout permissionLayout = null;
	
	private ProgressBar loadingProgress = null;
	
	private LinearLayout searchItemBackground = null;
	
	private LoadingTask loadingTask = null;
	
	private ArrayList<Integer> listViewPositionList = new ArrayList<>();
	private ArrayList<FileData> parentList = new ArrayList<>();
	
	private int searchIndex = -1;
	private ArrayList<Integer> searchIndexList = new ArrayList<>();
	
	private Handler handler = null;
	
	
	private static final int REQUEST_CODE = 7;
	private static final String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		this.player = MusicPlayer.getInstance();
		
		this.openMenuBu = (ImageButton)findViewById(R.id.open_menu);
		int[] ids = { R.id.search_list, R.id.folder_list, R.id.library_list, R.id.mediadb_list, R.id.playlist_list, R.id.disable_menu };
		for(int a = 0, l = ids.length; a < l; a ++)
			this.menuIcons[a] = (ImageButton)findViewById(ids[a]);
		
		this.menuBackgroundLayout = (FrameLayout)findViewById(R.id.menu_background_layout);
		this.menuBackgroundLayout.setVisibility(View.GONE);
		
		this.menuLayout = (LinearLayout)findViewById(R.id.menu);
		
		this.listView = (ListView)findViewById(R.id.list);
		this.listView.setOnItemClickListener(this);
		
		this.folderAdapter = new FileDataAdapter(this);
		this.libraryAdapter = new FileDataAdapter(this);
		this.mediadbAdapter = new FileDataAdapter(this);
		this.playlistAdapter = new FileDataAdapter(this);
		
		this.playerLayout = (FrameLayout)findViewById(R.id.player);

		this.playerTouchLayout = (RelativeLayout)findViewById(R.id.player_touch);
		this.playerTouchLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(MainActivity.this, PlayerActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.player_activity_start, R.anim.hold);
			}
		});
		
		this.playerThumbnail = (ImageView)findViewById(R.id.player_thumbnail);
		this.playerTitle = (TextView)findViewById(R.id.player_title);
		this.playerButton = (ImageButton)findViewById(R.id.player_button);
		
		this.loadingProgress = (ProgressBar)findViewById(R.id.loading);
		
		this.permissionLayout = (LinearLayout)findViewById(R.id.permission_layout);
		
		this.searchItemBackground = (LinearLayout)findViewById(R.id.searchitem_background);
		
		this.init();
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		Resources res = this.getResources();
		int size1 = 0, size2 = 0;
		
		switch(newConfig.orientation) {
			case Configuration.ORIENTATION_PORTRAIT :
				size1 = (int)res.getDimension(R.dimen.open_menu_size);
				size2 = (int)res.getDimension(R.dimen.menu_icon_size);
				break;
			case Configuration.ORIENTATION_LANDSCAPE :
				size1 = (int)res.getDimension(R.dimen.open_menu_size_land);
				size2 = (int)res.getDimension(R.dimen.menu_icon_size_land);
		}
		
		ViewGroup.LayoutParams params = this.openMenuBu.getLayoutParams();
		params.width = size1;
		params.height = size1;
		this.openMenuBu.setLayoutParams(params);

		for(ImageButton bu : this.menuIcons) {
			params = bu.getLayoutParams();
			params.width = size2;
			params.height = size2;
			bu.setLayoutParams(params);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if(requestCode == REQUEST_CODE) {
			if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				this.isPermissionDenied = false;
				this.listView.setVisibility(View.VISIBLE);
				this.permissionLayout.setVisibility(View.GONE);

				this.initPlayer(this.player);
				this.initList(true, null, 0, false);
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		this.player.addListener(this, false);
		
		if(!this.isPermissionDenied)
			this.initPlayer(this.player);
		else
			this.init();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		
		this.player.removeListener(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed()
	{
		if(!this.isPermissionDenied) {
			if(this.parentList.size() > 0) {
				if(this.listType == ListType.FOLDER) {
					this.initList(false, new Folder(this.parentList.remove(0).getParent()), this.listViewPositionList.remove(0), true);
					return;
				}
				else {
					this.parentList.remove(0);
					this.initList(false, null, this.listViewPositionList.remove(0), true);
				}
			}
		}
		super.onBackPressed();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long l_pos)
	{
		if(this.isListLoading)
			return;
		FileData data = (FileData)this.currentAdapter.getItem(pos);
		if(data != null) {
			if(data instanceof MusicFile) {
				if(data.isExist()) {
					ArrayList<FileData> playList = new ArrayList<>();
					ArrayList<FileData> dataList = this.currentAdapter.getDataList();
					for(int a = 0, l = dataList.size(); a < l; a ++) {
						FileData item = dataList.get(a);
						if(item instanceof MusicFile)
							playList.add(item);
					}
					MusicPlayList.setPlayList(playList);
					MusicPlayList.setCurrentIndex(playList.indexOf(data));
					this.player.setMusicFile(MusicPlayList.getCurrentMusicFile());
					this.player.setProgress(0);
					this.playMusic();
					if(this.isPlayerVisible)
						this.refreshPlayer(this.player);
					else
						this.initPlayer(this.player);
				}
			}
			else
				this.initList(false, data, 0, false);
		}
	}

	@Override
	public void onPlayStateChanged(MusicPlayer player, boolean isPlaying)
	{
		if(isPlaying)
			this.playerButton.setImageResource(R.drawable.ic_pause);
		else
			this.playerButton.setImageResource(R.drawable.ic_play);
	}

	@Override
	public void onLoopStateChanged(MusicPlayer player, boolean isLooping) {}

	@Override
	public void onError(MusicPlayer player, int what, int extra) {}

	@Override
	public void onPrepared(MusicPlayer player)
	{
		if(this.isPlayerVisible)
			this.refreshPlayer(this.player);
		else
			this.initPlayer(this.player);
	}

	@Override
	public void onCompletion(MusicPlayer player) {}
	
	private boolean checkPermissions(String[] permission)
	{
		for(int a = 0, l = permission.length; a < l;a ++) {
			if(this.checkSelfPermission(permission[a]) == PackageManager.PERMISSION_DENIED)
				return false;
		}
		return true;
	}
	
	private void init()
	{
		if(!this.checkPermissions(PERMISSIONS)) {
			this.isPermissionDenied = true;
			
			this.permissionLayout.setVisibility(View.VISIBLE);
			this.listView.setVisibility(View.GONE);
		}
		else {
			this.isPermissionDenied = false;
			
			this.permissionLayout.setVisibility(View.GONE);
			this.listView.setVisibility(View.VISIBLE);
			
			this.initPlayer(this.player);
			this.initList(true, null, 0, false);
		}
	}
	
	private void initList(final boolean isAdapterChanged, final FileData data, final int position, boolean isRestored)
	{
		if(this.isListLoading && this.handler != null) {
			this.handler.removeCallbacksAndMessages(null);
			this.loadingTask.cancel(true);
		}
			
		this.isListLoading = true;
		
		if(isAdapterChanged) {
			this.listViewPositionList.clear();
			this.parentList.clear();
			if(this.currentAdapter != null)
				this.currentAdapter.clearSavedList();
			this.searchItemBackground.setVisibility(View.GONE);
		}
		else {
			if(!isRestored) {
				this.currentAdapter.saveList();
				int cp = listView.getFirstVisiblePosition();
				this.listViewPositionList.add(0, cp);
				this.parentList.add(0, data);
			}
			else {
				if(this.currentAdapter.restoreSavedList()) {
					if(!this.isPlayerVisible)
						this.listView.setSelection(position);
					else
						this.listView.setSelection(position == 0 ? -1 : position + 1);
					this.isListLoading = false;
					return;
				}
			}
		}
		
		this.loadingTask = new LoadingTask(this)
		{
			@Override
			public void onPost()
			{
				loadingProgress.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				
				if(!isAdapterChanged)
					setSelection(position);

				isListLoading = false;
			}
		};
		
		if(this.listType == ListType.FOLDER)
			this.currentAdapter = this.folderAdapter;
		else if(this.listType == ListType.LIBRARY)
			this.currentAdapter = this.libraryAdapter;
		else if(this.listType == ListType.MEDIA_DB)
			this.currentAdapter = this.mediadbAdapter;
		else if(this.listType == ListType.PLAYLIST)
			this.currentAdapter = this.playlistAdapter;
		
		if(isAdapterChanged)
			this.listView.setAdapter(this.currentAdapter);
		
		this.loadingTask.setAdapter(this.currentAdapter);
		
		this.listView.setVisibility(View.GONE);
		this.loadingProgress.setVisibility(View.VISIBLE);
		
		this.handler = new Handler();
		this.handler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				loadingTask.execute(listType, data);
				handler = null;
			}
		}, 100);
	}
	
	private void initPlayer(MusicPlayer p)
	{
		Resources res = this.getResources();
		int paddingVisible = (int)res.getDimension(R.dimen.player_visible_padding);
		int paddingInvisible = (int)res.getDimension(R.dimen.player_invisible_padding);
		
		MusicFile mf = p.getMusicFile();
		if(mf == null) {
			PlayerDataManager.readPlayerState(this, p);
			mf = p.getMusicFile();
		}
		
		if(mf == null || !mf.isExist()) {
			this.isPlayerVisible = false;
			
			this.playerLayout.setVisibility(View.GONE);
			
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)this.searchItemBackground.getLayoutParams();
			params.setMargins(0, paddingInvisible, paddingInvisible * 2, 0);
			this.searchItemBackground.setLayoutParams(params);
			
			this.listView.setPadding(paddingInvisible, paddingInvisible, paddingInvisible, paddingInvisible);
			this.listView.setFastScrollEnabled(true);
		}
		else {
			if(!this.isPlayerVisible) {
				this.isPlayerVisible = true;
			
				this.listView.setFastScrollEnabled(false);
				this.playerLayout.setVisibility(View.VISIBLE);
				
				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)this.searchItemBackground.getLayoutParams();
				params.setMargins(0, paddingVisible, paddingInvisible * 2, 0);
				this.searchItemBackground.setLayoutParams(params);
				
				this.listView.setPadding(paddingInvisible, paddingVisible, paddingInvisible, paddingInvisible);
				this.listView.setFastScrollEnabled(true);
			}
			this.refreshPlayer(p);
		}
	}
	
	private void refreshPlayer(MusicPlayer p)
	{
		MusicFile mf = p.getMusicFile();
		
		if(mf == null || !mf.isExist())
			return;
		else {
			Bitmap thumb = mf.getThumbnail();
			if(thumb != null)
				this.playerThumbnail.setImageBitmap(thumb);
			else
				this.playerThumbnail.setImageResource(R.drawable.empty_cover);
			this.playerTitle.setText(mf.getTitle());
			this.playerButton.setImageResource(p.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
		}
	}
	
	private void showMenu()
	{
		if(this.isAnimationPlaying || this.isListLoading)
			return;
			
		this.isAnimationPlaying = true;
		
		this.openMenuBu.setVisibility(View.GONE);
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.menu_visible);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation anim)
			{
				isAnimationPlaying = false;
			}

			@Override
			public void onAnimationStart(Animation anim)
			{
				menuBackgroundLayout.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						disableMenu();
					}
				});
			}

			@Override
			public void onAnimationRepeat(Animation anim)
			{

			}
		});
		
		this.menuBackgroundLayout.setVisibility(View.VISIBLE);
		this.menuBackgroundLayout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.menu_background_visible));
		this.menuLayout.startAnimation(anim);
	}
	
	private void disableMenu()
	{
		if(this.isAnimationPlaying)
			return;
			
		this.isAnimationPlaying = true;
			
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.menu_invisible);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation anim)
			{
				menuBackgroundLayout.setVisibility(View.GONE);
				openMenuBu.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						isAnimationPlaying = false;
					}
				}, 100);
			}
			
			@Override
			public void onAnimationStart(Animation anim)
			{
				menuBackgroundLayout.setOnClickListener(null);
			}
			
			@Override
			public void onAnimationRepeat(Animation anim) {}
		});
		this.menuLayout.startAnimation(anim);
		this.menuBackgroundLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.menu_background_invisible));
	}
	
	public void OnClick(View v)
	{
		int id = v.getId();
		switch(id) {
			case R.id.open_menu :
				this.showMenu();
				break;
			case R.id.disable_menu :
				this.disableMenu();
				break;
			case R.id.search_list :
				if(!this.isPermissionDenied)
					this.showSearchDialog();
				this.disableMenu();
				break;
			case R.id.folder_list :
				this.disableMenu();
				this.changeListType(ListType.FOLDER);
				if(!this.isPermissionDenied)
					this.initList(true, null, 0, false);
				break;
			case R.id.library_list :
				this.disableMenu();
				this.changeListType(ListType.LIBRARY);
				if(!this.isPermissionDenied)
					this.initList(true, null, 0, false);
				break;
			case R.id.mediadb_list :
				this.disableMenu();
				this.changeListType(ListType.MEDIA_DB);
				if(!this.isPermissionDenied)
					this.initList(true, null, 0, false);
				break;
			case R.id.playlist_list :
				this.disableMenu();
				this.changeListType(ListType.PLAYLIST);
				if(!this.isPermissionDenied)
					this.initList(true, null, 0, false);
				break;
			case R.id.player_button :
				this.playPauseMusic();
				break;
			case R.id.request_permission :
				this.requestPermissions(PERMISSIONS, REQUEST_CODE);
				break;
			case R.id.searchitem_close :
				this.searchItemBackground.setVisibility(View.GONE);
				break;
			case R.id.searchitem_up :
				this.searchUp();
				break;
			case R.id.searchitem_down :
				this.searchDown();
		}
	}
	
	private void showSearchDialog()
	{
		EditTextDialogManager manager = new EditTextDialogManager(this, R.string.search_title, R.string.search_message, R.string.search_hint)
		{
			@Override
			public void onClickPositive(String t)
			{
				if(t.length() == 0)
					return;
				
				searchIndexList.clear();
				ArrayList<FileData> list = currentAdapter.getDataList();
				for(int a = 0, l = list.size(); a < l; a ++) {
					try {
						FileData data = list.get(a);
						if(data instanceof Folder) {
							if(data.getName().toLowerCase().indexOf(t.toLowerCase()) >= 0)
								searchIndexList.add(a);
							continue;
						}
						else if(data instanceof MusicFile) {
							MusicFile mf = (MusicFile)data;
							if(mf.getTitle().toLowerCase().indexOf(t.toLowerCase()) >= 0 || mf.getArtist().toLowerCase().indexOf(t.toLowerCase()) >= 0)
								searchIndexList.add(a);
							continue;
						}
					} catch(Exception err) {}
				}
				
				Toast.makeText(MainActivity.this, searchIndexList.size() + " found", 0).show();
				
				if(searchIndexList.size() == 0)
					return;
				
				searchIndex = 0;
				listView.setSelection(searchIndexList.get(searchIndex));
				
				searchItemBackground.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onClickNegative(String t)
			{
				
			}
		};
		manager.show();
	}
	
	private void searchUp()
	{
		if(this.searchIndexList.size() == 0)
			return;
			
		if(--this.searchIndex < 0)
			this.searchIndex += this.searchIndexList.size();
		
		this.listView.setSelection(this.searchIndexList.get(this.searchIndex));
		
		FileData data = (FileData)this.currentAdapter.getItem(this.searchIndexList.get(this.searchIndex));
		if(data instanceof Folder)
			Toast.makeText(this, data.getName(), 0).show();
		else if(data instanceof MusicFile) {
			MusicFile mf = (MusicFile)data;
			Toast.makeText(this, mf.getTitle(), 0).show();
		}
	}
	
	private void searchDown()
	{
		if(this.searchIndexList.size() == 0)
			return;
		
		this.searchIndex = (this.searchIndex + 1) % this.searchIndexList.size();
		
		this.listView.setSelection(this.searchIndexList.get(this.searchIndex));
		
		FileData data = (FileData)this.currentAdapter.getItem(this.searchIndexList.get(this.searchIndex));
		if(data instanceof Folder)
			Toast.makeText(this, data.getName(), 0).show();
		else if(data instanceof MusicFile) {
			MusicFile mf = (MusicFile)data;
			Toast.makeText(this, mf.getTitle(), 0).show();
		}
	}
	
	private void setSelection(int i)
	{
		if(!this.isPlayerVisible)
			this.listView.setSelection(i);
		else
			this.listView.setSelection(i == 0 ? -1 : i + 1);
	}
	
	private void playMusic()
	{
		if(this.player.getMusicFile() != null) {
			Intent i = new Intent(this, MusicPlayerService.class);
			i.setAction(MusicPlayerService.Action.PLAY);
			this.startService(i);
		}
	}
	
	private void playPauseMusic()
	{
		if(this.player.getMusicFile() != null) {
			Intent i = new Intent(this, MusicPlayerService.class);
			i.setAction(MusicPlayerService.Action.PLAY_PAUSE);
			this.startService(i);
		}
	}
	
	private void playPreviousMusic()
	{
		if(this.player.getMusicFile() != null) {
			Intent i = new Intent(this, MusicPlayerService.class);
			i.setAction(MusicPlayerService.Action.PREVIOUS);
			this.startService(i);
		}
	}

	private void playNextMusic()
	{
		if(this.player.getMusicFile() != null) {
			Intent i = new Intent(this, MusicPlayerService.class);
			i.setAction(MusicPlayerService.Action.NEXT);
			this.startService(i);
		}
	}
	
	private PlaybackState getPlaybackState(MusicPlayer player)
	{
		PlaybackState.Builder builder = new PlaybackState.Builder()
			.setActions(PlaybackState.ACTION_PLAY_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS)
			.setState(player.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, player.getProgress(), 1.0f);
		return builder.build();
	}
	
	private void changeListType(ListType type)
	{
		this.listType = type;
	}
}
