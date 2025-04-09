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
	@Override
	public void addEntries(IErrorReport report) {
		report.addEntry(new EclipseErrorLogsReportEntry());
		report.addEntry(new ParseErrorsLogReportEntry());
	}
}
