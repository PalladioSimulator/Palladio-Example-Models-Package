package org.palladiosimulator.examples.test;

import static org.eclipse.emf.common.util.Diagnostic.OK;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;

@TestMethodOrder(OrderAnnotation.class)
class ValidModelTest {
    // @formatter:off
    public static final Set<String> KNOWN_MODEL_EXTENSIONS = Set.of(".allocation",
                                                                     ".experiments",
                                                                     ".measuringpoint",
                                                                     ".monitorrepository",
                                                                     ".repository",
                                                                     ".resourceenvironment",
                                                                     ".system",
                                                                     ".usagemodel",
                                                                     ".metricspec");
    // @formatter:on
    public static final int MAX_DEPTH = Integer.MAX_VALUE;
    private static final Map<URI, Resource> resources = new HashMap<>();

    public static final Path START = Path.of("../../bundles/org.palladiosimulator.examples.package/target/examples/")
            .toAbsolutePath().normalize();

    private static URI createUri(final Path path) {
        return URI.createURI(path.normalize().toUri().toString());
    }

    private static Stream<URI> findTheModels() throws IOException {
        return Files.find(START, MAX_DEPTH, ValidModelTest::isKnownModel).map(ValidModelTest::createUri);
    }

    private static Stream<URI> getTheModels() {
        return resources.keySet().stream();
    }

    private static boolean isKnownModel(final Path filePath, final BasicFileAttributes fileAttributes) {
        final String fileName = filePath.toAbsolutePath().normalize().getFileName().toString().toLowerCase();
        return fileAttributes.isRegularFile() && KNOWN_MODEL_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        try {
            StandaloneInitializerBuilder.builder()
                    .registerProjectURI(org.palladiosimulator.architecturaltemplates.catalog.black.PCMLibrary.class,
                            "org.palladiosimulator.architecturaltemplates.catalog")
                    .addCustomTask(new EMFProfileInitializationTask("org.palladiosimulator.architecturaltemplates.catalog",
                            "profiles/DynamicHorizontalScalingAssemblyContext.emfprofile_diagram"))
                    .build().init();
        } catch (final StandaloneInitializationException e) {
            EcorePlugin.ExtensionProcessor.process(null);
        }
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        resources.values().forEach(Resource::unload);
        resources.clear();
    }

    @ParameterizedTest
    @MethodSource("findTheModels")
    @Order(1)
    void loadResourceTest(final URI uri) {
        assertNull(uri.toFileString(), resources.put(uri, new ResourceSetImpl().getResource(uri, true)));
    }

    @ParameterizedTest
    @MethodSource("getTheModels")
    @Order(2)
    void validateResourceTest(final URI uri) {
        assertFalse(uri.toFileString(), resources.get(uri).getContents().stream().map(Diagnostician.INSTANCE::validate)
                .mapToInt(Diagnostic::getSeverity).filter(s -> s != OK).findAny().isPresent());
    }

}
