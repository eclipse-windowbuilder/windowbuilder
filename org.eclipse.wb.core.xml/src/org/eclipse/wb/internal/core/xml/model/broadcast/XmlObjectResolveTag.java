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
 * Listener for preparing namespace and tag for {@link Class} name.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public interface XmlObjectResolveTag {
	/**
	 * Fills namespace and tag.
	 *
	 * @param object
	 *          some {@link XmlObjectInfo} in hierarchy.
	 * @param clazz
	 *          the {@link Class} to create namespace/tag for.
	 * @param namespace
	 *          the array with single element, initially <code>null</code>.
	 * @param tag
	 *          the array with single element, initially <code>null</code>.
	 */
	void invoke(XmlObjectInfo object, Class<?> claz, String[] namespace, String[] tag)
			throws Exception;
}