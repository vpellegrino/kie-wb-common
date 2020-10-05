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

package org.kie.workbench.common.dmn.client.editors.drd;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import elemental2.dom.DomGlobal;
import org.kie.workbench.common.dmn.api.DMNDefinitionSet;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.HasText;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagram;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagramElement;
import org.kie.workbench.common.dmn.api.definition.model.DMNModelInstrumentedBase;
import org.kie.workbench.common.dmn.api.definition.model.Definitions;
import org.kie.workbench.common.dmn.api.graph.DMNDiagramUtils;
import org.kie.workbench.common.dmn.client.commands.clone.DMNDeepCloneProcess;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramSelected;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramTuple;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramsSession;
import org.kie.workbench.common.dmn.client.marshaller.common.DMNGraphUtils;
import org.kie.workbench.common.dmn.client.marshaller.unmarshall.DMNUnmarshaller;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bound;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.HasContentDefinitionId;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.ControlPoint;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2DConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnectorImpl;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.kie.workbench.common.stunner.core.util.UUID;

import static org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils.getDefinitionId;

@ApplicationScoped
public class DRDContextMenuService {

    private static String NEW_DIAGRAM_NAME = "new-diagram";

    private final DMNDiagramsSession dmnDiagramsSession;

    private final FactoryManager factoryManager;

    private final Event<DMNDiagramSelected> selectedEvent;

    private final DMNDiagramUtils dmnDiagramUtils;

    private final DMNDeepCloneProcess dmnDeepCloneProcess;

    private final DMNUnmarshaller dmnUnmarshaller;

    @Inject
    public DRDContextMenuService(final DMNDiagramsSession dmnDiagramsSession,
                                 final FactoryManager factoryManager,
                                 final Event<DMNDiagramSelected> selectedEvent,
                                 final DMNDiagramUtils dmnDiagramUtils,
                                 final DMNDeepCloneProcess dmnDeepCloneProcess,
                                 final DMNUnmarshaller dmnUnmarshaller) {
        this.dmnDiagramsSession = dmnDiagramsSession;
        this.factoryManager = factoryManager;
        this.selectedEvent = selectedEvent;
        this.dmnDiagramUtils = dmnDiagramUtils;
        this.dmnDeepCloneProcess = dmnDeepCloneProcess;
        this.dmnUnmarshaller = dmnUnmarshaller;
    }

    public List<DMNDiagramTuple> getDiagrams() {
        return dmnDiagramsSession.getDMNDiagrams();
    }

    public void addToNewDRD(final Collection<Node<? extends Definition<?>, Edge>> selectedNodes) {
        final DMNDiagramElement dmnElement = makeDmnDiagramElement();
        final Diagram stunnerElement = buildStunnerElement(dmnElement);

        processNodes(selectedNodes, dmnElement, stunnerElement);

        addDmnDiagramElementToDRG(dmnElement);

        dmnDiagramsSession.add(dmnElement, stunnerElement);
        selectedEvent.fire(new DMNDiagramSelected(dmnElement));
    }

    public void addToExistingDRD(final DMNDiagramTuple dmnDiagram,
                                 final Collection<Node<? extends Definition<?>, Edge>> selectedNodes) {
        processNodes(selectedNodes, dmnDiagram.getDMNDiagram(), dmnDiagram.getStunnerDiagram());

        selectedEvent.fire(new DMNDiagramSelected(dmnDiagram.getDMNDiagram()));
    }

    private void processNodes(final Collection<Node<? extends Definition<?>, Edge>> selectedNodes, final DMNDiagramElement dmnElement, final Diagram stunnerElement) {
        Map<String, Node> clonedSelectedNodesMap = new HashMap<>();
        selectedNodes.forEach(node -> addNodesToDRD(node, dmnElement, stunnerElement)
                        .ifPresent(clonedNode -> clonedSelectedNodesMap.put(node.getUUID(), clonedNode)));

        selectedNodes.forEach(selectedNode ->
                                      cloneOutEdges(clonedSelectedNodesMap, selectedNode.getUUID(), selectedNode.getOutEdges()));
    }

