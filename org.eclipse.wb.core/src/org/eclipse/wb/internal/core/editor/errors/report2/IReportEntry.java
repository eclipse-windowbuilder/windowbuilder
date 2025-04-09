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

import java.util.zip.ZipOutputStream;

/**
 * Interface for error reporting using zipped report files.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public interface IReportEntry {
	/**
	 * Writes the contents into given Zip stream.
	 */
	void write(ZipOutputStream zips) throws Exception;
}
