package software.hoegg.mule;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.mule.tools.maven.plugin.app.Exclusion;
import org.mule.tools.maven.plugin.app.Inclusion;
import software.hoegg.mule.tasks.BundleDirectoryTask;
import software.hoegg.mule.tasks.BundleConfigFilesTask;
import software.hoegg.mule.tasks.BundleLibsTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Creates a single mule application by bundling together the applications in the dependencies
 */
@Mojo(name = "bundle", requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class PrepareBundleMojo extends AbstractMojo {

	@Parameter( defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	@Component
	protected TransformZipUnArchiver unArchiver;

	@Component
	protected BundleConfigFilesTask bundleConfigFilesTask;

	@Component
	protected BundleLibsTask bundleLibsTask;

	@Component(hint = "classes")
	protected BundleDirectoryTask bundleClassesTask;

	@Component(hint = "api")
	protected BundleDirectoryTask bundleApisTask;

	@Parameter( defaultValue = "${project.build.directory}/mule-bundle", required = true)
	protected File outputDirectory;

	@Parameter
	protected List<Inclusion> inclusions;

	@Parameter
	protected List<Exclusion> exclusions;

	@Parameter( defaultValue = "**/*unbundled.xml")
	protected String configExcludes;

	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().debug("Should bundle " + StringUtils.join(getZipDependencies(), ","));
		outputDirectory.mkdirs();

		final List<String> includedFiles = bundleConfigFilesTask.bundleConfigFiles(
			getZipDependencies(),
			outputDirectory,
			configExcludes);
		getLog().info("Bundled mule configuration files: " + StringUtils.join(includedFiles, ","));
		bundleLibsTask.bundleLibDependencies(project, inclusions, exclusions, outputDirectory);
		bundleClassesTask.bundle(getZipDependencies(), outputDirectory);
		bundleApisTask.bundle(getZipDependencies(), outputDirectory);

		List<String> bundleFiles = bundleResources();
		bundleFiles.addAll(includedFiles);
		//get the bundle source files and add the files to mule-deploy properties
		writeMuleDeployProperties(bundleFiles);
	}

	private List<String> bundleResources() throws MojoFailureException {
		try {
			File bundleSourceDirectory = bundleSourceDir();
			FileUtils.copyDirectory(bundleSourceDirectory, outputDirectory);
			return (bundleSourceDirectory.exists()?FileUtils.getFileNames(bundleSourceDirectory, "**/*.xml", "", false): new ArrayList<String>());
		}
		catch (IOException e) {
			throw new MojoFailureException("Unable to copy bundle resources from src/main/bundle", e);
		}
	}

	private File bundleSourceDir() {
		return new File(project.getBasedir(), "src/main/bundle");
	}

	private void writeMuleDeployProperties(List<String> includedFiles) throws MojoFailureException {
		final Properties deployProperties = new Properties();
		deployProperties.setProperty("config.resources", StringUtils.join(includedFiles, ","));
		final File muleDeployPropertiesFile = new File(outputDirectory, "mule-deploy.properties");
		try {
			muleDeployPropertiesFile.createNewFile();
			final FileOutputStream out = new FileOutputStream(muleDeployPropertiesFile);
			deployProperties.store(out, "");
			out.flush();
			IOUtils.closeQuietly(out);
		}
		catch (IOException e) {
			throw new MojoFailureException("Could not create bundle mule-deploy.properties", e);
		}
	}

	private Set<Artifact> getZipDependencies() {
		return CollectionUtils.select(project.getArtifacts(), new Predicate<Artifact>() {
			public boolean evaluate(Artifact a) {
				return "zip".equals(a.getType());
			}
		}, new HashSet<Artifact>());
	}
}
