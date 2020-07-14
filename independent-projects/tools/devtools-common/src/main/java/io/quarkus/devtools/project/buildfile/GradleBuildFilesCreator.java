package io.quarkus.devtools.project.buildfile;

import io.quarkus.bootstrap.model.AppArtifactCoords;
import io.quarkus.devtools.project.QuarkusProject;
import io.quarkus.devtools.project.buildfile.AbstractGradleBuildFile.Model;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import io.quarkus.platform.tools.ToolsUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

abstract class GradleBuildFilesCreator {

    private static final String GRADLE_PROPERTIES_PATH = "gradle.properties";
    private final QuarkusProject quarkusProject;

    private AtomicReference<Model> modelReference = new AtomicReference<>();

    public GradleBuildFilesCreator(QuarkusProject quarkusProject) {
        this.quarkusProject = quarkusProject;
    }

    abstract String getBuildGradlePath();

    abstract String getSettingGradlePath();

    abstract void addDependencyInBuildFile(AppArtifactCoords coords) throws IOException;

    abstract void createBuildContent(String groupId, String version) throws IOException;

    abstract void createSettingsContent(String artifactId) throws IOException;

    QuarkusProject getQuarkusProject() {
        return quarkusProject;
    }

    public void create(String groupId, String artifactId, String version,
            Properties properties, List<AppArtifactCoords> extensions) throws IOException {
        createSettingsContent(artifactId);
        createBuildContent(groupId, version);
        createProperties();
        extensions.stream()
                .forEach(e -> {
                    try {
                        addDependencyInBuildFile(e);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
        this.writeToDisk();
    }

    private void writeToDisk() throws IOException {
        writeToProjectFile(getSettingGradlePath(), getModel().getSettingsContent().getBytes());
        writeToProjectFile(getBuildGradlePath(), getModel().getBuildContent().getBytes());
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            getModel().getPropertiesContent().store(out, "Gradle properties");
            writeToProjectFile(GRADLE_PROPERTIES_PATH, out.toByteArray());
        }
    }

    public String getProperty(String propertyName) throws IOException {
        return getModel().getPropertiesContent().getProperty(propertyName);
    }

    Model getModel() throws IOException {
        return modelReference.updateAndGet(model -> {
            if (model == null) {
                try {
                    return readModel();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return model;
        });
    }

    boolean containsBOM(String groupId, String artifactId) throws IOException {
        String buildContent = getModel().getBuildContent();
        return buildContent.contains("enforcedPlatform(\"${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:")
                || buildContent.contains("enforcedPlatform(\"" + groupId + ":" + artifactId + ":");
    }

    private Model readModel() throws IOException {
        String settingsContent = "";
        String buildContent = "";
        Properties propertiesContent = new Properties();
        if (hasProjectFile(getSettingGradlePath())) {
            final byte[] settings = readProjectFile(getSettingGradlePath());
            settingsContent = new String(settings, StandardCharsets.UTF_8);
        }
        if (hasProjectFile(getBuildGradlePath())) {
            final byte[] build = readProjectFile(getBuildGradlePath());
            buildContent = new String(build, StandardCharsets.UTF_8);
        }
        if (hasProjectFile(GRADLE_PROPERTIES_PATH)) {
            final byte[] properties = readProjectFile(GRADLE_PROPERTIES_PATH);
            propertiesContent.load(new ByteArrayInputStream(properties));
        }
        return new Model(settingsContent, buildContent, propertiesContent, null, null);
    }

    protected boolean hasProjectFile(final String fileName) throws IOException {
        final Path filePath = quarkusProject.getProjectDirPath().resolve(fileName);
        return Files.exists(filePath);
    }

    protected byte[] readProjectFile(final String fileName) throws IOException {
        final Path filePath = quarkusProject.getProjectDirPath().resolve(fileName);
        return Files.readAllBytes(filePath);
    }

    protected void writeToProjectFile(final String fileName, final byte[] content) throws IOException {
        Files.write(quarkusProject.getProjectDirPath().resolve(fileName), content);
    }

    private void createProperties() throws IOException {
        final QuarkusPlatformDescriptor platform = quarkusProject.getPlatformDescriptor();
        Properties props = getModel().getPropertiesContent();
        if (props.getProperty("quarkusPluginVersion") == null) {
            props.setProperty("quarkusPluginVersion", ToolsUtils.getPluginVersion(ToolsUtils.readQuarkusProperties(platform)));
        }
        if (props.getProperty("quarkusPlatformGroupId") == null) {
            props.setProperty("quarkusPlatformGroupId", platform.getBomGroupId());
        }
        if (props.getProperty("quarkusPlatformArtifactId") == null) {
            props.setProperty("quarkusPlatformArtifactId", platform.getBomArtifactId());
        }
        if (props.getProperty("quarkusPlatformVersion") == null) {
            props.setProperty("quarkusPlatformVersion", platform.getBomVersion());
        }
    }

}
