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

import org.rometools.fetcher.impl.HttpURLFeedFetcher;
import org.rometools.fetcher.impl.FeedFetcherCache;
import org.rometools.fetcher.FeedFetcher;



public class HttpURLFeedFetcherTest extends AbstractJettyTest {
	
	protected FeedFetcher getFeedFetcher() {
		return new HttpURLFeedFetcher();
	}		
	
	protected FeedFetcher getFeedFetcher(FeedFetcherCache cache) {
		return new HttpURLFeedFetcher(cache);
	}

    public FeedFetcher getAuthenticatedFeedFetcher() {
        java.net.Authenticator.setDefault(new TestBasicAuthenticator());
        FeedFetcher feedFetcher = getFeedFetcher();	
        return feedFetcher;
    }

}