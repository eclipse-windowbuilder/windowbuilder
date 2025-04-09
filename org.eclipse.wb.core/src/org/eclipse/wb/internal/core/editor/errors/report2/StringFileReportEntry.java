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

import java.io.InputStream;

/**
 * Report entry for writing a string into a file.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public class StringFileReportEntry extends FileReportEntry {
	private final String m_contents;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StringFileReportEntry(String name, String contents) {
		super(name);
		m_contents = contents;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FileReportEntry
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected InputStream getContents() throws Exception {
		return IOUtils.toInputStream(m_contents);
	}
}
