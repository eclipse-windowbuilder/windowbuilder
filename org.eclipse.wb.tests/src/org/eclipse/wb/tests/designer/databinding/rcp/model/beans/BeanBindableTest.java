/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.databinding.rcp.model.beans;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyDescriptorBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.CollectionPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.FieldBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.MethodBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.ViewerObservablePropertyBindableInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.databinding.rcp.model.AbstractBindingTest;

import org.junit.Test;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class BeanBindableTest extends AbstractBindingTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_fields_methods() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import java.io.File;",
						"public class Test {",
						"  protected Shell m_shell;",
						"  private String m_name;",
						"  private File m_file;",
						"  public static void main(String[] args) {",
						"    Test test = new Test();",
						"    test.open();",
						"  }",
						"  public void open() {",
						"    Display display = new Display();",
						"    createContents();",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
						"  protected void createContents() {",
						"    m_shell = new Shell();",
						"  }",
						"}");
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(BeansObserveTypeContainer.class, provider.getContainers().get(0));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		assertNotNull(observes);
		assertEquals(4, observes.size());
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_shell - Shell|m_shell|org.eclipse.swt.widgets.Shell",
				observes.get(0));
		assertSame(shell.getPresentation().getIcon(), observes.get(0).getPresentation().getImageDescriptor());
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_name - String|m_name|java.lang.String",
				observes.get(1));
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_file - File|m_file|java.io.File",
				observes.get(2));
		//
		assertBindable(
				MethodBeanBindableInfo.class,
				"getClass()|getClass()|java.lang.Class",
				observes.get(3));
	}

	@Test
	public void test_children_properties() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import java.util.ArrayList;",
						"public class Test {",
						"  protected Shell m_shell;",
						"  private String m_name;",
						"  private ArrayList m_list;",
						"  public static void main(String[] args) {",
						"    Test test = new Test();",
						"    test.open();",
						"  }",
						"  public void open() {",
						"    Display display = new Display();",
						"    createContents();",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
						"  protected void createContents() {",
						"    m_shell = new Shell();",
						"  }",
						"}");
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(BeansObserveTypeContainer.class, provider.getContainers().get(0));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		assertNotNull(observes);
		assertEquals(4, observes.size());
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_name - String|m_name|java.lang.String",
				observes.get(1));
		//
		List<IObserveInfo> nameChildren =
				observes.get(1).getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, nameChildren.size());
		assertBindable(
				MethodBeanBindableInfo.class,
				observes.get(1),
				true,
				"m_name.getClass()|m_name.getClass()|java.lang.Class",
				nameChildren.get(0));
		//
		List<IObserveInfo> nameProperties =
				observes.get(1).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(4, nameProperties.size());
		//
		assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"blank|\"blank\"|boolean",
				nameProperties.get(0));
		//
		assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"bytes|\"bytes\"|[B",
				nameProperties.get(1));
		//
		List<IObserveInfo> bytesProperties =
				nameProperties.get(1).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(1, bytesProperties.size());
		//
		assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				nameProperties.get(1),
				true,
				"class|\"bytes.class\"|java.lang.Class",
				bytesProperties.get(0));
		//
		FieldBeanBindableInfo nameField = (FieldBeanBindableInfo) observes.get(1);
		assertNull(nameField.resolvePropertyReference("bytez"));
		assertSame(nameProperties.get(1), nameField.resolvePropertyReference("\"bytes\""));
		assertSame(bytesProperties.get(0), nameField.resolvePropertyReference("\"bytes.class\""));
		//
		assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"class|\"class\"|java.lang.Class",
				nameProperties.get(2));
		//
		assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"empty|\"empty\"|boolean",
				nameProperties.get(3));
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_list - ArrayList|m_list|java.util.ArrayList",
				observes.get(2));
		List<IObserveInfo> listChildren =
				observes.get(2).getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, listChildren.size());
		assertBindable(
				MethodBeanBindableInfo.class,
				observes.get(2),
				true,
				"m_list.getClass()|m_list.getClass()|java.lang.Class",
				listChildren.get(0));
		//
		List<IObserveInfo> listProperties =
				observes.get(2).getChildren(ChildrenContext.ChildrenForPropertiesTable);

		if (Runtime.version().feature() >= 21) {
			assertEquals(5, listProperties.size());
		} else {
			assertEquals(3, listProperties.size());
		}
		//
		assertBindable(
				CollectionPropertyBindableInfo.class,
				null,
				false,
				"Collection as WritableList/Properties.selfList()|m_list|java.util.ArrayList",
				listProperties.get(0));
		//
		assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"class|\"class\"|java.lang.Class",
				listProperties.get(1));
		//
		assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"empty|\"empty\"|boolean",
				listProperties.get(2));
		//
		if (Runtime.version().feature() >= 21) {
			assertBindable(
					BeanPropertyDescriptorBindableInfo.class,
					null,
					true,
					"first|\"first\"|java.lang.Object",
					listProperties.get(3));
			//
			assertBindable(
					BeanPropertyDescriptorBindableInfo.class,
					null,
					true,
					"last|\"last\"|java.lang.Object",
					listProperties.get(4));
		}
	}

	@Test
	public void test_superClass_thisMethods() throws Exception {
		setFileContentSrc(
				"test/TestSuper.java",
				getSourceDQ(
						"package test;",
						"public class TestSuper {",
						"  public String getBean() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		CompositeInfo shell =
				parseComposite(
						"import java.io.File;",
						"public class Test extends TestSuper {",
						"  protected Shell m_shell;",
						"  public static void main(String[] args) {",
						"    Test test = new Test();",
						"    test.open();",
						"  }",
						"  public void open() {",
						"    Display display = new Display();",
						"    createContents();",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
						"  protected void createContents() {",
						"    m_shell = new Shell();",
						"  }",
						"  public String getName() {",
						"    return null;",
						"  }",
						"}");
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(BeansObserveTypeContainer.class, provider.getContainers().get(0));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		assertNotNull(observes);
		assertEquals(4, observes.size());
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_shell - Shell|m_shell|org.eclipse.swt.widgets.Shell",
				observes.get(0));
		assertBindable(
				MethodBeanBindableInfo.class,
				"getBean()|getBean()|java.lang.String",
				observes.get(1));
		assertBindable(
				MethodBeanBindableInfo.class,
				"getClass()|getClass()|java.lang.Class",
				observes.get(2));
		assertBindable(
				MethodBeanBindableInfo.class,
				"getName()|getName()|java.lang.String",
				observes.get(3));
	}

	@Test
	public void test_viewer() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test {",
						"  protected Shell m_shell;",
						"  private ISelectionProvider m_selection;",
						"  private ICheckable m_checkable;",
						"  public static void main(String[] args) {",
						"    Test test = new Test();",
						"    test.open();",
						"  }",
						"  public void open() {",
						"    Display display = new Display();",
						"    createContents();",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
						"  protected void createContents() {",
						"    m_shell = new Shell();",
						"  }",
						"}");
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(BeansObserveTypeContainer.class, provider.getContainers().get(0));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		assertNotNull(observes);
		assertEquals(4, observes.size());
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_shell - Shell|m_shell|org.eclipse.swt.widgets.Shell",
				observes.get(0));
		assertSame(shell.getPresentation().getIcon(), observes.get(0).getPresentation().getImageDescriptor());
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_selection - ISelectionProvider|m_selection|org.eclipse.jface.viewers.ISelectionProvider",
				observes.get(1));
		//
		assertBindable(
				FieldBeanBindableInfo.class,
				"m_checkable - ICheckable|m_checkable|org.eclipse.jface.viewers.ICheckable",
				observes.get(2));
		//
		assertBindable(
				MethodBeanBindableInfo.class,
				"getClass()|getClass()|java.lang.Class",
				observes.get(3));
		//
		//
		List<IObserveInfo> selectionProperties =
				observes.get(1).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(4, selectionProperties.size());
		//
		assertBindable(
				ViewerObservablePropertyBindableInfo.class,
				null,
				false,
				"single selection|observeSingleSelection|java.lang.Object",
				selectionProperties.get(0));
		//
		assertBindable(
				ViewerObservablePropertyBindableInfo.class,
				null,
				false,
				"part of selection|observeSingleSelection|java.lang.Object",
				selectionProperties.get(1));
		//
		assertBindable(
				ViewerObservablePropertyBindableInfo.class,
				null,
				false,
				"multi selection|observeMultiSelection|java.lang.Object",
				selectionProperties.get(2));
		//
		assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"selection|\"selection\"|org.eclipse.jface.viewers.ISelection",
				selectionProperties.get(3));
		//
		List<IObserveInfo> checkableProperties =
				observes.get(2).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(1, checkableProperties.size());
		//
		assertBindable(
				ViewerObservablePropertyBindableInfo.class,
				null,
				false,
				"checked elements|observeCheckedElements|java.lang.Object",
				checkableProperties.get(0));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static void assertBindable(Class<?> testClass, String testString, Object object)
			throws Exception {
		assertBindable(testClass, null, true, testString, object);
	}

	public static void assertBindable(Class<?> testClass,
			Object testParent,
			boolean propertiesState,
			String testString,
			Object object) throws Exception {
		assertInstanceOf(testClass, object);
		BindableInfo bindable = (BindableInfo) object;
		assertSame(ObserveType.BEANS, bindable.getType());
		assertNotNull(bindable.getObjectType());
		assertSame(testParent, bindable.getParent());
		assertEquals(testString, bindable.getPresentation().getText()
				+ "|"
				+ bindable.getReference()
				+ "|"
				+ bindable.getObjectType().getName());
		assertNotNull(bindable.getPresentation());
		//
		assertNotNull(bindable.getChildren(ChildrenContext.ChildrenForMasterTable));
		//
		assertNotNull(bindable.getChildren(ChildrenContext.ChildrenForPropertiesTable));
		assertEquals(
				propertiesState,
				!bindable.getChildren(ChildrenContext.ChildrenForPropertiesTable).isEmpty());
	}
}