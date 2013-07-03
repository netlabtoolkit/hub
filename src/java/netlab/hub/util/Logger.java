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

package netlab.hub.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;

// TODO if this is running on a remote spot then use remote logger?

/**
 * @author ebranda
 */
public class Logger {
	
	private static boolean showDebug = false;
	private static Level originalLevel = null;
	private static boolean configured = false;
	
	public static void switchDebugLevel() {
		if (originalLevel == null) {
			originalLevel = org.apache.log4j.Logger.getLogger("ROOT").getEffectiveLevel();
		}
		if (originalLevel.equals(Level.DEBUG)) {
			info("Default Log4j level already set to DEBUG");
		} else {
			info("Turning debug logging "+(showDebug?"off":"on"));
		}
		showDebug = !showDebug;
		org.apache.log4j.Logger.getLogger("ROOT").setLevel(showDebug?Level.DEBUG:originalLevel);
	}
	
	/*
	private static List outputWriters = new ArrayList();
	
	public static void addOutputMessageWriter(OutputMessageWriter w) {
		outputWriters.add(w);
	}
	*/
	
	public static boolean isDebug() {
		return showDebug;
	}

	public static void configure(URL configFile)
	throws IOException {
		Properties log4jProps = new Properties();
		InputStream log4jConfigData = configFile.openStream();
		log4jProps.load(log4jConfigData);
		log4jConfigData.close();
		configure(log4jProps);
	}

	public static void configure(File configFile)
	throws IOException {
		Properties log4jProps = new Properties();
		InputStream log4jConfigData = new FileInputStream(configFile);
		log4jProps.load(log4jConfigData);
		log4jConfigData.close();
		configure(log4jProps);
	}
	
	private static void configure(Properties log4jProps) {
		PropertyConfigurator.configure(log4jProps);
		configured = true;
	}
	
	public static void debug(Throwable t) {
		if (!configured) return;
		org.apache.log4j.Logger.getLogger("ROOT").error(getStackTrace(t));
	}
	
	public static void debug(Object arg) {
		if (!configured) return;
		org.apache.log4j.Logger.getLogger("ROOT").debug(arg);
	}
	
	public static void debug(Object arg, Throwable t) {
		if (!configured) return;
		org.apache.log4j.Logger.getLogger("ROOT").debug(arg);
		org.apache.log4j.Logger.getLogger("ROOT").error(getStackTrace(t));
	}
	
	public static void info(Object arg) {
		if (!configured) return;
		org.apache.log4j.Logger.getLogger("ROOT").info(arg);
	}
	
	public static void warn(Object arg) {
		if (!configured) return;
		org.apache.log4j.Logger.getLogger("ROOT").warn(arg);
	}
	
	public static void error(String arg) {
		if (!configured) return;
		org.apache.log4j.Logger.getLogger("ROOT").error(arg);
	}
	
	public static void error(Object arg, Throwable t) {
		error(arg, t, isDebug());
	}
	
	public static void error(Object arg, Throwable t, boolean stackTrace) {
		if (!configured) return;
		error(arg.toString()+": "+(t.getMessage() == null ? t.toString() : t.getMessage()));
		if (stackTrace)
			org.apache.log4j.Logger.getLogger("ROOT").error(getStackTrace(t));
	}
	
	public static void fatal(String arg) {
		if (!configured) return;
		org.apache.log4j.Logger.getLogger("ROOT").fatal(arg);
	}
	
	public static void fatal(Object arg, Throwable t) {
		if (!configured) return;
		org.apache.log4j.Logger.getLogger("ROOT").fatal(arg);
		org.apache.log4j.Logger.getLogger("ROOT").fatal(getStackTrace(t));
	}
	
	public static String getStackTrace(Throwable t) {
		StringWriter msg = new StringWriter();
		PrintWriter writer = new PrintWriter(msg);
		t.printStackTrace(writer);
		return msg.toString();
	}

}
