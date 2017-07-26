package software.hoegg.mule.stubs;

import org.apache.maven.artifact.Artifact;

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
		return artifacts;
	}
}
