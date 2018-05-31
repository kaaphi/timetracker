package com.kaaphi.timetracking.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeEvent implements Comparable<TimeEvent> {
	public final long time;
	public final String eventName;
	
	public TimeEvent(long time, String eventName) {
		super();
		this.time = time;
		this.eventName = eventName;
	}

	@Override
	public int compareTo(TimeEvent that) {
		return Long.valueOf(this.time).compareTo(Long.valueOf(that.time));
	}
	
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return String.format("%s (%s)", sdf.format(new Date(time)), eventName);
	}
}
