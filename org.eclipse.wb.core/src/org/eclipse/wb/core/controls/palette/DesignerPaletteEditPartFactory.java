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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.palette.PaletteEditPartFactory;

public class DesignerPaletteEditPartFactory extends PaletteEditPartFactory {
	private final PaletteComposite composite;

	public DesignerPaletteEditPartFactory(PaletteComposite composite) {
		this.composite = composite;
	}

	@Override
	@SuppressWarnings("removal")
	protected EditPart createDrawerEditPart(EditPart parentEditPart, Object model) {
		return new CategoryEditPart(composite, (ICategory) model);
	}

	@Override
	@SuppressWarnings("removal")
	protected EditPart createEntryEditPart(EditPart parentEditPart, Object model) {
		return new EntryEditPart(composite, (IEntry) model);
	}

	@Override
	@SuppressWarnings("removal")
	protected EditPart createMainPaletteEditPart(EditPart parentEditPart, Object model) {
		return new PaletteEditPart(composite, (IPalette) model);
	}
}
