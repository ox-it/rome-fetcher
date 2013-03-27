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

import org.junit.Assert;
import org.junit.Test;
import org.rometools.fetcher.impl.ResponseHandler;

public class ResponseHandlerTest {

	@Test
	public void testGetCharacterEncodingString() {
		Assert.assertEquals(ResponseHandler.defaultCharacterEncoding, ResponseHandler.getCharacterEncoding((String)null));
		Assert.assertEquals(ResponseHandler.defaultCharacterEncoding, ResponseHandler.getCharacterEncoding("text/xml"));
		Assert.assertEquals(ResponseHandler.defaultCharacterEncoding, ResponseHandler.getCharacterEncoding("text/xml;"));
		Assert.assertEquals("ISO-8859-4", ResponseHandler.getCharacterEncoding("text/xml; charset=ISO-8859-4"));
		Assert.assertEquals("ISO-8859-4", ResponseHandler.getCharacterEncoding("text/xml;charset=ISO-8859-4"));
		Assert.assertEquals("ISO-8859-4", ResponseHandler.getCharacterEncoding("text/xml;charset=ISO-8859-4;something"));
		Assert.assertEquals(ResponseHandler.defaultCharacterEncoding, ResponseHandler.getCharacterEncoding("text/xml;something"));
	}

}
