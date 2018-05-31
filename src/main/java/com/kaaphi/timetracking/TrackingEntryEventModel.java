package com.kaaphi.timetracking;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.AbsoluteDuration;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.DurationValue;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.PercentDuration;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.TrackingEntryModelListener;

public class TrackingEntryEventModel {
	private Date start;
	private Date end;
	
	private List<TrackingEntryModel> entries;
	private TrackingEntryModel remainingEntry;
	private long remainingDuration;
	private TrackingEntryEventModelListener listener;
	private Map<String, Set<String>> availableCategoriesAndActivities;
	
	public static interface TrackingEntryEventModelListener {
		public void modelUpdate(Date start, Date end, long remainingDuration, boolean isValid);
	}
	
	public TrackingEntryEventModel(TrackingEntryEventModelListener listener, Date start, Date end,  Map<String, Set<String>> availableCategoriesAndActivities) {
		this.entries = new LinkedList<TrackingEntryModel>();
		this.start = start;
		this.end = end;
		this.remainingDuration = end.getTime() - start.getTime();
		this.listener = listener;
		this.availableCategoriesAndActivities = availableCategoriesAndActivities;
		listener.modelUpdate(start, end, remainingDuration, isValid());
	}
	
	public TrackingEntryModel createEntryModel(TrackingEntryModelListener l) {
		TrackingEntryModel m = new TrackingEntryModel(this, l);
		entries.add(m);
		recalculateEntries();
		return m;		
	}
	
	public void removeEntryModel(TrackingEntryModel m) {
		entries.remove(m);
		if(remainingEntry == m) {
			remainingEntry = null;
		}
		recalculateEntries();
	}	
	
	public Date getStart() {
		return start;
	}
	
	public Date getEnd() {
		return end;
	}
	
	private void setRemainingEntry(TrackingEntryModel m) {
		if(remainingEntry != null && remainingEntry != m) {
			remainingEntry.resetDurationValue();
		}
		
		remainingEntry = m;
	}
	
	public long getRemainingDuration() {
		return remainingDuration;
	}
	
	public Map<String, Set<String>> getAvailableCategories() {
		return availableCategoriesAndActivities;
	}
	
	public void setEnd(Date end) {
		this.end = end;
		recalculateEntries();
	}
	
	public void setStart(Date start) {
		this.start = start;
		recalculateEntries();
	}
	
	public void setDateRange(Date start, Date end) {
		this.start = start;
		this.end = end;
		
		recalculateEntries();
	}
	
	public void recalculateEntries() {
		long totalDuration = end.getTime() - start.getTime();
		
		List<TrackingEntryModel> percentEntries = new LinkedList<TrackingEntryModel>();
		
		for(TrackingEntryModel m : entries) {
			DurationValue dv = m.getDurationValue();
			if(dv == null) {
				m.setAbsoluteDuration(0);
			} else if(dv instanceof AbsoluteDuration) {
				long absolute = ((AbsoluteDuration) dv).getDuration();
				m.setAbsoluteDuration(absolute);
				totalDuration -= absolute;
			} else if (dv instanceof PercentDuration) {
				percentEntries.add(m);
			}
		}
		
		
		long availableDuration = totalDuration;
		for(TrackingEntryModel m : percentEntries) {
			PercentDuration d = (PercentDuration) m.getDurationValue();
			long absolute = (availableDuration*d.getPercent())/100L;
			totalDuration -= absolute;
			m.setAbsoluteDuration(absolute);
		}
		
		if(remainingEntry != null) {
			remainingEntry.setAbsoluteDuration(totalDuration);
			remainingDuration = 0;
		} else {
			remainingDuration = totalDuration;
		}
		
		listener.modelUpdate(start, end, remainingDuration, isValid());
	}
	
	private boolean isValid() {
		for(TrackingEntryModel m : entries) {
			if(!m.isValid()) {
				return false;
			}
		}
		
		return remainingDuration == 0;
	}
	
	private static boolean isNullOrEmptyAfterTrim(String str) {
		return str == null || str.trim().isEmpty();
	}
	
	public static class TrackingEntryModel {
		private String activity;
		private String activityCategory;
		private long absoluteDuration;
		private DurationValue durationValue;
		private TrackingEntryModelListener listener;
		
		private final TrackingEntryEventModel eventModel;
		
		public interface TrackingEntryModelListener {
			public void absoluteDurationUpdated(long duration);
			public void durationValueUpdated(DurationValue dv);
		}
		
		private TrackingEntryModel(TrackingEntryEventModel eventModel, TrackingEntryModelListener listener) {
			this.eventModel = eventModel;
			this.listener = listener;
		}
		
		public static interface DurationValue {}
		
		public static class AbsoluteDuration implements DurationValue {
			private final long duration;
			public AbsoluteDuration(long duration) {this.duration = duration;}
			public long getDuration() { return duration; }
		}
		
		public static class PercentDuration implements DurationValue {
			private final long percent;
			public PercentDuration(long percent) {this.percent = percent;}
			public long getPercent() { return percent; }
		}
		
		public static class RemainingDuration implements DurationValue {}
		
		public DurationValue getDurationValue() {
			return this.durationValue;
		}
		
		private boolean isValid() {
			return getAbsoluteDuration() > 0 && !isNullOrEmptyAfterTrim(activity) && !isNullOrEmptyAfterTrim(activityCategory);
		}
		
		public void setDurationValue(DurationValue durationValue) {
			
			if(this.durationValue instanceof RemainingDuration && !(durationValue instanceof RemainingDuration)) {
				eventModel.setRemainingEntry(null);
			} else if(durationValue instanceof RemainingDuration) {
				eventModel.setRemainingEntry(this);
			}
			this.durationValue = durationValue;
			eventModel.recalculateEntries();
		}
		
		private void resetDurationValue() {
			durationValue = null;
			absoluteDuration = 0;
			listener.durationValueUpdated(null);
			listener.absoluteDurationUpdated(0);
		}
		
		public long getAbsoluteDuration() {
			return absoluteDuration;
		}
		
		private void setAbsoluteDuration(long duration) {
			this.absoluteDuration = duration;
			listener.absoluteDurationUpdated(duration);
		}

		public String getActivity() {
			return activity;
		}

		public void setActivity(String activity) {
			this.activity = activity;
		}

		public String getActivityCategory() {
			return activityCategory;
		}

		public void setActivityCategory(String activityCategory) {
			this.activityCategory = activityCategory;
		}
		
		
	}
}
