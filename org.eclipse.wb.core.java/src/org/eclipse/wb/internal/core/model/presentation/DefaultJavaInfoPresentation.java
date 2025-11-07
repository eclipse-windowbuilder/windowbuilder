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
package org.eclipse.wb.internal.core.model.presentation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.factory.AbstractExplicitFactoryCreationSupport;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Default {@link IObjectPresentation} for {@link JavaInfo}
 *
 * @author scheglov_ke
 * @coverage core.model.presentation
 */
public class DefaultJavaInfoPresentation extends DefaultObjectPresentation {
	protected final JavaInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DefaultJavaInfoPresentation(JavaInfo javaInfo) {
		super(javaInfo);
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObjectPresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		// for factory try to get "factory method" specific icon
		if (m_javaInfo.getCreationSupport() instanceof AbstractExplicitFactoryCreationSupport) {
			AbstractExplicitFactoryCreationSupport factoryCreationSupport =
					(AbstractExplicitFactoryCreationSupport) m_javaInfo.getCreationSupport();
			ImageDescriptor icon = factoryCreationSupport.getDescription().getIcon();
			if (icon != null) {
				return icon;
			}
		}
		// by default use "component type" specific icon
		return m_javaInfo.getDescription().getIcon();
	}

	@Override
	public String getText() throws Exception {
		return m_javaInfo.getVariableSupport().getTitle();
	}
}
