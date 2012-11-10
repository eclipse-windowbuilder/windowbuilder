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
package org.eclipse.wb.internal.swing.gef.policy.menu;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;

import java.util.List;

/**
 * {@link LayoutEditPolicy} allowing drop {@link JPopupMenuInfo} on {@link ComponentInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public class MenuPopupDropLayoutEditPolicy extends LayoutEditPolicy {
  private final ComponentInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuPopupDropLayoutEditPolicy(ComponentInfo component) {
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void showLayoutTargetFeedback(Request request) {
    PolicyUtils.showBorderTargetFeedback(this);
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    PolicyUtils.eraseBorderTargetFeedback(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(final CreateRequest request) {
    if (!canAcceptPopup()) {
      return null;
    }
    // OK, we can add new "popup"
    return new EditCommand(m_component) {
      @Override
      protected void executeEdit() throws Exception {
        JPopupMenuInfo popup = (JPopupMenuInfo) request.getNewObject();
        popup.command_CREATE(m_component);
      }
    };
  }

  @Override
  protected Command getPasteCommand(PasteRequest request) {
    if (!canAcceptPopup()) {
      return null;
    }
    // OK, we can paste "popup"
    return LayoutPolicyUtils2.getPasteCommand(
        m_component,
        request,
        JPopupMenuInfo.class,
        new IPasteProcessor<JPopupMenuInfo>() {
          public void process(JPopupMenuInfo popup) throws Exception {
            popup.command_CREATE(m_component);
          }
        });
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    return null;
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    if (!canAcceptPopup()) {
      return null;
    }
    // OK, we can move "popup"
    final JPopupMenuInfo popup = (JPopupMenuInfo) request.getEditParts().get(0).getModel();
    return new EditCommand(m_component) {
      @Override
      protected void executeEdit() throws Exception {
        popup.command_MOVE(m_component);
      }
    };
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator instance
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ILayoutRequestValidator VALIDATOR = new ILayoutRequestValidator() {
    public boolean validateCreateRequest(EditPart host, CreateRequest request) {
      return request.getNewObject() instanceof JPopupMenuInfo;
    }

    public boolean validatePasteRequest(EditPart host, final PasteRequest request) {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
        public Boolean runObject() throws Exception {
          // check that memento contains JPopupMenu_Info
          @SuppressWarnings("unchecked")
          List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
          if (mementos.size() == 1) {
            JavaInfo javaInfo = mementos.get(0).create(m_component);
            if (javaInfo instanceof JPopupMenuInfo) {
              request.setObject(javaInfo);
              return true;
            }
          }
          // not a JPopupMenu_Info
          return false;
        }
      }, false);
    }

    public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
      return request.getEditParts().size() == 1
          && request.getEditParts().get(0).getModel() instanceof JPopupMenuInfo;
    }

    public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
      if (request.getEditParts().size() == 1) {
        Object object = request.getEditParts().get(0).getModel();
        return object instanceof JPopupMenuInfo;
      }
      return false;
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Checks
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if new {@link JPopupMenuInfo} can be added on this
   *         {@link ComponentInfo}.
   */
  private boolean canAcceptPopup() {
    return !hasChildPopup() && !isPartOfMenu();
  }

  /**
   * @return <code>true</code> if this {@link ComponentInfo} already has {@link JPopupMenuInfo}.
   */
  private boolean hasChildPopup() {
    return !m_component.getChildren(JPopupMenuInfo.class).isEmpty();
  }

  /**
   * @return <code>true</code> if this {@link ComponentInfo} is already located on
   *         {@link JMenuBarInfo} or {@link JPopupMenuInfo}.
   */
  private boolean isPartOfMenu() {
    for (ObjectInfo object = m_component; object != null; object = object.getParent()) {
      if (object instanceof JMenuBarInfo || object instanceof JPopupMenuInfo) {
        return true;
      }
    }
    return false;
  }
}
