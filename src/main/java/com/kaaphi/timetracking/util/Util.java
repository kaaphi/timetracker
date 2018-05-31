package com.kaaphi.timetracking.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Util {

	final static int[] FIELDS = {Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
	final static int HOUR = 0;
	final static int MINUTE = 1;
	final static int SECOND = 2;
	public static long timeToNext(long date, int nearest, int field) {
		Calendar c = GregorianCalendar.getInstance();
		c.setTimeInMillis(date);
		int fieldVal = c.get(FIELDS[field]);
		c.set(FIELDS[field], (fieldVal/nearest)*nearest);
		c.add(FIELDS[field], nearest);

		for(int i = field+1; i < FIELDS.length; i++) {
			c.set(FIELDS[i], 0);
		}
		
		return c.getTimeInMillis() - date;
	}
	
	public static void main(String[] args) {
		long toNext = timeToNext(System.currentTimeMillis(), 1, 0);
		System.out.println(toNext);
		System.out.println(toNext/1000);
		System.out.println(toNext/1000/60);
	}

}
