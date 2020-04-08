package com.backbase.oss.blade.tomcat;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import com.backbase.oss.blade.utils.BladeUtils;
import java.io.File;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vladimir Raskin
 * @author Bart Veenstra
 */
@SuppressWarnings("squid:S3010")
public class BladeTomcat extends Tomcat {

    private Logger logger = LoggerFactory.getLogger(BladeTomcat.class);
    private final Blade blade;
    private final ClassLoader classLoader;
    private boolean running = false;
    private static BladeTomcat instance = null;
    private final boolean enableBladeConsole;

    protected BladeTomcat(BladeTomcatBuilder builder) throws BladeStartException {
        this.blade = builder.blade;
        this.enableBladeConsole = builder.enableBladeConsole;

        boolean temp = new File(builder.catalinaHome, "temp").mkdirs();
        boolean work = new File(builder.catalinaHome, "work").mkdirs();
        boolean webApps = new File(builder.catalinaHome, "webApps").mkdirs();


        logger.debug("Create tomcat temp dir: {}", temp);
        logger.debug("Create tomcat work dir: {}", work);
        logger.debug("Create tomcat webApps dir: {}", webApps);

        setBaseDir(builder.catalinaHome.getAbsolutePath());
        setHostname("0.0.0.0");
        setPort(blade.getPort());
        enableNaming();

        setupDefaultConnector(builder);

        if (builder.enableHttps) {
            setupHttpsConnector(builder);
        }

        classLoader = Thread.currentThread().getContextClassLoader();

        for (WebApp webApp : builder.bootstrappedWebApps) {
            getHost().deploy(webApp, false, classLoader);
        }
        instance = this;
    }

    public static BladeTomcat getInstance() {
        return instance;
    }

    private void setupHttpsConnector(BladeTomcatBuilder builder) {
        int numberOfWebApps = builder.dynamicMaxThreads ? builder.bootstrappedWebApps.size() + numberOfWebApps(blade) : 1;
        Connector connector = new Connector();
        connector.setPort(blade.getSecurePort());
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setAttribute("keyAlias", builder.keyAlias);
        connector.setAttribute("keystorePass", builder.keystorePass);
        connector.setAttribute("keystoreType", builder.keystoreType);
        connector.setAttribute("keystoreFile", builder.keystoreFile);
        connector.setAttribute("clientAuth", "false");
        connector.setAttribute("protocol", "HTTP/1.1");
        connector.setAttribute("sslProtocol", "TLS");
        connector.setAttribute("maxHttpHeaderSize", builder.maxHttpHeaderSize);
        connector.setAttribute("maxThreads", String.valueOf(Math.min(numberOfWebApps * builder.maxThreads, 10)));
        connector.setAttribute("protocol", "org.apache.coyote.http11.Http11AprProtocol");
        connector.setAttribute("SSLEnabled", true);
        getService().addConnector(connector);
    }

    private void setupDefaultConnector(BladeTomcatBuilder builder) {
        int numberOfWebApps = builder.dynamicMaxThreads ? builder.bootstrappedWebApps.size() + numberOfWebApps(blade) : 1;
        Connector connector = getConnector();
        connector.setAsyncTimeout(150000);
        connector.setMaxPostSize(Integer.MAX_VALUE);
        connector.setURIEncoding("UTF-8");
        connector.setUseBodyEncodingForURI(true);
        connector.setProperty("maxHttpHeaderSize", String.valueOf(builder.maxHttpHeaderSize));
        connector.setProperty("maxThreads", String.valueOf(Math.min(numberOfWebApps * builder.maxThreads, 10)));
        connector.setProperty("minSpareThreads", "5");
        connector.setProperty("maxSpareThreads", "5");

        if (builder.enableGzip) {
            enableGzip(connector);
        }
    }

    private void enableGzip(Connector connector) {
        connector.setProperty("compression", "on");
        connector.setProperty("compressionMinSize", "1024");
        connector.setProperty("noCompressionUserAgents", "gozilla, traviata");
        connector.setProperty("compressableMimeType", "text/html,text/xml, text/css, application/json, application/javascript");
    }

    private int numberOfWebApps(Blade blade) {
        int numberOfWebApps = 0;
        for (Stage stage : blade.getStages()) {
            numberOfWebApps += stage.getWebApps().size();
        }

        return numberOfWebApps;
    }

    @Override
    public BladeHost getHost() {
        Engine engine = getEngine();
        if (engine.findChildren().length > 0) {
            return (BladeHost) engine.findChildren()[0];
        }
        BladeHost host = new BladeHost(blade, this::pushLifecycleEvent);
        host.setName(hostname);
        host.setAutoDeploy(false);
        host.setDeployOnStartup(false);
        getEngine().addChild(host);
        host.addContainerListener(this::pushHostEvent);
        return host;
    }

    public void startBladeServer() throws BladeStartException {
        blade.setStarting(true);
        try {
            super.start();
        } catch (Exception e) {
            throw new BladeStartException("Error starting Blade:", e);
        }
        running = true;
        blade.setRunning(true);
        updateStatus();
        logger.info("Blade Server Ready for b-business on http://localhost:{}", port);
        if (this.getServer().getPort() != -1) {
            logger.info("Shutdown port {}", this.getServer().getPort());
        }

    }


    public void autoStartStages() throws BladeStartException {
        for (Stage stage : blade.getStages()) {
            if (stage.isAutoStart()) {
                getHost().deploy(stage, classLoader);
            }
        }
        updateStatus();
    }

    public boolean isRunning() {
        return running;
    }

    private synchronized void pushHostEvent(ContainerEvent hostEvent) {
        Object data = hostEvent.getData();
        if (data instanceof StandardContext) {
            StandardContext standardContext = (StandardContext) data;
            updateBlade(standardContext);
        }
    }

    private WebApp find(String name) {
        for (Stage stage : blade.getStages()) {
            for (WebApp webApp : stage.getWebApps()) {
                if (name.equals(webApp.getName())) {
                    return webApp;
                }
            }
        }
        return null;
    }

    private void updateStatus() {
        if (enableBladeConsole) {
            if (isRunning()) {
                BladeUtils.updateLocalBlade(blade);
            }
            BladeUtils.updateBladeMaster(blade);
        }
    }

    private synchronized void pushLifecycleEvent(LifecycleEvent lifecycleEvent) {
        Object source = lifecycleEvent.getSource();
        if (source instanceof StandardContext) {
            StandardContext standardContext = (StandardContext) source;
            updateBlade(standardContext);
        }
    }

    private void updateBlade(StandardContext standardContext) {
        WebApp webApp = find(standardContext.getName());
        if (webApp != null) {

            String currentState = standardContext.getStateName();
            long startupTime = standardContext.getStartupTime();
            String state = webApp.getState();
            Long webAppStartupTime = webApp.getStartupTime();
            boolean stateChanged = currentState != null && !currentState.equals(state);
            boolean startupTimeChanged = webAppStartupTime != null && !webAppStartupTime.equals(startupTime);
            if (stateChanged || startupTimeChanged) {
                webApp.setState(currentState);
                if (startupTime > 100) {
                    webApp.setStartupTime(startupTime);
                }
                logger.debug("Web App: {} state changed", webApp.getName());
                updateStatus();
            }
        }
    }
}
