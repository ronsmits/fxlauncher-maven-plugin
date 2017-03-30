Fxlauncher-maven-plugin is the companion to fxlauncher. 
This is (for now) an experimental plugin. Please report issues if and when you find them.
 
 ## configuration
 This is the most basic configuration.
 ```xml
<build>
        <plugins>
            <plugin>
                <groupId>no.tornadofx.fxlauncher</groupId>
                <artifactId>fxlauncher-maven-plugin</artifactId>
                <version>0.1.0-SNAPSHOT</version>
                <configuration>
                    <appName>name</appName>
                    <baseUrl>http://localhost</baseUrl>
                    <mainClass>main</mainClass>
                    <whatsNew>readme.txt</whatsNew>
                    <identityFile>${user.home}/.ssh/id_rsa</identityFile>
                    <deployTarget>ron@ronsmits.org:www/demo/</deployTarget>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>package</goal>
                            <goal>install</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

| Parameter | Type | Required | Description |
| --------- | ---- | -------- | ------------|
|appName|String|true|Name of the app as how it will be used by the native installer|
|baseUrl|URL|true|Location where Fxlauncher will go to download the files from|
|mainclass|String|true|Main entry point into the application|
|whatsNew|String|false|If set fxlauncher will show a webview with the contents of the whatsNew variable in it|
|identityKeyFile|String|true|Location of the ssh private key file that will be used to upload the files|
|deployTarget|String|true|Location where the files will be uploaded to|
|cacheDir|String|false|Location where fxlauncher will store the files. Default is `USERLIB/${appName}/cache`|
|parameters|String|false|parameters that need to be passed to the application when Fxlauncher starts it up|
|vendor|String|false|Used when building a native installer|
|version|String|false|Used when building a native installer|
|buildDir|String|false|Used to assemble the files that need to be uploaded. Default is `${project.build.directory}/app`|
|includeExtensions|List|false|Extension, like md or txt, that need to be included |


 