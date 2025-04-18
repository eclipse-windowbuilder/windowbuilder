/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.databinding.model.presentation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link IObservePresentation} for presentation {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public class JavaInfoObservePresentation implements IObservePresentation {
	protected ObjectInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfoObservePresentation(ObjectInfo javaInfo) {
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setJavaInfo(ObjectInfo javaInfo) {
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText() throws Exception {
		return ObjectInfo.getText(m_javaInfo);
	}

	@Override
	public String getTextForBinding() throws Exception {
		return m_javaInfo.getPresentation().getText();
	}

	@Override
	public ImageDescriptor getImageDescriptor() throws Exception {
		return ExecutionUtils.runObjectLog(() -> ObjectInfo.getImageDescriptor(m_javaInfo), null);
	}
}