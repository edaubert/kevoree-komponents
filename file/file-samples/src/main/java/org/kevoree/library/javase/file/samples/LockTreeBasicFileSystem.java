package org.kevoree.library.javase.file.samples;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.fileSystem.api.AbstractItem;
import org.kevoree.library.javase.fileSystem.api.LockTreeFileService;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/08/13
 * Time: 09:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class LockTreeBasicFileSystem extends LockBasicFileSystem implements LockTreeFileService {

    private TreeBasicFileSystem treeFileSystem;

    @Override
    public void start() throws Exception {
        super.start();
        treeFileSystem = new TreeBasicFileSystem();
        treeFileSystem.setDictionary(getDictionary());
        treeFileSystem.start();
    }

    @Override
    public synchronized AbstractItem getTree(String relativePath) {
        return treeFileSystem.getTree(relativePath);
    }
}
