package com.backbase.oss.blade.webapp;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.WebApp;
import com.backbase.oss.blade.tomcat.BladeHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.manager.ManagerServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1989")

public class BladeServlet extends ManagerServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BladeRegistry bladeRegistry = BladeRegistry.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(BladeServlet.class);

    private BladeHost getBladeHost() {
        return (BladeHost) host;
    }

    private Host getHost() {
        return host;
    }

    public BladeServlet() {
        super();
        logger.info("Starting Blade Manager Servlet");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json");
        Map<String, Blade> blades = bladeRegistry.getBlades();

        String pathInfo = request.getPathInfo();
        if (pathInfo.startsWith("/api/status")) {
            String[] parts = pathInfo.split("/");
            if (parts.length == 3) {
                // Return all blades from blade registry
                objectMapper.writeValue(response.getOutputStream(), blades.values());
            } else if (parts.length == 4) {
                // Return named blade with ID from blade registry
                objectMapper.writeValue(response.getOutputStream(), bladeRegistry.get(parts[3]));
            }
        } else {
            super.doGet(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String body = read(req.getInputStream());

        String pathInfo = req.getPathInfo();
        if ("/api/blades".equals(pathInfo)) {
            registerBlade(resp, body);
        } else if (pathInfo.startsWith("/api/start/")) {
            startWebApp(resp, pathInfo);
        } else if (pathInfo.startsWith("/api/stop/")) {
            stopWebApp(resp, pathInfo);
        }
    }

    private void stopWebApp(HttpServletResponse resp, String pathInfo) throws ServletException {
        WebApp webApp = getWebApp(pathInfo);
        if (webApp != null) {
            try {
                stop(webApp);
            } catch (LifecycleException e) {
                throw new ServletException("Cannot stop webapp: " + webApp.getName(), e);
            }
            webApp.setStartupTime(0L);
            resp.setStatus(200);
        } else {
            throw new ServletException("Invalid Request: " + pathInfo);
        }
    }

    private void startWebApp(HttpServletResponse resp, String pathInfo) throws ServletException {
        WebApp webApp = getWebApp(pathInfo);
        if (webApp != null) {
            try {
                start(webApp);
            } catch (LifecycleException e) {
                throw new ServletException("Cannot start webapp: " + webApp.getName(), e);
            }
            resp.setStatus(200);
        } else {
            throw new ServletException("Invalid request: " + pathInfo);
        }
    }

    private void registerBlade(HttpServletResponse resp, String body) throws IOException {
        Blade blade = objectMapper.readValue(body, Blade.class);
        boolean isNew = bladeRegistry.hasBlade(blade.getId());
        bladeRegistry.put(blade);
        if (isNew) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    private WebApp getWebApp(String pathInfo) {
        String webAppName = pathInfo.substring(pathInfo.lastIndexOf('/'));
        return bladeRegistry.find(webAppName);
    }

    private void stop(WebApp webApp) throws LifecycleException {
        logger.info("Stopping Web App: {}", webApp);
        Container child = getHost().findChild(webApp.getName());
        if (child != null) {
            child.stop();
            logger.info("Stopped Web App: {}", webApp);
        }
    }

    private void start(WebApp webApp) throws LifecycleException {
        logger.info("Starting Web App: {}", webApp);
        Container child = getHost().findChild(webApp.getName());
        if (child == null) {
            // App is not auto deployed. Deploy it on host
            try {
                getBladeHost().deploy(webApp, true, Thread.currentThread().getContextClassLoader());
                logger.info("Started Web App: {}", webApp);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error deploying webApp: " + webApp, e);
            }
        } else {
            child.start();
        }
    }

    private String read(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

}
