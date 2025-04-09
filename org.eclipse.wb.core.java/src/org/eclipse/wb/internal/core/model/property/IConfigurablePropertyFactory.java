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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.ConfigurablePropertyDescription;

/**
 * Factory for creating configurable {@link Property}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public interface IConfigurablePropertyFactory {
	/**
	 * @return the {@link Property} created/configured according the given
	 *         {@link ConfigurablePropertyDescription} .
	 */
	Property create(JavaInfo javaInfo, ConfigurablePropertyDescription description) throws Exception;
}
