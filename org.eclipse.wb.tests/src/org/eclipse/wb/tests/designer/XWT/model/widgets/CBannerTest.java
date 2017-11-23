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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.xwt.model.widgets.AbstractPositionInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CBannerInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link CBannerInfo}.
 * 
 * @author scheglov_ke
 */
public class CBannerTest extends XwtModelTest {
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
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTestSource_namespaces() {
    return super.getTestSource_namespaces() + " xmlns:c='clr-namespace:org.eclipse.swt.custom'";
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
    CBannerInfo banner =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CBanner/>");
    // no "real" Control's
    assertNull(banner.getControl("noSuchMethod"));
    assertNull(banner.getControl("left"));
    assertNull(banner.getControl("right"));
    assertNull(banner.getControl("bottom"));
  }

  /**
   * Test for {@link CBannerInfo#getControl(String)}.
   */
  public void test_children() throws Exception {
    CBannerInfo banner =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CBanner>",
            "  <CBanner.bottom>",
            "    <Button wbp:name='button'/>",
            "  </CBanner.bottom>",
            "</CBanner>");
    ControlInfo button = getObjectByName("button");
    assertSame(button, banner.getControl("bottom"));
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
        "<CBanner>",
        "  <CBanner.bottom>",
        "    <Button wbp:name='button'/>",
        "  </CBanner.bottom>",
        "</CBanner>");
    ControlInfo button = getObjectByName("button");
    assertEquals("bottom - Button", ObjectsLabelProvider.INSTANCE.getText(button));
  }

  /**
   * Even when no "real" {@link ControlInfo} children, tree still has {@link AbstractPositionInfo}
   * placeholders.
   */
  public void test_getChildrenTree_placeholders() throws Exception {
    CBannerInfo banner = parse("<CBanner/>");
    // no "real" Control's, but in "tree" we have position placeholder children
    List<ObjectInfo> children = banner.getPresentation().getChildrenTree();
    assertThat(children).hasSize(3);
    assertThat(GenericsUtils.select(children, AbstractPositionInfo.class)).hasSize(3);
    assertEquals("left", ObjectsLabelProvider.INSTANCE.getText(children.get(0)));
    assertEquals("right", ObjectsLabelProvider.INSTANCE.getText(children.get(1)));
    assertEquals("bottom", ObjectsLabelProvider.INSTANCE.getText(children.get(2)));
  }
}