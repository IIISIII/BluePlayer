package com.lllslll.blueplayer_mk2.file;

import java.io.*;

public class FileData implements Comparable<FileData>
{
	private String path = null;
	private String name = null;
	private String parent = null;
	
	public FileData(String path)
	{
		this(new File(path));
	}
	
	public FileData(File f)
	{
		this.path = f.getAbsolutePath();
		this.name = f.getName();
		this.parent = f.getParent();
	}
	
	protected FileData(String path, String name)
	{
		this.path = path;
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getPath()
	{
		return this.path;
	}
	
	public String getParent()
	{
		return this.parent;
	}
	
	public boolean isExist()
	{
		return new File(this.path).exists();
	}

	@Override
	public int compareTo(FileData obj)
	{
		if((this instanceof Folder) && !(obj instanceof Folder))
			return -1;
		if(!(this instanceof Folder) && (obj instanceof Folder))
			return 1;
			
		return this.name.compareTo(obj.name);
	}
}
