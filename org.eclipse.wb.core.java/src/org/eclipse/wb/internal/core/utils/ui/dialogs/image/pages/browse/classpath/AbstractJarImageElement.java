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