    @SuppressWarnings("unchecked")
    private Optional<Node> addNodesToDRD(final Node<? extends Definition<?>, Edge> node,
                                         final DMNDiagramElement dmnElement,
                                         final Diagram stunnerElement) {
            final Definition<?> content = node.getContent();
            final Object definition = ((View) content).getDefinition();
            if (definition instanceof HasContentDefinitionId) {
                final Node<?, ?> dmnDiagramRoot = DMNGraphUtils.findDMNDiagramRoot(stunnerElement.getGraph());
                final Node clone = cloneNode(node, dmnElement);

                connectRootWithChild(dmnDiagramRoot, clone);

                stunnerElement
                        .getGraph()
                        .addNode(clone);

                return Optional.of(clone);
            }
            return Optional.empty();
    }

    private void connectRootWithChild(final Node dmnDiagramRoot,
                                      final Node child) {
        final String uuid = UUID.uuid();
        final Edge<Child, Node> edge = new EdgeImpl<>(uuid);
        edge.setContent(new Child());
        connectEdge(edge, dmnDiagramRoot, child);
        final Definitions definitions = ((DMNDiagram) ((View) dmnDiagramRoot.getContent()).getDefinition()).getDefinitions();
        final DMNModelInstrumentedBase childDRG = (DMNModelInstrumentedBase) ((View) child.getContent()).getDefinition();
        childDRG.setParent(definitions);
    }

    private Collection<Edge> cloneOutEdges(final Map<String, Node> clonedSelectedNodesMap, final String sourceNodeUUID, final List<Edge> outEdges) {
        return outEdges
                .stream()
                .filter(edge -> edge.getContent() instanceof View)
                .filter(edge -> clonedSelectedNodesMap.containsKey(edge.getTargetNode().getUUID()))
                .map(edge -> {
                    final ViewConnectorImpl<Object>  edgeContentView = (ViewConnectorImpl<Object> ) edge.getContent();
                    final Edge clonedEdge = new EdgeImpl<>(UUID.uuid());
                    ViewConnectorImpl<Object> clonedEdgeContent = new ViewConnectorImpl<>(
                            dmnDeepCloneProcess.clone(edgeContentView.getDefinition()),
                            cloneBounds(edgeContentView.getBounds())
                    );
                    edgeContentView.getSourceConnection().ifPresent(sourceConnection -> clonedEdgeContent.setSourceConnection(new Point2DConnection(sourceConnection.getLocation())));
                    edgeContentView.getTargetConnection().ifPresent(targetConnection -> clonedEdgeContent.setTargetConnection(new Point2DConnection(targetConnection.getLocation())));
                    clonedEdgeContent.setControlPoints(Arrays.stream(edgeContentView.getControlPoints()).map(controlPoint -> ControlPoint.build(controlPoint.getLocation())).toArray(ControlPoint[]::new));

                    connectEdge(clonedEdge, clonedSelectedNodesMap.get(sourceNodeUUID), clonedSelectedNodesMap.get(edge.getTargetNode().getUUID()));
                    clonedEdge.setContent(clonedEdgeContent);
                    DomGlobal.console.log(edgeContentView.getBounds(), ((View) clonedEdge.getContent()).getBounds());
                    DomGlobal.console.log(edge, clonedEdge);
                    return clonedEdge;
                })
                .collect(Collectors.toList());
    }

    private void connectEdge(final Edge edge,
                             final Node source,
                             final Node target) {
        edge.setSourceNode(source);
        edge.setTargetNode(target);
        source.getOutEdges().add(edge);
        target.getInEdges().add(edge);
    }

