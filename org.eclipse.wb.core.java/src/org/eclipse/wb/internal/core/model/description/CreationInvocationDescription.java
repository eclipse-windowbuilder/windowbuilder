/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Description for adding {@link MethodInvocation} during creation.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class CreationInvocationDescription extends AbstractDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Signature
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_signature;

	public String getSignature() {
		return m_signature;
	}

	public void setSignature(String signature) {
		m_signature = signature;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Arguments
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_arguments;

	public String getArguments() {
		return m_arguments;
	}

	public void setArguments(String arguments) {
		m_arguments = arguments;
	}
}
