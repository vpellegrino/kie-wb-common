/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.dmn.client.marshaller.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.kie.workbench.common.dmn.client.marshaller.common.IdUtils.getPrefixedId;
import static org.kie.workbench.common.dmn.client.marshaller.common.IdUtils.getRawId;
import static org.kie.workbench.common.dmn.client.marshaller.common.IdUtils.uniqueId;

public class IdUtilsTest {

    @Test
    public void testGetPrefixedId() {
        assertEquals("1111#2222", getPrefixedId("1111", "2222"));
        assertEquals("2222", getPrefixedId("", "2222"));
        assertEquals("2222", getPrefixedId(null, "2222"));
    }

    @Test
    public void testGetRawId() {
        assertEquals("2222", getRawId("1111#2222"));
        assertEquals("2222", getRawId("#2222"));
        assertEquals("2222", getRawId("2222"));
    }

    @Test
    public void testUniqueId() {
        assertNotEquals(uniqueId(), uniqueId());
    }
}
