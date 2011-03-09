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
package org.eclipse.wb.internal.core.editor.errors.report.logs;

import com.google.common.collect.Lists;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * The provider of Eclipse log files.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public class EclipseErrorLogsProvider implements IErrorLogsProvider {
  public List<File> getLogFiles(IProject project) {
    // get path to Eclipse .log file(s) and get it's directory 
    IPath logsPath = Platform.getLogFileLocation().makeAbsolute().removeLastSegments(1);
    File logsPathAsFile = logsPath.toFile();
    // get list of .log files
    File[] logFiles = logsPathAsFile.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".log");
      }
    });
    return Lists.newArrayList(logFiles);
  }
}
