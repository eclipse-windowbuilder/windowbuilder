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
package org.eclipse.wb.internal.swt.model.jface.resource;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.resource.ColorRegistry;

/**
 * Implementation model for {@link org.eclipse.jface.resource.ColorRegistry}.
 *
 * @author lobas_av
 * @coverage swt.model.jface
 */
public final class ColorRegistryInfo extends ResourceRegistryInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColorRegistryInfo(AstEditor editor,
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
	public ColorRegistry getResourceRegistry() {
		return (ColorRegistry) super.getObject();
	}
}