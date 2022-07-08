package multipacks.sampleplugin;

import multipacks.plugins.MultipacksPlugin;

public class SamplePlugin implements MultipacksPlugin {
	@Override
	public void onLoad() {
		System.out.println("Hello world!");
	}
}
