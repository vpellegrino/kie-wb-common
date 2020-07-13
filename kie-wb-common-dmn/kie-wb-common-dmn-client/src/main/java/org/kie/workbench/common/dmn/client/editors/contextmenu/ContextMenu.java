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

package org.kie.workbench.common.dmn.client.editors.contextmenu;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.HasListSelectorControl.ListSelectorHeaderItem;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.HasListSelectorControl.ListSelectorItem;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.HasListSelectorControl.ListSelectorTextItem;
import org.uberfire.client.mvp.UberElemental;
import org.uberfire.mvp.Command;

public class ContextMenu {

    private final List<ListSelectorItem> menuItems;

    private View view;

    @Inject
    public ContextMenu(final View view) {
        this.view = view;
        this.menuItems = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public void show() { view.show(); }

    public void hide() { view.hide(); }

    public HTMLElement getElement() {
        return view.getElement();
    }

    public List<ListSelectorItem> getItems() {
        return menuItems;
    }

    public void addTextMenuItem(String itemName, boolean isEnabled, Command command) {
        menuItems.add(ListSelectorTextItem.build(itemName, isEnabled, command));
    }

    public void resetMenuItems() {
        menuItems.clear();
    }

    public void setHeaderMenu(final String title, final String iconClass) {
        menuItems.add(ListSelectorHeaderItem.buildWithIcon(title, iconClass));
    }

    public interface View extends UberElemental<ContextMenu>, IsElement {
        void show();
        void hide();
    }
}
