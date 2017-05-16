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
package org.eclipse.wb.internal.core.model.layout.absolute;

import org.eclipse.swt.graphics.Image;

/**
 * Interface which provides the image by relative path. See the all of the our Activator instances.
 *
 * @author mitin_aa
 */
public interface IImageProvider {
  /**
   * @return the Image instance by given path.
   */
  Image getImage(String path);
}
