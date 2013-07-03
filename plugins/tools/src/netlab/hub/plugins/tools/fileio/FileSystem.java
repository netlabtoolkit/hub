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

package netlab.hub.plugins.tools.fileio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author ebranda
 */
public class FileSystem {
	
	File baseDirectory;
	String filename;

	/**
	 * 
	 */
	public FileSystem() {
		super();
	}
	
	/**
	 * @param dir
	 * @throws FileNotFoundException
	 */
	public void setBaseDirectory(File dir) throws FileNotFoundException {
		if (dir != null) {
			if (!dir.exists()) {
				throw new FileNotFoundException(
						"Base directory ["+dir.getAbsolutePath()+"] does not exist");
			} else if (!dir.isDirectory()) {
				throw new FileNotFoundException(
						"Base directory ["+dir.getAbsolutePath()+"] is not a directory");
			}
			this.baseDirectory = dir;
		}
	}
	
	public File getBaseDirectory() {
		return this.baseDirectory;
	}
	
	/**
	 * @param filename
	 * @throws IOException
	 */
	public void setFilename(String filename) throws IOException {
		if (filename != null && filename.trim().length() > 0) {
			this.filename = filename;
		} else {
			throw new IOException("Filename cannot be empty");
		}
	}
	
	public String getFilename() {
		return this.filename;
	}
	
	/**
	 * @param content
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void get(PrintWriter content) 
					throws FileNotFoundException, IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(getFile()));
			String line;
			while ((line = reader.readLine()) != null) {
				content.println(line);
			}
		} finally {
			if (reader != null) reader.close();
		}
	}
	
	/**
	 * @param content
	 * @throws IOException
	 */
	public void put(String content) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(getFile());
			writer.write(content);
		} finally {
			writer.close();
		}
	}
	
	/**
	 * @param content
	 * @throws IOException
	 */
	public void append(String content) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(getFile(), true));
			writer.newLine();
			writer.write(content);
		} finally {
			writer.close();
		}
	}
	
	
	private File getFile() throws FileNotFoundException {
		if (this.baseDirectory == null) {
			throw new FileNotFoundException("No base directory set.");
		}
		if (this.filename == null) {
			throw new FileNotFoundException("No filename set.");
		}
		File f = new File(this.baseDirectory, this.filename);
		//if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		return f;
	}

}
