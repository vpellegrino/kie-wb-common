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

package org.kie.workbench.common.dmn.client.widgets.drd.controls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import elemental2.dom.DomGlobal;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.DropDownMenu;

public class DRDContextMenuViewImpl extends Composite implements DRDContextMenuView{

    interface Binder
            extends
            UiBinder<Widget, DRDContextMenuViewImpl> {
    }

    private static Binder uiBinder = GWT.create(Binder.class );

    private DRDContextMenu presenter;

    @UiField
    DropDownMenu contextMenuDropdown;

    @UiField
    AnchorListItem menuItemPreferences;

    @UiField
    AnchorListItem menuItemLogout;

    public DRDContextMenuViewImpl() {
        initWidget( uiBinder.createAndBindUi( this ) );

        menuItemLogout.addClickHandler(clickEvent -> DomGlobal.console.log(">>>>> menuItemLogout"));
        menuItemPreferences.addClickHandler(clickEvent -> DomGlobal.console.log(">>>>> menuItemPreferences"));
    }

    @Override
    public void setPresenter(final DRDContextMenu presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clear() {
        contextMenuDropdown.clear();
    }

    @Override
    public void show() {
        setVisible( true );
    }

    @Override
    public void hide() {
        setVisible( false );
    }

}
