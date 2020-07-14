package io.quarkus.devtools.project.buildfile;

import io.quarkus.bootstrap.model.AppArtifactKey;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

// We keep it here to take advantage of the abstract tests
abstract class AbstractGradleBuildFile extends BuildFile {

    private static final String GRADLE_PROPERTIES_PATH = "gradle.properties";

    private final Optional<Path> rootProjectPath;

    private AtomicReference<Model> modelReference = new AtomicReference<>();

    AbstractGradleBuildFile(final Path projectDirPath, final QuarkusPlatformDescriptor platformDescriptor) {
        super(projectDirPath, platformDescriptor);
        this.rootProjectPath = Optional.empty();
    }

    AbstractGradleBuildFile(final Path projectDirPath, final QuarkusPlatformDescriptor platformDescriptor,
            Path rootProjectPath) {
        super(projectDirPath, platformDescriptor);
        this.rootProjectPath = Optional.ofNullable(rootProjectPath);
    }

    protected abstract String getBuildGradlePath();

    protected abstract String getSettingGradlePath();

    @Override
    public void writeToDisk() throws IOException {
        if (rootProjectPath.isPresent()) {
            Files.write(rootProjectPath.get().resolve(getSettingGradlePath()), getModel().getRootSettingsContent().getBytes());
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                getModel().getRootPropertiesContent().store(out, "Gradle properties");
                Files.write(rootProjectPath.get().resolve(GRADLE_PROPERTIES_PATH),
                        out.toByteArray());
            }
        } else {
            writeToProjectFile(getSettingGradlePath(), getModel().getSettingsContent().getBytes());
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                getModel().getPropertiesContent().store(out, "Gradle properties");
                writeToProjectFile(GRADLE_PROPERTIES_PATH, out.toByteArray());
            }
        }
        writeToProjectFile(getBuildGradlePath(), getModel().getBuildContent().getBytes());
    }

    @Override
    protected void removeDependencyFromBuildFile(AppArtifactKey key) throws IOException {
        StringBuilder newBuildContent = new StringBuilder();
        Scanner scanner = new Scanner(getModel().getBuildContent());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains(key.getGroupId() + ":" + key.getArtifactId())) {
                newBuildContent.append(line).append(System.lineSeparator());
            }
        }
        scanner.close();
        getModel().setBuildContent(newBuildContent.toString());
    }

    @Override
    protected boolean containsBOM(String groupId, String artifactId) throws IOException {
        String buildContent = getModel().getBuildContent();
        return buildContent.contains("enforcedPlatform(\"${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:")
                || buildContent.contains("enforcedPlatform(\"" + groupId + ":" + artifactId + ":");
    }

    @Override
    public String getProperty(String propertyName) throws IOException {
        final String property = getModel().getPropertiesContent().getProperty(propertyName);
        if (property != null || getModel().getRootPropertiesContent() == null) {
            return property;
        }
        return getModel().getRootPropertiesContent().getProperty(propertyName);
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

    @Override
    protected void refreshData() {
        this.modelReference.set(null);
    }

    private boolean hasRootProjectFile(final String fileName) throws IOException {
        if (!rootProjectPath.isPresent()) {
            return false;
        }
        final Path filePath = rootProjectPath.get().resolve(fileName);
        return Files.exists(filePath);
    }

    private byte[] readRootProjectFile(final String fileName) throws IOException {
        final Path filePath = rootProjectPath
                .orElseThrow(() -> new IllegalStateException("There is no rootProject defined in this GradleBuildFile"))
                .resolve(fileName);
        return Files.readAllBytes(filePath);
    }

    private Model readModel() throws IOException {
        String settingsContent = "";
        String buildContent = "";
        Properties propertiesContent = new Properties();
        String rootSettingsContent = null;
        Properties rootPropertiesContent = null;
        if (hasProjectFile(getSettingGradlePath())) {
            final byte[] settings = readProjectFile(getSettingGradlePath());
            settingsContent = new String(settings, StandardCharsets.UTF_8);
        }
        if (hasRootProjectFile(getSettingGradlePath())) {
            final byte[] settings = readRootProjectFile(getSettingGradlePath());
            rootSettingsContent = new String(settings, StandardCharsets.UTF_8);
        }
        if (hasProjectFile(getBuildGradlePath())) {
            final byte[] build = readProjectFile(getBuildGradlePath());
            buildContent = new String(build, StandardCharsets.UTF_8);
        }
        if (hasProjectFile(GRADLE_PROPERTIES_PATH)) {
            final byte[] properties = readProjectFile(GRADLE_PROPERTIES_PATH);
            propertiesContent.load(new ByteArrayInputStream(properties));
        }
        if (hasRootProjectFile(GRADLE_PROPERTIES_PATH)) {
            final byte[] properties = readRootProjectFile(GRADLE_PROPERTIES_PATH);
            rootPropertiesContent = new Properties();
            rootPropertiesContent.load(new ByteArrayInputStream(properties));
        }
        return new Model(settingsContent, buildContent, propertiesContent, rootSettingsContent, rootPropertiesContent);
    }

    protected String getBuildContent() throws IOException {
        return getModel().getBuildContent();
    }

    static void readLineByLine(String content, Consumer<String> lineConsumer) {
        try (Scanner scanner = new Scanner(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                String currentLine = scanner.nextLine();
                lineConsumer.accept(currentLine);
            }
        }
    }

    static class Model {
        private String settingsContent;
        private String buildContent;
        private Properties propertiesContent;

        private String rootSettingsContent;
        private Properties rootPropertiesContent;

        public Model(String settingsContent, String buildContent, Properties propertiesContent, String rootSettingsContent,
                Properties rootPropertiesContent) {
            this.settingsContent = settingsContent;
            this.buildContent = buildContent;
            this.propertiesContent = propertiesContent;
            this.rootSettingsContent = rootSettingsContent;
            this.rootPropertiesContent = rootPropertiesContent;
        }

        public String getSettingsContent() {
            return settingsContent;
        }

        public String getBuildContent() {
            return buildContent;
        }

        public Properties getPropertiesContent() {
            return propertiesContent;
        }

        public String getRootSettingsContent() {
            return rootSettingsContent;
        }

        public Properties getRootPropertiesContent() {
            return rootPropertiesContent;
        }

        public void setSettingsContent(String settingsContent) {
            this.settingsContent = settingsContent;
        }

        public void setBuildContent(String buildContent) {
            this.buildContent = buildContent;
        }

    }

}
