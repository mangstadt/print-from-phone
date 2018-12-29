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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mcnpl.pfp.model.CsvFile;

/**
 * @author Michael Angstadt
 */
public class CsvFileTest {
	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	/**
	 * Create the CSV file if it doesn't exist.
	 */
	@Test
	public void constructor_new_file() throws Exception {
		Path path = folder.newFile().toPath();
		Files.delete(path);
		new CsvFile(path, "one,two", "three\"four", "five");

		List<String> lines = Files.readAllLines(path);
		assertEquals(Arrays.asList("\"one,two\",\"three\"\"four\",five"), lines);
	}

	/**
	 * Do not create the file if no headers were given.
	 */
	@Test
	public void constructor_new_file_no_headers() throws Exception {
		Path path = folder.newFile().toPath();
		Files.delete(path);
		new CsvFile(path);

		assertFalse(Files.exists(path));
	}

	/**
	 * Do not append the column headers if the file already exists.
	 */
	@Test
	public void constructor_existing_file() throws Exception {
		Path path = folder.newFile().toPath();
		new CsvFile(path, "one,two", "three\"four", "five");

		assertEquals(0, Files.size(path));
	}

	/**
	 * Escape special characters in values.
	 */
	@Test
	public void appendRow_escape_special_characters() throws Exception {
		Path path = folder.newFile().toPath();
		CsvFile csv = new CsvFile(path);
		csv.appendRow("one,two", "three\"four", "five");

		List<String> lines = Files.readAllLines(path);
		assertEquals(Arrays.asList("\"one,two\",\"three\"\"four\",five"), lines);
	}

	/**
	 * Null values are acceptable.
	 */
	@Test
	public void appendRow_null() throws Exception {
		Path path = folder.newFile().toPath();
		CsvFile csv = new CsvFile(path);
		csv.appendRow((String) null);

		List<String> lines = Files.readAllLines(path);
		assertEquals(Arrays.asList("null"), lines);
	}
}
