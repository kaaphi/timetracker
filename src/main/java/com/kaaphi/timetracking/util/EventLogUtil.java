package com.kaaphi.timetracking.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;

public class EventLogUtil {
	private static final int REVERSE_ORDER= 0x8;
	
	public static final int LOCK_EVENT = 4800;
	public static final int UNLOCK_EVENT = 4801;
	
	public static List<TimeEvent> getRecentLocks() {
		List<EventRecord> locks = getRecordsLastDay(LOCK_EVENT);
		List<TimeEvent> evts = new LinkedList<TimeEvent>();
		for(EventRecord r : locks) {
			evts.add(new TimeEvent(r.time, "Lock"));
		}
		return evts;
	}
	
	public static List<TimeEvent> getRecentUnlocks() {
		List<EventRecord> locks = getRecordsLastDay(UNLOCK_EVENT);
		List<TimeEvent> evts = new LinkedList<TimeEvent>();
		for(EventRecord r : locks) {
			evts.add(new TimeEvent(r.time, "Unlock"));
		}
		return evts;
	}
	
	public static List<EventRecord> getRecordsLastDay(int...ids) {
		return getRecords(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1), -1, ids);
	}
	
	public static List<EventRecord> getRecords(long dateLimit, int countLimit, int...ids) {
		List<EventRecord> records = new LinkedList<EventLogUtil.EventRecord>();
		Arrays.sort(ids);
		
		EventLogIterator it = new EventLogIterator("", "Security", REVERSE_ORDER);
		long lastEventDate = 0;
		int eventCount = 0;
		do {
			EventLogRecord rec = it.next();
			eventCount++;
			lastEventDate = rec.getRecord().TimeGenerated.longValue() * 1000;
			if(Arrays.binarySearch(ids, rec.getEventId()) >= 0) {
				records.add(new EventRecord(rec.getEventId(), lastEventDate));
			}
		} while(it.hasNext() && (dateLimit < 0 || lastEventDate < dateLimit) && (countLimit < 0 || countLimit > eventCount));
		
		return records;
	}
	
	public static class EventRecord {
		public final int id;
		public final long time;
		public EventRecord(int id, long time) {
			super();
			this.time = time;
			this.id = id;
		}
		
		
	}
}
