package com.kaaphi.timetracking;


import static com.kaaphi.timetracking.UiUtil.formatHourMinuteDuration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryEventModelListener;
import com.kaaphi.timetracking.TrackingEntryEventModel.TrackingEntryModel;
import com.kaaphi.timetracking.data.TrackingEntry;
import com.kaaphi.timetracking.util.TimeEvent;

public class TimeEntryEventEditor extends JDialog implements TrackingEntryEventModelListener {
	private static final Logger log = Logger.getLogger(TimeEntryEventEditor.class);
	
	private TrackingEntryEventModel model;
	
	private JPanel entryPanelsPanel;
	private List<TimeEntryPanel> entryPanels;
	private JButton addEntryPanelButton;
	private JLabel remainingDurationLabel;
	private JLabel dateRangeLabel;
	private JButton okButton;
	private volatile boolean isCanceled = false;
	private Date lastEntryEnd;

	public static EditResult showDialog(Date start, Date lastEntryEnd, Map<String, Set<String>> categoriesActivities) {
		TimeEntryEventEditor editor = new TimeEntryEventEditor(start, new Date(), lastEntryEnd, categoriesActivities);

		try {
			editor.setVisible(true);

			if(!editor.isCanceled) {
				return new EditResult(editor.getTrackingEntries(), editor.getStart(), editor.getEnd());
			} else {
				return null;
			}
		} finally {
			((DummyFrame)editor.getParent()).dispose();
			editor.dispose();
		}
	}
	
	public static class EditResult {
		private final List<TrackingEntry> entries;
		private final Date start;
		private final Date end;
		protected EditResult(List<TrackingEntry> entries, Date start, Date end) {
			super();
			this.entries = entries;
			this.start = start;
			this.end = end;
		}
		public List<TrackingEntry> getEntries() {
			return entries;
		}
		public Date getStart() {
			return start;
		}
		public Date getEnd() {
			return end;
		}
		
		
	}
	
	public TimeEntryEventEditor(Date start, Date end, Date lastEntryEnd, Map<String, Set<String>> categories) {
		super(new DummyFrame());
		this.lastEntryEnd = lastEntryEnd;
		
		createGui();
		
		entryPanels = new LinkedList<TimeEntryPanel>();
		model = new TrackingEntryEventModel(this, start, end, categories);
	}
	
