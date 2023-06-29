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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;

/**
 * Implementation of {@link IObjectPresentation} for {@link LayoutDataInfo}.
 *
 * @author lobas_av
 * @coverage swt.model.presentation
 */
public final class LayoutDataPresentation extends DefaultJavaInfoPresentation {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutDataPresentation(LayoutDataInfo layoutData) {
		super(layoutData);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObjectPresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isVisible() throws Exception {
		return false;
	}
}