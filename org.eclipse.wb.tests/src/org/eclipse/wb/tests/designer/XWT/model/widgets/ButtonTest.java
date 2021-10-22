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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.internal.xwt.model.widgets.ButtonInfo;
import org.eclipse.wb.internal.xwt.model.widgets.StylePresentation;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

/**
 * Tests for {@link ButtonInfo}.
 *
 * @author sablin_aa
 */
public class ButtonTest extends XwtModelTest {
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
  // MenuStylePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link StylePresentation} returns different icons for menus with different styles.
   */
  public void test_StylePresentation() throws Exception {
    parse(
        "<Shell>",
        "  <Button text='Button' wbp:name='button'/>",
        "  <Button x:Style='CHECK' text='Check Button' wbp:name='checkbox'/>",
        "  <Button x:Style='RADIO' text='Radio Button' wbp:name='radio'/>",
        "</Shell>");
    refresh();
    // prepare models
    ButtonInfo button = getObjectByName("button");
    ButtonInfo checkbox = getObjectByName("checkbox");
    ButtonInfo radio = getObjectByName("radio");
    // test icons
    assertNotSame(button.getPresentation().getIcon(), checkbox.getPresentation().getIcon());
    assertNotSame(checkbox.getPresentation().getIcon(), radio.getPresentation().getIcon());
    assertNotSame(radio.getPresentation().getIcon(), button.getPresentation().getIcon());
  }
}