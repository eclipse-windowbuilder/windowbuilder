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
package org.eclipse.wb.internal.xwt.gef.policy;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator.LayoutRequestValidatorStubFalse;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;
import org.eclipse.wb.internal.xwt.model.widgets.ShellInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuInfo;

/**
 * {@link LayoutEditPolicy} allowing drop "bar" {@link MenuInfo} on {@link ShellInfo}.
 *
 * @author mitin_aa
 * @coverage XWT.gef.policy
 */
public class MenuBarDropLayoutEditPolicy extends LayoutEditPolicy {
  private final ShellInfo m_shell;
  private final ILayoutRequestValidator m_validator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuBarDropLayoutEditPolicy(ShellInfo shell) {
    m_shell = shell;
    m_validator = new MenuBarDrop_Validator(shell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return m_validator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_fillFeedback;

  @Override
  protected void showLayoutTargetFeedback(Request request) {
    if (m_fillFeedback == null) {
      // create figure
      m_fillFeedback = new Figure() {
        @Override
        protected void paintClientArea(Graphics graphics) {
          // draw placeholder text
          Rectangle bounds = getBounds();
          graphics.setForegroundColor(IColorConstants.darkGreen);
          String menuBarText = "Menu bar would be placed here";
          Dimension textExtent = graphics.getTextExtent(menuBarText);
          int x = bounds.width / 2 - textExtent.width / 2;
          int y = bounds.height / 2 - textExtent.height / 2;
          graphics.drawString(menuBarText, x, y);
        }
      };
      m_fillFeedback.setOpaque(true);
      m_fillFeedback.setBackground(IColorConstants.menuBackground);
      // set figure bounds
      Insets clientAreaInsets = m_shell.getClientAreaInsets();
      final Rectangle bounds = getHostFigure().getBounds().getCopy();
      bounds.width -= clientAreaInsets.getWidth();
      if (EnvironmentUtils.IS_MAC) {
        bounds.x = AbstractComponentEditPart.TOP_LOCATION.x;
        bounds.y = MenuEditPartFactory.MENU_Y_LOCATION;
      } else {
        bounds.x += clientAreaInsets.left;
        bounds.y += clientAreaInsets.top;
      }
      ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          bounds.height = ToolkitSupport.getDefaultMenuBarHeight();
        }
      });
      m_fillFeedback.setBounds(bounds);
      // add some border
      m_fillFeedback.setBorder(new LineBorder(IColorConstants.menuBackgroundSelected, 1));
      addFeedback(m_fillFeedback);
    }
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    if (m_fillFeedback != null) {
      removeFeedback(m_fillFeedback);
      m_fillFeedback = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    final MenuInfo menu = (MenuInfo) request.getNewObject();
    return new EditCommand(m_shell) {
      @Override
      protected void executeEdit() throws Exception {
        menu.commandCreate(m_shell);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final class MenuBarDrop_Validator extends LayoutRequestValidatorStubFalse {
    private final ShellInfo m_m_shell;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuBarDrop_Validator(ShellInfo m_shell) {
      m_m_shell = m_shell;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean validateCreateRequest(EditPart host, CreateRequest request) {
      // only one "bar"
      for (MenuInfo menuInfo : m_m_shell.getChildren(MenuInfo.class)) {
        if (menuInfo.isBar()) {
          return false;
        }
      }
      // check object
      Object newObject = request.getNewObject();
      if (newObject instanceof MenuInfo) {
        return ((MenuInfo) newObject).isBar();
      }
      // unknown object
      return false;
    }
  }
}
