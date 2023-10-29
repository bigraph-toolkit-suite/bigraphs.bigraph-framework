package org.bigraphs.framework.core.utils.auxiliary;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;

import java.util.Objects;

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
