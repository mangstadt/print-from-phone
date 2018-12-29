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
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.mcnpl.common.App;
import org.mcnpl.common.gui.DialogBuilder;
import org.mcnpl.pfp.image.Images;
import org.mcnpl.pfp.model.MainModel;
import org.mcnpl.pfp.model.MainModelImpl;
import org.mcnpl.pfp.model.MockMailServer;
import org.mcnpl.pfp.view.MainView;
import org.mcnpl.pfp.view.MainViewImpl;
import org.mcnpl.pfp.wcm.WindowsCredentialManager;

import jodd.mail.MailServer;
import jodd.mail.ReceiveMailSession;

/**
 * Contains the main method.
 * @author Michael Angstadt
 */
public class Main {
	public static final String VERSION;
	static {
		Properties properties = new Properties();
		try (InputStream in = Main.class.getResourceAsStream("build.properties")) {
			properties.load(in);
		} catch (IOException ignore) {
		}

		VERSION = properties.getProperty("version");
	}

	public static void main(String[] args) throws Exception {
		App app = App.create("print-from-phone");
		app.setFontSize(24);
		app.setIcon(Images.APP_ICON);
		app.init();

		String settingsFile = "settings.properties";
		boolean mock = false;
		for (String arg : args) {
			if ("--mock".equals(arg)) {
				mock = true;
			} else {
				settingsFile = arg;
			}
		}

		Settings settings;
		{
			Path path = Paths.get(settingsFile);
			settings = new Settings(path);

			List<String> undefinedFields = settings.getUndefinedRequiredFields();
			if (!undefinedFields.isEmpty()) {
				launchError("Invalid configuration in settings file.\n\nThe following fields are required:\n" + String.join("\n", undefinedFields));
				return;
			}
		}

		/*
		 * Get the email account credentials.
		 */
		String username, password;
		if (settings.getEmailUsername() == null) {
			String[] credentials = WindowsCredentialManager.getCredentials("Print From Phone");
			if (credentials == null) {
				launchError("No login credentials for the email account were found in the settings file or in the Windows Credential Mananger.");
				return;
			}
			username = credentials[0];
			password = credentials[1];
		} else {
			username = settings.getEmailUsername();
			password = settings.getEmailPassword();
		}

		/*
		 * Define the email server connection information.
		 */
		MailServer<ReceiveMailSession> server;
		if (mock) {
			server = new MockMailServer();
		} else {
			//@formatter:off
			MailServer.Builder builder = MailServer.create()
			.host(settings.getEmailServer())
			.ssl(true)
			.auth(username, password);
			//@formatter:on

			String protocol = settings.getEmailProtocol();
			switch (protocol.toLowerCase()) {
			case "imap":
				server = builder.buildImapMailServer();
				break;
			case "pop":
				server = builder.buildPop3MailServer();
				break;
			default:
				launchError("Invalid configuration in settings file.\n\n\"" + protocol + "\" is not a valid value for the \"email.protocol\" setting. Only \"imap\" and \"pop\" are supported.");
				return;
			}
		}

		//@formatter:off
		MainModel model = new MainModelImpl.Builder()
			.server(server)
			.email(settings.getEmail())
			.downloadDirectory(settings.getAttachmentSaveLocation())
			.statistics(settings.getStatisticsFile())
			.disclaimer(settings.getDisclaimer())
		.build();
		//@formatter:on

		MainView view = new MainViewImpl();
		new MainPresenter(view, model);
	}

	/**
	 * Display an error message to the user explaining why the app cannot be
	 * launched.
	 * @param message the error message to display
	 */
	private static void launchError(String message) {
		//@formatter:off
		DialogBuilder.error()
			.title("Configuration Error")
			.text("Cannot launch app: " + message)
			.buttons(JOptionPane.DEFAULT_OPTION, "Exit")
		.show();
		//@formatter:on
	}

	/**
	 * Gets the computer name.
	 * @return the computer name or null if it can't be found
	 */
	public static String getComputerName() {
		return System.getenv().get("COMPUTERNAME");
	}
}
