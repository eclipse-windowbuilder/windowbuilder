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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * {@link LabelProvider} for {@link ObservePropertyAdapter}.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class ObservePropertyAdapterLabelProvider extends LabelProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// LabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Object element) {
		ObservePropertyAdapter adapter = (ObservePropertyAdapter) element;
		return adapter.getName();
	}

	@Override
	public Image getImage(Object element) {
		try {
			ObservePropertyAdapter adapter = (ObservePropertyAdapter) element;
			return adapter.getObserve().getPresentation().getImage();
		} catch (Throwable e) {
			return null;
		}
	}
}