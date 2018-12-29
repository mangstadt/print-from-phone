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

package org.mcnpl.pfp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mcnpl.pfp.Settings;

/**
 * @author Michael Angstadt
 */
public class SettingsTest {
	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void all_properties_missing() throws Exception {
		Path path = folder.newFile().toPath();
		Settings settings = new Settings(path);

		assertNull(settings.getDisclaimer());
		assertNull(settings.getEmail());
		assertNull(settings.getEmailProtocol());
		assertNull(settings.getEmailServer());
		assertNull(settings.getEmailUsername());
		assertNull(settings.getEmailPassword());
		assertNull(settings.getStatisticsFile());
		assertNull(settings.getAttachmentSaveLocation());
	}

	@Test
	public void getUndefinedRequiredFields() throws Exception {
		Path path = folder.newFile().toPath();
		Files.write(path, Arrays.asList("email=foo@example.com"));
		Settings settings = new Settings(path);

		assertEquals(Arrays.asList("email.server", "email.protocol", "attachmentSaveLocation"), settings.getUndefinedRequiredFields());
	}
}
