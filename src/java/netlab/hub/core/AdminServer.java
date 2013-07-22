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

import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;
import static org.rendersnake.HtmlAttributesFactory.id;
import static org.rendersnake.HtmlAttributesFactory.type;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

import netlab.hub.serial.SerialPort;
import netlab.hub.util.FileUtils;
import netlab.hub.util.Logger;
import netlab.hub.util.NetworkUtils;
import netlab.hub.util.ThreadUtil;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.ext.jquery.JQueryLibrary;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class AdminServer {
	
	public static String message;

	int port;
	Hub hub;
	HttpServer server;

	public AdminServer(int port, Hub hub) {
		this.port = port;
		this.hub = hub;
	}

	public void start() throws IOException, BindException {
		InetSocketAddress addr = new InetSocketAddress(this.port);
		server = HttpServer.create(addr, 0);
		server.createContext("/", new HttpHandler() {

			public synchronized void handle(HttpExchange exchange) throws IOException {
				ResponseWriter response = new ResponseWriter(exchange.getResponseBody());
				try {
					String requestMethod = exchange.getRequestMethod();
					if (requestMethod.equalsIgnoreCase("GET")) {
						Headers responseHeaders = exchange.getResponseHeaders();
						responseHeaders.set("Content-Type", "text/html");
						exchange.sendResponseHeaders(200, 0);
						String path = exchange.getRequestURI().getPath();
						if (path.indexOf("admin.css") > -1) {
							// CSS was requested
							response.write(FileUtils.fileToString(new File(hub.getRootDir(), "conf"+File.separator+"admin.css")));
						} else {
							// page was requested
							Map<String, String> params = getParams(exchange);
							executeCommand(params);
							HtmlCanvas html = new HtmlCanvas();
							html.html();
							headHtml(html);
							html.body();
							navigationHtml(html);
							headerHtml(html);
							mainHtml(params, html);
							html._body();
							html._html();
							response.write(html.toHtml());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				response.responseBody.close();
			}
		});
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
	}
	
	public synchronized void stop() throws IOException {
		final int port = this.port;
		if (server == null) return;
		new Thread(new Runnable() {
			public void run() {
				server.stop(port);
			}
		}).start();
		ThreadUtil.pause(1000);
	}
	

	
	void headHtml(HtmlCanvas html) throws IOException {
		html.head()
				.macros().stylesheet("/admin.css")
				.render(JQueryLibrary.core("1.4.3"))
				.script().content("$(document).ready(function() {var log=$('#log'); log.scrollTop(log[0].scrollHeight - log.height());});")
			._head();
	}
	
	void navigationHtml(HtmlCanvas html) throws IOException {
		html.div(class_("nav"))
			.a(href("/").class_("navlink")).content("Dashboard")
			.a(href("/?command=services").class_("navlink")).content("Services")
			.a(href("/?command=log").class_("navlink")).content("Log file")
			._div();
	}
	
	void headerHtml(HtmlCanvas html) throws IOException {
		html.h1().content("NETLab Hub");
		html.p().content("Version "+Config.getAppVersion()+" build "+Config.getAppBuild());
		if (message != null) {
			html.p(class_("message")).content(message);
			message = null;
		}
	}
	
	void executeCommand(Map<String, String> requestParams) {
		String command = requestParams.get("command");
		if ("log".equals(command)) {
			if ("toggle".equals(requestParams.get("debug"))) {
				Logger.switchDebugLevel();
			}
		} else if ("restart".equals(command)) {
			message = "Hub restarted.";
			hub.restart();
//			new Thread(new Runnable() {
//				public void run() {
//					// Allow time for response to be sent, then quit
//					ThreadUtil.pause(2000);
//					hub.restart();
//				}
//			}).start();
		}
	}
	
	void mainHtml(Map<String, String> requestParams, HtmlCanvas html) throws IOException {
		String command = requestParams.get("command");
		if ("log".equals(command)) {
			logFileHtml(html);
		} else if ("services".equals(command)) {
			servicesHtml(html);
		} else {
			dashboardHtml(html);
		}
	}
	
	void dashboardHtml(HtmlCanvas html) throws IOException {
		
		html.table()
			.tr()
				.td(class_("first")).content("IP address")
				.td().content(NetworkUtils.getLocalMachineAddress())
			._tr()
			.tr()
				.td(class_("first")).content("TCP/IP socket port")
				.td().content(Integer.toString(Config.getPort()))
			._tr()
			.tr()
				.td(class_("first")).content("Websocket port")
				.td().content(Integer.toString(Config.getWebSocketPort()))
			._tr()
		._table();
		
		html.h2().content("Connected clients");
		html.table();
		String[] activeSessions = hub.listActiveSessions();
		if (activeSessions.length == 0) {
			html.tr().td().write("None")._td()._tr();
		} else {
			for (int i=0; i<activeSessions.length; i++) {
				html.tr().td().write(activeSessions[i])._td()._tr();
			}
		}
		html._table();
		
		html.h2().content("Connected serial devices");
		html.table();
		String[] serialDevices = SerialPort.list();
		for (int i=0; i<serialDevices.length; i++) {
			html.tr().td().write(serialDevices[i])._td()._tr();
		}
		
		html._table();
		
		html.div(class_("footer"))
			.input(type("button").value("Refresh").onClick("location.reload();"))
			.input(type("button").value("Restart Hub").onClick("if (confirm('Are you sure you want to restart the Hub?')) location.replace('/?command=restart');"))
			._div();
	}
	
	void servicesHtml(HtmlCanvas html) throws IOException {
		html.h2().content("Installed services");
		html.table(class_("services"));
		html.tr(class_("head"))
				.th().write("Status")._th()
				.th().write("Plug-in")._th()
				.th().write("Address")._th()
				.th().write("Description")._th()
			._tr();
		int i=0;
		for (Iterator<ServiceConfig> it=ServiceConfig.getAll().iterator(); it.hasNext();) {
			ServiceConfig config = it.next();
			String rowClass = (config.isEnabled() ? "enabled" : "disabled")+" "+(i++%2 == 1 ? "odd" : "even");
			html.tr(class_(rowClass))
					.td(class_("status")).write(config.isEnabled() ? "enabled" : "disabled")._td()
					.td(class_("group")).write(config.getGroup())._td()
					.td(class_("address")).write(config.getAddress())._td()
					.td(class_("description")).write(config.getDescription())._td()
				._tr();
		}
		html._table();
	}
	
	void logFileHtml(HtmlCanvas html) throws IOException {
		String logContents = "";
		Appender appender = org.apache.log4j.Logger.getRootLogger().getAppender("filedest");
		if (appender == null) {
			logContents = "No appender [filedest] found for root appender. Please check your Log4J settings.";
		} else {
			try {
				File file = new File(((FileAppender)appender).getFile());
				logContents = FileUtils.fileToString(file);
			} catch (ClassCastException e) {
				logContents = "Appender [filedest] must be a FileAppender. Please check your Log4J settings.";
			} catch (IOException e) {
				logContents = e.toString();
			}
		}
		html.h2().content("Log file");
		html.textarea(id("log").onLoad("this.scrollTop=this.scrollHeight;")).content(logContents);
		html.div(class_("footer"))
				.input(type("button").value("Reload log file").onClick("location.reload();"))
				.input(type("button").value((Logger.isDebug() ? "Disable" : "Enable")+" debug logging").onClick("location.replace('/?command=log&debug=toggle');"))
			._div();
	}
	
	Map<String, String> getParams(HttpExchange request) {
		String query = request.getRequestURI().getQuery();
		Map<String, String> params = new HashMap<String, String>();
		if (query != null) {
			String[] segments = query.split("&");
			for (int i=0; i<segments.length; i++) {
				String[] pair = segments[i].split("=");
				if (pair.length == 2) {
					params.put(pair[0], pair[1]);
				}
			}
		}
		return params;
	}

}

class ResponseWriter {
	OutputStream responseBody;
	public ResponseWriter(OutputStream responseBody) {
		this.responseBody = responseBody;
	}
	public void write(Object out) throws IOException {
		write(out.toString());
	}
	public void write(String out) throws IOException {
		responseBody.write(out.getBytes());
	}
}
