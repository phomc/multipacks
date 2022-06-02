package multipacks.transforms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TFSListing {
	public final String path;
	public final File actualFile;
	public final byte[] transformed;

	public TFSListing(String path, File actualFile) {
		this.path = path;
		this.actualFile = actualFile;
		this.transformed = null;
	}

	public TFSListing(String path, byte[] val) {
		this.path = path;
		this.actualFile = null;
		this.transformed = val;
	}

	public byte[] getAsBytes() throws IOException {
		if (transformed != null) return transformed;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = new FileInputStream(actualFile);
		in.transferTo(out);
		in.close();
		return out.toByteArray();
	}
}
