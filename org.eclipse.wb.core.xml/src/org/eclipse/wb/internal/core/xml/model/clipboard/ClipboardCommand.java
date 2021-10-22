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

import java.io.Serializable;

/**
 * Abstract command for copy/paste operation.
 *
 * @author scheglov_ke
 * @coverage XML.model.clipboard
 */
public abstract class ClipboardCommand implements Serializable {
  private static final long serialVersionUID = 0L;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Executes this {@link ClipboardCommand} in context of its {@link XmlObjectInfo}.
   */
  public abstract void execute(XmlObjectInfo object) throws Exception;
}
