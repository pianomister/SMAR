package de.dhbw.smar.svg;

import java.io.File;
import java.io.Serializable;

public class SVGObject implements Serializable {

	private static final long serialVersionUID = 42L;
	private File file;
	
	/**
	 * Constructor
	 * @param f File containing an SVG document
	 */
	public SVGObject(File f) {
		file = f;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
