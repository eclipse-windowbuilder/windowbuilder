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
package org.eclipse.wb.internal.swing.model.property.editor.font;

import java.awt.Font;

/**
 * Information object about {@link Font}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class NullFontInfo extends FontInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Font getFont() {
    return null;
  }

  @Override
  public String getText() {
    return null;
  }

  @Override
  public String getSource() throws Exception {
    return "null";
  }
}
