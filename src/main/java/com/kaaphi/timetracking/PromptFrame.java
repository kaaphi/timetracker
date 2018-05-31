package com.kaaphi.timetracking;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

public class PromptFrame extends JFrame {
	public static enum PromptResponse {
		ENTER_TIME,
		IGNORE,
		SNOOZE
	}
	
	private JLabel enterTimeLabel;
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private volatile ScheduledFuture<?> blinkTask;
	private volatile CountDownLatch latch;
	private volatile PromptResponse returnValue;
	private BlinkingGlassPane glassPane;
	
	private static class BlinkingGlassPane extends JComponent {
		private JComponent parent;
		
		public BlinkingGlassPane(JComponent parent) {
			this.parent = parent;
		}
		
		protected void paintComponent(Graphics g) {
			g.setColor(parent.getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
	    }
		
		public void blink() {
			for(int i = 0; i < 20 && !Thread.interrupted(); i++) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setVisible(!isVisible());
					}
				});
				try {Thread.sleep(200);} catch (InterruptedException e) {}
			}
		}
	}
	
	public PromptFrame() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
		JButton enterTime = new JButton("Enter Time");
		JButton snooze = new JButton("Snooze");
		JButton ignore = new JButton("Ignore");
		buttonPanel.add(enterTime);
		buttonPanel.add(snooze);
		buttonPanel.add(ignore);
		
		enterTimeLabel = new JLabel("Time to enter time.");
		enterTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		ignore.addActionListener(new ButtonActionListener(PromptResponse.IGNORE));
		snooze.addActionListener(new ButtonActionListener(PromptResponse.SNOOZE));
		enterTime.addActionListener(new ButtonActionListener(PromptResponse.ENTER_TIME));
		
		panel.add(enterTimeLabel, BorderLayout.NORTH);
		panel.add(buttonPanel, BorderLayout.CENTER);
		
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		glassPane = new BlinkingGlassPane(panel);
		
		setGlassPane(glassPane);
		setAlwaysOnTop(true);
		getContentPane().add(panel);
		setUndecorated(true);
		pack();
	}
	
	private class ButtonActionListener implements ActionListener {
		private final PromptResponse response;
		
		public ButtonActionListener(PromptResponse response) {this.response = response;}
		
		public void actionPerformed(ActionEvent e) {
			doClose(response);
		}
	}
	
	public PromptResponse displayFrame() {
		latch = new CountDownLatch(1);
		blinkTask = executor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				glassPane.blink();
			}
		}, 30, 20, TimeUnit.SECONDS);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle b = gc.getBounds();
		setSize(300, 100);

		setLocation(b.x+b.width - getWidth(), b.y+b.height - getHeight());
		setFocusableWindowState(false);		
		setVisible(true);

		while(latch.getCount() > 0) {
			try {latch.await();} catch (InterruptedException e) {}
		}

		return returnValue;
	}
	
	private void doClose(PromptResponse value) {
		blinkTask.cancel(true);
		setVisible(false);
		returnValue = value;
		latch.countDown();
	}
}
