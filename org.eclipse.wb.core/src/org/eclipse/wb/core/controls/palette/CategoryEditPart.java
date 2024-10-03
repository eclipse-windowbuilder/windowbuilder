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

import org.eclipse.wb.core.controls.palette.PaletteComposite.CategoryFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import java.util.List;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class CategoryEditPart extends AbstractGraphicalEditPart {
	private final PaletteComposite composite;

	@SuppressWarnings("removal")
	public CategoryEditPart(PaletteComposite composite, ICategory category) {
		this.composite = composite;
		setModel(category);
	}

	@Override
	@SuppressWarnings("removal")
	public ICategory getModel() {
		return (ICategory) super.getModel();
	}

	@Override
	public CategoryFigure getFigure() {
		return (CategoryFigure) super.getFigure();
	}

	@Override
	@SuppressWarnings("removal")
	protected List<? extends IEntry> getModelChildren() {
		return getModel().getEntries();
	}

	@Override
	protected IFigure createFigure() {
		return composite.new CategoryFigure(getModel());
	}

	@Override
	protected void createEditPolicies() {
		// nothing to do
	}
}
