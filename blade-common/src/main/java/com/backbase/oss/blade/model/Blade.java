package com.backbase.oss.blade.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Blade {

    private String id;
    private String name;
    private int port;
    private int securePort;
    private boolean isStarting = false;
    private boolean isReady = false;
    private boolean isRunning = false;
    private Date startedOn;
    private List<Stage> stages = new ArrayList<>();
    private URL bladeMaster;
    private boolean reloadable;

    public Blade() {

    }

    public Blade(String id) {
        this.id = id;
    }

    public Blade(String id, List<Stage> stages) {
        this(id);
        this.stages = stages;
        this.isStarting = false;
        this.isReady = false;
        this.isRunning = false;
    }

    public String getId() {
        return id;
    }

    public boolean isStarting() {
        return isStarting;
    }

    public void setStarting(boolean starting) {
        isStarting = starting;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public Date getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(Date startedOn) {
        this.startedOn = startedOn;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSecurePort() {
        return securePort;
    }

    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setStages(List<Stage> newStages) {
        this.stages = newStages;
    }

    public URL getBladeMaster() {
        return bladeMaster;
    }

    public void setBladeMaster(URL bladeMaster) {
        this.bladeMaster = bladeMaster;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    @Override
    public String toString() {
        return "Blade{" +
                "isStarting=" + isStarting +
                ", isReady=" + isReady +
                ", isRunning=" + isRunning +
                ", startedOn=" + startedOn +
                ", stages=" + stages +
                '}';
    }
}
