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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.MethodInvocation;

import java.awt.Container;

/**
 * Abstract model for position based {@link ContainerInfo}. {@link Association} should be
 * {@link InvocationChildAssociation} for {@link Container#add(java.awt.Component, Object)}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public abstract class AbstractPositionContainerInfo extends ContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractPositionContainerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ComponentInfo} and associates using given method.
   */
  public final void command_CREATE(ComponentInfo component, String methodName) throws Exception {
    JavaInfoUtils.add(component, getAssociation(methodName), this, null);
  }

  /**
   * Moves child {@link ComponentInfo} to given position.
   */
  public final void command_MOVE(ComponentInfo component, String methodName) throws Exception {
    MethodInvocation invocation = getAssociationInvocation(component);
    getEditor().replaceInvocationName(invocation, methodName);
  }

  /**
   * Reparents {@link ComponentInfo} to given position.
   */
  public final void command_ADD(ComponentInfo component, String methodName) throws Exception {
    JavaInfoUtils.move(component, getAssociation(methodName), this, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AssociationObject} for associating using given method.
   */
  private static AssociationObject getAssociation(String methodName) throws Exception {
    return AssociationObjects.invocationChild("%parent%." + methodName + "(%child%)", false);
  }

  /**
   * @return the {@link MethodInvocation} of given {@link JavaInfo} association.
   */
  private static MethodInvocation getAssociationInvocation(JavaInfo javaInfo) {
    return ((InvocationChildAssociation) javaInfo.getAssociation()).getInvocation();
  }
}
