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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public abstract class CustomTooltipProvider implements ICustomTooltipProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// ICustomTooltipProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Control createTooltipControl(Composite parent, ICustomTooltipSite site, Figure figure) {
		FigureCanvas canvas = new FigureCanvas(parent, SWT.NONE);
		GridDataFactory.create(canvas).fill().grab();
		canvas.addListener(SWT.MouseDown, site.getHideListener());
		canvas.addListener(SWT.MouseExit, site.getHideListener());
		//
		RootFigure rootFigure = canvas.getRootFigure();
		rootFigure.setForeground(parent.getForeground());
		rootFigure.setBackground(parent.getBackground());
		//
		Layer layer = new Layer("Tooltip");
		layer.add(createTooltipFigure(figure));
		rootFigure.addLayer(layer);
		//
		return canvas;
	}

	@Override
	public void show(Shell shell) {
		shell.setVisible(true);
	}

	protected abstract Figure createTooltipFigure(Figure hostFigure);
}