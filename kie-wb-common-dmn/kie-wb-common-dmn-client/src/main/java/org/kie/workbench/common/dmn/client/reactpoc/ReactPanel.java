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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;

public class ReactPanel extends Composite {
    interface ViewBinder extends UiBinder<Widget, ReactPanel> {

    }

    private final String containerId;

    private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

    @UiField
    ScrollPanel content;

    public ReactPanel(final String containerId) {
        this.containerId = containerId;
        initWidget(uiBinder.createAndBindUi(this));
        content.setWidth(Window.getClientWidth() + "px");

        Window.addResizeHandler(event -> content.setWidth(Window.getClientWidth() + "px"));

        content.getElement().setId(containerId);
    }


    @Override
    protected void onLoad() {
        super.onLoad();
        renderComponent(containerId, new InputProperty("testing properties..."));
    }

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native void renderComponent(String divId, InputProperty inputProperty);
}
