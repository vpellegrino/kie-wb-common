/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { ExpressionProps, LogicType } from "../../api";
import * as React from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import { LogicTypeSelector } from "../LogicTypeSelector";
import * as _ from "lodash";

export interface ContextEntryExpressionProps {
  /** The expression wrapped by the entry */
  expression: ExpressionProps;
  /** Function invoked when updating expression */
  onUpdatingRecursiveExpression: (expression: ExpressionProps) => void;
  /** Function invoked when resetting expression */
  onExpressionResetting?: () => void;
}

export const ContextEntryExpression: React.FunctionComponent<ContextEntryExpressionProps> = ({
  expression,
  onUpdatingRecursiveExpression,
  onExpressionResetting,
}) => {
  const expressionContainerRef = useRef<HTMLDivElement>(null);

  const [entryExpression, setEntryExpression] = useState(expression);

  const expressionChangedExternally = expression.logicType === undefined;
  useEffect(() => {
    setEntryExpression(_.omit(expression, "isHeadless"));
    // Every time, for an expression, its logic type is undefined, it means that corresponding entry has been just added
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expressionChangedExternally]);

  useEffect(() => {
    onUpdatingRecursiveExpression(_.omit(entryExpression, "isHeadless"));
  }, [onUpdatingRecursiveExpression, entryExpression]);

  const getLogicTypeSelectorRef = useCallback(() => {
    return expressionContainerRef.current!;
  }, []);

  const onLogicTypeUpdating = useCallback((logicType) => {
    setEntryExpression((previousSelectedExpression: ExpressionProps) => {
      const updatedExpression = {
        ...previousSelectedExpression,
        logicType: logicType,
      };
      return _.omit(updatedExpression, "isHeadless");
    });
  }, []);

  const onLogicTypeResetting = useCallback(() => {
    setEntryExpression((previousSelectedExpression: ExpressionProps) => ({
      name: previousSelectedExpression.name,
      dataType: previousSelectedExpression.dataType,
      logicType: LogicType.Undefined,
    }));
    onExpressionResetting?.();
  }, [onExpressionResetting]);

  return (
    <div className="entry-expression" ref={expressionContainerRef}>
      <LogicTypeSelector
        isHeadless={true}
        onUpdatingRecursiveExpression={onUpdatingRecursiveExpression}
        selectedExpression={entryExpression}
        onLogicTypeUpdating={onLogicTypeUpdating}
        onLogicTypeResetting={onLogicTypeResetting}
        getPlacementRef={getLogicTypeSelectorRef}
      />
    </div>
  );
};