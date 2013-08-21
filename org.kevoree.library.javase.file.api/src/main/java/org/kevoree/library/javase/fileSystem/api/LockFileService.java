package org.kevoree.library.javase.fileSystem.api;

/**
 * Created with IntelliJ IDEA.
 * User: tboschat
 * Date: 7/17/12
 * Time: 2:20 PM
 */

public interface LockFileService extends FileService {

    public boolean lock(String relativePath);

    public boolean unlock(String relativePath);
}
