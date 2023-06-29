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

/**
 * Listener for {@link XmlObjectInfo} events.
 * <p>
 * Used to get notified after object of {@link XmlObjectInfo} becomes set using
 * {@link XmlObjectInfo#setObject(Object)}
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public interface XmlObjectSetObjectAfter {
	void invoke(XmlObjectInfo target, Object o) throws Exception;
}