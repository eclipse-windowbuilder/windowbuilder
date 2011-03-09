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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.internal.swt.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;

/**
 * Provider of {@link Image} and {@link ImageDescriptor} for {@link GridLayout}.
 * 
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class GridImages {
  /**
   * @return the {@link Image} for {@link GridLayout}.
   */
  public static Image getImage(String path) {
    return Activator.getImage("info/layout/GridLayout/" + path);
  }

  /**
   * @return the {@link ImageDescriptor} for {@link GridLayout}.
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return Activator.getImageDescriptor("info/layout/GridLayout/" + path);
  }
}