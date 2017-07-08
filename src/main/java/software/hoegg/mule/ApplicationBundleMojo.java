package software.hoegg.mule;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a single mule application by bundling together the applications in the dependencies
 */
@Mojo(name = "create", requiresDependencyResolution = ResolutionScope.COMPILE)
public class ApplicationBundleMojo extends AbstractMojo {

	@Component
	protected MavenProject mavenProject;

	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info("Should bundle " + StringUtils.join(getZipDependencies(), ","));
	}

	private Set<Artifact> getZipDependencies() {
		return CollectionUtils.select(mavenProject.getArtifacts(), new Predicate<Artifact>() {
			public boolean evaluate(Artifact a) {
				return "zip".equals(a.getType());
			}
		}, new HashSet<Artifact>());
	}
}
