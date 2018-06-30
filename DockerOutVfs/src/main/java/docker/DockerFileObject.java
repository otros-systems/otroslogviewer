package docker;

import com.github.dockerjava.api.DockerClient;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.FileOperations;

public abstract class DockerFileObject implements FileObject {

    protected final FileSystem fileSystem;
    protected final DockerClient dockerClient;

    protected DockerFileObject(FileSystem fileSystem, DockerClient dockerClient) {
        this.fileSystem = fileSystem;
        this.dockerClient = dockerClient;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isWriteable() {
        return false;
    }


    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }


    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public int delete(FileSelector selector) {
        return 0;
    }

    @Override
    public void createFolder() throws FileSystemException {
        throw new FileSystemException("Not supported");
    }

    @Override
    public void createFile() throws FileSystemException {
        throw new FileSystemException("Not supported");
    }

    @Override
    public void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        throw new FileSystemException("Not supported");
    }

    @Override
    public void moveTo(FileObject destFile) throws FileSystemException {
        throw new FileSystemException("Not supported");
    }

    @Override
    public boolean canRenameTo(FileObject newfile) {
        return false;
    }


    @Override
    public FileOperations getFileOperations() {

        return null;
    }
}
