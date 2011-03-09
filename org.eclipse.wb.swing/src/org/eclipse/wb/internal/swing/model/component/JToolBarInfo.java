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
package org.eclipse.wb.internal.swing.model.component;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * Model for {@link JToolBar}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JToolBarInfo extends ContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JToolBarInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link JToolBar} is horizontally oriented.
   */
  public boolean isHorizontal() {
    JToolBar bar = (JToolBar) getObject();
    return bar.getOrientation() == SwingConstants.HORIZONTAL;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ComponentInfo}.
   */
  public void command_CREATE(ComponentInfo component, ComponentInfo nextComponent) throws Exception {
    JavaInfoUtils.add(component, getAssociation_(), this, nextComponent);
  }

  /**
   * Creates new {@link ComponentInfo} for {@link JButton} using {@link ActionInfo}.
   * 
   * @return the created {@link ComponentInfo}.
   */
  public ComponentInfo command_CREATE(ActionInfo action, ComponentInfo nextComponent)
      throws Exception {
    // ensure that ActionInfo is already added
    if (action.getParent() == null) {
      ActionContainerInfo.add(getRootJava(), action);
    }
    // prepare CreationSupport
    CreationSupport creationSupport;
    {
      String source = TemplateUtils.format("add({0})", action);
      creationSupport = new ImplicitFactoryCreationSupport("add(javax.swing.Action)", source);
    }
    // create JButton
    ComponentInfo newButton =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(getEditor(), JButton.class, creationSupport);
    JavaInfoUtils.add(newButton, AssociationObjects.invocationVoid(), this, nextComponent);
    getBroadcastObject().select(ImmutableList.of(newButton));
    return newButton;
  }

  /**
   * Creates new {@link JToolBarSeparatorInfo}.
   */
  public void command_CREATE(JToolBarSeparatorInfo separator, ComponentInfo nextComponent)
      throws Exception {
    JavaInfoUtils.add(
        separator,
        new VoidInvocationVariableSupport(separator),
        PureFlatStatementGenerator.INSTANCE,
        AssociationObjects.invocationVoid(),
        this,
        nextComponent);
  }

  /**
   * Moves child {@link ComponentInfo}.
   */
  public void command_MOVE(ComponentInfo component, ComponentInfo nextComponent) throws Exception {
    JavaInfoUtils.move(component, getAssociation_(), this, nextComponent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AssociationObject} for standard association with {@link JToolBar}.
   */
  private static AssociationObject getAssociation_() throws Exception {
    return AssociationObjects.invocationChild("%parent%.add(%child%)", false);
  }
}
