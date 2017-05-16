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
 * Empty implementation of {@link QHandler}.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public class QHandlerAdapter implements QHandler {
  public void startDocument() throws Exception {
  }

  public void endDocument() throws Exception {
  }

  public void startElement(int offset,
      int length,
      String tag,
      Map<String, String> attributes,
      List<QAttribute> attrList,
      boolean closed) throws Exception {
  }

  public void endElement(int offset, int endOffset, String tag) throws Exception {
  }

  public void text(String text, boolean isCDATA) throws Exception {
  }
}
