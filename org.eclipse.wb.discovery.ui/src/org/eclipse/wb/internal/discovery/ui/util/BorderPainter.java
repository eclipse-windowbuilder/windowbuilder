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

package org.eclipse.wb.internal.discovery.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * Acts as a visual separator between two components.
 */
public class BorderPainter implements PaintListener {
  
  public void paintControl(PaintEvent e) {
    Composite composite = (Composite) e.widget;
    Rectangle bounds = composite.getBounds();
    GC gc = e.gc;
    gc.setLineStyle(SWT.LINE_DOT);
    gc.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
  }
  
}
