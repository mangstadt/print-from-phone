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
import java.nio.file.Path;
import java.util.List;

import jodd.mail.MailException;

/**
 * @author Michael Angstadt
 */
public interface MainModel {
	/**
	 * Gets the disclaimer text that the user must agree to before using the
	 * app.
	 * @return the disclaimer text (in HTML) or null if not defined
	 */
	String getDisclaimer();

	/**
	 * Gets the email address that the user must send their emails to.
	 * @return the email address
	 */
	String getEmail();

	/**
	 * Gets the directory where the email attachments will be saved.
	 * @return the download directory
	 */
	Path getDownloadDirectory();

	/**
	 * Searches for emails from the user and downloads the emails' attachments.
	 * @param fromEmail only download emails that are from this email address
	 * @param callback callback interface for monitoring the status of the
	 * download operation
	 * @throws MailException if there's a problem connecting to the email server
	 * or downloading the emails
	 * @throws IOException if there's a problem or saving the email attachments
	 */
	void downloadAttachments(String fromEmail, DownloadAttachmentsCallback callback) throws IOException;

	/**
	 * Records statistics for the last download operation. Statistics are only
	 * recorded if one or more attachments were downloaded.
	 * @throws IOException if there was a problem saving the statistics
	 */
	void storeStatistics() throws IOException;

	/**
	 * Gets the attachments that the user has downloaded and has not yet deleted
	 * (see {@link #deleteDownloadedFiles}).
	 * @return the downloaded attachments
	 */
	List<Path> getDownloadedFiles();

	/**
	 * Deletes all attachments off of the local computer that the user has
	 * downloaded.
	 * @return the files that COULD NOT be deleted (for example, if a file is
	 * open, then the system may not be able to delete it). If a downloaded file
	 * does not exist (i.e. if the user deleted the file themselves before this
	 * method was called), then the file will be skipped and will not be
	 * included in the returned list.
	 */
	List<Path> deleteDownloadedFiles();

	/**
	 * Callback interface for the {@link #downloadAttachments} method.
	 * @author Michael Angstadt
	 */
	public interface DownloadAttachmentsCallback {
		/**
		 * Called after the connection with the email server has been
		 * established.
		 */
		void connected();

		/**
		 * Called after all attachments have been downloaded.
		 * @param emails the number of emails that were found
		 * @param attachments the total number of attachments in those emails
		 */
		void done(int emails, int attachments);
	}
}
