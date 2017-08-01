package software.hoegg.mule.stubs;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class AbstractBundleProjectStub extends MavenProjectStub {
	protected Artifact stubApplicationArtifact(String name) {
		DefaultArtifact a = new DefaultArtifact("software.hoegg.mule.test", name, "1.0.0", "compile", "zip", "", null);
		a.setFile(new File(pluginTestDir(), name + ".zip"));
		a.setDependencyTrail(makeTrail(a));
		return a;
	}

	protected File pluginTestDir() {
		return new File( super.getBasedir(),
			"target/test-bundle-resources");
	}

	protected List<String> makeTrail(Artifact a) {
		final List<String> trail = new LinkedList<String>();
		trail.add(a.getId());
		trail.add(a.getDependencyConflictId() + ":" + a.getVersion());
		return trail;
	}
}
