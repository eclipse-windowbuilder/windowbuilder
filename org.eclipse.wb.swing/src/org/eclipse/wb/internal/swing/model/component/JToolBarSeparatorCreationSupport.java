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
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.VoidInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;

import org.eclipse.jdt.core.dom.MethodInvocation;

import javax.swing.JToolBar;

/**
 * Implementation of {@link CreationSupport} for {@link JToolBar#addSeparator()} and
 * {@link JToolBar#addSeparator(java.awt.Dimension)}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JToolBarSeparatorCreationSupport extends VoidInvocationCreationSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JToolBarSeparatorCreationSupport(JavaInfo hostJavaInfo,
      MethodDescription description,
      MethodInvocation invocation,
      JavaInfo[] argumentInfos) {
    super(hostJavaInfo, description, invocation);
  }

  public JToolBarSeparatorCreationSupport(JavaInfo hostJavaInfo) {
    super(hostJavaInfo, getMethodDescription(hostJavaInfo));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static MethodDescription getMethodDescription(JavaInfo hostJavaInfo) {
    return hostJavaInfo.getDescription().getMethod("addSeparator()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Object getObject(Object hostObject) throws Exception {
    JToolBar bar = (JToolBar) hostObject;
    int index = bar.getComponentCount() - 1;
    return bar.getComponent(index);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReorder() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String add_getMethodSource() throws Exception {
    return "addSeparator()";
  }
}
