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
package org.eclipse.wb.internal.core.xml.model.property.converter;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * {@link ExpressionConverter} converts property value into XML attribute value.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public abstract class ExpressionConverter {
  /**
   * @return the value of XML attribute with given value.
   */
  public abstract String toSource(XmlObjectInfo object, Object value) throws Exception;
}
