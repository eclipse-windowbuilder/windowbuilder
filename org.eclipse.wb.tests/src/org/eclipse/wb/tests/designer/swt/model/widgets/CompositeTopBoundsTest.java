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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Basic tests for {@link CompositeTopBoundsSupport}.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public class CompositeTopBoundsTest extends RcpGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Size in properties of {@link IResource}.
   */
  public void test_resize_properties() throws Exception {
    Dimension oldSize = new Dimension(450, 300);
    Dimension newSize = new Dimension(400, 350);
    ICompilationUnit unit =
        check_resize_Composite("// no size", oldSize, newSize, newSize, "// no size");
    // close editor, reopen and check for size - it should be same as we set
    {
      TestUtils.closeAllEditors();
      openDesign(unit);
      assertEquals(newSize, canvas.getSize(m_contentEditPart));
    }
  }

  /**
   * Size in properties of {@link IResource} during set <code>pack()</code>.
   */
  public void test_resize_pack() throws Exception {
    Dimension packSize = new Dimension(150, 50);
    Dimension newSize = new Dimension(400, 350);
    String sizeCode;
    if (EnvironmentUtils.IS_WINDOWS) {
      sizeCode = "button.setLayoutData(new RowData("
            + (packSize.width - 3 - 3)
            + ", "
            + (packSize.height - 3 - 3)
            + "));\n"
            + "\t\tpack();";
    } else {
      sizeCode = "button.setLayoutData(new RowData("
          + (packSize.width - 4 - 4)
          + ", "
          + (packSize.height - 4 - 4)
          + "));\n"
          + "\t\tpack();";
    }
    ICompilationUnit unit = check_resize_Composite(sizeCode, packSize, newSize, packSize, sizeCode);
    // close editor, reopen and check for size - it should be same as we set
    {
      TestUtils.closeAllEditors();
      openDesign(unit);
      assertEquals(packSize, canvas.getSize(m_contentEditPart));
    }
  }

  /**
   * Size in <code>setSize(int,int)</code>
   */
  public void test_resize_setSize_ints() throws Exception {
    Dimension oldSize = new Dimension(300, 200);
    Dimension newSize = new Dimension(400, 300);
    check_resize_Composite("setSize(300, 200);", oldSize, newSize, newSize, "setSize(400, 300);");
  }

  /**
   * Size in <code>setSize(Point)</code>
   */
  public void test_resize_setSize_Point() throws Exception {
    Dimension oldSize = new Dimension(300, 200);
    Dimension newSize = new Dimension(400, 300);
    check_resize_Composite(
        "setSize(new Point(300, 200));",
        oldSize,
        newSize,
        newSize,
        "setSize(new Point(400, 300));");
  }

  private ICompilationUnit check_resize_Composite(String oldSizeLine,
      Dimension oldSize,
      Dimension resizeSize,
      Dimension newSize,
      String newSizeLine) throws Exception {
    CompositeInfo composite =
        openComposite(
            "class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new RowLayout());",
            "    Button button = new Button(this, SWT.NONE);",
            "    " + oldSizeLine,
            "  }",
            "}");
    // check size
    assertEquals(oldSize, canvas.getSize(composite));
    waitEventLoop(50);
    // change size
    canvas.beginResize(composite, IPositionConstants.EAST);
    canvas.dragTo(composite, resizeSize.width, 0).endDrag();
    canvas.beginResize(composite, IPositionConstants.SOUTH);
    canvas.dragTo(composite, 0, resizeSize.height).endDrag();
    // check new size
    assertEquals(newSize, canvas.getSize(composite));
    assertEditor(
        "class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new RowLayout());",
        "    Button button = new Button(this, SWT.NONE);",
        "    " + newSizeLine,
        "  }",
        "}");
    //
    return m_lastEditor.getModelUnit();
  }
}