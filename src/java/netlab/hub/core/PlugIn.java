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

package netlab.hub.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netlab.hub.util.Logger;

/**
 * @author ebranda
 */
public class PlugIn {
	
	public static List<PlugIn> loadAll(File root) throws IOException {
		List<PlugIn> plugIns = new ArrayList<PlugIn>();
		File[] plugInBaseDirs = new File(root, "plugins").listFiles();
		for (int i=0; i<plugInBaseDirs.length; i++) {
			File plugInBase = plugInBaseDirs[i];
			if (!plugInBase.isDirectory()) continue;
			try {
				PlugIn p = new PlugIn(plugInBase);
				p.load();
				plugIns.add(p);
				Logger.info("Loaded plugin ["+p.getName()+"] "+p.getVersion()+" "+p.getBuild()+"");
			} catch(Exception e) {
				Logger.error("Error loading plug-in ["+plugInBase.getAbsolutePath()+"] "+e);
			}
		}
		return plugIns;
	}

	
	String version;
	String build;
	File baseDir;
	XMLConfig config;

	/**
	 * 
	 */
	public PlugIn(File baseDir) {
		super();
		this.baseDir = baseDir;
	}

	/**
	 * @return
	 */
	public String getName() {
		return baseDir.getName();
	}

	/**
	 * @return Returns the build.
	 */
	public String getBuild() {
		return build;
	}
	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return
	 */
	public File getBaseDirectory() {
		return this.baseDir;
	}
	
	public XMLConfig getConfigDocument() {
		return config;
	}
	
	public void load() throws IOException, ConfigurationException {
		//ClasspathManager.addFile(baseDir, true);
		//modifySystemLibraryPath(baseDir); // Add the plug-in's "lib" directory to the system java.library.path so that native libs in plugins can be loaded automatically
		// Load the plugin config
		this.config = new XMLConfig(baseDir);
		if (config != null) {
			this.version = config.getDocument().getRootElement().getAttributeValue("version");
			this.build = config.getDocument().getRootElement().getAttributeValue("build");
			if (this.build == null)
				this.build = "";
		}
		// Load logging config if available
		File log4j = new File(baseDir, "log4j.properties");
		if (log4j.exists()) {
			Logger.configure(log4j);
		}
	}
	
	
	
	/**
	 * @param baseDir
	 * @throws IOException
	 */
//	private void modifySystemLibraryPath(File baseDir) throws IOException {
//		ClasspathManager.addDirectoryToSystemLibraryPath(new File(baseDir, "lib"));
//	}
	
	
}

