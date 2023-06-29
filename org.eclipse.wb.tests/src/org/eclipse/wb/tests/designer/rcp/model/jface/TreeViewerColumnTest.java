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

import org.eclipse.wb.internal.rcp.model.jface.viewers.ViewerColumnInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Test for {@link ViewerColumnInfo}.
 *
 * @author scheglov_ke
 */
public class TreeViewerColumnTest extends RcpModelTest {
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
	public void test_0() throws Exception {
		parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    TreeViewer treeViewer = new TreeViewer(this, SWT.NONE);",
				"    {",
				"      TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/new TreeViewer(this, SWT.NONE)/}",
				"  {implicit-layout: absolute} {implicit-layout} {}",
				"  {viewer: public org.eclipse.swt.widgets.Tree org.eclipse.jface.viewers.TreeViewer.getTree()} {viewer} {}",
				"    {new: org.eclipse.jface.viewers.TreeViewer} {local-unique: treeViewer} {/new TreeViewer(this, SWT.NONE)/ /new TreeViewerColumn(treeViewer, SWT.NONE)/}",
				"    {viewer: public org.eclipse.swt.widgets.TreeColumn org.eclipse.jface.viewers.TreeViewerColumn.getColumn()} {viewer} {}",
				"      {new: org.eclipse.jface.viewers.TreeViewerColumn} {local-unique: treeViewerColumn} {/new TreeViewerColumn(treeViewer, SWT.NONE)/}");
	}
}
