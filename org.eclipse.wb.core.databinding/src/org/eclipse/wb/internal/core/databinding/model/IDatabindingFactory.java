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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.parser.DatabindingRootProcessor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This interface is contributed via extension point and used by {@link DatabindingRootProcessor}
 * for creating {@link IDatabindingsProvider} for root {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IDatabindingFactory {
	/**
	 * @return {@link IDatabindingsProvider} for given root {@link JavaInfo} or <code>null</code> if
	 *         operation for given root not supported.
	 */
	IDatabindingsProvider createProvider(JavaInfo javaInfoRoot) throws Exception;

	/**
	 * @return {@link AbstractUIPlugin} host for this factory.
	 */
	AbstractUIPlugin getPlugin();
}