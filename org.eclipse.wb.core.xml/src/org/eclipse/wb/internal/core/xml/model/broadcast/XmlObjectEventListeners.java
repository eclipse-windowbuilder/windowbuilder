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
package org.eclipse.wb.internal.core.xml.model.broadcast;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.event.AbstractListenerProperty;

import java.util.List;

/**
 * Allows subscribers contribute event properties.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public interface XmlObjectEventListeners {
  void invoke(XmlObjectInfo object, List<AbstractListenerProperty> properties) throws Exception;
}