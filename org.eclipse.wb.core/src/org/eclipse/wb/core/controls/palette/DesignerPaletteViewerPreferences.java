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

import org.eclipse.gef.ui.palette.DefaultPaletteViewerPreferences;

public final class DesignerPaletteViewerPreferences extends DefaultPaletteViewerPreferences {
	public DesignerPaletteViewerPreferences() {
		setAutoCollapseSetting(COLLAPSE_NEVER);
	}
}
