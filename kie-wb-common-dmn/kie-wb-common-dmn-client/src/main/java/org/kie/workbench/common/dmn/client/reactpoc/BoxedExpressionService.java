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

package org.kie.workbench.common.dmn.client.reactpoc;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import org.kie.workbench.common.dmn.client.reactpoc.expression.props.ExpressionProps;

public class BoxedExpressionService {
    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native void renderBoxedExpressionEditor(String divId, ExpressionProps expressionProps);

    public static void registerBroadcastForExpression(final ReactPanel reactPanel) {
        createNamespace();
        registerResetExpressionDefinition(reactPanel);
        registerBroadcastForLiteralExpression(reactPanel);
    }

    private static native void createNamespace()/*-{
        $wnd["beeApi"] = {};
    }-*/;

    private static native void registerResetExpressionDefinition(final ReactPanel reactPanel)/*-{
        $wnd["beeApi"].resetExpressionDefinition = function() {
            return reactPanel.@ReactPanel::resetExpressionDefinition(*)();
        };
    }-*/;

    private static native void registerBroadcastForLiteralExpression(final ReactPanel reactPanel)/*-{
        $wnd["beeApi"].broadcastLiteralExpressionDefinition = function(literalExpressionDefinition) {
            return reactPanel.@ReactPanel::broadcastLiteralExpressionDefinition(*)(literalExpressionDefinition);
        };
    }-*/;
}
