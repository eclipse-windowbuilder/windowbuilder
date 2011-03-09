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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Layout assistant for {@link org.eclipse.swt.layout.FillLayout}.
 * 
 * @author lobas_av
 * @coverage swt.assistant
 */
public final class FillLayoutAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FillLayoutAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // orientation
    {
      Group orientationGroup =
          addChoiceProperty(this, "type", "Orientation", new Object[][]{
              new Object[]{"Horizontal", SWT.HORIZONTAL},
              new Object[]{"Vertical", SWT.VERTICAL}});
      GridDataFactory.create(orientationGroup).fillV();
    }
    // spacing
    {
      Group spacingGroup =
          addIntegerProperties(this, "Margin && Spacing", new String[][]{
              new String[]{"marginWidth", "Margin width:"},
              new String[]{"marginHeight", "Margin height:"},
              new String[]{"spacing", "Spacing:"}});
      GridDataFactory.create(spacingGroup).fillV();
    }
  }
}