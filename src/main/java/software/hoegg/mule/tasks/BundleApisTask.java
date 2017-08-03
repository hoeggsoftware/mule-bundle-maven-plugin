package software.hoegg.mule.tasks;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Component;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import software.hoegg.mule.TransformZipUnArchiver;

import java.io.File;
import java.util.Set;

public class BundleApisTask {
	@Component
	protected TransformZipUnArchiver unArchiver;

	public void bundleApis(Set<Artifact> appArtifacts, File outputDirectory) {
		File apiDir = new File(outputDirectory, "api");
		if (! apiDir.exists()) {
			apiDir.mkdirs();
		}
		unArchiver.setDestDirectory(outputDirectory);
		unArchiver.setTransformer(TransformZipUnArchiver.NO_TRANSFORMER);
		unArchiver.setFileSelectors(new FileSelector[] {
			apiFilesSelector()
		});

		for (Artifact appArtifact : appArtifacts) {
			unArchiver.setSourceFile(appArtifact.getFile());
			unArchiver.extract();
		}
	}

	private IncludeExcludeFileSelector apiFilesSelector() {
		IncludeExcludeFileSelector s = new IncludeExcludeFileSelector();
		s.setIncludes(new String[] {
			"api/**/*"
		});
		return s;
	}
}
