package io.quarkus.devtools.codestarts;

import static io.quarkus.devtools.codestarts.QuarkusCodestarts.inputBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.bootstrap.model.AppArtifactKey;
import io.quarkus.devtools.PlatformAwareTestBase;
import io.quarkus.devtools.ProjectTestUtil;
import io.quarkus.devtools.codestarts.CodestartSpec.Type;

class CodestartsTest extends PlatformAwareTestBase {

    private final Path projectPath = Paths.get("target/codestarts-test");

    @BeforeEach
    void setUp() throws IOException {
        ProjectTestUtil.delete(projectPath.toFile());
    }

    @Test
    void loadBundledCodestartsTest() throws IOException {
        final Collection<Codestart> codestarts = CodestartLoader
                .loadBundledCodestarts(inputBuilder(getPlatformDescriptor()).build());
        assertThat(codestarts).hasSize(11);
    }

    @Test
    void prepareProjectTestEmpty() throws IOException {
        final CodestartProject codestartProject = Codestarts
                .prepareProject(inputBuilder(getPlatformDescriptor()).build());
        assertThat(codestartProject.getRequiredCodestart(Type.PROJECT)).extracting(Codestart::getResourceDir)
                .isEqualTo("bundled-codestarts/project/quarkus");
        assertThat(codestartProject.getRequiredCodestart(Type.BUILDTOOL)).extracting(Codestart::getResourceDir)
                .isEqualTo("bundled-codestarts/buildtool/maven");
        assertThat(codestartProject.getRequiredCodestart(Type.CONFIG)).extracting(Codestart::getResourceDir)
                .isEqualTo("bundled-codestarts/config/properties");
        assertThat(codestartProject.getRequiredCodestart(Type.LANGUAGE)).extracting(Codestart::getResourceDir)
                .isEqualTo("bundled-codestarts/language/java");
        assertThat(codestartProject.getBaseCodestarts()).hasSize(4);
        assertThat(codestartProject.getExtraCodestarts()).extracting(Codestart::getResourceDir)
                .containsExactlyInAnyOrder("bundled-codestarts/tooling/dockerfiles");
    }

    @Test
    void prepareProjectTestGradle() throws IOException {
        final CodestartInput input = inputBuilder(getPlatformDescriptor())
                .putData("buildtool.name", "gradle")
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        assertThat(codestartProject.getRequiredCodestart(Type.BUILDTOOL)).extracting(Codestart::getResourceDir)
                .isEqualTo("bundled-codestarts/buildtool/gradle");
    }

    @Test
    void prepareProjectTestKotlin() throws IOException {
        final CodestartInput input = inputBuilder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-kotlin"))
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        assertThat(codestartProject.getRequiredCodestart(Type.LANGUAGE)).extracting(Codestart::getResourceDir)
                .isEqualTo("bundled-codestarts/language/kotlin");
    }

    @Test
    void prepareProjectTestScala() throws IOException {
        final CodestartInput input = inputBuilder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-scala"))
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        assertThat(codestartProject.getRequiredCodestart(Type.LANGUAGE)).extracting(Codestart::getResourceDir)
                .isEqualTo("bundled-codestarts/language/scala");
    }

    @Test
    void prepareProjectTestConfigYaml() throws IOException {
        final CodestartInput input = inputBuilder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-config-yaml"))
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        assertThat(codestartProject.getRequiredCodestart(Type.CONFIG)).extracting(Codestart::getResourceDir)
                .isEqualTo("bundled-codestarts/config/yaml");
    }

    @Test
    void prepareProjectTestResteasy() throws IOException {
        final CodestartInput input = inputBuilder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-resteasy"))
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        assertThat(codestartProject.getBaseCodestarts()).extracting(Codestart::getResourceDir)
                .contains("bundled-codestarts/config/properties");
        assertThat(codestartProject.getExtraCodestarts()).extracting(Codestart::getResourceDir)
                .containsExactlyInAnyOrder("bundled-codestarts/tooling/dockerfiles",
                        "codestarts/resteasy-example");
    }

    @Test
    void prepareProjectTestCommandMode() throws IOException {
        final CodestartInput input = inputBuilder(getPlatformDescriptor())
                .includeExamples()
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        assertThat(codestartProject.getBaseCodestarts()).extracting(Codestart::getResourceDir)
                .contains("bundled-codestarts/config/properties");
        assertThat(codestartProject.getExtraCodestarts()).extracting(Codestart::getResourceDir)
                .containsExactlyInAnyOrder("bundled-codestarts/tooling/dockerfiles",
                        "bundled-codestarts/example/commandmode-example");
    }
}
