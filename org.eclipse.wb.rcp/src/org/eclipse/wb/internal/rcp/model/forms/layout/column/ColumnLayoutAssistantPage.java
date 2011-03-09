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
package org.eclipse.wb.internal.rcp.model.forms.layout.column;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.ColumnLayout;

/**
 * Layout assistant for {@link ColumnLayout}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class ColumnLayoutAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnLayoutAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // columns
    {
      Group columnsGroup =
          addIntegerProperties(this, "Number of columns", new String[][]{
              new String[]{"minNumColumns", "Minimum:"},
              new String[]{"maxNumColumns", "Maximum:"},});
      GridDataFactory.create(columnsGroup).fill();
    }
    // spacing
    {
      Group spacingGroup =
          addIntegerProperties(this, "Spacing", new String[][]{
              new String[]{"horizontalSpacing", "Horizontal:"},
              new String[]{"verticalSpacing", "Vertical:"},});
      GridDataFactory.create(spacingGroup).fillV();
    }
    // margins for sides
    {
      Group marginsGroup =
          addIntegerProperties(this, "Margins for sides", new String[][]{
              new String[]{"leftMargin", "Margin left:"},
              new String[]{"rightMargin", "Margin right:"},
              new String[]{"topMargin", "Margin top:"},
              new String[]{"bottomMargin", "Margin bottom:"}});
      GridDataFactory.create(marginsGroup).fillV();
    }
  }
}