package com.backbase.oss.blade.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "runner", threadSafe = true, aggregator = true,
        requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE)
@Execute(phase = LifecyclePhase.PACKAGE)
public class BladeRunnerMojo extends BladeRunMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("A real connoisseur uses blade:runner");
        super.execute();
    }
}
