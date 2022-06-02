package multipacks.packs;

import java.io.File;
import java.io.IOException;

import multipacks.utils.IOUtils;

public class Pack {
	private File packRoot;
	private PackIndex index;
	private PackIdentifier identifier;

	public Pack(File packRoot) throws IOException {
		this.packRoot = packRoot;
		this.index = PackIndex.fromJson(IOUtils.jsonFromFile(new File(packRoot, "multipacks.json")).getAsJsonObject());
		this.identifier = index.getIdentifier();
	}

	public PackIndex getIndex() {
		return index;
	}

	public File getRoot() {
		return packRoot;
	}

	public File getAssetsRoot() {
		File assets = new File(packRoot, "assets");
		return assets.exists()? assets : null;
	}

	public File getDataRoot() {
		File data = new File(packRoot, "data");
		return data.exists()? data : null;
	}

	public PackIdentifier getIdentifier() {
		return identifier;
	}
}
