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

import java.nio.file.Path;
import java.util.List;

import org.mcnpl.pfp.model.MainModel;
import org.mcnpl.pfp.view.MainView;

/**
 * @author Michael Angstadt
 */
public class MainPresenter {
	private final MainView view;
	private final MainModel model;

	public MainPresenter(MainView view, MainModel model) {
		this.view = view;
		this.model = model;

		view.setDisclaimer(model.getDisclaimer());
		view.setLibraryEmail(model.getEmail());
		view.setDownloadDirectory(model.getDownloadDirectory());

		view.onCheckForEmails(this::onCheckForEmails);
		view.onDeleteFiles(this::onDeleteFiles);
		view.onClose(this::onClose);

		view.display();
	}

	private void onCheckForEmails() {
		String from = view.getPatronEmail();

		view.setStatusConnecting();

		Thread t = new Thread(() -> {
			try {
				model.downloadAttachments(from, new MainModel.DownloadAttachmentsCallback() {
					@Override
					public void connected() {
						view.invoke(view::setStatusDownloading);
					}

					@Override
					public void done(int emails, int attachments) {
						view.invoke(() -> {
							view.setStatusDownloaded(emails, attachments);
						});
					}
				});
			} catch (Throwable thrown) {
				view.invoke(() -> {
					view.setStatusError(thrown);
				});
				return;
			}

			try {
				model.storeStatistics();
			} catch (Exception e) {
				/*
				 * If the statistics can't be updated, let it pass to the
				 * unhandled exception handler so staff knows about it. Do not
				 * prevent the user from viewing their attachments.
				 */
				throw new RuntimeException(e);
			}
		});

		t.setDaemon(true);
		t.start();
	}

	private void onDeleteFiles() {
		while (true) {
			int total = model.getDownloadedFiles().size();
			List<Path> couldNotDelete = model.deleteDownloadedFiles();
			if (couldNotDelete.isEmpty()) {
				view.showFilesDeletedMessage(total);
				break;
			}

			boolean tryAgain = view.showFilesNotDeletedPrompt(total - couldNotDelete.size(), couldNotDelete);
			if (!tryAgain) {
				break;
			}
		}
	}

	private void onClose() {
		List<Path> downloadedFiles = model.getDownloadedFiles();
		if (downloadedFiles.isEmpty()) {
			view.close();
			return;
		}

		Boolean delete = view.showDeleteFilesPrompt(downloadedFiles);
		if (delete == null) {
			return;
		}
		if (!delete) {
			view.close();
			return;
		}

		while (true) {
			List<Path> couldNotDelete = model.deleteDownloadedFiles();
			if (couldNotDelete.isEmpty()) {
				view.showFilesDeletedMessage(downloadedFiles.size());
				view.close();
				break;
			}

			int filesSuccessfullyDeleted = downloadedFiles.size() - couldNotDelete.size();
			boolean tryAgain = view.showFilesNotDeletedPrompt(filesSuccessfullyDeleted, couldNotDelete);
			if (!tryAgain) {
				//do not exit the program
				break;
			}
		}
	}
}
