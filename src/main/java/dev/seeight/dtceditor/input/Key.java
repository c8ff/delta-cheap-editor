package dev.seeight.dtceditor.input;

public class Key {
	private final int[] codes;
	boolean down;

	public Key(int... codes) {
		if (codes.length == 0) {
			throw new IllegalArgumentException();
		}

		this.codes = codes;
	}

	public boolean isDown() {
		return down;
	}

	public boolean isCode(int code) {
		if (codes.length == 1) {
			return codes[0] == code;
		}

		for (int i : codes) {
			if (i == code) {
				return true;
			}
		}

		return false;
	}
}
