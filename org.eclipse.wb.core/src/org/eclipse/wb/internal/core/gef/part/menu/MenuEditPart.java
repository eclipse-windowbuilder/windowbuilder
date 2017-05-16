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
package org.eclipse.wb.internal.core.gef.part.menu;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.SelectEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.gef.policy.menu.MenuLayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.menu.MenuSelectionEditPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;

import java.util.List;

/**
 * {@link EditPart} for {@link IMenuInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public class MenuEditPart extends MenuObjectEditPart {
  // TODO(scheglov) move TOP_LOCATION to shared location
  public static final Point TOP_LOCATION = EnvironmentUtils.IS_MAC
      ? new Point(20, 28)
      : new Point(20, 20);
  private final IMenuInfo m_menu;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuEditPart(Object toolkitModel, IMenuInfo menu) {
    super(toolkitModel, menu);
    m_menu = menu;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IMenuInfo} model.
   */
  public IMenuInfo getMenu() {
    return m_menu;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    if (isStandaloneMenu()) {
      return new MenuImageFigure(m_menu);
    } else if (isSubMenu()) {
      return new MenuImageFigure(m_menu);
    } else {
      return new Figure();
    }
  }
  @Override
  protected void refreshVisuals() {
    // edit parts hierarchy:
    // 1. bar -> menu item -> ...
    // 2. component -> bar -> menu item -> menu container -> menu item -> ...
    // 3. component -> popup -> menu container -> menu item -> ...
    if (isStandaloneMenu()) {
      Rectangle bounds = m_menu.getBounds();
      bounds = bounds.getCopy().setLocation(TOP_LOCATION);
      getFigure().setBounds(bounds);
    } else if (isSubMenu()) {
      Dimension size = m_menu.getBounds().getSize();
      // prepare parent location
      Rectangle parentBounds;
      {
        Figure parentFigure = ((GraphicalEditPart) getParent()).getFigure();
        parentBounds = parentFigure.getBounds().getCopy();
        FigureUtils.translateFigureToAbsolute(parentFigure, parentBounds);
      }
      // prepare "menu" location
      Point figureLocation;
      if (getParent() instanceof MenuPopupEditPart) {
        // parent is popup
        figureLocation = parentBounds.getBottomLeft();
      } else if (getParent().getParent() instanceof MenuEditPart) {
        // parent is item
        IMenuInfo parentItemMenu = ((MenuEditPart) getParent().getParent()).m_menu;
        if (parentItemMenu.isHorizontal()) {
          figureLocation = parentBounds.getBottomLeft();
        } else {
          figureLocation = parentBounds.getTopRight().getTranslated(-3, -2);
        }
      } else {
        figureLocation = parentBounds.getTopRight().getTranslated(-3, -2);
      }
      // set bounds
      getFigure().setBounds(new Rectangle(figureLocation, size));
    } else {
      // menu on some other component
      getFigure().setBounds(m_menu.getBounds());
    }
  }
  private boolean isStandaloneMenu() {
    return false;
    // TODO(scheglov)
    //    return getParent() instanceof DesignRootEditPart;
  }
  protected final boolean isSubMenu() {
    return getParent() instanceof MenuObjectEditPart;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new MenuLayoutEditPolicy(m_menu));
    installEditPolicy(EditPolicy.SELECTION_ROLE, new MenuSelectionEditPolicy());
  }
  @Override
  public Tool getDragTrackerTool(Request request) {
    // we don't need any move/resize for menu edit part!
    if (isSubMenu()) {
      return new SelectEditPartTracker(this);
    }
    // top level
    return super.getDragTrackerTool(request);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    List<Object> modelItems = Lists.newArrayList();
    for (IMenuItemInfo menuItem : m_menu.getItems()) {
      modelItems.add(menuItem.getModel());
    }
    return modelItems;
  }
}
