package software.hoegg.mule.stubs;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.io.File;

public class AbstractBundleProjectStub extends MavenProjectStub {
	protected Artifact stubApplicationArtifact(String name) {
		ArtifactStub a = new ArtifactStub();
		a.setArtifactId(name);
		a.setType("zip");
		a.setFile(new File(pluginTestDir(), name + ".zip"));
		return a;
	}

	protected File pluginTestDir() {
		return new File( super.getBasedir(),
			"target/test-bundle-resources");
	}
}
