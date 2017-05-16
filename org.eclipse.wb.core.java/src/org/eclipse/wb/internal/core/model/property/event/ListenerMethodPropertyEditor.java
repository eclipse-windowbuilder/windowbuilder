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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.swt.graphics.Point;

/**
 * Implementation of {@link PropertyEditor} for {@link ListenerMethodProperty}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
final class ListenerMethodPropertyEditor extends TextDisplayPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ListenerMethodPropertyEditor();

  private ListenerMethodPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TextDisplayPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    ListenerMethodProperty methodProperty = (ListenerMethodProperty) property;
    MethodDeclaration method = methodProperty.findStubMethod();
    if (method != null) {
      JavaInfo javaInfo = methodProperty.getJavaInfo();
      int line = javaInfo.getEditor().getLineNumber(method.getStartPosition());
      return "line " + line;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void doubleClick(Property property, Point location) throws Exception {
    openStubMethod(property);
  }

  @Override
  public boolean activate(PropertyTable propertyTable, Property property, Point location)
      throws Exception {
    if (location == null) {
      openStubMethod(property);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens stub method in "Source" page.
   */
  private void openStubMethod(Property property) throws Exception {
    final ListenerMethodProperty methodProperty = (ListenerMethodProperty) property;
    ExecutionUtils.run(methodProperty.getJavaInfo(), new RunnableEx() {
      public void run() throws Exception {
        methodProperty.openStubMethod();
      }
    });
  }
}
