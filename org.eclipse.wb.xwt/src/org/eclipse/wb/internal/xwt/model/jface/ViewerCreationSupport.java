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
package org.eclipse.wb.internal.xwt.model.jface;

import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ILiveCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

import java.lang.reflect.Method;

/**
 * {@link CreationSupport} for creating {@link Control}, wrapped into {@link Viewer}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.jface
 */
public class ViewerCreationSupport extends CreationSupport implements ILiveCreationSupport {
  private final ViewerInfo m_viewer;
  private final Method m_method;
  private final String m_property;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerCreationSupport(ViewerInfo viewer, Method method, String property) {
    m_viewer = viewer;
    m_method = method;
    m_property = property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addElement(DocumentElement parent, int index) throws Exception {
    m_viewer.getCreationSupport().addElement(parent, index);
    m_object.setCreationSupport(new ViewerControlCreationSupport(m_viewer, m_method, m_property));
    m_object.addChild(m_viewer);
    // configure using script
    {
      String script = XmlObjectUtils.getParameter(m_viewer, "viewer.configureNew");
      if (script != null) {
        ScriptUtils.evaluate(
            m_viewer.getContext().getClassLoader(),
            script,
            "viewer",
            m_viewer,
            "control",
            m_object);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILiveCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationSupport getLiveComponentCreation() {
    return ((ILiveCreationSupport) m_viewer.getCreationSupport()).getLiveComponentCreation();
  }
}
