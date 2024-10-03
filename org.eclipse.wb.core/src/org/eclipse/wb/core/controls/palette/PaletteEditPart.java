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

import org.eclipse.wb.core.controls.palette.PaletteComposite.PaletteFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import java.util.List;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class PaletteEditPart extends AbstractGraphicalEditPart {
	private final PaletteComposite composite;

	@SuppressWarnings("removal")
	public PaletteEditPart(PaletteComposite composite, IPalette category) {
		this.composite = composite;
		setModel(category);
	}

	@Override
	@SuppressWarnings("removal")
	public IPalette getModel() {
		return (IPalette) super.getModel();
	}

	@Override
	public PaletteFigure getFigure() {
		return (PaletteFigure) super.getFigure();
	}

	@Override
	@SuppressWarnings("removal")
	protected List<? extends ICategory> getModelChildren() {
		return getModel().getCategories();
	}

	@Override
	protected IFigure createFigure() {
		for (IFigure category : List.copyOf(composite.getPaletteFigure().getChildren())) {
			composite.getPaletteFigure().remove(category);
		}
		return composite.getPaletteFigure();
	}

	@Override
	protected void createEditPolicies() {
		// nothing to do
	}
}
