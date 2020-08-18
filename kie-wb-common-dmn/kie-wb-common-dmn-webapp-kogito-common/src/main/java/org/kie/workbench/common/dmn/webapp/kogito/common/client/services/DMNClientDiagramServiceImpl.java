/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.webapp.kogito.common.client.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import org.kie.workbench.common.dmn.api.DMNDefinitionSet;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagram;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagramElement;
import org.kie.workbench.common.dmn.api.factory.DMNDiagramFactory;
import org.kie.workbench.common.dmn.api.property.dmn.Id;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.DMNShapeSet;
import org.kie.workbench.common.dmn.client.docks.navigator.DecisionNavigatorPresenter;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramSelected;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramsSession;
import org.kie.workbench.common.dmn.client.marshaller.common.DMNGraphUtils;
import org.kie.workbench.common.dmn.client.marshaller.marshall.DMNMarshaller;
import org.kie.workbench.common.dmn.client.marshaller.unmarshall.DMNUnmarshaller;
import org.kie.workbench.common.dmn.client.session.DMNSession;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.MainJs;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.callbacks.DMN12MarshallCallback;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.callbacks.DMN12UnmarshallCallback;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.DMN12;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDefinitions;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.JSIName;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.JsUtils;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.DiagramParsingException;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.diagram.MetadataImpl;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.util.StringUtils;
import org.kie.workbench.common.stunner.kogito.api.editor.DiagramType;
import org.kie.workbench.common.stunner.kogito.api.editor.impl.KogitoDiagramResourceImpl;
import org.kie.workbench.common.stunner.kogito.client.service.AbstractKogitoClientDiagramService;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.client.promise.Promises;
import org.uberfire.commons.uuid.UUID;

@ApplicationScoped
public class DMNClientDiagramServiceImpl extends AbstractKogitoClientDiagramService {

    private static final String DIAGRAMS_PATH = "diagrams";

    //This path is needed by DiagramsNavigatorImpl's use of AbstractClientDiagramService.lookup(..) to retrieve a list of diagrams
    private static final String ROOT = "default://master@system/stunner/" + DIAGRAMS_PATH;

    private DMNUnmarshaller dmnMarshallerKogitoUnmarshaller;
    private DMNMarshaller dmnMarshallerKogitoMarshaller;
    private FactoryManager factoryManager;
    private DefinitionManager definitionManager;
    private DMNDiagramFactory dmnDiagramFactory;
    private Promises promises;
    private ServiceCallback<Diagram> onLoadDiagramCallback;
    private Metadata metadata;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private DecisionNavigatorPresenter decisionNavigatorPresenter;

    @Inject
    private DMNDiagramsSession dmnDiagramsSession;

    public DMNClientDiagramServiceImpl() {
        //CDI proxy
    }

    @Inject
    public DMNClientDiagramServiceImpl(final DMNUnmarshaller dmnMarshallerKogitoUnmarshaller,
                                       final DMNMarshaller dmnMarshallerKogitoMarshaller,
                                       final FactoryManager factoryManager,
                                       final DefinitionManager definitionManager,
                                       final DMNDiagramFactory dmnDiagramFactory,
                                       final Promises promises) {
        this.dmnMarshallerKogitoUnmarshaller = dmnMarshallerKogitoUnmarshaller;
        this.dmnMarshallerKogitoMarshaller = dmnMarshallerKogitoMarshaller;
        this.factoryManager = factoryManager;
        this.definitionManager = definitionManager;
        this.dmnDiagramFactory = dmnDiagramFactory;
        this.promises = promises;
    }

    //Kogito requirements

    @Override
    public void transform(final String fileName,
                          final String xml,
                          final ServiceCallback<Diagram> callback) {
        if (Objects.isNull(xml) || xml.isEmpty()) {
            doNewDiagram(fileName, callback);
        } else {
            doTransformation(fileName, xml, callback);
        }
    }

    @Override
    public String generateDefaultId() {
        return UUID.uuid();
    }

    @Override
    public void transform(final String xml,
                          final ServiceCallback<Diagram> callback) {
        transform(UUID.uuid(), xml, callback);
    }

