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
package org.eclipse.wb.internal.rcp.model.layout;

import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.custom.StackLayout;

/**
 * Interface model for {@link StackLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.layout
 */
public interface IStackLayoutInfo<C extends IControlInfo> extends ILayoutInfo<C> {
  /**
   * @return the {@link IControlInfo} before active one.
   */
  C getPrevControl();

  /**
   * @return the {@link IControlInfo} after active one.
   */
  C getNextControl();

  /**
   * Sets {@link IControlInfo} to show on {@link StackLayout}.
   */
  void show(C control);
}