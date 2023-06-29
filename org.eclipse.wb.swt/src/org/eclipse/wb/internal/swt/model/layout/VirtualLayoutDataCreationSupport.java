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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Implementation of {@link CreationSupport} for virtual {@link LayoutDataInfo}.
 *
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class VirtualLayoutDataCreationSupport extends CreationSupport
implements
IImplicitCreationSupport {
	private final ControlInfo m_control;
	private final Object m_dataObject;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public VirtualLayoutDataCreationSupport(ControlInfo control, Object dataObject) {
		m_control = control;
		m_dataObject = dataObject;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setJavaInfo(JavaInfo javaInfo) throws Exception {
		super.setJavaInfo(javaInfo);
		m_javaInfo.setObject(m_dataObject);
		m_control.addBroadcastListener(new JavaInfoSetObjectAfter() {
			@Override
			public void invoke(JavaInfo target, Object object) throws Exception {
				// check, may be this creation support is not active
				if (m_javaInfo.getCreationSupport() != VirtualLayoutDataCreationSupport.this) {
					m_control.removeBroadcastListener(this);
					return;
				}
				// OK, check for control
				if (target == m_control) {
					m_javaInfo.setObject(m_dataObject);
				}
			}
		});
	}

	@Override
	public boolean isJavaInfo(ASTNode node) {
		return false;
	}

	@Override
	public ASTNode getNode() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void delete() throws Exception {
		JavaInfoUtils.deleteJavaInfo(m_javaInfo, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		Class<?> layoutDataClass = getComponentClass();
		return "virtual-layout_data: " + layoutDataClass.getName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardImplicitCreationSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IClipboardImplicitCreationSupport getImplicitClipboard() {
		return null;
	}
}