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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.widgets.TableWrapData;

import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link ITableWrapLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class TableWrapSelectionEditPolicy<C extends IControlInfo>
    extends
      AbstractGridSelectionEditPolicy {
  private final ITableWrapLayoutInfo<C> m_layout;
  private final C m_component;
  private final GridHelper<C> m_gridHelper = new GridHelper<C>(this, false);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableWrapSelectionEditPolicy(ITableWrapLayoutInfo<C> layout, C component) {
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
    // add size hint handle
    handlesList.add(createSizeHandle(IPositionConstants.SOUTH, 0.25));
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
  @SuppressWarnings("unchecked")
  protected Figure createAlignmentFigure(IAbstractComponentInfo component, boolean horizontal) {
    IEditPartViewer viewer = getHost().getViewer();
    try {
      final ITableWrapDataInfo layoutData = m_layout.getTableWrapData2((C) component);
      if (horizontal) {
        return new AbstractPopupFigure(viewer, 9, 5) {
          @Override
          protected Image getImage() {
            return layoutData.getSmallAlignmentImage(true);
          }

          @Override
          protected void fillMenu(IMenuManager manager) {
            layoutData.fillHorizontalAlignmentMenu(manager);
          }
        };
      } else {
        return new AbstractPopupFigure(viewer, 5, 9) {
          @Override
          protected Image getImage() {
            return layoutData.getSmallAlignmentImage(false);
          }

          @Override
          protected void fillMenu(IMenuManager manager) {
            layoutData.fillVerticalAlignmentMenu(manager);
          }
        };
      }
    } catch (Throwable e) {
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command createSizeCommand(final boolean horizontal, final Dimension size) {
    return new EditCommand(m_object) {
      @Override
      protected void executeEdit() throws Exception {
        m_layout.command_setHeightHint(m_component, size.height);
      }
    };
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
        m_layout.command_setCells(m_component, cells, true);
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
        // horizontal
        if (c == 'h') {
          flipGrab(true);
        } else if (c == 'l') {
          setAlignment(true, TableWrapData.LEFT);
        } else if (c == 'c') {
          setAlignment(true, TableWrapData.CENTER);
        } else if (c == 'r') {
          setAlignment(true, TableWrapData.RIGHT);
        } else if (c == 'f') {
          setAlignment(true, TableWrapData.FILL);
        }
        // vertical
        if (c == 'v') {
          flipGrab(false);
        } else if (c == 't') {
          setAlignment(false, TableWrapData.TOP);
        } else if (c == 'm') {
          setAlignment(false, TableWrapData.MIDDLE);
        } else if (c == 'b') {
          setAlignment(false, TableWrapData.BOTTOM);
        } else if (c == 'F') {
          setAlignment(false, TableWrapData.FILL);
        }
      }
    }
  }

  /**
   * Flips the horizontal/vertical grab.
   */
  private void flipGrab(final boolean horizontal) {
    execute(new RunnableEx() {
      @Override
      public void run() throws Exception {
        ITableWrapDataInfo layoutData = m_layout.getTableWrapData2(m_component);
        if (horizontal) {
          layoutData.setHorizontalGrab(!layoutData.getHorizontalGrab());
        } else {
          layoutData.setVerticalGrab(!layoutData.getVerticalGrab());
        }
      }
    });
  }

  /**
   * Sets the horizontal/vertical alignment.
   */
  private void setAlignment(final boolean horizontal, final int alignment) {
    execute(new RunnableEx() {
      @Override
      public void run() throws Exception {
        ITableWrapDataInfo layoutData = m_layout.getTableWrapData2(m_component);
        if (horizontal) {
          layoutData.setHorizontalAlignment(alignment);
        } else {
          layoutData.setVerticalAlignment(alignment);
        }
      }
    });
  }

  /**
   * Executes given {@link RunnableEx} as edit operation.
   */
  private void execute(final RunnableEx runnable) {
    getHost().getViewer().getEditDomain().executeCommand(new EditCommand(m_component) {
      @Override
      protected void executeEdit() throws Exception {
        runnable.run();
      }
    });
  }
}
