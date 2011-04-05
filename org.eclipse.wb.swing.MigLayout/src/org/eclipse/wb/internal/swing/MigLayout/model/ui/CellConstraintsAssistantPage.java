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
import org.eclipse.wb.internal.swing.MigLayout.model.ModelMessages;
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
          addChoiceProperty(
              this,
              "h alignment", ModelMessages.CellConstraintsAssistantPage_hAlignmentGroup, new Object[][]{ //$NON-NLS-1$
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_hAlignment_default,
                      MigColumnInfo.Alignment.DEFAULT},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_hAlignment_left,
                      MigColumnInfo.Alignment.LEFT},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_hAlignment_center,
                      MigColumnInfo.Alignment.CENTER},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_hAlignment_right,
                      MigColumnInfo.Alignment.RIGHT},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_hAlignment_fill,
                      MigColumnInfo.Alignment.FILL},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_hAlignment_leading,
                      MigColumnInfo.Alignment.LEADING},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_hAlignment_trailing,
                      MigColumnInfo.Alignment.TRAILING},});
      GridDataFactory.modify(horizontalGroup).fill();
    }
    // vertical alignments
    {
      Group verticalGroup =
          addChoiceProperty(
              this,
              "v alignment", ModelMessages.CellConstraintsAssistantPage_vAlignmentGroup, new Object[][]{ //$NON-NLS-1$
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_vAlignment_default,
                      MigRowInfo.Alignment.DEFAULT},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_vAlignment_top,
                      MigRowInfo.Alignment.TOP},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_vAlignment_center,
                      MigRowInfo.Alignment.CENTER},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_vAlignment_bottom,
                      MigRowInfo.Alignment.BOTTOM},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_vAlignment_fill,
                      MigRowInfo.Alignment.FILL},
                  new Object[]{
                      ModelMessages.CellConstraintsAssistantPage_vAlignment_baseline,
                      MigRowInfo.Alignment.BASELINE},});
      GridDataFactory.modify(verticalGroup).fill();
    }
    // grid
    {
      Group gridGroup =
          addIntegerProperties(
              this,
              ModelMessages.CellConstraintsAssistantPage_gridGroup,
              new String[][]{{"grid x", ModelMessages.CellConstraintsAssistantPage_gridX}, //$NON-NLS-1$
                  {"grid y", ModelMessages.CellConstraintsAssistantPage_gridY}, //$NON-NLS-1$
                  {"grid width", ModelMessages.CellConstraintsAssistantPage_gridWidth}, //$NON-NLS-1$
                  {"grid height", ModelMessages.CellConstraintsAssistantPage_gridHeight}}); //$NON-NLS-1$
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