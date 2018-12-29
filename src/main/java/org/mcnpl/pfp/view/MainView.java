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

package org.mcnpl.pfp.view;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Michael Angstadt
 */
public interface MainView {
	/**
	 * Gets the user's email address.
	 * @return the email address
	 */
	String getPatronEmail();

	/**
	 * Sets the email address that the user must send their email to.
	 * @param email the email address
	 */
	void setLibraryEmail(String email);

	/**
	 * Sets the disclaimer text to show when the app first opens.
	 * @param disclaimer the disclaimer text (in HTML)
	 */
	void setDisclaimer(String disclaimer);

	/**
	 * Sets the directory where the attachments will be saved to.
	 * @param directory the directory
	 */
	void setDownloadDirectory(Path directory);

	/**
	 * Update the view to show that the app is connecting to the email server.
	 */
	void setStatusConnecting();

	/**
	 * Update the view to show that the app is searching for emails and
	 * downloading attachments.
	 */
	void setStatusDownloading();

	/**
	 * Update the view to show that the app is done downloading the attachments.
	 * @param emails the number of emails found
	 * @param attachments the total number of attachments in those emails
	 */
	void setStatusDownloaded(int emails, int attachments);

	/**
	 * Update the view to show an error that occurred during the
	 * connection/download process.
	 * @param thrown the error that was thrown
	 */
	void setStatusError(Throwable thrown);

	/**
	 * Specify the action to take when the user is ready to connect to the email
	 * server and download attachments.
	 * @param runnable the action
	 */
	void onCheckForEmails(Runnable runnable);

	/**
	 * Specify the action to take when the user wants to delete the attachments
	 * they've downloaded off of the local computer.
	 * @param runnable the action
	 */
	void onDeleteFiles(Runnable runnable);

	/**
	 * Specify the action to take when the user closes the program.
	 * @param runnable the action
	 */
	void onClose(Runnable runnable);

	/**
	 * Shows the user's downloaded attachments.
	 */
	void openDownloadLocation();

	/**
	 * Shows a message in response to the user requesting to delete their files,
	 * saying that the user's attachments were successfully deleted off of the
	 * local computer.
	 * @param deleted the number of files that were deleted
	 */
	void showFilesDeletedMessage(int deleted);

	/**
	 * Shows a message in response to the user requesting to delete their files,
	 * saying that some files could not be deleted.
	 * @param deleted the number of files that could be deleted
	 * @param notDeleted the files that could not be deleted
	 * @return true if the user wants to try deleting the files again, false if
	 * not
	 */
	boolean showFilesNotDeletedPrompt(int deleted, List<Path> notDeleted);

	/**
	 * Shows a message asking the user if they want to delete their downloaded
	 * files before exiting the program.
	 * @param files the files they've downloaded
	 * @return true if the user wants to delete them, false if they don't, null
	 * to cancel the close operation
	 */
	Boolean showDeleteFilesPrompt(List<Path> files);

	/**
	 * Displays the view on the screen. This method should be called after the
	 * view object has been initialized and it's ready to be displayed to the
	 * user. It should only be called once.
	 */
	void display();

	/**
	 * Closes the view.
	 */
	void close();

	/**
	 * Runs the given piece of code from within the GUI's event dispatch thread.
	 * This method should be used when updating the GUI from another thread.
	 * @param runnable the code to run
	 */
	void invoke(Runnable runnable);
}
