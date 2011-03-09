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

import org.eclipse.wb.internal.core.editor.errors.BrowserMessageDialog;
import org.eclipse.wb.internal.core.utils.XmlWriter;

/**
 * Report part should represent itself as XML and HTML.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public interface IReportInfo {
  /**
   * Write itself into xml file using given {@link XmlWriter}. This method is responsible for
   * {@link XmlWriter} to stay consistent, i.e., any open tag should be closed and so on.
   * 
   * @throws Exception
   */
  void writeXML(XmlWriter xmlWriter) throws Exception;

  /**
   * @return the html part to be viewed by user in {@link BrowserMessageDialog}.
   */
  String getHTML();
}
