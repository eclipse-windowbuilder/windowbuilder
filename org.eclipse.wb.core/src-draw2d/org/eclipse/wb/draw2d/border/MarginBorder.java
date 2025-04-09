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
package org.eclipse.wb.draw2d.border;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Insets;

/**
 * A border that provides blank padding.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class MarginBorder extends Border {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs a {@link MarginBorder} with dimensions specified by <i>insets</i>.
	 */
	public MarginBorder(Insets insets) {
		super(insets);
	}

	/**
	 * Constructs a {@link MarginBorder} with padding specified by the passed values.
	 */
	public MarginBorder(int allsides) {
		this(new Insets(allsides));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Border
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This method does nothing, since this border is just for spacing.
	 */
	@Override
	protected void paint(int ownerWidth, int ownerHeight, Graphics graphics) {
	}
}