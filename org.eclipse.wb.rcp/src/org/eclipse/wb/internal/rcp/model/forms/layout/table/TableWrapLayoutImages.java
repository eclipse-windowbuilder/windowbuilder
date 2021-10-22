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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.internal.rcp.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Provider of {@link Image} and {@link ImageDescriptor} for {@link TableWrapLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapLayoutImages {
  /**
   * @return the {@link Image} for {@link ITableWrapLayoutInfo}.
   */
  public static Image getImage(String path) {
    return Activator.getImage("info/layout/TableWrapLayout/" + path);
  }

  /**
   * @return the {@link ImageDescriptor} for {@link ITableWrapLayoutInfo}.
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return Activator.getImageDescriptor("info/layout/TableWrapLayout/" + path);
  }
}