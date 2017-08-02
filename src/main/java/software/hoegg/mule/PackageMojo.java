package software.hoegg.mule;

import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import java.io.File;
import java.io.IOException;

@Mojo(name = "package", requiresProject = true)
public class PackageMojo extends AbstractMojo {

	@Parameter( defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	@Parameter( defaultValue = "${project.build.directory}", required = true)
	protected File outputDirectory;

	@Parameter( defaultValue = "${project.build.directory}/mule-bundle", required = true)
	protected File bundleDirectory;

	@Component( role = Archiver.class, hint = "zip" )
	private ZipArchiver zipArchiver;

	@Component
	private MavenProjectHelper projectHelper;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		zipArchiver.addFileSet(new DefaultFileSet(bundleDirectory));
		zipArchiver.setDestFile(new File(outputDirectory, project.getBuild().getFinalName() + ".zip"));
		try {
			zipArchiver.createArchive();
		}
		catch (IOException e) {
			throw new MojoFailureException("Failed to create bundle zip archive", e);
		}
		projectHelper.attachArtifact(project, "zip", zipArchiver.getDestFile());
	}
}
