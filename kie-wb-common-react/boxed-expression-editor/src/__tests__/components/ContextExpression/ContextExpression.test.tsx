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

import { render } from "@testing-library/react";
import { usingTestingBoxedExpressionI18nContext } from "../test-utils";
import { ContextExpression } from "../../../components/ContextExpression";
import * as React from "react";
import { DataType, LogicType } from "../../../api";

describe("ContextExpression tests", () => {
  const name = "contextName";
  const dataType = DataType.Boolean;
  test("should show a table with two rows: two context entries, where last is representing the result", () => {
    const { container } = render(
      usingTestingBoxedExpressionI18nContext(
        <ContextExpression logicType={LogicType.Context} name={name} dataType={dataType} />
      ).wrapper
    );

    expect(container.querySelector(".context-expression")).toBeTruthy();
    expect(container.querySelector(".context-expression table")).toBeTruthy();
    expect(container.querySelectorAll(".context-expression table tbody tr")).toHaveLength(2);
    expect(container.querySelector(".context-expression table tbody tr:first-of-type")).toContainHTML("ContextEntry-1");
    expect(container.querySelector(".context-expression table tbody tr:last-of-type")).toContainHTML("result");
  });

  test("should show a table with one row for each passed entry, plus the passed entry result", () => {
    const firstEntry = "first entry";
    const firstDataType = DataType.Boolean;
    const firstExpression = { name: "expressionName", dataType: DataType.Any, logicType: LogicType.LiteralExpression };
    const secondEntry = "second entry";
    const secondDataType = DataType.Date;
    const secondExpression = { name: "anotherName", dataType: DataType.Undefined };
    const resultEntry = "result entry";
    const resultDataType = DataType.Undefined;

    const contextEntries = [
      {
        entryInfo: {
          name: firstEntry,
          dataType: firstDataType,
        },
        entryExpression: firstExpression,
      },
      {
        entryInfo: {
          name: secondEntry,
          dataType: secondDataType,
        },
        entryExpression: secondExpression,
      },
    ];

    const result = {
      name: resultEntry,
      dataType: resultDataType,
      expression: {},
    };

    const { container } = render(
      usingTestingBoxedExpressionI18nContext(
        <ContextExpression
          logicType={LogicType.Context}
          name={name}
          dataType={dataType}
          contextEntries={contextEntries}
          result={result}
        />
      ).wrapper
    );

    expect(container.querySelector(".context-expression")).toBeTruthy();
    expect(container.querySelector(".context-expression table")).toBeTruthy();
    expect(container.querySelectorAll(".context-expression table tbody tr")).toHaveLength(3);

    checkEntryContent(contextEntry(container, 1), { name: firstEntry, dataType: firstDataType });
    checkEntryContent(contextEntry(container, 2), { name: secondEntry, dataType: secondDataType });
    checkEntryContent(contextEntry(container, 3), { name: "result", dataType: "" });

    checkEntryStyle(contextEntry(container, 1), "logic-type-selected");
    checkEntryLogicType(contextEntry(container, 1), "literal-expression");
    checkEntryStyle(contextEntry(container, 2), "logic-type-not-present");
    checkEntryStyle(contextEntry(container, 3), "logic-type-not-present");
  });
});

function contextEntry(container: Element, index: number): Element | null {
  return container.querySelector(`.context-expression table tbody tr:nth-of-type(${index})`);
}

function checkEntryContent(entry: Element | null, entryRecordInfo: { name: string; dataType: string }): void {
  expect(entry).toContainHTML(entryRecordInfo.name);
  expect(entry).toContainHTML(entryRecordInfo.dataType);
}

function checkEntryStyle(entry: Element | null, cssClass: string): void {
  expect(entry?.querySelector(".entry-expression")?.firstChild).toHaveClass(cssClass);
}

function checkEntryLogicType(entry: Element | null, cssClass: string): void {
  expect(entry?.querySelector(".entry-expression")?.firstChild?.firstChild).toHaveClass(cssClass);
}