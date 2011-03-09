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
package org.eclipse.wb.swt.widgets.baseline;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * package private class used to fetch baseline values on Windows platform
 * 
 * @author mitin_aa
 */
final class WindowsBaseline extends Baseline {
  /**
   * Adjust baseline position for default Windows Theme
   */
  @Override
  protected int adjustBaseline(Control control, int baseline) {
    int style = control.getStyle();
    int borderWidth = control.getBorderWidth();
    Class<?> controlClass = control.getClass();
    boolean isButton = Button.class.isAssignableFrom(controlClass);
    boolean isText = Text.class.isAssignableFrom(controlClass);
    boolean isSpinner = spinnerClass != null && spinnerClass.isAssignableFrom(controlClass);
    if (Combo.class.isAssignableFrom(controlClass)) {
      baseline -= 1;
    } else if (List.class.isAssignableFrom(controlClass)) {
      baseline += 2;
    }
    if ((style & SWT.BORDER) != 0 && !isButton) {
      baseline += borderWidth;
      if (isText || isSpinner) {
        baseline += 1;
      }
    }
    return baseline;
  }

  /*private int adjustBaseline2(Control control, int baseline) {
  	int style = control.getStyle();
  	int borderWidth = control.getBorderWidth();
  	boolean isButton = control.getClass().isAssignableFrom(Button.class);
  	if (isButton) {
  		baseline += 2;
  	} else if (control.getClass().isAssignableFrom(Label.class)) {
  		baseline += 2;
  	} else if (control.getClass().isAssignableFrom(Text.class)) {
  		baseline += 1;
  	} else if (control.getClass().isAssignableFrom(Combo.class)) {
  		baseline += 1;
  	} else if (control.getClass().isAssignableFrom(List.class)) {
  		baseline += 2;
  	}
  	if ((style & SWT.BORDER) != 0 && !isButton) {
  		baseline += borderWidth;
  	}
  	return baseline;
  }*/
  @Override
  protected boolean centerAlignedText(Class<?> clazz, int style) {
    return Button.class.isAssignableFrom(clazz) || Combo.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean topAlignedText(Class<?> clazz, int style) {
    return Label.class.isAssignableFrom(clazz)
        || Text.class.isAssignableFrom(clazz)
        || List.class.isAssignableFrom(clazz)
        || spinnerClass != null
        && spinnerClass.isAssignableFrom(clazz);
  }
}
