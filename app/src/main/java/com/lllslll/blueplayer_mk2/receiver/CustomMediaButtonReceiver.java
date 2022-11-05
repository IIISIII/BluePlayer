package com.lllslll.blueplayer_mk2.receiver;

import android.content.*;
import android.view.*;

import com.lllslll.blueplayer_mk2.util.*;


public class CustomMediaButtonReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context c, Intent i)
	{
		if(MusicPlayList.getPlayList() == null)
			return;
		MusicPlayer player = MusicPlayer.getInstance();
		
		String action = i.getAction();
		if(action == null)
			return;
		if(action.equals(Intent.ACTION_MEDIA_BUTTON)) {
			KeyEvent keyEvent = (KeyEvent)i.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if(keyEvent.getAction() == KeyEvent.ACTION_UP) {
				switch(keyEvent.getKeyCode()) {
					case KeyEvent.KEYCODE_MEDIA_PLAY :
					case KeyEvent.KEYCODE_MEDIA_PAUSE :
					case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE :
					case KeyEvent.KEYCODE_HEADSETHOOK :
						if(player.getMusicFile() == null)
							player.setMusicFile(MusicPlayList.getCurrentMusicFile());
						if(player.isPlaying())
							player.pause();
						else
							player.play();
						break;
					case KeyEvent.KEYCODE_MEDIA_NEXT :
						MusicPlayList.moveTo(1);
						player.setMusicFile(MusicPlayList.getCurrentMusicFile());
						player.setProgress(0);
						player.play();
						break;
					case KeyEvent.KEYCODE_MEDIA_PREVIOUS :
						MusicPlayList.moveTo(-1);
						player.setMusicFile(MusicPlayList.getCurrentMusicFile());
						player.setProgress(0);
						player.play();
						break;
					case KeyEvent.KEYCODE_MEDIA_STOP :
						player.pause();
				}
			}
		}
	}
}
