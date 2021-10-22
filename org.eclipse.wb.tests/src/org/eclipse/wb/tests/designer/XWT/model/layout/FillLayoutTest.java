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
package org.eclipse.wb.tests.designer.XWT.model.layout;

import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.xwt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

/**
 * Test for {@link FillLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class FillLayoutTest extends XwtModelTest {
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
  public void test_setLayout() throws Exception {
    CompositeInfo shell = parse("<Shell/>");
    FillLayoutInfo layout = createObject("org.eclipse.swt.layout.FillLayout");
    shell.setLayout(layout);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isHorizontal()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FillLayoutInfo#isHorizontal()}.
   */
  public void test_isHorizontal_default() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    shell.refresh();
    FillLayoutInfo layout = (FillLayoutInfo) shell.getLayout();
    //
    assertTrue(layout.isHorizontal());
  }

  /**
   * Test for {@link FillLayoutInfo#isHorizontal()}.
   */
  public void test_isHorizontal_true() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout type='HORIZONTAL'/>",
            "  </Shell.layout>",
            "</Shell>");
    shell.refresh();
    FillLayoutInfo layout = (FillLayoutInfo) shell.getLayout();
    //
    assertTrue(layout.isHorizontal());
  }

  /**
   * Test for {@link FillLayoutInfo#isHorizontal()}.
   */
  public void test_isHorizontal_false() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout type='VERTICAL'/>",
            "  </Shell.layout>",
            "</Shell>");
    shell.refresh();
    FillLayoutInfo layout = (FillLayoutInfo) shell.getLayout();
    //
    assertFalse(layout.isHorizontal());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard_copyLayout() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Composite wbp:name='composite'>",
            "    <Composite.layout>",
            "      <FillLayout/>",
            "    </Composite.layout>",
            "    <Button text='Button 1'/>",
            "    <Button text='Button 2'/>",
            "  </Composite>",
            "</Shell>");
    refresh();
    CompositeInfo composite = getObjectByName("composite");
    //
    shell.startEdit();
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(composite);
      CompositeInfo newComposite = (CompositeInfo) memento.create(shell);
      shell.getLayout().command_CREATE(newComposite, null);
      memento.apply();
    }
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.layout>",
        "      <FillLayout/>",
        "    </Composite.layout>",
        "    <Button text='Button 1'/>",
        "    <Button text='Button 2'/>",
        "  </Composite>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <FillLayout/>",
        "    </Composite.layout>",
        "    <Button text='Button 1'/>",
        "    <Button text='Button 2'/>",
        "  </Composite>",
        "</Shell>");
  }
}