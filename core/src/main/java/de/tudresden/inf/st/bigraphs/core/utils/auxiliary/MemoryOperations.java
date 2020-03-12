package de.tudresden.inf.st.bigraphs.core.utils.auxiliary;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;

/**
 * @author Dominik Grzelak
 */
public class MemoryOperations {

    public static DefaultFileSystemManager createFileSystemManager() throws FileSystemException {
        RamFileProvider ramFileProvider = new RamFileProvider();
        DefaultFileSystemManager fileSystemManager = new DefaultFileSystemManager();
//        // this.fileSystemManager.setLogger(log);
        fileSystemManager.addProvider("ram", ramFileProvider);
        fileSystemManager.setDefaultProvider(ramFileProvider);
        fileSystemManager.init();
        return fileSystemManager;
    }
}
