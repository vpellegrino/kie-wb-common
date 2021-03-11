/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import org.kie.workbench.common.dmn.client.boxed_expression_editor_bridge.expression.props.ExpressionProps;

public class BoxedExpressionService {
    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native void renderBoxedExpressionEditor(String divId, ExpressionProps expressionProps);

    public static void registerBroadcastForExpression(final BoxedExpressionEditorPanel boxedExpressionEditorPanel) {
        createNamespace();
        registerResetExpressionDefinition(boxedExpressionEditorPanel);
        registerBroadcastForLiteralExpression(boxedExpressionEditorPanel);
        registerBroadcastForContextExpression(boxedExpressionEditorPanel);
        registerBroadcastForRelationExpression(boxedExpressionEditorPanel);
    }

    private static native void createNamespace()/*-{
        $wnd["beeApi"] = {};
    }-*/;

    private static native void registerResetExpressionDefinition(final BoxedExpressionEditorPanel boxedExpressionEditorPanel)/*-{
        $wnd["beeApi"].resetExpressionDefinition = function() {
            return boxedExpressionEditorPanel.@BoxedExpressionEditorPanel::resetExpressionDefinition(*)();
        };
    }-*/;

    private static native void registerBroadcastForLiteralExpression(final BoxedExpressionEditorPanel boxedExpressionEditorPanel)/*-{
        $wnd["beeApi"].broadcastLiteralExpressionDefinition = function(literalExpressionDefinition) {
            return boxedExpressionEditorPanel.@BoxedExpressionEditorPanel::broadcastLiteralExpressionDefinition(*)(literalExpressionDefinition);
        };
    }-*/;

    private static native void registerBroadcastForContextExpression(final BoxedExpressionEditorPanel boxedExpressionEditorPanel)/*-{
        $wnd["beeApi"].broadcastContextExpressionDefinition = function(contextExpressionDefinition) {
            return boxedExpressionEditorPanel.@BoxedExpressionEditorPanel::broadcastContextExpressionDefinition(*)(contextExpressionDefinition);
        };
    }-*/;

    private static native void registerBroadcastForRelationExpression(final BoxedExpressionEditorPanel boxedExpressionEditorPanel)/*-{
        $wnd["beeApi"].broadcastRelationExpressionDefinition = function(relationExpressionDefinition) {
            return boxedExpressionEditorPanel.@BoxedExpressionEditorPanel::broadcastRelationExpressionDefinition(*)(relationExpressionDefinition);
        };
    }-*/;
}
