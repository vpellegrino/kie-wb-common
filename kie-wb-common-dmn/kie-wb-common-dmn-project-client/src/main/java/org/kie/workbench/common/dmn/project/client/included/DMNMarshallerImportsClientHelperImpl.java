/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.project.client.included;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import elemental2.promise.Promise;
import org.kie.workbench.common.dmn.api.definition.model.ItemDefinition;
import org.kie.workbench.common.dmn.api.editors.included.DMNIncludedModel;
import org.kie.workbench.common.dmn.api.editors.included.DMNIncludedNode;
import org.kie.workbench.common.dmn.api.editors.included.IncludedModel;
import org.kie.workbench.common.dmn.api.editors.included.PMMLDocumentMetadata;
import org.kie.workbench.common.dmn.client.marshaller.included.DMNMarshallerImportsClientHelper;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDefinitions;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITImport;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.uberfire.client.promise.Promises;

@ApplicationScoped
public class DMNMarshallerImportsClientHelperImpl implements DMNMarshallerImportsClientHelper {

    private final Promises promises;

    @Inject
    public DMNMarshallerImportsClientHelperImpl(final Promises promises) {
        this.promises = promises;
    }

    @Override
    public Promise<Map<JSITImport, JSITDefinitions>> getImportDefinitionsAsync(final Metadata metadata, final List<JSITImport> imports) {
        return promises.resolve();
    }

    @Override
    public Promise<Map<JSITImport, PMMLDocumentMetadata>> getPMMLDocumentsAsync(final Metadata metadata, final List<JSITImport> imports) {
        return promises.resolve();
    }

    @Override
    public void getImportedItemDefinitionsByNamespaceAsync(final String modelName, final String namespace, final ServiceCallback<List<ItemDefinition>> callback) {

    }

    @Override
    public void loadNodesFromModels(final List<DMNIncludedModel> includedModels, final ServiceCallback<List<DMNIncludedNode>> callback) {

    }

    @Override
    public void loadModels(final ServiceCallback<List<IncludedModel>> callback) {

    }

    @Override
    public Map<JSITImport, String> getImportXML(final Metadata metadata, final List<JSITImport> jsitImports) {
        return new HashMap<>();
    }

    @Override
    public List<org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDRGElement> getImportedDRGElements(final Map<JSITImport, JSITDefinitions> importDefinitions) {
        return new ArrayList<>();
    }

    @Override
    public List<org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITItemDefinition> getImportedItemDefinitions(final Map<JSITImport, JSITDefinitions> importDefinitions) {
        return new ArrayList<>();
    }
}
