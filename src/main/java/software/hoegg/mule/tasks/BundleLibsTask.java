package software.hoegg.mule.tasks;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.mule.tools.maven.plugin.app.ArtifactFilter;
import org.mule.tools.maven.plugin.app.Exclusion;
import org.mule.tools.maven.plugin.app.Inclusion;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BundleLibsTask {

	public void bundleLibDependencies(MavenProject project, List<Inclusion> inclusions, List<Exclusion> exclusions, File outputDirectory) throws MojoFailureException {
		File libDir = new File(outputDirectory, "lib");
		if (! libDir.exists()) {
			libDir.mkdirs();
		}
		for (Artifact a : getLibDependencies(project, inclusions, exclusions)) {
			try {
				FileUtils.copyFileToDirectory(a.getFile(), libDir);
			}
			catch (IOException e) {
				throw new MojoFailureException("Unable to copy dependency to bundle lib", e);
			}
		}
	}

	private Set<Artifact> getLibDependencies(MavenProject project, List<Inclusion> inclusions, List<Exclusion> exclusions) {
		ArtifactFilter filter = new ArtifactFilter(project, inclusions, exclusions, true);
		return keepOnlyJarArtifacts(filter.getArtifactsToArchive());
	}

	private Set<Artifact> keepOnlyJarArtifacts(Set<Artifact> artifactsToArchive) {
		return CollectionUtils.select(artifactsToArchive, new Predicate<Artifact>() {
			@Override public boolean evaluate(Artifact a) {
				return "jar".equals(a.getType());
			}
		}, new HashSet<Artifact>());
	}
}
