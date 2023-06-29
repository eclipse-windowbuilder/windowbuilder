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

public class CarbonBaseline extends Baseline {
	@Override
	protected boolean centerAlignedText(Class<?> clazz, int style) {
		return Button.class.isAssignableFrom(clazz)
				|| datetimeClass != null
				&& datetimeClass.isAssignableFrom(clazz);
	}

	@Override
	protected boolean topAlignedText(Class<?> clazz, int style) {
		return Label.class.isAssignableFrom(clazz)
				|| Combo.class.isAssignableFrom(clazz)
				|| Text.class.isAssignableFrom(clazz)
				|| List.class.isAssignableFrom(clazz)
				|| spinnerClass != null
				&& spinnerClass.isAssignableFrom(clazz);
	}

	@Override
	protected int adjustBaseline(Control control, int baseline) {
		int style = control.getStyle();
		boolean hasBorder = (style & SWT.BORDER) != 0;
		int borderWidth = control.getBorderWidth();
		Class<?> controlClass = control.getClass();
		boolean isSpinner = spinnerClass != null && spinnerClass.isAssignableFrom(controlClass);
		boolean isDateTime = datetimeClass != null && datetimeClass.isAssignableFrom(controlClass);
		if (Button.class.isAssignableFrom(controlClass)) {
			if ((style & SWT.CHECK) == 0 && (style & SWT.RADIO) == 0) {
				baseline -= 1;
			}
		} else if (Combo.class.isAssignableFrom(controlClass)) {
			if ((style & SWT.READ_ONLY) != 0) {
				baseline += 6;
			} else {
				baseline += 7;
			}
		} else if (List.class.isAssignableFrom(controlClass) || isSpinner) {
			baseline += 5;
		} else if (isDateTime) {
			if ((style & 1 << 10 /*SWT.CALENDAR*/) != 0) { // SWT.CALENDAR is Eclipse 3.3 and above
				return NO_BASELINE;
			}
		} else if (Text.class.isAssignableFrom(controlClass)) {
			if (hasBorder) {
				baseline += borderWidth + 5;
			} else {
				baseline += 1;
			}
		}
		return baseline;
	}
	/*int fetchBaseline2(Control control, int width, int height) {
   try {
   Method method = Control.class.getDeclaredMethod("defaultThemeFont", new Class[]{});
   method.setAccessible(true);
   short defaultThemeFont = ((Integer) method.invoke(control, new Object[]{})).shortValue();
   //
   int cfString = stringToStringRef("a");
   try {
   //
   org.eclipse.swt.internal.carbon.Point ioBounds = new org.eclipse.swt.internal.carbon.Point();
   short[] baseline = new short[1];
   OS.GetThemeTextDimensions(cfString, defaultThemeFont, OS.kThemeStateActive, false, ioBounds, baseline);
   if (baseline[0] != 0) {
   return baseline[0];
   }
   } finally {
   OS.CFRelease(cfString);
   }
   return NO_BASELINE;
   } catch (Throwable e) {
   return NO_BASELINE;
   }
   }
   private int stringToStringRef(String string) {
   char[] buffer = new char[string.length()];
   string.getChars(0, buffer.length, buffer, 0);
   return OS.CFStringCreateWithCharacters(OS.kCFAllocatorDefault, buffer, buffer.length);
   }*/
}
