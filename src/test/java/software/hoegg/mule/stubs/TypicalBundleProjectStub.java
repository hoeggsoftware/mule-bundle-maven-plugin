package software.hoegg.mule.stubs;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;

import java.io.File;
import java.util.*;

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
		artifacts.add(stubArtifact("software.hoegg.mule.test", "unwanted", "1.0.0", "zip"));
		return artifacts;
	}

	private Artifact stubJarArtifact(String groupId, String artifactId, String version) {
		return stubArtifact(groupId, artifactId, version, "jar");
	}

	private Artifact stubArtifact(String groupId, String artifactId, String version, String type) {
		final String filename = artifactId + "-" + version + "." + type;
		final DefaultArtifact a = new DefaultArtifact(groupId, artifactId, version, "compile", type, null, new DefaultArtifactHandler());
		a.setFile(new File(super.getBasedir() + "/src/test/resources/bin/" + filename));
		a.setDependencyTrail(makeTrail(a));
		return a;
	}
}
