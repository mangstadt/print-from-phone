# Print From Phone

| Download: | [Version 1.0](https://github.com/mangstadt/print-from-phone/downloads/print-from-phone-1.0.zip) |
| License: | [![GPL v3 License](https://img.shields.io/badge/license-GPL%20v3-blue.svg)](https://github.com/mangstadt/print-from-phone/blob/master/LICENSE.txt) |

**Print From Phone** is a desktop application for public libraries that aims to simplify the process of printing documents from patrons' smart phones.

# How does it work?

**Step 1:** Patron opens the app and accepts the terms and conditions.

[![Step 1](https://github.com/mangstadt/print-from-phone/screenshots/step1.png)]

**Step 2:** Patron is prompted to email the documents they would like to print from their phone to an email account that is under the library's control. This can often by done by opening the document on their phone, and then clicking a "Share" button. They can also forward an existing email that has attachments.

[![Step 2](https://github.com/mangstadt/print-from-phone/screenshots/step2.png)]

**Step 3:** Patron is prompted to enter their email address.

[![Step 3](https://github.com/mangstadt/print-from-phone/screenshots/step3.png)]

**Step 4:** The application connects to the library-controlled email account and downloads the files that are attached to the patron's email.

[![Step 4](https://github.com/mangstadt/print-from-phone/screenshots/step4.png)]

# Installation instructions

1. Print From Phone requires Java 8 or later. You can download Java here: [https://java.com](https://java.com)
1. Download the Print From Phone ZIP file here: [https://github.com/mangstadt/print-from-phone/downloads/print-from-phone-1.0.zip](https://github.com/mangstadt/print-from-phone/downloads/print-from-phone-1.0.zip)
1. Extract the contents of the ZIP file to a location of your choice.
1. Open the "settings.properties" file with a text editor and edit as needed. This file must be stored in the same place as the "print-from-phone-1.0.jar" file.
1. Open the "license.html" file and edit to your liking. This is the "term and conditions" text that the patron first sees when they open the application.
1. Double-click the "print-from-phone-1.0.jar" file to launch the application.

# How to add credentials to the Windows Credential Manager

The email account's username and password can be stored in the Windows Credential Manager instead of the settings.properties file.

1. Open the Start Menu and search for "Credential Manager".
1. Click "Windows Credentials".
1. Click "Add a generic credential".
1. In the "Internet or network address" field, type "Print From Phone".
1. Enter the email account username and password in the "User name" and "Password" fields.
1. In the settings.properties file, empty out the "email.username" and "email.password" fields.

# How to configure Gmail to be the recipient address

1. Enable IMAP.
  1. In the settings, go to "Forwarding and POP/IMAP".
  1. Select "Enable IMAP".
  1. Select "Auto-Expunge off"
  1. Under "When a message is marked as deleted...", select "Immediately delete the message forever". This will force Gmail to permanently delete the email after Print From Email downloads it.
  1. In the settings.properties file, set the "email.server" field to "imap.gmail.com".
1. Turn off the spam filter.
  1. In the settings, go to "Filters and Blocked Addresses".
  1. Click "Create new filter".
  1. In the "Has the words" field, type "is:spam".
  1. Click "Create filter with this search".
  1. A confirmation dialog will appear. Click OK.
  1. Check the "Never send it to Spam" checkbox.
  1. Click "Create filter".
1. Make sure "less secure apps" is enabled. Google considers IMAP to be less secure.
  1. Go to [https://myaccount.google.com](https://myaccount.google.com).
  1. Search for "less secure app access".
  1. Enable the "Allow less secure apps" setting.

# Security warning

Since the email password is stored in plain text, it could potentially be discovered by someone who has the technical skills and motivation to do so. As stated in the [license](https://github.com/mangstadt/print-from-phone/blob/master/LICENSE.txt), this program is provided without warranty.

# Build instructions for developers

Print From Phone uses [Gradle](https://gradle.org/) as its build tool.

Running the command `gradle build` will build a fat, executable JAR file and save it to the `build/libs` folder.

# Questions and feedback

Please let me know how Print From Phone is working out for you:

 * If you have a feature request or found a bug, please create a ticket on the [Issue tracker](https://github.com/mangstadt/print-from-phone/issues).
 * You can also email me directly if you prefer: [mike.angstadt@gmail.com](mailto:mike.angstadt@gmail.com)

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8CEN7MPKRBKU6&lc=US&item_name=Michael%20Angstadt&item_number=Email%20Print&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)