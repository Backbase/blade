package com.backbase.oss.blade.webapp;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import com.backbase.oss.blade.utils.BladeUtils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BladeRegistry {

    private static BladeRegistry instance;
    private final Map<String, Blade> blades;
    private final PropertyChangeSupport propertySupport;
    private final Logger logger = LoggerFactory.getLogger(BladeRegistry.class);

    public static BladeRegistry getInstance() {
        if (instance == null) {
            instance = new BladeRegistry();
            // Load latest status
        }
        return instance;
    }

    private BladeRegistry() {
        blades = new LinkedHashMap<>();
        propertySupport = new PropertyChangeSupport(this);
    }


    public void put(Blade blade) {
        blades.put(blade.getId(), blade);
        propertySupport.firePropertyChange("put", null, blades.values());
    }

    public void remove(String bladeId) {

        blades.remove(bladeId);
        propertySupport.firePropertyChange("remove", null, blades.values());
    }

    public Blade get(String id) {
        return blades.get(id);
    }


    public Map<String, Blade> getBlades() {
        return blades;
    }

    public boolean hasBlade(String id) {
        return blades.containsKey(id);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public WebApp find(String name) {
        for (Blade blade : blades.values()) {
            for (Stage stage : blade.getStages()) {
                for (WebApp webApp : stage.getWebApps()) {
                    if (name.equals(webApp.getName())) {
                        return webApp;
                    }
                }
            }
        }
        return null;
    }

    public void refresh() {
        new Thread(() -> {
            for (Blade blade : blades.values()) {
                try {
                    Blade updatedBlade = BladeUtils.getBladeStatus(blade);
                    blades.put(blade.getId(), updatedBlade);
                    blade.setReady(true);
                    blade.setRunning(true);
                    blade.setStarting(false);
                } catch (IOException e) {
                    logger.warn("Cannot get blade: {} ", blade.getId());
                    blade.setReady(false);
                    blade.setRunning(false);
                    blade.setStarting(false);
                }
            }
        }).start();

    }


}
