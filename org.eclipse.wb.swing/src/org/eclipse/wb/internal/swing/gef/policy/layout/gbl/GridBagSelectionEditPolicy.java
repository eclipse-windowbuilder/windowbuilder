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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link AbstractGridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class GridBagSelectionEditPolicy extends AbstractGridSelectionEditPolicy {
  private final AbstractGridBagLayoutInfo m_layout;
  private final ComponentInfo m_component;
  private final GridHelper m_gridHelper = new GridHelper(this, false);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridBagSelectionEditPolicy(AbstractGridBagLayoutInfo layout, ComponentInfo component) {
    super(component);
    m_layout = layout;
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isActiveLayout() {
    return m_component.getParent().getChildren().contains(m_layout);
  }

  @Override
  protected IGridInfo getGridInfo() {
    return m_layout.getGridInfo();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handlesList = Lists.newArrayList();
    // add move handle
    handlesList.add(createMoveHandle());
    // add span handles
    {
      handlesList.add(createSpanHandle(IPositionConstants.NORTH, 0.25));
      handlesList.add(createSpanHandle(IPositionConstants.WEST, 0.25));
      handlesList.add(createSpanHandle(IPositionConstants.EAST, 0.75));
      handlesList.add(createSpanHandle(IPositionConstants.SOUTH, 0.75));
    }
    //
    return handlesList;
  }

  @Override
  protected void showPrimarySelection() {
    super.showPrimarySelection();
    m_gridHelper.showGridFeedback();
  }

  @Override
  protected void hideSelection() {
    m_gridHelper.eraseGridFeedback();
    super.hideSelection();
  }

  @Override
  protected Figure createAlignmentFigure(IAbstractComponentInfo component, boolean horizontal) {
    IEditPartViewer viewer = getHost().getViewer();
    final AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
    if (horizontal) {
      return new AbstractPopupFigure(viewer, 9, 5) {
        @Override
        protected Image getImage() {
          switch (constraints.getHorizontalAlignment()) {
            case LEFT :
              return getImage2("h/alignment/left.gif");
            case CENTER :
              return getImage2("h/alignment/center.gif");
            case RIGHT :
              return getImage2("h/alignment/right.gif");
            case FILL :
              return getImage2("h/alignment/fill.gif");
          }
          return null;
        }

        @Override
        protected void fillMenu(IMenuManager manager) {
          constraints.fillHorizontalAlignmentMenu(manager);
        }
      };
    } else {
      return new AbstractPopupFigure(viewer, 5, 9) {
        @Override
        protected Image getImage() {
          switch (constraints.getVerticalAlignment()) {
            case TOP :
              return getImage2("v/alignment/top.gif");
            case CENTER :
              return getImage2("v/alignment/center.gif");
            case BOTTOM :
              return getImage2("v/alignment/bottom.gif");
            case FILL :
              return getImage2("v/alignment/fill.gif");
            case BASELINE :
              return getImage2("v/alignment/baseline.gif");
            case BASELINE_ABOVE :
              return getImage2("v/alignment/baseline_above.gif");
            case BASELINE_BELOW :
              return getImage2("v/alignment/baseline_below.gif");
          }
          return null;
        }

        @Override
        protected void fillMenu(IMenuManager manager) {
          constraints.fillVerticalAlignmentMenu(manager);
        }
      };
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} for {@link AbstractGridBagLayoutInfo}.
   */
  protected final Image getImage2(String name) {
    return AbstractGridBagLayoutInfo.getImage("headers/" + name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Span
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command createSpanCommand(final boolean horizontal, final Rectangle cells) {
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
        if (horizontal) {
          constraints.setX(cells.x);
          constraints.setWidth(cells.width);
        } else {
          constraints.setY(cells.y);
          constraints.setHeight(cells.height);
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Keyboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      if (keyRequest.isPressed()) {
        char c = keyRequest.getCharacter();
        // grab/fill both
        if (c == 'o') {
          setFillBoth();
        }
        // horizontal
        if (c == 'h') {
          flipGrab(true);
        } else if (c == 'l') {
          setAlignment(ColumnInfo.Alignment.LEFT);
        } else if (c == 'c') {
          setAlignment(ColumnInfo.Alignment.CENTER);
        } else if (c == 'r') {
          setAlignment(ColumnInfo.Alignment.RIGHT);
        } else if (c == 'f') {
          setAlignment(ColumnInfo.Alignment.FILL);
        }
        // vertical
        if (c == 'v') {
          flipGrab(false);
        } else if (c == 't') {
          setAlignment(RowInfo.Alignment.TOP);
        } else if (c == 'm') {
          setAlignment(RowInfo.Alignment.CENTER);
        } else if (c == 'b') {
          setAlignment(RowInfo.Alignment.BOTTOM);
        } else if (c == 'F') {
          setAlignment(RowInfo.Alignment.FILL);
        } else if (c == 'L') {
          setAlignment(RowInfo.Alignment.BASELINE);
        } else if (c == 'A') {
          setAlignment(RowInfo.Alignment.BASELINE_ABOVE);
        } else if (c == 'B') {
          setAlignment(RowInfo.Alignment.BASELINE_BELOW);
        }
      }
    }
  }

  /**
   * Sets grab/fill for both dimensions.
   */
  private void setFillBoth() {
    execute(new RunnableEx() {
      public void run() throws Exception {
        AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
        constraints.getColumn().setWeight(1.0);
        constraints.getRow().setWeight(1.0);
        constraints.setAlignment(ColumnInfo.Alignment.FILL, RowInfo.Alignment.FILL);
      }
    });
  }

  /**
   * Flips horizontal/vertical grab.
   */
  private void flipGrab(final boolean horizontal) {
    execute(new RunnableEx() {
      public void run() throws Exception {
        AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
        DimensionInfo dimension;
        if (horizontal) {
          dimension = constraints.getColumn();
        } else {
          dimension = constraints.getRow();
        }
        double weight = dimension.getWeight();
        dimension.setWeight(weight != 0.0 ? 0.0 : 1.0);
      }
    });
  }

  /**
   * Sets the horizontal alignment.
   */
  private void setAlignment(final ColumnInfo.Alignment alignment) {
    execute(new RunnableEx() {
      public void run() throws Exception {
        AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
        constraints.setHorizontalAlignment(alignment);
      }
    });
  }

  /**
   * Sets the vertical alignment.
   */
  private void setAlignment(final RowInfo.Alignment alignment) {
    execute(new RunnableEx() {
      public void run() throws Exception {
        AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
        constraints.setVerticalAlignment(alignment);
      }
    });
  }

  /**
   * Executes given {@link RunnableEx} as edit operation.
   */
  private void execute(final RunnableEx runnable) {
    ExecutionUtils.run(m_component, runnable);
  }
}
