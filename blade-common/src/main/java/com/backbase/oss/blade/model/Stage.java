package com.backbase.oss.blade.model;

import java.util.ArrayList;
import java.util.List;

public class Stage {

    private String id;
    private String name;
    private boolean autoStart = true;
    private List<WebApp> webApps = new ArrayList<>();
    private boolean started = false;
    private boolean multiThreaded = false;
    private String url;
    private Long startupTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public List<WebApp> getWebApps() {
        return webApps;
    }

    public void setWebApps(List<WebApp> webApps) {
        this.webApps = webApps;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isMultiThreaded() {
        return multiThreaded;
    }

    public void setMultiThreaded(boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(Long startupTime) {
        this.startupTime = startupTime;
    }

    @Override
    public String toString() {
        return "Stage{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", autoStart=" + autoStart +
                ", webApps=" + webApps +
                ", started=" + started +
                '}';
    }
}
