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

import org.eclipse.wb.internal.core.editor.errors.report2.IErrorReport;
import org.eclipse.wb.internal.core.editor.errors.report2.IReportEntriesProvider;

/**
 * Provider for Eclipse .log report entry and parse error log entries.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class LogsReportEntriesProvider implements IReportEntriesProvider {
  public void addEntries(IErrorReport report) {
    report.addEntry(new EclipseErrorLogsReportEntry());
    report.addEntry(new ParseErrorsLogReportEntry());
  }
}
