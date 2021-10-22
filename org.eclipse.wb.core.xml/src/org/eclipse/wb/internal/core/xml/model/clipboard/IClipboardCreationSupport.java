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
package org.eclipse.wb.internal.core.xml.model.clipboard;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;

import java.io.Serializable;

/**
 * {@link CreationSupport} return implementation of this class to create {@link CreationSupport} for
 * pasting its {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.clipboard
 */
public abstract class IClipboardCreationSupport implements Serializable {
  private static final long serialVersionUID = 0L;

  /**
   * @param rootObject
   *          the root {@link XmlObjectInfo} to which new {@link XmlObjectInfo} will be added.
   *
   * @return the {@link CreationSupport} for creating {@link XmlObjectInfo}.
   */
  public abstract CreationSupport create(XmlObjectInfo rootObject) throws Exception;

  /**
   * Notification that {@link XmlObjectInfo} was created using this {@link CreationSupport}.
   */
  public void apply(XmlObjectInfo object) throws Exception {
  }
}
