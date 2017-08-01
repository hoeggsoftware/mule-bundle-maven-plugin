package software.hoegg.mule;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
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
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.util.FileUtils;

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

	@Parameter( defaultValue = "${project.build.directory}/mule-bundle", required = true)
	protected File outputDirectory;

	@Parameter( defaultValue = "**/*unbundled.xml")
	protected String configExcludes;

	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().debug("Should bundle " + StringUtils.join(getZipDependencies(), ","));
		outputDirectory.mkdirs();

		final List<String> includedFiles = bundleConfigFiles();
		getLog().info("Bundled mule configuration files: " + StringUtils.join(includedFiles, ","));
		writeMuleDeployProperties(includedFiles);
		bundleLibDependencies();
	}

	private void bundleLibDependencies() throws MojoFailureException {
		File libDir = new File(outputDirectory, "lib");
		if (! libDir.exists()) {
			libDir.mkdirs();
		}
		for (Artifact a : getJarDependencies()) {
			try {
				FileUtils.copyFileToDirectory(a.getFile(), libDir);
			}
			catch (IOException e) {
				throw new MojoFailureException("Unable to copy dependency to bundle lib", e);
			}
		}
	}

	private List<String> bundleConfigFiles() {
		unArchiver.setDestDirectory(outputDirectory);
		unArchiver.setFileSelectors(new FileSelector[] {
			muleConfigSelector()
		});

		final PrefixMuleConfigTransformer transformer = new PrefixMuleConfigTransformer();
		unArchiver.setTransformer(transformer);

		for (Artifact appArtifact : getZipDependencies()) {
			unArchiver.setSourceFile(appArtifact.getFile());
			transformer.setPrefix(appArtifact.getArtifactId() + ".");
			unArchiver.extract();
		}
		return transformer.getIncludedFiles();
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

	private IncludeExcludeFileSelector muleConfigSelector() {
		IncludeExcludeFileSelector s = new IncludeExcludeFileSelector();
		s.setIncludes(new String[] {
			"*.xml"
		});
		if (StringUtils.isNotEmpty(configExcludes)) {
			s.setExcludes(configExcludes.split(","));
		}
		return s;
	}

	private Set<Artifact> getZipDependencies() {
		return CollectionUtils.select(project.getArtifacts(), new Predicate<Artifact>() {
			public boolean evaluate(Artifact a) {
				return "zip".equals(a.getType());
			}
		}, new HashSet<Artifact>());
	}

	private Set<Artifact> getJarDependencies() {
		return CollectionUtils.select(project.getArtifacts(), new Predicate<Artifact>() {
			@Override public boolean evaluate(Artifact a) {
				return "jar".equals(a.getType());
			}
		}, new HashSet<Artifact>());
	}

	public class PrefixMuleConfigTransformer implements TransformZipUnArchiver.Transformer {
			private String prefix;
			private List<String> includedFiles = new ArrayList<String>();

		public File transformDestinationDirectory(TransformZipUnArchiver.EntryContext context) {
			return context.getDestinationDirectory();
		}

		public String transformEntryName(TransformZipUnArchiver.EntryContext context) {
			String name = getPrefix() + context.getEntryName();
			includedFiles.add(name);
			return name;
		}

		public Date transformEntryDate(TransformZipUnArchiver.EntryContext context) {
			return context.getEntryDate();
		}

		public List<String> getIncludedFiles() {
			return Collections.unmodifiableList(includedFiles);
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
	}
}
