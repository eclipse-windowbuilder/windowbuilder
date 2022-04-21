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
package org.eclipse.wb.internal.core.databinding.xml.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectMove;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;

/**
 * Helper class for auto synchronize {@link XmlObjectInfo}'s and observe, binding info's.
 *
 * @author lobas_av
 * @coverage bindings.xml.model
 */
public final class SynchronizeManager {
  private final IDatabindingsProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SynchronizeManager(IDatabindingsProvider provider, XmlObjectInfo xmlObjectRoot) {
    m_provider = provider;
    xmlObjectRoot.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        synchronizeObserves();
      }
    });
    xmlObjectRoot.addBroadcastListener(new XmlObjectAdd() {
      @Override
      public void after(ObjectInfo parent, final XmlObjectInfo child) throws Exception {
        child.addBroadcastListener(new XmlObjectSetObjectAfter() {
          @Override
          public void invoke(XmlObjectInfo target, Object o) throws Exception {
            if (child == target) {
              target.removeBroadcastListener(this);
              synchronizeObserves();
            }
          }
        });
      }
    });
    xmlObjectRoot.addBroadcastListener(new XmlObjectMove() {
      @Override
      public void after(XmlObjectInfo child, ObjectInfo oldParent, ObjectInfo newParent)
          throws Exception {
        synchronizeObserves();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private void synchronizeObserves() throws Exception {
    m_provider.synchronizeObserves();
  }
}