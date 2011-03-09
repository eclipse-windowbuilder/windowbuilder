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
package org.eclipse.wb.internal.core.editor.errors.report;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.IOUtils2;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Various utils for {@link IReportInfo} instances.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public class ReportInfoUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if all files excluded.
   */
  public static boolean isAllFilesExcluded(Map<String, Boolean> filesMap) {
    for (boolean included : filesMap.values()) {
      if (included) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check added files for availability and excludes unavailable to read. Shows a message to the
   * user.
   */
  public static void checkFiles(Map<String, Boolean> filesMap) {
    Map<String, Boolean> filesCopy = Collections.unmodifiableMap(filesMap);
    Set<Entry<String, Boolean>> entrySet = filesCopy.entrySet();
    for (Entry<String, Boolean> fileEntry : entrySet) {
      String fileName = fileEntry.getKey();
      // may be already excluded
      if (!fileEntry.getValue()) {
        continue;
      }
      // try to open and read file
      try {
        IOUtils2.readBytes(new File(fileName));
      } catch (Throwable e) {
        // exclude file
        filesMap.put(fileName, false);
        // show error
        String message =
            MessageFormat.format(
                "Error opening file {0} to read. This file would be excluded from report.",
                fileName);
        ErrorDialog.openError(
            DesignerPlugin.getShell(),
            "Open file error",
            message,
            new Status(IStatus.ERROR,
                DesignerPlugin.PLUGIN_ID,
                0,
                "The file may be moved, offline or unaccessible due to invalid permissions.",
                e));
      }
    }
  }
}
