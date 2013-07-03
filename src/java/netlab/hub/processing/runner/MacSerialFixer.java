/*
Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org

Copyright (c) 2006-2013 Ewan Branda

NETLab Hub is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

NETLab Hub is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
*/

package netlab.hub.processing.runner;

import java.io.File;

import javax.swing.JOptionPane;

import netlab.hub.util.Logger;
import processing.core.PApplet;

/**
 * Detect and repair missing serial port lock file directory on
 * OS X. Based on the SerialFixer class in Processing.
 * See https://github.com/processing/processing/blob/master/app/src/processing/app/tools/SerialFixer.java
 */
public class MacSerialFixer {

	static public boolean isNeeded() {
		try {
			if (isMacOS()) {
				File lockFolder = new File("/var/lock");
				if (!lockFolder.exists() || !lockFolder.canRead()
						|| !lockFolder.canWrite() || !lockFolder.canExecute()) {
					return true;
				}
			}
		} catch (Throwable t) {
			Logger.error("Error checking serial port condition", t);
		}
		return false;
	}
	
	static public boolean isMacOS() {
		// Base.isMacOS() fails with error when running
		// in standalone application mode, so just do the
		// platform check here.
		return System.getProperty("os.name").indexOf("Mac") != -1;
	}
	
	
	PApplet parent;

	public MacSerialFixer(PApplet parent) {
		this.parent = parent;
	}

	public void run() {
		if (isMacOS()) {
			if (confirmFix()) {
				doFix();
			}
		}
	}
	
	public boolean confirmFix() {
		final String primary = "Serial port configuration problem";
		final String secondary = "The NETLab Hub needs to make a one-time adjustment to your Mac's serial port.<br />"+
									"Click ÒOKÓ to perform additional installation steps to enable the Serial library.<br />"+
									"An administrator password will be required.<br /><br />"+
									"Note: You will need to connect again after doing this.";
		int result = JOptionPane.showConfirmDialog(parent, "<html> "
				+ "<head> <style type=\"text/css\">"
				+ "b { font: 13pt \"Lucida Grande\" }"
				+ "p { font: 11pt \"Lucida Grande\"; margin-top: 8px }"
				+ "</style> </head>" + "<b>" + primary + "</b>" + "<p>"
				+ secondary + "</p>", "Commander",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		return result == JOptionPane.OK_OPTION;
	}
	
	public void doFix() {
		String shellScript = "/bin/chmod 777 /var/lock";
		File lockFolder = new File("/var/lock");
		if (!lockFolder.exists()) {
			shellScript = "/bin/mkdir -p /var/lock && "+shellScript;
		}
		String appleScript = "do shell script \"" + shellScript
				+ "\" with administrator privileges";
		
		// The approved Java 6 method for invoking a script is:
	    //ScriptEngineManager mgr = new ScriptEngineManager();
	    //ScriptEngine engine = mgr.getEngineByName("AppleScript");
		//engine.eval(appleScript);
		
	    try {
			Logger.debug("Running AppleScript ["+appleScript+"]");
			PApplet.exec(new String[] { "osascript", "-e", appleScript });	
		} catch (Throwable e) {
			Logger.error("Error running AppleScript ["+appleScript+"]", e);
		}
	}
}