	private void createGui() {
		setModal(true);
		setTitle("Enter Time");
		okButton = new JButton("OK");
		okButton.setEnabled(false);
		getContentPane().add(okButton, BorderLayout.SOUTH);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isCanceled = false;
				setVisible(false);
			}
		});
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isCanceled = true;
				setVisible(false);
			}
		});

		setLocationByPlatform(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		JPanel centerPanel = new JPanel(new BorderLayout());
		addEntryPanelButton = new JButton("Add");
		JButton updateEndTime = new JButton("Update Range End");
		JButton updateStartTime = new JButton(String.format("<html>Update Range Start<br>(%s)</html>", 
				lastEntryEnd == null ? "none" : new SimpleDateFormat("MM/dd HH:mm").format(lastEntryEnd)));
		updateStartTime.setEnabled(lastEntryEnd != null);
		JButton setStartTime = new JButton ("Set Range Start");
		JButton setEndTime = new JButton ("Set Range End");
		
		
		rightPanel.add(dateRangeLabel = new JLabel("00:00 - 00:00"));
		rightPanel.add(remainingDurationLabel = new JLabel("00:00"));
		rightPanel.add(addEntryPanelButton);
		rightPanel.add(updateEndTime);
		rightPanel.add(updateStartTime);
		rightPanel.add(setStartTime);
		rightPanel.add(setEndTime);
		
		addEntryPanelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNewEntryPanel();
			}
		});
		
		updateEndTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.setEnd(new Date());
			}
		});
		
		updateStartTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.setStart(lastEntryEnd);
			}
		});
		
		setStartTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<TimeEvent> evts = new LinkedList<>();

				evts.add(new TimeEvent(model.getStart().getTime(), ""));
				
				Date date = UiUtil.getDateInput(TimeEntryEventEditor.this, evts);
				
				if(date != null) {
					model.setStart(date);
				}
			}
		});
		
		setEndTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String time = JOptionPane.showInputDialog(TimeEntryEventEditor.this, "Enter time:", sdf.format(model.getStart()));
				if(time != null) {
					Date date;
					try {
						date = sdf.parse(time);
						model.setEnd(date);
					} catch (ParseException e1) {
						log.error("Bad date!", e1);
					}
				}
			}
		});
		
		
		entryPanelsPanel = new JPanel();
		entryPanelsPanel.setPreferredSize(new Dimension(500, 200));
		entryPanelsPanel.setLayout(new BoxLayout(entryPanelsPanel, BoxLayout.Y_AXIS));
		JScrollPane pane = new JScrollPane(entryPanelsPanel);
		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		centerPanel.add(pane);
		
		add(rightPanel, BorderLayout.EAST);
		add(centerPanel, BorderLayout.CENTER);
		pack();
	}
	
	private void addNewEntryPanel() {
		JButton removeButton = new JButton("-");
		final TimeEntryPanel panel = new TimeEntryPanel(model);
		
		final JPanel p = new JPanel();
		p.add(removeButton);
		p.add(panel);
		p.setMaximumSize(new Dimension(p.getMaximumSize().width, p.getMinimumSize().height));
		entryPanels.add(panel);
		entryPanelsPanel.add(p);
		revalidateEntryPanel();

		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.removeEntryModel(panel.getModel());
				entryPanels.remove(panel);
				entryPanelsPanel.remove(p);
				revalidateEntryPanel();
			}
		});
		
		panel.requestFocus();
	}
	
	private void revalidateEntryPanel() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				entryPanelsPanel.revalidate();
				entryPanelsPanel.repaint();
			}
		});
	}

	@Override
	public void modelUpdate(Date start, Date end, long remainingDuration, boolean isValid) {
		this.remainingDurationLabel.setText(formatHourMinuteDuration(remainingDuration));
		
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		this.dateRangeLabel.setText(String.format("%s - %s", format.format(start), format.format(end)));
		okButton.setEnabled(isValid);
	}
	
	public List<TrackingEntry> getTrackingEntries() {
		List<TrackingEntry> entries = new LinkedList<>();
		
		for(TimeEntryPanel p : entryPanels) {
			TrackingEntryModel m = p.getModel();
			entries.add(new TrackingEntry(model.getStart(), model.getEnd(), m.getAbsoluteDuration(), m.getActivity(), m.getActivityCategory()));
		}
		
		return entries;
	}
	
	public Date getStart() {
		return model.getStart();
	}
	
	public Date getEnd() {
		return model.getEnd();
	}
	
	private static class DummyFrame extends JFrame {
	    DummyFrame() {
	        super("Enter Time");
	        setUndecorated(true);
	        setVisible(true);
	        setLocationRelativeTo(null);
	        setIconImages(Arrays.asList(TimeIcon.INSTANCE.getImage()));
	    }
	}
	
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
			}
		});
		
		Map<String, Set<String>> categories = new LinkedHashMap<>();
		categories.put("c1", new LinkedHashSet<>(Arrays.asList(new String[] {"c1a1", "c1a2"})));
		categories.put("c2", new LinkedHashSet<>(Arrays.asList(new String[] {"c2a1"})));
		categories.put("c3", new LinkedHashSet<>(Arrays.asList(new String[] {"c3a1", "c3a2", "c3a3"})));
		
		
		System.out.println(showDialog(new Date(System.currentTimeMillis()-TimeUnit.MINUTES.toMillis(60)), new Date(System.currentTimeMillis()-TimeUnit.HOURS.toMillis(9)), categories));
	}
}
