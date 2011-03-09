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
package org.eclipse.wb.tests.designer.editor.action;

import org.eclipse.wb.internal.core.editor.actions.SwitchAction;
import org.eclipse.wb.internal.core.editor.multi.MultiMode;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

/**
 * Test for {@link SwitchAction}.
 * 
 * @author mitin_aa
 */
public class SwitchActionTest extends SwingGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for switching to source and back to design.
   */
  public void test_1() throws Exception {
    openContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "	public Test() {",
        "	}",
        "}");
    MultiMode multiMode = (MultiMode) m_designerEditor.getMultiMode();
    // prepare action
    SwitchAction switchAction;
    {
      switchAction = new SwitchAction();
      switchAction.setActiveEditor(null, m_designerEditor);
    }
    // after "openDesign" the "Design" page is active
    assertFalse(multiMode.getSourcePage().isActive());
    waitEventLoop(10);
    // switch to "Source" using action
    switchAction.run(null);
    waitEventLoop(10);
    assertTrue(multiMode.getSourcePage().isActive());
    // switch to "Design" using action
    switchAction.run(null);
    waitEventLoop(10);
    assertFalse(multiMode.getSourcePage().isActive());
  }
}
