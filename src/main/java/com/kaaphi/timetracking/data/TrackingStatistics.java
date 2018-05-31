package com.kaaphi.timetracking.data;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TrackingStatistics {
	private final Map<String, Category> categories;
	private final long totalDuration;
	
	public static TrackingStatistics sumStats(Map<Date, TrackingStatistics> statMap) {
		long totalDuration = 0;
		Map<String, Map<String, Long>> categories = new HashMap<String, Map<String,Long>>();
		for(TrackingStatistics stat : statMap.values()) {
			totalDuration += stat.getTotalDuration();
			Map<String, Category> cats = stat.getCategories();
			for(Category c : cats.values()) {
				Map<String, Long> activites = categories.get(c.getCategory());
				if(activites == null) {
					categories.put(c.getCategory(), activites = new HashMap<>());
				}
				
				for(Map.Entry<String, Long> e : c.getActivities().entrySet()) {
					Long l = activites.get(e.getKey());
					if(l == null) {
						l = Long.valueOf(0);
					}
					activites.put(e.getKey(),l += e.getValue());
				}
			}
		}
		
		Map<String, Category> cats = new HashMap<>();
		for(Entry<String, Map<String, Long>> e : categories.entrySet()) {
			Category c = new Category(e.getKey(), e.getValue());
			cats.put(e.getKey(), c);
		}
		
		return new TrackingStatistics(totalDuration, cats);
	}
	
	public TrackingStatistics(long totalDuration, Map<String, Category> categories) {
		this.categories = categories;
		this.totalDuration = totalDuration;
	}
	
	public Map<String, Category> getCategories() {
		return Collections.unmodifiableMap(categories);
	}
	
	public long getTotalDuration() {
		return totalDuration;
	}
	
	public static class Category {
		private String category;
		private long totalDuration;
		private Map<String, Long> activityDurations;
		
		public Category(String category, Map<String, Long> activityDurations) {
			super();
			this.category = category;
			this.activityDurations = activityDurations;
			updateTotalDuration();
		}
		
		public String getCategory() {
			return category;
		}

		public long getTotalDuration() {
			return totalDuration;
		}
		
		private void updateTotalDuration() {
			long total = 0;
			for(Long l : activityDurations.values()) {
				total += l;
			}
			this.totalDuration = total;
		}
		
		public Map<String, Long> getActivities() {
			return Collections.unmodifiableMap(activityDurations);
		}
	
	}
}
