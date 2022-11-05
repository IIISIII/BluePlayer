package com.lllslll.blueplayer_mk2.util;

import java.io.*;
import java.util.*;

import com.lllslll.blueplayer_mk2.file.*;


public class FileSearcher
{
	public static ArrayList<FileData> searchFileByExt(final FileData folder, final boolean withFolder, final String ext)
	{
		File f = new File(folder.getPath());
		File[] list = f.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f)
				{
					if(withFolder)
						return f.isDirectory() || f.getAbsolutePath().endsWith(ext);
					return f.getAbsolutePath().endsWith(ext);
				}
			});
		ArrayList<FileData> fileList = new ArrayList<>();
		for(int a = 0, l = list.length; a < l; a ++) {
			if(list[a].isDirectory())
				fileList.add(new Folder(list[a]));
			else if(list[a].getAbsolutePath().endsWith(".mp3"))
				fileList.add(MusicFile.createMusicFile(list[a].getAbsolutePath(), null, null, folder));
		}
		Collections.sort(fileList);
		return fileList;
	}
}
