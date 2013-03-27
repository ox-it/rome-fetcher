package org.rometools.fetcher.impl;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.rometools.fetcher.impl.DiskFeedInfoCache;
import org.rometools.fetcher.impl.SyndFeedInfo;

public class DiskFeedInfoCacheTest {

	@Test
	public void testClear() throws Exception {
		File cacheDir = new File("test-cache");
		cacheDir.mkdir();
		cacheDir.deleteOnExit();
		
		final DiskFeedInfoCache cache = new DiskFeedInfoCache(cacheDir.getCanonicalPath());
		SyndFeedInfo info = new SyndFeedInfo();
		URL url = new URL("http://nowhere.com");
		cache.setFeedInfo(url, info);
		
		cache.clear();
		final Object returned = cache.getFeedInfo(url);
		Assert.assertTrue( returned == null );
	}
	
	@Test
	public void testRemove() throws Exception {
		File cacheDir = new File("test-cache");
		cacheDir.mkdir();
		cacheDir.deleteOnExit();
		
		final DiskFeedInfoCache cache = new DiskFeedInfoCache( cacheDir.getCanonicalPath() );
		SyndFeedInfo info = new SyndFeedInfo();
		URL url = new URL("http://nowhere.com");
		cache.setFeedInfo( url, info );
		
		SyndFeedInfo removedInfo = cache.remove( url );
		Assert.assertTrue( removedInfo.equals(info) );
		SyndFeedInfo shouldBeNull = cache.remove( url );
		Assert.assertTrue( null == shouldBeNull );	
	}
	
}
