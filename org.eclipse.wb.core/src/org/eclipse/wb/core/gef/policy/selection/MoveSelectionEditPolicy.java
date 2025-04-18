/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.core.gef.policy.selection;

import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} that shows only {@link MoveHandle} as selection
 * feedback.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class MoveSelectionEditPolicy extends SelectionEditPolicy {
	private final Color m_color;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MoveSelectionEditPolicy() {
		this(ColorConstants.black);
	}

	public MoveSelectionEditPolicy(Color color) {
		m_color = color;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		{
			MoveHandle moveHandle = new MoveHandle(getHost());
			moveHandle.setForegroundColor(m_color);
			handles.add(moveHandle);
		}
		return handles;
	}
}
