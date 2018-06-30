package docker;

import com.github.dockerjava.api.DockerClient;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.VirtualFileName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class DockerImageFile extends DockerFileObject {

    private final String container;

    protected DockerImageFile(String container, FileSystem fileSystem, DockerClient dockerClient) {
        super(fileSystem, dockerClient);
        this.container = container;
    }

    @Override
    public FileName getName() {
        return new VirtualFileName("docker", "docker://" + container, FileType.FILE);
    }

    @Override
    public URL getURL() throws FileSystemException {
        try {
            return new URL("docker://" + container);
        } catch (MalformedURLException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public boolean exists() throws FileSystemException {
        return !dockerClient.listContainersCmd().withShowAll(true).withNameFilter(Collections.singleton(container)).exec().isEmpty();
    }

    @Override
    public boolean isReadable() throws FileSystemException {
        return true;
    }

    @Override
    public FileType getType() throws FileSystemException {
        return FileType.FILE;
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        return new DockerRootFileObject(fileSystem, dockerClient);
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        return new FileObject[0];
    }

    @Override
    public FileObject getChild(String name) throws FileSystemException {
        throw new FileSystemException("No children");
    }

    @Override
    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        throw new FileSystemException("No children");
    }

    @Override
    public FileObject resolveFile(String path) throws FileSystemException {
        throw new FileSystemException("No children");
    }

    @Override
    public FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        return new FileObject[0];
    }

    @Override
    public void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        return;
    }

    @Override
    public FileContent getContent() throws FileSystemException {

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
