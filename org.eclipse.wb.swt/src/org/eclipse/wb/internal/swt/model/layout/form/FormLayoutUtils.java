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

import org.eclipse.wb.draw2d.IPositionConstants;

import org.eclipse.swt.SWT;

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
        throw new IllegalArgumentException("Invalid side requested: " + side);
    }
  }

  /**
   * @return converted SWT constant (LEFT, RIGHT, TOP, BOTTOM) into appropriate constants from
   *         IPositionConstants.
   */
  public static int convertSwtAlignment(int alignment) {
    switch (alignment) {
      case SWT.LEFT :
        return IPositionConstants.LEFT;
      case SWT.RIGHT :
        return IPositionConstants.RIGHT;
      case SWT.TOP :
        return IPositionConstants.TOP;
      case SWT.BOTTOM :
        return IPositionConstants.BOTTOM;
      case SWT.CENTER :
        return IPositionConstants.CENTER;
      default :
        throw new IllegalArgumentException("Invalid SWT alignment requested: " + alignment);
    }
  }

  /**
   * @return converted IPositionConstants constant (LEFT, RIGHT, TOP, BOTTOM) into appropriate
   *         constants from SWT.
   */
  public static int convertGefSide(int side) {
    switch (side) {
      case IPositionConstants.LEFT :
        return SWT.LEFT;
      case IPositionConstants.RIGHT :
        return SWT.RIGHT;
      case IPositionConstants.TOP :
        return SWT.TOP;
      case IPositionConstants.BOTTOM :
        return SWT.BOTTOM;
      default :
        throw new IllegalArgumentException("Invalid side requested: " + side);
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
