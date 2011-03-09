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
package org.eclipse.wb.internal.ercp.gefTree.policy;

import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.CommandInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link LayoutEditPolicy} for dropping {@link CommandInfo} on {@link ControlInfo}.
 * 
 * @author scheglov_ke
 * @coverage swt.gef.policy
 */
public final class CommandDropLayoutEditPolicy extends ObjectLayoutEditPolicy<CommandInfo> {
  private final ControlInfo m_control;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CommandDropLayoutEditPolicy(ControlInfo control) {
    super(control);
    m_control = control;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof CommandInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ILayoutRequestValidator VALIDATOR =
      LayoutRequestValidators.modelType(CommandInfo.class);

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getPasteCommand(PasteRequest request, Object referenceObject) {
    return null;
  }

  @Override
  protected void command_CREATE(CommandInfo command, CommandInfo reference) throws Exception {
    command.commandCreate(m_control, reference);
  }

  @Override
  protected void command_MOVE(CommandInfo command, CommandInfo reference) throws Exception {
    command.commandMove(m_control, reference);
  }
}
