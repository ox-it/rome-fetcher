package org.rometools.fetcher.impl;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.rometools.fetcher.impl.HashMapFeedInfoCache;
import org.rometools.fetcher.impl.SyndFeedInfo;

public class HashMapFeedInfoCacheTest {

	@Test
	public void testRemove() throws Exception {
		final HashMapFeedInfoCache cache = new HashMapFeedInfoCache();
		Assert.assertNotNull( cache );
		
		final URL url = new URL("http://foo.com");
		final SyndFeedInfo syndFeedInfo = new SyndFeedInfo();
		syndFeedInfo.setUrl(url);
		cache.setFeedInfo(url, syndFeedInfo);
		
		final SyndFeedInfo returned = cache.remove(url);
		Assert.assertTrue( returned.equals(syndFeedInfo) );
		Assert.assertTrue( url.equals( returned.getUrl() ));
	}
	
	@Test
	public void testClear() throws Exception {
		final HashMapFeedInfoCache cache = new HashMapFeedInfoCache();
		Assert.assertNotNull( cache );
		
		final URL url = new URL("http://foo.com");
		final SyndFeedInfo syndFeedInfo = new SyndFeedInfo();
		syndFeedInfo.setUrl(url);
		cache.setFeedInfo(url, syndFeedInfo);
		
		//clear it
		cache.clear();
		
		//we should not get a result back
		final Object returned = cache.getFeedInfo(url);
		Assert.assertTrue( returned == null );
	}
}
