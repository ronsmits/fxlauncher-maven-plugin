package no.tornadofx.fxlauncher;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.*;

/**
 * Created by ronsmits on 23/03/2017.
 */

public class AbstractFxLauncherMojoTest {

    @Rule public MojoRule rule = new MojoRule();

    @Ignore @Test
    public void doIstartUp() throws Exception {
        System.out.println("started");
        File pom = new File("src/test/resources/testpom.xml");

        Assert.assertNotNull(pom);
        AbstractFxLauncherMojo abstractFxLauncherMojo = (AbstractFxLauncherMojo) rule.lookupMojo("createManifest", pom);
        Assert.assertNotNull(abstractFxLauncherMojo);
        abstractFxLauncherMojo.execute();

    }
}