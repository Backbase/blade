package com.backbase.oss.blade.mojo;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import com.backbase.oss.blade.utils.BladeUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

abstract class AbstractBladeMojo extends AbstractMojo {

    private static final String BACKBASE_CONFIG_DIR = "backbase.config.dir";
    private static final String ESAPI_RESOURCES = "org.owasp.esapi.resources";

    private static final Logger logger = LoggerFactory.getLogger(AbstractBladeMojo.class.getName());
    private static final String SLASH = "/";

    /**
     * To honor Rutger Hauer (1944 – 2019)
     */
    protected static final int SHUTDOWN_DEFAULT_PORT = 2019;

    @Parameter(defaultValue = "false")
    protected boolean fork;

    @Parameter(defaultValue = "${project.artifactId}")
    private String id;

    @Parameter(defaultValue = "${project.name}")
    private String name;

    @Parameter(defaultValue = "8080")
    int port;

    @Parameter(defaultValue = "-1")
    int shutdownPort;

    @Parameter(defaultValue = "8443")
    int securePort;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter
    private Map systemProperties;

    @Parameter(defaultValue = "${project.basedir}/config/backbase/")
    private File backbaseConfigDir;

    @Parameter(defaultValue = "${project.build.directory}/tomcat")
    File catalinaHome;

    @Parameter(defaultValue = "${project.basedir}/config/tomcat/conf/Catalina/localhost/")
    private File contextFileDir;

    @Parameter(defaultValue = "false")
    private boolean multiThreaded;

    @Parameter(defaultValue = "false")
    private boolean detectAndConfigureSpringConfigLocation;

    @Parameter
    List<Stage> stages;

    @Parameter
    List<WebApp> bootstrapped;

    @Parameter(defaultValue = "http://localhost:8080")
    private URL masterBladeAddress;

    @Parameter(defaultValue = "true")
    protected boolean enableBladeConsole;

    @Parameter(defaultValue = "false")
    protected boolean enableApplicationReload;

    Blade initializeStages() throws MojoExecutionException {
        if (stages == null || stages.isEmpty()) {
            autoconfigureStages();
        } else {
            for (Stage stage : stages) {
                if (stage.getName() == null) {
                    stage.setName(stage.getId());
                }
                for (WebApp webApp : stage.getWebApps()) {
                    setup(webApp);
                }
            }
        }
        try {
            Blade blade = new Blade(id);
            blade.setName(name);
            blade.setPort(port);
            blade.setSecurePort(securePort);
            blade.setStages(stages);
            blade.setReady(false);
            blade.setBladeMaster(masterBladeAddress);
            blade.setReloadable(enableApplicationReload);
            return blade;
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot initialize blade.json", e);
        }
    }

    private void autoconfigureStages() {
        stages = new ArrayList<>();
        Stage stage = new Stage();
        stage.setAutoStart(true);
        stage.setId(project.getArtifactId());

        List<WebApp> bootstrappedApps = getBootstrappedApps(project);
        if (project.getPackaging().equals("pom")) {
            stage.setWebApps(bootstrappedApps);
        } else if (project.getPackaging().equals("war") && bootstrappedApps.size() == 1) {
            for (WebApp webApp : bootstrappedApps) {
                File docBase = new File(project.getBuild().getDirectory(), project.getBuild().getFinalName());
                logger.info("Setting Doc Base for Web App: {} to: {}", webApp.getName(), docBase);
                webApp.setDocBase(docBase);
            }
            stage.setWebApps(bootstrappedApps);
        }
        if (bootstrappedApps.size() == 1) {
            for (WebApp webApp : bootstrappedApps) {
                if (detectAndConfigureSpringConfigLocation) {
                    detectAndConfigureSpringConfigLocation(project, webApp);
                }
            }
        }

        stage.setMultiThreaded(multiThreaded);
        stages.add(stage);
    }

    private Artifact getArtifact(MavenProject mavenProject, WebApp webApp) {
        Set<Artifact> artifacts = mavenProject.getArtifacts();

        for (Artifact artifact : artifacts) {
            if (!artifact.getGroupId().equals(webApp.getGroupId())
                    || !artifact.getArtifactId().equals(webApp.getArtifactId())
                    || (artifact.getClassifier() != null && artifact.getClassifier().equals("war"))) {
                continue;
            }
            return artifact;
        }
        return null;
    }

    private List<Artifact> getSubModuleArtifacts(MavenProject mavenProject) {
        return mavenProject.getArtifacts().stream().
                filter(artifact -> !mavenProject.getParent().getArtifacts().contains(artifact)).
                filter(this::isWar).
                collect(Collectors.toList());
    }

