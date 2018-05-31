package com.kaaphi.timetracking;

import static com.kaaphi.timetracking.UiUtil.formatHourMinuteDuration;
import static com.kaaphi.timetracking.UiUtil.parseHourMinuteDuration;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryEventModelListener;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.AbsoluteDuration;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.DurationValue;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.PercentDuration;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.RemainingDuration;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel.TrackingEntryModelListener;
import com.kaaphi.timetracking.UiUtil.SpecialComboBoxEditor;

public class TimeEntryPanel extends JPanel implements TrackingEntryModelListener {
	private TrackingEntryModel model;
	private JLabel absoluteDuration;
	private JTextField durationEntry;
	private JComboBox<String> category;
	private JComboBox<String> activity;
	private Map<String, Set<String>> availableCategories;
	
	private static final TimeEntryType[] entryTypes = new TimeEntryType[] {
			new TimeEntryType("[Rr]") {
				protected DurationValue createValue(Matcher m) {
					return new RemainingDuration();
				}
			},
			new TimeEntryType("(\\d+)%") {
				protected DurationValue createValue(Matcher m) throws Exception {
					long l = Long.parseLong(m.group(1));
					return new PercentDuration(l);
				}
			},
			new TimeEntryType("\\d+:\\d\\d") {
				protected DurationValue createValue(Matcher m) throws Exception {
					long millis = parseHourMinuteDuration(m.group());
					return new AbsoluteDuration(millis);
				}
			},
			new TimeEntryType("(\\d+)([hm]?)") {
				protected DurationValue createValue(Matcher m) throws Exception {
					long value = Long.parseLong(m.group(1));
					TimeUnit unit = "h".equals(m.group(2)) ? TimeUnit.HOURS: TimeUnit.MINUTES;
					return new AbsoluteDuration(unit.toMillis(value));
				}
			}
	};
	
	private static abstract class TimeEntryType {
		private Pattern pattern;
		
		public TimeEntryType(String pattern) {
			this.pattern = Pattern.compile(pattern);
		}
		
		public DurationValue getValue(String text) {
			Matcher m = pattern.matcher(text);
			if(m.matches()) {
				try {
					return createValue(m);
				} catch (Throwable th) {
					return null;
				}
			} else {
				return null;
			}
		}
		
		protected abstract DurationValue createValue(Matcher m) throws Exception;
	}
	
	public TimeEntryPanel(TrackingEntryEventModel eventModel) {
		this.model = eventModel.createEntryModel(this);
		absoluteDuration = new JLabel("00:00");
		durationEntry = new JTextField(5);
		availableCategories = eventModel.getAvailableCategories();
		category = new JComboBox<String>(availableCategories.keySet().toArray(new String[0]));
		category.setEditor(new SpecialComboBoxEditor(category));
		category.setEditable(true);
		activity = new JComboBox<String>();
		activity.setEditor(new SpecialComboBoxEditor(activity));
		activity.setEditable(true);
		category.setSelectedItem(null);
		
		category.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					model.setActivityCategory((String)e.getItem());
					Set<String> activities = availableCategories.get(e.getItem());
					String txt = (String) activity.getSelectedItem();
					if(activities != null) {
						activity.setModel(new DefaultComboBoxModel<>(activities.toArray(new String[0])));
						activity.setSelectedItem(txt);
					} else {
						activity.setModel(new DefaultComboBoxModel<String>());
					}
				}
			}
		});
		
		activity.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					model.setActivity((String)e.getItem());
				}
			}
		});
		
		durationEntry.setInputVerifier(new InputVerifier() {
			
			@Override
			public boolean verify(JComponent input) {
				String text = ((JTextField)input).getText();
				try {
					if(text.isEmpty()) {
						model.setDurationValue(null);
						return true;
					}
					
					boolean rv = false;
					for(TimeEntryType entryType : entryTypes) {
						DurationValue dv = entryType.getValue(text);
						if(dv != null) {
							model.setDurationValue(dv);
							rv = true;
							break;
						}
					}
					
					return rv;
				} catch (Throwable th) {
					th.printStackTrace();
					return false;
				}
			}
		});
		
		add(category);
		add(activity);
		add(durationEntry);
		add(absoluteDuration);
		
		addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				category.requestFocus();
			}
		});
	}

	public TrackingEntryModel getModel() {
		return model;
	}
	
	@Override
	public void absoluteDurationUpdated(long duration) {
		if(absoluteDuration != null) {
			absoluteDuration.setText(formatHourMinuteDuration(duration));
		}
	}

	@Override
	public void durationValueUpdated(DurationValue dv) {
		if(dv == null) {
			durationEntry.setText("");
		}
	}
		
	public static void main(String[] args) {
		JFrame f = new JFrame("Test");
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		long end = System.currentTimeMillis();
		TrackingEntryEventModel model = new TrackingEntryEventModel(new TrackingEntryEventModelListener() {public void modelUpdate(Date start, Date end, long remainingDuration, boolean isValid) {}},
			new Date(end-TimeUnit.MINUTES.toMillis(60)), new Date(end), Collections.<String, Set<String>>emptyMap());
		
		
		p.add(new TimeEntryPanel(model));
		p.add(new TimeEntryPanel(model));
		
		f.getContentPane().add(p);
		
		f.setLocationByPlatform(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
}
