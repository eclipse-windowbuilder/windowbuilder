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

/**
 * Interface for extension point to provide additional report entries.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public interface IReportEntriesProvider {
	/**
	 * Adds entries into report.
	 */
	void addEntries(IErrorReport report);
}
