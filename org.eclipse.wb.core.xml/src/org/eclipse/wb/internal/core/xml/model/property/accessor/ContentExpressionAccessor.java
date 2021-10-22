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
package org.eclipse.wb.internal.core.xml.model.property.accessor;

import org.eclipse.wb.internal.core.utils.xml.DocumentTextNode;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * {@link ExpressionAccessor} for content of XML element.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public final class ContentExpressionAccessor extends ExpressionAccessor {
  public static final ExpressionAccessor INSTANCE = new ContentExpressionAccessor();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ContentExpressionAccessor() {
    super(null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified(XmlObjectInfo object) throws Exception {
    return object.getElement().getTextNode() != null;
  }

  @Override
  public String getExpression(XmlObjectInfo object) {
    DocumentTextNode textNode = object.getElement().getTextNode();
    return textNode != null ? textNode.getText() : null;
  }

  @Override
  public void setExpression(XmlObjectInfo object, String expression) throws Exception {
    object.getElement().setText(expression, false);
  }
}
