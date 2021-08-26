package com.backbase.oss.blade.tomcat;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;

public class BladeHost extends StandardHost {

    public static final String FAILED_TO_START_WEB_APP = "Failed to start Web App ";
    private final LifecycleListener contextLifecycleListener;
    private final Blade blade;

    BladeHost(Blade blade, LifecycleListener contextLifecycleListener) {
        this.blade = blade;
        setName(blade.getName());
        this.contextLifecycleListener = contextLifecycleListener;
    }

    void deploy(Stage stage, ClassLoader classLoader) throws BladeStartException {
        long startTime = System.currentTimeMillis();
        Log logger = getLogger();
        logger.info("Automatically starting stage: " + stage.getId());
        int size = stage.getWebApps().size();

        if (size > 1 && stage.isMultiThreaded()) {
            setStartStopThreads(0);
        } else {
            size = 1;
        }

        logger.info("Setting start/stop threads to number of webapps in stage: " + size);
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        List<Future<WebApp>> futures = new ArrayList<>();
        for (WebApp webApp : stage.getWebApps()) {
            Callable<WebApp> callable = () -> {
                try {
                    deploy(webApp, stage.isAutoStart(), classLoader);
                } catch (BladeStartException e) {
                    webApp.setState("FAILED");
                    logger.error("Failed to start WebApp:", e);
                    Thread.currentThread().interrupt();
                    throw e;
                }
                return webApp;
            };
            futures.add(executorService.submit(callable));
        }
        for (Future<WebApp> future : futures) {
            try {
                WebApp webApp = future.get();
                logger.info("Started: " + webApp.getName());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                if (e.getCause() instanceof BladeStartException) {
                    throw (BladeStartException) e.getCause();
                } else {
                    throw new BladeStartException("Failed to start Blade", e);
                }
            }
        }

        executorService.shutdown();

        // Maximum time to start stage = number of web apps + 5 minutes
        try {
            int maxTimeout = size * 5;
            executorService.awaitTermination(maxTimeout, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BladeStartException("A Web App look longer than 5 minutes to start. Terminating Blade", e);
        }

        long time = System.currentTimeMillis() - startTime;
        logger.info("Started Stage " + stage.getId() + " in : " + time / 1000d + "seconds");
        stage.setStartupTime(System.currentTimeMillis() - startTime);
        stage.setStarted(true);

        // If all auto starting stages are started then blade is ready
        blade.setReady(blade.getStages().stream()
            .filter(Stage::isAutoStart)
            .allMatch(Stage::isStarted)
        );
    }

    @SuppressWarnings("java:S3776")
    public void deploy(WebApp webApp, boolean start, ClassLoader classLoader) throws BladeStartException {
        String contextPath = webApp.getContextPath();
        if (webApp.getName() == null) {
            if (contextPath == null) {
                webApp.setName("Blade webApp");
            } else {
                webApp.setName(contextPath.replace("/", ""));
            }
            logger.info("WebApp Name not set. Setting name to context path: " + webApp.getName());

        }

        Log logger = getLogger();
        Container child = findChild(webApp.getName());
        if (child == null) {
            long startTime = System.currentTimeMillis();
            StandardContext ctx = new StandardContext();

            ctx.setParentClassLoader(classLoader);
            LifecycleListener listener;

            try {
                Class<?> clazz = Class.forName(getConfigClass());
                listener = (LifecycleListener) clazz.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new BladeStartException("Cannot create listener", e);
            }
            ctx.setName(webApp.getName());
            ctx.setPath(contextPath);
            ctx.setDocBase(webApp.getDocBase().getAbsolutePath());
            ctx.addLifecycleListener(listener);
            ctx.addLifecycleListener(new Tomcat.DefaultWebXmlListener());
            ctx.addLifecycleListener(contextLifecycleListener);

            ctx.setLoader(new WebappLoader());
            ctx.setPrivileged(webApp.isPrivileged());
            ctx.setReloadable(blade.isReloadable());

            if (webApp.getContextFileLocation() != null) {
                setupContextFileLocation(webApp, ctx);
            }

            if (webApp.getEnvironmentVariables() != null) {
                setupEnvironmentVariables(webApp, ctx);
            }

            try {
                addChild(ctx);
            } catch (Exception e) {
                throw new BladeStartException(FAILED_TO_START_WEB_APP + webApp.getName(), e);
            }

            if (start || webApp.getName().startsWith("blade-webapp") || webApp.getName().startsWith("jolokia-war")) {
                try {
                    ctx.start();
                } catch (LifecycleException e) {
                    throw new BladeStartException(FAILED_TO_START_WEB_APP + webApp.getName(), e);
                }
            }else{
                try {
                    ctx.stop();
                } catch (LifecycleException e) {
                    e.printStackTrace();
                }
            }
            long time = System.currentTimeMillis() - startTime;
            double seconds = time / 1000d;
            ctx.setStartupTime(time);
            logger.info("Started WebApp " + webApp.getName() + " in : " + seconds + " seconds");
            webApp.setStartupTime(time);

        } else {
            try {
                child.start();
            } catch (LifecycleException e) {
                throw new BladeStartException(FAILED_TO_START_WEB_APP + webApp.getName(), e);
            }
        }
    }

    private void setupEnvironmentVariables(WebApp webApp, StandardContext ctx) {
        webApp.getEnvironmentVariables().forEach((name, value) -> {
            ContextEnvironment environmentVariable = createEnvironmentVariable(webApp, name, value);
            ctx.getNamingResources().addEnvironment(environmentVariable);
        });
    }

    private void setupContextFileLocation(WebApp webApp, StandardContext ctx) throws BladeStartException {
        URL configFile = null;
        try {
            configFile = webApp.getContextFileLocation().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new BladeStartException("Invalid webApp Context File URL" + webApp.getName(), e);
        }
        ctx.setConfigFile(configFile);
    }

    private ContextEnvironment createEnvironmentVariable(WebApp webApp, String name, String value) {
        logger.info(
            "Setting Context Environment Variable: " + name + " for webApp: " + webApp.getName() + " with value: "
                + value);
        ContextEnvironment contextEnvironment = new ContextEnvironment();
        contextEnvironment.setName(name);
        contextEnvironment.setValue(value);
        contextEnvironment.setOverride(false);
        contextEnvironment.setType("java.lang.String");
        return contextEnvironment;
    }
}
