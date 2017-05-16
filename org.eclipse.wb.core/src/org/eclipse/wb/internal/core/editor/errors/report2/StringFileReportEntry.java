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

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Report entry for writing a string into a file.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public class StringFileReportEntry extends FileReportEntry {
  private final String m_contents;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringFileReportEntry(String name, String contents) {
    super(name);
    m_contents = contents;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FileReportEntry
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected InputStream getContents() throws Exception {
    return IOUtils.toInputStream(m_contents);
  }
}
