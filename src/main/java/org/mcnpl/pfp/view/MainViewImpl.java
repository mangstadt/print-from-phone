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

import static org.mcnpl.common.gui.GuiUtils.addCloseDialogListener;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.mcnpl.common.App;
import org.mcnpl.common.gui.DialogBuilder;
import org.mcnpl.pfp.Main;
import org.mcnpl.pfp.image.Images;

import net.miginfocom.swing.MigLayout;

/**
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class MainViewImpl extends JFrame implements MainView {
	private final int WIDTH = 800;

	private List<StepPanel> panels;
	private int panelPos;
	private PromptForEmailStep emailStep;
	private DownloadStep downloadStep;
	private boolean somethingWasDownloaded = false;

	private String libraryEmail, disclaimer;
	private Path downloadDirectory;
	private Runnable onCheckForEmails, onDeleteFiles, onClose;

	public MainViewImpl() {
		setSize(WIDTH, 700);
		setTitle("Print From Phone v" + Main.VERSION + " by Michael Angstadt");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addCloseDialogListener(this, (event) -> {
			onClose.run();
		});
		setIconImage(App.get().getIcon().getImage());
		setLocationRelativeTo(null); //center on screen
	}

	private class DisclaimerStep extends StepPanel {
		public DisclaimerStep(int step, String disclaimer) {
			super(step);

			//@formatter:off
			JButton next = new ButtonBuilder()
				.text("Next")
				.icon(Images.FORWARD)
				.iconOnRight(true)
				.onClick(this::onNext)
			.build();
			next.setEnabled(false);
			//@formatter:on

			JEditorPane disclaimerLabel = new JEditorPane("text/html", disclaimer);
			disclaimerLabel.setEditable(false);

			JCheckBox agree = new JCheckBox("I agree");
			agree.addActionListener((event) -> {
				next.setEnabled(agree.isSelected());
			});

			JScrollPane scroll = new JScrollPane(disclaimerLabel);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			add(scroll, "w 100%, h 500, wrap");
			add(agree, "align center, wrap");
			add(next, "align center");
		}
	}

	private class InstructionsStep extends StepPanel {
		public InstructionsStep(int step, String libraryEmail) {
			super(step);

			//@formatter:off
			JButton next = new ButtonBuilder()
				.text("Next")
				.icon(Images.FORWARD)
				.iconOnRight(true)
				.onClick(this::onNext)
			.build();
			//@formatter:on

			JLabel libraryEmailLabel = new BlackLabel(libraryEmail);

			add(new JLabel("<html>Send email with attachment(s) to:"), "align center, wrap");
			add(libraryEmailLabel, "align center, wrap");
			add(next, "align center");
		}
	}

	private class PromptForEmailStep extends StepPanel {
		private final JTextField email;
		private final Pattern emailRegex = Pattern.compile(".+@.+\\..+");

		public PromptForEmailStep(int step) {
			super(step);

			email = new JTextField();
			email.addActionListener((event) -> {
				onNext();
			});

			//@formatter:off
			JButton back = new ButtonBuilder()
				.text("Back")
				.icon(Images.BACK)
				.onClick(this::onBack)
			.build();

			JButton next = new ButtonBuilder()
				.text("Next")
				.icon(Images.FORWARD)
				.iconOnRight(true)
				.onClick(this::onNext)
			.build();
			//@formatter:on

			//layout UI
			{
				add(new JLabel("What is your email address?"), "align center, wrap");
				add(email, "w 70%, align center, wrap");

				add(back, "align center, split 2");
				add(next);
			}
		}

		@Override
		protected void onShow() {
			email.requestFocus();
		}

		@Override
		protected void onNext() {
			boolean validEmail = emailRegex.matcher(getPatronEmail()).matches();
			if (!validEmail) {
				//@formatter:off
				DialogBuilder.error()
					.parent(MainViewImpl.this)
					.text("<html><span style=\"color:#880000\">You have not entered a valid email address.")
					.title("Error")
				.show();
				//@formatter:on

				email.requestFocus();
				return;
			}

			super.onNext();
		}
	}

	private class DownloadStep extends StepPanel {
		private final StatusLabel connecting, downloading;
		private boolean connected = false;

		public DownloadStep(int step) {
			super(step);

			connecting = new StatusLabel("Connecting to " + libraryEmail + "...");
			downloading = new StatusLabel("Downloading emails from <b>" + getPatronEmail() + "</b>...");

			//layout UI
			{
				add(connecting, "wrap");
				add(downloading, "gapbottom 20, wrap");
			}
		}

		@Override
		public void onShow() {
			onCheckForEmails.run();
		}

		private void connecting() {
			connecting.setStatusLoading();
		}

		private void downloading() {
			connected = true;
			connecting.setStatusDone();
			downloading.setStatusLoading();
		}

		private void downloaded(int emails, int attachments) {
			if (emails > 0) {
				somethingWasDownloaded = true;
			}

			downloading.setStatusDone();

			//@formatter:off
			JButton back = new ButtonBuilder()
				.text("Re-enter my email")
				.icon(Images.BACK)
				.onClick(this::onBack)
			.build();
			
			JButton checkAgain = new ButtonBuilder()
				.text("Check again")
				.icon(Images.RELOAD)
				.onClick((event) -> {
					/*
					 * "Reload" this panel by decrementing the position counter and calling "onNext()".
					 */
					panelPos--;
					onNext();
				})
			.build();
			
			JButton showDownloadedFiles = new ButtonBuilder()
				.text("<html>Show my downloaded emails and attachments") //prefixing with <html> enables word wrapping
				.icon(Images.SHOW_FILES)
				.onClick(MainViewImpl.this::openDownloadLocation)
			.build();

			JButton deleteFiles = new ButtonBuilder()
				.text("<html><b style=\"color:#880000\">Delete my downloaded files from this computer")
				.icon(Images.DELETE)
				.onClick(onDeleteFiles)
			.build();

			JButton exit = new ButtonBuilder()
				.text("Exit")
				.icon(Images.EXIT)
				.onClick(onClose)
			.build();
			//@formatter:on

			add(new JLabel("<html><b>Found " + emails + plural(" email", emails) + " and " + attachments + plural(" attachment", attachments) + "."), "gapbottom 20, wrap");

			boolean noEmailsFound = (emails == 0);
			boolean noAttachmentsFound = (attachments == 0);

			String errorMessage = null;
			List<String> suggestions = Collections.emptyList();
			if (noEmailsFound) {
				errorMessage = "No emails from <b>" + getPatronEmail() + "</b> were found in our inbox.";

				//@formatter:off
				suggestions = Arrays.asList(
					"Did you send the email to <b>" + libraryEmail + "</b>?",
					"Is your email address correct?",
					"Is the email in your phone's \"Sent\" folder yet?",
					"The email you sent us may not have arrived yet. Wait a minute and click \"Check again\"."
				);
				//@formatter:on
			} else if (noAttachmentsFound) {
				//@formatter:off
				suggestions = Arrays.asList(
					"The " + plural("email", emails) + " we found did not have any attachments. Did you forget to attach your files to the email? If so, send us another email with the documents attached, and click \"Check again\"."
				);
				//@formatter:on
			}

			if (errorMessage != null) {
				add(new JLabel("<html><span style=\"color:#880000\">" + errorMessage), "wrap, gapbottom 20");
			}
			if (!suggestions.isEmpty()) {
				add(new BulletedList(suggestions), "wrap, gapbottom 20");
			}

			/*
			 * This StepPanel object is recreated every time the user checks for
			 * new emails. Show the "Show Downloaded Files" and "Delete files"
			 * button if something was downloaded since the app was first
			 * launched.
			 */
			if (somethingWasDownloaded) add(showDownloadedFiles, "w 400!, wrap");
			if (noEmailsFound) add(back, "w 400!, wrap");
			add(checkAgain, "w 400!, wrap");
			if (somethingWasDownloaded) add(deleteFiles, "w 400!, wrap");
			add(exit, "w 400!");

			refreshGui();

			/*
			 * When the app is configured to prompt the user for their email
			 * address, and then it doesn't find any emails, the "Exit" button
			 * gets partially cut off because the window height is not high
			 * enough, despite the fact that we are calling "pack()".
			 * 
			 * I think it does this because it doesn't take into account the
			 * extra space the red error message takes up when it wraps onto
			 * multiple lines.
			 * 
			 * Dirty workaround: Manually increase the window height.
			 */
			MainViewImpl.this.setSize(getWidth(), getHeight() + 150);
		}

		private void error(Throwable thrown) {
			StatusLabel erroredStep = connected ? downloading : connecting;
			erroredStep.setStatusError();

			StackTraceBox stackTrace = new StackTraceBox(thrown);

			add(new JLabel("<html><span style=\"color:#880000\"><b>An error occurred. Please alert a staff member."), "gapbottom 20, wrap");
			JScrollPane scroll = new JScrollPane(stackTrace);
			add(scroll, "grow, w 100%, h 100%, align center");

			refreshGui();
		}
	}

	private boolean showingDisclaimer() {
		return disclaimer != null;
	}

	private void refreshGui() {
		pack();
		validate();
		repaint();
	}

	@Override
	public String getPatronEmail() {
		/*
		 * Remove all whitespace from the email address.
		 * 
		 * People sometimes type spaces when they're not supposed to. Since
		 * email addresses never have spaces in them, it should be safe to just
		 * remove them.
		 */
		return emailStep.email.getText().replaceAll("\\s+", "");
	}

	@Override
	public void setLibraryEmail(String email) {
		this.libraryEmail = email;
	}

	@Override
	public void setDisclaimer(String disclaimer) {
		this.disclaimer = disclaimer;
	}

	@Override
	public void setDownloadDirectory(Path directory) {
		downloadDirectory = directory;
	}

	@Override
	public void setStatusConnecting() {
		downloadStep.connecting();
	}

	@Override
	public void setStatusDownloading() {
		downloadStep.downloading();
	}

	@Override
	public void setStatusDownloaded(int emails, int attachments) {
		downloadStep.downloaded(emails, attachments);
	}

	@Override
	public void setStatusError(Throwable t) {
		downloadStep.error(t);
	}

	@Override
	public void onCheckForEmails(Runnable runnable) {
		onCheckForEmails = runnable;
	}

	@Override
	public void onDeleteFiles(Runnable runnable) {
		onDeleteFiles = runnable;
	}

	@Override
	public void onClose(Runnable runnable) {
		onClose = runnable;
	}

	@Override
	public void openDownloadLocation() {
		if (!Desktop.isDesktopSupported()) {
			return;
		}

		Desktop d = Desktop.getDesktop();
		if (d == null || !d.isSupported(Desktop.Action.OPEN)) {
			return;
		}

		try {
			d.open(downloadDirectory.toFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void showFilesDeletedMessage(int deleted) {
		//@formatter:off
		DialogBuilder.info()
			.parent(this)
			.text(deleted + plural(" file has", " files have", deleted) +  " been deleted.")
			.title("Success")
		.show();
		//@formatter:on
	}

	@Override
	public boolean showFilesNotDeletedPrompt(int deleted, List<Path> notDeleted) {
		//@formatter:off
		int answer = DialogBuilder.warning()
			.parent(this)
			.text("The following files could not be deleted. Close all open windows and click \"Try Again\":\n" + bulletedFilenameList(notDeleted))
			.title("Files could not be deleted")
			.buttons(JOptionPane.YES_NO_OPTION, "*Try again", "Cancel")
		.show();
		//@formatter:on

		return answer == JOptionPane.YES_OPTION;
	}

	@Override
	public Boolean showDeleteFilesPrompt(List<Path> downloadedFiles) {
		//@formatter:off
		int answer = DialogBuilder.question()
			.parent(this)
			.text("You've downloaded the following files.\nDo you want to delete them off of this computer?\n" + bulletedFilenameList(downloadedFiles))
			.title("Delete Downloaded Files?")
			.buttons(JOptionPane.YES_NO_CANCEL_OPTION, "Delete", "Keep", "*Cancel")
		.show();
		//@formatter:on

		switch (answer) {
		case JOptionPane.YES_OPTION:
			return Boolean.TRUE;
		case JOptionPane.NO_OPTION:
			return Boolean.FALSE;
		default:
			return null;
		}
	}

	private String bulletedFilenameList(List<Path> files) {
		StringBuilder sb = new StringBuilder();
		for (Path file : files) {
			sb.append("\n• ").append(file.getFileName().toString());
		}
		return sb.toString();
	}

	@Override
	public void invoke(Runnable runnable) {
		SwingUtilities.invokeLater(runnable);
	}

	@Override
	public void display() {
		int steps = 0;
		panels = new ArrayList<>();
		if (showingDisclaimer()) {
			panels.add(new DisclaimerStep(++steps, disclaimer));
		}
		panels.add(new InstructionsStep(++steps, libraryEmail));

		emailStep = new PromptForEmailStep(++steps);
		panels.add(emailStep);

		/*
		 * The download step is not included in the "panels" list because a new
		 * instance must be created every time it is shown.
		 */

		setLayout(new MigLayout());
		add(panels.get(0), "w " + WIDTH);
		pack();

		setVisible(true);
	}

	@Override
	public void close() {
		dispose();
	}

	/**
	 * Returns the singular or plural version of a word.
	 * @param word the singular version of the word
	 * @param number the number
	 * @return the singular version if the number is 1, otherwise returns the
	 * plural version
	 */
	private static String plural(String word, int number) {
		return plural(word, word + "s", number);
	}

	/**
	 * Determines whether to use a singular or plural version of a word.
	 * @param singular the singular version
	 * @param plural the plural version
	 * @param number the number
	 * @return the singular version of the number is 1, otherwise returns the
	 * plural version
	 */
	private static String plural(String singular, String plural, int number) {
		return (number == 1) ? singular : plural;
	}

	private class StepPanel extends JPanel {
		public StepPanel(int stepNumber) {
			super(new MigLayout());

			int numSteps = 3;
			if (showingDisclaimer()) {
				numSteps++;
			}

			/**
			 * Set width to 100% because without this, the letter "L" gets cut
			 * off at the end because it's in italics.
			 */
			add(new JLabel("<html><b><i>Print From Phone"), "w 100%, wrap");

			add(new JSeparator(), "w 100%, wrap");
			add(new JLabel("<html><span style=\"color:#0000aa; font-weight:bold; font-size:2em\">Step " + stepNumber + " of " + numSteps), "wrap");
		}

		protected void onShow() {
			//empty, meant to be overridden if needed
		}

		protected void onNext() {
			if (panelPos >= panels.size()) {
				//already showing the last panel
				return;
			}

			panelPos++;
			StepPanel next;
			if (panelPos == panels.size()) {
				/*
				 * The download step is not included in the "panels" list
				 * because a new instance of this panel must be created every
				 * time it is shown.
				 */
				downloadStep = new DownloadStep(panelPos + 1);
				next = downloadStep;
			} else {
				next = panels.get(panelPos);
			}

			show(next);
		}

		protected void onBack() {
			if (panelPos == 0) {
				return;
			}

			panelPos--;
			StepPanel prev = panels.get(panelPos);
			show(prev);
		}

		private void show(StepPanel panel) {
			MainViewImpl.this.remove(this);
			MainViewImpl.this.add(panel, "w " + MainViewImpl.this.WIDTH);
			refreshGui();
			panel.onShow();
		}
	}

	private static class BlackLabel extends JLabel {
		public BlackLabel(String text) {
			super("<html><b><span style=\"font-size:1.5em\">&nbsp;" + text + " ");
			setOpaque(true);
			setBackground(Color.BLACK);
			setForeground(Color.WHITE);
		}
	}

	private static class StatusLabel extends JLabel {
		private static final ImageIcon empty = Images.scale(Images.EMPTY, 32);
		private static final ImageIcon loading = Images.scale(Images.LOADING, 32);
		private static final ImageIcon done = Images.scale(Images.CHECKMARK, 32);
		private static final ImageIcon error = Images.scale(Images.ERROR, 32);

		public StatusLabel(String htmlText) {
			setText("<html>" + htmlText);
			setIcon(empty);
			setEnabled(false); //gives the label a dark gray color
		}

		public void setStatusLoading() {
			setEnabled(true);
			setIcon(loading);
		}

		public void setStatusDone() {
			setEnabled(true);
			setIcon(done);
		}

		public void setStatusError() {
			setEnabled(true);
			setIcon(error);
		}
	}

	private static class StackTraceBox extends JTextArea {
		public StackTraceBox(Throwable thrown) {
			setEditable(false);
			setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

			setText(getStackTrace(thrown));
			setCaretPosition(0); //scroll to top after setting the text
		}

		private static String getStackTrace(Throwable thrown) {
			StringWriter sw = new StringWriter();
			PrintWriter writer = new PrintWriter(sw);
			thrown.printStackTrace(writer);
			return sw.toString().replaceAll("\t", "  "); //tabs take up too much space
		}
	}

	private static class ButtonBuilder {
		private String text;
		private ImageIcon icon;
		private boolean iconOnRight;
		private ActionListener listener;

		public ButtonBuilder text(String text) {
			this.text = text;
			return this;
		}

		public ButtonBuilder icon(ImageIcon icon) {
			this.icon = icon;
			return this;
		}

		public ButtonBuilder iconOnRight(boolean iconOnRight) {
			this.iconOnRight = iconOnRight;
			return this;
		}

		public ButtonBuilder onClick(Runnable runnable) {
			return onClick((event) -> {
				runnable.run();
			});
		}

		public ButtonBuilder onClick(ActionListener listener) {
			this.listener = listener;
			return this;
		}

		public JButton build() {
			JButton button = new JButton();

			button.setText(text);
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.setIconTextGap(10);
			button.setIcon(Images.scale(icon, 48));
			button.addActionListener(listener);
			if (iconOnRight) {
				button.setHorizontalTextPosition(SwingConstants.LEFT);
			}

			return button;
		}
	}

	private static class BulletedList extends JLabel {
		public BulletedList(List<String> items) {
			StringBuilder sb = new StringBuilder("<html><table cellpadding=\"0\" cellspacing=\"0\">");
			for (String item : items) {
				//@formatter:off
				sb.append(
				"<tr>" +
					"<td valign=\"top\">&bull;&nbsp;</td>" +
					"<td>" + item + "</td>" +
				"</tr>"
				);
				//@formatter:on
			}
			setText(sb.toString());
		}
	}
}