    @SuppressWarnings("unchecked")
    private Node cloneNode(final Node nodeToClone, final DMNDiagramElement dmnElement) {
        final View content = (View) nodeToClone.getContent();
        final Bounds bounds = content.getBounds();
        final Object definition = content.getDefinition();

        final Node clonedNode = factoryManager.newElement(UUID.uuid(), getDefinitionId(definition.getClass())).asNode();
        final View clonedContent = (View) clonedNode.getContent();
        clonedContent.setDefinition(cloneDefinition(dmnElement, definition));
        clonedContent.setBounds(cloneBounds(bounds));
        return clonedNode;
    }

    private HasContentDefinitionId cloneDefinition(final DMNDiagramElement dmnElement, final Object definition) {
        final HasContentDefinitionId originalDefinition = (HasContentDefinitionId) definition;
        final HasContentDefinitionId clonedDefinition = dmnDeepCloneProcess.clone(originalDefinition);
        clonedDefinition.setContentDefinitionId(originalDefinition.getContentDefinitionId());
        clonedDefinition.setDiagramId(dmnElement.getId().getValue());

        if (definition instanceof HasText && clonedDefinition instanceof HasText) {
            HasText hasText = (HasText) definition;
            ((HasText) clonedDefinition).setText(hasText.getText());
        }

        if (definition instanceof HasName && clonedDefinition instanceof HasName) {
            HasName hasName = (HasName) definition;
            ((HasName) clonedDefinition).setName(hasName.getValue());
        }
        return clonedDefinition;
    }

    private Bounds cloneBounds(final Bounds bounds) {
        final Bound ul = bounds.getUpperLeft();
        final Bound lr = bounds.getLowerRight();
        return Bounds.create(ul.getX(), ul.getY(), lr.getX(), lr.getY());
    }

    public void removeFromCurrentDRD(final Collection<Node<? extends Definition<?>, Edge>> selectedNodes) {
        final Diagram diagram = dmnDiagramsSession
                .getCurrentDiagram()
                .orElse(dmnDiagramsSession.getDRGDiagram());

        selectedNodes.forEach(node -> diagram.getGraph().removeNode(node.getUUID()));

        dmnDiagramsSession
                .getCurrentDMNDiagramElement()
                .ifPresent(dmnDiagramElement -> selectedEvent.fire(new DMNDiagramSelected(dmnDiagramElement)));
    }

    private void addDmnDiagramElementToDRG(final DMNDiagramElement dmnElement) {
        dmnDiagramUtils
                .getDefinitions(dmnDiagramsSession.getDRGDiagram())
                .getDiagramElements()
                .add(dmnElement);
    }

    private Diagram buildStunnerElement(final DMNDiagramElement dmnElement) {
        final String diagramId = dmnElement.getId().getValue();
        return factoryManager.newDiagram(diagramId,
                                         BindableAdapterUtils.getDefinitionSetId(DMNDefinitionSet.class),
                                         getMetadata());
    }

    private DMNDiagramElement makeDmnDiagramElement() {
        final DMNDiagramElement diagramElement = new DMNDiagramElement();
        diagramElement.getName().setValue(getUniqueName());
        return diagramElement;
    }

    private String getUniqueName() {

        final List<String> currentDiagramNames = getCurrentDiagramNames();

        if (currentDiagramNames.contains(NEW_DIAGRAM_NAME)) {
            return getUniqueName(2, currentDiagramNames);
        }

        return NEW_DIAGRAM_NAME;
    }

    private List<String> getCurrentDiagramNames() {
        return dmnDiagramsSession
                .getDMNDiagrams()
                .stream()
                .map(e -> e.getDMNDiagram().getName().getValue())
                .collect(Collectors.toList());
    }

    private String getUniqueName(final int seeds,
                                 final List<String> currentDiagramNames) {

        final String newDiagramName = NEW_DIAGRAM_NAME + "-" + seeds;

        if (currentDiagramNames.contains(newDiagramName)) {
            return getUniqueName(seeds + 1, currentDiagramNames);
        }

        return newDiagramName;
    }

    private Metadata getMetadata() {
        return dmnDiagramsSession.getDRGDiagram().getMetadata();
    }
}
