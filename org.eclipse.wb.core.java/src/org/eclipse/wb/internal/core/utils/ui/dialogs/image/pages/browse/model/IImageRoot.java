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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.AbstractBrowseImagePage;

/**
 * The root for browsing using {@link AbstractBrowseImagePage}.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public interface IImageRoot {
  /**
   * @return the top level {@link IImageElement}'s.
   */
  IImageElement[] elements();

  /**
   * Allows {@link IImageRoot} dispose any resources allocated for browsing.
   */
  void dispose();

  /**
   * @return the array of models that should be expanded/selected to show object specified by given
   *         data.
   */
  Object[] getSelectionPath(Object data);
}