    void doNewDiagram(final String fileName,
                      final ServiceCallback<Diagram> callback) {
        final String title = createDiagramTitleFromFilePath(fileName);
        final Metadata metadata = buildMetadataInstance(fileName);

        try {
            final String defSetId = BindableAdapterUtils.getDefinitionSetId(DMNDefinitionSet.class);
            final Diagram stunnerDiagram = factoryManager.newDiagram(title, defSetId, metadata);
            final Node<?, ?> dmnDiagramRoot = DMNGraphUtils.findDMNDiagramRoot(stunnerDiagram.getGraph());
            final DMNDiagram definition = ((View<DMNDiagram>) dmnDiagramRoot.getContent()).getDefinition();
            final DMNDiagramElement drgDiagram = new DMNDiagramElement(new Id(), new Name("DRG"));
            definition.getDefinitions().getDmnDiagramElements().add(drgDiagram);
            final String diagramId = drgDiagram.getId().getValue();

            final Map<String, Diagram> diagramsByDiagramElementId = new HashMap<>();
            final Map<String, DMNDiagramElement> dmnDiagramsByDiagramElementId = new HashMap<>();

            diagramsByDiagramElementId.put(diagramId, stunnerDiagram);
            dmnDiagramsByDiagramElementId.put(diagramId, drgDiagram);

            dmnDiagramsSession.setState(metadata, diagramsByDiagramElementId, dmnDiagramsByDiagramElementId);

            updateClientShapeSetId(stunnerDiagram);

            callback.onSuccess(stunnerDiagram);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Metadata buildMetadataInstance(final String fileName) {
        final String defSetId = BindableAdapterUtils.getDefinitionSetId(DMNDefinitionSet.class);
        final String shapeSetId = BindableAdapterUtils.getShapeSetId(DMNShapeSet.class);
        return new MetadataImpl.MetadataImplBuilder(defSetId,
                                                    definitionManager)
                .setRoot(PathFactory.newPath(".", ROOT))
                .setPath(PathFactory.newPath(".", ROOT + "/" + fileName))
                .setShapeSetId(shapeSetId)
                .build();
    }

    private void updateClientShapeSetId(final Diagram diagram) {
        if (Objects.nonNull(diagram)) {
            final Metadata metadata = diagram.getMetadata();
            if (Objects.nonNull(metadata) && StringUtils.isEmpty(metadata.getShapeSetId())) {
                final String shapeSetId = BindableAdapterUtils.getShapeSetId(DMNShapeSet.class);
                metadata.setShapeSetId(shapeSetId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    void doTransformation(final String fileName,
                          final String xml,
                          final ServiceCallback<Diagram> callback) {

        onLoadDiagramCallback = callback;
        metadata = buildMetadataInstance(fileName);

        try {

            final DMN12UnmarshallCallback jsCallback = dmn12 -> {
                final JSITDefinitions definitions = Js.uncheckedCast(JsUtils.getUnwrappedElement(dmn12));

                dmnMarshallerKogitoUnmarshaller.unmarshall(metadata, definitions).then(_graph -> {
                    final Diagram diagram = dmnDiagramFactory.build("DRG", metadata, _graph);
                    updateClientShapeSetId(diagram);
                    callback.onSuccess(diagram);

                    return promises.resolve();
                });
            };

            MainJs.unmarshall(xml, "", jsCallback);
        } catch (Exception e) {
            DomGlobal.console.log("-------------->", e);
//            GWT.log(e.getMessage(), e);
//            callback.onError(new ClientRuntimeError(new DiagramParsingException(metadata, xml)));
        }
    }

    public void switchToDMNDiagramElement(final @Observes DMNDiagramSelected selected) {

        decisionNavigatorPresenter.disableRefreshHandlers();
        final DMNDiagramElement dmnDiagram = selected.getDiagramElement();
        final Diagram stunnerDiagram = dmnDiagramsSession.getDiagram(dmnDiagram.getId().getValue());
        final Graph graph = stunnerDiagram.getGraph();
        final Optional<ServiceCallback<Diagram>> callback = getOnLoadDiagramCallback();

        if (!callback.isPresent()) {
            return;
        }

        metadata = buildMetadataInstance(stunnerDiagram.getMetadata().getPath().getFileName());

        final Diagram diagram = dmnDiagramFactory.build(dmnDiagram.getName().getValue(), metadata, graph);
        updateClientShapeSetId(diagram);
        callback.get().onSuccess(diagram);

        decisionNavigatorPresenter.enableRefreshHandlers();
        decisionNavigatorPresenter.refreshTreeView();
    }

    private Optional<ServiceCallback<Diagram>> getOnLoadDiagramCallback() {
        return Optional.ofNullable(onLoadDiagramCallback);
    }

    public void getDefinitions(final String xml,
                               final ServiceCallback<Object> callback) {
        final DMN12UnmarshallCallback jsCallback = dmn12 -> {
            final JSITDefinitions definitions = Js.uncheckedCast(JsUtils.getUnwrappedElement(dmn12));
            callback.onSuccess(definitions);
        };
        MainJs.unmarshall(xml, "", jsCallback);
    }

    @Override
    public Promise<String> transform(final KogitoDiagramResourceImpl resource) {
        if (resource.getType() == DiagramType.PROJECT_DIAGRAM) {
            return promises.create((resolveOnchangeFn, rejectOnchangeFn) -> {
                if (resource.projectDiagram().isPresent()) {
                    final Diagram diagram = resource.projectDiagram().get();
                    marshall(diagram,
                             resolveOnchangeFn,
                             rejectOnchangeFn);
                } else {
                    rejectOnchangeFn.onInvoke(new IllegalStateException("DiagramType is PROJECT_DIAGRAM however no instance present"));
                }
            });
        }
        return promises.resolve(resource.xmlDiagram().orElse("DiagramType is XML_DIAGRAM however no instance present"));
    }

    @SuppressWarnings("unchecked")
    private void marshall(final Diagram diagram,
                          final Promise.PromiseExecutorCallbackFn.ResolveCallbackFn<String> resolveOnchangeFn,
                          final Promise.PromiseExecutorCallbackFn.RejectCallbackFn rejectOnchangeFn) {
        if (Objects.isNull(diagram)) {
            return;
        }
        final Graph graph = diagram.getGraph();
        if (Objects.isNull(graph)) {
            return;
        }

        final DMN12MarshallCallback jsCallback = result -> {
            String xml = result;
            if (!xml.startsWith("<?xml version=\"1.0\" ?>")) {
                xml = "<?xml version=\"1.0\" ?>" + xml;
            }
            resolveOnchangeFn.onInvoke(xml);
        };

        try {
            final JSITDefinitions jsitDefinitions = dmnMarshallerKogitoMarshaller.marshall(graph);
            final DMN12 dmn12 = Js.uncheckedCast(JsUtils.newWrappedInstance());
            JsUtils.setNameOnWrapped(dmn12, makeJSINameForDMN12());
            JsUtils.setValueOnWrapped(dmn12, jsitDefinitions);

            final JavaScriptObject namespaces = createNamespaces(jsitDefinitions.getOtherAttributes(),
                                                                 jsitDefinitions.getNamespace());
            MainJs.marshall(dmn12, namespaces, jsCallback);
        } catch (Exception e) {
            GWT.log(e.getMessage(), e);
            rejectOnchangeFn.onInvoke(e);
        }
    }

    private JavaScriptObject createNamespaces(final Map<QName, String> otherAttributes,
                                              final String defaultNamespace) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(defaultNamespace, new JSONString(""));
        otherAttributes.forEach((key, value) -> jsonObject.put(value, new JSONString(key.getLocalPart())));
        return jsonObject.getJavaScriptObject();
    }

    private JSIName makeJSINameForDMN12() {
        final org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.JSIName jsiName = JSITDefinitions.getJSIName();
        jsiName.setPrefix("dmn");
        jsiName.setLocalPart("definitions");
        final String key = "{" + jsiName.getNamespaceURI() + "}" + jsiName.getLocalPart();
        final String keyString = "{" + jsiName.getNamespaceURI() + "}" + jsiName.getPrefix() + ":" + jsiName.getLocalPart();
        jsiName.setKey(key);
        jsiName.setString(keyString);
        return jsiName;
    }

    private DMNSession getCurrentSession() {
        return sessionManager.getCurrentSession();
    }
}
