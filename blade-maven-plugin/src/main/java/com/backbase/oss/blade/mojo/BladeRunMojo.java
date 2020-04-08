package com.backbase.oss.blade.mojo;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.WebApp;
import com.backbase.oss.blade.tomcat.BladeTomcat;
import com.backbase.oss.blade.tomcat.BladeTomcatBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "run", threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE, aggregator = true)
@Execute(phase = LifecyclePhase.PACKAGE)
public class BladeRunMojo extends AbstractBladeMojo {

    private static final String JOLOKIA_WAR_1_3_7_WAR = "jolokia-war-1.3.7.war";


    private static final Logger logger = LoggerFactory.getLogger(BladeRunMojo.class);

    private static final String SELFSIGNED_JKS = "selfsigned.jks";



    @Parameter(defaultValue = "true")
    private boolean enableJolokia;

    @Parameter(defaultValue = "false")
    private boolean enableGzip;

    @Parameter(defaultValue = "false")
    private boolean enableHttps;


    @Parameter(defaultValue = "tomcat")
    private String keyAlias;

    @Parameter(defaultValue = "backbase")
    private String keystorePass;

    @Parameter(defaultValue = "JKS")
    private String keystoreType;

    @Parameter(defaultValue = "${project.build.directory}/selfsigned.jks")
    private String keystoreFile;

    @Parameter(defaultValue = "false")
    private boolean openBrowserOnStartup;

    @Parameter(property = "connector.maxThreads", defaultValue = "20")
    private int maxThreads;

    @Parameter(property = "connector.maxThreads.dynamic", defaultValue = "false")
    private boolean dynamicMaxThreads;

    @Parameter(property = "connector.maxHttpHeaderSize", defaultValue = "65000")
    private int maxHttpHeaderSize;

    @Parameter(defaultValue = "")
    private String bladeConsoleContextPath;


    public void execute() throws MojoExecutionException, MojoFailureException {
        testTomcatPort();
        createCatalinaHome();
        setupSystemProperties();
        Blade blade = initializeStages();
        copyEsapiProperties();

        try {
            File webapps = new File(catalinaHome, "webapps");
            if (webapps.exists()) {
                getLog().info("Cleaning up previous blade");
                FileUtils.deleteDirectory(webapps);
            }

            if (bootstrapped == null)
                bootstrapped = new ArrayList<>();

            List<WebApp> bootstrappedApps = new ArrayList<>(bootstrapped);

            if (enableBladeConsole)
                addBladeWar(bootstrappedApps);
            if (enableJolokia) {
                addJolokiaWar(bootstrappedApps);
            }

            if (enableHttps) {
                copySelfSignedCertificateToTarget();
            }

            BladeTomcat tomcat = new BladeTomcatBuilder()
                    .setCatalinaHome(catalinaHome)
                    .setBootstrappedWebApps(bootstrappedApps)
                    .setBlade(blade)
                    .setEnableGzip(enableGzip)
                    .setMaxThreads(maxThreads)
                    .setDynamicMaxThreads(dynamicMaxThreads)
                    .setEnableHttps(enableHttps)
                    .setKeyAlias(keyAlias)
                    .setKeystorePass(keystorePass)
                    .setKeystoreType(keystoreType)
                    .setKeystoreFile(keystoreFile)
                    .setEnableBladeConsole(enableBladeConsole)
                    .setMaxHttpHeaderSize(maxHttpHeaderSize)
                    .build();

            tomcat.getServer().setPort(getShutdownPort());
            tomcat.startBladeServer();

            if (enableBladeConsole) {
                if (openBrowserOnStartup && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                } else {
                    logger.warn("{} Desktop API BROWSE in this platform is not supported.", System.getProperty("os.name"));
                }
            }

            tomcat.autoStartStages();

            if (!fork)
                tomcat.getServer().await();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void copyEsapiProperties() throws MojoFailureException {

        File target = new File(catalinaHome, "ESAPI.properties");

        InputStream inputStream = this.getClass().getResourceAsStream("/ESAPI.properties");
        try {
            copyInputStreamToFile(target, inputStream);
        } catch (IOException e) {
            throw new MojoFailureException("Cannot copy ESAPI.properties");
        }
    }

    private void addJolokiaWar(List<WebApp> webapps) throws IOException {
        addWebApp(webapps, JOLOKIA_WAR_1_3_7_WAR, "/jolokia", false, false);
    }

    private void addBladeWar(List<WebApp> webApps) throws IOException {
        String contextPath = StringUtils.isEmpty(bladeConsoleContextPath) ? "" : bladeConsoleContextPath;
        addWebApp(webApps, "blade-webapp.war", contextPath, true, true);
    }

    private void addWebApp(List<WebApp> webapps, String warFile, String contextPath, boolean isPrivileged, boolean inheritClassloader) throws IOException {

        logger.info("Adding bootstrapped webapp: {} with contextPath: {}", warFile, contextPath);

        File deploymentDirectory = new File(catalinaHome, "webapps");
        deploymentDirectory.mkdirs();

        File targetFile = new File(deploymentDirectory, warFile);

        if (!targetFile.exists()) {
            InputStream inputStream = this.getClass().getResourceAsStream("/" + warFile);
            copyInputStreamToFile(targetFile, inputStream);
        }
        WebApp webApp = new WebApp(targetFile, contextPath, null);
        webApp.setPrivileged(isPrivileged);
        webApp.setInheritClassloader(inheritClassloader);
        webApp.setName(warFile);
        webapps.add(webApp);
    }

    private void copySelfSignedCertificateToTarget() throws IOException {
        File target = new File(keystoreFile);
        if (!target.exists()) {
            InputStream inputStream = this.getClass().getResourceAsStream("/" + SELFSIGNED_JKS);
            copyInputStreamToFile(target, inputStream);
        }
    }


}
