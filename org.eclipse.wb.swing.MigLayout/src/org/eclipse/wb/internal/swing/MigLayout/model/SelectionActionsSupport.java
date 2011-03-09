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
package org.eclipse.wb.internal.swing.MigLayout.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;

import java.util.List;

/**
 * Helper for adding selection actions for {@link MigLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public class SelectionActionsSupport extends ObjectEventListener {
  private final MigLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionActionsSupport(MigLayoutInfo layout) {
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
    List<CellConstraintsSupport> constraints = Lists.newArrayList();
    for (ObjectInfo object : objects) {
      // check object
      if (!(object instanceof ComponentInfo) || object.getParent() != m_layout.getContainer()) {
        return;
      }
      // add data info
      ComponentInfo component = (ComponentInfo) object;
      constraints.add(MigLayoutInfo.getConstraints(component));
    }
    // create horizontal actions
    actions.add(new Separator());
    addHorizontalAlignmentAction(actions, constraints, MigColumnInfo.Alignment.DEFAULT);
    addHorizontalAlignmentAction(actions, constraints, MigColumnInfo.Alignment.LEFT);
    addHorizontalAlignmentAction(actions, constraints, MigColumnInfo.Alignment.CENTER);
    addHorizontalAlignmentAction(actions, constraints, MigColumnInfo.Alignment.RIGHT);
    addHorizontalAlignmentAction(actions, constraints, MigColumnInfo.Alignment.FILL);
    addHorizontalAlignmentAction(actions, constraints, MigColumnInfo.Alignment.LEADING);
    addHorizontalAlignmentAction(actions, constraints, MigColumnInfo.Alignment.TRAILING);
    // create vertical actions
    actions.add(new Separator());
    addVerticalAlignmentAction(actions, constraints, MigRowInfo.Alignment.DEFAULT);
    addVerticalAlignmentAction(actions, constraints, MigRowInfo.Alignment.TOP);
    addVerticalAlignmentAction(actions, constraints, MigRowInfo.Alignment.CENTER);
    addVerticalAlignmentAction(actions, constraints, MigRowInfo.Alignment.BOTTOM);
    addVerticalAlignmentAction(actions, constraints, MigRowInfo.Alignment.FILL);
    addVerticalAlignmentAction(actions, constraints, MigRowInfo.Alignment.BASELINE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Horizontal
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addHorizontalAlignmentAction(List<Object> actions,
      List<CellConstraintsSupport> constraints,
      MigColumnInfo.Alignment alignment) {
    boolean isChecked = hasGivenHorizontalAlignment(constraints, alignment);
    IAction action = new HorizontalAlignmentAction(constraints, isChecked, alignment);
    actions.add(action);
  }

  private boolean hasGivenHorizontalAlignment(List<CellConstraintsSupport> constraints,
      MigColumnInfo.Alignment alignment) {
    for (CellConstraintsSupport constraint : constraints) {
      if (constraint.getHorizontalAlignment() != alignment) {
        return false;
      }
    }
    return true;
  }

  private final class HorizontalAlignmentAction extends ObjectInfoAction {
    private final List<CellConstraintsSupport> m_constraints;
    private final MigColumnInfo.Alignment m_alignment;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public HorizontalAlignmentAction(List<CellConstraintsSupport> constraints,
        boolean checked,
        MigColumnInfo.Alignment alignment) {
      super(m_layout, alignment.getText(), alignment.getMenuImage(), AS_RADIO_BUTTON);
      setChecked(checked);
      m_constraints = constraints;
      m_alignment = alignment;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void runEx() throws Exception {
      for (CellConstraintsSupport constraint : m_constraints) {
        constraint.setHorizontalAlignment(m_alignment);
        constraint.write();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Vertical
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addVerticalAlignmentAction(List<Object> actions,
      List<CellConstraintsSupport> constraints,
      MigRowInfo.Alignment alignment) {
    boolean isChecked = hasGivenVerticalAlignment(constraints, alignment);
    IAction action = new VerticalAlignmentAction(constraints, isChecked, alignment);
    actions.add(action);
  }

  private boolean hasGivenVerticalAlignment(List<CellConstraintsSupport> constraints,
      MigRowInfo.Alignment alignment) {
    for (CellConstraintsSupport constraint : constraints) {
      if (constraint.getVerticalAlignment() != alignment) {
        return false;
      }
    }
    return true;
  }

  private final class VerticalAlignmentAction extends ObjectInfoAction {
    private final List<CellConstraintsSupport> m_constraints;
    private final MigRowInfo.Alignment m_alignment;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public VerticalAlignmentAction(List<CellConstraintsSupport> constraints,
        boolean checked,
        MigRowInfo.Alignment alignment) {
      super(m_layout, alignment.getText(), alignment.getMenuImage(), AS_RADIO_BUTTON);
      setChecked(checked);
      m_constraints = constraints;
      m_alignment = alignment;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void runEx() throws Exception {
      for (CellConstraintsSupport constraint : m_constraints) {
        constraint.setVerticalAlignment(m_alignment);
        constraint.write();
      }
    }
  }
}