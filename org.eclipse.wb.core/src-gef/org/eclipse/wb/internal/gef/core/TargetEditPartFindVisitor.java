/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.TargetFigureFindVisitor;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPartViewer;

/**
 * This class use to find {@link EditPart} under mouse.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class TargetEditPartFindVisitor extends TargetFigureFindVisitor {
	private final EditPartViewer m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TargetEditPartFindVisitor(FigureCanvas canvas, int x, int y, EditPartViewer viewer) {
		super(canvas, x, y);
		m_viewer = viewer;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Return mouse target {@link EditPart}.
	 */
	public EditPart getTargetEditPart() {
		return extractEditPart(getTargetFigure());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Extract {@link EditPart} from given {@link IFigure}.
	 */
	protected EditPart extractEditPart(IFigure figure) {
		EditPart editPart = null;
		while (editPart == null && figure != null) {
			editPart = (EditPart) m_viewer.getVisualPartMap().get(figure);
			figure = figure.getParent();
		}
		return editPart;
	}
}