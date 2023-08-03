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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.widgets.Group;

import org.junit.Test;

/**
 * Tests for {@link MorphingSupport} and RCP.
 *
 * @author scheglov_ke
 */
public class MorphingSupportTest extends RcpModelTest {
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
	// Morphing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If we cache {@link JavaInfo} in {@link Expression}, then after morphing we think that this
	 * {@link Expression} still represents old {@link JavaInfo}.
	 */
	@Test
	public void test_keepChildren() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Composite composite = new Composite(this, SWT.NONE);",
						"    Button button = new Button(composite, SWT.NONE);",
						"    button.setBounds(10, 10, 100, 50);",
						"  }",
						"}");
		shell.refresh();
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new Composite(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {new: org.eclipse.swt.widgets.Composite} {local-unique: composite} {/new Composite(this, SWT.NONE)/ /new Button(composite, SWT.NONE)/}",
				"    {implicit-layout: absolute} {implicit-layout} {}",
				"    {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(composite, SWT.NONE)/ /button.setBounds(10, 10, 100, 50)/}");
		// do morphing
		{
			CompositeInfo myPanel = (CompositeInfo) shell.getChildrenControls().get(0);
			MorphingTargetDescription morphingTarget = new MorphingTargetDescription(Group.class, null);
			morph(myPanel, morphingTarget);
		}
		// check result
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    Group composite = new Group(this, SWT.NONE);",
				"    Button button = new Button(composite, SWT.NONE);",
				"    button.setBounds(10, 10, 100, 50);",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {new: org.eclipse.swt.widgets.Group} {local-unique: composite} {/new Button(composite, SWT.NONE)/ /new Group(this, SWT.NONE)/}",
				"    {implicit-layout: absolute} {implicit-layout} {}",
				"    {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(composite, SWT.NONE)/ /button.setBounds(10, 10, 100, 50)/}");
		// refresh() should work
		shell.refresh();
		assertNoErrors(shell);
	}

	/**
	 * Performs morphing of {@link JavaInfo} into given target.
	 */
	private static void morph(JavaInfo javaInfo, MorphingTargetDescription target) throws Exception {
		MorphingSupport.morph("org.eclipse.swt.widgets.Control", javaInfo, target);
	}
}
