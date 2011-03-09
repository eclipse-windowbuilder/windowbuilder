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
package org.eclipse.wb.internal.core.xml.model.association;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.apache.commons.lang.NotImplementedException;

/**
 * {@link Association} which is used only during re-order.
 * 
 * @author scheglov_ke
 * @coverage XML.model.association
 */
public class OrderAssociation extends Association {
  public static final Association INSTANCE = new OrderAssociation();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  OrderAssociation() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "order";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(XmlObjectInfo object, ElementTarget target) throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public void move(XmlObjectInfo object,
      ElementTarget target,
      XmlObjectInfo oldParent,
      XmlObjectInfo newParent) throws Exception {
    Assert.isTrue(newParent == oldParent, "Only reorder is supported.");
    DocumentElement objectElement = object.getCreationSupport().getElementMove();
    DocumentElement elementInOldParent =
        XmlObjectUtils.getElementInParent(oldParent, objectElement);
    // move element
    {
      DocumentElement targetElement = target.getElement();
      int targetIndex = target.getIndex();
      targetElement.moveChild(elementInOldParent, targetIndex);
    }
  }
}
