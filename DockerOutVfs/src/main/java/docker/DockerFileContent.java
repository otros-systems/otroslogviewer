package docker;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Map;

public class DockerFileContent implements FileContent {

    @Override
    public FileObject getFile() {
        return null;
    }

    @Override
    public long getSize() throws FileSystemException {
        return 0;
    }

    @Override
    public long getLastModifiedTime() throws FileSystemException {
        return 0;
    }

    @Override
    public void setLastModifiedTime(long modTime) throws FileSystemException {

    }

    @Override
    public boolean hasAttribute(String attrName) throws FileSystemException {
        return false;
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        return null;
    }

    @Override
    public String[] getAttributeNames() throws FileSystemException {
        return new String[0];
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return null;
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {

    }

    @Override
    public void removeAttribute(String attrName) throws FileSystemException {

    }

    @Override
    public Certificate[] getCertificates() throws FileSystemException {
        return new Certificate[0];
    }

    @Override
    public InputStream getInputStream() throws FileSystemException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws FileSystemException {
        return null;
    }

    @Override
    public RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
        return null;
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        return null;
    }

    @Override
    public void close() throws FileSystemException {

    }

    @Override
    public FileContentInfo getContentInfo() throws FileSystemException {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }
}
