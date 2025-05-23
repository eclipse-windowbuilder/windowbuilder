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

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.java6.Messages;
import org.eclipse.wb.internal.swing.laf.ILayoutStyleSupport;

import org.eclipse.draw2d.PositionConstants;

import java.awt.Container;
import java.text.MessageFormat;

import javax.swing.JComponent;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;

/**
 * Java6 LayoutStyle support implementation.
 *
 * @author mitin_aa
 * @coverage swing.laf
 */
public final class LayoutStyleSupport implements ILayoutStyleSupport {
	private LayoutStyle m_layoutStyle;

	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutStyleSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setLayoutStyle(LookAndFeel laf) {
		Assert.isNotNull(laf);
		m_layoutStyle = laf.getLayoutStyle();
	}

	@Override
	public int getContainerGap(JComponent component, int position, Container parent) {
		return m_layoutStyle.getContainerGap(component, convertPositionConstants(position), parent);
	}

	@Override
	public int getPreferredGap(JComponent component1,
			JComponent component2,
			int componentPlacement,
			int position,
			Container parent) {
		return m_layoutStyle.getPreferredGap(
				component1,
				component2,
				convertPlacement(componentPlacement),
				convertPositionConstants(position),
				parent);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts from {@link PositionConstants} into {@link SwingConstants}.
	 */
	private static int convertPositionConstants(int positionConstant) {
		switch (positionConstant) {
		case PositionConstants.TOP :
			return SwingConstants.NORTH;
		case PositionConstants.BOTTOM :
			return SwingConstants.SOUTH;
		case PositionConstants.LEFT :
			return SwingConstants.WEST;
		case PositionConstants.RIGHT :
			return SwingConstants.EAST;
		default :
			throw new IllegalArgumentException(MessageFormat.format(
					Messages.LayoutStyleSupport_unsupportedPosition,
					positionConstant));
		}
	}

	/**
	 * Converts integer into {@link ComponentPlacement} constant.
	 */
	private ComponentPlacement convertPlacement(int componentPlacement) {
		ComponentPlacement[] values = ComponentPlacement.values();
		for (ComponentPlacement placement : values) {
			if (placement.ordinal() == componentPlacement) {
				return placement;
			}
		}
		throw new IllegalArgumentException(MessageFormat.format(
				Messages.LayoutStyleSupport_unsupportedPlacement,
				componentPlacement));
	}
}
