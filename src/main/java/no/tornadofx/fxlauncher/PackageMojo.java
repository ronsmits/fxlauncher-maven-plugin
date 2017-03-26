package no.tornadofx.fxlauncher;

import fxlauncher.FXManifest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Created by ronsmits on 24/03/2017.
 */
@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.PACKAGE)
public class PackageMojo extends AbstractFxLauncherMojo {


    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            copyDependencies();
            renameFxLauncherJar();
            copyArtifactIntoBuildDir();
            FXManifest manifest = createManifest();
            JAXB.marshal(manifest, Paths.get(buildDir).resolve("app.xml").toFile());
            addtoLauncher(String.valueOf(Paths.get(buildDir).resolve("app.xml").toAbsolutePath()));
            if(whatsNew!=null) addtoLauncher(
                    String.valueOf(Paths.get(projectBuildDirectory).resolve(whatsNew).toAbsolutePath()));
        } catch (IOException e) {
            throw new MojoExecutionException("Error in creating Manifest", e);
        }
    }
}
