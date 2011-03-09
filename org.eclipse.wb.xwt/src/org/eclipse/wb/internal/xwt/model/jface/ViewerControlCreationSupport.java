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

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.xwt.parser.XwtParserBindToElement;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

import java.lang.reflect.Method;

/**
 * {@link CreationSupport} for {@link Control} of {@link Viewer}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.jface
 */
public class ViewerControlCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport {
  private final ViewerInfo m_viewer;
  private final Method m_method;
  private final String m_property;
  private DocumentElement m_element;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerControlCreationSupport(ViewerInfo viewer, Method method, String property) {
    m_viewer = viewer;
    m_method = method;
    m_property = property;
    viewer.addBroadcastListener(new XmlObjectSetObjectAfter() {
      public void invoke(XmlObjectInfo target, Object o) throws Exception {
        if (target == m_viewer) {
          Object object = m_method.invoke(o);
          m_object.setObject(object);
        }
      }
    });
    // prepare "element"
    {
      DocumentElement viewerElement = m_viewer.getCreationSupport().getElement();
      String controlTag = viewerElement.getTag() + "." + m_property;
      m_element = viewerElement.getChild(controlTag, true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    if (m_element == null) {
      return "<" + getTitle() + "?>";
    }
    return ElementCreationSupport.getElementString(m_element);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() {
    return m_viewer.getCreationSupport().getTitle() + "." + m_property;
  }

  @Override
  public void setObject(XmlObjectInfo object) throws Exception {
    super.setObject(object);
    if (m_element != null) {
      m_viewer.getBroadcast(XwtParserBindToElement.class).invoke(m_object, m_element);
    }
  }

  @Override
  public DocumentElement getElement() {
    if (m_element == null) {
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          DocumentElement viewerElement = m_viewer.getCreationSupport().getElement();
          // create "control" element
          m_element = new DocumentElement();
          m_element.setTag(viewerElement.getTag() + "." + m_property);
          // add it
          viewerElement.addChild(m_element, 0);
        }
      });
    }
    return m_element;
  }

  @Override
  public DocumentElement getElementMove() {
    return m_viewer.getCreationSupport().getElementMove();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void delete() throws Exception {
    m_viewer.delete();
    // remove from parent
    m_object.getParent().removeChild(m_object);
  }
}
