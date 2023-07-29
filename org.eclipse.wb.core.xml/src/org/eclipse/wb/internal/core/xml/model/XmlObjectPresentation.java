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
package org.eclipse.wb.internal.core.xml.model;

import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Presentation for {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public class XmlObjectPresentation extends DefaultObjectPresentation {
	private final XmlObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectPresentation(XmlObjectInfo object) {
		super(object);
		m_object = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() throws Exception {
		return new ImageImageDescriptor(m_object.getDescription().getIcon());
	}

	@Override
	public String getText() throws Exception {
		return m_object.getCreationSupport().getTitle();
	}
}
