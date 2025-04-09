/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.errors.report2;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Abstract report entry dealing with single file.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public abstract class FileReportEntry implements IReportEntry {
	private final String m_filePath;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FileReportEntry(String name) {
		m_filePath = name;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IReportEntry
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void write(ZipOutputStream zipStream) throws Exception {
		InputStream fileStream = getContents();
		if (fileStream != null) {
			zipStream.putNextEntry(new ZipEntry(getName()));
			try {
				IOUtils.copy(fileStream, zipStream);
			} finally {
				zipStream.closeEntry();
				IOUtils.closeQuietly(fileStream);
			}
		}
	}

	protected InputStream getContents() throws Exception {
		File file = new File(m_filePath);
		if (checkFile(file)) {
			return new FileInputStream(file);
		}
		// doesn't exist
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the raw file path as it passed into constructor.
	 */
	protected final String getFilePath() {
		return m_filePath;
	}

	/**
	 * @return the name of ZipEntry.
	 */
	protected String getName() {
		return m_filePath;
	}

	/**
	 * @return <code>true</code> if the file exists and accessible for reading.
	 */
	public static boolean checkFile(File file) {
		if (file.exists()) {
			// check file for reading
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				inputStream.read();
				// all OK
				return true;
			} catch (Throwable e) {
				// ignore, means not accessible
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
		// cannot read
		return false;
	}
}
