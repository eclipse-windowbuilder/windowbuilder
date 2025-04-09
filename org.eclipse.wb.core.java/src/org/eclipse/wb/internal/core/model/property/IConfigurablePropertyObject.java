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
package org.eclipse.wb.internal.core.model.property;

import org.eclipse.wb.internal.core.utils.state.EditorState;

import java.util.Map;

/**
 * Extends object with ability to be configured.
 *
 * @author scheglov_ke
 */
public interface IConfigurablePropertyObject {
	/**
	 * Configures object with given {@link Map} of parameters.
	 */
	void configure(EditorState state, Map<String, Object> parameters) throws Exception;
}
