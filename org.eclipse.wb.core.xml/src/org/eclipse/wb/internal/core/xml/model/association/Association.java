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

/**
 * Presentation of parent/child link.
 * 
 * @author scheglov_ke
 * @coverage XML.model.association
 */
public abstract class Association {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link DocumentElement} of new {@link XmlObjectInfo}.
   */
  public abstract void add(XmlObjectInfo object, ElementTarget target) throws Exception;

  /**
   * Moves {@link DocumentElement} of existing {@link XmlObjectInfo}.
   */
  public abstract void move(XmlObjectInfo object,
      ElementTarget target,
      XmlObjectInfo oldParent,
      XmlObjectInfo newParent) throws Exception;
}
