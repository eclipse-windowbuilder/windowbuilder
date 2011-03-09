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

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Layout assistant for {@link TableWrapData}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapLayoutDataAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableWrapLayoutDataAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(3);
    {
      Group composite = new Group(this, SWT.NONE);
      composite.setText("Alignment");
      GridLayoutFactory.create(composite).columns(2);
      GridDataFactory.create(composite).fill().grab().spanH(2).spanV(2);
      // Horizontal alignment & grab
      {
        Group horizontalGroup =
            addChoiceProperty(composite, "horizontalAlignment", "Horizontal", new Object[][]{
                new Object[]{"Left", TableWrapData.LEFT},
                new Object[]{"Center", TableWrapData.CENTER},
                new Object[]{"Right", TableWrapData.RIGHT},
                new Object[]{"Fill", TableWrapData.FILL}});
        //
        addBooleanProperty(horizontalGroup, "grabHorizontal", "Grab");
        GridDataFactory.create(horizontalGroup).alignHC().fillV().grab();
      }
      // Vertical alignment & grab
      {
        Group verticalGroup =
            addChoiceProperty(composite, "verticalAlignment", "Vertical", new Object[][]{
                new Object[]{"Top", TableWrapData.TOP},
                new Object[]{"Center", TableWrapData.MIDDLE},
                new Object[]{"Bottom", TableWrapData.BOTTOM},
                new Object[]{"Fill", TableWrapData.FILL}});
        //
        addBooleanProperty(verticalGroup, "grabVertical", "Grab");
        GridDataFactory.create(verticalGroup).alignHC().fillV().grab();
      }
    }
    // Hints
    {
      Group group =
          addIntegerProperties(this, "Hints", new String[][]{
              {"indent", "Indent:"},
              {"maxWidth", "Max Width:"},
              {"maxHeight", "Max Height:"},
              {"heightHint", "Height Hint:"}}, new int[]{0, SWT.DEFAULT, SWT.DEFAULT, SWT.DEFAULT});
      GridDataFactory.create(group).fillH().fillV();
    }
  }
}