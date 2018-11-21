package com.kaaphi.timetracking.data;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TextFileCategoryDefaultsDao implements CategoryDefaultsDao {
	private final Path categoryFile;
		
	public static CategoryDefaultsDao getDao(String path) throws TrackingDaoException {
		return new TextFileCategoryDefaultsDao(Paths.get(path));
	}
	
	public TextFileCategoryDefaultsDao(Path categoryFile) {
		this.categoryFile = categoryFile;
	}

	@Override
	public Set<String> getDefaultCategories() throws TrackingDaoException {
		try {
			if(Files.exists(categoryFile)) {
				return Files.lines(categoryFile, Charset.forName("UTF-8"))
						.sequential()
						.collect(Collectors.toCollection(LinkedHashSet::new));
			} else {
				return Collections.emptySet();
			}
		} catch (IOException e) {
			throw new TrackingDaoException(e);
		}
	}

	@Override
	public void setDefaultCategories(Set<String> categories) throws TrackingDaoException {
		try {
			Files.write(categoryFile, categories, Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new TrackingDaoException(e);
		}		
	}

	
}
