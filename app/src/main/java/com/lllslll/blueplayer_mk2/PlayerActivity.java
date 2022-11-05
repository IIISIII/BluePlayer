package com.lllslll.blueplayer_mk2;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import com.lllslll.blueplayer_mk2.adapter.*;
import com.lllslll.blueplayer_mk2.file.*;
import com.lllslll.blueplayer_mk2.service.*;
import com.lllslll.blueplayer_mk2.util.*;


public class PlayerActivity extends Activity implements MusicPlayer.OnMusicPlayerListener, AdapterView.OnItemClickListener
{
	private boolean isActivityRunning = false;
	private boolean isSeekBarTouching = false;
	private boolean isLoopThreadRunning = false;
	private boolean isPlayListVisible = false;
	
	private MusicPlayer player = null;
	
	private TextView playerTitle = null;
	private TextView playerArtist = null;
	private TextView playerProgress = null;
	private TextView playerVolume = null;
	
	private SeekBar playerSeekBar = null;
	private SeekBar playerVolumeSeekBar = null;
	
	private ImageView playerCover = null;
	
	private ImageButton playerButton = null;
	private ImageButton playerLoop = null;
	
	private FrameLayout playlistBackground = null;
	
	private ListView playlistView = null;
	
	private PlayListAdapter playlistAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.big_player_layout);
		this.getActionBar().hide();
		
		this.isActivityRunning = true;
		
		this.player = MusicPlayer.getInstance();
		
		this.playerTitle = (TextView)findViewById(R.id.big_player_title);
		this.playerArtist = (TextView)findViewById(R.id.big_player_artist);
		this.playerProgress = (TextView)findViewById(R.id.big_player_progress);
		this.playerVolume = (TextView)findViewById(R.id.big_player_volume);
		
		this.playerSeekBar = (SeekBar)findViewById(R.id.big_player_seekbar);
		this.playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onStartTrackingTouch(SeekBar seek)
			{
				isSeekBarTouching = true;
			}
			
			@Override
			public void onProgressChanged(SeekBar seek, int progress, boolean fromUser)
			{
				playerProgress.setText(MusicFile.durationToTimetext(progress) + "/" + MusicFile.durationToTimetext(seek.getMax()));
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seek)
			{
				isSeekBarTouching = false;
				player.setProgress(seek.getProgress());
			}
		});
		this.playerVolumeSeekBar = (SeekBar)findViewById(R.id.big_player_volume_seekbar);
		this.playerVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onStartTrackingTouch(SeekBar seek) {}

			@Override
			public void onProgressChanged(SeekBar seek, int progress, boolean fromUser)
			{
				if(fromUser)
					player.setVolume(progress / 100.0f);
				playerVolume.setText(progress + "%");
			}

			@Override
			public void onStopTrackingTouch(SeekBar seek) {}
		});
		
		this.playerCover = (ImageView)findViewById(R.id.big_player_cover);
		
		this.playerButton = (ImageButton)findViewById(R.id.big_player_play_pause);
		this.playerLoop = (ImageButton)findViewById(R.id.big_player_loop);
		
		this.playlistBackground = (FrameLayout)findViewById(R.id.playList_background);
		
		this.playlistView = (ListView)findViewById(R.id.playList_listview);
		this.playlistView.setFastScrollEnabled(true);
		this.playlistView.setOnItemClickListener(this);
		this.playlistAdapter = new PlayListAdapter(this);
		this.playlistView.setAdapter(this.playlistAdapter);
		
		if(!this.isLoopThreadRunning) {
			this.loopThread.start();
			this.isLoopThreadRunning = true;
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		this.player.addListener(this, false);
		
		if(!this.init())
			super.finish();
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
		
		this.isActivityRunning = false;
	}

	@Override
	public void finish()
	{
		super.finish();
		this.overridePendingTransition(R.anim.hold, R.anim.player_activity_exit);
	}

	@Override
	public void onBackPressed()
	{
		if(this.isPlayListVisible) {
			this.disablePlayList();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onPlayStateChanged(MusicPlayer player, boolean isPlaying)
	{
		this.playerButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
	}

	@Override
	public void onPrepared(MusicPlayer player)
	{
		MusicFile mf = player.getMusicFile();
		if(!mf.isExist())
			return;
			
		this.playlistAdapter.select(MusicPlayList.getCurrentIndex());

		Bitmap thumb = mf.getThumbnail();
		if(thumb == null)
			this.playerCover.setImageResource(R.drawable.empty_cover);
		else
			this.playerCover.setImageBitmap(thumb);

		this.playerTitle.setText(mf.getTitle());
		this.playerArtist.setText(mf.getArtist());

		this.playerSeekBar.setMax(mf.getDuration());
		this.playerSeekBar.setProgress(player.getProgress());
		
		this.playerProgress.setText(MusicFile.durationToTimetext(this.playerSeekBar.getProgress()) + "/" + MusicFile.durationToTimetext(this.playerSeekBar.getMax()));
	}

	@Override
	public void onLoopStateChanged(MusicPlayer player, boolean isLooping)
	{
		this.playerLoop.setImageResource(this.player.isLooping() ? R.drawable.ic_repeat : R.drawable.ic_no_repeat);
	}
	
	@Override
	public void onCompletion(MusicPlayer player) {}

	@Override
	public void onError(MusicPlayer player, int what, int extra) {}
	
	public void OnClick(View v)
	{
		switch(v.getId()) {
			case R.id.big_player_play_pause :
				this.playPauseMusic();
				break;
			case R.id.big_player_previous:
				this.playPreviousMusic();
				break;
			case R.id.big_player_next :
				this.playNextMusic();
				break;
			case R.id.big_player_loop :
				this.player.setLooping(!this.player.isLooping());
				break;
			case R.id.open_playlist :
				showPlayList();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long l_pos)
	{
		MusicPlayList.setCurrentIndex(pos);
		this.player.setMusicFile(MusicPlayList.getCurrentMusicFile());
		this.player.setProgress(0);
		this.playMusic();
		this.disablePlayList();
	}
	
	private boolean init()
	{
		MusicFile mf = this.player.getMusicFile();
		if(!mf.isExist())
			return false;
			
		try {
			Bitmap thumb = mf.getThumbnail();
			if(thumb == null)
				this.playerCover.setImageResource(R.drawable.empty_cover);
			else
				this.playerCover.setImageBitmap(thumb);

			this.playerTitle.setText(mf.getTitle());
			this.playerTitle.setSelected(true);
			this.playerArtist.setText(mf.getArtist());

			this.playerSeekBar.setMax(mf.getDuration());
			this.playerSeekBar.setProgress(this.player.getProgress());

			if(this.player.isPlaying() || this.player.isPreparing())
				this.playerButton.setImageResource(R.drawable.ic_pause);
			else
				this.playerButton.setImageResource(R.drawable.ic_play);

			this.playerLoop.setImageResource(this.player.isLooping() ? R.drawable.ic_repeat : R.drawable.ic_no_repeat);

			this.playerVolumeSeekBar.setProgress((int)(this.player.getVolume() * 100));
		} catch(Exception err) {
			return false;
		}
		
		return true;
	}
	
	private void showPlayList()
	{
		if(this.isPlayListVisible)
			return;
		this.playlistBackground.setVisibility(View.VISIBLE);
		this.playlistAdapter.select(MusicPlayList.getCurrentIndex());
		this.playlistAdapter.setDataList(MusicPlayList.getPlayList());
		this.playlistView.setSelection(MusicPlayList.getCurrentIndex());
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.menu_background_visible);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation anim)
			{
				playlistBackground.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(isPlayListVisible)
							disablePlayList();
					}
				});
			}

			@Override
			public void onAnimationStart(Animation anim) {}
			
			@Override
			public void onAnimationRepeat(Animation anim) {}
		});
		
		this.playlistBackground.startAnimation(anim);
		this.playlistView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.playlist_visible));
		
		this.isPlayListVisible = true;
	}
	
	private void disablePlayList()
	{
		if(!this.isPlayListVisible)
			return;
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.menu_background_invisible);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation anim)
			{
				playlistBackground.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationStart(Animation anim)
			{
				playlistBackground.setOnClickListener(null);
			}

			@Override
			public void onAnimationRepeat(Animation anim) {}
		});
		
		this.playlistBackground.startAnimation(anim);
		this.playlistView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.playlist_invisible));
		
		this.isPlayListVisible = false;
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
	
	private Thread loopThread = new Thread(new Runnable()
	{
		@Override
		public void run()
		{
			while(isActivityRunning) {
				try {
					if(player.isPlaying()) {
						if(playerSeekBar != null && !isSeekBarTouching)
							playerSeekBar.setProgress(player.getProgress());
					}
					Thread.sleep(100);
				} catch(InterruptedException err) {
					
				}
			}
			isLoopThreadRunning = false;
		}
	});
}
