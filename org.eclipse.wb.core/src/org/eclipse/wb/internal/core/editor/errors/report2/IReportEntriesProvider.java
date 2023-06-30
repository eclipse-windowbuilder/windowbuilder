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
