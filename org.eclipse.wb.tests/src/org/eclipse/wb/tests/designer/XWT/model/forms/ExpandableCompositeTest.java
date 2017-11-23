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
package org.eclipse.wb.tests.designer.XWT.model.forms;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.xwt.model.forms.ExpandableCompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.AbstractPositionInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link ExpandableCompositeInfo}.
 * 
 * @author scheglov_ke
 */
public class ExpandableCompositeTest extends XwtModelTest {
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
  /**
   * No any children {@link ControlInfo}'s, so for all positions <code>null</code>.
   */
  public void test_childrenNo() throws Exception {
    ExpandableCompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<!-- Forms API -->",
            "<ExpandableComposite/>");
    // no "real" Control's
    assertNull(composite.getControl("noSuchMethod"));
    assertNull(composite.getControl("textClient"));
    assertNull(composite.getControl("client"));
  }

  /**
   * Test for {@link ExpandableCompositeInfo#getControl(String)}.
   */
  public void test_children() throws Exception {
    ExpandableCompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<!-- Forms API -->",
            "<ExpandableComposite>",
            "  <ExpandableComposite.client>",
            "    <Button wbp:name='button'/>",
            "  </ExpandableComposite.client>",
            "</ExpandableComposite>");
    ControlInfo button = getObjectByName("button");
    assertSame(button, composite.getControl("client"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Each {@link ControlInfo} text is decorated with its position method.
   */
  public void test_presentation_decorateText() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<!-- Forms API -->",
        "<ExpandableComposite>",
        "  <ExpandableComposite.client>",
        "    <Button wbp:name='button'/>",
        "  </ExpandableComposite.client>",
        "</ExpandableComposite>");
    ControlInfo button = getObjectByName("button");
    assertEquals("client - Button", ObjectsLabelProvider.INSTANCE.getText(button));
  }

  /**
   * Even when no "real" {@link ControlInfo} children, tree still has {@link AbstractPositionInfo}
   * placeholders.
   */
  public void test_getChildrenTree_placeholders() throws Exception {
    ExpandableCompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<!-- Forms API -->",
            "<ExpandableComposite/>");
    // no "real" Control's, but in "tree" we have position placeholder children
    List<ObjectInfo> children = composite.getPresentation().getChildrenTree();
    assertThat(children).hasSize(2);
    assertEquals("textClient", ObjectsLabelProvider.INSTANCE.getText(children.get(0)));
    assertEquals("client", ObjectsLabelProvider.INSTANCE.getText(children.get(1)));
  }
}