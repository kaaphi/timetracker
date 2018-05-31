package com.kaaphi.timetracking;

import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import com.kaaphi.timetracking.util.TimeEvent;

public class UiUtil {
	private static final Logger log = Logger.getLogger(UiUtil.class); 
	private static final SimpleDateFormat hourMinuteDateFormat = new SimpleDateFormat("HH:mm");
	static {hourMinuteDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));}
	
	public static Date getDateInput(Component parent, List<TimeEvent> events) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		JComboBox<?> input = new JComboBox<>(new Vector<Object>(events));
		input.setEditable(true);
		int rv = JOptionPane.showConfirmDialog(parent, new Object[] {"Enter Time:", input}, "Enter Time", JOptionPane.OK_CANCEL_OPTION);
		if(rv == JOptionPane.OK_OPTION) {
			Object value = input.getSelectedItem();
			if(value != null) {
				if(value instanceof String) {
					try {
						return sdf.parse((String)value);
					} catch (ParseException e1) {
						log.error("Bad date!", e1);
					}
				} else if (value instanceof TimeEvent) {
					return new Date(((TimeEvent) value).time);
				}
			}
		}
		
		
		return null;
	}
	
	public static String formatEghtHourDaysDuration(long duration) {
		double days = duration / (double)TimeUnit.HOURS.toMillis(8);
		
		return String.format("%.1fd", days);
	}
	
	public static String formatHourMinuteDuration(long duration) {
		return String.format("%02d:%02d", duration/1000/60/60, (duration/1000/60) % 60);
		
		//return hourMinuteDateFormat.format(new Date(duration));
	}
	
	public static long parseHourMinuteDuration(String duration) throws ParseException {
		return hourMinuteDateFormat.parse(duration).getTime();
	}
	
	public static class SpecialComboBoxEditor extends BasicComboBoxEditor implements DocumentListener {
		private JComboBox<String> box;
		private volatile boolean isUpdating = false;
		
		public SpecialComboBoxEditor(JComboBox<String> box) {
			super();
			this.box = box;
			editor.getDocument().addDocumentListener(this);
		}
		
		private String findMatch(String pre) {
			if(pre.isEmpty()) return null;
			
			for(int i = 0; i < box.getModel().getSize(); i++) {
				String e = box.getModel().getElementAt(i);
				if(e.startsWith(pre) && !e.equals(pre)) {
					return e;
				}
			}
			
			return null;
		}
		
		private void doUpdate(Document d) {
			try {
			if(isUpdating) return;
			
				final String text = d.getText(0, d.getLength());
				final String match = findMatch(text);
				System.out.format("[%s] [%s]%n", text, match);
				if(match != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								isUpdating = true;
								int start = text.length();
								editor.setText(match);
								editor.setSelectionStart(start);
								editor.setSelectionEnd(match.length());
								isUpdating = false;
							} catch (Throwable th) {
								th.printStackTrace();
							} finally {
								isUpdating = false;
							}							
						}
					});
				}				
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}

		public void insertUpdate(DocumentEvent e) {
			doUpdate(e.getDocument());
		}

		public void removeUpdate(DocumentEvent e) {
			try {
				System.out.format("Remove: [%s]%n", e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}

		public void changedUpdate(DocumentEvent e) { 
			try {
				System.out.format("Changed: [%s]%n", e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (Throwable th) {
				th.printStackTrace();
			}			
		}
	}
	
	public static void main(String[] args) {
		System.out.println(formatHourMinuteDuration(TimeUnit.HOURS.toMillis(50) + TimeUnit.MINUTES.toMillis(35) + TimeUnit.SECONDS.toMillis(41)));
	}
}
