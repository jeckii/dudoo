package org.pangdoo.duboo.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pangdoo.duboo.fetcher.Configuration;
import org.pangdoo.duboo.fetcher.Fetcher;
import org.pangdoo.duboo.fetcher.HttpResponse;
import org.pangdoo.duboo.handler.PageParser;
import org.pangdoo.duboo.handler.reader.HTMLReader;
import org.pangdoo.duboo.request.AbstractUrlRequst;
import org.pangdoo.duboo.robots.RobotsTxtFecher;
import org.pangdoo.duboo.url.UrlCollector;
import org.pangdoo.duboo.url.WebUrl;

public class PageCrawler {
	
	private PageParser parser;
	private Configuration configuration;
	private RobotsTxtFecher robotsTxtFecher;
	
	public PageCrawler(Configuration configuration, PageParser parser) {
		this.configuration = configuration;
		this.robotsTxtFecher = new RobotsTxtFecher(configuration);
		if (parser == null) {
			throw new IllegalArgumentException("Parser is null.");
		}
		this.parser = parser;
	}
	
	public List<Object> crawl(AbstractUrlRequst urlRequst, UrlCollector collector) throws Exception {
		Set<String> locations = collector.locations();
		for (String location : locations) {
			robotsTxtFecher.fetch(location);
			List<String> disallows = robotsTxtFecher.disallow();
			for (String disallow : disallows) {
				collector.filter(location, disallow);
			}
		}
		long size = collector.size();
		if (size == 0L) {
			return new ArrayList<Object>(0);
		}
		List<Object> dataList = new ArrayList<Object>(new Long(size).intValue());
		Fetcher fetcher = new Fetcher(configuration);
		HTMLReader reader;
		while (collector.hasNext()) {
			WebUrl webUrl = collector.consume();
			urlRequst.setUrl(webUrl.getUrl().toString());
			HttpResponse response = fetcher.fetch(urlRequst);
			reader = new HTMLReader(response.getEntity().getContent(), "UTF-8", webUrl.getUrl().baseUrl());
			dataList.add(parser.parse(reader.getDocument()));
			Thread.sleep(configuration.getDelay());
		}
		if (fetcher != null) {
			fetcher.shutdown();
		}
		return dataList;
	}
	
}
