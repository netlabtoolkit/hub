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
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * @author ebranda
 */
public class XMLUtils {

	public static Document loadDocument(File source) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(source);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> selectNodes(Object context, String xpathStr) throws Exception {
		return (List<Object>)XPath.selectNodes(context, xpathStr);
	}
	
	public static Object selectSingleNode(Object context, String xpathStr) throws Exception {
		return XPath.selectSingleNode(context, xpathStr);
	}
	
	public static String stringValue(Object context, String xpathStr) throws Exception {
		Object node = selectSingleNode(context, xpathStr);
		if (node != null) {
			if (node instanceof Attribute) {
				return ((Attribute)node).getValue();
			} else if (node instanceof Content) {
				return ((Content)node).getValue();
			}
		}
		return null;
	}

}
