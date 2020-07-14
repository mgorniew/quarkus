package io.quarkus.devtools.commands;

import io.quarkus.devtools.commands.data.QuarkusCommandException;
import io.quarkus.devtools.commands.data.QuarkusCommandOutcome;
import io.quarkus.devtools.project.BuildTool;
import io.quarkus.devtools.project.QuarkusProject;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Disabled;

class RemoveGradleKotlinExtensionsTest extends AbstractRemoveExtensionsTest<List<String>> {

    @Disabled
    void addExtensionTwiceInTwoBatches() throws IOException {
        //FIXME This is currently not working
    }

    @Override
    protected List<String> createProject() throws IOException, QuarkusCommandException {
        CreateProjectTest.delete(getProjectPath().toFile());
        new CreateProject(getProjectPath(), getPlatformDescriptor())
                .buildTool(BuildTool.GRADLE_KOTLIN_DSL)
                .groupId("org.acme")
                .artifactId("add-gradle-kts-extension-test")
                .version("0.0.1-SNAPSHOT")
                .execute();
        return readProject();
    }

    @Override
    protected List<String> readProject() throws IOException {
        return Files.readAllLines(getProjectPath().resolve("build.gradle.kts"));
    }

    @Override
    protected QuarkusCommandOutcome addExtensions(final List<String> extensions) throws IOException, QuarkusCommandException {
        return new AddExtensions(getQuarkusProject())
                .extensions(new HashSet<>(extensions))
                .execute();
    }

    @Override
    protected QuarkusCommandOutcome removeExtensions(final List<String> extensions)
            throws IOException, QuarkusCommandException {
        return new RemoveExtensions(getQuarkusProject())
                .extensions(new HashSet<>(extensions))
                .execute();
    }

    @Override
    protected long countDependencyOccurrences(final List<String> buildFile, final String groupId, final String artifactId,
            final String version) {
        return buildFile.stream()
                .filter(d -> d.equals(getBuildFileDependencyString(groupId, artifactId, version)))
                .count();
    }

    private QuarkusProject getQuarkusProject() {
        final Path projectPath = getProjectPath();
        final QuarkusPlatformDescriptor platformDescriptor = getPlatformDescriptor();
        return QuarkusProject.of(projectPath, platformDescriptor,
                new AddGradleKotlinExtensionsTest.TestingGradleBuildFile(projectPath, platformDescriptor));
    }

    private static String getBuildFileDependencyString(final String groupId, final String artifactId, final String version) {
        final String versionPart = version != null ? ":" + version : "";
        return "    implementation(\"" + groupId + ":" + artifactId + versionPart + "\")";
    }
}
