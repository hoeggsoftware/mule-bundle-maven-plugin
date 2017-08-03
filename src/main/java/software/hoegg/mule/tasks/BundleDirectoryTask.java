package software.hoegg.mule.tasks;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Component;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import software.hoegg.mule.TransformZipUnArchiver;

import java.io.File;
import java.util.Set;

public class BundleDirectoryTask {

	@Component
	protected TransformZipUnArchiver unArchiver;

	private String path;
	private String includePattern;

	public void bundle(Set<Artifact> appArtifacts, File outputDirectory) {
		File dir = new File(outputDirectory, path);
		if (! dir.exists()) {
			dir.mkdirs();
		}
		unArchiver.setDestDirectory(outputDirectory);
		unArchiver.setTransformer(TransformZipUnArchiver.NO_TRANSFORMER);
		unArchiver.setFileSelectors(new FileSelector[] {
			selector()
		});

		for (Artifact appArtifact : appArtifacts) {
			unArchiver.setSourceFile(appArtifact.getFile());
			unArchiver.extract();
		}
	}

	private IncludeExcludeFileSelector selector() {
		IncludeExcludeFileSelector s = new IncludeExcludeFileSelector();
		s.setIncludes(new String[] {
			includePattern
		});
		return s;
	}
}
