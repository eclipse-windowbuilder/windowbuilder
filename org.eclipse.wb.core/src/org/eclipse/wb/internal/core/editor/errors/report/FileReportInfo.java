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

import org.eclipse.wb.internal.core.utils.XmlWriter;

import java.io.OutputStream;

/**
 * Generates mimed (possibly zipped) file from various sources.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public abstract class FileReportInfo implements IReportInfo {
  private static String XML_ATTR_FILENAME = "file-name";

  ////////////////////////////////////////////////////////////////////////////
  //
  // xml related
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name of file to include into "file-name" attribute.
   */
  protected abstract String getFileNameAttribute();

  /**
   * @return the xml tag name to create xml part.
   */
  protected String getXMLTagName() {
    return "attachment";
  }

  /**
   * Encode (and possibly zip) file data.
   * 
   * @param outStream
   *          the {@link OutputStream} to encode into. This method shouldn't close the stream.
   * @throws Exception
   */
  protected abstract void encode(OutputStream outStream) throws Exception;

  /**
   * @return <code>true</code> it this {@link IReportInfo} have data to be included into xml.
   */
  protected boolean hasData() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public void writeXML(XmlWriter xmlWriter) throws Exception {
    if (!hasData()) {
      return;
    }
    xmlWriter.beginTag(getXMLTagName());
    xmlWriter.writeAttribute(XML_ATTR_FILENAME, getFileNameAttribute());
    xmlWriter.endTag();
    OutputStream outputStream = xmlWriter.streamCDATA();
    try {
      encode(outputStream);
    } finally {
      outputStream.close();
    }
    xmlWriter.closeTag();
  }

  public String getHTML() {
    return "<p>" + getFileNameAttribute() + "</p>";
  }
}
