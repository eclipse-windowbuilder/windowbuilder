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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoPresentationDecorateIcon {
  /**
   * This method is used to support external decoration of "default" object icon.
   *
   * @param object
   *          the {@link ObjectInfo} to decorate icon.
   * @param icon
   *          the array with single {@link Image}, listener can replace this image
   */
  void invoke(ObjectInfo object, Image[] icon) throws Exception;
}
