package com.kaaphi.timetracking;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.kaaphi.timetracking.data.TrackingStatistics;
import com.kaaphi.timetracking.data.TrackingStatistics.Category;

public class StatisticsViewer extends JPanel {
	//private Map<Date, TrackingStatistics> stats;
	private JTextArea viewer;
	
	public static void showSummary(TrackingStatistics stats) {
		JFrame frame = new JFrame("Stats");
		
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(new StatisticsViewer(stats));
		frame.pack();
		frame.setSize(new Dimension(frame.getWidth()+35, 550));
		frame.setVisible(true);
	}
	
	public static void show(Map<Date, TrackingStatistics> stats) {
		JFrame frame = new JFrame("Stats");
		
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(new StatisticsViewer(stats));
		frame.pack();
		frame.setSize(new Dimension(frame.getWidth()+35, 550));
		frame.setVisible(true);
	}
		
	public StatisticsViewer(TrackingStatistics stats) {
		this();
		viewer.setText(makeDaySummary(stats));
	}
	
	public StatisticsViewer(Map<Date, TrackingStatistics> stats) {
		this();
		viewer.setText(makeStatString(stats));
	}
	
	public StatisticsViewer() {
		super(new BorderLayout());
		
		viewer = new JTextArea();
		viewer.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		viewer.setEditable(false);
		
		add(new JScrollPane(viewer), BorderLayout.CENTER);
	}
	
	private static String makeDaySummary(TrackingStatistics sum) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Totals for Range (total days: %s)%n%n", UiUtil.formatEghtHourDaysDuration(sum.getTotalDuration())));
		for(Category c : sum.getCategories().values()) {
			sb.append(String.format("%-30s~%s%n", c.getCategory()+"~", UiUtil.formatHourMinuteDuration(c.getTotalDuration())).replace(' ', '.').replace('~', ' '));
			for(Entry<String, Long> activityEntry : c.getActivities().entrySet()) {
				sb.append(String.format("~~%-28s~%s%n", activityEntry.getKey()+"~", UiUtil.formatHourMinuteDuration(activityEntry.getValue())).replace(' ', '.').replace('~', ' '));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private static String makeStatString(Map<Date, TrackingStatistics> statsMap) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder sb = new StringBuilder();

		long total = 0;
		for(Map.Entry<Date, TrackingStatistics> e : statsMap.entrySet()) {
			sb.append("===============\n\n");
			TrackingStatistics stats = e.getValue();
			sb.append(String.format("%s TOTAL: %s%n%n", format.format(e.getKey()), UiUtil.formatHourMinuteDuration(stats.getTotalDuration())));

			total += stats.getTotalDuration();
			for(Category c : stats.getCategories().values()) {
				sb.append(String.format("%-30s~%s%n", c.getCategory()+"~", UiUtil.formatHourMinuteDuration(c.getTotalDuration())).replace(' ', '.').replace('~', ' '));
				for(Entry<String, Long> activityEntry : c.getActivities().entrySet()) {
					sb.append(String.format("~~%-28s~%s%n", activityEntry.getKey()+"~", UiUtil.formatHourMinuteDuration(activityEntry.getValue())).replace(' ', '.').replace('~', ' '));
				}
				sb.append("\n");
			}
		}
		
		sb.insert(0, String.format("TOTAL: %s%n%n", UiUtil.formatHourMinuteDuration(total)));
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Map<String, Category> categories = new LinkedHashMap<>();
		
		Map<String, Long> activities = new LinkedHashMap<>();
		activities.put("a1", 60000L);
		activities.put("a2", 1200000L);
		activities.put("a3", 1200000L);
		categories.put("c1", new Category("c1", activities));
		
		Map<Date, TrackingStatistics> map = new HashMap<Date, TrackingStatistics>();
		map.put(new Date(), new TrackingStatistics(1301200L*10L, categories));
		show(map);
	}
}
