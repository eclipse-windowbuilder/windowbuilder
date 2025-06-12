/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.rcp.model.forms.DetailsPageInfo;

import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IManagedForm;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link DetailsPageInfo}.
 *
 * @author scheglov_ke
 */
public class DetailsPageTest extends AbstractFormsTest {
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
	@Test
	public void test_0() throws Exception {
		DetailsPageInfo page =
				parseJavaInfo(
						"public abstract class Test implements IDetailsPage {",
						"  private IManagedForm managedForm;",
						"  public Test() {",
						"  }",
						"  public void initialize(IManagedForm form) {",
						"    managedForm = form;",
						"  }",
						"  public void createContents(Composite parent) {",
						"    parent.setLayout(new FillLayout());",
						"    Composite composite = managedForm.getToolkit().createComposite(parent, SWT.NONE);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.ui.forms.IDetailsPage} {this} {}",
				"  {parameter} {parent} {/parent.setLayout(new FillLayout())/ /managedForm.getToolkit().createComposite(parent, SWT.NONE)/}",
				"    {new: org.eclipse.swt.layout.FillLayout} {empty} {/parent.setLayout(new FillLayout())/}",
				"    {instance factory: {toolkitAccess} createComposite(org.eclipse.swt.widgets.Composite,int)} {local-unique: composite} {/managedForm.getToolkit().createComposite(parent, SWT.NONE)/}",
				"      {implicit-layout: absolute} {implicit-layout} {}",
				"  {instance factory container}",
				"    {toolkitAccess: managedForm.getToolkit()} {toolkitAccess} {/managedForm.getToolkit().createComposite(parent, SWT.NONE)/}");
		// refresh
		page.refresh();
		assertNoErrors(page);
		assertEquals(page.getBounds().width, 600);
		assertEquals(page.getBounds().height, 500);
		// Set new size.
		// This test intentionally commented.
		// ScrolledForm performs re-flow in async, so requires running messages loop,
		// but we can not do this, because GEF canvas will start painting, and we don't have images, etc.
		/*{
    	page.getTopBoundsSupport().setSize(450, 300);
    	page.refresh();
    	assertEquals(page.getBounds().width, 450);
    	assertEquals(page.getBounds().height, 300);
    }*/
	}

