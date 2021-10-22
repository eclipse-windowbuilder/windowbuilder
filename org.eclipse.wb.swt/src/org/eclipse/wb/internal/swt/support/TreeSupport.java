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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Stub class for using SWT {@link org.eclipse.swt.widget.Tree}'s in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class TreeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // TreeItem
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>TreeItem.getBounds()</code> for given tree item.
   */
  public static Rectangle getBounds(Object treeItem) throws Exception {
    Object rectangle = ReflectionUtils.invokeMethod(treeItem, "getBounds()");
    return RectangleSupport.getRectangle(rectangle);
  }
}