package de.dhbw.smar.container;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * Class holding paths to all SVGObjects including shelf id and lastupdate timestamp.
 * 
 * @author Sebastian Kowalski
 *
 */
public class SVGObjectContainer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6714681817362708956L;
	
	private long lastDownload = 0;
	private ArrayList<SVGObjectContainerElement> svgObjects = new ArrayList<SVGObjectContainerElement>();
	
	public SVGObjectContainer() {
		this.lastDownload = 0;
	}
	
	public SVGObjectContainer(long lastDownload) {
		this.lastDownload = lastDownload;
	}
	
	public ArrayList<SVGObjectContainerElement> getSVGObjects() {
		return svgObjects;
	}
	
	public boolean issetSVGObject(int shelfID) {
		for(SVGObjectContainerElement svgoe: svgObjects) {
			if(svgoe.getShelfID() == shelfID) {
				return true;
			}
		}
		return false;
	}
	
	public SVGObjectContainerElement getSVGObject(int shelfID) {
		for(SVGObjectContainerElement svgoe: svgObjects) {
			if(svgoe.getShelfID() == shelfID) {
				return svgoe;
			}
		}
		return null;
	}
	
	public String getSVGObjectPath(int shelfID) {
		SVGObjectContainerElement svgoe = getSVGObject(shelfID);
		if(svgoe == null)
			return "";
		else
			return svgoe.getSVGPath();
	}
	
	public void addSVGObject(SVGObjectContainerElement svgoe) {
		svgObjects.add(svgoe);
	}
	
	public void addSVGObject(String svgObjectPath, int shelfID) {
		svgObjects.add(new SVGObjectContainerElement(svgObjectPath, shelfID));
	}
	
	public long getLastDownload() {
		return lastDownload;
	}

	public void setLastDownload(long lastDownload) {
		this.lastDownload = lastDownload;
	}

}
