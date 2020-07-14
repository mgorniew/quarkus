package io.quarkus.devtools.project.buildfile;

import io.quarkus.bootstrap.model.AppArtifactCoords;
import io.quarkus.devtools.project.BuildTool;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractGroovyGradleBuildFile extends AbstractGradleBuildFile {

    public AbstractGroovyGradleBuildFile(Path projectDirPath, QuarkusPlatformDescriptor platformDescriptor) {
        super(projectDirPath, platformDescriptor);
    }

    public AbstractGroovyGradleBuildFile(Path projectDirPath, QuarkusPlatformDescriptor platformDescriptor,
            Path rootProjectPath) {
        super(projectDirPath, platformDescriptor, rootProjectPath);
    }

    @Override
    protected String getBuildGradlePath() {
        return "build.gradle";
    }

    @Override
    protected String getSettingGradlePath() {
        return "settings.gradle";
    }

    @Override
    public BuildTool getBuildTool() {
        return BuildTool.GRADLE;
    }

    @Override
    protected void addDependencyInBuildFile(AppArtifactCoords coords) throws IOException {
        addDependencyInModel(getModel(), coords);
    }

    static void addDependencyInModel(Model model, AppArtifactCoords coords) throws IOException {
        StringBuilder newBuildContent = new StringBuilder();
        readLineByLine(model.getBuildContent(), currentLine -> {
            newBuildContent.append(currentLine).append(System.lineSeparator());
            if (currentLine.startsWith("dependencies {")) {
                newBuildContent.append("    implementation '")
                        .append(coords.getGroupId())
                        .append(":")
                        .append(coords.getArtifactId());
                if (coords.getVersion() != null && !coords.getVersion().isEmpty()) {
                    newBuildContent.append(":")
                            .append(coords.getVersion());
                }
                newBuildContent.append("'")
                        .append(System.lineSeparator());
            }
        });
        model.setBuildContent(newBuildContent.toString());
    }

}
