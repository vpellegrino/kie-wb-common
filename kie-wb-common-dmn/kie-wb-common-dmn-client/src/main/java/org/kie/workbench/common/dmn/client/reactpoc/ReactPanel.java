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

package org.kie.workbench.common.dmn.client.reactpoc;

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
import org.kie.workbench.common.dmn.api.definition.model.DecisionTable;
import org.kie.workbench.common.dmn.api.definition.model.Expression;
import org.kie.workbench.common.dmn.api.definition.model.FunctionDefinition;
import org.kie.workbench.common.dmn.api.definition.model.InformationItemPrimary;
import org.kie.workbench.common.dmn.api.definition.model.Invocation;
import org.kie.workbench.common.dmn.api.definition.model.IsLiteralExpression;
import org.kie.workbench.common.dmn.api.definition.model.List;
import org.kie.workbench.common.dmn.api.definition.model.LiteralExpression;
import org.kie.workbench.common.dmn.api.definition.model.Relation;
import org.kie.workbench.common.dmn.api.editors.types.BuiltInTypeUtils;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.api.property.dmn.QName;
import org.kie.workbench.common.dmn.api.property.dmn.Text;
import org.kie.workbench.common.dmn.api.property.dmn.types.BuiltInType;
import org.kie.workbench.common.dmn.client.reactpoc.expression.props.ExpressionProps;
import org.kie.workbench.common.dmn.client.reactpoc.expression.props.LiteralExpressionProps;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;

@ApplicationScoped
public class ReactPanel extends Composite {

    private SessionManager sessionManager;
    private Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent;

    private String containerId;
    private HasExpression hasExpression;

    interface ViewBinder extends UiBinder<Widget, ReactPanel> {

    }

    @SuppressWarnings("unused")
    public ReactPanel() {
        //empty constructor for injection
    }

    public ReactPanel(final SessionManager sessionManager, final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent) {
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
            HasVariable<InformationItemPrimary> hasVariable = (HasVariable<InformationItemPrimary>) hasExpression;
            dataType = hasVariable.getVariable().getTypeRef().getLocalPart();
        }
        ExpressionProps expressionProps = new ExpressionProps(expressionName, dataType, null);
        if (hasExpression != null && hasExpression.getExpression() != null) {
            final Expression wrappedExpression = hasExpression.getExpression();
            if (wrappedExpression instanceof IsLiteralExpression) {
                final LiteralExpression literalExpression = (LiteralExpression) wrappedExpression;
                expressionProps = new LiteralExpressionProps(expressionName, dataType, literalExpression.getText().getValue());
            } else if (wrappedExpression instanceof Context) {

            } else if (wrappedExpression instanceof Relation) {

            } else if (wrappedExpression instanceof List) {

            } else if (wrappedExpression instanceof Invocation) {

            } else if (wrappedExpression instanceof FunctionDefinition) {

            } else if (wrappedExpression instanceof DecisionTable) {

            }
        }
        BoxedExpressionService.renderBoxedExpressionEditor(containerId, expressionProps);
        BoxedExpressionService.registerBroadcastForExpression(this);
    }

    public void setExpression(final String nodeUUID, final HasExpression hasExpression) {
        this.hasExpression = hasExpression;
        refreshFormPropertiesEvent.fire(new RefreshFormPropertiesEvent(sessionManager.getCurrentSession(), nodeUUID));
    }

    public void broadcastLiteralExpressionDefinition(final LiteralExpressionProps literalExpressionProps) {
        final HasName hasName = (HasName) hasExpression;
        final QName typeRef = BuiltInTypeUtils.findBuiltInTypeByName(literalExpressionProps.dataType).orElse(BuiltInType.UNDEFINED).asQName();
        hasName.setName(new Name(literalExpressionProps.name));
        if (hasExpression instanceof HasVariable) {
            @SuppressWarnings("unchecked")
            HasVariable<InformationItemPrimary> hasVariable = (HasVariable<InformationItemPrimary>) hasExpression;
            hasVariable.getVariable().setTypeRef(typeRef);
        }
        if (hasExpression.getExpression() == null) {
            hasExpression.setExpression(new LiteralExpression());
        }
        final LiteralExpression literalExpression = (LiteralExpression) hasExpression.getExpression();
        literalExpression.setText(new Text(literalExpressionProps.content));
        literalExpression.setTypeRef(typeRef);
    }
}
