/*
 * Copyright 2014-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.utflute.guice.web.webmock;

import org.dbflute.utflute.guice.web.WebContainerTestCase;

/**
 * @author jflute
 * @since 0.4.1 (2014/03/25 Tuesday)
 */
public abstract class WebMockBaseTestCase extends WebContainerTestCase {

    protected void doTest_noMock() throws Exception {
        assertNull(getMockRequest());
        assertNull(getMockResponse());
        assertNull(getMockSession());
    }

    protected void doTest_existingMock() {
        assertNotNull(getMockRequest());
        assertNotNull(getMockResponse());
        assertNotNull(getMockSession());

        assertHasZeroElement(getMockRequest().getParameterMap().keySet());
        addMockRequestParameter("fooParam", "barParam");
        assertHasAnyElement(getMockRequest().getParameterMap().keySet());

        assertNull(getMockRequestAttribute("fooRequest"));
        setMockRequestAttribute("fooRequest", "barRequest");
        assertNotNull(getMockRequestAttribute("fooRequest"));

        assertNull(getMockSessionAttribute("fooSession"));
        setMockSessionAttribute("fooSession", "barSession");
        assertNotNull(getMockSessionAttribute("fooSession"));
    }
}
