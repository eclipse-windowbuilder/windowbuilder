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
package org.eclipse.wb.internal.swing.model.layout.gbl.actions;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.eclipse.jface.action.Separator;

import org.apache.commons.lang.SystemUtils;

import java.util.List;

/**
 * Helper for adding selection actions for {@link AbstractGridBagLayoutInfo}.
 * 
 * @author lobas_av
 * @coverage swing.model.layout
 */
public class SelectionActionsSupport extends ObjectEventListener {
  private final AbstractGridBagLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionActionsSupport(AbstractGridBagLayoutInfo layout) {
    m_layout = layout;
    m_layout.addBroadcastListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectEventListener
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions) throws Exception {
    if (objects.isEmpty()) {
      return;
    }
    // prepare constraints
    List<AbstractGridBagConstraintsInfo> constraints = Lists.newArrayList();
    for (ObjectInfo object : objects) {
      // check object
      if (!m_layout.isManagedObject(object)) {
        return;
      }
      // add data info
      ComponentInfo component = (ComponentInfo) object;
      constraints.add(m_layout.getConstraints(component));
    }
    // create horizontal actions
    actions.add(new Separator());
    addAlignmentAction(
        actions,
        constraints,
        true,
        "left.gif",
        ModelMessages.SelectionActionsSupport_haLeft,
        ColumnInfo.Alignment.LEFT);
    addAlignmentAction(
        actions,
        constraints,
        true,
        "center.gif",
        ModelMessages.SelectionActionsSupport_haCenter,
        ColumnInfo.Alignment.CENTER);
    addAlignmentAction(
        actions,
        constraints,
        true,
        "right.gif",
        ModelMessages.SelectionActionsSupport_haRight,
        ColumnInfo.Alignment.RIGHT);
    addAlignmentAction(
        actions,
        constraints,
        true,
        "fill.gif",
        ModelMessages.SelectionActionsSupport_haFill,
        ColumnInfo.Alignment.FILL);
    // create vertical actions
    actions.add(new Separator());
    addAlignmentAction(
        actions,
        constraints,
        false,
        "top.gif",
        ModelMessages.SelectionActionsSupport_vaTop,
        RowInfo.Alignment.TOP);
    addAlignmentAction(
        actions,
        constraints,
        false,
        "center.gif",
        ModelMessages.SelectionActionsSupport_vaCenter,
        RowInfo.Alignment.CENTER);
    addAlignmentAction(
        actions,
        constraints,
        false,
        "bottom.gif",
        ModelMessages.SelectionActionsSupport_vaBottom,
        RowInfo.Alignment.BOTTOM);
    addAlignmentAction(
        actions,
        constraints,
        false,
        "fill.gif",
        ModelMessages.SelectionActionsSupport_vaFill,
        RowInfo.Alignment.FILL);
    if (SystemUtils.IS_JAVA_1_6 || SystemUtils.IS_JAVA_1_7) {
      addAlignmentAction(
          actions,
          constraints,
          false,
          "baseline.gif",
          ModelMessages.SelectionActionsSupport_vaBaseline,
          RowInfo.Alignment.BASELINE);
      addAlignmentAction(
          actions,
          constraints,
          false,
          "baseline_above.gif",
          ModelMessages.SelectionActionsSupport_vaAboveBaseline,
          RowInfo.Alignment.BASELINE_ABOVE);
      addAlignmentAction(
          actions,
          constraints,
          false,
          "baseline_below.gif",
          ModelMessages.SelectionActionsSupport_vaBelowBaseline,
          RowInfo.Alignment.BASELINE_BELOW);
    }
    // create grow actions
    actions.add(new Separator());
    addGrowAction(
        actions,
        constraints,
        true,
        "grow.gif",
        ModelMessages.SelectionActionsSupport_haGrow);
    addGrowAction(
        actions,
        constraints,
        false,
        "grow.gif",
        ModelMessages.SelectionActionsSupport_vaGrow);
  }

