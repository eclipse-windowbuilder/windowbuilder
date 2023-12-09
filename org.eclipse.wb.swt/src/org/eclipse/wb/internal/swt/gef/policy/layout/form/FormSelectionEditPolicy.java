/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteComplexSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.FormLayout;

/**
 * Selection policy for edit containers with {@link FormLayout}.
 *
 * @author mitin_aa
 * @coverage swt.gef.policy.form
 */
public final class FormSelectionEditPolicy<C extends IControlInfo> extends AbsoluteComplexSelectionEditPolicy<C> {
	protected final FormLayoutInfoImplAutomatic<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormSelectionEditPolicy(IFormLayoutInfo<C> layout) {
		super((IAbsoluteLayoutCommands) layout.getImpl());
		m_layout = (FormLayoutInfoImplAutomatic<C>) layout.getImpl();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Overrides
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getActionImageDescriptor(String imageName) {
		return FormLayoutInfoImplAutomatic.getImageDescriptor(imageName);
	}

	@Override
	protected ComponentAttachmentInfo getComponentAttachmentInfo(IAbstractComponentInfo widget,
			int side) throws Exception {
		return m_layout.getComponentAttachmentInfo(widget, side);
	}

	@Override
	protected void hideSelection() {
		super.hideSelection();
	}

	@Override
	protected void showSelection() {
		super.showSelection();
	}
}
