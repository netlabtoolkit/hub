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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class FileUtils {
	
	public static Properties loadProperties(File f) {
		Properties props = new Properties();
		try {
			loadProperties(f, props);
		} catch (IOException e) {
			Logger.error("Error loading properties from file", e);
		}
		return props;
	}
	
	public static void loadProperties(File f, Properties props) throws IOException {
		if (f.exists() && f.isFile()) {
			FileInputStream fis = new FileInputStream(f);
	        props.load(fis);    
	        fis.close();

		}
	}
	
	public static String fileToString(File file) throws IOException {
		final StringBuffer out = new StringBuffer();
		FileUtils.read(file, new IFileLineProcessor() {
			public void processLine(String line) {
				out.append(line).append(System.getProperty("line.separator"));
			}
		});
		return out.toString();
	}
	
	
	/**
	 * Usage: 
	 * 
	 * FileUtils.read(new File("myfile.txt"), new IFileLineProcessor() {
			public void processLine(String line) {
				System.out.println(line);
			}
		});
	 * 
	 * @param f
	 * @param client
	 * @throws IOException
	 */
	public static void read(File file, IFileLineProcessor processor) throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
	        String line;
	        while((line = in.readLine()) != null) {
	        	processor.processLine(line);
	        }
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null)
				in.close();
		}
	}
	
	public static void write(File dest, String content, boolean append) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(dest, append));
			writer.write(content);
		} finally {
			writer.close();
		}
	}

}
