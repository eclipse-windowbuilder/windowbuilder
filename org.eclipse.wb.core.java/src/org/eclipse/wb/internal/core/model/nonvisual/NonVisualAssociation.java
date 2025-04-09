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
package org.eclipse.wb.internal.core.model.nonvisual;

import org.eclipse.wb.core.model.association.Association;

/**
 * {@link Association} for non visual object (NVO).
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class NonVisualAssociation extends Association {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NonVisualAssociation() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean remove() throws Exception {
		NonVisualBeanInfo beanInfo = NonVisualBeanInfo.getNonVisualInfo(m_javaInfo);
		beanInfo.remove();
		return super.remove();
	}
}
