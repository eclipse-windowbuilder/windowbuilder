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
package org.eclipse.wb.internal.core.xml.gefTree.part;

import org.eclipse.wb.core.gefTree.part.ObjectEditPart;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * {@link TreeEditPart} for {@link XmlObjectInfo}.
 *
 * @author mitin_aa
 * @coverage XML.gefTree
 */
public class XmlObjectEditPart extends ObjectEditPart {
	private final XmlObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectEditPart(XmlObjectInfo object) {
		super(object);
		m_object = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		//DoubleClickLayoutEditPolicy.install(this);TODO
	}
}
