package com.backbase.oss.blade.webapp;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import java.util.Collections;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BladeServerEndpointTests {

    @Test
    public void testBladeServerEndpoint() {

        BladeServerEndpoint bladeServerEndpoint = new BladeServerEndpoint();
        Session session = Mockito.mock(Session.class);
        Async async = Mockito.mock(Async.class);
        Mockito.when(session.getAsyncRemote()).thenReturn(async);

        Mockito.when(session.isOpen()).thenReturn(true);


        bladeServerEndpoint.onOpen(session);
        setupBladeRegistry();
        bladeServerEndpoint.onClose(session);
    }

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
