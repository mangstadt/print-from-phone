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

package org.mcnpl.pfp.wcm;

import java.nio.charset.StandardCharsets;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Gets credentials stored in the Windows Credential Manager (accessible via
 * Control Panel).
 * @author Michael Angstadt
 * @author <a href= "https://github.com/dariusz-szczepaniak">Dariusz
 * Szczepaniak</a>
 * @see "https://github.com/dariusz-szczepaniak/java.jna.WindowsCredentialManager"
 */
public final class WindowsCredentialManager {
	/**
	 * Gets credentials stored in the Windows Credential Manager (only supports
	 * the retrieval of "generic" credentials).
	 * @param address the internet or network address of the credential
	 * @throws IllegalStateException if there is a problem retrieving the
	 * credentials
	 * @return the username (index 0) and password (index 1), or null if not
	 * found
	 */
	public static String[] getCredentials(String address) {
		IntByReference pCount = new IntByReference();
		PointerByReference pCredentials = new PointerByReference();
		Advapi32_Credentials.INSTANCE.CredEnumerateW(null, 0, pCount, pCredentials);
		Pointer[] ps = pCredentials.getValue().getPointerArray(0, pCount.getValue());

		for (int i = 0; i < pCount.getValue(); i++) {
			Credential arrRef = new Credential(ps[i]);
			arrRef.read();

			/*
			 * Only search over "generic" credentials.
			 */
			if (arrRef.Type != 1) {
				continue;
			}

			String targetName = arrRef.TargetName.getWideString(0);
			if (address.equalsIgnoreCase(targetName)) {
				String username = null;
				try {
					if (arrRef.UserName != null) {
						username = arrRef.UserName.getWideString(0);
					}
				} catch (Error e) {
					throw new IllegalStateException("Could not retrieve username from Windows Credential Manager.", e);
				}

				String password;
				if (arrRef.CredentialBlobSize > 0) {
					byte[] bytes = arrRef.CredentialBlob.getByteArray(0, arrRef.CredentialBlobSize);
					password = new String(bytes, StandardCharsets.UTF_16LE);
				} else {
					password = "";
				}

				return new String[] { username, password };
			}
		}

		return null;
	}

	private WindowsCredentialManager() {
		//hide
	}
}
