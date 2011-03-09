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
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.VoidInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;

import org.eclipse.jdt.core.dom.MethodInvocation;

import javax.swing.JMenu;

/**
 * Implementation of {@link CreationSupport} for {@link JMenu#addSeparator()}.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swing.model.menu
 */
public final class JMenuSeparatorCreationSupport extends VoidInvocationCreationSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JMenuSeparatorCreationSupport(JavaInfo hostJavaInfo,
      MethodDescription description,
      MethodInvocation invocation,
      JavaInfo[] argumentInfos) {
    super(hostJavaInfo, description, invocation);
  }

  public JMenuSeparatorCreationSupport(JavaInfo hostJavaInfo) {
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
    JMenu menu = (JMenu) hostObject;
    int index = menu.getMenuComponentCount() - 1;
    return menu.getMenuComponent(index);
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
