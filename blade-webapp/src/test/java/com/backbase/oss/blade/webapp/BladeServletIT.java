package com.backbase.oss.blade.webapp;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import com.backbase.oss.blade.tomcat.BladeStartException;
import com.backbase.oss.blade.tomcat.BladeTomcat;
import com.backbase.oss.blade.tomcat.BladeTomcatBuilder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import org.apache.catalina.LifecycleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("squid:S1989")
public class BladeServletIT {

    private static Logger logger = LoggerFactory.getLogger(BladeServletIT.class);
    @Test
    public void testBladeServlet() throws IOException, BladeStartException, LifecycleException {
        File[] warFiles = new File("target").listFiles(pathname -> pathname.getName().endsWith(".war"));

        Path target;
        if(warFiles != null &&  warFiles.length == 1) {
            target = warFiles[0].toPath();
        } else {
            throw new IllegalStateException("no valid war file found. Please run after package");
        }

        WebApp bladeWebApp = new WebApp(target.toFile(), "/blade");
        bladeWebApp.setPrivileged(true);

        Blade blade = setupBlade(9991);

        BladeTomcat bladeTomcat = new BladeTomcatBuilder()
            .setCatalinaHome(new File("./target/tomcat"))
            .setBootstrappedWebApps(Collections.singletonList(bladeWebApp))
            .setBlade(blade)
            .setDynamicMaxThreads(false)
            .setEnableGzip(true)
            .setMaxHttpHeaderSize(1024*1024)
            .setEnableBladeConsole(true)
            .build();

        bladeTomcat.startBladeServer();


        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> stringResponseEntity = restTemplate
            .postForEntity("http://127.0.0.1:9991/blade/blade/api/blades", blade, String.class);
        Assertions.assertEquals(202, stringResponseEntity.getStatusCode().value());


        ResponseEntity<Blade[]> getBlades = restTemplate
            .getForEntity("http://127.0.0.1:9991/blade/blade/api/status", Blade[].class);
        Assertions.assertEquals(1, getBlades.getBody().length );

        ResponseEntity<Blade> getBlade = restTemplate.getForEntity("http://127.0.0.1:9991/blade/blade/api/status/blade", Blade.class);
        Assertions.assertNotNull(getBlade.getBody());

        ResponseEntity<String> stringResponseEntity1 = restTemplate
            .postForEntity("http://127.0.0.1:9991/blade/blade/api/start/jolokia", null, String.class);
        System.out.println(stringResponseEntity1.getBody());

        ResponseEntity<String> stringResponseEntity2 = restTemplate
            .postForEntity("http://127.0.0.1:9991/blade/blade/api/stop/jolokia", null, String.class);

        System.out.println(stringResponseEntity.getBody());
        bladeTomcat.stop();
    }

    private Blade setupBlade(int port) throws MalformedURLException {
        Blade blade = new Blade("blade");
        blade.setName("blade");
        blade.setPort(port);
        blade.setSecurePort(9443);
        blade.setBladeMaster(new URL("http://localhost:" + blade.getPort()));

        File docBase = new File(getClass().getResource("/jolokia-war-1.3.7.war").getFile());
        WebApp jolokia = new WebApp(docBase, "/jolokia");



        Stage stage = new Stage();
        stage.setAutoStart(false);
        stage.setWebApps(Collections.singletonList(jolokia));
        blade.setStages(Collections.singletonList(stage));

        return blade;
    }


    @BeforeEach
    private void setupBladeRegistry() {
        BladeRegistry bladeRegistry = BladeRegistry.getInstance();
        String bladeId = "test";
        Blade blade = new Blade(bladeId);

        WebApp webApp = new WebApp();
        webApp.setName("webApp");

        Stage stage = new Stage();
        stage.setWebApps(Collections.singletonList(webApp));
        blade.setStages(Collections.singletonList(stage));

        bladeRegistry.put(blade);

        Assertions.assertNotNull(bladeRegistry.get(bladeId));
        Assertions.assertNotNull(bladeRegistry.find("webApp"));
        bladeRegistry.remove(bladeId);
        Assertions.assertNull(bladeRegistry.get(bladeId));
    }

}
