package com.kaaphi.timetracking.data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TrackingEntryDao {
	public void saveEntries(List<TrackingEntry> entries) throws TrackingDaoException;
	
	public List<TrackingEntry> loadEntriesByDate(Date date) throws TrackingDaoException;
	
	public TrackingEntry loadLastEntry() throws TrackingDaoException;
	
	public TrackingStatistics getTotalsForRange(Date start, Date end) throws TrackingDaoException;
	
	public Map<String,Set<String>> getRecentActivitiesAndCategories(Date since) throws TrackingDaoException;
}
