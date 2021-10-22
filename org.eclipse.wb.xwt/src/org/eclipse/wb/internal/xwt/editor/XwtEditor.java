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
package org.eclipse.wb.internal.xwt.editor;

import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;

/**
 * Editor for XWT UI.
 *
 * @author scheglov_ke
 * @coverage XWT.editor
 */
public final class XwtEditor extends AbstractXmlEditor {
  public static final String ID = "org.eclipse.wb.xwt.editor.XwtEditor";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected XmlDesignPage createDesignPage() {
    return new XwtDesignPage();
  }
}
