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
package org.kie.workbench.common.dmn.showcase.client.services;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import jsinterop.base.Js;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.dmn.api.DMNContentService;
import org.kie.workbench.common.dmn.api.DMNDefinitionSet;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagramElement;
import org.kie.workbench.common.dmn.api.factory.DMNDiagramFactory;
import org.kie.workbench.common.dmn.client.DMNShapeSet;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramSelected;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramsSession;
import org.kie.workbench.common.dmn.client.graph.DMNGraphUtils;
import org.kie.workbench.common.dmn.client.marshaller.unmarshall.DMNUnmarshaller;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.MainJs;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.callbacks.DMN12UnmarshallCallback;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDefinitions;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.JsUtils;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.export.CanvasExport;
import org.kie.workbench.common.stunner.core.client.canvas.export.CanvasURLExportSettings;
import org.kie.workbench.common.stunner.core.client.service.ClientDiagramServiceImpl;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.DiagramParsingException;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.diagram.MetadataImpl;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.lookup.LookupManager;
import org.kie.workbench.common.stunner.core.lookup.diagram.DiagramLookupRequest;
import org.kie.workbench.common.stunner.core.lookup.diagram.DiagramRepresentation;
import org.kie.workbench.common.stunner.core.util.StringUtils;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.client.promise.Promises;

@ApplicationScoped
public class DMNShowcaseDiagramService {

    private static final String DIAGRAMS_PATH = "diagrams";
    private static final String ROOT = "default://master@system/stunner/" + DIAGRAMS_PATH;

    @Inject
    private Caller<DMNContentService> dmnContentServiceCaller;

    @Inject
    private DMNUnmarshaller dmnMarshallerKogitoUnmarshaller;

    @Inject
    private DMNDiagramFactory dmnDiagramFactory;

    @Inject
    private DefinitionManager definitionManager;

    @Inject
    private Promises promises;

    @Inject
    private ClientDiagramServiceImpl clientDiagramServices;

    @Inject
    private CanvasExport<AbstractCanvasHandler> canvasExport;

    @Inject
    private DMNDiagramsSession dmnDiagramsSession;

    @Inject
    private DMNGraphUtils dmnGraphUtils;

    private ServiceCallback<Diagram> onLoadDiagramCallback;

    public DMNShowcaseDiagramService() {
        this(null,
             null);
    }

    public DMNShowcaseDiagramService(final ClientDiagramServiceImpl clientDiagramServices,
                                     final CanvasExport<AbstractCanvasHandler> canvasExport) {
        this.clientDiagramServices = clientDiagramServices;
        this.canvasExport = canvasExport;
    }

    public void loadByName(final String name,
                           final ServiceCallback<Diagram> callback) {
        final DiagramLookupRequest request = new DiagramLookupRequest.Builder().withName(name).build();
        clientDiagramServices.lookup(request,
                                     new ServiceCallback<LookupManager.LookupResponse<DiagramRepresentation>>() {
                                         @Override
                                         public void onSuccess(LookupManager.LookupResponse<DiagramRepresentation> diagramRepresentations) {
                                             if (null != diagramRepresentations && !diagramRepresentations.getResults().isEmpty()) {
                                                 final Path path = diagramRepresentations.getResults().get(0).getPath();

                                                 open(path, callback);
                                             }
                                         }

                                         @Override
                                         public void onError(final ClientRuntimeError error) {
                                             callback.onError(error);
                                         }
                                     });
    }

    private void open(final Path path,
                      final ServiceCallback<Diagram> callback) {
        dmnContentServiceCaller.call(new RemoteCallback<String>() {
            @Override
            public void callback(final String xml) {

                try {
                    final DMN12UnmarshallCallback jsCallback = dmn12 -> {
                        final JSITDefinitions definitions = Js.uncheckedCast(JsUtils.getUnwrappedElement(dmn12));
                        load(path, definitions, callback);
                    };

                    MainJs.unmarshall(xml, "", jsCallback);
                } catch (Exception e) {
                    GWT.log(e.getMessage(), e);
                    callback.onError(new ClientRuntimeError(new DiagramParsingException(null, xml)));
                }
            }
        }).getContent(path);
    }

