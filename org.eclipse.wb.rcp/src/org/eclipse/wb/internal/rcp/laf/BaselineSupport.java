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
package org.eclipse.wb.internal.rcp.laf;

import org.eclipse.wb.internal.core.laf.IBaselineSupport;
import org.eclipse.wb.swt.widgets.baseline.Baseline;

import org.eclipse.swt.widgets.Control;

/**
 * Baseline support for SWT widgets.
 *
 * @author mitin_aa
 */
public class BaselineSupport implements IBaselineSupport {
  public int getBaseline(Object component) {
    if (!(component instanceof Control)) {
      return NO_BASELINE;
    }
    return Baseline.getBaseline((Control) component);
  }
}
