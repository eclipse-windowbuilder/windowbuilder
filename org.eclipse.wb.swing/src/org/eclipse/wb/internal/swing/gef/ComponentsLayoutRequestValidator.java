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
package org.eclipse.wb.internal.swing.gef;

import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.gef.policy.validator.ModelClassLayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

/**
 * {@link ILayoutRequestValidator} for {@link ComponentInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef
 */
public final class ComponentsLayoutRequestValidator {
	public static final ILayoutRequestValidator INSTANCE =
			LayoutRequestValidators.modelType(ComponentInfo.class);
	public static final ILayoutRequestValidator INSTANCE_EXT =
			LayoutRequestValidators.finalize(LayoutRequestValidators.and(
					MenuLayoutRequestValidator.INSTANCE,
					new ModelClassLayoutRequestValidator(ComponentInfo.class)));
}