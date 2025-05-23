/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.custom.CCombo;

import java.util.ArrayList;
import java.util.List;

/**
 * Property editor allowing to select one control within list of controls on the same parent (except
 * itself).
 *
 * @author mitin_aa
 * @coverage swt.property.editor
 */
public final class ControlSelectionPropertyEditor extends AbstractComboPropertyEditor {
	private final List<ControlInfo> m_controls = new ArrayList<>();

	@Override
	protected void addItems(Property property, CCombo combo) throws Exception {
		FormAttachmentInfo formAttachment = getAttachment(property);
		ControlInfo thisControl = (ControlInfo) formAttachment.getParent().getParent();
		CompositeInfo compositeInfo = (CompositeInfo) thisControl.getParent();
		List<ControlInfo> childrenControls = compositeInfo.getChildrenControls();
		for (ControlInfo controlInfo : childrenControls) {
			if (controlInfo != thisControl) {
				combo.add(controlInfo.getPresentation().getText());
				m_controls.add(controlInfo);
			}
		}
	}

	@Override
	protected void selectItem(Property property, CCombo combo) throws Exception {
		combo.setText(getText(property));
	}

	@Override
	protected void toPropertyEx(Property property, CCombo combo, int index) throws Exception {
		ControlInfo controlInfo = m_controls.get(index);
		// set expression would further be caught in FormAttachment
		((GenericPropertyImpl) property).setExpression("", controlInfo);
	}

	@Override
	protected String getText(Property property) throws Exception {
		Object control = property.getValue();
		if (control != null) {
			FormAttachmentInfo attachment = getAttachment(property);
			JavaInfo controlInfo = attachment.getRootJava().getChildByObject(control);
			if (controlInfo != null) {
				return controlInfo.getPresentation().getText();
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc/Helpers
	//
	////////////////////////////////////////////////////////////////////////////
	private FormAttachmentInfo getAttachment(Property property) {
		GenericProperty genericProperty = (GenericProperty) property;
		FormAttachmentInfo formAttachment = (FormAttachmentInfo) genericProperty.getJavaInfo();
		return formAttachment;
	}
}
