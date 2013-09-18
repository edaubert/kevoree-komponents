package org.kevoree.library.javase.fileSystem.api;

import org.kevoree.library.javase.fileSystem.api.tree.AbstractItem;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/08/13
 * Time: 08:49
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface TreeFileService extends FileService {

    public AbstractItem getTree(String relativePath);
}
