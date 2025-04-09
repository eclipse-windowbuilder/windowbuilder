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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.rcp.swtawt.palette.SwingCompositeEntryInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import org.junit.Ignore;
import org.junit.Test;

import java.awt.Component;

/**
 * Test for {@link SWT_AWT#new_Frame(Composite)} support and {@link SwingCompositeEntryInfo}.
 *
 * @author scheglov_ke
 */
public class SwtAwtTest extends RcpModelTest {
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
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureToolkits() {
		super.configureToolkits();
		configureDefaults(org.eclipse.wb.internal.swing.ToolkitProvider.DESCRIPTION);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parse
	//
	////////////////////////////////////////////////////////////////////////////
	@Ignore
	@Test
	public void test_parse() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import java.awt.Frame;",
						"import org.eclipse.swt.awt.SWT_AWT;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Composite composite = new Composite(this, SWT.EMBEDDED);",
						"    SWT_AWT.new_Frame(composite);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new Composite(this, SWT.EMBEDDED)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {new: org.eclipse.swt.widgets.Composite} {local-unique: composite} {/new Composite(this, SWT.EMBEDDED)/ /SWT_AWT.new_Frame(composite)/}",
				"    {implicit-layout: absolute} {implicit-layout} {}",
				"    {static factory: org.eclipse.swt.awt.SWT_AWT new_Frame(org.eclipse.swt.widgets.Composite)} {empty} {/SWT_AWT.new_Frame(composite)/}",
				"      {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
		ComponentInfo frame = composite.getChildren(ComponentInfo.class).get(0);
		// refresh
		shell.refresh();
		assertNotNull(frame.getImage());
		// "frame" fills "composite"
		{
			Rectangle frameBounds = frame.getBounds();
			Rectangle compositeBounds = composite.getBounds();
			assertEquals(frameBounds.x, 0);
			assertEquals(frameBounds.y, 0);
			assertEquals(frameBounds.width, compositeBounds.width);
			assertEquals(frameBounds.height, compositeBounds.height);
		}
		// "frame" is visible
		{
			Component frameComponent = frame.getComponent();
			assertTrue(frameComponent.isVisible());
		}
	}

	@Ignore
	@Test
	public void test_parseWithBorder() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import java.awt.Frame;",
						"import org.eclipse.swt.awt.SWT_AWT;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Composite composite = new Composite(this, SWT.BORDER | SWT.EMBEDDED);",
						"    SWT_AWT.new_Frame(composite);",
						"  }",
						"}");
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
		ComponentInfo frame = composite.getChildren(ComponentInfo.class).get(0);
		// refresh
		shell.refresh();
		assertNotNull(frame.getImage());
		{
			Rectangle frameBounds = frame.getBounds();
			assertEquals(frameBounds.x, 2);
			assertEquals(frameBounds.y, 2);
			assertEquals(frameBounds.width, composite.getBounds().width - 2 - 2);
			assertEquals(frameBounds.height, composite.getBounds().height - 2 - 2);
		}
	}

	/**
	 * <p>
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=355351
	 */
	// Test gets stuck during the Linux build...
	@Ignore
	@Test
	public void test_parseWhenZeroSize() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import java.awt.Frame;",
						"import org.eclipse.swt.awt.SWT_AWT;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Composite composite = new Composite(this, SWT.EMBEDDED);",
						"    composite.setLayoutData(new RowData(0,0));",
						"    Frame frame = SWT_AWT.new_Frame(composite);",
						"  }",
						"}");
		ComponentInfo frame = getJavaInfoByName("frame");
		// refresh
		shell.refresh();
		assertNotNull(frame.getImage());
		{
			Rectangle frameBounds = frame.getBounds();
			assertEquals(frameBounds.x, 0);
			assertEquals(frameBounds.y, 0);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Add
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SwingCompositeEntryInfo}.
	 */
	@Test
	public void test_SwingComposite() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import java.awt.Frame;",
						"import org.eclipse.swt.awt.SWT_AWT;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"  }",
						"}");
		shell.refresh();
		// prepare entry
		ToolEntryInfo entry = new SwingCompositeEntryInfo();
		assertTrue(entry.initialize(null, shell));
		assertNotNull(entry.getIcon());
		// activate tool
		CompositeInfo composite;
		{
			CreationTool creationTool = (CreationTool) entry.createTool();
			ICreationFactory creationFactory = creationTool.getFactory();
			creationFactory.activate();
			composite = (CompositeInfo) creationFactory.getNewObject();
		}
		// add Composite
		{
			FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
			fillLayout.command_CREATE(composite, null);
		}
		// verify
		assertEditor(
				"import java.awt.Frame;",
				"import org.eclipse.swt.awt.SWT_AWT;",
				"import java.awt.Panel;",
				"import java.awt.BorderLayout;",
				"import javax.swing.JRootPane;",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      Composite composite = new Composite(this, SWT.EMBEDDED);",
				"      {",
				"        Frame frame = SWT_AWT.new_Frame(composite);",
				"        {",
				"          Panel panel = new Panel();",
				"          frame.add(panel);",
				"          panel.setLayout(new BorderLayout(0, 0));",
				"          {",
				"            JRootPane rootPane = new JRootPane();",
				"            panel.add(rootPane);",
				"          }",
				"        }",
				"      }",
				"    }",
				"  }",
				"}");
	}
}