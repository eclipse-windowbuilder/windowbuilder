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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;

import java.text.MessageFormat;

/**
 * Information about side in {@link FormLayout}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout.form
 */
public enum FormSide {
	LEFT {
		@Override
		public String getField() {
			return "left";
		}

		@Override
		public int getFormSide() {
			return SWT.LEFT;
		}

		@Override
		public int getEngineSide() {
			return PositionConstants.LEFT;
		}

		@Override
		public FormSide getOppositeSide() {
			return RIGHT;
		}
	},
	RIGHT {
		@Override
		public String getField() {
			return "right";
		}

		@Override
		public int getFormSide() {
			return SWT.RIGHT;
		}

		@Override
		public int getEngineSide() {
			return PositionConstants.RIGHT;
		}

		@Override
		public FormSide getOppositeSide() {
			return LEFT;
		}
	},
	TOP {
		@Override
		public String getField() {
			return "top";
		}

		@Override
		public int getFormSide() {
			return SWT.TOP;
		}

		@Override
		public int getEngineSide() {
			return PositionConstants.TOP;
		}

		@Override
		public FormSide getOppositeSide() {
			return BOTTOM;
		}
	},
	BOTTOM {
		@Override
		public String getField() {
			return "bottom";
		}

		@Override
		public int getFormSide() {
			return SWT.BOTTOM;
		}

		@Override
		public int getEngineSide() {
			return PositionConstants.BOTTOM;
		}

		@Override
		public FormSide getOppositeSide() {
			return TOP;
		}
	};
	/**
	 * @return the name of field.
	 */
	public abstract String getField();

	/**
	 * @return the side, can be either of SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM.
	 */
	public abstract int getFormSide();

	/**
	 * @return the side, can be either of PositionConstants.LEFT, PositionConstants.RIGHT,
	 *         PositionConstants.TOP, PositionConstants.BOTTOM.
	 */
	public abstract int getEngineSide();

	/**
	 * @return the FormSide as opposite to this side.
	 */
	public abstract FormSide getOppositeSide();

	////////////////////////////////////////////////////////////////////////////
	//
	// Static
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link FormSide} by integer side: PositionConstants.LEFT,
	 *         PositionConstants.RIGHT, PositionConstants.TOP, PositionConstants.BOTTOM.
	 */
	public static FormSide get(int side) {
		if (side == PositionConstants.LEFT) {
			return LEFT;
		}
		if (side == PositionConstants.RIGHT) {
			return RIGHT;
		}
		if (side == PositionConstants.TOP) {
			return TOP;
		}
		if (side == PositionConstants.BOTTOM) {
			return BOTTOM;
		}
		throw new Error(MessageFormat.format(ModelMessages.FormSide_unknownSize, side));
	}
}
