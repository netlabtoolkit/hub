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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author ebranda
 */
public class ClasspathManager {
	
	@SuppressWarnings("rawtypes")
	private static final Class[] parameters = new Class[]{URL.class};
	
	
	 
	public static void addToClasspath(File resource, boolean recursive) throws IOException {
		if (resource.isDirectory() || 
				resource.getName().endsWith(".jar") || 
				resource.getName().endsWith(".zip")) {
			addURL(resource.toURI().toURL());
			Logger.debug("Added ["+resource.getAbsolutePath()+"] to classpath");
		}
		if (recursive && resource.isDirectory()) {
			File[] files = resource.listFiles();
			for (int f=0; f<files.length; f++) {
				File childResource = files[f];
				addToClasspath(childResource, true); // Recursively descend
			}
		}
	}
	 
	public static void addURL(URL u) throws IOException {
			
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;
	 
		try {
			Method method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}
			
	}
	
	public static void addLibrariesToSystemLibraryPath(File resource, String libraryDirectoryName) throws IOException {
		if (resource.isDirectory()) {
			if (resource.getName().equals(libraryDirectoryName)) {
				addDirectoryToSystemLibraryPath(resource);
			} else {
				File[] files = resource.listFiles();
				for (int f=0; f<files.length; f++) {
					addLibrariesToSystemLibraryPath(files[f], libraryDirectoryName); // Recursively descend
				}
			}
		}
	}
	
	public static void addDirectoryToSystemLibraryPath(File dir) throws IOException {
		if (!dir.exists()) 
			return;
		try {
			// This enables the java.library.path to be modified at runtime
			// From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
			//
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[])field.get(null);
			for (int i = 0; i < paths.length; i++) {
				if (dir.getAbsolutePath().equals(paths[i])) {
					return;
				}
			}
			String[] tmp = new String[paths.length+1];
			System.arraycopy(paths,0,tmp,0,paths.length);
			tmp[paths.length] = dir.getAbsolutePath();
			field.set(null,tmp);
			System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + dir.getAbsolutePath());
			//System.out.println("Added new entry to java.library.path: "+dir.getAbsolutePath());
		} catch (IllegalAccessException e) {
			throw new IOException("Failed to get permissions to set library path");
		} catch (NoSuchFieldException e) {
			throw new IOException("Failed to get field handle to set library path");
		}
		Logger.debug("Added ["+dir.getAbsolutePath()+"] to java.library.path");
	}

}