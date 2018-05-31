package com.kaaphi.timetracking.util;

import java.util.Date;

import javax.swing.JFrame;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.Wtsapi32;
import com.sun.jna.win32.W32APIOptions;

public class LockUnlockListener {

	
	
	private static MyUser32 USER_INSTANCE = (MyUser32) Native.loadLibrary("user32", MyUser32.class, W32APIOptions.UNICODE_OPTIONS);
	
	private static interface MyUser32 extends User32 {
		public int SetWindowLong(WinDef.HWND hWnd, int nIndex, WinUser.WindowProc callback);
	}
	
	
	public LockUnlockListener() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setVisible(true);

		HWND hwnd = new HWND();
		hwnd.setPointer(Native.getWindowPointer(frame));

		Wtsapi32.INSTANCE.WTSRegisterSessionNotification(hwnd, Wtsapi32.NOTIFY_FOR_ALL_SESSIONS);
		
		WinUser.WindowProc listener = new WinUser.WindowProc()
		{
			@Override
			public LRESULT callback(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam)
			{
				if (uMsg == WinUser.WM_SESSION_CHANGE)
				{
					switch (wParam.intValue())
					{
						case Wtsapi32.WTS_SESSION_LOCK:
							System.out.println("Locked " + new Date());
						break;
						
						case Wtsapi32.WTS_SESSION_UNLOCK:
							System.out.println("Unlocked "  + new Date());
						break;
					}
				}
				return User32.INSTANCE.DefWindowProc(hWnd, uMsg, wParam, lParam);
			}
		};
		
		USER_INSTANCE.SetWindowLong(hwnd, User32.GWL_WNDPROC, listener);

	}

}
