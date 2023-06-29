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
package org.eclipse.wb.internal.core.xml.model.generic;

import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidator;
import org.eclipse.wb.internal.core.xml.model.association.Association;

/**
 * Configuration for {@link SimpleContainerConfigurable}.
 *
 * @author scheglov_ke
 * @coverage XML.model.generic
 */
public class SimpleContainerConfiguration {
	private final ContainerObjectValidator m_componentValidator;
	private final Association m_association;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SimpleContainerConfiguration(ContainerObjectValidator componentValidator,
			Association association) {
		m_componentValidator = componentValidator;
		m_association = association;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public ContainerObjectValidator getComponentValidator() {
		return m_componentValidator;
	}

	public Association getAssociation() {
		return m_association;
	}
}
