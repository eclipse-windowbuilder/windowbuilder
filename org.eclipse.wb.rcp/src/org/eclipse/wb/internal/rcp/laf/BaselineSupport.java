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
package org.eclipse.wb.internal.rcp.laf;

import org.eclipse.wb.internal.core.laf.IBaselineSupport;
import org.eclipse.wb.swt.widgets.baseline.Baseline;

import org.eclipse.swt.widgets.Control;

/**
 * Baseline support for SWT widgets.
 *
 * @author mitin_aa
 */
public class BaselineSupport implements IBaselineSupport {
	@Override
	public int getBaseline(Object component) {
		if (!(component instanceof Control)) {
			return NO_BASELINE;
		}
		return Baseline.getBaseline((Control) component);
	}
}
