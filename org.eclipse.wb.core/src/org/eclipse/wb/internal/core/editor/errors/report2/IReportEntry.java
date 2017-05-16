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
