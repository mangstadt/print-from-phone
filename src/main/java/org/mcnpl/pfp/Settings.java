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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mcnpl.common.PropertiesWrapper;

/**
 * Contains the settings that are defined in the "settings.properties" file.
 * @author Michael Angstadt
 */
public class Settings {
	private final String email, emailProtocol, emailServer, emailUsername, emailPassword, disclaimer;
	private final Path attachmentSaveLocation, statisticsFile;

	/**
	 * Loads the settings from a properties file.
	 * @param file the path to the properties file
	 * @throws IOException if there's a problem reading the file
	 */
	public Settings(Path file) throws IOException {
		PropertiesWrapper properties = new PropertiesWrapper(file);

		email = properties.grab("email");
		emailServer = properties.grab("email.server");
		emailProtocol = properties.grab("email.protocol");
		emailUsername = properties.grab("email.username");
		emailPassword = properties.get("email.password"); //use "get" method because password could contain whitespace or be empty
		attachmentSaveLocation = properties.getFile("attachmentSaveLocation");
		statisticsFile = properties.getFile("statistics");

		Path path = properties.getFile("disclaimer");
		disclaimer = (path == null) ? null : new String(Files.readAllBytes(path));
	}

	/**
	 * Gets the names of all required fields that are not defined.
	 * @return the field names
	 */
	public List<String> getUndefinedRequiredFields() {
		Map<String, Object> fields = new LinkedHashMap<>();
		fields.put("email", email);
		fields.put("email.server", emailServer);
		fields.put("email.protocol", emailProtocol);
		fields.put("attachmentSaveLocation", attachmentSaveLocation);

		List<String> undefined = new ArrayList<>();
		fields.forEach((fieldName, value) -> {
			if (value == null) {
				undefined.add(fieldName);
			}
		});

		return undefined;
	}

	/**
	 * Gets the disclaimer text that the user must agree to before using the
	 * app.
	 * @return the disclaimer text (in HTML) or null if not defined
	 */
	public String getDisclaimer() {
		return disclaimer;
	}

	/**
	 * Gets the email address of the email account.
	 * @return the email address
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Gets the protocol to use for connecting to the email server.
	 * @return the protocol
	 */
	public String getEmailProtocol() {
		return emailProtocol;
	}

	/**
	 * Gets the URL to the email server.
	 * @return the URL
	 */
	public String getEmailServer() {
		return emailServer;
	}

	/**
	 * Gets the email account's username (may be the same as the email address).
	 * @return the username
	 */
	public String getEmailUsername() {
		return emailUsername;
	}

	/**
	 * Gets the email account's password.
	 * @return the password
	 */
	public String getEmailPassword() {
		return emailPassword;
	}

	/**
	 * Gets the path to a CSV file were the statistics are stored.
	 * @return the path to the file or null if not specified
	 */
	public Path getStatisticsFile() {
		return statisticsFile;
	}

	/**
	 * Gets the directory where the downloaded attachments should be saved to.
	 * @return the path to the download directory
	 */
	public Path getAttachmentSaveLocation() {
		return attachmentSaveLocation;
	}
}
