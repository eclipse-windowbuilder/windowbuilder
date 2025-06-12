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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.rcp.EditorPartInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ExtensionElementProperty;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.IServiceLocator;

import org.apache.commons.lang3.NotImplementedException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link EditorPartInfo}.
 *
 * @author scheglov_ke
 */
public class EditorPartTest extends RcpModelTest {
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
	/**
	 * Test for many elements of {@link EditorPartInfo}.
	 */
	@Test
	public void test_0() throws Exception {
		EditorPartInfo part =
				parseJavaInfo(
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public abstract class Test extends EditorPart {",
						"  public Test() {",
						"  }",
						"  public void createPartControl(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{this: org.eclipse.ui.part.EditorPart} {this} {}",
				"  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
				"    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
				"    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/}",
				"      {implicit-layout: absolute} {implicit-layout} {}");
		CompositeInfo parentComposite = part.getChildren(CompositeInfo.class).get(0);
		CompositeInfo container = (CompositeInfo) parentComposite.getChildrenControls().get(0);
		// refresh()
		part.refresh();
		assertNoErrors(part);
		// check bounds
		assertEquals(part.getBounds().width, 600);
		assertEquals(part.getBounds().height, 500);
		Assertions.assertThat(parentComposite.getBounds().width).isGreaterThan(300);
		Assertions.assertThat(parentComposite.getBounds().height).isGreaterThan(30);
		Assertions.assertThat(container.getBounds().width).isGreaterThan(300);
		Assertions.assertThat(container.getBounds().height).isGreaterThan(300);
	}

	/**
	 * Test for {@link IEditorInput} implementation.
	 */
	@Test
	public void test_IEditorInput() throws Exception {
		EditorPartInfo part =
				parseJavaInfo(
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public abstract class Test extends EditorPart {",
						"  public Test() {",
						"  }",
						"  public void init(IEditorSite site, IEditorInput input) throws PartInitException {",
						"    setSite(site);",
						"    setInput(input);",
						"  }",
						"  public void createPartControl(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"  }",
						"}");
		part.refresh();
		assertNoErrors(part);
		// IEditorInput
		{
			IEditorPart editorPart = (IEditorPart) part.getObject();
			IEditorInput editorInput = editorPart.getEditorInput();
			assertNotNull(editorInput);
			assertEquals(0, editorInput.hashCode());
		}
	}

	/**
	 * Test for {@link IEditorSite} implementation.
	 */
	@Test
	public void test_IEditorSite() throws Exception {
		EditorPartInfo part =
				parseJavaInfo(
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public abstract class Test extends EditorPart {",
						"  public static final String ID = 'some.editor.Identifier';",
						"  public Test() {",
						"  }",
						"  public void init(IEditorSite site, IEditorInput input) throws PartInitException {",
						"    setSite(site);",
						"    setInput(input);",
						"  }",
						"  public void createPartControl(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"  }",
						"}");
		part.refresh();
		//
		IEditorSite editorSite = (IEditorSite) ReflectionUtils.invokeMethod(part.getObject(), "getEditorSite()");
		try {
			editorSite.getShell();
			fail();
		} catch (NotImplementedException e) {
		}
		assertEquals("IEditorSite_stub", editorSite.toString());
		assertEquals(0, editorSite.hashCode());
		assertEquals("some.editor.Identifier", editorSite.getId());
		{
			Object window = editorSite.getWorkbenchWindow();
			assertSame(DesignerPlugin.getActiveWorkbenchWindow(), window);
		}
		// IServiceLocator
		{
			IServiceLocator serviceLocator = editorSite;
			assertTrue(serviceLocator.hasService(IMenuService.class));
			assertNotNull(serviceLocator.getService(IMenuService.class));
		}
	}

	/**
	 * Test that we can expose {@link Control} from {@link EditorPart}.
	 */
	@Test
	public void test_exposeControl_usingField() throws Exception {
		setFileContentSrc(
				"test/MyEditor.java",
				getTestSource(
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public abstract class MyEditor extends EditorPart {",
						"  protected Composite m_composite;",
						"  public void createPartControl(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"    m_composite = new Composite(container, SWT.NONE);",
						"    m_composite.setLayout(new FillLayout());",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		EditorPartInfo part =
				parseJavaInfo(
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public abstract class Test extends MyEditor {",
						"  public Test() {",
						"  }",
						"  public void createPartControl(Composite parent) {",
						"    super.createPartControl(parent);",
						"    Button button = new Button(m_composite, SWT.NONE);",
						"  }",
						"}");
		assertHierarchy(
				"{this: test.MyEditor} {this} {}",
				"  {parameter} {parent} {/super.createPartControl(parent)/}",
				"    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
				"  {field: org.eclipse.swt.widgets.Composite} {m_composite} {/new Button(m_composite, SWT.NONE)/}",
				"    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
				"    {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(m_composite, SWT.NONE)/}");
		// refresh()
		part.refresh();
		assertNoErrors(part);
	}

	/**
	 * Test that we can expose {@link Control} from {@link EditorPart}.
	 */
	@Test
	public void test_exposeControl_usingMethod() throws Exception {
		setFileContentSrc(
				"test/MyEditor.java",
				getTestSource(
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public abstract class MyEditor extends EditorPart {",
						"  private Composite m_inner;",
						"  public void createPartControl(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"    m_inner = new Composite(container, SWT.NONE);",
						"    m_inner.setLayout(new FillLayout());",
						"  }",
						"  public Composite getInner() {",
						"    return m_inner;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		EditorPartInfo part =
				parseJavaInfo(
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public abstract class Test extends MyEditor {",
						"  public Test() {",
						"  }",
						"  public void createPartControl(Composite parent) {",
						"    super.createPartControl(parent);",
						"    Button button = new Button(getInner(), SWT.NONE);",
						"  }",
						"}");
		assertHierarchy(
				"{this: test.MyEditor} {this} {}",
				"  {parameter} {parent} {/super.createPartControl(parent)/}",
				"    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
				"  {method: public org.eclipse.swt.widgets.Composite test.MyEditor.getInner()} {property} {/new Button(getInner(), SWT.NONE)/}",
				"    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
				"    {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(getInner(), SWT.NONE)/}");
		// refresh()
		part.refresh();
		assertNoErrors(part);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Valid "editor" extension for this {@link EditorPart} class, so we have "Extension" property and
	 * its sub-properties.
	 */
	@Test
	public void test_extensionProperties_hasExtension() throws Exception {
		do_projectDispose();
		do_projectCreate();
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		AbstractPdeTest.createPluginXML(new String[]{
				"<plugin>",
				"  <extension point='org.eclipse.ui.editors'>",
				"    <editor id='id_1' name='name 1' icon='icons/false.gif' class='test.Test' "
						+ "extensions='htm, html' default='true'/>",
						"  </extension>",
		"</plugin>"});
		// parse
		EditorPartInfo part =
				parseJavaInfo(
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public abstract class Test extends EditorPart {",
						"  public Test() {",
						"  }",
						"  public void createPartControl(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"  }",
						"}");
		// "Extension" property
		Property extensionProperty = part.getPropertyByTitle("Extension");
		assertNotNull(extensionProperty);
		assertTrue(extensionProperty.getCategory().isSystem());
		// sub-properties
		Property[] subProperties = getSubProperties(extensionProperty);
		Assertions.assertThat(subProperties).hasSize(4);
		Assertions.assertThat(subProperties).hasOnlyElementsOfType(ExtensionElementProperty.class);
		{
			Property nameProperty = subProperties[0];
			assertEquals("name", nameProperty.getTitle());
			assertTrue(nameProperty.isModified());
			assertEquals("name 1", nameProperty.getValue());
		}
		{
			Property iconProperty = subProperties[1];
			assertEquals("icon", iconProperty.getTitle());
			assertTrue(iconProperty.isModified());
			assertEquals("icons/false.gif", iconProperty.getValue());
		}
		{
			Property extensionsProperty = subProperties[2];
			assertEquals("extensions", extensionsProperty.getTitle());
			assertTrue(extensionsProperty.isModified());
			assertEquals("htm, html", extensionsProperty.getValue());
		}
		{
			Property defaultProperty = subProperties[3];
			assertEquals("default", defaultProperty.getTitle());
			assertTrue(defaultProperty.isModified());
			assertEquals(Boolean.TRUE, defaultProperty.getValue());
		}
	}
}