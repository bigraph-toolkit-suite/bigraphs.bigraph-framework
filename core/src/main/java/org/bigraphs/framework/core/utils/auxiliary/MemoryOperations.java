/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.core.utils.auxiliary;

import java.util.Objects;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;

/**
 * Some utility methods for memory-based operations.
 *
 * @author Dominik Grzelak
 */
public class MemoryOperations {

    private static volatile MemoryOperations instance = null;
    private static volatile DefaultFileSystemManager fileSystemManager = null;
    private static volatile RamFileProvider ramFileProvider = null;

    private MemoryOperations() {
    }

    public static MemoryOperations getInstance() {
        if (instance == null) {
            synchronized (MemoryOperations.class) {
                if (instance == null) {
                    instance = new MemoryOperations();
                }
            }
        }
        return instance;
    }

    public DefaultFileSystemManager createFileSystemManager() throws FileSystemException {
        if (Objects.isNull(fileSystemManager)) {
            ramFileProvider = new RamFileProvider();
            fileSystemManager = new DefaultFileSystemManager();
//        // this.fileSystemManager.setLogger(log);
            fileSystemManager.addProvider("ram", ramFileProvider);
            fileSystemManager.setDefaultProvider(ramFileProvider);
            fileSystemManager.init();
        }
        return fileSystemManager;
    }
}
