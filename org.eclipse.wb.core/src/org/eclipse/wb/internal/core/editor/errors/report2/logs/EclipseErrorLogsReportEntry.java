/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.editor.errors.report2.logs;

import org.eclipse.wb.internal.core.editor.errors.report2.FileListReportEntry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

/**
 * The provider of Eclipse log files.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class EclipseErrorLogsReportEntry extends FileListReportEntry {
	@Override
	protected List<File> getFiles() {
		// get path to Eclipse .log file(s) and get it's directory
		IPath logsPath = Platform.getLogFileLocation().makeAbsolute().removeLastSegments(1);
		File logsPathAsFile = logsPath.toFile();
		// get list of .log files
		File[] logFiles = logsPathAsFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".log");
			}
		});
		return Arrays.asList(logFiles);
	}

	@Override
	protected String getPrefix() {
		return "eclipse-logs/";
	}
}
