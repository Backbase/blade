package com.backbase.oss.blade.webapp;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BladeRegistryTests {

    @Test
    public void teestRegistru() {
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

        bladeRegistry.refresh();
        bladeRegistry.remove(bladeId);
        Assertions.assertNull(bladeRegistry.get(bladeId));



    }

}
