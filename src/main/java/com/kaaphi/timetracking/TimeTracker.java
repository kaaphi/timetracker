package com.kaaphi.timetracking;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.kaaphi.timetracking.PromptFrame.PromptResponse;
import com.kaaphi.timetracking.TimeEntryEventEditor.EditResult;
import com.kaaphi.timetracking.data.H2TrackingEntryDao;
import com.kaaphi.timetracking.data.TrackingEntry;
import com.kaaphi.timetracking.data.TrackingEntryDao;
import com.kaaphi.timetracking.data.TrackingStatistics;


public class TimeTracker implements Runnable {
	private static final Logger log = Logger.getLogger(TimeTracker.class);
	
	private static final int INTERVAL = Integer.parseInt(System.getProperty("time.tracking.interval", "1"));
	private static final int INTERVAL_FIELD = Integer.parseInt(System.getProperty("time.tracking.interval.field", "0"));
	
	private PromptFrame promptFrame;
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private TrackingEntryDao dao;
	private Date start;
	
	public TimeTracker(TrackingEntryDao dao) throws Exception {
		this.dao = dao;
		TrayIcon icon = new TrayIcon(TimeIcon.INSTANCE.getImage());
		
		PopupMenu popup = new PopupMenu();
		MenuItem enterTime = new MenuItem("Enter Time Now");
		enterTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doEdit();
			}
		});
		
		MenuItem showStats = new MenuItem("Show Stats");
		showStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStats();
			}
		});
		
		MenuItem showLastWeekStats = new MenuItem("Show Last Week Stats");
		showLastWeekStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showLastWeekStats();
			}
		});
		
		MenuItem showWeekStats = new MenuItem("Show Stats for Week");
		showWeekStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStatsForWeek();
			}
		});
		
		MenuItem showStatsForRange = new MenuItem("Show Stats For Range");
		showStatsForRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStatsForRange();
			}
		});
		
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Really exit?", "Exit?", JOptionPane.YES_NO_OPTION)) {
					System.exit(0);					
				}				
			}
		});
		
		popup.add(enterTime);
		popup.add(showStats);
		popup.add(showLastWeekStats);
		popup.add(showWeekStats);
		popup.add(showStatsForRange);
		popup.add(exit);
		
		icon.setPopupMenu(popup);
		icon.setImageAutoSize(false);
		
		SystemTray tray = SystemTray.getSystemTray();
		
		tray.add(icon);
		
		promptFrame = new PromptFrame();
	}
	
	
	public void go() throws Exception {
		log.debug("Running.");
		start = new Date();
		executor.schedule(this, timeToNext(System.currentTimeMillis(), INTERVAL, INTERVAL_FIELD), TimeUnit.MILLISECONDS);
	}
	
	public void run() {
		if(!promptFrame.isVisible()) {
			PromptResponse resp = promptFrame.displayFrame();
			
			if(resp == PromptResponse.ENTER_TIME && doEdit()) {
				executor.schedule(this, timeToNext(System.currentTimeMillis(), INTERVAL, INTERVAL_FIELD), TimeUnit.MILLISECONDS);
			} else if (resp == PromptResponse.SNOOZE) {
				executor.schedule(this, 5, TimeUnit.MINUTES);
			} else {
				//ignore and prompt at next interval
				executor.schedule(this, timeToNext(System.currentTimeMillis(), INTERVAL, INTERVAL_FIELD), TimeUnit.MILLISECONDS);
			}
		}
	}

	private boolean doEdit() {
		try {
			TrackingEntry entry = dao.loadLastEntry();			
			EditResult result = TimeEntryEventEditor.showDialog(start, entry != null ? entry.getEndDate() : null, dao.getRecentActivitiesAndCategories(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5))));
			if(result != null) {
				log.debug("Before save.");
				dao.saveEntries(result.getEntries());
				log.debug("After save.");
				start = result.getEnd();
				return true;
			} else {
				log.debug("canceled");
				return false;
			}			
		} catch (Throwable th) {
			log.error("Failed to save!", th);
			return false;
		}
	}
	
	private void showStats() {
		showStats(GregorianCalendar.getInstance());
	}
	
	private void showStatsForWeek() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		JTextField date = new JTextField(formatter.format(new Date()));
		int option = JOptionPane.showConfirmDialog(null, new Object[] {date}, "Enter Dates", JOptionPane.PLAIN_MESSAGE);
		if(option == JOptionPane.OK_OPTION) {
			try {
				Date endDate = formatter.parse(date.getText());

				Calendar c = GregorianCalendar.getInstance();
				c.setTime(endDate);
				showStats(c);
			} catch (Throwable th) {
				log.error("Failed to load stats!", th);
			}
		}
	}
	
	private void showLastWeekStats() {
		Calendar c = GregorianCalendar.getInstance();
		
		c.add(Calendar.WEEK_OF_YEAR, -1);
		c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		
		showStats(c);
	}

	private void showStatsForRange() {
		Date defaultEnd = new Date();
		Date defaultStart = new Date(defaultEnd.getTime() - TimeUnit.DAYS.toMillis(13));

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		JTextField start = new JTextField(formatter.format(defaultStart));
		JTextField end = new JTextField(formatter.format(defaultEnd));
		int option = JOptionPane.showConfirmDialog(null, new Object[] {start, end}, "Enter Start and End Dates", JOptionPane.PLAIN_MESSAGE);
		if(option == JOptionPane.OK_OPTION) {
			try {
				Date startDate = formatter.parse(start.getText());
				Date endDate = formatter.parse(end.getText());

				StatisticsViewer.showSummary(dao.getTotalsForRange(startDate, endDate));
			} catch (Throwable th) {
				log.error("Failed to load stats!", th);
			}
		}
	}
	
	private void showStats(Calendar c) {
		try {
			int currentDay = c.get(Calendar.DAY_OF_WEEK);
			
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			c.add(Calendar.WEEK_OF_YEAR, -1);
			
			Map<Date, TrackingStatistics> map = new LinkedHashMap<>();
			
			int startDay;
			do {
				startDay = c.get(Calendar.DAY_OF_WEEK);
				Date start = c.getTime();
				c.add(Calendar.DAY_OF_MONTH, 1);
				Date end = c.getTime();
				
				TrackingStatistics stats = dao.getTotalsForRange(start, end);
				map.put(start, stats);
			} while (startDay != currentDay);
			

			StatisticsViewer.show(map);
		} catch (Throwable th) {
			log.error("Failed to load stats!", th);
		}
	}

	final Calendar c = GregorianCalendar.getInstance();
	final static int[] FIELDS = {Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
	final static int HOUR = 0;
	final static int MINUTE = 1;
	final static int SECOND = 2;
	private long timeToNext(long date, int nearest, int field) {
		c.setTimeInMillis(date);
		int fieldVal = c.get(FIELDS[field]);
		c.set(FIELDS[field], (fieldVal/nearest)*nearest);
		c.add(FIELDS[field], nearest);

		for(int i = field+1; i < FIELDS.length; i++) {
			c.set(FIELDS[i], 0);
		}
		
		return c.getTimeInMillis() - date;
	}

	public static void main(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				log.error("Uncaught Exception!", e);
			}
		});
		
		String db;
		if(args.length > 0) {
			db = args[0];
		} else {
			db = "time_tracking_db";
		}
		
		TrackingEntryDao dao = H2TrackingEntryDao.getDao(db);
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		TimeTracker t = new TimeTracker(dao);
		
		t.go();
	}
}
