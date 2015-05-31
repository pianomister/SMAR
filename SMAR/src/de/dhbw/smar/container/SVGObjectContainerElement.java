package de.dhbw.smar.container;

import java.io.Serializable;

public class SVGObjectContainerElement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8620122644404652418L;
	
	private String svgObjectPath;
	private int shelfID;
	
	public SVGObjectContainerElement(String svgObjectPath, int shelfID) {
		this.svgObjectPath = svgObjectPath;
		this.shelfID = shelfID;
	}
	
	public String getSVGPath() {
		return this.svgObjectPath;
	}
	
	public int getShelfID() {
		return this.shelfID;
	}
}
