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

package org.mcnpl.pfp.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Appends data to a CSV file. Only opens the file when a row needs to be
 * appended.
 * @author Michael Angstadt
 */
public class CsvFile {
	private final Path path;

	/**
	 * @param path the path to the file (file will be created if it doesn't
	 * exist)
	 * @param columnHeadings the column headings in the file
	 * @throws IOException if the file doesn't exist and there's a problem
	 * creating it
	 */
	public CsvFile(Path path, Object... columnHeadings) throws IOException {
		this.path = path;
		if (!Files.exists(path) && columnHeadings.length > 0) {
			appendRow(columnHeadings);
		}
	}

	/**
	 * Appends a row to the file.
	 * @param values the row values (each object's <code>toString</code> method
	 * will be called; if an object is null then "null" will be printed)
	 * @throws IOException if there's a problem writing to the file
	 */
	public void appendRow(Object... values) throws IOException {
		StringBuilder line = new StringBuilder(64);
		boolean first = true;
		for (Object value : values) {
			if (!first) {
				line.append(',');
			} else {
				first = false;
			}

			String valueStr = Objects.toString(value);
			if (valueStr.contains("\"") || valueStr.contains(",")) {
				line.append('"');
				line.append(valueStr.replaceAll("\"", "\"\""));
				line.append('"');
			} else {
				line.append(valueStr);
			}
		}

		List<String> lines = Arrays.asList(line.toString());
		Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}
}
