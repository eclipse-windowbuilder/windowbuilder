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
package org.eclipse.wb.internal.xwt.parser;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * Broadcast used to notify {@link XwtParser} that {@link XmlObjectInfo} was created and should be
 * bound to specified {@link DocumentElement}.
 * 
 * @author scheglov_ke
 * @coverage XWT.parser
 */
public interface XwtParserBindToElement {
  void invoke(XmlObjectInfo object, DocumentElement element);
}
