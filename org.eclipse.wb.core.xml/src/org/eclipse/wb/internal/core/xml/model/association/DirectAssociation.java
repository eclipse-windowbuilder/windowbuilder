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

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

/**
 * {@link Association} for element of child directly in element parent.
 *
 * @author scheglov_ke
 * @coverage XML.model.association
 */
public class DirectAssociation extends Association {
  public static final Association INSTANCE = new DirectAssociation();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected DirectAssociation() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "direct";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(XmlObjectInfo object, ElementTarget target) throws Exception {
    DocumentElement targetElement = target.getElement();
    int targetIndex = target.getIndex();
    object.getCreationSupport().addElement(targetElement, targetIndex);
  }

  @Override
  public void move(XmlObjectInfo object,
      ElementTarget target,
      XmlObjectInfo oldParent,
      XmlObjectInfo newParent) throws Exception {
    DocumentElement objectElement = object.getCreationSupport().getElementMove();
    DocumentElement elementInOldParent =
        XmlObjectUtils.getElementInParent(oldParent, objectElement);
    // move element
    {
      DocumentElement targetElement = target.getElement();
      int targetIndex = target.getIndex();
      targetElement.moveChild(objectElement, targetIndex);
    }
    // remove intermediate element(s)
    if (objectElement != elementInOldParent) {
      elementInOldParent.remove();
    }
  }
}