	@Test
	public void test_FormToolkit_inLocalVariable() throws Exception {
		parseJavaInfo(
				"public abstract class Test implements IDetailsPage {",
				"  private IManagedForm managedForm;",
				"  public Test() {",
				"  }",
				"  public void initialize(IManagedForm form) {",
				"    managedForm = form;",
				"  }",
				"  public void createContents(Composite parent) {",
				"    parent.setLayout(new FillLayout());",
				"    FormToolkit toolkit = managedForm.getToolkit();",
				"    Composite composite = toolkit.createComposite(parent, SWT.NONE);",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.ui.forms.IDetailsPage} {this} {}",
				"  {parameter} {parent} {/parent.setLayout(new FillLayout())/ /toolkit.createComposite(parent, SWT.NONE)/}",
				"    {new: org.eclipse.swt.layout.FillLayout} {empty} {/parent.setLayout(new FillLayout())/}",
				"    {instance factory: {toolkitAccess} createComposite(org.eclipse.swt.widgets.Composite,int)} {local-unique: composite} {/toolkit.createComposite(parent, SWT.NONE)/}",
				"      {implicit-layout: absolute} {implicit-layout} {}",
				"  {instance factory container}",
				"    {toolkitAccess: toolkit} {toolkitAccess} {/toolkit.createComposite(parent, SWT.NONE)/}");
	}

	/**
	 * We should ignore "super" invocations, because component class is interface.
	 */
	@Test
	public void test_callDefaultSuper() throws Exception {
		DetailsPageInfo page =
				parseJavaInfo(
						"public abstract class Test implements IDetailsPage {",
						"  private IManagedForm managedForm;",
						"  public Test() {",
						"    super();",
						"  }",
						"  public void initialize(IManagedForm form) {",
						"    managedForm = form;",
						"  }",
						"  public void createContents(Composite parent) {",
						"    parent.setLayout(new FillLayout());",
						"    Composite composite = managedForm.getToolkit().createComposite(parent, SWT.NONE);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.ui.forms.IDetailsPage} {this} {}",
				"  {parameter} {parent} {/parent.setLayout(new FillLayout())/ /managedForm.getToolkit().createComposite(parent, SWT.NONE)/}",
				"    {new: org.eclipse.swt.layout.FillLayout} {empty} {/parent.setLayout(new FillLayout())/}",
				"    {instance factory: {toolkitAccess} createComposite(org.eclipse.swt.widgets.Composite,int)} {local-unique: composite} {/managedForm.getToolkit().createComposite(parent, SWT.NONE)/}",
				"      {implicit-layout: absolute} {implicit-layout} {}",
				"  {instance factory container}",
				"    {toolkitAccess: managedForm.getToolkit()} {toolkitAccess} {/managedForm.getToolkit().createComposite(parent, SWT.NONE)/}");
		// refresh
		page.refresh();
		assertNoErrors(page);
	}

	@Test
	public void test_parseNoUsingFormToolkit() throws Exception {
		DetailsPageInfo page =
				parseJavaInfo(
						"public abstract class Test implements IDetailsPage {",
						"  private IManagedForm managedForm;",
						"  public Test() {",
						"  }",
						"  public void initialize(IManagedForm form) {",
						"    managedForm = form;",
						"  }",
						"  public void createContents(Composite parent) {",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.ui.forms.IDetailsPage} {this} {}",
				"  {parameter} {parent} {}",
				"    {implicit-layout: absolute} {implicit-layout} {}",
				"  {instance factory container}",
				"    {toolkitAccess: managedForm.getToolkit()} {toolkitAccess} {}");
		// refresh
		page.refresh();
		assertNoErrors(page);
	}

	@Test
	public void test_setLayout_forParent() throws Exception {
		DetailsPageInfo page =
				parseJavaInfo(
						"public abstract class Test implements IDetailsPage {",
						"  private IManagedForm managedForm;",
						"  public Test() {",
						"  }",
						"  public void initialize(IManagedForm form) {",
						"    managedForm = form;",
						"  }",
						"  public void createContents(Composite parent) {",
						"    parent.setLayout(new GridLayout());",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.ui.forms.IDetailsPage} {this} {}",
				"  {parameter} {parent} {/parent.setLayout(new GridLayout())/}",
				"    {new: org.eclipse.swt.layout.GridLayout} {empty} {/parent.setLayout(new GridLayout())/}",
				"  {instance factory container}",
				"    {toolkitAccess: managedForm.getToolkit()} {toolkitAccess} {}");
		// refresh
		page.refresh();
		assertNoErrors(page);
	}

	/**
	 * We should ignore {@link IManagedForm} in {@link IDetailsPage#initialize(IManagedForm)} not only
	 * for direct implementation of {@link IDetailsPage}, but also in subclass of other class which
	 * implements it.
	 * <p>
	 * http://www.eclipse.org/forums/index.php/t/262821/
	 */
	@Test
	public void test_extendAbstractClass() throws Exception {
		setFileContentSrc(
				"test/AbstractPage.java",
				getSourceDQ(
						"package test;",
						"import org.eclipse.ui.forms.IDetailsPage;",
						"public abstract class AbstractPage implements IDetailsPage {",
						"}"));
		waitForAutoBuild();
		// parse
		DetailsPageInfo page =
				parseJavaInfo(
						"public abstract class Test extends AbstractPage {",
						"  private IManagedForm managedForm;",
						"  public Test() {",
						"  }",
						"  public void initialize(IManagedForm form) {",
						"    managedForm = form;",
						"  }",
						"  public void createContents(Composite parent) {",
						"    parent.setLayout(new GridLayout());",
						"  }",
						"}");
		assertHierarchy(
				"{this: test.AbstractPage} {this} {}",
				"  {parameter} {parent} {/parent.setLayout(new GridLayout())/}",
				"    {new: org.eclipse.swt.layout.GridLayout} {empty} {/parent.setLayout(new GridLayout())/}",
				"  {instance factory container}",
				"    {toolkitAccess: managedForm.getToolkit()} {toolkitAccess} {}");
		// refresh
		page.refresh();
		assertNoErrors(page);
	}
}