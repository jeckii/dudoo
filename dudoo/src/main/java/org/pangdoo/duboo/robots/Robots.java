package org.pangdoo.duboo.robots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.pangdoo.duboo.fetcher.Fetcher;
import org.pangdoo.duboo.fetcher.Options;
import org.pangdoo.duboo.http.HttpResponse;
import org.pangdoo.duboo.http.basic.BasicHttpGet;
import org.pangdoo.duboo.url.URLNullErrorException;
import org.pangdoo.duboo.url.WebURL;
import org.pangdoo.duboo.util.LogLogger;

public class Robots {
	
	private final static LogLogger logger = LogLogger.getLogger(Robots.class);
	
	private static final String ROBOTS_TXT = "/robots.txt";
	
	private static final String ALLOW_ITEM = "allow";
	
	private static final String DISALLOW_ITEM = "disallow";
	
	private Fetcher fetcher;
	
	private Map<String, List<String>> items;

	private Options options;

	public Robots(Options options) {
		this.options = options;
		this.fetcher = Fetcher.custom(options).build();
	}

	public static Robots custom(Options options) {
		return new Robots(options);
	}
	
	public void fetch(String location) {
		try {
			if (location == null) {
				throw new URLNullErrorException("The location is null.");
			}
			if (RobotsCache.hasLocation(location)) {
				return;
			}
			WebURL webUrl = new WebURL(location + ROBOTS_TXT);
	    	HttpResponse response = fetcher.fetch(new BasicHttpGet(webUrl));
	    	if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 200) {
	    		HttpEntity entity = response.getEntity();
	    		if (entity != null) {
	    			RobotsParser reader = new RobotsParser(entity.getContent(), options.getCharset());
		        	items = reader.items(options.getUserAgent());
		        	Robot robot = new Robot();
		        	robot.setAllow(allow());
		        	robot.setDisallow(disallow());
		        	RobotsCache.put(location, robot);
	    		}
	    	}
		} catch (IOException e) {
			logger.warn(e);
		}
	}
	
	private List<String> allow() {
		if (items != null) {
			List<String> allow = items.get(ALLOW_ITEM);
			if (allow == null) {
				return new ArrayList<String>(0);
			}
			return allow;
		}
		return new ArrayList<String>(0);
	}
	
	private List<String> disallow() {
		if (items != null) {
			List<String> disallow = items.get(DISALLOW_ITEM);
			if (disallow == null) {
				return new ArrayList<String>(0);
			}
			return disallow;
		}
		return new ArrayList<String>(0);
	}
	
	public void shutdown() {
		if (fetcher != null) {
			fetcher.shutdown();
		}
	}
	
}
