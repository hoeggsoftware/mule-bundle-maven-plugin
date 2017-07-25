package software.hoegg.mule.stubs;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SimpleBundleProjectStub extends MavenProjectStub {

	public SimpleBundleProjectStub() {
		readModel(new File(getBasedir(), "pom.xml"));
	}

	@Override
	public File getBasedir()
	{
		return new File( super.getBasedir() + "/src/test/resources/simple-bundle-project" );
	}

	@Override
	public Set<Artifact> getArtifacts() {
		Set<Artifact> artifacts = new HashSet<Artifact>();
		artifacts.add(stubApplicationArtifact("test-app1"));
		artifacts.add(stubApplicationArtifact("test-app2"));
		return artifacts;
	}

	private Artifact stubApplicationArtifact(String name) {
		ArtifactStub a = new ArtifactStub();
		a.setArtifactId(name);
		a.setType("zip");
		a.setFile(new File(pluginTestDir(), name + ".zip"));
		return a;
	}

	private File pluginTestDir() {
		return new File( super.getBasedir(),
			"target/test-bundle-resources");
	}
}
