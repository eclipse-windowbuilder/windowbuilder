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
package org.eclipse.wb.internal.core.xml.model.utils;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectResolveTag;

/**
 * Interface for registering {@link XmlObjectResolveTag} broadcasts.
 *
 * @author scheglov_ke
 * @coverage XML.model.utils
 */
public interface TagResolverProvider {
	/**
	 * Analyzes given {@link XmlObjectInfo} and may be adds {@link XmlObjectResolveTag} broadcast.
	 */
	void register(XmlObjectInfo rootObject) throws Exception;
}
