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
package org.eclipse.wb.internal.core.utils.xml.parser;

import java.util.List;
import java.util.Map;

/**
 * Handler for event in {@link QParser}.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public interface QHandler {
  /**
   * Notifies that top level element starts.
   */
  void startDocument() throws Exception;

  /**
   * Notifies that top level element ends.
   */
  void endDocument() throws Exception;

  /**
   * Notifies that element starts.
   *
   * @param offset
   *          the offset of <code>"&lt;"</code> character.
   * @param length
   *          the length of element, including <code>"&gt;"</code> character.
   * @param tag
   *          the name of tag.
   * @param attributes
   *          simple {@link Map} of attribute names and their values.
   * @param attrList
   *          the {@link List} of information about each attribute.
   * @param closed
   *          is <code>true</code> if this element is closed directly at start, i.e. ends with
   *          <code>"/&gt;"</code>.
   */
  void startElement(int offset,
      int length,
      String tag,
      Map<String, String> attributes,
      List<QAttribute> attrList,
      boolean closed) throws Exception;

  /**
   * Notifies that end of element was reached.
   *
   * @param offset
   *          the offset of <code>"&lt;"</code> character. May be <code>-1</code> if element closed
   *          directly at start.
   * @param endOffset
   *          the offset after <code>"/&gt;"</code> characters.
   * @param tag
   *          the name of tag.
   */
  void endElement(int offset, int endOffset, String tag) throws Exception;

  /**
   * Notifies that some text between elements was processed.
   *
   * @param text
   *          the text.
   * @param isCDATA
   *          is <code>true</code> if text was enclosed into <code>CDATA</code> section.
   */
  void text(String text, boolean isCDATA) throws Exception;
}
