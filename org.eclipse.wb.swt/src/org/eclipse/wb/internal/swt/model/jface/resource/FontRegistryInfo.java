/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.resource.FontRegistry;

/**
 * Implementation model for {@link org.eclipse.jface.resource.FontRegistry}.
 *
 * @author lobas_av
 * @coverage swt.model.jface
 */
public final class FontRegistryInfo extends ResourceRegistryInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FontRegistryInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	@Override
	public Runnable getDisposeRunnable() {
		if (getObject() != null) {
			return (Runnable) ReflectionUtils.getFieldObject(getObject(), "displayRunnable");
		}
		return null;
	}

	@Override
	public FontRegistry getResourceRegistry() {
		return (FontRegistry) super.getObject();
	}
}