package org.kevoree.library.javase.file.samples;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.fileSystem.api.*;

import java.io.File;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/08/13
 * Time: 08:55
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Provides({
        @ProvidedPort(name = "files", type = PortType.SERVICE, className = TreeFileService.class)
})
@ComponentType
public class TreeBasicFileSystem extends BasicFileSystem implements TreeFileService {

    @Port(name = "files", method = "getTree")
    public AbstractItem getTree(String relativePath) {
        String nameDirectory;
        File file;
        if ("/".equals(relativePath)) {
            nameDirectory = "/";
            file = baseFolder;
        } else {
            file = new File(baseFolder.getAbsolutePath() + File.separator + relativePath.replace("/", File.separator));
            nameDirectory = file.getName();
        }

        FolderItem root = new FolderItem();
        root.setName(nameDirectory);
        root.setPath("/");
        process(file, root);
        sortList(root.getChilds());
        return root;
    }

    public void process(File file, FolderItem item) {
//        if (!file.getName().contains(".git") && !file.getName().endsWith("~")) {
            if (file.isFile()) {
                FileItem itemToAdd = new FileItem();
                itemToAdd.setName(file.getName());
                itemToAdd.setParent(item);
                itemToAdd.setPath(getRelativePath(file.getPath()));
                item.add(itemToAdd);
            } else if (file.isDirectory()) {
                FolderItem folder = new FolderItem();
                folder.setName(file.getName());
                folder.setParent(item);
                folder.setPath(getRelativePath(file.getPath() + "/"));
                item.add(folder);
                File[] listOfFiles = file.listFiles();
                if (listOfFiles != null) {
                    for (File listOfFile : listOfFiles) process(listOfFile, folder);
                }
            }
//        }
    }

    private void sortList(List<AbstractItem> list) {
        int indexCurrentChar = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(indexCurrentChar).getClass() == FileItem.class) {
                list.add(list.get(indexCurrentChar));
                list.remove(indexCurrentChar);
            } else {
                sortList(((FolderItem) list.get(indexCurrentChar)).getChilds());
                indexCurrentChar++;
            }
        }
    }
}
