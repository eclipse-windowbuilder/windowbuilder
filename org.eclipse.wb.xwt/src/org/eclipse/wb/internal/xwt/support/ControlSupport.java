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
package org.eclipse.wb.internal.xwt.support;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * Helper for working with {@link Control}.
 *
 * @author scheglov_ke
 * @coverage XWT.support
 */
public class ControlSupport {
  /**
   * @return <code>true</code> if given {@link Widget} styles contains given style bit.
   */
  public static boolean hasStyle(Widget widget, int style) {
    return (widget.getStyle() & style) != 0;
  }

  /**
   * Performs safe {@link Widget#dispose()} operation.
   */
  public static void dispose(Widget widget) {
    if (widget == null) {
      return;
    }
    if (!widget.isDisposed()) {
      widget.dispose();
    }
  }
}