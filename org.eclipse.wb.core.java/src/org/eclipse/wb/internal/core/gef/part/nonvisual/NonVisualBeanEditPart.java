/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.gef.part.nonvisual;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.graphical.DesignEditPart;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link EditPart} for <i>non-visual bean</i> model.
 *
 * @author lobas_av
 * @coverage core.gef.nonvisual
 */
public final class NonVisualBeanEditPart extends DesignEditPart {
	private final NonVisualBeanInfo m_beanInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NonVisualBeanEditPart(JavaInfo javaInfo) {
		m_beanInfo = NonVisualBeanInfo.getNonVisualInfo(javaInfo);
		setModel(javaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public NonVisualBeanInfo getNonVisualInfo() {
		return m_beanInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableSelectionEditPolicy());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IFigure createFigure() {
		ImageDescriptor imageDescriptor = ObjectInfo.getImageDescriptor(m_beanInfo.getJavaInfo());
		return new BeanFigure(imageDescriptor);
	}

	@Override
	protected void refreshVisuals() {
		String text = ObjectInfo.getText(m_beanInfo.getJavaInfo());
		BeanFigure figure = (BeanFigure) getFigure();
		figure.update(text, m_beanInfo.getLocation());
	}
}