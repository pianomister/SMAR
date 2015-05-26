package de.dhbw.smar.svg;

import java.io.Serializable;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.caverock.androidsvg.SVGParser;

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

        SVG graphic;
		try {
			// Parse the SVG file from string
			graphic = SVG.getFromString(svg);
			// Get a drawable from the parsed SVG
	        return new PictureDrawable(graphic.renderToPicture());
		} catch (SVGParseException e) {
			e.printStackTrace();
	        return null;
		}
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
		
        SVG graphic;
		try {
			// Parse the SVG file from string
			graphic = SVG.getFromString(svg);
			// Get a drawable from the parsed SVG
	        return new PictureDrawable(graphic.renderToPicture());
		} catch (SVGParseException e) {
			e.printStackTrace();
	        return null;
		}
	}
	
	
}
