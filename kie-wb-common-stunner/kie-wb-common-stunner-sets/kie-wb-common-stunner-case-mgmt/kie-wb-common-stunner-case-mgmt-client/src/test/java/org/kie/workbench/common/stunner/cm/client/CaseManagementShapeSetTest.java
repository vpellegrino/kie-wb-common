/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
 */

package org.kie.workbench.common.stunner.cm.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.cm.CaseManagementDefinitionSet;
import org.kie.workbench.common.stunner.cm.client.shape.factory.CaseManagementShapeFactory;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CaseManagementShapeSetTest {

    @Mock
    private DefinitionManager definitionManager;

    @Mock
    private CaseManagementShapeFactory factoryRegistry;

    private CaseManagementShapeSet shapeSet;

    @Before
    public void setup() {
        this.shapeSet = new CaseManagementShapeSet(definitionManager,
                                                   factoryRegistry);
    }

    @Test
    public void assertDefinitionSetClass() {
        assertEquals(CaseManagementDefinitionSet.class,
                     shapeSet.getDefinitionSetClass());
    }
}