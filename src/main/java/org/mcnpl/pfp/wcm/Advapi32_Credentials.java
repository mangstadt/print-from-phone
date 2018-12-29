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

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * @author <a href="https://github.com/dariusz-szczepaniak">Dariusz
 * Szczepaniak</a>
 * @see "https://github.com/dariusz-szczepaniak/java.jna.WindowsCredentialManager"
 */
public interface Advapi32_Credentials extends StdCallLibrary {
	Advapi32_Credentials INSTANCE = (Advapi32_Credentials) Native.loadLibrary("advapi32", Advapi32_Credentials.class);

	//@formatter:off
	/*
	BOOL CredEnumerate( 
		_In_  LPCTSTR     Filter,
		_In_  DWORD       Flags,
		_Out_ DWORD       *Count,
		_Out_ PCREDENTIAL **Credentials
	)
	*/
	//@formatter:on
	boolean CredEnumerateW(String filter, int flags, IntByReference count, PointerByReference pref);
}
