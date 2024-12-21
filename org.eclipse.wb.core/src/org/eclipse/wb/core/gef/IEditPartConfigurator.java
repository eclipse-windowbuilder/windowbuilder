/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.gef;

import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.core.gef.EditPartFactory;

import org.eclipse.gef.EditPart;

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
