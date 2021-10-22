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

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;

/**
 * Abstract command for {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public abstract class CompositeClipboardCommand extends ClipboardCommand {
  private static final long serialVersionUID = 0L;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void execute(XmlObjectInfo object) throws Exception {
    if (object instanceof CompositeInfo) {
      CompositeInfo composite = (CompositeInfo) object;
      execute(composite);
    }
  }

  /**
   * Implementation of {@link #execute(XmlObjectInfo)} for {@link CompositeInfo}.
   */
  protected abstract void execute(CompositeInfo composite) throws Exception;
}
