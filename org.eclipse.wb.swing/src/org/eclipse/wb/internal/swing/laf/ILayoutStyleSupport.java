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

import org.eclipse.draw2d.PositionConstants;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;

/**
 * Interface providing LayoutStyle functionality.
 *
 * @author mitin_aa
 * @coverage swing.laf
 */
public interface ILayoutStyleSupport {
	String LAYOUT_STYLE_POINT = "org.eclipse.wb.swing.layoutStyle";

	/**
	 * Sets the LayoutStyle instance for given <code>laf</code>.
	 *
	 * @param laf
	 *          a {@link LookAndFeel} which LayoutStyle would be used.
	 */
	void setLayoutStyle(LookAndFeel laf);

	/**
	 * Calls appropriate LayoutStyle.getPreferredGap() method of LayoutStyle class using reflection.
	 * Note, the <code>position</code> should be one of {@link PositionConstants#LEFT},
	 * {@link PositionConstants#RIGHT}, {@link PositionConstants#TOP},
	 * {@link PositionConstants#BOTTOM} (it would be converted to appropriate {@link SwingConstants}
	 * values).
	 */
	int getPreferredGap(JComponent component1,
			JComponent component2,
			int componentPlacement,
			int position,
			Container parent);

	/**
	 * Calls appropriate LayoutStyle.getContainerGap() method of LayoutStyle class using reflection.
	 * Note, the <code>position</code> should be one of {@link PositionConstants#LEFT},
	 * {@link PositionConstants#RIGHT}, {@link PositionConstants#TOP},
	 * {@link PositionConstants#BOTTOM} (it would be converted to appropriate {@link SwingConstants}
	 * values).
	 */
	int getContainerGap(JComponent component, int position, Container parent);
}
