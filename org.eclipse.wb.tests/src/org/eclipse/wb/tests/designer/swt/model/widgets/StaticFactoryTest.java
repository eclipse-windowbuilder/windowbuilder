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

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.FactoryParentAssociation;
import org.eclipse.wb.core.model.association.WrappedObjectAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.graphics.Image;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for static factory and eSWT.
 *
 * @author scheglov_ke
 */
public class StaticFactoryTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		setFileContentSrc(
				"test/StaticFactory.java",
				getTestSource(
						"public final class StaticFactory {",
						"  /**",
						"  * @wbp.factory.parameter.source text 'SF button'",
						"  * @wbp.factory.parameter.property text setText(java.lang.String)",
						"  */",
						"  public static Button createButton(Composite parent, String text) {",
						"    Button button = new Button(parent, SWT.NONE);",
						"    button.setText(text);",
						"    return button;",
						"  }",
						"  public static TableViewer createTableViewer(Composite parent) {",
						"    return new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);",
						"  }",
						"}"));
		waitForAutoBuild();
	}

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
	// parse
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_parse_Button() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Button button = StaticFactory.createButton(this, 'SF button');",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		// check association
		Association association = button.getAssociation();
		assertInstanceOf(FactoryParentAssociation.class, association);
		assertEquals("StaticFactory.createButton(this, \"SF button\")", association.getSource());
	}

	@Test
	public void test_parse_TableViewer() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    TableViewer tableViewer = StaticFactory.createTableViewer(this);",
						"  }",
						"}");
		// check for Table of TableViewer
		ControlInfo table = shell.getChildrenControls().get(0);
		{
			Association tableAssociation = table.getAssociation();
			assertInstanceOf(WrappedObjectAssociation.class, tableAssociation);
			assertEquals("StaticFactory.createTableViewer(this)", tableAssociation.getSource());
		}
		// check for TableViewer
		{
			ViewerInfo viewer = (ViewerInfo) table.getChildrenJava().get(0);
			Association viewerAssociation = viewer.getAssociation();
			assertInstanceOf(FactoryParentAssociation.class, viewerAssociation);
			assertEquals("StaticFactory.createTableViewer(this)", viewerAssociation.getSource());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ADD
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ADD_TableViewer() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    //",
						"    TableViewer tableViewer = StaticFactory.createTableViewer(this);",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"      composite.setLayout(new RowLayout());",
						"    }",
						"  }",
						"}");
		// prepare table
		ControlInfo table = shell.getChildrenControls().get(0);
		// prepare composite
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(1);
		RowLayoutInfo compositeLayout = (RowLayoutInfo) composite.getLayout();
		// do move
		compositeLayout.command_MOVE(table, null);
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayout(new RowLayout());",
				"      //",
				"      TableViewer tableViewer = StaticFactory.createTableViewer(composite);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_Button() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"  }",
						"}");
		RowLayoutInfo layout = (RowLayoutInfo) shell.getLayout();
		// prepare Button
		ControlInfo button = createNewButton(m_lastEditor);
		// add Button
		layout.command_CREATE(button, null);
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = StaticFactory.createButton(this, 'SF button');",
				"    }",
				"  }",
				"}");
		assertEquals(
				"StaticFactory.createButton(this, \"SF button\")",
				button.getAssociation().getSource());
	}

	/**
	 * @return the "Button" {@link ControlInfo} from static factory.
	 */
	private ControlInfo createNewButton(AstEditor editor) throws Exception {
		FactoryMethodDescription description =
				FactoryDescriptionHelper.getDescription(
						editor,
						m_lastLoader.loadClass("test.StaticFactory"),
						"createButton(org.eclipse.swt.widgets.Composite,java.lang.String)",
						true);
		return (ControlInfo) JavaInfoUtils.createJavaInfo(
				editor,
				m_lastLoader.loadClass("org.eclipse.swt.widgets.Button"),
				new StaticFactoryCreationSupport(description));
	}

	@Test
	public void test_CREATE_TableViewer() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"  }",
						"}");
		RowLayoutInfo layout = (RowLayoutInfo) shell.getLayout();
		// prepare TableViewer and Table
		ViewerInfo viewer;
		ControlInfo table;
		{
			FactoryMethodDescription description =
					FactoryDescriptionHelper.getDescription(
							m_lastEditor,
							m_lastLoader.loadClass("test.StaticFactory"),
							"createTableViewer(org.eclipse.swt.widgets.Composite)",
							true);
			viewer =
					(ViewerInfo) JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							m_lastLoader.loadClass("org.eclipse.jface.viewers.TableViewer"),
							new StaticFactoryCreationSupport(description));
			table = (ControlInfo) JavaInfoUtils.getWrapped(viewer);
		}
		// add TableViewer
		layout.command_CREATE(table, null);
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      TableViewer tableViewer = StaticFactory.createTableViewer(this);",
				"      Table table = tableViewer.getTable();",
				"    }",
				"  }",
				"}");
		assertSame(shell, table.getParent());
		assertSame(table, viewer.getParent());
		// check associations
		assertEquals("StaticFactory.createTableViewer(this)", table.getAssociation().getSource());
		assertSame(table.getAssociation().getStatement(), viewer.getAssociation().getStatement());
	}

	@Test
	public void test_CREATE_liveImage() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		AbsoluteLayoutInfo absoluteLayout = (AbsoluteLayoutInfo) shell.getLayout();
		// prepare Button
		ControlInfo button = createNewButton(m_lastEditor);
		// check that button has "create" image
		Image image = button.getImage();
		assertNotNull(image);
		// check that after asking "live image" we still can add button to real layout
		absoluteLayout.commandCreate(button, null);
		assertEditor(
				"// filler filler filler",
				"class Test extends Shell {",
				"  public Test() {",
				"    {",
				"      Button button = StaticFactory.createButton(this, 'SF button');",
				"    }",
				"  }",
				"}");
	}
}