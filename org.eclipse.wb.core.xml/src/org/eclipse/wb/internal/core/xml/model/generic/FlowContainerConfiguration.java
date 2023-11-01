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
package org.eclipse.wb.internal.core.xml.model.generic;

import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidator;
import org.eclipse.wb.internal.core.xml.model.association.Association;

import java.util.function.Predicate;

/**
 * Configuration for {@link FlowContainerConfigurable}.
 *
 * @author scheglov_ke
 * @coverage XML.model.generic
 */
public class FlowContainerConfiguration {
	private final Predicate<Object> m_horizontalPredicate;
	private final Predicate<Object> m_rtlPredicate;
	private final Association m_association;
	private final ContainerObjectValidator m_componentValidator;
	private final ContainerObjectValidator m_referenceValidator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FlowContainerConfiguration(Predicate<Object> horizontalPredicate,
			Predicate<Object> rtlPredicate,
			Association association,
			ContainerObjectValidator componentValidator,
			ContainerObjectValidator referenceValidator) {
		m_horizontalPredicate = horizontalPredicate;
		m_rtlPredicate = rtlPredicate;
		m_association = association;
		m_componentValidator = componentValidator;
		m_referenceValidator = referenceValidator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public Predicate<Object> getHorizontalPredicate() {
		return m_horizontalPredicate;
	}

	public Predicate<Object> getRtlPredicate() {
		return m_rtlPredicate;
	}

	public Association getAssociation() {
		return m_association;
	}

	public ContainerObjectValidator getComponentValidator() {
		return m_componentValidator;
	}

	public ContainerObjectValidator getReferenceValidator() {
		return m_referenceValidator;
	}
}
