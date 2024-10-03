/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.wb.core.controls.palette.PaletteComposite.EntryFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class EntryEditPart extends AbstractGraphicalEditPart {
	private final PaletteComposite composite;

	@SuppressWarnings("removal")
	public EntryEditPart(PaletteComposite composite, IEntry category) {
		this.composite = composite;
		setModel(category);
	}

	@Override
	@SuppressWarnings("removal")
	public IEntry getModel() {
		return (IEntry) super.getModel();
	}

	@Override
	public EntryFigure getFigure() {
		return (EntryFigure) super.getFigure();
	}

	@Override
	protected IFigure createFigure() {
		return composite.new EntryFigure(getModel());
	}

	@Override
	protected void createEditPolicies() {
		// nothing to do
	}
}
