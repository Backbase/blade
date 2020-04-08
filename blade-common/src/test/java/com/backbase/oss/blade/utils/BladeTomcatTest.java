package com.backbase.oss.blade.utils;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import com.backbase.oss.blade.tomcat.BladeStartException;
import com.backbase.oss.blade.tomcat.BladeTomcat;
import com.backbase.oss.blade.tomcat.BladeTomcatBuilder;
import org.apache.catalina.connector.Connector;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BladeTomcatTest {

    private final File jolokiaWarFile = new File(BladeTomcatTest.class.getResource("/jolokia-war-1.3.7.war").getFile());
    private final File sampleWarFile = new File(BladeTomcatTest.class.getResource("/sample.war").getFile());

    @Test
    public void startStartEmptyBlade() throws Exception {
        Blade blade = setupBlade(9990);
        BladeTomcat bladeTomcat = new BladeTomcatBuilder()
                .setCatalinaHome(new File("./target/tomcat"))
                .setBlade(blade)
                .setDynamicMaxThreads(false)
                .setEnableGzip(true)
                .build();
        bladeTomcat.startBladeServer();

        assertTrue(bladeTomcat.isRunning());
        bladeTomcat.stop();

    }

    @Test(expected = BladeStartException.class)
    public void failStart() throws Exception {
        WebApp jolokiaWebApp = new WebApp(jolokiaWarFile, "jolokia", null);
        Blade blade = setupBlade(9992);

        Stage stage = new Stage();
        stage.setName("Test");
        stage.setId("test");
        stage.setMultiThreaded(true);
        stage.setAutoStart(true);
        stage.getWebApps().add(new WebApp(new File("missingfile.war"), "sample", null));
        blade.getStages().add(stage);


        BladeTomcat bladeTomcat = new BladeTomcatBuilder()
            .setCatalinaHome(new File("./target/tomcat"))
            .setBootstrappedWebApps(Collections.singletonList(jolokiaWebApp))
            .setBlade(blade)
            .setMaxThreads(10)
            .setDynamicMaxThreads(false)
            .setEnableGzip(true)
            .build();

        bladeTomcat.startBladeServer();
        bladeTomcat.autoStartStages();
        assertTrue(bladeTomcat.isRunning());
        bladeTomcat.stop();
    }

    @Test
    public void testBootStrappedApps() throws Exception {

        WebApp jolokiaWebApp = new WebApp(jolokiaWarFile, "jolokia", null);

        Blade blade = setupBlade(9991);

        BladeTomcat bladeTomcat = new BladeTomcatBuilder()
                .setCatalinaHome(new File("./target/tomcat"))
                .setBootstrappedWebApps(Collections.singletonList(jolokiaWebApp))
                .setBlade(blade)
                .setDynamicMaxThreads(false)
                .setEnableGzip(true)
                .build();

        bladeTomcat.startBladeServer();
        assertTrue(bladeTomcat.isRunning());
        bladeTomcat.stop();
    }

    @Test
    public void testStages() throws Exception {
        WebApp jolokiaWebApp = new WebApp(jolokiaWarFile, "jolokia", null);
        Blade blade = setupBlade(9992);

        Stage stage = new Stage();
        stage.setName("Test");
        stage.setId("test");
        stage.setMultiThreaded(true);
        stage.setAutoStart(true);
        stage.getWebApps().add(new WebApp(sampleWarFile, "sample", null));
        stage.getWebApps().add(new WebApp(sampleWarFile, "sample2", null));
        blade.getStages().add(stage);


        BladeTomcat bladeTomcat = new BladeTomcatBuilder()
                .setCatalinaHome(new File("./target/tomcat"))
                .setBootstrappedWebApps(Collections.singletonList(jolokiaWebApp))
                .setBlade(blade)
                .setMaxThreads(10)
                .setDynamicMaxThreads(false)
                .setEnableGzip(true)
                .build();

        bladeTomcat.startBladeServer();
        bladeTomcat.autoStartStages();
        assertTrue(bladeTomcat.isRunning());
        bladeTomcat.stop();
    }

    @Test
    public void verifyHttpsConnectorMaxHttpHeaderSize() throws Exception {
        Blade blade = setupBlade(9993);
        BladeTomcat bladeTomcat = new BladeTomcatBuilder()
                .setCatalinaHome(new File("./target/tomcat"))
                .setBlade(blade)
                .setDynamicMaxThreads(false)
                .setEnableGzip(true)
                .setEnableHttps(true)
                .setMaxHttpHeaderSize(65000)
                .build();
        bladeTomcat.startBladeServer();
        boolean foundHttpsConnector = false;
        for (Connector connector : bladeTomcat.getService().findConnectors()) {
            if(connector.getScheme().equals("https")) {
                assertEquals("maxHttpHeaderSize attribute for https connector does not match value expected",
                        65000, connector.getAttribute("maxHttpHeaderSize"));
                foundHttpsConnector = true;
            }
        }
        assertTrue("No https connector found", foundHttpsConnector);
        bladeTomcat.stop();
    }

    private Blade setupBlade(int port) throws MalformedURLException {
        Blade blade = new Blade();
        blade.setName("blade");
        blade.setPort(port);
        blade.setSecurePort(9443);
        blade.setBladeMaster(new URL("http://localhost:" + blade.getPort()));
        return blade;
    }




}
