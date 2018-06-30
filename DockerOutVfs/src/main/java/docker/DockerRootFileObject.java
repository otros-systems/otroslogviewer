package docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.VirtualFileName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class DockerRootFileObject extends DockerFileObject {

    protected DockerRootFileObject(FileSystem fileSystem, DockerClient dockerClient) {
        super(fileSystem,dockerClient);
    }

    @Override
    public FileName getName() {
        return new VirtualFileName("docker","docker://",FileType.FOLDER);
    }

    @Override
    public URL getURL() throws FileSystemException {
        try {
            return new URL("docker://");
        } catch (MalformedURLException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public FileType getType() {
        return FileType.FOLDER;
    }

    @Override
    public FileObject getParent() {
        return null;
    }

    @Override
    public FileObject[] getChildren() {
        List<Container> exec = dockerClient.listContainersCmd().withShowAll(true).exec();
        List<DockerImageFile> files = exec
                .stream()
                .map(c -> c.getNames()[0].replaceFirst("^/", ""))
                .map(c -> new DockerImageFile(c, fileSystem, dockerClient))
                .collect(Collectors.toList());
        return files.toArray(new FileObject[0]);
    }

    @Override
    public FileObject getChild(String name) {
        return new DockerImageFile(name,fileSystem,dockerClient);
    }

    @Override
    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        return resolveFile(name);
    }

    @Override
    public FileObject resolveFile(String path) throws FileSystemException {
        System.out.println("DockerRootFileObject.resolveFile " + path);
        return null;
    }

    @Override
    public FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        return new FileObject[0];
    }

    @Override
    public void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {

    }

    @Override
    public FileContent getContent() {
        return null;
    }

    @Override
    public void close() throws FileSystemException {

    }

    @Override
    public void refresh() throws FileSystemException {

    }

    @Override
    public boolean isAttached() {
        return false;
    }

    @Override
    public boolean isContentOpen() {
        return false;
    }
}
