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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.swt.graphics.Image;

/**
 * Interface for visual presentation of {@link IObserveInfo} - title and image.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IObservePresentation {
  /**
   * @return the text to display for user.
   */
  String getText() throws Exception;

  /**
   * @return the text to display for user.
   */
  String getTextForBinding() throws Exception;

  /**
   * @return the image to display for user.
   */
  Image getImage() throws Exception;
}