  private void addAlignmentAction(List<Object> actions,
      List<AbstractGridBagConstraintsInfo> constraints,
      boolean horizontal,
      String iconPath,
      String tooltip,
      Object alignment) {
    boolean isChecked = true;
    // prepare select current value
    for (AbstractGridBagConstraintsInfo constraint : constraints) {
      if (horizontal) {
        if (constraint.getHorizontalAlignment() != alignment) {
          isChecked = false;
          break;
        }
      } else {
        if (constraint.getVerticalAlignment() != alignment) {
          isChecked = false;
          break;
        }
      }
    }
    // create action
    AlignmentAction action =
        new AlignmentAction(constraints, horizontal, iconPath, tooltip, isChecked, alignment);
    actions.add(action);
  }

  private void addGrowAction(List<Object> actions,
      List<AbstractGridBagConstraintsInfo> constraints,
      boolean horizontal,
      String iconPath,
      String tooltip) {
    boolean isChecked = true;
    // prepare select current value
    for (AbstractGridBagConstraintsInfo constraint : constraints) {
      if (horizontal) {
        if (!constraint.getColumn().hasWeight()) {
          isChecked = false;
          break;
        }
      } else {
        if (!constraint.getRow().hasWeight()) {
          isChecked = false;
          break;
        }
      }
    }
    // create action
    GrowAction action =
        new GrowAction(constraints, horizontal, iconPath, tooltip, isChecked, !isChecked);
    actions.add(action);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private abstract class AbstractAction extends ObjectInfoAction {
    private final List<AbstractGridBagConstraintsInfo> m_constraints;
    private final boolean m_horizontal;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AbstractAction(int style,
        List<AbstractGridBagConstraintsInfo> constraints,
        boolean horizontal,
        String iconPath,
        String tooltip,
        boolean checked) {
      super(m_layout, "", style);
      m_constraints = constraints;
      m_horizontal = horizontal;
      String path = "headers/" + (m_horizontal ? "h" : "v") + "/menu/" + iconPath;
      setImageDescriptor(AbstractGridBagLayoutInfo.getImageDescriptor(path));
      setToolTipText(tooltip);
      setChecked(checked);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void runEx() throws Exception {
      for (AbstractGridBagConstraintsInfo constraint : m_constraints) {
        if (m_horizontal) {
          handleHorizontal(constraint);
        } else {
          handleVertical(constraint);
        }
      }
    }

    protected abstract void handleHorizontal(AbstractGridBagConstraintsInfo constraint)
        throws Exception;

    protected abstract void handleVertical(AbstractGridBagConstraintsInfo constraint)
        throws Exception;
  }
  private final class AlignmentAction extends AbstractAction {
    private final Object m_alignment;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AlignmentAction(List<AbstractGridBagConstraintsInfo> constraints,
        boolean horizontal,
        String iconPath,
        String tooltip,
        boolean checked,
        Object alignment) {
      super(AS_RADIO_BUTTON, constraints, horizontal, iconPath, tooltip, checked);
      m_alignment = alignment;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // AbstractAction
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void handleHorizontal(AbstractGridBagConstraintsInfo constraint) throws Exception {
      constraint.setHorizontalAlignment((ColumnInfo.Alignment) m_alignment);
    }

    @Override
    protected void handleVertical(AbstractGridBagConstraintsInfo constraint) throws Exception {
      constraint.setVerticalAlignment((RowInfo.Alignment) m_alignment);
    }
  }
  private final class GrowAction extends AbstractAction {
    private final boolean m_grow;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public GrowAction(List<AbstractGridBagConstraintsInfo> constraints,
        boolean horizontal,
        String iconPath,
        String tooltip,
        boolean checked,
        boolean grow) {
      super(AS_CHECK_BOX, constraints, horizontal, iconPath, tooltip, checked);
      m_grow = grow;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // AbstractAction
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void handleHorizontal(AbstractGridBagConstraintsInfo constraint) throws Exception {
      ColumnInfo column = constraint.getColumn();
      column.setWeight(m_grow ? 1 : 0);
    }

    @Override
    protected void handleVertical(AbstractGridBagConstraintsInfo constraint) throws Exception {
      RowInfo row = constraint.getRow();
      row.setWeight(m_grow ? 1 : 0);
    }
  }
}