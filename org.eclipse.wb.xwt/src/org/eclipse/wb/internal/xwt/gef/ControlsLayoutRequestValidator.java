/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.gef;

import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

/**
 * {@link ILayoutRequestValidator} for {@link ControlInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gef
 */
public final class ControlsLayoutRequestValidator {
	public static final ILayoutRequestValidator INSTANCE =
			LayoutRequestValidators.modelType(ControlInfo.class);
}
