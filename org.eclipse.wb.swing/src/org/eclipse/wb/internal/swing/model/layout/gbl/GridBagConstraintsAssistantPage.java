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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.apache.commons.lang.SystemUtils;

/**
 * Layout assistant for {@link java.awt.GridBagConstraints}.
 * 
 * @author lobas_av
 * @coverage swing.assistant
 */
public class GridBagConstraintsAssistantPage extends AbstractAssistantPage {
  private static final String GROW_H = "__Grow_H";
  private static final String GROW_V = "__Grow_V";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridBagConstraintsAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(3);
    //
    if (SystemUtils.IS_JAVA_1_6) {
      // horizontal alignments
      {
        Group horizontalGroup = createHorizontalAlignmentGroup();
        GridDataFactory.modify(horizontalGroup).fill();
      }
      // vertical alignments
      {
        Group verticalGroup =
            addChoiceProperty(this, "verticalAlignment", "Vertical", new Object[][]{
                new Object[]{"Top", RowInfo.Alignment.TOP},
                new Object[]{"Center", RowInfo.Alignment.CENTER},
                new Object[]{"Bottom", RowInfo.Alignment.BOTTOM},
                new Object[]{"Fill", RowInfo.Alignment.FILL},
                new Object[]{"Baseline", RowInfo.Alignment.BASELINE},
                new Object[]{"Above baseline", RowInfo.Alignment.BASELINE_ABOVE},
                new Object[]{"Below baseline", RowInfo.Alignment.BASELINE_BELOW}});
        GridDataFactory.modify(verticalGroup).fillV();
      }
      // insets
      {
        Group insetsGroup = createInsetsGroup();
        GridDataFactory.modify(insetsGroup).fillV();
      }
      // weight
      {
        Group weightGroup = createWeightGroup();
        GridDataFactory.modify(weightGroup).fillV();
      }
      // grow
      {
        createGrowGroup();
      }
      // padding
      {
        Group paddingGroup = createPaddingGroup();
        GridDataFactory.create(paddingGroup).fill();
      }
    } else {
      // horizontal alignments
      {
        Group horizontalGroup = createHorizontalAlignmentGroup();
        GridDataFactory.modify(horizontalGroup).fillV();
      }
      // vertical alignments
      {
        Group verticalGroup =
            addChoiceProperty(this, "verticalAlignment", "Vertical", new Object[][]{
                new Object[]{"Top", RowInfo.Alignment.TOP},
                new Object[]{"Center", RowInfo.Alignment.CENTER},
                new Object[]{"Bottom", RowInfo.Alignment.BOTTOM},
                new Object[]{"Fill", RowInfo.Alignment.FILL}});
        GridDataFactory.modify(verticalGroup).fillV();
      }
      // insets
      {
        createInsetsGroup();
      }
      // padding
      {
        Group paddingGroup = createPaddingGroup();
        GridDataFactory.create(paddingGroup).fill().spanH(2);
      }
      // grow
      {
        createGrowGroup();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Builders
  //
  ////////////////////////////////////////////////////////////////////////////
  private Group createInsetsGroup() {
    return addIntegerProperties(this, "Insets", new String[][]{
        new String[]{"insets.top", "Top:"},
        new String[]{"insets.left", "Left:"},
        new String[]{"insets.bottom", "Bottom:"},
        new String[]{"insets.right", "Right:"}});
  }

  private Group createWeightGroup() {
    return addDoubleProperties(this, "Weight", new String[][]{
        new String[]{"weightx", "X:"},
        new String[]{"weighty", "Y:"}});
  }

  private Group createPaddingGroup() {
    return addIntegerProperties(this, "Padding", new String[][]{
        new String[]{"ipadx", "Width:"},
        new String[]{"ipady", "Height:"}});
  }

  private Group createHorizontalAlignmentGroup() {
    return addChoiceProperty(this, "horizontalAlignment", "Horizontal", new Object[][]{
        new Object[]{"Left", ColumnInfo.Alignment.LEFT},
        new Object[]{"Center", ColumnInfo.Alignment.CENTER},
        new Object[]{"Right", ColumnInfo.Alignment.RIGHT},
        new Object[]{"Fill", ColumnInfo.Alignment.FILL}});
  }

  private void createGrowGroup() {
    Group growGroup = new Group(this, SWT.NONE);
    GridLayoutFactory.create(growGroup);
    GridDataFactory.create(growGroup).fill();
    growGroup.setText("Grow");
    //
    addBooleanProperty(growGroup, GROW_H, "Horizontal");
    addBooleanProperty(growGroup, GROW_V, "Vetical");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Property getCustomProperty(Object object, String propertyName) throws Exception {
    // horizontal grow
    if (GROW_H.equals(propertyName)) {
      AbstractGridBagConstraintsInfo constraints = (AbstractGridBagConstraintsInfo) object;
      return new GrowProperty(constraints.getColumn());
    }
    // vertical grow
    if (GROW_V.equals(propertyName)) {
      AbstractGridBagConstraintsInfo constraints = (AbstractGridBagConstraintsInfo) object;
      return new GrowProperty(constraints.getRow());
    }
    return null;
  }

  private static final class GrowProperty extends InternalProperty {
    private final DimensionInfo m_dimension;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public GrowProperty(DimensionInfo dimension) {
      m_dimension = dimension;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getValue() throws Exception {
      return m_dimension.hasWeight();
    }

    @Override
    public void setValue(Object value) throws Exception {
      m_dimension.setWeight(m_dimension.hasWeight() ? 0 : 1);
    }
  }
}