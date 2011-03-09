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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.IWrapperInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * Model for JFace {@link Viewer}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.jface
 */
public class ViewerInfo extends XmlObjectInfo implements IWrapperInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    StylePropertyEditor.addStyleProperty(this);
    if (GlobalState.isParsing()) {
      addBroadcastListener(new ObjectInfoChildAddAfter() {
        public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
          if (child == ViewerInfo.this) {
            removeBroadcastListener(this);
            rebindHierarchy();
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the wrapped {@link ControlInfo}.
   */
  public ControlInfo getControl() {
    return (ControlInfo) getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWrapperInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  private ControlInfo m_wrappedControl;

  public ControlInfo getWrapped() throws Exception {
    if (m_wrappedControl == null) {
      Method method = getWrapperMethod();
      String property = getPropertyName(method);
      m_wrappedControl =
          (ControlInfo) XmlObjectUtils.createObject(
              getContext(),
              method.getReturnType(),
              new ViewerCreationSupport(this, method, property));
    }
    return m_wrappedControl;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_deleting;

  @Override
  public void delete() throws Exception {
    if (!m_deleting) {
      m_deleting = true;
      ControlInfo wrapped = getControl();
      if (!wrapped.isDeleting()) {
        m_deleting = true;
        try {
          wrapped.delete();
        } finally {
          m_deleting = false;
        }
        return;
      }
    }
    super.delete();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates corresponding {@link ControlInfo} and binds models.
   */
  private void rebindHierarchy() throws Exception {
    Method method = getWrapperMethod();
    String property = getPropertyName(method);
    XmlObjectInfo control =
        XmlObjectUtils.createObject(
            getContext(),
            method.getReturnType(),
            new ViewerControlCreationSupport(this, method, property));
    // do rebind
    ObjectInfo parent = getParent();
    parent.addChild(control, this);
    parent.removeChild(this);
    control.addChild(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Method} which is used to access wrapped {@link Control}.
   */
  private Method getWrapperMethod() throws NoSuchMethodException {
    String methodName = XmlObjectUtils.getParameter(this, "viewer.control.method");
    return getDescription().getComponentClass().getMethod(methodName);
  }

  /**
   * @return the name of property which corresponds to the given getter {@link Method}.
   */
  private static String getPropertyName(Method method) {
    String property = StringUtils.removeStart(method.getName(), "get");
    return StringUtils.uncapitalize(property);
  }
}
