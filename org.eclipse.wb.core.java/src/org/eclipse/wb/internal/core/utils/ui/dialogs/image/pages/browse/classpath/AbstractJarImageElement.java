/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;

import org.eclipse.core.runtime.IPath;

/**
 * Abstract implementation of {@link IImageElement} for single entry in jar.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractJarImageElement implements IImageElement {
	protected final JarImageContainer m_jarContainer;
	protected final IPath m_entryPath;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractJarImageElement(JarImageContainer jarContainer, IPath entryPath) {
		m_jarContainer = jarContainer;
		m_entryPath = entryPath;
	}
}
