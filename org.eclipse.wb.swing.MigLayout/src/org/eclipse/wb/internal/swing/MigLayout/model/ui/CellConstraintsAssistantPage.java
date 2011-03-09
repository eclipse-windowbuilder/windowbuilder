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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.util.List;

/**
 * Layout assistant for {@link MigLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public class CellConstraintsAssistantPage extends AbstractAssistantPage {
  private final MigLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellConstraintsAssistantPage(Composite parent,
      MigLayoutInfo layout,
      List<ObjectInfo> objects) {
    super(parent, objects);
    m_layout = layout;
    GridLayoutFactory.create(this).columns(3);
    // horizontal alignments
    {
      Group horizontalGroup =
          addChoiceProperty(this, "h alignment", "Horizontal", new Object[][]{
              new Object[]{"Default", MigColumnInfo.Alignment.DEFAULT},
              new Object[]{"Left", MigColumnInfo.Alignment.LEFT},
              new Object[]{"Center", MigColumnInfo.Alignment.CENTER},
              new Object[]{"Right", MigColumnInfo.Alignment.RIGHT},
              new Object[]{"Fill", MigColumnInfo.Alignment.FILL},
              new Object[]{"Leading", MigColumnInfo.Alignment.LEADING},
              new Object[]{"Trailing", MigColumnInfo.Alignment.TRAILING},});
      GridDataFactory.modify(horizontalGroup).fill();
    }
    // vertical alignments
    {
      Group verticalGroup =
          addChoiceProperty(this, "v alignment", "Vertical", new Object[][]{
              new Object[]{"Default", MigRowInfo.Alignment.DEFAULT},
              new Object[]{"Top", MigRowInfo.Alignment.TOP},
              new Object[]{"Center", MigRowInfo.Alignment.CENTER},
              new Object[]{"Bottom", MigRowInfo.Alignment.BOTTOM},
              new Object[]{"Fill", MigRowInfo.Alignment.FILL},
              new Object[]{"Baseline", MigRowInfo.Alignment.BASELINE},});
      GridDataFactory.modify(verticalGroup).fill();
    }
    // grid
    {
      Group gridGroup =
          addIntegerProperties(this, "Grid", new String[][]{
              {"grid x", "X:"},
              {"grid y", "Y:"},
              {"grid width", "Width:"},
              {"grid height", "Height:"}});
      GridDataFactory.modify(gridGroup).fill();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractAssistantPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ObjectInfo getEditObject() {
    return m_layout;
  }

  @Override
  protected Property getCustomProperty(Object object, String propertyName) throws Exception {
    if (object instanceof ComponentInfo) {
      ComponentInfo component = (ComponentInfo) object;
      return MigLayoutInfo.getConstraints(component).getPropertyByTitle(propertyName);
    }
    return null;
  }
}