    private void detectAndConfigureSpringConfigLocation(MavenProject mavenProject, WebApp webApp) {
        File applicationYaml = new File(mavenProject.getBasedir(), "application.yml");
        if (applicationYaml.exists()) {
            if (webApp.isSpringBoot1App()) {
                webApp.getEnvironmentVariables().put("spring.config.location", applicationYaml.getAbsolutePath());
            } else {
                webApp.getEnvironmentVariables().put("spring.config.additional-location", applicationYaml.getAbsolutePath());
            }
        }
    }

    private List<Artifact> getWarDependencies(MavenProject mavenProject) {
        return mavenProject.getArtifacts()
                .stream()
                .filter(this::isWar)
                .collect(Collectors.toList());
    }


    private List<WebApp> getBootstrappedApps(MavenProject mavenProject) {
        return getWarDependencies(mavenProject).stream()
                .map(artifact -> {
                    WebApp webApp = new WebApp();
                    mapArtifact(webApp, artifact);
                    return webApp;
                })
                .collect(Collectors.toList());
    }

    private boolean isWar(Artifact artifact) {
        return artifact.getType().equals("war");
    }

    private void setup(WebApp webApp) throws MojoExecutionException {
        // If webApp did not setup a docBase property, try to resolve a Maven Artifact
        MavenProject mavenProject;
        if (webApp.getModule() == null) {
            mavenProject = project;
        } else {
            mavenProject = getMavenSubModule(webApp);
            // Copy over configuration files to target as we only have one backbase config dir
            copyConfigurationFilesIntoTarget(webApp, mavenProject);
        }
        if (webApp.getDocBase() == null && webApp.getModule() != null) {
            setSubmoduleWebApp(webApp, mavenProject);
        } else {
            mapMavenAsWebApp(webApp, mavenProject);
        }

        if (detectAndConfigureSpringConfigLocation) {
            detectAndConfigureSpringConfigLocation(mavenProject, webApp);
        }
        if (webApp.getVersion() == null) {
            webApp.setVersion(mavenProject.getVersion());
        }

        String contextPath = webApp.getContextPath() != null ? webApp.getContextPath() : webApp.getArtifactId();
        contextPath = ensureContextPathStartsWithSlash(contextPath);

        webApp.setName(webApp.getName() == null ? contextPath : webApp.getName());
        webApp.setContextPath(contextPath);
        if (StringUtils.isEmpty(webApp.getUrl())) {
            webApp.setUrl(contextPath);
        }

        File contextFile = getContextFile(contextPath);
        if (contextFile.exists()) {
            webApp.setContextFileLocation(contextFile);
        }

        logger.debug("*********");
        log("Name", webApp.getName());
        log("GroupID", webApp.getGroupId());
        log("ArtifactId", webApp.getArtifactId());
        log("Version", webApp.getVersion());
        log("DocBase", webApp.getDocBase());
        log("Maven Sub Module", webApp.getModule());
        log("Context Path", webApp.getContextPath());
        log("Context File", webApp.getContextFileLocation());
        log("GroupID", webApp.getGroupId());
        logger.debug("**********");
    }

    private void mapMavenAsWebApp(WebApp webApp, MavenProject mavenProject) {
        Artifact artifact = getArtifact(mavenProject, webApp);
        if (artifact != null) {
            mapArtifact(webApp, artifact);
        }
    }

    private void setSubmoduleWebApp(WebApp webApp, MavenProject mavenProject) throws MojoExecutionException {
        if (mavenProject.getPackaging().equals("pom")) {
            // Maven Project must have a single WAR dependency!
            List<Artifact> subModuleWarDependencies = getSubModuleArtifacts(mavenProject);
            int size = subModuleWarDependencies.size();
            if (size == 0) {
                throw new MojoExecutionException("Cannot setup Web Application from submodule " + mavenProject.getName() + " as no WAR dependency is setup as a Dependency");
            } else if (size > 1) {
                throw new MojoExecutionException("Cannot setup Web Application from submodule " + mavenProject.getName() + ". Mapping sub modules in Blade requires a single WAR dependency");
            }
            mapArtifact(webApp, subModuleWarDependencies.get(0));
        } else {
            mapArtifact(webApp, mavenProject.getArtifact());
            File docBase = new File(mavenProject.getBuild().getDirectory(), mavenProject.getBuild().getFinalName());
            logger.info("Setting Doc Base for submodule: {} to: {}", webApp.getModule(), docBase);
            webApp.setDocBase(docBase);
        }
    }

    private void log(String label, Object value) {
        if (value != null && logger.isDebugEnabled()) {
            logger.debug("{}:{}", StringUtils.left(label, 20), value);
        }
    }

