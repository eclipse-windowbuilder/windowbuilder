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
package org.eclipse.wb.internal.core.utils.state;

import java.util.Map;

/**
 * Provider for parameters of component, from description or instance specific.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IParametersProvider {
	/**
	 * @return the read only {@link Map} of parameters from model or description.
	 */
	Map<String, String> getParameters(Object object);

	/**
	 * @return the parameter value from model or description.
	 */
	String getParameter(Object object, String name);

	/**
	 * Checks if object has parameter with value <code>"true"</code>.
	 */
	boolean hasTrueParameter(Object object, String name);
}
