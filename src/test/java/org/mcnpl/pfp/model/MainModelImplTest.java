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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mcnpl.pfp.Main;

import jodd.mail.EmailAttachment;
import jodd.mail.EmailFilter;
import jodd.mail.MailServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;

/**
 * @author Michael Angstadt
 */
public class MainModelImplTest {
	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void constructor_create_download_directory() throws Exception {
		Path downloadDirectory = tempFolder.newFolder().toPath();
		Files.delete(downloadDirectory);

		MailServer<ReceiveMailSession> server = mockMailServer();

		//@formatter:off
		new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(downloadDirectory)
		.build();
		//@formatter:on

		assertTrue(Files.isDirectory(downloadDirectory));
	}

	@Test
	public void constructor_create_statistics_file() throws Exception {
		Path stats = tempFolder.newFile().toPath();
		Files.delete(stats);

		MailServer<ReceiveMailSession> server = mockMailServer();

		//@formatter:off
		new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
			.statistics(stats)
		.build();
		//@formatter:on

		assertTrue(Files.exists(stats));
	}

	@Test
	public void downloadAttachments() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment("file1.txt", "one"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		MainModel.DownloadAttachmentsCallback callback = mock(MainModel.DownloadAttachmentsCallback.class);
		model.downloadAttachments("from@email.com", callback);

		verify(callback).connected();
		verify(callback).done(1, 1);

		assertDownloadedFiles("from@email.com.txt", "file1.txt");
	}

