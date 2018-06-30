package docker;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

import java.util.Arrays;

public class DockerVfs {


    public static void main(String[] args) throws FileSystemException, InterruptedException {
        FileSystemManager manager = VFS.getManager();

        DefaultFileSystemManager defaultFileSystemManager = new DefaultFileSystemManager();
        defaultFileSystemManager.addProvider("docker", new DockerFileProvider());
        defaultFileSystemManager.init();
        Arrays.asList(manager.getSchemes()).forEach(System.out::println);

        FileObject dockerRoot = manager.resolveFile("docker://");

        Thread.sleep(4000L);
        System.out.println("Listing children of root");
        FileObject[] children = dockerRoot.getChildren();
        for (FileObject child:children){
            System.out.println("File: " + child.getName());
        }

    }
}
