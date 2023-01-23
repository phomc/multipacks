package multipacks.cli.api;

public class Parameters {
	public final String[] params;
	private int index = 0;

	public Parameters(String[] params) {
		this.params = new String[params.length];
		System.arraycopy(params, 0, this.params, 0, params.length);
	}

	public boolean endOfParams() {
		return index >= params.length;
	}

	public String getCurrent() {
		if (endOfParams()) return null;
		return params[index];
	}

	public String getThenAdvance() {
		if (endOfParams()) return null;
		return params[index++];
	}
}
