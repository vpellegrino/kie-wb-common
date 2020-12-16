/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import { Dispatch, RefObject, SetStateAction, useCallback, useEffect, useRef, useState } from "react";

export function useContextMenuHandler(): [
  RefObject<HTMLDivElement>,
  string,
  string,
  boolean,
  Dispatch<SetStateAction<boolean>>
] {
  const wrapperRef = useRef<HTMLDivElement>(null);

  const [xPos, setXPos] = useState("0px");
  const [yPos, setYPos] = useState("0px");
  const [contextMenuVisible, setContextMenuVisible] = useState(false);

  const hideContextMenu = useCallback(() => {
    contextMenuVisible && setContextMenuVisible(false);
  }, [contextMenuVisible]);

  const showContextMenu = useCallback(
    (event: MouseEvent) => {
      if (wrapperRef.current && wrapperRef.current === event.target) {
        event.preventDefault();
        setXPos(`${event.pageX}px`);
        setYPos(`${event.pageY}px`);
        setContextMenuVisible(true);
      }
    },
    [setXPos, setYPos]
  );

  useEffect(() => {
    document.addEventListener("click", hideContextMenu);
    document.addEventListener("contextmenu", showContextMenu);
    return () => {
      document.removeEventListener("click", hideContextMenu);
      document.removeEventListener("contextmenu", showContextMenu);
    };
  });

  return [wrapperRef, xPos, yPos, contextMenuVisible, setContextMenuVisible];
}
