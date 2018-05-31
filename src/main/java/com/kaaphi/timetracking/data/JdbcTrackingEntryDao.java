package com.kaaphi.timetracking.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.kaaphi.timetracking.data.TrackingStatistics.Category;

public class JdbcTrackingEntryDao implements TrackingEntryDao {
	private static final String INSERT_ENTRY = 
			"INSERT INTO TimeTracking (startTime, endTime, category, activity, duration) " +
			"VALUES (?, ?, ?, ?, ?)";
	
	private static final String GET_ACTIVITIES_AND_CATEGORIES = 
			"SELECT CATEGORY, ACTIVITY FROM TIMETRACKING WHERE STARTTIME >= ? GROUP BY CATEGORY, ACTIVITY";
	
	private static final String GET_LAST_ENTRY = 
			"SELECT TOP 1 startTime, endTime, category, activity, duration FROM TimeTracking ORDER BY endTime DESC, id DESC";
	
	private static final String GET_TOTALS = 
			"SELECT category, activity, SUM(duration) FROM TimeTracking WHERE startTime >= ? AND startTime < ? GROUP BY category, activity ORDER BY category, activity";
	
	private final DataSource ds;
		
	public JdbcTrackingEntryDao(DataSource ds) {
		this.ds = ds;
	}
	
	@Override
	public void saveEntries(List<TrackingEntry> entries)
			throws TrackingDaoException {
		try(Connection conn = ds.getConnection(); PreparedStatement stmt = conn.prepareStatement(INSERT_ENTRY)) {
			try {
				conn.setAutoCommit(false);

				for(TrackingEntry e : entries) {
					stmt.setTimestamp(1, new Timestamp(e.getStartDate().getTime()));
					stmt.setTimestamp(2, new Timestamp(e.getEndDate().getTime()));
					stmt.setString(3, e.getCategory());
					stmt.setString(4, e.getActivity());
					stmt.setLong(5, e.getDuration());
					
					stmt.executeUpdate();
				}
				
				conn.commit();
			} catch (Throwable th) {
				conn.rollback();
				throw new TrackingDaoException(th);
			}
		} catch (TrackingDaoException e) {
			throw e;
		} catch (Throwable th) {
			throw new TrackingDaoException(th);
		}
	}

	@Override
	public List<TrackingEntry> loadEntriesByDate(Date date)
			throws TrackingDaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String,Set<String>> getRecentActivitiesAndCategories(Date since)
			throws TrackingDaoException {
		try(
				Connection conn = ds.getConnection(); 
				PreparedStatement stmt = conn.prepareStatement(GET_ACTIVITIES_AND_CATEGORIES); 
				) {
			stmt.setTimestamp(1, new Timestamp(since.getTime()));
			ResultSet rs = stmt.executeQuery();
			Map<String, Set<String>> categories = new LinkedHashMap<>();
			while(rs.next()) {
				String c = rs.getString(1);
				String a = rs.getString(2);
				Set<String> as = categories.get(c);
				if(as == null) {
					categories.put(c, as = new LinkedHashSet<>());
				}
				as.add(a);
			}
			return categories;
		} catch (Throwable th) {
			throw new TrackingDaoException(th);
		}
	}

	@Override
	public TrackingEntry loadLastEntry() throws TrackingDaoException {
		try(
				Connection conn = ds.getConnection(); 
				PreparedStatement stmt = conn.prepareStatement(GET_LAST_ENTRY); 
				) {
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return new TrackingEntry(rs.getTimestamp(1), rs.getTimestamp(2), rs.getLong(5), rs.getString(4), rs.getString(3));
			} else {
				return null;
			}
		} catch (Throwable th) {
			throw new TrackingDaoException(th);
		}
	}

	@Override
	public TrackingStatistics getTotalsForRange(Date start, Date end) throws TrackingDaoException {
		try(
				Connection conn = ds.getConnection(); 
				PreparedStatement stmt = conn.prepareStatement(GET_TOTALS); 
				) {
			long totalDuration = 0;
			
			stmt.setTimestamp(1, new Timestamp(start.getTime()));
			stmt.setTimestamp(2, new Timestamp(end.getTime()));
			ResultSet rs = stmt.executeQuery();
			Map<String, Category> categories = new LinkedHashMap<>();
			
			String currentCategory = null;
			Map<String, Long> currentActivities = new LinkedHashMap<>();
			long categoryTotal = 0;
			while(rs.next()) {
				
				String category = rs.getString(1);
				String activity = rs.getString(2);
				long duration = rs.getLong(3);

				if(currentCategory == null) {
					currentCategory = category;
				}
				
				if(!category.equals(currentCategory)) {
					categories.put(currentCategory, new Category(currentCategory, currentActivities));
					currentCategory = category;
					currentActivities = new LinkedHashMap<>();
					categoryTotal = 0;
				}
				
				currentActivities.put(activity, duration);
				categoryTotal += duration;
				totalDuration += duration;
			}
			
			categories.put(currentCategory, new Category(currentCategory, currentActivities));
			
			return new TrackingStatistics(totalDuration, categories);
		} catch (Throwable th) {
			throw new TrackingDaoException(th);
		}
	}

}
