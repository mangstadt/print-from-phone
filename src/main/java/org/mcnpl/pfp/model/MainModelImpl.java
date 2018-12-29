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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.activation.DataSource;

import org.mcnpl.pfp.Main;

import jodd.mail.EmailAttachment;
import jodd.mail.MailServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;

/**
 * @author Michael Angstadt
 */
public class MainModelImpl implements MainModel {
	private final MailServer<ReceiveMailSession> server;
	private final String email, disclaimer;
	private final Path downloadDirectory;
	private final CsvFile statistics;
	private final List<Path> downloadedFiles = new ArrayList<>();

	private int emailsDownloaded, attachmentsDownloaded;

	private MainModelImpl(Builder builder) throws IOException {
		server = Objects.requireNonNull(builder.server);
		email = Objects.requireNonNull(builder.email);

		downloadDirectory = Objects.requireNonNull(builder.downloadDirectory);
		if (!Files.isDirectory(downloadDirectory)) {
			Files.createDirectories(downloadDirectory);
		}

		statistics = (builder.statistics == null) ? null : new CsvFile(builder.statistics, "Date", "Computer", "Emails Found", "Attachments Downloaded");

		disclaimer = builder.disclaimer;
	}

	@Override
	public String getDisclaimer() {
		return disclaimer;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public Path getDownloadDirectory() {
		return downloadDirectory;
	}

	@Override
	public void downloadAttachments(String from, DownloadAttachmentsCallback callback) throws IOException {
		ReceivedEmail[] emails;
		try (ReceiveMailSession session = server.createSession()) {
			session.open();
			callback.connected();
			emails = session.receiveEmailAndDelete(new EmailFilterCustom().from(from));
		}

		emailsDownloaded = emails.length;

		attachmentsDownloaded = 0;
		for (ReceivedEmail email : emails) {
			List<EmailAttachment<? extends DataSource>> attachments = email.attachments();

			for (EmailAttachment<? extends DataSource> attachment : attachments) {
				String name = attachment.getName();
				if (name == null || name.trim().isEmpty()) {
					name = "attachment";
				}

				Path file = saveFile(name, attachment.toByteArray());
				downloadedFiles.add(file);
				attachmentsDownloaded++;
			}
		}

		callback.done(emails.length, attachmentsDownloaded);
	}

	@Override
	public void storeStatistics() throws IOException {
		if (statistics == null || attachmentsDownloaded == 0) {
			return;
		}

		//@formatter:off
		statistics.appendRow(
			LocalDateTime.now().format(DateTimeFormatter.ofPattern("M/d/yyyy h:mm a")),
			Main.getComputerName(),
			emailsDownloaded,
			attachmentsDownloaded
		);
		//@formatter:on
	}

	private Path saveFile(String name, byte[] data) throws IOException {
		Path file = downloadDirectory.resolve(name);
		int num = 0;
		while (Files.exists(file)) {
			file = downloadDirectory.resolve(addNumberToFilename(name, ++num));
		}

		Files.write(file, data);
		return file;
	}

	@Override
	public List<Path> deleteDownloadedFiles() {
		downloadedFiles.removeIf(file -> {
			try {
				if (Files.exists(file)) {
					Files.delete(file);
				}
				return true;
			} catch (IOException e) {
				//could not delete
				return false;
			}
		});

		return new ArrayList<>(downloadedFiles);
	}

	@Override
	public List<Path> getDownloadedFiles() {
		/*
		 * Remove files from the list that were manually deleted by the user.
		 */
		downloadedFiles.removeIf(file -> !Files.exists(file));

		return new ArrayList<>(downloadedFiles);
	}

	/**
	 * Appends the given number to the given filename, taking into account the
	 * file's extension.
	 * @param filename the filename (e.g. "attachment.pdf")
	 * @param number the number to append
	 * @return the filename containing the number (e.g. "attachment 4.pdf")
	 */
	private static String addNumberToFilename(String filename, int number) {
		int pos = filename.lastIndexOf('.');

		//@formatter:off
		return (pos < 0) ?
			filename + " " + number :
			filename.substring(0, pos) + " " + number + filename.substring(pos);	
		//@formatter:on
	}

	/**
	 * Creates new instances of {@link MainModelImpl}.
	 * @author Michael Angstadt
	 */
	public static class Builder {
		private MailServer<ReceiveMailSession> server;
		private String email, disclaimer;
		private Path downloadDirectory, statistics;

		/**
		 * Sets the connection to the mail server.
		 * @param server the connection
		 * @return this
		 */
		public Builder server(MailServer<ReceiveMailSession> server) {
			this.server = server;
			return this;
		}

		/**
		 * Sets the actual email address of the email account you are connecting
		 * to.
		 * @param email the email address
		 * @return this
		 */
		public Builder email(String email) {
			this.email = email;
			return this;
		}

		/**
		 * Sets the location where the email attachments will be downloaded to.
		 * @param downloadDirectory the download location
		 * @return this
		 */
		public Builder downloadDirectory(Path downloadDirectory) {
			this.downloadDirectory = downloadDirectory;
			return this;
		}

		/**
		 * Sets the path to the CSV file where download usage statistics are
		 * stored.
		 * @param statistics the CSV file
		 * @return this
		 */
		public Builder statistics(Path statistics) {
			this.statistics = statistics;
			return this;
		}

		/**
		 * Sets the disclaimer text that the patron must agree to before they
		 * can use the app.
		 * @param disclaimer the disclaimer text (in HTML)
		 * @return this
		 */
		public Builder disclaimer(String disclaimer) {
			this.disclaimer = disclaimer;
			return this;
		}

		/**
		 * Builds the model object.
		 * @return this
		 * @throws IOException if there's a problem building the object
		 */
		public MainModelImpl build() throws IOException {
			return new MainModelImpl(this);
		}
	}
}
