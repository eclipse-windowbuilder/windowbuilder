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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.SWT;

import java.text.MessageFormat;

/**
 * Utility class for working with SWT FormLayout.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public final class FormLayoutUtils {
	/**
	 * @return the constant which represents 'opposite' side in one dimension, i.e. for LEFT it
	 *         returns RIGHT.
	 */
	public static int getOppositeSide(int side) {
		switch (side) {
		case SWT.LEFT :
			return SWT.RIGHT;
		case SWT.RIGHT :
			return SWT.LEFT;
		case SWT.TOP :
			return SWT.BOTTOM;
		case SWT.BOTTOM :
			return SWT.TOP;
		default :
			throw new IllegalArgumentException(MessageFormat.format(
					ModelMessages.FormLayoutUtils_invalidSide,
					side));
		}
	}

	/**
	 * @return converted SWT constant (LEFT, RIGHT, TOP, BOTTOM) into appropriate constants from
	 *         PositionConstants.
	 */
	public static int convertSwtAlignment(int alignment) {
		switch (alignment) {
		case SWT.LEFT :
			return PositionConstants.LEFT;
		case SWT.RIGHT :
			return PositionConstants.RIGHT;
		case SWT.TOP :
			return PositionConstants.TOP;
		case SWT.BOTTOM :
			return PositionConstants.BOTTOM;
		case SWT.CENTER :
			return PositionConstants.CENTER;
		default :
			throw new IllegalArgumentException(MessageFormat.format(
					ModelMessages.FormLayoutUtils_invalidAlignment,
					alignment));
		}
	}

	/**
	 * @return converted PositionConstants constant (LEFT, RIGHT, TOP, BOTTOM) into appropriate
	 *         constants from SWT.
	 */
	public static int convertGefSide(int side) {
		switch (side) {
		case PositionConstants.LEFT :
			return SWT.LEFT;
		case PositionConstants.RIGHT :
			return SWT.RIGHT;
		case PositionConstants.TOP :
			return SWT.TOP;
		case PositionConstants.BOTTOM :
			return SWT.BOTTOM;
		default :
			throw new IllegalArgumentException(MessageFormat.format(
					ModelMessages.FormLayoutUtils_invalidSide,
					side));
		}
	}

	/**
	 * @return the source string for SWT.LEFT, RIGHT, TOP, BOTTOM.
	 */
	public static String getAlignmentSource(int alignment) {
		switch (alignment) {
		case SWT.LEFT :
			return "org.eclipse.swt.SWT.LEFT";
		case SWT.RIGHT :
			return "org.eclipse.swt.SWT.RIGHT";
		case SWT.TOP :
			return "org.eclipse.swt.SWT.TOP";
		case SWT.BOTTOM :
			return "org.eclipse.swt.SWT.BOTTOM";
		default :
			return "org.eclipse.swt.SWT.DEFAULT";
		}
	}
}
