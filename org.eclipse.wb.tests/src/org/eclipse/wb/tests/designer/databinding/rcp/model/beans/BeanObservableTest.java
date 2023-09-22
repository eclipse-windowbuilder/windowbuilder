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

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyDescriptorBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.CollectionPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.FieldBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.MethodBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectPropertyObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;
import org.eclipse.wb.tests.designer.databinding.rcp.model.AbstractBindingTest;

import org.junit.Test;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class BeanObservableTest extends AbstractBindingTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Direct tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_direct() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"import java.util.ArrayList;",
						"public class Test {",
						"  protected Shell m_shell;",
						"  private IObservableValue m_value;",
						"  private IObservableList m_list;",
						"  private IObservableSet m_set;",
						"  private IObservableMap m_map;",
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
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(BeansObserveTypeContainer.class, provider.getContainers().get(0));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		assertNotNull(observes);
		assertEquals(6, observes.size());
		// ----------------------------------------------
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_value - IObservableValue|m_value|org.eclipse.core.databinding.observable.value.IObservableValue",
				observes.get(1));
		//
		List<IObserveInfo> valueChildren =
				observes.get(1).getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, valueChildren.size());
		BeanBindableTest.assertBindable(
				MethodBeanBindableInfo.class,
				observes.get(1),
				true,
				"m_value.getRealm()|m_value.getRealm()|org.eclipse.core.databinding.observable.Realm",
				valueChildren.get(0));
		//
		List<IObserveInfo> valueProperties =
				observes.get(1).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(7, valueProperties.size());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"Object as IObservableValue||org.eclipse.core.databinding.observable.value.IObservableValue",
				valueProperties.get(0));
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"Detail for IObservableValue||org.eclipse.core.databinding.observable.value.IObservableValue",
				valueProperties.get(1));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"disposed|\"disposed\"|boolean",
				valueProperties.get(2));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"realm|\"realm\"|org.eclipse.core.databinding.observable.Realm",
				valueProperties.get(3));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"stale|\"stale\"|boolean",
				valueProperties.get(4));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"value|\"value\"|java.lang.Object",
				valueProperties.get(5));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"valueType|\"valueType\"|java.lang.Object",
				valueProperties.get(6));
		// ----------------------------------------------
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_list - IObservableList|m_list|org.eclipse.core.databinding.observable.list.IObservableList",
				observes.get(2));
		//
		List<IObserveInfo> listChildren =
				observes.get(2).getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, listChildren.size());
		BeanBindableTest.assertBindable(
				MethodBeanBindableInfo.class,
				observes.get(2),
				true,
				"m_list.getRealm()|m_list.getRealm()|org.eclipse.core.databinding.observable.Realm",
				listChildren.get(0));
		//
		List<IObserveInfo> listProperties =
				observes.get(2).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		if (Runtime.version().feature() >= 21) {
			assertEquals(8, listProperties.size());
		} else {
			assertEquals(6, listProperties.size());
		}
		//
		int index = 0;
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"Object as IObservableList||org.eclipse.core.databinding.observable.list.IObservableList",
				listProperties.get(index++));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"disposed|\"disposed\"|boolean",
				listProperties.get(index++));
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"elementType|\"elementType\"|java.lang.Object",
				listProperties.get(index++));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"empty|\"empty\"|boolean",
				listProperties.get(index++));
		//
		if (Runtime.version().feature() >= 21) {
			BeanBindableTest.assertBindable(
					BeanPropertyDescriptorBindableInfo.class,
					null,
					true,
					"first|\"first\"|java.lang.Object",
					listProperties.get(index++));
			//
			BeanBindableTest.assertBindable(
					BeanPropertyDescriptorBindableInfo.class,
					null,
					true,
					"last|\"last\"|java.lang.Object",
					listProperties.get(index++));
		}
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"realm|\"realm\"|org.eclipse.core.databinding.observable.Realm",
				listProperties.get(index++));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"stale|\"stale\"|boolean",
				listProperties.get(index++));
		// ----------------------------------------------
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_set - IObservableSet|m_set|org.eclipse.core.databinding.observable.set.IObservableSet",
				observes.get(3));
		//
		List<IObserveInfo> setChildren =
				observes.get(3).getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, setChildren.size());
		BeanBindableTest.assertBindable(
				MethodBeanBindableInfo.class,
				observes.get(3),
				true,
				"m_set.getRealm()|m_set.getRealm()|org.eclipse.core.databinding.observable.Realm",
				setChildren.get(0));
		//
		List<IObserveInfo> setProperties =
				observes.get(3).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(6, setProperties.size());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"Object as IObservableSet||org.eclipse.core.databinding.observable.set.IObservableSet",
				setProperties.get(0));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"disposed|\"disposed\"|boolean",
				setProperties.get(1));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"elementType|\"elementType\"|java.lang.Object",
				setProperties.get(2));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"empty|\"empty\"|boolean",
				setProperties.get(3));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"realm|\"realm\"|org.eclipse.core.databinding.observable.Realm",
				setProperties.get(4));
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"stale|\"stale\"|boolean",
				setProperties.get(5));
		// ----------------------------------------------
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				false,
				"m_map - IObservableMap|m_map|org.eclipse.core.databinding.observable.map.IObservableMap",
				observes.get(4));
		//
		List<IObserveInfo> mapChildren =
				observes.get(4).getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(0, mapChildren.size());
		//
		List<IObserveInfo> mapProperties =
				observes.get(4).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(0, mapProperties.size());
	}

	@Test
	public void test_directObservable_1() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private WritableValue m_value;",
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
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(BeansObserveTypeContainer.class, provider.getContainers().get(0));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		assertNotNull(observes);
		assertEquals(3, observes.size());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_shell - Shell|m_shell|org.eclipse.swt.widgets.Shell",
				observes.get(0));
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_value - WritableValue|m_value|org.eclipse.core.databinding.observable.value.WritableValue",
				observes.get(1));
		BeanBindableTest.assertBindable(
				MethodBeanBindableInfo.class,
				"getClass()|getClass()|java.lang.Class",
				observes.get(2));
		//
		List<IObserveInfo> properties =
				observes.get(1).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(8, properties.size());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"Object as IObservableValue||org.eclipse.core.databinding.observable.value.WritableValue",
				properties.get(0));
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"Detail for IObservableValue||org.eclipse.core.databinding.observable.value.WritableValue",
				properties.get(1));
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"class|\"class\"|java.lang.Class",
				properties.get(2));
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"disposed|\"disposed\"|boolean",
				properties.get(3));
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"realm|\"realm\"|org.eclipse.core.databinding.observable.Realm",
				properties.get(4));
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"stale|\"stale\"|boolean",
				properties.get(5));
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"value|\"value\"|java.lang.Object",
				properties.get(6));
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"valueType|\"valueType\"|java.lang.Object",
				properties.get(7));
	}

	@Test
	public void test_directObservable_2() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private DataBindingContext m_bindingContext;",
						"  private WritableValue m_value;",
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
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  private DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    IObservableValue observeWidget = WidgetProperties.text().observe(m_shell);",
						"    bindingContext.bindValue(observeWidget, m_value, null, null);",
						"    return bindingContext;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		assertInstanceOf(DirectObservableInfo.class, binding.getModelObservable());
		assertEquals("m_value", binding.getModelObservable().getVariableIdentifier());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_value - WritableValue|m_value|org.eclipse.core.databinding.observable.value.WritableValue",
				binding.getModelObservable().getBindableObject());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"Object as IObservableValue||org.eclipse.core.databinding.observable.value.WritableValue",
				binding.getModelObservable().getBindableProperty());
	}

	@Test
	public void test_directObservable_detail() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private DataBindingContext m_bindingContext;",
								"  private WritableValue m_value;",
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
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    IObservableValue observeWidget = WidgetProperties.text().observe(m_shell);",
								"    IObservableValue observeDetail = BeanProperties.value(\"name\", String.class).observeDetail(m_value);",
								"    bindingContext.bindValue(observeWidget, observeDetail, null, null);",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		assertInstanceOf(DetailValueBeanObservableInfo.class, binding.getModelObservable());
		DetailValueBeanObservableInfo modelObservable =
				(DetailValueBeanObservableInfo) binding.getModelObservable();
		//
		assertNotNull(modelObservable.getMasterObservable());
		assertInstanceOf(DirectObservableInfo.class, modelObservable.getMasterObservable());
		//
		assertSame(
				modelObservable.getMasterObservable().getBindableObject(),
				modelObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_value - WritableValue|m_value|org.eclipse.core.databinding.observable.value.WritableValue",
				modelObservable.getBindableObject());
		//
		assertSame(
				modelObservable.getMasterObservable().getBindableProperty(),
				modelObservable.getBindableProperty());
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"Detail for IObservableValue||org.eclipse.core.databinding.observable.value.WritableValue",
				modelObservable.getBindableProperty());
		//
		assertEquals("\"name\"", modelObservable.getDetailPropertyReference());
		//
		assertNotNull(modelObservable.getDetailPropertyType());
		assertEquals("java.lang.String", modelObservable.getDetailPropertyType().getName());
	}

	@Test
	public void test_directObservableProperties_1() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private Binding m_binding;",
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
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(BeansObserveTypeContainer.class, provider.getContainers().get(0));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		assertNotNull(observes);
		assertEquals(3, observes.size());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_shell - Shell|m_shell|org.eclipse.swt.widgets.Shell",
				observes.get(0));
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_binding - Binding|m_binding|org.eclipse.core.databinding.Binding",
				observes.get(1));
		BeanBindableTest.assertBindable(
				MethodBeanBindableInfo.class,
				"getClass()|getClass()|java.lang.Class",
				observes.get(2));
		//
		List<BindableInfo> properties =
				CoreUtils.cast(observes.get(1).getChildren(ChildrenContext.ChildrenForPropertiesTable));
		assertEquals(7, properties.size());
		//
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"class|\"class\"|java.lang.Class",
				properties.get(0));
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"disposed|\"disposed\"|boolean",
				properties.get(1));
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"getModel()|getModel()|org.eclipse.core.databinding.observable.IObservable",
				properties.get(2));
		assertNull(properties.get(2).getObservableFactory());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"getModels()|getModels()|org.eclipse.core.databinding.observable.list.IObservableList",
				properties.get(3));
		assertNotNull(properties.get(3).getObservableFactory());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"getTarget()|getTarget()|org.eclipse.core.databinding.observable.IObservable",
				properties.get(4));
		assertNull(properties.get(4).getObservableFactory());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"getTargets()|getTargets()|org.eclipse.core.databinding.observable.list.IObservableList",
				properties.get(5));
		assertNotNull(properties.get(5).getObservableFactory());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"getValidationStatus()|getValidationStatus()|org.eclipse.core.databinding.observable.value.IObservableValue",
				properties.get(6));
		assertNotNull(properties.get(6).getObservableFactory());
	}

	@Test
	public void test_directObservableProperties_2() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private DataBindingContext m_bindingContext;",
								"  private Binding m_binding0;",
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
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    IObservableValue observeWidget = WidgetProperties.text().observe(m_shell);",
								"    bindingContext.bindValue(observeWidget, m_binding0.getValidationStatus(), null, null);",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		assertInstanceOf(DirectPropertyObservableInfo.class, binding.getModelObservable());
		assertEquals(
				"m_binding0.getValidationStatus()",
				binding.getModelObservable().getVariableIdentifier());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				"m_binding0 - Binding|m_binding0|org.eclipse.core.databinding.Binding",
				binding.getModelObservable().getBindableObject());
		//
		BeanBindableTest.assertBindable(
				DirectPropertyBindableInfo.class,
				null,
				false,
				"getValidationStatus()|getValidationStatus()|org.eclipse.core.databinding.observable.value.IObservableValue",
				binding.getModelObservable().getBindableProperty());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_observeValue_1() throws Exception {
		observeValue(
				"    IObservableValue observeValue0 = BeanProperties.value(\"name\").observe(m_bean0);",
				"    IObservableValue observeValue1 = BeanProperties.value(\"value\").observe(getBean1());");
	}

	@Test
	public void test_observeValue_2() throws Exception {
		observeValue(
				"    IObservableValue observeValue0 = BeanProperties.value(\"name\").observe(Realm.getDefault(), m_bean0);",
				"    IObservableValue observeValue1 = BeanProperties.value(\"value\").observe(Realm.getDefault(), getBean1());");
	}

	private void observeValue(String line0, String line1) throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public int getValue() {",
						"    return 0;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private DataBindingContext m_bindingContext;",
						"  private TestBean m_bean0;",
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
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  private DataBindingContext initDataBindings() {",
						line0,
						line1,
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    bindingContext.bindValue(observeValue0, observeValue1, null, null);",
						"    return bindingContext;",
						"  }",
						"  public TestBean getBean1() {",
						"    return null;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		//
		assertInstanceOf(ValueBeanObservableInfo.class, binding.getTargetObservable());
		ValueBeanObservableInfo targetObservable =
				(ValueBeanObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeValue0", targetObservable.getVariableIdentifier());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_bean0 - TestBean|m_bean0|test.TestBean",
				targetObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"name|\"name\"|java.lang.String",
				targetObservable.getBindableProperty());
		//
		assertInstanceOf(ValueBeanObservableInfo.class, binding.getModelObservable());
		ValueBeanObservableInfo modelObservable =
				(ValueBeanObservableInfo) binding.getModelObservable();
		//
		assertEquals("observeValue1", modelObservable.getVariableIdentifier());
		//
		BeanBindableTest.assertBindable(
				MethodBeanBindableInfo.class,
				null,
				true,
				"getBean1()|getBean1()|test.TestBean",
				modelObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				false,
				"value|\"value\"|int",
				modelObservable.getBindableProperty());
	}

	@Test
	public void test_observeList_1() throws Exception {
		observeList(
				"    IObservableList observeList0 = BeanProperties.list(\"names\").observe(Realm.getDefault(), m_bean0);",
				"    IObservableList observeList1 = BeanProperties.list(\"names\").observe(Realm.getDefault(), getBean1());");
	}

	@Test
	public void test_observeList_2() throws Exception {
		observeList(
				"    IObservableList observeList0 = BeanProperties.list(\"names\", java.lang.String.class).observe(Realm.getDefault(),m_bean0);",
				"    IObservableList observeList1 = BeanProperties.list(\"names\", String.class).observe(Realm.getDefault(), getBean1());");
	}

	private void observeList(String line0, String line1) throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public java.util.List getNames() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private DataBindingContext m_bindingContext;",
						"  private TestBean m_bean0;",
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
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  private DataBindingContext initDataBindings() {",
						line0,
						line1,
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    bindingContext.bindList(observeList0, observeList1, null, null);",
						"    return bindingContext;",
						"  }",
						"  public TestBean getBean1() {",
						"    return null;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		//
		assertInstanceOf(ListBeanObservableInfo.class, binding.getTargetObservable());
		ListBeanObservableInfo targetObservable =
				(ListBeanObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeList0", targetObservable.getVariableIdentifier());
		assertEquals("m_bean0.names(List)", targetObservable.getPresentationText());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_bean0 - TestBean|m_bean0|test.TestBean",
				targetObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"names|\"names\"|java.util.List",
				targetObservable.getBindableProperty());
		//
		assertInstanceOf(ListBeanObservableInfo.class, binding.getModelObservable());
		ListBeanObservableInfo modelObservable = (ListBeanObservableInfo) binding.getModelObservable();
		//
		assertEquals("observeList1", modelObservable.getVariableIdentifier());
		assertEquals("getBean1().names(List)", modelObservable.getPresentationText());
		//
		BeanBindableTest.assertBindable(
				MethodBeanBindableInfo.class,
				null,
				true,
				"getBean1()|getBean1()|test.TestBean",
				modelObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"names|\"names\"|java.util.List",
				modelObservable.getBindableProperty());
	}

	@Test
	public void test_observeSet_1() throws Exception {
		observeSet(
				"    IObservableSet observeSet0 = BeanProperties.set(\"names\").observe(Realm.getDefault(), m_bean0);",
				"    IObservableSet observeSet1 = BeanProperties.set(\"names\").observe(Realm.getDefault(), getBean1());");
	}

	@Test
	public void test_observeSet_2() throws Exception {
		observeSet(
				"    IObservableSet observeSet0 = BeanProperties.set(\"names\", java.lang.String.class).observe(Realm.getDefault(), m_bean0);",
				"    IObservableSet observeSet1 = BeanProperties.set(\"names\", String.class).observe(Realm.getDefault(), getBean1());");
	}

	private void observeSet(String line0, String line1) throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public java.util.Set getNames() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private DataBindingContext m_bindingContext;",
						"  private TestBean m_bean0;",
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
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  private DataBindingContext initDataBindings() {",
						line0,
						line1,
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    bindingContext.bindSet(observeSet0, observeSet1, null, null);",
						"    return bindingContext;",
						"  }",
						"  public TestBean getBean1() {",
						"    return null;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		//
		assertInstanceOf(SetBeanObservableInfo.class, binding.getTargetObservable());
		SetBeanObservableInfo targetObservable = (SetBeanObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeSet0", targetObservable.getVariableIdentifier());
		assertEquals("m_bean0.names(Set)", targetObservable.getPresentationText());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_bean0 - TestBean|m_bean0|test.TestBean",
				targetObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"names|\"names\"|java.util.Set",
				targetObservable.getBindableProperty());
		//
		assertInstanceOf(SetBeanObservableInfo.class, binding.getModelObservable());
		SetBeanObservableInfo modelObservable = (SetBeanObservableInfo) binding.getModelObservable();
		//
		assertEquals("observeSet1", modelObservable.getVariableIdentifier());
		assertEquals("getBean1().names(Set)", modelObservable.getPresentationText());
		//
		BeanBindableTest.assertBindable(
				MethodBeanBindableInfo.class,
				null,
				true,
				"getBean1()|getBean1()|test.TestBean",
				modelObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"names|\"names\"|java.util.Set",
				modelObservable.getBindableProperty());
	}

	@Test
	public void test_observeDetailValue_1() throws Exception {
		observeDetailValue("    IObservableValue observeDetailsValue = BeanProperties.value(\"empty\", boolean.class).observeDetail(observeValue0);");
	}

	@Test
	public void test_observeDetailValue_2() throws Exception {
		observeDetailValue("    IObservableValue observeDetailsValue = BeanProperties.value(String.class, \"empty\", boolean.class).observeDetail(observeValue0);");
	}

	private void observeDetailValue(String line) throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public int getValue() {",
						"    return 0;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private DataBindingContext m_bindingContext;",
								"  private TestBean m_bean0;",
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
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableValue observeValue0 = BeanProperties.value(\"name\").observe(m_bean0);",
								line,
								"    IObservableValue observeValue1 = BeanProperties.value(\"value\").observe(getBean1());",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindValue(observeDetailsValue, observeValue1, null, null);",
								"    return bindingContext;",
								"  }",
								"  public TestBean getBean1() {",
								"    return null;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		//
		assertInstanceOf(DetailValueBeanObservableInfo.class, binding.getTargetObservable());
		DetailValueBeanObservableInfo targetObservable =
				(DetailValueBeanObservableInfo) binding.getTargetObservable();
		//
		assertNotNull(targetObservable.getMasterObservable());
		assertInstanceOf(ValueBeanObservableInfo.class, targetObservable.getMasterObservable());
		//
		assertSame(
				targetObservable.getMasterObservable().getBindableObject(),
				targetObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_bean0 - TestBean|m_bean0|test.TestBean",
				targetObservable.getBindableObject());
		//
		assertSame(
				targetObservable.getMasterObservable().getBindableProperty(),
				targetObservable.getBindableProperty());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"name|\"name\"|java.lang.String",
				targetObservable.getBindableProperty());
		//
		assertEquals("\"empty\"", targetObservable.getDetailPropertyReference());
		//
		assertNotNull(targetObservable.getDetailPropertyType());
		assertEquals("boolean", targetObservable.getDetailPropertyType().getName());
		//
		assertEquals(
				"m_bean0.name.detailValue(\"empty\", boolean.class)",
				targetObservable.getPresentationText());
	}

	@Test
	public void test_observeDetailList() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.List getValues() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private DataBindingContext m_bindingContext;",
								"  private TestBean m_bean0;",
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
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableValue observeValue0 = BeanProperties.value(\"name\").observe(m_bean0);",
								"    IObservableList observeDetailsList = BeanProperties.list(\"empty\", boolean.class).observeDetail(observeValue0);",
								"    IObservableList observeList1 = BeanProperties.list(\"values\").observe(getBean1());",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindList(observeDetailsList, observeList1, null, null);",
								"    return bindingContext;",
								"  }",
								"  public TestBean getBean1() {",
								"    return null;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		//
		assertInstanceOf(DetailListBeanObservableInfo.class, binding.getTargetObservable());
		DetailBeanObservableInfo targetObservable =
				(DetailBeanObservableInfo) binding.getTargetObservable();
		//
		assertNotNull(targetObservable.getMasterObservable());
		assertInstanceOf(ValueBeanObservableInfo.class, targetObservable.getMasterObservable());
		//
		assertSame(
				targetObservable.getMasterObservable().getBindableObject(),
				targetObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_bean0 - TestBean|m_bean0|test.TestBean",
				targetObservable.getBindableObject());
		//
		assertSame(
				targetObservable.getMasterObservable().getBindableProperty(),
				targetObservable.getBindableProperty());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"name|\"name\"|java.lang.String",
				targetObservable.getBindableProperty());
		//
		assertEquals("\"empty\"", targetObservable.getDetailPropertyReference());
		//
		assertNotNull(targetObservable.getDetailPropertyType());
		assertEquals("boolean", targetObservable.getDetailPropertyType().getName());
		//
		assertEquals(
				"m_bean0.name.detailList(\"empty\", boolean.class)",
				targetObservable.getPresentationText());
		//
		targetObservable.setDetailPropertyReference(Object.class, "\"foo\"");
		targetObservable.setDetailPropertyType(String.class);
		//
		assertEquals(
				"m_bean0.name.detailList(\"foo\", String.class)",
				targetObservable.getPresentationText());
		//
		targetObservable.setDetailPropertyReference(Object.class, null);
		targetObservable.setDetailPropertyType(null);
		//
		assertEquals(
				"m_bean0.name.detailList(?????, ?????.class)",
				targetObservable.getPresentationText());
	}

	@Test
	public void test_observeDetailSet() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.Set getValues() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private DataBindingContext m_bindingContext;",
								"  private TestBean m_bean0;",
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
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableValue observeValue0 = BeanProperties.value(\"name\").observe(m_bean0);",
								"    IObservableSet observeDetailsSet = BeanProperties.set(\"empty\", boolean.class).observeDetail(observeValue0);",
								"    IObservableSet observeSet1 = BeanProperties.set(\"values\").observe(getBean1());",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindSet(observeDetailsSet, observeSet1, null, null);",
								"    return bindingContext;",
								"  }",
								"  public TestBean getBean1() {",
								"    return null;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		//
		assertInstanceOf(DetailSetBeanObservableInfo.class, binding.getTargetObservable());
		DetailBeanObservableInfo targetObservable =
				(DetailBeanObservableInfo) binding.getTargetObservable();
		//
		assertNotNull(targetObservable.getMasterObservable());
		assertInstanceOf(ValueBeanObservableInfo.class, targetObservable.getMasterObservable());
		//
		assertSame(
				targetObservable.getMasterObservable().getBindableObject(),
				targetObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_bean0 - TestBean|m_bean0|test.TestBean",
				targetObservable.getBindableObject());
		//
		assertSame(
				targetObservable.getMasterObservable().getBindableProperty(),
				targetObservable.getBindableProperty());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"name|\"name\"|java.lang.String",
				targetObservable.getBindableProperty());
		//
		assertEquals("\"empty\"", targetObservable.getDetailPropertyReference());
		//
		assertNotNull(targetObservable.getDetailPropertyType());
		assertEquals("boolean", targetObservable.getDetailPropertyType().getName());
		//
		assertEquals(
				"m_bean0.name.detailSet(\"empty\", boolean.class)",
				targetObservable.getPresentationText());
	}

	@Test
	public void test_WritableList_1() throws Exception {
		test_WritableList("    WritableList writableList = new WritableList(m_list, String.class);");
	}

	@Test
	public void test_WritableList_2() throws Exception {
		test_WritableList("    WritableList writableList = new WritableList(Realm.getDefault(), m_list, String.class);");
	}

	private void test_WritableList(String line) throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public java.util.List getNames() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private DataBindingContext m_bindingContext;",
								"  private TestBean m_bean0;",
								"  private java.util.ArrayList m_list;",
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
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableList observeList = BeanProperties.list(\"names\", java.lang.String.class).observe(Realm.getDefault(), m_bean0);",
								line,
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindList(observeList, writableList, null, null);",
								"    return bindingContext;",
								"  }",
								"  public TestBean getBean1() {",
								"    return null;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		//
		assertInstanceOf(ListBeanObservableInfo.class, binding.getTargetObservable());
		ListBeanObservableInfo targetObservable =
				(ListBeanObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeList", targetObservable.getVariableIdentifier());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_bean0 - TestBean|m_bean0|test.TestBean",
				targetObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"names|\"names\"|java.util.List",
				targetObservable.getBindableProperty());
		//
		assertInstanceOf(WritableListBeanObservableInfo.class, binding.getModelObservable());
		WritableListBeanObservableInfo modelObservable =
				(WritableListBeanObservableInfo) binding.getModelObservable();
		//
		assertEquals("writableList", modelObservable.getVariableIdentifier());
		assertEquals("WritableList(m_list, String.class)", modelObservable.getPresentationText());
		assertNotNull(modelObservable.getElementType());
		assertSame(String.class, modelObservable.getElementType());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_list - ArrayList|m_list|java.util.ArrayList",
				modelObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				CollectionPropertyBindableInfo.class,
				null,
				false,
				"Collection as WritableList/Properties.selfList()|m_list|java.util.ArrayList",
				modelObservable.getBindableProperty());
		//
		modelObservable.setElementType(null);
		assertEquals("WritableList(m_list, ?????.class)", modelObservable.getPresentationText());
	}

	@Test
	public void test_WritableSet_1() throws Exception {
		test_WritableSet("    WritableSet writableSet = new WritableSet(m_set, String.class);");
	}

	@Test
	public void test_WritableSet_2() throws Exception {
		test_WritableSet("    WritableSet writableSet = new WritableSet(Realm.getDefault(), m_set, String.class);");
	}

	private void test_WritableSet(String line) throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public java.util.Set getNames() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private DataBindingContext m_bindingContext;",
								"  private TestBean m_bean0;",
								"  private java.util.HashSet m_set;",
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
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableSet observeSet = BeanProperties.set(\"names\", java.lang.String.class).observe(m_bean0);",
								line,
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindSet(observeSet, writableSet, null, null);",
								"    return bindingContext;",
								"  }",
								"  public TestBean getBean1() {",
								"    return null;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IBindingInfo> bindings = provider.getBindings();
		//
		assertNotNull(bindings);
		assertEquals(1, bindings.size());
		//
		BindingInfo binding = (BindingInfo) bindings.get(0);
		//
		assertInstanceOf(SetBeanObservableInfo.class, binding.getTargetObservable());
		SetBeanObservableInfo targetObservable = (SetBeanObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeSet", targetObservable.getVariableIdentifier());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_bean0 - TestBean|m_bean0|test.TestBean",
				targetObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				BeanPropertyDescriptorBindableInfo.class,
				null,
				true,
				"names|\"names\"|java.util.Set",
				targetObservable.getBindableProperty());
		//
		assertInstanceOf(WritableSetBeanObservableInfo.class, binding.getModelObservable());
		WritableSetBeanObservableInfo modelObservable =
				(WritableSetBeanObservableInfo) binding.getModelObservable();
		//
		assertEquals("writableSet", modelObservable.getVariableIdentifier());
		assertEquals("WritableSet(m_set, String.class)", modelObservable.getPresentationText());
		assertNotNull(modelObservable.getElementType());
		assertSame(String.class, modelObservable.getElementType());
		//
		BeanBindableTest.assertBindable(
				FieldBeanBindableInfo.class,
				null,
				true,
				"m_set - HashSet|m_set|java.util.HashSet",
				modelObservable.getBindableObject());
		BeanBindableTest.assertBindable(
				CollectionPropertyBindableInfo.class,
				null,
				false,
				"Collection as WritableSet/Properties.selfSet()|m_set|java.util.HashSet",
				modelObservable.getBindableProperty());
		//
		modelObservable.setElementType(null);
		assertEquals("WritableSet(m_set, ?????.class)", modelObservable.getPresentationText());
	}
}