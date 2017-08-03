package software.hoegg.mule.tasks;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Component;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import software.hoegg.mule.TransformZipUnArchiver;

import java.io.File;
import java.util.*;

public class BundleConfigFilesTask {
	@Component
	protected TransformZipUnArchiver unArchiver;

	public List<String> bundleConfigFiles(Set<Artifact> appArtifacts, File outputDirectory, String excludes) {
		unArchiver.setDestDirectory(outputDirectory);
		unArchiver.setFileSelectors(new FileSelector[] {
			muleConfigSelector(excludes)
		});

		final PrefixMuleConfigTransformer transformer = new PrefixMuleConfigTransformer();
		unArchiver.setTransformer(transformer);

		for (Artifact appArtifact : appArtifacts) {
			unArchiver.setSourceFile(appArtifact.getFile());
			transformer.setPrefix(appArtifact.getArtifactId() + ".");
			unArchiver.extract();
		}
		return transformer.getIncludedFiles();
	}

	private IncludeExcludeFileSelector muleConfigSelector(String excludes) {
		IncludeExcludeFileSelector s = new IncludeExcludeFileSelector();
		s.setIncludes(new String[] {
			"*.xml"
		});
		if (StringUtils.isNotEmpty(excludes)) {
			s.setExcludes(excludes.split(","));
		}
		return s;
	}

	public class PrefixMuleConfigTransformer implements TransformZipUnArchiver.Transformer {
		private String prefix;
		private List<String> includedFiles = new ArrayList<String>();

		public File transformDestinationDirectory(TransformZipUnArchiver.EntryContext context) {
			return context.getDestinationDirectory();
		}

		public String transformEntryName(TransformZipUnArchiver.EntryContext context) {
			String name = getPrefix() + context.getEntryName();
			includedFiles.add(name);
			return name;
		}

		public Date transformEntryDate(TransformZipUnArchiver.EntryContext context) {
			return context.getEntryDate();
		}

		public List<String> getIncludedFiles() {
			return Collections.unmodifiableList(includedFiles);
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
	}
}
