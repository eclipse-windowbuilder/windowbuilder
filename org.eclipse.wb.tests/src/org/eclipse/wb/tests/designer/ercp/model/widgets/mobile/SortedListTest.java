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
package org.eclipse.wb.tests.designer.ercp.model.widgets.mobile;

import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.SortedListInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.ercp.ErcpModelTest;

/**
 * Tests for {@link SortedListInfo}.
 * 
 * @author scheglov_ke
 */
public class SortedListTest extends ErcpModelTest {
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
  // "hasFilter"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ConstructorCreationSupport} used without "FILTER" style, so don't allow to change
   * "hasFilter" property.
   */
  public void test_hasFilter_constructor_noFILTERArgument() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    SortedList sortedList = new SortedList(this, SWT.BORDER);",
            "  }",
            "}");
    shell.refresh();
    SortedListInfo sortedList = (SortedListInfo) shell.getChildrenControls().get(0);
    Property hasFilterProperty = sortedList.getPropertyByTitle("hasFilter");
    //
    assertEquals(Boolean.FALSE, hasFilterProperty.getValue());
    hasFilterProperty.setValue(Boolean.TRUE);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    SortedList sortedList = new SortedList(this, SWT.BORDER);",
        "  }",
        "}");
  }

  /**
   * {@link ConstructorCreationSupport} used with 3 argument, but without "FILTER" style, so don't
   * allow to change "hasFilter" property.
   */
  public void test_hasFilter_constructor_noFILTER() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    SortedList sortedList = new SortedList(this, SWT.BORDER, 0);",
            "  }",
            "}");
    shell.refresh();
    SortedListInfo sortedList = (SortedListInfo) shell.getChildrenControls().get(0);
    Property hasFilterProperty = sortedList.getPropertyByTitle("hasFilter");
    //
    assertEquals(Boolean.FALSE, hasFilterProperty.getValue());
    hasFilterProperty.setValue(Boolean.FALSE);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    SortedList sortedList = new SortedList(this, SWT.BORDER, 0);",
        "  }",
        "}");
  }

  /**
   * {@link ConstructorCreationSupport} used with "FILTER" style, so allow to change "hasFilter"
   * property.
   */
  public void test_hasFilter_constructor_FILTER() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    SortedList sortedList = new SortedList(this, SWT.BORDER, SortedList.FILTER);",
            "  }",
            "}");
    shell.refresh();
    SortedListInfo sortedList = (SortedListInfo) shell.getChildrenControls().get(0);
    Property hasFilterProperty = sortedList.getPropertyByTitle("hasFilter");
    // with "FILTER" default value is "true", so set it to FALSE
    assertEquals(Boolean.TRUE, hasFilterProperty.getValue());
    hasFilterProperty.setValue(Boolean.FALSE);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    SortedList sortedList = new SortedList(this, SWT.BORDER, SortedList.FILTER);",
        "    sortedList.hasFilter = false;",
        "  }",
        "}");
  }
}