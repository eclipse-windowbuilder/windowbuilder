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
package org.eclipse.wb.core.model;

import java.util.List;

/**
 * Processor for root {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IRootProcessor {
	/**
	 * Last step before building tree {@link JavaInfo} complete.
	 *
	 * @param root
	 *          the {@link JavaInfo} that is root of build component hierarchy.
	 * @param components
	 *          all components, bound and not bound, that were created during parsing.
	 */
	void process(JavaInfo root, List<JavaInfo> components) throws Exception;
}
