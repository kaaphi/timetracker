package com.kaaphi.timetracking.data;

import java.util.Set;

public interface CategoryDefaultsDao {
	public Set<String> getDefaultCategories() throws TrackingDaoException;
	public void setDefaultCategories(Set<String> categories) throws TrackingDaoException;
}
