package software.hoegg.mule;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TransformZipUnArchiver extends ZipUnArchiver {
	private Transformer transformer = NO_TRANSFORMER;

	@Override
	protected void extractFile(File sourceFile, File destDirectory, InputStream compressedInputStream, String entryName, Date entryDate, boolean isDirectory, Integer mode, String symlinkDestination) throws IOException, ArchiverException {
		final EntryContext context = new EntryContext(sourceFile, destDirectory, entryName, entryDate, isDirectory, mode);
		super.extractFile(
			sourceFile,
			transformer.transformDestinationDirectory(context),
			compressedInputStream,
			transformer.transformEntryName(context),
			transformer.transformEntryDate(context),
			isDirectory,
			mode,
			symlinkDestination);
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public interface Transformer {
		File transformDestinationDirectory(EntryContext context);
		String transformEntryName(EntryContext context);
		Date transformEntryDate(EntryContext context);
	}

	public static Transformer NO_TRANSFORMER = new Transformer() {

		public File transformDestinationDirectory(EntryContext context) {
			return context.getDestinationDirectory();
		}

		public String transformEntryName(EntryContext context) {
			return context.getEntryName();
		}

		public Date transformEntryDate(EntryContext context) {
			return context.getEntryDate();
		}
	};

	public static class EntryContext {
		private final File sourceFile;
		private final File destinationDirectory;
		private final String entryName;
		private final Date entryDate;
		private final boolean isDirectory;
		private final Integer mode;

		public EntryContext(File sourceFile, File destDirectory, String entryName, Date entryDate, boolean isDirectory, Integer mode) {
			this.sourceFile = sourceFile;
			this.destinationDirectory = destDirectory;
			this.entryName = entryName;
			this.entryDate = entryDate;
			this.isDirectory = isDirectory;
			this.mode = mode;
		}

		public File getSourceFile() {
			return sourceFile;
		}

		public File getDestinationDirectory() {
			return destinationDirectory;
		}

		public String getEntryName() {
			return entryName;
		}

		public Date getEntryDate() {
			return entryDate;
		}

		public boolean isDirectory() {
			return isDirectory;
		}

		public Integer getMode() {
			return mode;
		}
	}
}
