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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Test for {@link TableTreeViewer}.
 *
 * @author scheglov_ke
 */
public class TableTreeViewerTest extends RcpModelTest {
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
  public void test_properties() throws Exception {
    CompositeInfo shell = parseComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TableTreeViewer tableTreeViewer = new TableTreeViewer(this, SWT.NONE);",
        "  }",
        "}");
    shell.refresh();
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new TableTreeViewer(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {viewer: public org.eclipse.swt.custom.TableTree org.eclipse.jface.viewers.TableTreeViewer.getTableTree()} {viewer} {}",
        "    {new: org.eclipse.jface.viewers.TableTreeViewer} {local-unique: tableTreeViewer} {/new TableTreeViewer(this, SWT.NONE)/}",
        "    {method: public org.eclipse.swt.widgets.Table org.eclipse.swt.custom.TableTree.getTable()} {property} {}");
  }
}