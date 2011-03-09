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
package org.eclipse.wb.internal.xwt.model.layout;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

/**
 * Model for virtual {@link LayoutDataInfo}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public final class VirtualLayoutDataCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport {
  private final ControlInfo m_control;
  private final Object m_dataObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualLayoutDataCreationSupport(ControlInfo control, Object dataObject) {
    m_control = control;
    m_dataObject = dataObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "virtual-LayoutData: " + m_object.getDescription().getComponentClass().getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(XmlObjectInfo object) throws Exception {
    super.setObject(object);
    m_object.setObject(m_dataObject);
    m_control.addBroadcastListener(new XmlObjectSetObjectAfter() {
      public void invoke(XmlObjectInfo target, Object object) throws Exception {
        // check, may be this creation support is not active
        if (m_object.getCreationSupport() != VirtualLayoutDataCreationSupport.this) {
          m_control.removeBroadcastListener(this);
          return;
        }
        // OK, check for widget
        if (target == m_control) {
          m_object.setObject(m_dataObject);
        }
      }
    });
  }

  @Override
  public String getTitle() {
    return toString();
  }

  @Override
  public DocumentElement getElement() {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        materialize();
      }
    });
    return m_object.getCreationSupport().getElement();
  }

  private void materialize() throws Exception {
    CreationSupport elementCreationSupport = new ElementCreationSupport();
    m_object.setCreationSupport(elementCreationSupport);
    // add element
    DocumentElement controlElement = m_control.getCreationSupport().getElement();
    Association association = Associations.property("layoutData");
    association.add(m_object, new ElementTarget(controlElement, 0));
  }
}
