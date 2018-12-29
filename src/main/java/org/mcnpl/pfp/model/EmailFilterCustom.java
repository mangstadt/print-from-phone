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

import javax.mail.Message;
import javax.mail.search.SearchTerm;
import javax.mail.search.StringTerm;

import jodd.mail.EmailFilter;

/**
 * <p>
 * I had to override the "subject" method of Jodd Mail's {@link EmailFilter}
 * class in order to provide an alternative implementation to Java Mail's
 * {@link javax.mail.search.SubjectTerm SubjectTerm} class.
 * </p>
 * <p>
 * Java Mail's {@link javax.mail.search.SubjectTerm SubjectTerm} class returns a
 * match if the given search term is located ANYWHERE inside the subject line.
 * It does not match the entire subject string. This is a problem because, for
 * example, if a patron is on computer #1, then it will download all emails
 * whose subjects have a "1" SOMEWHERE in the subject line. This means that
 * emails from computers 10, 11, etc would also be downloaded.
 * </p>
 * @author Michael Angstadt
 */
public class EmailFilterCustom extends EmailFilter {
	@Override
	public EmailFilter subject(final String subject) {
		final SearchTerm subjectTerm = new SubjectTerm(subject);
		concat(subjectTerm);
		return this;
	}

	private static class SubjectTerm extends StringTerm {
		private static final long serialVersionUID = -7123356049611846476L;

		public SubjectTerm(String pattern) {
			super(pattern);
		}

		@Override
		public boolean match(Message email) {
			String subject;
			try {
				subject = email.getSubject();
			} catch (Exception e) {
				return false;
			}

			if (subject == null) {
				return false;
			}

			return ignoreCase ? subject.equalsIgnoreCase(pattern) : subject.equals(pattern);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof SubjectTerm)) return false;
			return super.equals(obj);
		}
	}
}
