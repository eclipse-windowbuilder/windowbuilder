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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables;

/**
 * Interface for provider access to abstract {@code delay} attribute.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public interface IDelayValueProvider {
	/**
	 * @return the delay current value.
	 */
	int getDelayValue();

	/**
	 * Sets delay value.
	 */
	void setDelayValue(int delayValue);
}