    private void copyConfigurationFilesIntoTarget(WebApp webApp, MavenProject mavenProject) throws MojoExecutionException {
        File projectConfigDirectory = new File(mavenProject.getBasedir(), "config");
        if (projectConfigDirectory.exists()) {
            try {
                FileUtils.copyDirectoryStructure(projectConfigDirectory, backbaseConfigDir.getParentFile());
                logger.info("Copying module configuration files from: {} to: {} ", projectConfigDirectory, backbaseConfigDir);
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot copy configuration files from module: " + webApp, e);
            }
        }
    }


    private MavenProject getMavenSubModule(WebApp webApp) throws MojoExecutionException {
        Optional<MavenProject> optionalMavenProject = project.getCollectedProjects().stream()
                .filter(p -> p.getArtifactId().equals(webApp.getModule()))
                .findFirst();
        if (!optionalMavenProject.isPresent()) {
            throw new MojoExecutionException("Cannot find maven module: " + webApp.getModule() + " as submodule of this project");
        }
        return optionalMavenProject.get();
    }

    private void mapArtifact(WebApp webApp, Artifact artifact) {
        webApp.setArtifactId(artifact.getArtifactId());
        webApp.setGroupId(artifact.getGroupId());
        webApp.setDocBase(artifact.getFile());
        webApp.setVersion(artifact.getVersion());
    }

    private String ensureContextPathStartsWithSlash(String contextPath) {
        if (!contextPath.startsWith(SLASH) && !contextPath.endsWith(SLASH)) {
            contextPath = SLASH + contextPath;
        }
        return contextPath;
    }


    private File getContextFile(String contextPath) {
        return new File(contextFileDir, contextPath + ".xml");
    }

    void setupSystemProperties() throws MojoExecutionException {
        Map properties = getSystemProperties();
        if (properties.values().stream().anyMatch(Objects::isNull)) {
            throw new MojoExecutionException("systemProperties cannot contain null values! " + properties.toString());
        }
        System.getProperties().putAll(properties);

        if (!properties.containsKey(BACKBASE_CONFIG_DIR)) {
            System.getProperties().put(BACKBASE_CONFIG_DIR, backbaseConfigDir.getAbsolutePath());
        }
        getLog().info(BACKBASE_CONFIG_DIR + " property set to: " + properties.get(BACKBASE_CONFIG_DIR));

        if(!properties.containsKey(ESAPI_RESOURCES)) {
            System.getProperties().put(ESAPI_RESOURCES, catalinaHome.getAbsolutePath());
        }
        getLog().info(ESAPI_RESOURCES + " property set to: " + properties.get(ESAPI_RESOURCES));

        printSystemProperties(System.getProperties());
    }

    private Map getSystemProperties() {
        Map properties = this.systemProperties;
        if (properties == null) {
            properties = new HashMap();
        }
        return properties;
    }

    void testTomcatPort() throws MojoExecutionException {
        if (!BladeUtils.isPortAvailable(port)) {
            String message = "Port " + port + " already in use.";
            logger.error(message);
            throw new MojoExecutionException(message);
        }
    }

    int getShutdownPort() {
        if (shutdownPort == -1 && BladeUtils.isPortAvailable(SHUTDOWN_DEFAULT_PORT)) {
            logger.info("Shutdown port: {} " , SHUTDOWN_DEFAULT_PORT);
            return SHUTDOWN_DEFAULT_PORT;
        }
        if (shutdownPort != -1 && BladeUtils.isPortAvailable(shutdownPort)) {
            logger.info("Shutdown port: {}", shutdownPort);
            return shutdownPort;
        } else {
            if (shutdownPort != -1) {
                logger.warn("Shutdown port {} already in use.", shutdownPort);
            }
            return -1;
        }
    }


    void createCatalinaHome() throws MojoExecutionException {
        if (!catalinaHome.exists() && !catalinaHome.mkdirs()) {
            throw new MojoExecutionException("Cannot create Tomcat Catalina Home directory: " + catalinaHome.getAbsolutePath());
        }

    }

    private void printSystemProperties(final Properties systemProperties) {
        logger.debug("System Properties:");

        systemProperties.stringPropertyNames().stream()
                .sorted()
                .forEach(property -> logger.debug("{}={}", property , systemProperties.get(property)));
    }

    void copyInputStreamToFile(File targetFile, InputStream inputStream) throws IOException {
        java.nio.file.Files.copy(
                inputStream,
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        inputStream.close();
    }

    @SuppressWarnings({"squid:S2142", "squid:S2274"})
    void waitIndefinitely() throws MojoExecutionException {
        Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException exception) {
                throw new MojoExecutionException("InterruptedException on wait Indefinitely lock:" + exception.getMessage(),
                        exception);
            }
        }
    }
}
