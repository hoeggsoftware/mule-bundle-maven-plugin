package software.hoegg.mule.tasks;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Component;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import software.hoegg.mule.TransformZipUnArchiver;

import java.io.File;
import java.util.Set;

public class BundleClassesTask {
	@Component
	protected TransformZipUnArchiver unArchiver;

	public void bundleClasses(Set<Artifact> appArtifacts, File outputDirectory) {
		File classesDir = new File(outputDirectory, "classes");
		if (! classesDir.exists()) {
			classesDir.mkdirs();
		}
		unArchiver.setDestDirectory(outputDirectory);
		unArchiver.setTransformer(TransformZipUnArchiver.NO_TRANSFORMER);
		unArchiver.setFileSelectors(new FileSelector[] {
			classesSelector()
		});

		for (Artifact appArtifact : appArtifacts) {
			unArchiver.setSourceFile(appArtifact.getFile());
			unArchiver.extract();
		}
	}

	private IncludeExcludeFileSelector classesSelector() {
		IncludeExcludeFileSelector s = new IncludeExcludeFileSelector();
		s.setIncludes(new String[] {
			"classes/**/*"
		});
		return s;
	}
}
