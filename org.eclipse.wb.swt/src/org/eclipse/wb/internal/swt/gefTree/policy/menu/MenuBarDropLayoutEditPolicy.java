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
package org.eclipse.wb.internal.swt.gefTree.policy.menu;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator.LayoutRequestValidatorStubFalse;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;

/**
 * {@link LayoutEditPolicy} allowing drop "bar" {@link MenuInfo} on <code>Shell</code>.
 *
 * @author mitin_aa
 * @coverage swt.gefTree.policy.menu
 */
public class MenuBarDropLayoutEditPolicy extends LayoutEditPolicy {
  private final CompositeInfo m_shell;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuBarDropLayoutEditPolicy(CompositeInfo shell) {
    m_shell = shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy/Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(final Object newObject, Object referenceObject) {
    final MenuInfo menu = (MenuInfo) newObject;
    return new EditCommand(m_shell) {
      @Override
      protected void executeEdit() throws Exception {
        menu.command_CREATE(m_shell);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator instance
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ILayoutRequestValidator VALIDATOR = new LayoutRequestValidatorStubFalse() {
    @Override
    public boolean validateCreateRequest(EditPart host, CreateRequest request) {
      // only one "bar"
      for (MenuInfo menuInfo : m_shell.getChildren(MenuInfo.class)) {
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
  };
}
