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

/**
 * Common interface for {@link ClasspathImageRoot} top level elements.
 * 
 * @author scheglov_ke
 */
interface IClasspathImageContainer {
  /**
   * @return <code>true</code> if this jar does not have any images.
   */
  boolean isEmpty();

  /**
   * Disposes any allocated resources.
   */
  void dispose();
}
