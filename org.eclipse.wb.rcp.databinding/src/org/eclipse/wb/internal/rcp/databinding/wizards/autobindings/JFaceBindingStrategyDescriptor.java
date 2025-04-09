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
package org.eclipse.wb.internal.rcp.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AbstractDescriptor;

/**
 * JFace UpdateStrategy descriptor.
 *
 * @author lobas_av
 * @coverage bindings.rcp.wizard.auto
 */
public final class JFaceBindingStrategyDescriptor extends AbstractDescriptor {
	private String m_targetStrategyCode;
	private String m_modelStrategyCode;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the source code for "target to model" strategy.
	 */
	public String getTargetStrategyCode() {
		return m_targetStrategyCode;
	}

	/**
	 * Sets the source code for "target to model" strategy.
	 */
	public void setTargetStrategyCode(String code) {
		m_targetStrategyCode = code;
	}

	/**
	 * @return the source code for "model to target" strategy.
	 */
	public String getModelStrategyCode() {
		return m_modelStrategyCode;
	}

	/**
	 * Sets the source code for "model to target" strategy.
	 */
	public void setModelStrategyCode(String code) {
		m_modelStrategyCode = code;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Default
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isDefault(Object property) {
		return true;
	}
}