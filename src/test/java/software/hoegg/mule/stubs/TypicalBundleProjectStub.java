package software.hoegg.mule.stubs;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TypicalBundleProjectStub extends AbstractBundleProjectStub {
	public TypicalBundleProjectStub() {
		readModel(new File(getBasedir(), "pom.xml"));
	}

	@Override
	public File getBasedir()
	{
		return new File( super.getBasedir() + "/src/test/resources/typical-bundle-project" );
	}

	@Override
	public Set<Artifact> getArtifacts() {
		Set<Artifact> artifacts = new HashSet<>();
		artifacts.add(stubApplicationArtifact("app1"));
		artifacts.add(stubApplicationArtifact("app2"));
		artifacts.add(stubJarArtifact("org.mule", "mule-core", "3.8.4"));
		artifacts.add(stubJarArtifact("commons-io", "commons-io", "2.4"));
		return artifacts;
	}

	private Artifact stubJarArtifact(String groupId, String artifactId, String version) {
		final String jar = artifactId + "-" + version + ".jar";
		final DefaultArtifact a = new DefaultArtifact(groupId, artifactId, version, "compile", "jar", null, new DefaultArtifactHandler());
		a.setFile(new File(super.getBasedir() + "/src/test/resources/bin/" + jar));
		return a;
	}
}
