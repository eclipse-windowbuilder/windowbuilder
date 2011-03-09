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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;

import org.eclipse.swt.widgets.Layout;

/**
 * Implementation of {@link CreationSupport} for implicit {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public final class ImplicitLayoutCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport {
  private final CompositeInfo m_composite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImplicitLayoutCreationSupport(CompositeInfo composite) {
    m_composite = composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    Class<?> layoutClass = m_object.getDescription().getComponentClass();
    // check for absolute layout
    if (layoutClass == null) {
      return "implicit-layout: absolute";
    }
    // "real" layout
    return "implicit-layout: " + layoutClass.getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public DocumentElement getElement() {
    return m_composite.getCreationSupport().getElement();
  }

  @Override
  public String getTitle() {
    return toString();
  }

  @Override
  public void setObject(XmlObjectInfo object) throws Exception {
    super.setObject(object);
    m_composite.addBroadcastListener(m_objectListener1);
    m_composite.addBroadcastListener(m_objectListener2);
    m_composite.addBroadcastListener(m_objectListener3);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void delete() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Object m_objectListener1 = new ObjectInfoChildAddBefore() {
    public void invoke(ObjectInfo parent, ObjectInfo child, ObjectInfo[] nextChild)
        throws Exception {
      if (isAddLayout(parent, child) && parent.getChildren().contains(m_object)) {
        if (nextChild[0] == m_object) {
          nextChild[0] = GenericsUtils.getNextOrNull(parent.getChildren(), m_object);
        }
        parent.removeChild(m_object);
      }
    }
  };
  private final Object m_objectListener2 = new ObjectEventListener() {
    @Override
    public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
      if (canAddImplicitLayout() && isAddLayout(parent, child)) {
        parent.addChild(m_object);
      }
    }
  };
  private final Object m_objectListener3 = new XmlObjectSetObjectAfter() {
    public void invoke(XmlObjectInfo target, Object o) throws Exception {
      if (target == m_composite) {
        Layout layout = m_composite.getComposite().getLayout();
        m_object.setObject(layout);
      }
    }
  };

  /**
   * @return <code>true</code> if implicit layout should be added.
   */
  private boolean canAddImplicitLayout() {
    return m_composite.getArbitraryValue(CompositeInfo.KEY_LAYOUT_REPLACING) != Boolean.TRUE;
  }

  /**
   * @return <code>true</code> if given combination of parent/child is adding new {@link LayoutInfo}
   *         on our {@link LayoutContainer_Info}.
   */
  private boolean isAddLayout(ObjectInfo parent, ObjectInfo child) {
    return parent == m_composite && child instanceof LayoutInfo && child != m_object;
  }
}
