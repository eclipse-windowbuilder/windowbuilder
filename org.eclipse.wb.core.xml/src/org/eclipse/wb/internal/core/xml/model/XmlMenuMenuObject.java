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
package org.eclipse.wb.internal.core.xml.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;

/**
 * Abstract base for {@link IMenuInfo} implementation, for {@link XmlObjectInfo} models.
 * 
 * @author scheglov_ke
 * @coverage XML.model
 */
public abstract class XmlMenuMenuObject extends AbstractMenuMenuObject {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlMenuMenuObject(XmlObjectInfo component) {
    super(component);
    m_component.addBroadcastListener(new ObjectInfoDelete() {
      private int m_level = 0;

      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (m_level == 0 && isRootFor(child)) {
          fireDeleteListeners(child);
        }
        m_level++;
      }

      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        m_level--;
      }
    });
  }
}
