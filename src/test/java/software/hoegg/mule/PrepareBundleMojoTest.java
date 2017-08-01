package software.hoegg.mule;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class PrepareBundleMojoTest {

	private static String basedir;

	@Rule
	public MojoRule rule = new MojoRule();

	@BeforeClass
	public static void getSystemProperties() {
		basedir = System.getProperty( "basedir" );
	}

	@Before
	public void cleanBundleDir() {
		FileUtils.deleteQuietly(bundleTargetDir());
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
		execute(simpleBundleProjectDir());

		Properties deployProps = getBundleMuleDeployProperties();
		assertTrue("Missing expected property config.resources",
			deployProps.containsKey("config.resources"));
		List<String> configResources = Arrays.asList(deployProps.getProperty("config.resources").split(","));
		assertEquals("config files", 5, configResources.size());
	}

	@Test
	public void shouldPrefixConfigurationFilenamesWithArtifactId() throws Exception {
		execute(simpleBundleProjectDir());

		Properties deployProps = getBundleMuleDeployProperties();
		assertTrue("Missing expected property config.resources",
			deployProps.containsKey("config.resources"));
		List<String> configResources = Arrays.asList(deployProps.getProperty("config.resources").split(","));
		assertThat(configResources, containsInAnyOrder(
			"simple-app1.config1.xml",
			"simple-app1.config2.xml",
			"simple-app1.globals.xml",
			"simple-app2.config-a.xml",
			"simple-app2.globals.xml"));
	}

	@Test
	public void shouldExcludeUnbundledConfigsByDefault() throws Exception {
		execute(realisticBundleProjectDir());

		Properties deployProps = getBundleMuleDeployProperties();
		assertTrue("Missing expected property config.resources",
			deployProps.containsKey("config.resources"));
		List<String> configResources = Arrays.asList(deployProps.getProperty("config.resources").split(","));
		assertThat(configResources, not(hasItem(containsString("unbundled"))));

		assertThat(Arrays.asList(bundleTargetDir().list()), not(hasItem(containsString( "unbundled"))));
	}

	@Test
	public void shouldCopyConfigurationFiles() throws Exception {
		execute(simpleBundleProjectDir());

		assertBundledFileContents("simple-app1.config1.xml", "<config1/>");
		assertBundledFileContents("simple-app1.config2.xml", "<config2/>");
		assertBundledFileContents("simple-app1.globals.xml", "<globals/>");
		assertBundledFileContents("simple-app2.config-a.xml", "<config-a/>");
		assertBundledFileContents("simple-app2.globals.xml", "<globals/>");
	}

	@Test
	@Ignore("test for next feature")
	public void shouldCopyDependenciesToLib() throws Exception {
		execute(realisticBundleProjectDir());

		File libDir = new File(bundleTargetDir(), "lib");
		assertTrue("bundle did not contain expected lib directory", libDir.exists() && libDir.isDirectory());
		final File jarFile = new File(libDir, "commons-io-2.4.jar");
		assertTrue("Did not find expected jar commons-io-2.4.jar in /lib", jarFile.exists());
		assertEquals(185140L, jarFile.length());
	}


	private void assertBundledFileContents(String filename, String contents) throws IOException {
		File f1 = new File(bundleTargetDir(), filename);
		assertTrue(f1.exists());
		assertEquals(contents, FileUtils.readFileToString(f1, Charset.defaultCharset()));
	}

	private void execute(File projectDir) throws Exception {
		final File pomFile = new File(projectDir, "pom.xml");
		PrepareBundleMojo mojo = (PrepareBundleMojo) rule.lookupMojo("bundle", pomFile);
		mojo.execute();
	}

	private Properties getBundleMuleDeployProperties() throws IOException {
		File descriptor = new File(bundleTargetDir(), "mule-deploy.properties");
		assertTrue(descriptor.exists());

		FileInputStream in = FileUtils.openInputStream(descriptor);
		Properties deployProps = new Properties();
		deployProps.load(in);
		IOUtils.closeQuietly(in);
		return deployProps;
	}

	private File simpleBundleProjectDir() {
		return new File( basedir,
			"target/test-classes/simple-bundle-project" );
	}

	private File realisticBundleProjectDir() {
		return new File( basedir,
			"target/test-classes/typical-bundle-project" );
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
}
