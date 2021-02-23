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

package org.kie.workbench.common.dmn.client.reactpoc.expression.props;

import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType
public class ExpressionProps {

    private final String name;

    private final String dataType;

    private final String logicType;

    public ExpressionProps(final String name, final String dataType, final String logicType) {
        this.name = name;
        this.dataType = dataType;
        this.logicType = logicType;
        Js.asPropertyMap(this).set("name", name);
        Js.asPropertyMap(this).set("dataType", dataType);
        Js.asPropertyMap(this).set("logicType", logicType);
    }

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public String getLogicType() {
        return logicType;
    }
}
