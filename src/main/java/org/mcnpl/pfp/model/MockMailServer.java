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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataSource;
import javax.mail.search.AndTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.StringTerm;

import jodd.mail.EmailAttachment;
import jodd.mail.EmailFilter;
import jodd.mail.MailException;
import jodd.mail.MailServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;

/**
 * A mock mail server that returns hard-coded emails depending on what the user
 * searches for.
 * @author Michael Angstadt
 */
public class MockMailServer extends MailServer<ReceiveMailSession> {
	public MockMailServer() {
		super("host", 0, null);
	}

	@Override
	public ReceiveMailSession createSession() {
		return new ReceiveMailSessionMock();
	}

	@Override
	protected Properties createSessionProperties() {
		return new Properties();
	}

	private static class ReceiveMailSessionMock extends ReceiveMailSession {
		public ReceiveMailSessionMock() {
			super(null, null);
		}

		@Override
		public void open() {
			sleep(1000);
		}

		@Override
		public void close() {
			//do nothing
		}

		@Override
		public ReceivedEmail[] receiveEmailAndDelete() {
			return new ReceivedEmail[] { //@formatter:off
				email(
					attachment("test-file-1.docx", ""),
					attachment("test-file-2.txt", "two")
				)
			}; //@formatter:on
		}

		@Override
		public ReceivedEmail[] receiveEmailAndDelete(EmailFilter filter) {
			sleep(1000);

			List<StringTerm> stringTerms = new ArrayList<>();
			SearchTerm searchTerm = filter.getSearchTerm();
			if (searchTerm instanceof AndTerm) {
				AndTerm andTerm = (AndTerm) searchTerm;

				stringTerms = Arrays.stream(andTerm.getTerms()) //@formatter:off
					.filter(t -> t instanceof StringTerm)
					.map(t -> (StringTerm)t)
				.collect(Collectors.toList()); //@formatter:on
			} else if (searchTerm instanceof StringTerm) {
				StringTerm stringTerm = (StringTerm) searchTerm;
				stringTerms.add(stringTerm);
			}

			for (StringTerm stringTerm : stringTerms) {
				String term = stringTerm.getPattern().toLowerCase();
				if (term.startsWith("error")) {
					throw new MailException("Mock error.");
				}
				if (term.startsWith("none")) {
					return new ReceivedEmail[0];
				}
				if (term.startsWith("noattach")) {
					return new ReceivedEmail[] { email() };
				}
			}

			return receiveEmailAndDelete();
		}

		private void sleep(long ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException ignore) {
			}
		}
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
	 * Creates an email.
	 * @param attachments the email's attachments
	 * @return the email
	 */
	@SafeVarargs
	private static ReceivedEmail email(EmailAttachment<? extends DataSource>... attachments) {
		return ReceivedEmail.create().attachments(Arrays.asList(attachments)).message("Email body", "text/plain");
	}
}
