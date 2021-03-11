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

package org.kie.workbench.common.dmn.client.boxed_expression_editor_bridge;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.HasVariable;
import org.kie.workbench.common.dmn.api.definition.model.Context;
import org.kie.workbench.common.dmn.api.definition.model.Expression;
import org.kie.workbench.common.dmn.api.definition.model.InformationItemPrimary;
import org.kie.workbench.common.dmn.api.definition.model.LiteralExpression;
import org.kie.workbench.common.dmn.api.definition.model.Relation;
import org.kie.workbench.common.dmn.api.editors.types.BuiltInTypeUtils;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.api.property.dmn.QName;
import org.kie.workbench.common.dmn.api.property.dmn.types.BuiltInType;
import org.kie.workbench.common.dmn.client.boxed_expression_editor_bridge.expression.converters.ExpressionFiller;
import org.kie.workbench.common.dmn.client.boxed_expression_editor_bridge.expression.props.ContextProps;
import org.kie.workbench.common.dmn.client.boxed_expression_editor_bridge.expression.props.ExpressionProps;
import org.kie.workbench.common.dmn.client.boxed_expression_editor_bridge.expression.props.LiteralExpressionProps;
import org.kie.workbench.common.dmn.client.boxed_expression_editor_bridge.expression.props.RelationProps;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;

@ApplicationScoped
public class BoxedExpressionEditorPanel extends Composite {
    private static HasExpression hasExpression;

    private SessionManager sessionManager;
    private Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent;

    private String containerId;

    interface ViewBinder extends UiBinder<Widget, BoxedExpressionEditorPanel> {

    }

    @SuppressWarnings("unused")
    public BoxedExpressionEditorPanel() {
        //empty constructor for injection
    }

    public BoxedExpressionEditorPanel(final SessionManager sessionManager, final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent) {
        this.sessionManager = sessionManager;
        this.refreshFormPropertiesEvent = refreshFormPropertiesEvent;
    }

    @UiField
    ScrollPanel content;

    public void setContainerId(final String containerId) {
        this.containerId = containerId;
        initWidget(GWT.<ViewBinder>create(ViewBinder.class).createAndBindUi(this));
        content.setWidth(Window.getClientWidth() + "px");

        Window.addResizeHandler(event -> content.setWidth(Window.getClientWidth() + "px"));

        content.getElement().setId(containerId);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        String expressionName = null;
        String dataType = null;
        if (hasExpression != null) {
            HasName hasName = (HasName) hasExpression;
            expressionName = hasName.getValue().getValue();
        }
        if (hasExpression instanceof HasVariable) {
            @SuppressWarnings("unchecked")
            final HasVariable<InformationItemPrimary> hasVariable = (HasVariable<InformationItemPrimary>) hasExpression;
            dataType = hasVariable.getVariable().getTypeRef().getLocalPart();
        }
        final Expression expression = hasExpression != null && hasExpression.getExpression() != null ? hasExpression.getExpression() : null;
        BoxedExpressionService.renderBoxedExpressionEditor(containerId, ExpressionFiller.buildAndFillJsInteropProp(expression, expressionName, dataType));
        BoxedExpressionService.registerBroadcastForExpression(this);
    }

    public void setExpression(final String nodeUUID, final HasExpression hasExpression) {
        BoxedExpressionEditorPanel.hasExpression = hasExpression;
        refreshFormPropertiesEvent.fire(new RefreshFormPropertiesEvent(sessionManager.getCurrentSession(), nodeUUID));
    }

    public void resetExpressionDefinition() {
        hasExpression.setExpression(null);
    }

    public void broadcastLiteralExpressionDefinition(final LiteralExpressionProps literalExpressionProps) {
        setExpressionName(literalExpressionProps);
        setTypeRef(literalExpressionProps.dataType);
        if (hasExpression.getExpression() == null) {
            hasExpression.setExpression(new LiteralExpression());
        }
        ExpressionFiller.fillLiteralExpression((LiteralExpression) hasExpression.getExpression(), literalExpressionProps);
    }

    public void broadcastContextExpressionDefinition(final ContextProps contextProps) {
        setExpressionName(contextProps);
        setTypeRef(contextProps.dataType);
        if (hasExpression.getExpression() == null) {
            hasExpression.setExpression(new Context());
        }
        ExpressionFiller.fillContextExpression((Context) hasExpression.getExpression(), contextProps);
    }

    public void broadcastRelationExpressionDefinition(final RelationProps relationProps) {
        if (hasExpression.getExpression() == null) {
            hasExpression.setExpression(new Relation());
        }
        ExpressionFiller.fillRelationExpression((Relation) hasExpression.getExpression(), relationProps);
    }

    private void setExpressionName(final ExpressionProps expressionProps) {
        final HasName hasName = (HasName) hasExpression;
        hasName.setName(new Name(expressionProps.name));
    }

    private void setTypeRef(final String dataType) {
        final QName typeRef = BuiltInTypeUtils
                .findBuiltInTypeByName(dataType)
                .orElse(BuiltInType.UNDEFINED)
                .asQName();
        if (hasExpression instanceof HasVariable) {
            @SuppressWarnings("unchecked")
            HasVariable<InformationItemPrimary> hasVariable = (HasVariable<InformationItemPrimary>) hasExpression;
            hasVariable.getVariable().setTypeRef(typeRef);
        }
    }
}
