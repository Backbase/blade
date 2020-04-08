package com.backbase.oss.blade.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, aggregator = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class BladeValidateMojo extends AbstractBladeMojo {

    @Override
    public void execute() throws MojoExecutionException {
        testTomcatPort();
        createCatalinaHome();
        setupSystemProperties();
        initializeStages();
    }
}
