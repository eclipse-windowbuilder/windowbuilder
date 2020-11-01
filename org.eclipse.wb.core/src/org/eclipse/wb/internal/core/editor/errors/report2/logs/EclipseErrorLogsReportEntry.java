/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.ArrayList;
import java.util.Collections;
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
    File[] logFiles =
        logsPathAsFile.listFiles((FilenameFilter) (dir, name) -> name.endsWith(".log"));
    List<File> list = new ArrayList<>();
    Collections.addAll(list, logFiles);
    return list;
  }

  @Override
  protected String getPrefix() {
    return "eclipse-logs/";
  }
}
