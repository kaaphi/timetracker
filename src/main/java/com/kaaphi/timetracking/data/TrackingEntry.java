package com.kaaphi.timetracking.data;

import java.util.Date;

public class TrackingEntry {
	private final Date start;
	private final Date end;
	private final long duration;
	private final String activity;
	private final String category;
	
	public TrackingEntry(Date start, Date end, long duration, String activity, String category) {
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.activity = activity;
		this.category = category;
	}
	public Date getStartDate() {
		return start;
	}
	public long getDuration() {
		return duration;
	}
	public String getActivity() {
		return activity;
	}
	public String getCategory() {
		return category;
	}
	public Date getEndDate() {
		return end;
	}
}
