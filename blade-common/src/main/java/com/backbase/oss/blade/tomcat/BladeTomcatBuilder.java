package com.backbase.oss.blade.tomcat;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.WebApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BladeTomcatBuilder {
    File catalinaHome;
    List<WebApp> bootstrappedWebApps = new ArrayList<>();
    Blade blade;
    boolean enableGzip;
    int maxThreads = 10;
    boolean dynamicMaxThreads = true;

    String keyAlias;
    String keystorePass;
    String keystoreType;
    String keystoreFile;
    boolean enableHttps;
    boolean enableBladeConsole;
    int maxHttpHeaderSize;

    public BladeTomcatBuilder setCatalinaHome(File catalinaHome) {
        this.catalinaHome = catalinaHome;
        return this;
    }

    public BladeTomcatBuilder setBootstrappedWebApps(List<WebApp> bootstrappedWebApps) {
        this.bootstrappedWebApps = bootstrappedWebApps;
        return this;
    }

    public BladeTomcatBuilder setBlade(Blade blade) {
        this.blade = blade;
        return this;
    }

    public BladeTomcatBuilder setEnableGzip(boolean enableGzip) {
        this.enableGzip = enableGzip;
        return this;
    }

    public BladeTomcatBuilder setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public BladeTomcatBuilder setDynamicMaxThreads(boolean dynamicMaxThreads) {
        this.dynamicMaxThreads = dynamicMaxThreads;
        return this;
    }

    public BladeTomcatBuilder setEnableHttps(boolean enableHttps) {
        this.enableHttps = enableHttps;
        return this;
    }

    public BladeTomcatBuilder setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
        return this;
    }

    public BladeTomcatBuilder setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
        return this;
    }

    public BladeTomcatBuilder setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
        return this;
    }

    public BladeTomcatBuilder setKeystoreFile(String keystoreFile) {
        this.keystoreFile = keystoreFile;
        return this;
    }

    public BladeTomcatBuilder setMaxHttpHeaderSize(int maxHttpHeaderSize) {
        this.maxHttpHeaderSize = maxHttpHeaderSize;
        return this;
    }

    public BladeTomcat build() throws BladeStartException {
        return new BladeTomcat(this);
    }

    public BladeTomcatBuilder setEnableBladeConsole(boolean enableBladeConsole) {
        this.enableBladeConsole = enableBladeConsole;
        return this;
    }

}