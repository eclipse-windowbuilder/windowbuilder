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
package org.eclipse.wb.internal.swing.java6.laf;

import org.eclipse.wb.internal.core.laf.IBaselineSupport;

import java.awt.Component;
import java.awt.Dimension;

/**
 * Baseline support for components using Java6 methods.
 *
 * @author mitin_aa
 */
public class BaselineSupport implements IBaselineSupport {
	@Override
	public int getBaseline(Object component) {
		if (!(component instanceof Component comp)) {
			return NO_BASELINE;
		}
		Dimension size = comp.getSize();
		return comp.getBaseline(size.width, size.height);
	}
}
