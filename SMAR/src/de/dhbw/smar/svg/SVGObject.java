package de.dhbw.smar.svg;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

public class SVGObject implements Serializable {

	private static final long serialVersionUID = 4208154811234L;
	private String svg;
	
	/**
	 * Constructor
	 * @param String svgDocument containing an SVG document
	 */
	public SVGObject(String svgDocument) {
		svg = svgDocument;
	}

	
	public Drawable getDrawable() {

		// Parse the SVG file from string
        SVG graphic = SVGParser.getSVGFromString(svg);
        // Get a drawable from the parsed SVG
        return graphic.createPictureDrawable();
	}
	
	
	/**
	 * Finds the section with given ID inside the SVG
	 * and returns a drawable with that section marked.
	 * 
	 * @param sectionID
	 * @return Drawable with marked section
	 */
	public Drawable getDrawable(int sectionID) {

		// TODO
		
		// Parse the SVG file from string
        SVG graphic = SVGParser.getSVGFromString(svg);
        // Get a drawable from the parsed SVG
        return graphic.createPictureDrawable();
	}
	
	
}