	/**
	 * When an attachment doesn't have a name.
	 */
	@Test
	public void downloadAttachments_no_name() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment(null, "one"))
				.attachment(attachment("", "two"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		MainModel.DownloadAttachmentsCallback callback = mock(MainModel.DownloadAttachmentsCallback.class);
		model.downloadAttachments("from@email.com", callback);

		verify(callback).connected();
		verify(callback).done(1, 2);

		assertDownloadedFiles("from@email.com.txt", "attachment", "attachment-1");
	}

	/**
	 * When multiple attachments have the same name.
	 */
	@Test
	public void downloadAttachments_same_name() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment("file.txt", "one"))
				.attachment(attachment("file.txt", "two"))
				.attachment(attachment("file.txt", "three"))
				.attachment(attachment("file", "four"))
				.attachment(attachment("file", "five"))
				.attachment(attachment("file", "six"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		MainModel.DownloadAttachmentsCallback callback = mock(MainModel.DownloadAttachmentsCallback.class);
		model.downloadAttachments("from@email.com", callback);

		verify(callback).connected();
		verify(callback).done(1, 6);

		assertDownloadedFiles("from@email.com.txt", "file.txt", "file-1.txt", "file-2.txt", "file", "file-1", "file-2");
	}

	/**
	 * When an email doesn't have any attachments.
	 */
	@Test
	public void downloadAttachments_email_without_attachments() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create().message("body", "text/plain"),
			ReceivedEmail.create().message("body", "text/plain")
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		MainModel.DownloadAttachmentsCallback callback = mock(MainModel.DownloadAttachmentsCallback.class);
		model.downloadAttachments("from@email.com", callback);

		verify(callback).connected();
		verify(callback).done(2, 0);

		assertDownloadedFiles("from@email.com.txt", "from@email.com-1.txt");
	}

	/**
	 * When no emails are downloaded.
	 */
	@Test
	public void downloadAttachments_no_emails() throws Exception {
		MailServer<ReceiveMailSession> server = mockMailServer();

		//@formatter:off
		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		MainModel.DownloadAttachmentsCallback callback = mock(MainModel.DownloadAttachmentsCallback.class);
		model.downloadAttachments("from@email.com", callback);

		verify(callback).connected();
		verify(callback).done(0, 0);

		assertDownloadedFiles();
	}

	@Test
	public void email_body_only_save_html_message() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.message("body", "text/rtf")
				.message("<html><body><b>Body</b></body></html>", "text/html")
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		MainModel.DownloadAttachmentsCallback callback = mock(MainModel.DownloadAttachmentsCallback.class);
		model.downloadAttachments("from@email.com", callback);

		verify(callback).connected();
		verify(callback).done(1, 0);

		assertDownloadedFiles("from@email.com.html");
	}

	@Test
	public void email_body_no_html_message() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.message("body", "text/rtf")
				.message("body", "unrecognized/mimeType")
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		MainModel.DownloadAttachmentsCallback callback = mock(MainModel.DownloadAttachmentsCallback.class);
		model.downloadAttachments("from@email.com", callback);

		verify(callback).connected();
		verify(callback).done(1, 0);

		assertDownloadedFiles("from@email.com.txt", "from@email.com.rtf", "from@email.com-1.txt");
	}

	@Test
	public void storeStatistics() throws Exception {
		Path stats = tempFolder.newFile().toPath();

		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment("file1.txt", "one"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
			.statistics(stats)
		.build();
		//@formatter:on

		model.downloadAttachments("from@email.com", mock(MainModel.DownloadAttachmentsCallback.class));
		model.storeStatistics();

		List<String> lines = Files.readAllLines(stats); //note: column headers are not created because the file already exists
		assertEquals(1, lines.size());
		assertTrue(lines.get(0).matches("\\d+/\\d+/\\d+ \\d+:\\d+ (AM|PM)," + Pattern.quote(Main.getComputerName()) + ",1,1"));
	}

	/*
	 * No statistics should be recorded when no emails are downloaded.
	 */
	@Test
	public void storeStatistics_no_emails() throws Exception {
		Path stats = tempFolder.newFile().toPath();

		MailServer<ReceiveMailSession> server = mockMailServer();

		//@formatter:off
		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
			.statistics(stats)
		.build();
		//@formatter:on

		model.downloadAttachments("from@email.com", mock(MainModel.DownloadAttachmentsCallback.class));
		model.storeStatistics();

		List<String> lines = Files.readAllLines(stats); //note: column headers are not created because the file already exists
		assertEquals(0, lines.size());
	}

	/*
	 * No statistics should be recorded when no attachments are downloaded.
	 */
	@Test
	public void storeStatistics_no_attachments() throws Exception {
		Path stats = tempFolder.newFile().toPath();

		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create().message("body", "text/plain")
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
			.statistics(stats)
		.build();
		//@formatter:on

		model.downloadAttachments("from@email.com", mock(MainModel.DownloadAttachmentsCallback.class));
		model.storeStatistics();

		List<String> lines = Files.readAllLines(stats); //note: column headers are not created because the file already exists
		assertEquals(0, lines.size());
	}

	@Test
	public void deleteDownloadedFiles() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment("file1.txt", "one"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		model.downloadAttachments("from@email.com", mock(MainModel.DownloadAttachmentsCallback.class));
		assertTrue(model.deleteDownloadedFiles().isEmpty());
		assertDownloadedFiles();
	}

	@Test
	public void deleteDownloadedFiles_file_cannot_be_deleted() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment("file1.txt", "one"))
				.attachment(attachment("file2.txt", "one"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		model.downloadAttachments("from@email.com", mock(MainModel.DownloadAttachmentsCallback.class));

		/*
		 * Hold a lock onto one of the files so it can't be deleted.
		 * 
		 * Source: https://gist.github.com/bmchae/1344404
		 */
		RandomAccessFile file = new RandomAccessFile(new File(tempFolder.getRoot(), "file1.txt"), "rw");
		FileLock lock = file.getChannel().tryLock();

		try {
			assertEquals(Arrays.asList(tempFolder.getRoot().toPath().resolve("file1.txt")), model.deleteDownloadedFiles());
			assertDownloadedFiles("file1.txt");
		} finally {
			lock.release();
			file.close();
		}
	}

	/**
	 * It should not freak out if a file doesn't exist due to it being manually
	 * deleted by the user.
	 */
	@Test
	public void deleteDownloadedFiles_user_manually_deletes_file() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment("file1.txt", "one"))
				.attachment(attachment("file2.txt", "one"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		model.downloadAttachments("from@email.com", mock(MainModel.DownloadAttachmentsCallback.class));
		Files.delete(tempFolder.getRoot().toPath().resolve("file1.txt"));
		assertTrue(model.deleteDownloadedFiles().isEmpty());
		assertDownloadedFiles();
	}

	@Test
	public void getDownloadedFiles() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment("file1.txt", "one"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		model.downloadAttachments("from@email.com", mock(MainModel.DownloadAttachmentsCallback.class));

		assertGetDownloadedFilesMethod(model, "file1.txt", "from@email.com.txt");
	}

	/**
	 * If the user manually deletes a file, that file should be removed from the
	 * "downloaded files" list.
	 */
	@Test
	public void getDownloadedFiles_user_manaully_deleted() throws Exception {
		//@formatter:off
		MailServer<ReceiveMailSession> server = mockMailServer(
			"from@email.com",
			ReceivedEmail.create()
				.message("body", "text/plain")
				.attachment(attachment("file1.txt", "one"))
		);

		MainModelImpl model = new MainModelImpl.Builder()
			.server(server)
			.email("email")
			.downloadDirectory(tempFolder.getRoot().toPath())
		.build();
		//@formatter:on

		model.downloadAttachments("from@email.com", mock(MainModel.DownloadAttachmentsCallback.class));
		Files.delete(tempFolder.getRoot().toPath().resolve("file1.txt"));
		assertGetDownloadedFilesMethod(model, "from@email.com.txt");
	}

	private void assertGetDownloadedFilesMethod(MainModelImpl model, String... expectedFilenames) {
		Path folder = tempFolder.getRoot().toPath();
		Set<Path> expected = Arrays.stream(expectedFilenames) //@formatter:off
			.map(s -> folder.resolve(s))
		.collect(Collectors.toSet()); //@formatter:on

		Set<Path> actual = new HashSet<>(model.getDownloadedFiles());

		assertEquals(expected, actual);
	}

	/**
	 * Tests to see if the given list of files were "downloaded".
	 * @param files the names of the downloaded files
	 * @throws IOException if there's a problem checking for the files
	 */
	private void assertDownloadedFiles(String... files) throws IOException {
		Set<String> expected = new HashSet<>(Arrays.asList(files));

		//@formatter:off
		Set<String> actual = Files.walk(tempFolder.getRoot().toPath())
			.filter(p -> !Files.isDirectory(p)) //exclude the directory itself
			.map(p -> p.getFileName().toString())
		.collect(Collectors.toSet());
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Creates a mock mail server.
	 * @param emails the emails to return when
	 * {@link ReceiveMailSession#receiveEmailAndDelete} is called
	 * @return the mock mail server
	 */
	private static MailServer<ReceiveMailSession> mockMailServer(ReceivedEmail... emails) throws Exception {
		return mockMailServer((Message) null, emails);
	}

	/**
	 * Creates a mock mail server that will test to see if the user is filtering
	 * emails by "from" address.
	 * @param from the from address that the filter object should have when the
	 * emails are "downloaded" from this mock server
	 * @param emails the emails to return when
	 * {@link ReceiveMailSession#receiveEmailAndDelete} is called
	 * @return the mock mail server
	 */
	private static MailServer<ReceiveMailSession> mockMailServer(String from, ReceivedEmail... emails) throws Exception {
		Message message = mock(Message.class);
		when(message.getFrom()).thenReturn(new Address[] { new InternetAddress(from) });
		return mockMailServer(message, emails);
	}

	/**
	 * Creates a mock mail server.
	 * @param messageForFilterTest used to test to make sure the filter object
	 * (which is passed into this server when the user requests to download
	 * emails) is filtering for the correct data (may be null)
	 * @param emails the emails to return when
	 * {@link ReceiveMailSession#receiveEmailAndDelete} is called
	 * @return the mock mail server
	 */
	private static MailServer<ReceiveMailSession> mockMailServer(Message messageForFilterTest, ReceivedEmail... emails) throws Exception {
		@SuppressWarnings("unchecked")
		MailServer<ReceiveMailSession> server = mock(MailServer.class);

		ReceiveMailSession session = new ReceiveMailSessionMock(messageForFilterTest, emails);
		when(server.createSession()).thenReturn(session);

		return server;
	}

	/**
	 * Creates an email attachment.
	 * @param filename the file name of the attachment
	 * @param content the content of the file
	 * @return the attachment
	 */
	private static EmailAttachment<? extends DataSource> attachment(String filename, String content) {
		return EmailAttachment.with().name(filename).content(content.getBytes()).buildByteArrayDataSource();
	}

	/**
	 * <p>
	 * A mock implementation of the {@link ReceiveMailSession} class.
	 * </p>
	 * <p>
	 * This class cannot be stubbed with Mockito because it requires some of its
	 * void methods to be stubbed. The reason these methods cannot be stubbed
	 * is: due to the may you stub void methods in Mockito, it calls the real
	 * method, which results in a NPE because the code inside of the method is
	 * referencing objects that haven't been set.
	 * </p>
	 * <p>
	 * For example, this code throws a NPE:
	 * </p>
	 * 
	 * <pre>
	 * ReceiveMailSession session = Mockito.mock(ReceiveMailSession.class);
	 * Mockito.doNothing().when(session).open();
	 * </pre>
	 * <p>
	 * Maybe you can catch the NPE and just ignore it? No. Mockito throws a
	 * different exception saying that it didn't finish stubbing the class.
	 * </p>
	 * 
	 * @author Michael Angstadt
	 */
	private static class ReceiveMailSessionMock extends ReceiveMailSession {
		private final ReceivedEmail[] emails;
		private final Message mockMessageForFilterTest;

		public ReceiveMailSessionMock(Message mockMessageForFilterTest, ReceivedEmail... emails) {
			super(null, null);
			this.mockMessageForFilterTest = mockMessageForFilterTest;
			this.emails = emails;
		}

		@Override
		public void open() {
			//do nothing
		}

		@Override
		public void close() {
			//do nothing
		}

		@Override
		public ReceivedEmail[] receiveEmailAndDelete() {
			return emails;
		}

		@Override
		public ReceivedEmail[] receiveEmailAndDelete(EmailFilter filter) {
			if (mockMessageForFilterTest != null) {
				assertTrue(filter.getSearchTerm().match(mockMessageForFilterTest));
			}

			return receiveEmailAndDelete();
		}
	}
}
