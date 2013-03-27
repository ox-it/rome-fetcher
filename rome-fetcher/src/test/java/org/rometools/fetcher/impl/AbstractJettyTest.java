/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.rometools.fetcher.impl;

import org.rometools.fetcher.impl.HashMapFeedInfoCache;
import org.rometools.fetcher.impl.FeedFetcherCache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.http.BasicAuthenticator;
import org.mortbay.http.HashUserRealm;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.SocketListener;
import org.mortbay.http.UserRealm;
import org.mortbay.http.handler.SecurityHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.MultiException;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

import org.rometools.fetcher.FeedFetcher;
import org.rometools.fetcher.FetcherEvent;
import org.rometools.fetcher.FetcherException;
import org.rometools.fetcher.FetcherListener;

/**
 * @author nl
 */
public abstract class AbstractJettyTest {

	private HttpServer server;
    private int testPort = 8283;
	
	protected HttpServer getServer() {
		return server;
	}

	protected abstract FeedFetcher getFeedFetcher();
	
	protected abstract FeedFetcher getFeedFetcher(FeedFetcherCache cache);
	
	@Before
	public void setup() throws Exception {
		setupServer();
	
		HttpContext context = createContext();
		
		ServletHandler servlets = createServletHandler();
		context.addHandler(servlets);
		
		server.addContext(context);		
		
		server.start();
	}
	
	@After
	public void cleanup() throws Exception {
		if (server != null) {
			server.stop();
			server.destroy();
			server = null;
		}
	}

    private void setupServer() throws InterruptedException {
        // Create the server
		if (server != null) {
			server.stop();
			server = null;
		}
		server = new HttpServer();
	
		// Create a port listener
		SocketListener listener=new SocketListener();
		listener.setPort(testPort);
		server.addListener(listener);
    }

    private ServletHandler createServletHandler() {
        ServletHandler servlets = new ServletHandler();
		servlets.addServlet("FetcherTestServlet",FetcherTestServlet.SERVLET_MAPPING,"org.rometools.fetcher.impl.FetcherTestServlet");
		servlets.addServlet("FetcherTestServlet",FetcherTestServlet.SERVLET_MAPPING2,"org.rometools.fetcher.impl.FetcherTestServlet");
        return servlets;
    }

    private HttpContext createContext() {
        HttpContext context = new HttpContext();
		context.setContextPath("/rome/*");
        return context;
    }

	class FetcherEventListenerImpl implements FetcherListener {
		boolean polled = false;
		boolean retrieved = false;
		boolean unchanged = false;

		public void reset() {
			polled = false;
			retrieved = false;
			unchanged = false;
		}

