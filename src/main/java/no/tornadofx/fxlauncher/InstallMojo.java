package no.tornadofx.fxlauncher;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import sun.misc.Unsafe;

import javax.swing.text.html.Option;
import java.io.Console;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ronsmits on 25/03/2017.
 */
@Mojo(name = "install", requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.INSTALL)
public class InstallMojo extends AbstractFxLauncherMojo {

    @Parameter(required = true) private String identityFile;
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        tryAvoidJCE();
        System.out.println(identityFile);
        int atIndex = deployTarget.indexOf("@");
        String username = deployTarget.substring(0, atIndex);
        int colonIndex = deployTarget.indexOf(":");
        String host = deployTarget.substring(atIndex + 1, colonIndex);
        String path = deployTarget.substring(colonIndex + 1);

        SSHClient sshClient = new SSHClient();
        try {
            sshClient.loadKnownHosts();
            sshClient.connect(host);

            KeyProvider noPassphrase = sshClient.loadKeys(identityFile);
            KeyProvider consoleProvider = sshClient.loadKeys(identityFile, consolePasswordFinder());
            KeyProvider javaFxProvider = sshClient.loadKeys(identityFile, javafxPasswordFinder());
            sshClient.authPublickey(username, noPassphrase, javaFxProvider, consoleProvider);
            sshClient.newSCPFileTransfer().upload(new FileSystemFile(buildDir), path);

        } catch (IOException e) {
            throw new MojoExecutionException("error in SSH communication", e);
        } finally {
            try {
                sshClient.disconnect();
            } catch (IOException e) {
                throw new MojoExecutionException("error in disconnect", e);
            }
        }
    }

    private PasswordFinder consolePasswordFinder() {
        PasswordFinder finder = new PasswordFinder() {
            @Override
            public char[] reqPassword(Resource<?> resource) {
                System.out.println("starting consoleProvider");
                Console console = System.console();
                if (console == null) {
                    System.out.println("Couldn't get Console instance");
                    System.exit(0);
                }

                char passwordArray[] = console.readPassword("Enter passphrase: ");
                return passwordArray;
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        };

        return finder;
    }

    private PasswordFinder javafxPasswordFinder() {
        return new PasswordFinder() {
            @Override
            public char[] reqPassword(Resource<?> resource) {
                StringProperty passphrase = new SimpleStringProperty();
                new JFXPanel(); // this kicks of the Platform Toolkit
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Platform.runLater(()->{
                    Dialog dialog = createDialog();
                    Optional<String> optional = dialog.showAndWait();
                    if(optional.isPresent()) passphrase.setValue(optional.get());
                    countDownLatch.countDown();
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return passphrase.getValue().toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        };
    }

    private Dialog<String> createDialog() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Passphrase");
        Dialog dialog = new Dialog<String>();
        dialog.setTitle("fxlauncher maven plugin");
        dialog.setHeaderText("Enter passphrase");
        ButtonType passwordButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);
        HBox hBox = new HBox();
        hBox.getChildren().add(passwordField);
        hBox.setPadding(new Insets(20));
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        dialog.getDialogPane().setContent(hBox);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == passwordButtonType) {
                return passwordField.getText();
            }
            return null;
        });
        return dialog;
    }

    private void tryAvoidJCE() throws MojoExecutionException {
        try {
            setJceSecurityUnrestricted();
        } catch (NoSuchFieldException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            try {
                setJceSecurityUnrestricted(getUnsafe());
            } catch (ClassNotFoundException | NoSuchFieldException | InstantiationException | IllegalAccessException e1) {
                throw new MojoExecutionException("cannot circumvent JCE restriction. Install JCE extension", e);
            }
        }
    }

    private static void setJceSecurityUnrestricted() throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException {
        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
        field.setAccessible(true);
        field.setBoolean(null, false);
    }

    @SuppressWarnings("restriction")
    private static void setJceSecurityUnrestricted(Unsafe unsafe) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
        unsafe.putBoolean(Class.forName("javax.crypto.JceSecurity"), unsafe.staticFieldOffset(field), false);
    }

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }
}
