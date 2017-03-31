package no.tornadofx.fxlauncher;

import fxlauncher.FXManifest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import javax.xml.bind.JAXB;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Map;

/**
 * Created by ronsmits on 24/03/2017.
 */
@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.PACKAGE)
public class PackageMojo extends AbstractFxLauncherMojo {


    @Parameter(defaultValue = "${project.build.outputDirectory}") private String classesDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            System.out.println(uiProvider);
            copyDependencies();
            renameFxLauncherJar();
            copyArtifactIntoBuildDir();
            if(uiProvider !=null){
                installUiProvider();
            }
            FXManifest manifest = createManifest();
            JAXB.marshal(manifest, Paths.get(buildDir).resolve("app.xml").toFile());
            addtoLauncher(String.valueOf(Paths.get(buildDir).resolve("app.xml").toAbsolutePath()));
            if(whatsNew!=null) addtoLauncher(
                    String.valueOf(Paths.get(projectBuildDirectory).resolve(whatsNew).toAbsolutePath()));
        } catch (IOException e) {
            throw new MojoExecutionException("Error in creating Manifest", e);
        }
    }

    private void installUiProvider() throws MojoExecutionException {
        addDirtoLauncher(Paths.get(uiProvider.getUiProviderLocation()), uiProvider.getUiProviderPackage());
        addServices(uiProvider);
    }

    private void addServices(UIProvider uiProvider) {
        URI uri = getUri();
        Map<String, String> props = getPropsForFileSystem();
        try (FileSystem jarFile = FileSystems.newFileSystem(uri, props)) {
            Path path = jarFile.getPath("/META-INF/services");
            Files.createDirectories(path);
//            if(Files.notExists(path))
//                Files.createDirectories(path);
            System.out.println("will write to " +path.resolve("fxlauncher.UIProvider"));
            BufferedWriter bufferedWriter = Files.newBufferedWriter(path.resolve("fxlauncher.UIProvider"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            bufferedWriter.write(String.format("%s.%s\n", uiProvider.getUiProviderPackage(), uiProvider.getUiProviderClass()));
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
