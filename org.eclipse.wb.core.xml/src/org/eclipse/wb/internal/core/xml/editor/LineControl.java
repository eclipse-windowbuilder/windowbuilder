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
package org.eclipse.wb.internal.core.xml.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * Horizontal or vertical line, line {@link Label} separator, but without highlight.
 *
 * @author scheglov_ke
 * @coverage XML.editor
 */
public final class LineControl extends Canvas {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LineControl(Composite parent, int style) {
		super(parent, style);
		addListener(SWT.Paint, new Listener() {
			@Override
			public void handleEvent(Event event) {
				GC gc = event.gc;
				gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
				if (isHorizontal()) {
					gc.drawLine(0, 0, getSize().x, 0);
				} else {
					gc.drawLine(0, 0, 0, getSize().y);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Control
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		if (isHorizontal()) {
			return new Point(wHint, 1);
		} else {
			return new Point(1, hHint);
		}
	}

	private boolean isHorizontal() {
		return (getStyle() & SWT.HORIZONTAL) == SWT.HORIZONTAL;
	}
}