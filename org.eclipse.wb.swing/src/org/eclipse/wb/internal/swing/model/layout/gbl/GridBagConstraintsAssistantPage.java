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
import org.eclipse.wb.internal.swing.model.ModelMessages;

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
    if (SystemUtils.IS_JAVA_1_6 || SystemUtils.IS_JAVA_1_7) {
      // horizontal alignments
      {
        Group horizontalGroup = createHorizontalAlignmentGroup();
        GridDataFactory.modify(horizontalGroup).fill();
      }
      // vertical alignments
      {
        Group verticalGroup =
            addChoiceProperty(
                this,
                "verticalAlignment",
                ModelMessages.GridBagConstraintsAssistantPage_verticalAlignmentGroup,
                new Object[][]{
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaTop,
                        RowInfo.Alignment.TOP},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaCenter,
                        RowInfo.Alignment.CENTER},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaBottom,
                        RowInfo.Alignment.BOTTOM},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaFill,
                        RowInfo.Alignment.FILL},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaBaseline,
                        RowInfo.Alignment.BASELINE},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaAboveBaseline,
                        RowInfo.Alignment.BASELINE_ABOVE},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaBelowBaseline,
                        RowInfo.Alignment.BASELINE_BELOW}});
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
            addChoiceProperty(
                this,
                "verticalAlignment",
                ModelMessages.GridBagConstraintsAssistantPage_verticalAlignmentGroup,
                new Object[][]{
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaTop,
                        RowInfo.Alignment.TOP},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaCenter,
                        RowInfo.Alignment.CENTER},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaBottom,
                        RowInfo.Alignment.BOTTOM},
                    new Object[]{
                        ModelMessages.GridBagConstraintsAssistantPage_vaFill,
                        RowInfo.Alignment.FILL}});
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
    return addIntegerProperties(
        this,
        ModelMessages.GridBagConstraintsAssistantPage_insetsGroup,
        new String[][]{
            new String[]{"insets.top", ModelMessages.GridBagConstraintsAssistantPage_insetsTop},
            new String[]{"insets.left", ModelMessages.GridBagConstraintsAssistantPage_insetsLeft},
            new String[]{
                "insets.bottom",
                ModelMessages.GridBagConstraintsAssistantPage_insetsBottom},
            new String[]{"insets.right", ModelMessages.GridBagConstraintsAssistantPage_insetsRight}});
  }

  private Group createWeightGroup() {
    return addDoubleProperties(
        this,
        ModelMessages.GridBagConstraintsAssistantPage_weightGroup,
        new String[][]{
            new String[]{"weightx", ModelMessages.GridBagConstraintsAssistantPage_weightX},
            new String[]{"weighty", ModelMessages.GridBagConstraintsAssistantPage_weightY}});
  }

  private Group createPaddingGroup() {
    return addIntegerProperties(
        this,
        ModelMessages.GridBagConstraintsAssistantPage_paddingGroup,
        new String[][]{
            new String[]{"ipadx", ModelMessages.GridBagConstraintsAssistantPage_paddingWidth},
            new String[]{"ipady", ModelMessages.GridBagConstraintsAssistantPage_paddingHeight}});
  }

  private Group createHorizontalAlignmentGroup() {
    return addChoiceProperty(
        this,
        "horizontalAlignment",
        ModelMessages.GridBagConstraintsAssistantPage_horizontalAlignmentGroup,
        new Object[][]{
            new Object[]{
                ModelMessages.GridBagConstraintsAssistantPage_haLeft,
                ColumnInfo.Alignment.LEFT},
            new Object[]{
                ModelMessages.GridBagConstraintsAssistantPage_haCenter,
                ColumnInfo.Alignment.CENTER},
            new Object[]{
                ModelMessages.GridBagConstraintsAssistantPage_haRight,
                ColumnInfo.Alignment.RIGHT},
            new Object[]{
                ModelMessages.GridBagConstraintsAssistantPage_haFill,
                ColumnInfo.Alignment.FILL}});
  }

  private void createGrowGroup() {
    Group growGroup = new Group(this, SWT.NONE);
    GridLayoutFactory.create(growGroup);
    GridDataFactory.create(growGroup).fill();
    growGroup.setText(ModelMessages.GridBagConstraintsAssistantPage_growGroup);
    //
    addBooleanProperty(
        growGroup,
        GROW_H,
        ModelMessages.GridBagConstraintsAssistantPage_growHorizontal);
    addBooleanProperty(
        growGroup,
        GROW_V,
        ModelMessages.GridBagConstraintsAssistantPage_growVertical);
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