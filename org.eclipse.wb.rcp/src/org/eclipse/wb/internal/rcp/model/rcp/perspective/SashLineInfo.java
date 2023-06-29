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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.draw2d.IPositionConstants;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Model for "sash" between {@link AbstractPartInfo}'s in {@link PageLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class SashLineInfo {
	public static final int SASH_SIZE = 3;
	private final AbstractPartInfo m_part;
	private final Rectangle m_bounds;
	private final Rectangle m_partBounds;
	private final Rectangle m_refBounds;
	private final int m_position;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SashLineInfo(AbstractPartInfo part,
			Rectangle partBounds,
			Rectangle refBounds,
			int position,
			Rectangle bounds) {
		m_part = part;
		m_position = position;
		m_bounds = bounds;
		m_partBounds = partBounds;
		m_refBounds = refBounds;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		String s_1 = m_part.getId() + "," + isHorizontal();
		return "(" + s_1 + "," + m_bounds + "," + m_partBounds + "," + m_refBounds + ")";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the host {@link AbstractPartInfo}.
	 */
	public AbstractPartInfo getPart() {
		return m_part;
	}

	/**
	 * @return the position relative to {@link #getPart()}, one of the
	 *         {@link IPositionConstants#NORTH}, {@link IPositionConstants#SOUTH},
	 *         {@link IPositionConstants#WEST} or {@link IPositionConstants#EAST}.
	 */
	public int getPosition() {
		return m_position;
	}

	/**
	 * @return <code>true</code> if this {@link AbstractPartInfo} references its "reference" in
	 *         horizontal direction, i.e. left/right.
	 */
	public boolean isHorizontal() {
		return m_position == IPositionConstants.WEST || m_position == IPositionConstants.EAST;
	}

	/**
	 * @return the bounds of this {@link SashLineInfo}.
	 */
	public Rectangle getBounds() {
		return m_bounds;
	}

	/**
	 * @return the bounds of {@link #getPart()}.
	 */
	public Rectangle getPartBounds() {
		return m_partBounds;
	}

	/**
	 * @return the bounds of "part" referenced by {@link #getPart()}.
	 */
	public Rectangle getRefBounds() {
		return m_refBounds;
	}
}