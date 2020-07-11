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

package org.kie.workbench.common.dmn.client.editors.drd.contextmenu;

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.HasListSelectorControl;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.ListSelector;

@Templated
public class DRDContextMenuView implements DRDContextMenu.View,
                                           HasListSelectorControl {

    private DRDContextMenu presenter;

    @Inject
    @DataField("list-selector")
    private ListSelector listSelector;

    @Override
    public void init(final DRDContextMenu presenter) {
        this.presenter = presenter;
        listSelector.bind(this, 0, 0);
        listSelector.show();
    }

    @Override
    public List<ListSelectorItem> getItems(int uiRowIndex, int uiColumnIndex) {
        return presenter.getItems();
    }

    @Override
    public void onItemSelected(ListSelectorItem item) {
        presenter.onItemSelected(item);
    }
}
