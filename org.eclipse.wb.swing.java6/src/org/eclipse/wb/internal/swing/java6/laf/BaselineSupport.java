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
package org.eclipse.wb.internal.swing.java6.laf;

import org.eclipse.wb.internal.core.laf.IBaselineSupport;

import java.awt.Component;
import java.awt.Dimension;

/**
 * Baseline support for components using Java6 methods.
 * 
 * @author mitin_aa
 */
public class BaselineSupport implements IBaselineSupport {
  @Override
  public int getBaseline(Object component) {
    if (!(component instanceof Component)) {
      return NO_BASELINE;
    }
    Component comp = (Component) component;
    Dimension size = comp.getSize();
    return comp.getBaseline(size.width, size.height);
  }
}
