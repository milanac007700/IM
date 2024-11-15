package com.milanac007.demo.im.db.entity;

public class SearchElement {
	public int startIndex = -1;
	public int endIndex = -1;
	
	@Override
	public String toString() {
		return "SearchElement [startIndex=" + startIndex + ", endIndex="
				+ endIndex + "]";
	}

	public void reset() {
		startIndex = -1;
		endIndex = -1;
	}
}