package de.dhbw.smar.svg;

import java.io.Serializable;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

public class SVGObject implements Serializable {

	private static final long serialVersionUID = 4208154811234L;
	private String svg;
	private Drawable drawable;
	
	/**
	 * Constructor
	 * @param String svgDocument containing an SVG document
	 */
	public SVGObject(String svgDocument) {
		this.setSVG(svgDocument);
	}

	/**
	 * sets new SVG Document for this object
	 * @param svgDocument
	 */
	public void setSVG(String svgDocument) {
		svg = svgDocument;
		drawable = null;
	}
	
	/**
	 * Generate / return drawable object for displaying in imageView
	 * @return Drawable Drawable for the given SVG
	 */
	public Drawable getDrawable() {

		if(this.drawable == null) {
			try {
				
				// Parse the SVG file from string
				SVG graphic = SVG.getFromString(svg);
				// Get a drawable from the parsed SVG
				this.drawable = new PictureDrawable(graphic.renderToPicture());

			} catch (SVGParseException e) {
				e.printStackTrace();
		        return null;
			}
		}
		return drawable;
	}
	
	
	/**
	 * Finds the section with given ID inside the SVG
	 * and returns a drawable with that section marked.
	 * 
	 * @param sectionID
	 * @return Drawable with marked section
	 */
	public Drawable getDrawable(int sectionID) {

		String section        = "section" + sectionID + "\" class=\"section\"";
		String sectionReplace = "section" + sectionID + "\" class=\"section selected\"";
		String text           = "section" + sectionID + "-text\"";
		String textReplace    = "section" + sectionID + "-text\" class=\"textselected\"";
		String svgMarked = svg;
		svgMarked = svgMarked.replace(section, sectionReplace);
		svgMarked = svgMarked.replace(text, textReplace);
		
		try {
			// Parse the SVG file from string
			SVG graphic = SVG.getFromString(svgMarked);
			// Get a drawable from the parsed SVG
			return new PictureDrawable(graphic.renderToPicture());
		} catch (SVGParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
