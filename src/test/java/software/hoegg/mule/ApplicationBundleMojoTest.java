package software.hoegg.mule;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import software.hoegg.mule.stubs.SimpleBundleProjectStub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationBundleMojoTest {

	private static String basedir;

	@Rule
	public MojoRule rule = new MojoRule();

	@BeforeClass
	public static void getSystemProperties() {
		basedir = System.getProperty( "basedir" );
	}

	@Before
	public void prepareApplicationZips() throws IOException {
		for (File appDir : bundledAppsDir().listFiles()) {
			pluginTestDir().mkdirs();
			final File applicationZip = new File(pluginTestDir(), appDir.getName() + ".zip");
			ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(applicationZip));
			try {
				compressSubDirectories(appDir, appDir, zipFile);
			} finally {
				IOUtils.closeQuietly(zipFile);
			}
		}
	}


	private void compressSubDirectories(File root, File subdirectory, ZipOutputStream zip) throws IOException {
		for (File f : subdirectory.listFiles()) {
			if (f.isDirectory()) {
				compressSubDirectories(root, f, zip);
			} else {
				zip.putNextEntry(
					new ZipEntry(f.getAbsolutePath().replace(root.getAbsolutePath() + "/", "")));
				FileInputStream in = new FileInputStream(f);
				IOUtils.copy(in, zip);
				IOUtils.closeQuietly(in);
			}
		}
	}

	@Test
	public void shouldGenerateDeploymentDescriptor() throws Exception {
		ApplicationBundleMojo mojo = (ApplicationBundleMojo) rule.lookupMojo("create", new File(bundleProjectDir(), "pom.xml"));
		mojo.execute();

		Properties deployProps = getBundleMuleDeployProperties();
		assertTrue("Missing expected property config.resources",
			deployProps.containsKey("config.resources"));
		List<String> configResources = Arrays.asList(deployProps.getProperty("config.resources").split(","));
		assertEquals("config files", 5, configResources.size());
	}

	private Properties getBundleMuleDeployProperties() throws IOException {
		File descriptor = new File(bundleTargetDir(), "mule-deploy.properties");
		assertTrue(descriptor.exists());

		FileInputStream in = new FileInputStream(descriptor);
		Properties deployProps = new Properties();
		deployProps.load(in);
		IOUtils.closeQuietly(in);
		return deployProps;
	}

	private File bundleProjectDir() {
		return new File( basedir,
			"target/test-classes/simple-bundle-project" );
	}

	private File bundledAppsDir() {
		return new File( basedir,
			"target/test-classes/bundled-apps");
	}

	private File pluginTestDir() {
		return new File( basedir,
			"target/test-bundle-resources");
	}

	private File bundleTargetDir() {
		return new File( basedir,
			"target/bundle-target/mule-bundle" );
	}

	private File testingLocalRepositoryDir() {
		return new File( basedir,
			"target/testing-local-repository");
	}
}
