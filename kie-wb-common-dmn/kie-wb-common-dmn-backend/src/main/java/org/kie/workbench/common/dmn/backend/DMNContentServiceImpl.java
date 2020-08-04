/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.dmn.backend;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.dmn.api.DMNContentService;
import org.kie.workbench.common.dmn.backend.common.DMNIOHelper;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;

@Service
@ApplicationScoped
public class DMNContentServiceImpl implements DMNContentService {

    private final IOService ioService;

    private final DMNIOHelper ioHelper;

    @Inject
    public DMNContentServiceImpl(final @Named("ioStrategy") IOService ioService,
                                 final DMNIOHelper ioHelper) {
        this.ioService = ioService;
        this.ioHelper = ioHelper;
    }

    @Override
    public String getContent(final Path path) {
        try {
            final InputStream inputStream = ioService.newInputStream(Paths.convert(path));
            return ioHelper.isAsString(inputStream);
        } catch (final Exception e) {
            return "";
        }
    }
}
