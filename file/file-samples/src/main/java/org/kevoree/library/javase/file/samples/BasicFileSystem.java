package org.kevoree.library.javase.file.samples;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.javase.fileSystem.api.FileService;
import org.kevoree.log.Log;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 22/11/11
 * Time: 20:02
 */
@Library(name = "file")
@Provides({
        @ProvidedPort(name = "files", type = PortType.SERVICE, className = FileService.class)
})
@DictionaryType({
        @DictionaryAttribute(name = "basedir", optional = false)
})
@ComponentType
public class BasicFileSystem extends AbstractComponentType implements FileService {

    //	private String baseURL = "";
    protected File baseFolder = null;

    @Start
    public void start() throws Exception {
        baseFolder = new File(this.getDictionary().get("basedir").toString());
        if ((!baseFolder.exists() && baseFolder.mkdirs()) || (baseFolder.exists() && baseFolder.isDirectory())) {
            Log.debug("FileSystem initialized with {} as root", baseFolder.getAbsolutePath());
        } else {
            throw new Exception("Unable to initialize file system " + getName() + " because the basedir attribute define a file instead of a folder.");
        }
    }

    @Stop
    public void stop() {
//NOP
    }

    @Update
    public void update() throws Exception {
        if (!getDictionary().get("basedir").toString().equals(baseFolder.getAbsolutePath())) {
            start();
        }
    }

    protected String[] getFlatFiles(File base, String relativePath, boolean root, Set<String> filters) {
        Set<String> files = new HashSet<String>();
        if (base.exists() && !base.getName().startsWith(".")) {
            if (base.isDirectory()) {
                File[] childs = base.listFiles();
                if (childs != null) {
                    for (File child : childs) {
                        if (root) {
                            Collections.addAll(files, getFlatFiles(child, relativePath, false, filters));
                        } else {
                            Collections.addAll(files, getFlatFiles(child, relativePath + "/" + base.getName(), false, filters));
                        }
                    }
                }
            } else {
                boolean filtered = false;
                if (filters != null) {
                    filtered = true;
                    for (String filter : filters) {
                        Pattern pattern = Pattern.compile(filter);
                        Matcher m = pattern.matcher(base.getAbsolutePath().substring(baseFolder.getAbsolutePath().length()));
                        if (m.matches()) {
                            filtered = false;
                        }
                    }
                }
                if (!filtered) {
                    if (!relativePath.endsWith("/") && !base.getName().startsWith("/")) {
                        files.add(relativePath + "/" + base.getName());
                    } else {
                        files.add(relativePath + base.getName());
                    }
                }
            }
        }
        String[] filesPath = new String[files.size()];
        files.toArray(filesPath);
        return filesPath;
    }


    @Port(name = "files", method = "list")
    public synchronized String[] list() {
        return getFlatFiles(baseFolder, "", true, null);
    }

    @Port(name = "files", method = "listFromFilter")
    public synchronized String[] listFromFilter(Set<String> filter) {
        return getFlatFiles(baseFolder, "/", true, filter);
    }

    @Port(name = "files", method = "getFileContent")
    public synchronized byte[] getFileContent(String relativePath) {
        File f = new File(baseFolder.getAbsolutePath() + File.separator + relativePath);
        if (f.exists()) {
            try {

                FileInputStream fs = new FileInputStream(f);
                byte[] result = convertStream(fs);
                fs.close();

                return result;
            } catch (Exception e) {
                Log.error("Error while getting file ", e);
            }
        } else {
            Log.debug("No file exist = {}", baseFolder.getAbsolutePath() + File.separator + relativePath);
            return new byte[0];
        }
        return new byte[0];
    }

    /*@Port(name = "files", method = "getAbsolutePath")
    public String getAbsolutePath(String relativePath) {
        if (new File(baseFolder.getAbsolutePath() + File.separator + relativePath).exists()) {
            return new File(baseFolder.getAbsolutePath() + File.separator + relativePath).getAbsolutePath();
        } else {
            return null;
        }
    }*/

    @Port(name = "files", method = "mkdirs")
    public synchronized boolean mkdirs(String relativePath) {
        return new File(baseFolder.getAbsolutePath() + File.separator + relativePath).mkdirs();
    }

    @Port(name = "files", method = "delete")
    public synchronized boolean delete(String relativePath) {
        String cleanedRelativePath = relativePath;
        if (cleanedRelativePath.startsWith("/")) {
            cleanedRelativePath = cleanedRelativePath.substring(1);
        }
        if (cleanedRelativePath.endsWith("/")) {
            cleanedRelativePath = cleanedRelativePath.substring(0, cleanedRelativePath.length() - 1);
        }

        cleanedRelativePath = cleanedRelativePath.replace("/", File.separator);

        return deleteRecursively(cleanedRelativePath);

    }

    private synchronized boolean deleteRecursively(String relativePath) {
        File file = new File(baseFolder.getAbsolutePath() + File.separator + relativePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        } else if (file.exists()) {
            boolean deleteRecursively = true;
            for (File child : file.listFiles()) {
                deleteRecursively = deleteRecursively && deleteRecursively(relativePath + File.separator + child.getName());
            }
            return deleteRecursively && file.delete();
        } else {
            return false;
        }
    }

    @Port(name = "files", method = "saveFile")
    public synchronized boolean saveFile(String relativePath, byte[] data) {
        File f = new File(baseFolder.getAbsolutePath() + File.separator + relativePath);
        if ((f.exists() && f.isFile()) || !f.exists()) {
            try {
                FileOutputStream fw = new FileOutputStream(f);
                fw.write(data);
                fw.flush();
                fw.close();
                return true;
            } catch (Exception e) {
                Log.error("Error while getting file ", e);
                return false;
            }
        } else {
            Log.debug("No file exist = {}", baseFolder.getAbsolutePath() + File.separator + relativePath);
            return false;
        }
    }

    @Port(name = "files", method = "move")
    public synchronized boolean move(String oldRelativePath, String newRelativePath) {

        File oldFile = new File(baseFolder.getAbsolutePath() + File.separator + oldRelativePath);
        File newFile = new File(baseFolder.getAbsolutePath() + File.separator + newRelativePath);

        if (oldFile.renameTo(newFile)) {
            return true;
        } else {
            Log.debug("Unable to move file {} on {}", oldRelativePath, newRelativePath);
            return false;
        }
    }

    public static byte[] convertStream(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int l;
        do {
            l = (in.read(buffer));
            if (l > 0) {
                out.write(buffer, 0, l);
            }
        } while (l > 0);
        return out.toByteArray();
    }

    public String getRelativePath(String absolutePath) {
        return absolutePath.substring((baseFolder.getAbsolutePath().length()) + 1);
    }

}
