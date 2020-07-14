package io.quarkus.devtools.project.buildfile;

import io.quarkus.bootstrap.model.AppArtifactCoords;
import io.quarkus.bootstrap.model.AppArtifactKey;
import io.quarkus.devtools.project.BuildTool;
import io.quarkus.devtools.project.extensions.ExtensionManager;
import java.io.IOException;
import java.util.Collection;

public class KotlinGradleBuildFile implements ExtensionManager {

    @Override
    public BuildTool getBuildTool() {
        return BuildTool.GRADLE_KOTLIN_DSL;
    }

    @Override
    public Collection<AppArtifactCoords> getInstalled() throws IOException {
        throw new IllegalStateException("This feature is not yet implemented outside of the Gradle Plugin.");
    }

    @Override
    public boolean hasQuarkusPlatformBom() throws IOException {
        throw new IllegalStateException("This feature is not yet implemented outside of the Gradle Plugin.");
    }

    @Override
    public ExtensionManager.InstallResult install(Collection<AppArtifactCoords> coords) throws IOException {
        throw new IllegalStateException("This feature is not yet implemented outside of the Gradle Plugin.");
    }

    @Override
    public ExtensionManager.UninstallResult uninstall(Collection<AppArtifactKey> keys) throws IOException {
        throw new IllegalStateException("This feature is not yet implemented outside of the Gradle Plugin.");
    }

}
