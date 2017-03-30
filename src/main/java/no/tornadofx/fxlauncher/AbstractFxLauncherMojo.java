package no.tornadofx.fxlauncher;

import fxlauncher.CreateManifest;
import fxlauncher.FXManifest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.impl.ArtifactResolver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Paths.get;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Base class for the mojos
 */

abstract class AbstractFxLauncherMojo extends AbstractMojo {

    @Parameter(required = true)
    private String appName;

    @Parameter(required = true)
    private String mainClass;

    @Parameter(defaultValue = "USERLIB/${appName}/cache")
    private String cacheDir;

    @Parameter
    private String parameters;

    @Parameter
    private String vendor;

    @Parameter
    private String version;

    @Parameter(required = true)
    private URL baseUrl;

    @Parameter(required = true, defaultValue = "deploy")
    String deployTarget;

    @Parameter(defaultValue = "${project.build.directory}/app")
    protected String buildDir;

    @Parameter
    protected String preLoadNativeLibraries;
    @Parameter(defaultValue = "${project.build.directory}/installer", required = true)
    private String installerDir;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDirectory;
    @Parameter
    private List<String> includeExtensions;

    @Parameter(defaultValue = "false")
    private boolean acceptDowngrade;

    @Parameter(defaultValue = "false")
    private boolean lingeringUpdateStream;
    @Parameter
    protected String whatsNew;
    @Parameter(defaultValue = "${session}")
    private MavenSession session;
    @Component
    private BuildPluginManager buildPluginManager;
    @Parameter(defaultValue = "${project}")
    private MavenProject project;
    @Component
    private ArtifactResolver artifactResolver;

    FXManifest createManifest() throws MojoExecutionException {
        getLog().info("Creating the manifest");
        if (includeExtensions != null) CreateManifest.setIncludeExtensions(includeExtensions);
        FXManifest manifest=null;
        try {
            manifest = CreateManifest.create(baseUrl.toURI(), mainClass, get(buildDir));
        } catch (IOException | URISyntaxException e) {
            throw new MojoExecutionException("Cannot create manifest", e);
        }
        if (cacheDir != null) manifest.cacheDir = cacheDir;
        if (acceptDowngrade) manifest.acceptDowngrade = acceptDowngrade;
        if (parameters != null) manifest.parameters = parameters;
        if (preLoadNativeLibraries != null) manifest.preloadNativeLibraries = preLoadNativeLibraries;
        if (whatsNew != null) manifest.whatsNewPage = whatsNew;
        manifest.lingeringUpdateScreen = lingeringUpdateStream;

        return manifest;

    }

    /**
     * Add a file to fxlauncher.jar.
     * The <code>filetoAdd</code> must be an absolute path.
     *
     * @param fileToAdd absolute path to the file that needs to be added.
     * @throws MojoExecutionException only when something goes wrong with the adding to the zipfile.
     * TODO: need to make it possible to maintain a relative path in it. Maybe strip the <code>projectBuildDirectory</code> of it?
     */
    void addtoLauncher(String fileToAdd) throws MojoExecutionException {
        getLog().info(String.format("placing %s in fxlauncher", fileToAdd));
        Path path = Paths.get(String.format("%s/fxlauncher.jar", buildDir));
        Map<String, String> props = new HashMap<>();
        props.put("create", "false");
        URI uri = URI.create("jar:" + path.toUri().toASCIIString());
        try (FileSystem jarFile = FileSystems.newFileSystem(uri, props)) {
            Path source = Paths.get(fileToAdd);
            Path target = jarFile.getPath(String.valueOf(source.getFileName()));
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MojoExecutionException("error in adding file to jar", e);
        }
    }

    /**
     * Copy the dependencies to the app directory
     *
     * @throws MojoExecutionException
     * @throws IOException
     */
    void copyDependencies() throws MojoExecutionException, IOException {
        getLog().info("copying to " + buildDir);
        executeMojo(
                plugin(groupId("org.apache.maven.plugins"),
                        artifactId("maven-dependency-plugin"),
                        version("2.0")
                ),
                goal("copy-dependencies"),
                configuration(
                        element(name("outputDirectory"), buildDir)
                ),
                executionEnvironment(
                        project,
                        session,
                        buildPluginManager
                ));
    }

    protected void copyArtifactIntoBuildDir() throws IOException {
        Artifact artifact = project.getArtifact();
        String name = String.format("%s/%s-%s.%s", projectBuildDirectory, artifact.getArtifactId(), artifact.getVersion(), artifact.getType());
        getLog().info(String.format("putting %s into %s", name, buildDir));
        Path source = Paths.get(name);
        Path target = Paths.get(String.format("%s/%s-%s.%s", buildDir, artifact.getArtifactId(), artifact.getVersion(), artifact.getType()));
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Rename the fxlauncher artifact. We dont want the use to have to download a file with the version information in it.
     * So we rename it.
     *
     * @throws IOException            error during File move
     * @throws MojoExecutionException fxlauncher not in dependency list.
     */
    protected void renameFxLauncherJar() throws IOException, MojoExecutionException {
        getLog().info("Setting correct name to fxlauncher.jar");
        List<Dependency> dependencies = project.getDependencies();

        Dependency fxlauncher = dependencies.stream()
                .filter(artifact -> artifact.getArtifactId()
                        .equals("fxlauncher"))
                .findAny()
                .orElse(null);
        if (fxlauncher == null) {
            throw new MojoExecutionException("fxlauncher not in the dependency list");
        }
        Files.move(
                Paths.get(String.format("%s/%s-%s.jar", buildDir, fxlauncher.getArtifactId(), fxlauncher.getVersion())),
                Paths.get(String.format("%s/fxlauncher.jar", buildDir)),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
