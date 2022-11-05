package com.lllslll.blueplayer_mk2.file;

public class PlayListData extends FileData
{
	private long id = -1;
	
	public PlayListData(String name, long id)
	{
		super(null, name);
		this.id = id;
	}
	
	public Long getId()
	{
		return this.id;
	}
}
