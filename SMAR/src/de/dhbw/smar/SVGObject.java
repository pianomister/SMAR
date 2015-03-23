package de.dhbw.smar;

import java.io.File;
import java.io.Serializable;

public class SVGObject implements Serializable {

	private File file;
	
	public SVGObject(File f) {
		file = f;
	}
}
