/*
 * Print From Phone
 * Copyright (C) 2018-present Michael Angstadt
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mcnpl.pfp.image;

import javax.swing.ImageIcon;

/**
 * Contains all of the images used in the app.
 * @author Michael Angstadt
 */
public final class Images extends org.mcnpl.common.gui.Images {
	public static final ImageIcon APP_ICON = load("app-icon.png");
	public static final ImageIcon BACK = load("back.png");
	public static final ImageIcon CHECKMARK = load("checkmark.png");
	public static final ImageIcon DELETE = load("delete.png");
	public static final ImageIcon EMPTY = load("empty.png");
	public static final ImageIcon ERROR = load("error.png");
	public static final ImageIcon EXIT = load("exit.png");
	public static final ImageIcon FORWARD = load("forward.png");
	public static final ImageIcon LOADING = load("loading.gif");
	public static final ImageIcon RELOAD = load("reload.png");
	public static final ImageIcon SHOW_FILES = load("show-files.png");

	/**
	 * Loads an image from the classpath.
	 * @param fileName the name of the image file
	 * @return the image
	 */
	private static ImageIcon load(String fileName) {
		return new ImageIcon(Images.class.getResource(fileName));
	}

	private Images() {
		//hide
	}
}
