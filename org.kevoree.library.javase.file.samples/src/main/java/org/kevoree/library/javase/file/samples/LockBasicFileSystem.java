package org.kevoree.library.javase.file.samples;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.PortType;
import org.kevoree.annotation.ProvidedPort;
import org.kevoree.annotation.Provides;
import org.kevoree.library.javase.fileSystem.api.LockFileService;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/08/13
 * Time: 09:09
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Provides({
        @ProvidedPort(name = "files", type = PortType.SERVICE, className = LockFileService.class)
})
@ComponentType
public class LockBasicFileSystem extends BasicFileSystem implements LockFileService {

    protected List<String> lockFiles;

    @Override
    public void start() throws Exception {
        super.start();
        lockFiles = new ArrayList<String>();
    }

    @Override
    public boolean lock(String relativePath) {
        if (relativePath.endsWith("/")) {
            relativePath = relativePath.substring(0, relativePath.length() - 1);
        }
        return !lockFiles.contains(relativePath) && lockFiles.add(relativePath);
    }

    @Override
    public boolean unlock(String relativePath) {
        if (relativePath.endsWith("/")) {
            relativePath = relativePath.substring(0, relativePath.length() - 1);
        }
        return lockFiles.contains(relativePath) && lockFiles.remove(relativePath);
    }


    public boolean saveFile(String relativePath, byte[] data) {
        return !lockFiles.contains(relativePath) && super.saveFile(relativePath, data);
    }

    public boolean mkdirs(String relativePath) {
        String[] splitted = relativePath.split("∕");
        String path = "";
        for (String split : splitted) {
            path += "/" + split;
            if (lockFiles.contains(path)) {
                return false;
            }
        }
        return super.mkdirs(relativePath);
    }

    public boolean delete(String relativePath) {
        return !lockFiles.contains(relativePath) && super.delete(relativePath);
    }

    public boolean move(String oldRelativePath, String newRelativePath) {
        return !lockFiles.contains(oldRelativePath) && super.move(oldRelativePath, newRelativePath);
    }
}
