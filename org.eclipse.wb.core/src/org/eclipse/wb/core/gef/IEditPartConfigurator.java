/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.core.gef;

import org.eclipse.wb.internal.core.gef.EditPartFactory;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

/**
 * Implementations of {@link IEditPartConfigurator} are used by {@link EditPartFactory} to configure
 * any created {@link EditPart}.
 * <p>
 * Now {@link IEditPartConfigurator}'s are used to add some {@link EditPolicy} on {@link EditPart},
 * even if {@link EditPart} itself does not know anything about these {@link EditPolicy}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public interface IEditPartConfigurator {
	/**
	 * Configures given {@link EditPart}.
	 */
	void configure(EditPart context, org.eclipse.wb.gef.core.EditPart editPart);
}
