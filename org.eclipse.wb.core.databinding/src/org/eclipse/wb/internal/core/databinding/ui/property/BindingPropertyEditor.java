/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.databinding.ui.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;

/**
 * Editor for single binding.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public final class BindingPropertyEditor extends TextDialogPropertyEditor {
	public static final BindingPropertyEditor EDITOR = new BindingPropertyEditor();

	////////////////////////////////////////////////////////////////////////////
	//
	// TextDisplayPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		AbstractBindingProperty bindingProperty = (AbstractBindingProperty) property;
		return bindingProperty.getText();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TextDialogPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openDialog(Property property) throws Exception {
		AbstractBindingProperty bindingProperty = (AbstractBindingProperty) property;
		bindingProperty.editBinding();
	}
}