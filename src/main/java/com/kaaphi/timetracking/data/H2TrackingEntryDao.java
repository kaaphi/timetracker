package com.kaaphi.timetracking.data;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

public class H2TrackingEntryDao extends JdbcTrackingEntryDao {

	private static final String CREATE_TABLE = 
			"CREATE TABLE IF NOT EXISTS TimeTracking (" +
			"	id BIGINT AUTO_INCREMENT PRIMARY KEY," +
			"	startTime TIMESTAMP NOT NULL," +
			"	endTime TIMESTAMP NOT NULL," +
			"	category VARCHAR_IGNORECASE NOT NULL," +
			"	activity VARCHAR_IGNORECASE NOT NULL," +
			"	duration BIGINT NOT NULL" +
			")";
	
	
	public static TrackingEntryDao getDao(String dbPath) throws TrackingDaoException {
		 JdbcDataSource ds = new JdbcDataSource();
		 ds.setURL("jdbc:h2:" + dbPath);
		 ds.setUser("");
		 ds.setPassword("");		
		 
		 return new H2TrackingEntryDao(ds);
	}
	
	private H2TrackingEntryDao(DataSource ds) throws TrackingDaoException {
		super(ds);
		
		try(Connection conn = ds.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
			conn.setAutoCommit(true);
			stmt.executeUpdate();
		} catch (Throwable th) {
			throw new TrackingDaoException(th);
		}
	}
}
