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
package org.eclipse.wb.internal.swt.gef;

import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link ILayoutRequestValidator} for {@link ControlInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef
 */
public final class ControlsLayoutRequestValidator {
	public static final ILayoutRequestValidator INSTANCE =
			LayoutRequestValidators.componentType("org.eclipse.swt.widgets.Control");
}
