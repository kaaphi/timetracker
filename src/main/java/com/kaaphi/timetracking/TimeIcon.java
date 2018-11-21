package com.kaaphi.timetracking;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

public class TimeIcon {
	private static final Logger log = Logger.getLogger(TimeIcon.class);
	
	public static final TimeIcon INSTANCE = new TimeIcon();
	
	private ImageIcon imageIcon;
	
	private TimeIcon() {
		this.imageIcon = loadIcon();
	}
	
	public ImageIcon getImageIcon() {
		return imageIcon;
	}
	
	public Image getImage() {
		return imageIcon.getImage();
	}
	
	private static ImageIcon loadIcon() {
	    InputStream in = null;
	    try {
	        in = ClassLoader.getSystemResourceAsStream("clock.png");
            int len = in.available();
            byte[] bytes = new byte[len];
            in.read(bytes, 0, len);
            in.close();
            return new ImageIcon(bytes);
	    } catch (Throwable th) {
	        log.error("Failed to load icon.", th);
	        throw new Error(th);
	    } finally {
	        try {
	            if(in != null) in.close();
	        } catch (IOException e) {
	            log.error("Failed to close icon input stream.", e);
	        }
	    }
	}
}
