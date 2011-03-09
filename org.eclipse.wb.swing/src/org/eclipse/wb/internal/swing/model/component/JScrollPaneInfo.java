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

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.awt.Component;
import java.lang.reflect.Method;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * Model for {@link JScrollPane}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JScrollPaneInfo extends AbstractPositionContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JScrollPaneInfo(AstEditor editor,
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
   * @return <code>true</code> if position is empty.
   */
  public boolean isEmptyPosition(String methodName) {
    try {
      Object container = getObject();
      Method method = container.getClass().getMethod(methodName);
      JViewport viewport = (JViewport) method.invoke(container);
      if (viewport == null) {
        return true;
      }
      //
      Component component = viewport.getView();
      return getChildByObject(component) == null;
    } catch (Throwable e) {
    }
    return false;
  }
}
