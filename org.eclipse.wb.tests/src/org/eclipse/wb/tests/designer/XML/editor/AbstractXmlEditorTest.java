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
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * Test for {@link AbstractXmlEditor}.
 * 
 * @author scheglov_ke
 */
public class AbstractXmlEditorTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getAdapter_StructuredTextEditor() throws Exception {
    openEditor("<Shell/>");
    // we can access it
    Object adapter = m_designerEditor.getAdapter(StructuredTextEditor.class);
    assertSame(m_designerEditor.getXMLEditor(), adapter);
  }
}