    private void load(final Path path,
                      final JSITDefinitions definitions,
                      final ServiceCallback<Diagram> callback) {

        this.onLoadDiagramCallback = callback;

        Metadata metadata = buildMetadataInstance(path);

        dmnMarshallerKogitoUnmarshaller.unmarshall(metadata, definitions).then(_graph -> {

            final Diagram diagram = dmnDiagramFactory.build("DRG", metadata, _graph);

            updateClientShapeSetId(diagram);
            callback.onSuccess(diagram);

            return promises.resolve();
        });
    }

    public void switchToDMNDiagramElement(final @Observes DMNDiagramSelected selected) {

        final DMNDiagramElement dmnDiagramElement = selected.getDiagramElement();
        final boolean belongsToCurrentSessionState = dmnDiagramsSession.belongsToCurrentSessionState(dmnDiagramElement);

        if (belongsToCurrentSessionState && getOnLoadDiagramCallback().isPresent()) {

            final String diagramId = dmnDiagramElement.getId().getValue();
            final String diagramName = dmnDiagramElement.getName().getValue();
            final Diagram stunnerDiagram = dmnDiagramsSession.getDiagram(diagramId);
            final Metadata metadata = buildMetadataInstance(stunnerDiagram.getMetadata().getPath());
            final Diagram diagram = dmnDiagramFactory.build(diagramName, metadata, stunnerDiagram.getGraph());

            updateClientShapeSetId(diagram);
            getOnLoadDiagramCallback().get().onSuccess(diagram);
        }
    }

    private Optional<ServiceCallback<Diagram>> getOnLoadDiagramCallback() {
        return Optional.ofNullable(onLoadDiagramCallback);
    }

    private Metadata buildMetadataInstance(final Path path) {
        final String defSetId = BindableAdapterUtils.getDefinitionSetId(DMNDefinitionSet.class);
        final String shapeSetId = BindableAdapterUtils.getShapeSetId(DMNShapeSet.class);
        return new MetadataImpl.MetadataImplBuilder(defSetId,
                                                    definitionManager)
                .setRoot(PathFactory.newPath(".", ROOT))
                .setPath(path)
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
    public void loadByPath(final Path path,
                           final ServiceCallback<Diagram> callback) {
        clientDiagramServices.getByPath(path,
                                        new ServiceCallback<Diagram<Graph, Metadata>>() {
                                            @Override
                                            public void onSuccess(final Diagram<Graph, Metadata> diagram) {
                                                callback.onSuccess(diagram);
                                            }

                                            @Override
                                            public void onError(final ClientRuntimeError error) {
                                                callback.onError(error);
                                            }
                                        });
    }

    @SuppressWarnings("unchecked")
    public void save(final Diagram diagram,
                     final ServiceCallback<Diagram<Graph, Metadata>> diagramServiceCallback) {
        // Perform update operation remote call.
        clientDiagramServices.saveOrUpdate(diagram,
                                           diagramServiceCallback);
    }

    public void save(final EditorSession session,
                     final ServiceCallback<Diagram<Graph, Metadata>> diagramServiceCallback) {
        // Update diagram's image data as thumbnail.
        final String thumbData = toImageData(session);
        final CanvasHandler canvasHandler = session.getCanvasHandler();
        final Diagram diagram = canvasHandler.getDiagram();
        diagram.getMetadata().setThumbData(thumbData);
        save(diagram,
             diagramServiceCallback);
    }

    private String toImageData(final EditorSession session) {
        return canvasExport.toImageData(session.getCanvasHandler(),
                                        CanvasURLExportSettings.build(CanvasExport.URLDataType.JPG));
    }
}
