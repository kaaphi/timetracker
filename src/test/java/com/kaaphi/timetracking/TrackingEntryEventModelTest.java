package com.kaaphi.timetracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryEventModelListener;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.AbsoluteDuration;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.DurationValue;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.PercentDuration;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.RemainingDuration;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.TrackingEntryModelListener;

public class TrackingEntryEventModelTest {
	private TrackingEntryEventModel em;
	private long start = 0;
	private long end = 10000;
	private TrackingEntryModelListener listener = new TrackingEntryModelListener() {
		
		@Override
		public void durationValueUpdated(DurationValue dv) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void absoluteDurationUpdated(long duration) {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Before
	public void before() {
		em = new TrackingEntryEventModel(
				new TrackingEntryEventModelListener() {public void modelUpdate(Date start, Date end, long remainingDuration, boolean isValid) {}}, 
				new Date(start), new Date(end),
				Collections.<String, Set<String>>emptyMap());
	}
	
	@Test
	public void oneEmptyDuration() {
		TrackingEntryModel m = em.createEntryModel(listener);
		assertEquals(0, m.getAbsoluteDuration());
		assertEquals(10000, em.getRemainingDuration());
	}
	
	@Test
	public void oneAbsoluteDuration() {
		TrackingEntryModel m = em.createEntryModel(listener);
		m.setDurationValue(new AbsoluteDuration(1000));
		assertEquals(1000, m.getAbsoluteDuration());
		assertEquals(9000, em.getRemainingDuration());
	}
	
	@Test
	public void changeAbsoluteToPercent() {
		TrackingEntryModel m = em.createEntryModel(listener);
		m.setDurationValue(new AbsoluteDuration(1000));
		assertEquals(1000, m.getAbsoluteDuration());
		assertEquals(9000, em.getRemainingDuration());
		
		m.setDurationValue(new PercentDuration(50));
		assertEquals(5000, m.getAbsoluteDuration());
		assertEquals(5000, em.getRemainingDuration());
	}
	
	@Test
	public void onePercentDuration() {
		TrackingEntryModel m = em.createEntryModel(listener);
		m.setDurationValue(new PercentDuration(50));
		assertEquals(5000, m.getAbsoluteDuration());
		assertEquals(5000, em.getRemainingDuration());
	}
	
	@Test
	public void oneRemainingDuration() {
		TrackingEntryModel m = em.createEntryModel(listener);
		m.setDurationValue(new RemainingDuration());
		assertEquals(10000, m.getAbsoluteDuration());
		assertEquals(0, em.getRemainingDuration());
	}
	
	@Test
	public void twoAbsoluteDuration() {
		TrackingEntryModel m1 = em.createEntryModel(listener);
		m1.setDurationValue(new AbsoluteDuration(1000));
		assertEquals(1000, m1.getAbsoluteDuration());
		assertEquals(9000, em.getRemainingDuration());
		
		TrackingEntryModel m2 = em.createEntryModel(listener);
		m2.setDurationValue(new AbsoluteDuration(3000));
		assertEquals(1000, m1.getAbsoluteDuration());
		assertEquals(3000, m2.getAbsoluteDuration());
		assertEquals(6000, em.getRemainingDuration());
	}
	
	@Test
	public void twoPercentDuration() {
		TrackingEntryModel m1 = em.createEntryModel(listener);
		m1.setDurationValue(new PercentDuration(50));
		assertEquals(5000, m1.getAbsoluteDuration());
		assertEquals(5000, em.getRemainingDuration());
		
		TrackingEntryModel m2 = em.createEntryModel(listener);
		m2.setDurationValue(new PercentDuration(25));
		assertEquals(5000, m1.getAbsoluteDuration());
		assertEquals(2500, m2.getAbsoluteDuration());
		assertEquals(2500, em.getRemainingDuration());
	}
	
	@Test
	public void secondRemainingDuration() {
		TrackingEntryModel m1 = em.createEntryModel(listener);
		m1.setDurationValue(new RemainingDuration());
		assertEquals(10000, m1.getAbsoluteDuration());
		assertEquals(0, em.getRemainingDuration());
		
		TrackingEntryModel m2 = em.createEntryModel(listener);
		m2.setDurationValue(new RemainingDuration());
		assertNull(m1.getDurationValue());
		assertEquals(0, m1.getAbsoluteDuration());
		assertEquals(10000, m2.getAbsoluteDuration());
		assertEquals(0, em.getRemainingDuration());
	}
	
	@Test
	public void twoAbsoluteDurationWithPercent() {
		twoAbsoluteDuration();
		TrackingEntryModel m = em.createEntryModel(listener);
		m.setDurationValue(new PercentDuration(25));
		assertEquals(1500, m.getAbsoluteDuration());
		assertEquals(4500, em.getRemainingDuration());
	}
	
	@Test
	public void twoAbsoluteDurationWithRemaining() {
		twoAbsoluteDuration();
		TrackingEntryModel m = em.createEntryModel(listener);
		m.setDurationValue(new RemainingDuration());
		assertEquals(6000, m.getAbsoluteDuration());
		assertEquals(0, em.getRemainingDuration());
	}
	
	@Test
	public void twoAbsoluteDurationWithPercentAndRemaining() {
		twoAbsoluteDurationWithPercent();
		TrackingEntryModel m = em.createEntryModel(listener);
		m.setDurationValue(new RemainingDuration());
		assertEquals(4500, m.getAbsoluteDuration());
		assertEquals(0, em.getRemainingDuration());
	}
}
