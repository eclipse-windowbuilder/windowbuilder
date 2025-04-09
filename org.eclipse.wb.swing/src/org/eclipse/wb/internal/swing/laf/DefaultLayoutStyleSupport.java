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
package org.eclipse.wb.internal.swing.laf;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;

/**
 * Default LayoutStyle support implementation.
 *
 * @author mitin_aa
 * @coverage swing.laf
 */
public final class DefaultLayoutStyleSupport implements ILayoutStyleSupport {
	// constants
	public static final int DEFAULT_PREFERRED_GAP = 6;
	public static final int DEFAULT_CONTAINER_GAP = 10;

	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutStyleSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setLayoutStyle(LookAndFeel laf) {
		// do nothing
	}

	@Override
	public int getContainerGap(JComponent component, int position, Container parent) {
		return DEFAULT_CONTAINER_GAP;
	}

	@Override
	public int getPreferredGap(JComponent component1,
			JComponent component2,
			int componentPlacement,
			int position,
			Container parent) {
		return DEFAULT_PREFERRED_GAP;
	}
}
