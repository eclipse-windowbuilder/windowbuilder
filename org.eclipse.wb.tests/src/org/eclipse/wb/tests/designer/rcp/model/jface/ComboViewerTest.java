/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Combo;

import org.junit.Test;

/**
 * Test for {@link ComboViewer}.
 *
 * @author scheglov_ke
 */
public class ComboViewerTest extends RcpModelTest {
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
	public void test_usualConstructor_whenCombo() throws Exception {
		parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      ComboViewer viewer = new ComboViewer(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new ComboViewer(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {viewer: public org.eclipse.swt.widgets.Combo org.eclipse.jface.viewers.ComboViewer.getCombo()} {viewer} {}",
				"    {new: org.eclipse.jface.viewers.ComboViewer} {local-unique: viewer} {/new ComboViewer(this, SWT.NONE)/}");
		refresh();
	}

	/**
	 * Pass {@link Combo} instance into {@link ComboViewer} constructor.
	 */
	@Test
	public void test_constructor_withCombo() throws Exception {
		parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      Combo combo = new Combo(this, SWT.NONE);",
				"      ComboViewer viewer = new ComboViewer(combo);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new Combo(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {new: org.eclipse.swt.widgets.Combo} {local-unique: combo} {/new Combo(this, SWT.NONE)/ /new ComboViewer(combo)/}",
				"    {new: org.eclipse.jface.viewers.ComboViewer} {local-unique: viewer} {/new ComboViewer(combo)/}");
		refresh();
	}

	/**
	 * Pass {@link CCombo} instance into {@link ComboViewer} constructor.
	 */
	@Test
	public void test_constructor_withCCombo() throws Exception {
		parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      CCombo combo = new CCombo(this, SWT.NONE);",
				"      ComboViewer viewer = new ComboViewer(combo);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new CCombo(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {new: org.eclipse.swt.custom.CCombo} {local-unique: combo} {/new CCombo(this, SWT.NONE)/ /new ComboViewer(combo)/}",
				"    {new: org.eclipse.jface.viewers.ComboViewer} {local-unique: viewer} {/new ComboViewer(combo)/}");
		refresh();
	}

	/**
	 * Create {@link ComboViewer} in factory with {@link CCombo} instance, but we don't see this in
	 * code. So, we need some special solution to decide whether use {@link ComboViewer#getCombo()} or
	 * {@link ComboViewer#getCCombo()} method.
	 */
	@Test
	public void test_useFactory_whenCCombo() throws Exception {
		setFileContentSrc(
				"test/Factory.java",
				getTestSource(
						"public class Factory {",
						"  public static ComboViewer createComboViewer(Composite parent) {",
						"    CCombo combo = new CCombo(parent, SWT.NONE);",
						"    return new ComboViewer(combo);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      ComboViewer viewer = Factory.createComboViewer(this);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /Factory.createComboViewer(this)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {viewer: public org.eclipse.swt.custom.CCombo org.eclipse.jface.viewers.ComboViewer.getCCombo()} {viewer} {}",
				"    {static factory: test.Factory createComboViewer(org.eclipse.swt.widgets.Composite)} {local-unique: viewer} {/Factory.createComboViewer(this)/}");
		refresh();
	}
}