		/**
		 * @see com.sun.syndication.fetcher.FetcherListener#fetcherEvent(com.sun.syndication.fetcher.FetcherEvent)
		 */
		public void fetcherEvent(FetcherEvent event) {
			String eventType = event.getEventType();
			if (FetcherEvent.EVENT_TYPE_FEED_POLLED.equals(eventType)) {
				System.err.println("\tEVENT: Feed Polled. URL = " + event.getUrlString());
				polled = true;
			} else if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(eventType)) {
				System.err.println("\tEVENT: Feed Retrieved. URL = " + event.getUrlString());
				retrieved = true;
			} else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(eventType)) {
				System.err.println("\tEVENT: Feed Unchanged. URL = " + event.getUrlString());
				unchanged = true;
			}
		}
	}

	@Test
	public void testRetrieveFeed() throws IllegalArgumentException, MalformedURLException, IOException, FeedException, FetcherException {
		FeedFetcher feedFetcher = getFeedFetcher();
		SyndFeed feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet/"));
		Assert.assertNotNull(feed);
		Assert.assertEquals("atom_1.0.feed.title", feed.getTitle());
	}

	@Test
	public void testBasicAuthentication() throws InterruptedException, IOException, MultiException, IllegalArgumentException, FeedException, FetcherException {
		
        setupServer();
        
        HttpContext context = createContext();

        URL url = this.getClass().getResource("/testuser.properties");            
        UserRealm ur = new HashUserRealm("test", url.getFile());						
        context.setRealm(ur);

        BasicAuthenticator ba = new BasicAuthenticator();
        context.setAuthenticator(ba);		
        
        SecurityHandler sh =  new SecurityHandler();					
        context.addHandler(sh);

        SecurityConstraint sc = new SecurityConstraint();
        sc.setName("test");
        sc.addRole("*");
        sc.setAuthenticate(true);		
        context.addSecurityConstraint("/", sc);			
        
        ServletHandler servlets = createServletHandler();
        context.addHandler(servlets);
        
        server.addContext(context);		
        
        server.start();            
        
        FeedFetcher feedFetcher = getAuthenticatedFeedFetcher();
		SyndFeed feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet/"));
		Assert.assertNotNull(feed);
		Assert.assertEquals("atom_1.0.feed.title", feed.getTitle());
		
	}
	
	public abstract FeedFetcher getAuthenticatedFeedFetcher();
	
	/**
	 * Test getting a feed via a http 301 redirect
	 */
	@Test
	public void testRetrieveRedirectedFeed() throws IllegalArgumentException, MalformedURLException, IOException, FeedException, FetcherException {
		FeedFetcher feedFetcher = getFeedFetcher();
		SyndFeed feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet?redirect=TRUE"));
		Assert.assertNotNull(feed);
		Assert.assertEquals("atom_1.0.feed.title", feed.getTitle());
	}
	
	@Test
	public void testError404Handling() throws IllegalArgumentException, MalformedURLException, IOException, FeedException {
		FeedFetcher feedFetcher = getFeedFetcher();
		try {
			feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet?error=404"));
			Assert.fail("4xx error handling did not work correctly");
		} catch (FetcherException e) {
			Assert.assertEquals(404, e.getResponseCode());
		}
	}
	
	@Test
	public void testError500Handling() throws IllegalArgumentException, MalformedURLException, IOException, FeedException {
		FeedFetcher feedFetcher = getFeedFetcher();
		try {
			feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet?error=500"));
			Assert.fail("5xx error handling did not work correctly");
		} catch (FetcherException e) {
			// expect this exception
			Assert.assertEquals(500, e.getResponseCode());
		}
	}
	
	@Test
	public void testUserAgent() {
		FeedFetcher feedFetcher = getFeedFetcher();
		//System.out.println(feedFetcher.getUserAgent());
		//System.out.println(System.getProperty("rome.fetcher.version", "UNKNOWN"));
		Assert.assertEquals("Rome Client (http://tinyurl.com/64t5n) Ver: " + System.getProperty("rome.fetcher.version", "UNKNOWN"), feedFetcher.getUserAgent());
	}

	/**
	 * Test events fired when there is no cache in use
	 */
	@Test
	public void testFetchEvents() throws IllegalArgumentException, MalformedURLException, IOException, FeedException, FetcherException {
		FeedFetcher feedFetcher = getFeedFetcher();
		FetcherEventListenerImpl listener = new FetcherEventListenerImpl();
		feedFetcher.addFetcherEventListener(listener);
		
		SyndFeed feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet/"));
		Assert.assertNotNull(feed);
		Assert.assertTrue(listener.polled);
		Assert.assertTrue(listener.retrieved);
		Assert.assertFalse(listener.unchanged);
		listener.reset();

		// since there is no cache, the events fired should be exactly the same if
		// we re-retrieve the feed
		feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet/"));
		Assert.assertNotNull(feed);
		Assert.assertTrue(listener.polled);
		Assert.assertTrue(listener.retrieved);
		Assert.assertFalse(listener.unchanged);
		listener.reset();
	}

	/**
	 * Test events fired when there is a cache in use
	 */
	@Test
	public void testFetchEventsWithCache() throws IllegalArgumentException, MalformedURLException, IOException, FeedException, FetcherException {
		FeedFetcherCache feedInfoCache = new HashMapFeedInfoCache();
		FeedFetcher feedFetcher = getFeedFetcher(feedInfoCache);
		FetcherEventListenerImpl listener = new FetcherEventListenerImpl();
		feedFetcher.addFetcherEventListener(listener);
		
		SyndFeed feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet/"));
		Assert.assertNotNull(feed);
		Assert.assertTrue(listener.polled);
		Assert.assertTrue(listener.retrieved);
		Assert.assertFalse(listener.unchanged);
		listener.reset();

		// Since the feed is cached, the second request should not
		// actually retrieve the feed
		feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet/"));
		Assert.assertNotNull(feed);
		Assert.assertTrue(listener.polled);
		Assert.assertFalse(listener.retrieved);
		Assert.assertTrue(listener.unchanged);
		listener.reset();

		// now simulate getting the feed after it has changed
		feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet?refreshfeed=TRUE"));
		Assert.assertNotNull(feed);
		Assert.assertTrue(listener.polled);
		Assert.assertTrue(listener.retrieved);
		Assert.assertFalse(listener.unchanged);
		listener.reset();
	}
	
	/**
	 * Test handling of GZipped feed
	 */
	@Test
	public void testGZippedFeed() throws IllegalArgumentException, MalformedURLException, IOException, FeedException, FetcherException {
	    FeedFetcher feedFetcher = getFeedFetcher();
		SyndFeed feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet?gzipfeed=TRUE"));
		Assert.assertNotNull(feed);
		Assert.assertEquals("atom_1.0.feed.title", feed.getTitle());
	}
	
	@Test
	public void testPreserveWireFeed() throws IllegalArgumentException, MalformedURLException, IOException, FeedException, FetcherException {
		FeedFetcher feedFetcher = getFeedFetcher();

		// first check we the WireFeed is not preserved by default
		SyndFeed feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet/"));
		Assert.assertNotNull(feed);
		Assert.assertEquals("atom_1.0.feed.title", feed.getTitle());
		Assert.assertNull(feed.originalWireFeed());
		
		SyndEntry syndEntry = (SyndEntry)feed.getEntries().get(0);
		Assert.assertNotNull(syndEntry);
		Assert.assertNull(syndEntry.getWireEntry());
		
		// now turn on WireFeed preservation	
		feedFetcher.setPreserveWireFeed(true);
		try {
			feed = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet/"));
			Assert.assertNotNull(feed);
			Assert.assertEquals("atom_1.0.feed.title", feed.getTitle());
			Assert.assertNotNull(feed.originalWireFeed());

			syndEntry = (SyndEntry)feed.getEntries().get(0);
			Assert.assertNotNull(syndEntry);
			Assert.assertNotNull(syndEntry.getWireEntry());
			
			Entry entry = (Entry) syndEntry.getWireEntry();
			Assert.assertEquals("atom_1.0.feed.entry[0].rights", entry.getRights());
			
		} finally {
			feedFetcher.setPreserveWireFeed(false); //reset 
		}
		
	}
	
	@Test
	public void testDeltaEncoding() throws IllegalArgumentException, MalformedURLException, IOException, FeedException, FetcherException {
	    FeedFetcherCache feedInfoCache = new HashMapFeedInfoCache();
		FeedFetcher feedFetcher = getFeedFetcher(feedInfoCache);	    		

		feedFetcher.setUsingDeltaEncoding(true);
		    
	    // first retrieval should just grab the default feed
		SyndFeed feed1 = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet?deltaencode=TRUE&refreshfeed=TRUE"));
		Assert.assertNotNull(feed1);
		Assert.assertEquals("atom_1.0.feed.title", feed1.getTitle());
		Assert.assertEquals(2, feed1.getEntries().size());
		SyndEntry entry1 = (SyndEntry) feed1.getEntries().get(0);
		Assert.assertEquals("atom_1.0.feed.entry[0].title", entry1.getTitle());
		
		// second retrieval should get only the new item
		/*
		 * This is breaking with Rome 0.5 ??
		 */ 
		SyndFeed feed2 = feedFetcher.retrieveFeed(new URL("http://localhost:"+testPort+"/rome/FetcherTestServlet?deltaencode=TRUE&refreshfeed=TRUE"));					
		Assert.assertNotNull(feed2);
		Assert.assertEquals(FetcherTestServlet.DELTA_FEED_TITLE, feed2.getTitle());
		Assert.assertEquals(3, feed2.getEntries().size());
		entry1 = (SyndEntry) feed2.getEntries().get(0);
		Assert.assertEquals(FetcherTestServlet.DELTA_FEED_ENTRY_TITLE, entry1.getTitle());
		
		SyndEntry entry2 = (SyndEntry) feed2.getEntries().get(1);
		Assert.assertEquals("atom_1.0.feed.entry[0].title", entry2.getTitle());			
			
	}
		
	
}
