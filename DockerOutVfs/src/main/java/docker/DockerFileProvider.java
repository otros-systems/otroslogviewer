package docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.FileProvider;

import java.util.Collection;
import java.util.List;

public class DockerFileProvider implements FileProvider {

    private final DockerClient dockerClient;

    public DockerFileProvider(){
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        // using jaxrs/jersey implementation here (netty impl is also available)
        DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
                .withReadTimeout(1000)
                .withConnectTimeout(1000)
                .withMaxTotalConnections(100)
                .withMaxPerRouteConnections(10);

        dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
    }


    @Override
    public FileObject findFile(FileObject baseFile, String uri, FileSystemOptions fileSystemOptions) throws FileSystemException {
        if (uri.matches("docker(://)?")){
            List<Container> imageList = dockerClient.listContainersCmd().withShowAll(true).exec();
            imageList.forEach(image -> System.out.println(image.getNames()[0]));
           return new DockerRootFileObject(null, dockerClient);
        }
        return null;
    }

    @Override
    public FileObject createFileSystem(String scheme, FileObject file, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return null;
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return null;
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return null;
    }

    @Override
    public FileName parseUri(FileName root, String uri) throws FileSystemException {
        return null;
    }
}
