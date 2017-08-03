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
				addAnyClasses(appDir.getName(), zipFile);
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

	private void addAnyClasses(String appName, ZipOutputStream zip) throws IOException {
		File packageDir = new File(targetTestClassesDir(), appName);
		if (packageDir.exists() && packageDir.isDirectory()) {
			for (File classFile : packageDir.listFiles()) {
				zip.putNextEntry(
					new ZipEntry("classes/" + appName + "/" + classFile.getName()));
				FileInputStream in = new FileInputStream(classFile);
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
	public void shouldCopyDependenciesToLib() throws Exception {
		execute(realisticBundleProjectDir());

		File libDir = new File(bundleTargetDir(), "lib");
		assertTrue("bundle did not contain expected lib directory", libDir.exists() && libDir.isDirectory());
		final File jarFile = new File(libDir, "commons-io-2.4.jar");
		assertTrue("Did not find expected jar commons-io-2.4.jar in /lib", jarFile.exists());
		assertEquals(185140L, jarFile.length());
	}

	@Test
	public void shouldNotCopyMuleDependenciesToLib() throws Exception {
		execute(realisticBundleProjectDir());

		File libDir = new File(bundleTargetDir(), "lib");
		assertTrue("bundle did not contain expected lib directory", libDir.exists() && libDir.isDirectory());
		final File jarFile = new File(libDir, "mule-core-3.8.4.jar");
		assertFalse("Found unexpected jar mule-core-3.8.4.jar in /lib", jarFile.exists());
	}

	@Test
	public void shouldNotCopyZipDependenciesToLib() throws Exception {
		execute(realisticBundleProjectDir());

		File libDir = new File(bundleTargetDir(), "lib");
		assertTrue("bundle did not contain expected lib directory", libDir.exists() && libDir.isDirectory());
		final File zipFile = new File(libDir, "unwanted-1.0.0.zip");
		assertFalse("Found unexpected zip unwanted-1.0.0.zip in /lib", zipFile.exists());
	}

	@Test
	public void shouldBundleResources() throws Exception {
		execute(realisticBundleProjectDir());

		File sharedBundleConfigFile = new File(bundleTargetDir(), "shared-bundle-config.xml");
		assertTrue("Did not find expected bundle configuration file shared-bundle-config.xml", sharedBundleConfigFile.exists());
		final File bundleSourceDir = new File(realisticBundleProjectDir(), "src/main/bundle");
		assertTrue(FileUtils.contentEquals(
			new File(bundleSourceDir, "shared-bundle-config.xml"),
			sharedBundleConfigFile));
	}

	@Test
	public void shouldBundleClasses() throws Exception {
		execute(realisticBundleProjectDir());

		File bundleClasses = new File(bundleTargetDir(), "classes");
		assertTrue("Did not find expected bundle classes directory", bundleClasses.exists());
		final File app1classFile = new File(bundleClasses, "app1/AppOneValidation.class");
		final File app2classFile = new File(bundleClasses, "app2/AppTwoModelWidget.class");
		assertTrue("Missing expected class file from app1: " + app1classFile.getAbsolutePath(), app1classFile.exists());
		assertTrue("Missing expected class file from app2: " + app2classFile.getAbsolutePath(), app2classFile.exists());
		assertTrue(FileUtils.contentEquals(
			new File(targetTestClassesDir(), "app1/AppOneValidation.class"),
			app1classFile));
		assertTrue(FileUtils.contentEquals(
			new File(targetTestClassesDir(), "app2/AppTwoModelWidget.class"),
			app2classFile));
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
		return new File( targetTestClassesDir(),
			"bundled-apps");
	}

	private File targetTestClassesDir() {
		return new File( basedir,
			"target/test-classes");
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
