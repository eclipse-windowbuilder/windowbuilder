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

/**
 * Container for children {@link IImageElement}'s.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public interface IImageContainer extends IImageElement {
  /**
   * @return the children {@link IImageElement}'s.
   */
  IImageElement[] elements();
}
