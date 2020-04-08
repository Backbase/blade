package com.backbase.oss.blade.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WebApp {

    private String groupId;
    private String artifactId;
    private String version;
    private String module;
    private String name;
    private String url;
    private File docBase;
    private String contextPath;
    private File contextFileLocation;
    private boolean privileged = false;
    private boolean inheritClassloader = false;
    private boolean isMavenModule;
    private String state = "STOPPED";
    private Long startupTime;
    private boolean springBoot1App = false;

    private Map<String, String> environmentVariables = new HashMap<>();

    public WebApp() {
    }

    public WebApp(File docBase, String contextPath) {
        this.docBase = docBase;
        this.contextPath = contextPath;
        this.setName(contextPath);
    }

    public WebApp(File docBase, String contextPath, File contextFile) {
        this.docBase = docBase;
        this.contextPath = contextPath;
        this.contextFileLocation = contextFile;
        this.setName(contextPath);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getDocBase() {
        return docBase;
    }

    public void setDocBase(File docBase) {
        this.docBase = docBase;
    }

    public File getContextFileLocation() {
        return contextFileLocation;
    }

    public void setContextFileLocation(File contextFileLocation) {
        this.contextFileLocation = contextFileLocation;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInheritClassloader() {
        return inheritClassloader;
    }

    public void setInheritClassloader(boolean inheritClassloader) {
        this.inheritClassloader = inheritClassloader;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(Long startupTime) {
        this.startupTime = startupTime;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean isSpringBoot1App() {
        return springBoot1App;
    }

    public void setSpringBoot1App(boolean springBoot1App) {
        this.springBoot1App = springBoot1App;
    }

    public void merge(WebApp webApp) {
        this.groupId = webApp.groupId;
        this.artifactId = webApp.artifactId;
        this.version = webApp.version;
        this.module = webApp.module;
        this.name = webApp.name;
        this.url = webApp.url;
        this.docBase = webApp.docBase;
        this.contextPath = webApp.contextPath;
        this.contextFileLocation = webApp.contextFileLocation;
        this.privileged = webApp.privileged;
        this.inheritClassloader = webApp.inheritClassloader;
        this.isMavenModule = webApp.isMavenModule;
        this.startupTime = webApp.startupTime;
        this.state = webApp.state;
        this.environmentVariables = webApp.environmentVariables;
        this.springBoot1App = webApp.springBoot1App;
    }

    @Override
    public String toString() {
        return "WebApp{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", docBase=" + docBase +
                ", contextPath='" + contextPath + '\'' +
                ", contextFileLocation=" + contextFileLocation +
                ", privileged=" + privileged +
                ", state='" + state + '\'' +
                '}';
    }

}
