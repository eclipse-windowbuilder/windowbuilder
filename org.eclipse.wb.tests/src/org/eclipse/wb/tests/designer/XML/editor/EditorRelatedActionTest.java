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

import org.eclipse.wb.internal.core.xml.editor.actions.EditorRelatedAction;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link EditorRelatedAction}.
 *
 * @author scheglov_ke
 */
public class EditorRelatedActionTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_run() throws Exception {
    // no editor
    assertSame(null, EditorRelatedAction.getActiveEditor());
    // now we have AbstractXMLEditor
    openEditor("<Shell/>");
    assertSame(m_designerEditor, EditorRelatedAction.getActiveEditor());
  }
}
