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
package org.eclipse.wb.tests.designer.ercp.model.jface;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.MethodParameterCreationSupport;
import org.eclipse.wb.internal.core.model.variable.MethodParameterVariableSupport;
import org.eclipse.wb.internal.ercp.model.rcp.ViewPartInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.ercp.ErcpModelTest;

/**
 * Test for {@link ViewPartInfo}.
 * 
 * @author scheglov_ke
 */
public class ViewPartTest extends ErcpModelTest {
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
  public void test_parse() throws Exception {
    ViewPartInfo part =
        parseJavaInfo(
            "class Test extends ViewPart {",
            "  public Test() {",
            "    setTitle('My title');",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NONE);",
            "    container.setLayout(new GridLayout());",
            "  }",
            "  public void setFocus() {",
            "  }",
            "}");
    // no "live" image/size expected
    {
      assertNull(part.getImage());
      assertNull(part.getPreferredSize());
    }
    // check for "title" property of page
    assertEquals("My title", part.getPropertyByTitle("title").getValue());
    // check "parent"
    CompositeInfo parent;
    {
      assertEquals(1, part.getChildrenJava().size());
      parent = (CompositeInfo) part.getChildrenJava().get(0);
      // supports
      assertInstanceOf(MethodParameterCreationSupport.class, parent.getCreationSupport());
      assertInstanceOf(MethodParameterVariableSupport.class, parent.getVariableSupport());
    }
    // check that "parent" has "contents" child
    {
      assertEquals(1, parent.getChildrenControls().size());
      ControlInfo contents = parent.getChildrenControls().get(0);
      assertEquals("container", contents.getVariableSupport().getName());
    }
  }

  public void test_refresh() throws Exception {
    ViewPartInfo part =
        parseJavaInfo(
            "class Test extends ViewPart {",
            "  public Test() {",
            "    setTitle('My title');",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NONE);",
            "    container.setLayout(new GridLayout());",
            "  }",
            "  public void setFocus() {",
            "  }",
            "}");
    part.refresh();
    // check that page has visual presentation
    {
      assertNotNull(part.getObject());
      assertNotNull(part.getImage());
      assertEquals(new Dimension(450, 300), part.getBounds().getSize());
    }
    // ...and parent too
    {
      CompositeInfo parent = (CompositeInfo) part.getChildrenJava().get(0);
      assertNotNull(parent.getObject());
      assertNotNull(parent.getImage());
      // check bounds
      Rectangle modelBounds = parent.getModelBounds();
      Rectangle shotBounds = parent.getBounds();
      assertEquals(new Point(0, 0), modelBounds.getLocation());
      assertTrue(shotBounds.x - modelBounds.x > 1);
      assertTrue(shotBounds.y - modelBounds.y > 10);
    }
    // set different size
    {
      TopBoundsSupport topBoundsSupport = part.getTopBoundsSupport();
      topBoundsSupport.setSize(800, 400);
      // check new size
      part.refresh();
      assertEquals(new Dimension(800, 400), part.getBounds().getSize());
    }
  }
}