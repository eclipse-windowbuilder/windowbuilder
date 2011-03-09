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
package org.eclipse.wb.internal.core.xml.model.broadcast;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * Listener for {@link XmlObjectInfo} move.
 * 
 * @author scheglov_ke
 * @coverage XML.model
 */
public abstract class XmlObjectMove {
  /**
   * Before {@link XmlObjectInfo} moved.
   */
  public void before(XmlObjectInfo child, ObjectInfo oldParent, ObjectInfo newParent)
      throws Exception {
  }

  /**
   * After {@link XmlObjectInfo} moved.
   */
  public void after(XmlObjectInfo child, ObjectInfo oldParent, ObjectInfo newParent)
      throws Exception {
  }
}