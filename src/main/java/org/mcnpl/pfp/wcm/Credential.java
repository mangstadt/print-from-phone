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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase.FILETIME;

/**
 * @author <a href="https://github.com/dariusz-szczepaniak">Dariusz
 * Szczepaniak</a>
 * @see "https://github.com/dariusz-szczepaniak/java.jna.WindowsCredentialManager"
 */
public class Credential extends Structure {
	//@formatter:off
	/*
	typedef struct _CREDENTIAL {
		DWORD                 Flags;
		DWORD                 Type;
		LPTSTR                TargetName;
		LPTSTR                Comment;
		FILETIME              LastWritten;
		DWORD                 CredentialBlobSize;
		LPBYTE                CredentialBlob;
		DWORD                 Persist;
		DWORD                 AttributeCount;
		PCREDENTIAL_ATTRIBUTE Attributes;
		LPTSTR                TargetAlias;
		LPTSTR                UserName;
	} CREDENTIAL, *PCREDENTIAL;
	*/
	//@formatter:on
	public static class ByReference extends Credential implements Structure.ByReference {
	}

	public int Flags;
	public int Type;
	public Pointer TargetName;
	public Pointer Comment;
	public FILETIME LastWritten = new FILETIME();
	public int CredentialBlobSize;
	public Pointer CredentialBlob;
	public int Persist;
	public int AttributeCount;
	public Pointer Attributes;
	public Pointer TargetAlias;
	public Pointer UserName;

	@Override
	protected List<?> getFieldOrder() {
		return Arrays.asList("Flags", "Type", "TargetName", "Comment", "LastWritten", "CredentialBlobSize", "CredentialBlob", "Persist", "AttributeCount", "Attributes", "TargetAlias", "UserName");
	}

	public Credential(Pointer p) {
		super(p);
	}

	public Credential() {
	}
}
