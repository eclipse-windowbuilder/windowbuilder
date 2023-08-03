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
package org.eclipse.wb.tests.designer.databinding.rcp.model.widgets;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.IMasterDetailProvider;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableListContentProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableListTreeContentProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableMapLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableSetContentProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableSetTreeContentProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.TreeStructureAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.TreeViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansListObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansSetObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.MultiSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ViewerObservableInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;
import org.eclipse.wb.tests.designer.databinding.rcp.model.AbstractBindingTest;

import org.junit.Test;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class ViewerObservableTest extends AbstractBindingTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Viewer
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_observeSingleSelection() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private DataBindingContext m_bindingContext;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableValue observeValue = BeanProperties.value(\"name\").observe(getClass());",
								"    IObservableValue observeWidget = ViewerProperties.singleSelection().observe(m_viewer);",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindValue(observeWidget, observeValue, null, null);",
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
		//
		assertInstanceOf(SingleSelectionObservableInfo.class, binding.getTargetObservable());
		ViewerObservableInfo observable = (ViewerObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeWidget", observable.getVariableIdentifier());
		assertEquals("m_viewer.selection", observable.getPresentationText());
		//
		WidgetBindableTest.assertBindable(
				shell.getChildrenControls().get(0).getChildren().get(0),
				WidgetBindableInfo.class,
				provider.getObserves(ObserveType.WIDGETS).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0),
				false,
				"m_viewer|m_viewer|org.eclipse.jface.viewers.TableViewer",
				observable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"single selection|observeSingleSelection|java.lang.Object",
				observable.getBindableProperty());
		//
		assertInstanceOf(IMasterDetailProvider.class, observable);
		IMasterDetailProvider detailProvider = (IMasterDetailProvider) observable;
		//
		ObservableInfo masterObservable = detailProvider.getMasterObservable();
		assertNotNull(masterObservable);
		assertNotSame(masterObservable, observable);
		assertInstanceOf(SingleSelectionObservableInfo.class, masterObservable);
		assertNull(masterObservable.getVariableIdentifier());
		assertEquals("m_viewer.selection", observable.getPresentationText());
		//
		WidgetBindableTest.assertBindable(
				shell.getChildrenControls().get(0).getChildren().get(0),
				WidgetBindableInfo.class,
				provider.getObserves(ObserveType.WIDGETS).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0),
				false,
				"m_viewer|m_viewer|org.eclipse.jface.viewers.TableViewer",
				masterObservable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"part of selection|observeSingleSelection|java.lang.Object",
				masterObservable.getBindableProperty());
	}

	@Test
	public void test_observeMultiSelection() throws Exception {
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
								"  private TableViewer m_viewer;",
								"  private TestBean m_bean;",
								"  private DataBindingContext m_bindingContext;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableList observeList = BeanProperties.list(\"name\").observe(Realm.getDefault(), getClass());",
								"    IObservableList observeWidget = ViewerProperties.multipleSelection().observe((Viewer)m_viewer);",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindList(observeWidget, observeList, null, null);",
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
		//
		assertInstanceOf(MultiSelectionObservableInfo.class, binding.getTargetObservable());
		ViewerObservableInfo observable = (ViewerObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeWidget", observable.getVariableIdentifier());
		assertEquals("m_viewer.multiSelection", observable.getPresentationText());
		//
		WidgetBindableTest.assertBindable(
				shell.getChildrenControls().get(0).getChildren().get(0),
				WidgetBindableInfo.class,
				provider.getObserves(ObserveType.WIDGETS).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0),
				false,
				"m_viewer|m_viewer|org.eclipse.jface.viewers.TableViewer",
				observable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"multi selection|observeMultiSelection|java.lang.Object",
				observable.getBindableProperty());
	}

	@Test
	public void test_observeCheckedElements() throws Exception {
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
								"  private CheckboxTreeViewer m_viewer;",
								"  private TestBean m_bean;",
								"  private DataBindingContext m_bindingContext;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new CheckboxTreeViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableSet observeSet = BeanProperties.set(\"names\").observe(Realm.getDefault(), m_bean);",
								"    IObservableSet observeWidget = ViewerProperties.checkedElements(Integer.class).observe((Viewer)m_viewer);",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindSet(observeWidget, observeSet, null, null);",
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
		//
		assertInstanceOf(CheckedElementsObservableInfo.class, binding.getTargetObservable());
		CheckedElementsObservableInfo observable =
				(CheckedElementsObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeWidget", observable.getVariableIdentifier());
		assertEquals("m_viewer.checkedElements(Integer.class)", observable.getPresentationText());
		//
		WidgetBindableTest.assertBindable(
				shell.getChildrenControls().get(0).getChildren().get(0),
				WidgetBindableInfo.class,
				provider.getObserves(ObserveType.WIDGETS).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0),
				false,
				"m_viewer|m_viewer|org.eclipse.jface.viewers.CheckboxTreeViewer",
				observable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"checked elements|observeCheckedElements|java.lang.Object",
				observable.getBindableProperty());
		//
		assertNotNull(observable.getElementType());
		assertEquals("java.lang.Integer", observable.getElementType().getName());
		//
		observable.setElementType(null);
		assertEquals("m_viewer.checkedElements(?????.class)", observable.getPresentationText());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Input
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Viewer_Input_OnlyList() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.List getBeans() {",
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
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private TestBean m_bean;",
								"  private TableViewer m_sourceViewer;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_sourceViewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    ObservableListContentProvider viewerContentProvider = new ObservableListContentProvider();",
								"    m_viewer.setContentProvider(viewerContentProvider);",
								"    IObservableMap viewerLabelProviderMap = BeanProperties.value(TestBean.class, \"name\").observeDetail(viewerContentProvider.getKnownElements());",
								"    m_viewer.setLabelProvider(new ObservableMapLabelProvider(viewerLabelProviderMap));",
								"    //",
								"    IObservableList selectionObserveList = ViewerProperties.multipleSelection().observe(m_sourceViewer);",
								"    m_viewer.setInput(selectionObserveList);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo sourceViewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo sourceViewerMultiSelectionProperty =
				sourceViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(ViewerInputBindingInfo.class, bindings.get(0));
		//
		ViewerInputBindingInfo binding = (ViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(sourceViewerObserve, binding.getModel());
		assertSame(sourceViewerMultiSelectionProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(MultiSelectionObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(sourceViewerObserve, inputObservable.getBindableObject());
		assertSame(sourceViewerMultiSelectionProperty, inputObservable.getBindableProperty());
		//
		assertEquals("m_sourceViewer.multiSelection", inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableListContentProvider, ObservableMaps[\"name\"])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		ObservableMapLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertNotNull(labelProvider);
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
				labelProvider.getClassName());
		assertEquals("ObservableMaps[\"name\"]", labelProvider.getPresentationText());
		assertNull(labelProvider.getVariableIdentifier());
		//
		MapsBeanObservableInfo mapsObservable = labelProvider.getMapsObservable();
		assertNotNull(mapsObservable);
		assertNotNull(mapsObservable.getElementType());
		assertEquals("test.TestBean", mapsObservable.getElementType().getName());
		assertNull(mapsObservable.getBindableObject());
		assertNull(mapsObservable.getBindableProperty());
		//
		String[] properties = mapsObservable.getProperties();
		assertNotNull(properties);
		assertEquals(1, properties.length);
		assertEquals("\"name\"", properties[0]);
		//
		assertNotNull(mapsObservable.getDomainObservable());
		assertInstanceOf(KnownElementsObservableInfo.class, mapsObservable.getDomainObservable());
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) mapsObservable.getDomainObservable();
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(
				knownElementsObservableInfo.getParent(),
				ReflectionUtils.getFieldObject(binding, "m_contentProvider"));
		assertInstanceOf(
				ObservableListContentProviderInfo.class,
				knownElementsObservableInfo.getParent());
		//
		ObservableListContentProviderInfo contentProvider =
				(ObservableListContentProviderInfo) knownElementsObservableInfo.getParent();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableListContentProvider",
				contentProvider.getClassName());
	}

	@Test
	public void test_Viewer_Input_List() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.List getBeans() {",
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
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private TestBean m_bean;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    ObservableListContentProvider viewerContentProvider = new ObservableListContentProvider();",
								"    m_viewer.setContentProvider(viewerContentProvider);",
								"    IObservableMap viewerLabelProviderMap = BeanProperties.value(TestBean.class, \"name\").observeDetail(viewerContentProvider.getKnownElements());",
								"    m_viewer.setLabelProvider(new ObservableMapLabelProvider(viewerLabelProviderMap));",
								"    //",
								"    IObservableList beanObserveList = BeanProperties.list(\"beans\").observe(Realm.getDefault(), m_bean);",
								"    m_viewer.setInput(beanObserveList);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo beanObserve = provider.getObserves(ObserveType.BEANS).get(3);
		IObserveInfo beanBeansProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(ViewerInputBindingInfo.class, bindings.get(0));
		//
		ViewerInputBindingInfo binding = (ViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(beanObserve, binding.getModel());
		assertSame(beanBeansProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(ListBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(beanObserve, inputObservable.getBindableObject());
		assertSame(beanBeansProperty, inputObservable.getBindableProperty());
		//
		assertEquals("m_bean.beans(List)", inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableListContentProvider, ObservableMaps[\"name\"])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		ObservableMapLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertNotNull(labelProvider);
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
				labelProvider.getClassName());
		assertEquals("ObservableMaps[\"name\"]", labelProvider.getPresentationText());
		assertNull(labelProvider.getVariableIdentifier());
		//
		MapsBeanObservableInfo mapsObservable = labelProvider.getMapsObservable();
		assertNotNull(mapsObservable);
		assertNotNull(mapsObservable.getElementType());
		assertEquals("test.TestBean", mapsObservable.getElementType().getName());
		assertNull(mapsObservable.getBindableObject());
		assertNull(mapsObservable.getBindableProperty());
		//
		String[] properties = mapsObservable.getProperties();
		assertNotNull(properties);
		assertEquals(1, properties.length);
		assertEquals("\"name\"", properties[0]);
		//
		assertNotNull(mapsObservable.getDomainObservable());
		assertInstanceOf(KnownElementsObservableInfo.class, mapsObservable.getDomainObservable());
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) mapsObservable.getDomainObservable();
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(
				knownElementsObservableInfo.getParent(),
				ReflectionUtils.getFieldObject(binding, "m_contentProvider"));
		assertInstanceOf(
				ObservableListContentProviderInfo.class,
				knownElementsObservableInfo.getParent());
		//
		ObservableListContentProviderInfo contentProvider =
				(ObservableListContentProviderInfo) knownElementsObservableInfo.getParent();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableListContentProvider",
				contentProvider.getClassName());
	}

	@Test
	public void test_Viewer_Input_List_InputCollection() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.List getBeans() {",
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
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private java.util.List m_beans;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    ObservableListContentProvider viewerContentProvider = new ObservableListContentProvider();",
								"    m_viewer.setContentProvider(viewerContentProvider);",
								"    IObservableMap viewerLabelProviderMap = BeanProperties.value(TestBean.class, \"name\").observeDetail(viewerContentProvider.getKnownElements());",
								"    m_viewer.setLabelProvider(new ObservableMapLabelProvider(viewerLabelProviderMap));",
								"    //",
								"    m_viewer.setInput(new WritableList(m_beans, TestBean.class));",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo beanObserve = provider.getObserves(ObserveType.BEANS).get(3);
		IObserveInfo beanInputCollectionProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(ViewerInputBindingInfo.class, bindings.get(0));
		//
		ViewerInputBindingInfo binding = (ViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(beanObserve, binding.getModel());
		assertSame(beanInputCollectionProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(WritableListBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(beanObserve, inputObservable.getBindableObject());
		assertSame(beanInputCollectionProperty, inputObservable.getBindableProperty());
		//
		assertEquals("WritableList(m_beans, TestBean.class)", inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableListContentProvider, ObservableMaps[\"name\"])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		ObservableMapLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertNotNull(labelProvider);
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
				labelProvider.getClassName());
		assertEquals("ObservableMaps[\"name\"]", labelProvider.getPresentationText());
		assertNull(labelProvider.getVariableIdentifier());
		//
		MapsBeanObservableInfo mapsObservable = labelProvider.getMapsObservable();
		assertNotNull(mapsObservable);
		assertNotNull(mapsObservable.getElementType());
		assertEquals("test.TestBean", mapsObservable.getElementType().getName());
		assertNull(mapsObservable.getBindableObject());
		assertNull(mapsObservable.getBindableProperty());
		//
		String[] properties = mapsObservable.getProperties();
		assertNotNull(properties);
		assertEquals(1, properties.length);
		assertEquals("\"name\"", properties[0]);
		//
		assertNotNull(mapsObservable.getDomainObservable());
		assertInstanceOf(KnownElementsObservableInfo.class, mapsObservable.getDomainObservable());
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) mapsObservable.getDomainObservable();
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(
				knownElementsObservableInfo.getParent(),
				ReflectionUtils.getFieldObject(binding, "m_contentProvider"));
		assertInstanceOf(
				ObservableListContentProviderInfo.class,
				knownElementsObservableInfo.getParent());
		//
		ObservableListContentProviderInfo contentProvider =
				(ObservableListContentProviderInfo) knownElementsObservableInfo.getParent();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableListContentProvider",
				contentProvider.getClassName());
	}

	@Test
	public void test_Viewer_Input_ListDetail() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"}"));
		setFileContentSrc(
				"test/BeanContainer.java",
				getSourceDQ(
						"package test;",
						"public class BeanContainer {",
						"  public java.util.List getBeans() {",
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
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private BeanContainer m_container;",
								"  private TableViewer m_sourceViewer;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_sourceViewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    ObservableListContentProvider viewerContentProvider = new ObservableListContentProvider();",
								"    m_viewer.setContentProvider(viewerContentProvider);",
								"    IObservableMap viewerLabelProviderMap = BeanProperties.value(TestBean.class, \"name\").observeDetail(viewerContentProvider.getKnownElements());",
								"    m_viewer.setLabelProvider(new ObservableMapLabelProvider(viewerLabelProviderMap));",
								"    //",
								"    IObservableValue selectionObserve = ViewerProperties.singleSelection().observe(m_sourceViewer);",
								"    IObservableList containerObserveDetailList = BeanProperties.list(\"beans\", TestBean.class).observeDetail(selectionObserve);",
								"    m_viewer.setInput(containerObserveDetailList);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo sourceViewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo sourceViewerSelectionDetailProperty =
				sourceViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(ViewerInputBindingInfo.class, bindings.get(0));
		//
		ViewerInputBindingInfo binding = (ViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(sourceViewerObserve, binding.getModel());
		assertSame(sourceViewerSelectionDetailProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(DetailListBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(sourceViewerObserve, inputObservable.getBindableObject());
		assertSame(sourceViewerSelectionDetailProperty, inputObservable.getBindableProperty());
		//
		assertEquals(
				"m_sourceViewer.selection.detailList(\"beans\", TestBean.class)",
				inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableListContentProvider, ObservableMaps[\"name\"])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		ObservableMapLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertNotNull(labelProvider);
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
				labelProvider.getClassName());
		assertEquals("ObservableMaps[\"name\"]", labelProvider.getPresentationText());
		assertNull(labelProvider.getVariableIdentifier());
		//
		MapsBeanObservableInfo mapsObservable = labelProvider.getMapsObservable();
		assertNotNull(mapsObservable);
		assertNotNull(mapsObservable.getElementType());
		assertEquals("test.TestBean", mapsObservable.getElementType().getName());
		assertNull(mapsObservable.getBindableObject());
		assertNull(mapsObservable.getBindableProperty());
		//
		String[] properties = mapsObservable.getProperties();
		assertNotNull(properties);
		assertEquals(1, properties.length);
		assertEquals("\"name\"", properties[0]);
		//
		assertNotNull(mapsObservable.getDomainObservable());
		assertInstanceOf(KnownElementsObservableInfo.class, mapsObservable.getDomainObservable());
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) mapsObservable.getDomainObservable();
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(
				knownElementsObservableInfo.getParent(),
				ReflectionUtils.getFieldObject(binding, "m_contentProvider"));
		assertInstanceOf(
				ObservableListContentProviderInfo.class,
				knownElementsObservableInfo.getParent());
		//
		ObservableListContentProviderInfo contentProvider =
				(ObservableListContentProviderInfo) knownElementsObservableInfo.getParent();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableListContentProvider",
				contentProvider.getClassName());
	}

	@Test
	public void test_Viewer_Input_OnlySet() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.Set getBeans() {",
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
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private TestBean m_bean;",
								"  private CheckboxTableViewer m_sourceViewer;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_sourceViewer = new CheckboxTableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    ObservableSetContentProvider viewerContentProvider = new ObservableSetContentProvider();",
								"    m_viewer.setContentProvider(viewerContentProvider);",
								"    IObservableMap viewerLabelProviderMap = BeanProperties.value(TestBean.class, \"name\").observeDetail(viewerContentProvider.getKnownElements());",
								"    m_viewer.setLabelProvider(new ObservableMapLabelProvider(viewerLabelProviderMap));",
								"    //",
								"    IObservableSet checkedObserveSet = ViewerProperties.checkedElements(TestBean.class).observe((Viewer)m_sourceViewer);",
								"    m_viewer.setInput(checkedObserveSet);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		//
		IObserveInfo sourceViewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo sourceViewerCheckedProperty =
				sourceViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(ViewerInputBindingInfo.class, bindings.get(0));
		//
		ViewerInputBindingInfo binding = (ViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(sourceViewerObserve, binding.getModel());
		assertSame(sourceViewerCheckedProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(CheckedElementsObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(sourceViewerObserve, inputObservable.getBindableObject());
		assertSame(sourceViewerCheckedProperty, inputObservable.getBindableProperty());
		//
		assertEquals(
				"m_sourceViewer.checkedElements(TestBean.class)",
				inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableSetContentProvider, ObservableMaps[\"name\"])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		ObservableMapLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertNotNull(labelProvider);
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
				labelProvider.getClassName());
		assertEquals("ObservableMaps[\"name\"]", labelProvider.getPresentationText());
		assertNull(labelProvider.getVariableIdentifier());
		//
		MapsBeanObservableInfo mapsObservable = labelProvider.getMapsObservable();
		assertNotNull(mapsObservable);
		assertNotNull(mapsObservable.getElementType());
		assertEquals("test.TestBean", mapsObservable.getElementType().getName());
		assertNull(mapsObservable.getBindableObject());
		assertNull(mapsObservable.getBindableProperty());
		//
		String[] properties = mapsObservable.getProperties();
		assertNotNull(properties);
		assertEquals(1, properties.length);
		assertEquals("\"name\"", properties[0]);
		//
		assertNotNull(mapsObservable.getDomainObservable());
		assertInstanceOf(KnownElementsObservableInfo.class, mapsObservable.getDomainObservable());
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) mapsObservable.getDomainObservable();
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(
				knownElementsObservableInfo.getParent(),
				ReflectionUtils.getFieldObject(binding, "m_contentProvider"));
		assertInstanceOf(
				ObservableSetContentProviderInfo.class,
				knownElementsObservableInfo.getParent());
		//
		ObservableSetContentProviderInfo contentProvider =
				(ObservableSetContentProviderInfo) knownElementsObservableInfo.getParent();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableSetContentProvider",
				contentProvider.getClassName());
	}

	@Test
	public void test_Viewer_Input_Set() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.Set getBeans() {",
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
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private TestBean m_bean;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    ObservableSetContentProvider viewerContentProvider = new ObservableSetContentProvider();",
								"    m_viewer.setContentProvider(viewerContentProvider);",
								"    IObservableMap viewerLabelProviderMap = BeanProperties.value(TestBean.class, \"name\").observeDetail(viewerContentProvider.getKnownElements());",
								"    m_viewer.setLabelProvider(new ObservableMapLabelProvider(viewerLabelProviderMap));",
								"    //",
								"    IObservableSet beanObserveSet = BeanProperties.set(\"beans\").observe(Realm.getDefault(), m_bean);",
								"    m_viewer.setInput(beanObserveSet);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo beanObserve = provider.getObserves(ObserveType.BEANS).get(3);
		IObserveInfo beanBeansProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(ViewerInputBindingInfo.class, bindings.get(0));
		//
		ViewerInputBindingInfo binding = (ViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(beanObserve, binding.getModel());
		assertSame(beanBeansProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(SetBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(beanObserve, inputObservable.getBindableObject());
		assertSame(beanBeansProperty, inputObservable.getBindableProperty());
		//
		assertEquals("m_bean.beans(Set)", inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableSetContentProvider, ObservableMaps[\"name\"])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		ObservableMapLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertNotNull(labelProvider);
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
				labelProvider.getClassName());
		assertEquals("ObservableMaps[\"name\"]", labelProvider.getPresentationText());
		assertNull(labelProvider.getVariableIdentifier());
		//
		MapsBeanObservableInfo mapsObservable = labelProvider.getMapsObservable();
		assertNotNull(mapsObservable);
		assertNotNull(mapsObservable.getElementType());
		assertEquals("test.TestBean", mapsObservable.getElementType().getName());
		assertNull(mapsObservable.getBindableObject());
		assertNull(mapsObservable.getBindableProperty());
		//
		String[] properties = mapsObservable.getProperties();
		assertNotNull(properties);
		assertEquals(1, properties.length);
		assertEquals("\"name\"", properties[0]);
		//
		assertNotNull(mapsObservable.getDomainObservable());
		assertInstanceOf(KnownElementsObservableInfo.class, mapsObservable.getDomainObservable());
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) mapsObservable.getDomainObservable();
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(
				knownElementsObservableInfo.getParent(),
				ReflectionUtils.getFieldObject(binding, "m_contentProvider"));
		assertInstanceOf(
				ObservableSetContentProviderInfo.class,
				knownElementsObservableInfo.getParent());
		//
		ObservableSetContentProviderInfo contentProvider =
				(ObservableSetContentProviderInfo) knownElementsObservableInfo.getParent();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableSetContentProvider",
				contentProvider.getClassName());
	}

	@Test
	public void test_Viewer_Input_Set_InputCollection() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.Set getBeans() {",
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
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private java.util.Set m_beans;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    ObservableSetContentProvider viewerContentProvider = new ObservableSetContentProvider();",
								"    m_viewer.setContentProvider(viewerContentProvider);",
								"    IObservableMap viewerLabelProviderMap = BeanProperties.value(TestBean.class, \"name\").observeDetail(viewerContentProvider.getKnownElements());",
								"    m_viewer.setLabelProvider(new ObservableMapLabelProvider(viewerLabelProviderMap));",
								"    //",
								"    m_viewer.setInput(new WritableSet(m_beans, TestBean.class));",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo beanObserve = provider.getObserves(ObserveType.BEANS).get(3);
		IObserveInfo beanInputCollectionProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(ViewerInputBindingInfo.class, bindings.get(0));
		//
		ViewerInputBindingInfo binding = (ViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(beanObserve, binding.getModel());
		assertSame(beanInputCollectionProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(WritableSetBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(beanObserve, inputObservable.getBindableObject());
		assertSame(beanInputCollectionProperty, inputObservable.getBindableProperty());
		//
		assertEquals("WritableSet(m_beans, TestBean.class)", inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableSetContentProvider, ObservableMaps[\"name\"])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		ObservableMapLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertNotNull(labelProvider);
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
				labelProvider.getClassName());
		assertEquals("ObservableMaps[\"name\"]", labelProvider.getPresentationText());
		assertNull(labelProvider.getVariableIdentifier());
		//
		MapsBeanObservableInfo mapsObservable = labelProvider.getMapsObservable();
		assertNotNull(mapsObservable);
		assertNotNull(mapsObservable.getElementType());
		assertEquals("test.TestBean", mapsObservable.getElementType().getName());
		assertNull(mapsObservable.getBindableObject());
		assertNull(mapsObservable.getBindableProperty());
		//
		String[] properties = mapsObservable.getProperties();
		assertNotNull(properties);
		assertEquals(1, properties.length);
		assertEquals("\"name\"", properties[0]);
		//
		assertNotNull(mapsObservable.getDomainObservable());
		assertInstanceOf(KnownElementsObservableInfo.class, mapsObservable.getDomainObservable());
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) mapsObservable.getDomainObservable();
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(
				knownElementsObservableInfo.getParent(),
				ReflectionUtils.getFieldObject(binding, "m_contentProvider"));
		assertInstanceOf(
				ObservableSetContentProviderInfo.class,
				knownElementsObservableInfo.getParent());
		//
		ObservableSetContentProviderInfo contentProvider =
				(ObservableSetContentProviderInfo) knownElementsObservableInfo.getParent();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableSetContentProvider",
				contentProvider.getClassName());
	}

	@Test
	public void test_Viewer_Input_SetDetail() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"}"));
		setFileContentSrc(
				"test/BeanContainer.java",
				getSourceDQ(
						"package test;",
						"public class BeanContainer {",
						"  public java.util.Set getBeans() {",
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
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TableViewer m_viewer;",
								"  private BeanContainer m_container;",
								"  private TableViewer m_sourceViewer;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_sourceViewer = new TableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    ObservableSetContentProvider viewerContentProvider = new ObservableSetContentProvider();",
								"    m_viewer.setContentProvider(viewerContentProvider);",
								"    IObservableMap viewerLabelProviderMap = BeanProperties.value(TestBean.class, \"name\").observeDetail(viewerContentProvider.getKnownElements());",
								"    m_viewer.setLabelProvider(new ObservableMapLabelProvider(viewerLabelProviderMap));",
								"    //",
								"    IObservableValue selectionObserve = ViewerProperties.singleSelection().observe(m_sourceViewer);",
								"    IObservableSet containerObserveDetailSet = BeanProperties.set(\"beans\", TestBean.class).observeDetail(selectionObserve);",
								"    m_viewer.setInput(containerObserveDetailSet);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo sourceViewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo sourceViewerSelectionDetailProperty =
				sourceViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(ViewerInputBindingInfo.class, bindings.get(0));
		//
		ViewerInputBindingInfo binding = (ViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(sourceViewerObserve, binding.getModel());
		assertSame(sourceViewerSelectionDetailProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(DetailSetBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(sourceViewerObserve, inputObservable.getBindableObject());
		assertSame(sourceViewerSelectionDetailProperty, inputObservable.getBindableProperty());
		//
		assertEquals(
				"m_sourceViewer.selection.detailSet(\"beans\", TestBean.class)",
				inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableSetContentProvider, ObservableMaps[\"name\"])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		ObservableMapLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertNotNull(labelProvider);
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
				labelProvider.getClassName());
		assertEquals("ObservableMaps[\"name\"]", labelProvider.getPresentationText());
		assertNull(labelProvider.getVariableIdentifier());
		//
		MapsBeanObservableInfo mapsObservable = labelProvider.getMapsObservable();
		assertNotNull(mapsObservable);
		assertNotNull(mapsObservable.getElementType());
		assertEquals("test.TestBean", mapsObservable.getElementType().getName());
		assertNull(mapsObservable.getBindableObject());
		assertNull(mapsObservable.getBindableProperty());
		//
		String[] properties = mapsObservable.getProperties();
		assertNotNull(properties);
		assertEquals(1, properties.length);
		assertEquals("\"name\"", properties[0]);
		//
		assertNotNull(mapsObservable.getDomainObservable());
		assertInstanceOf(KnownElementsObservableInfo.class, mapsObservable.getDomainObservable());
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) mapsObservable.getDomainObservable();
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(
				knownElementsObservableInfo.getParent(),
				ReflectionUtils.getFieldObject(binding, "m_contentProvider"));
		assertInstanceOf(
				ObservableSetContentProviderInfo.class,
				knownElementsObservableInfo.getParent());
		//
		ObservableSetContentProviderInfo contentProvider =
				(ObservableSetContentProviderInfo) knownElementsObservableInfo.getParent();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableSetContentProvider",
				contentProvider.getClassName());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree Input
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Viewer_TreeInput_List_InputCollection() throws Exception {
		DataBindingsCodeUtils.ensureDesignerResources(m_testProject.getJavaProject());
		//
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.List getBeans() {",
						"    return null;",
						"  }",
						"  public TestBean getParent() {",
						"    return null;",
						"  }",
						"  public boolean getHasChildren() {",
						"    return false;",
						"  }",
						"  public org.eclipse.swt.graphics.Image getImage() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"import org.eclipse.wb.rcp.databinding.BeansListObservableFactory;",
								"import org.eclipse.wb.rcp.databinding.TreeBeanAdvisor;",
								"import org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider;",
								"public class Test {",
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TreeViewer m_viewer;",
								"  private java.util.List m_beans;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TreeViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    BeansListObservableFactory treeViewerFactoryList = new BeansListObservableFactory(TestBean.class, \"beans\");",
								"    TreeBeanAdvisor treeViewerAdvisor = new TreeBeanAdvisor(TestBean.class, \"parent\", \"beans\", \"hasChildren\");",
								"    ObservableListTreeContentProvider treeViewerContentProviderList = new ObservableListTreeContentProvider(treeViewerFactoryList, treeViewerAdvisor);",
								"    m_viewer.setContentProvider(treeViewerContentProviderList);",
								"    //",
								"    m_viewer.setLabelProvider(new TreeObservableLabelProvider(treeViewerContentProviderList.getKnownElements(), TestBean.class, \"name\", \"image\"));",
								"    //",
								"    WritableList beansWritableList = new WritableList(m_beans, TestBean.class);",
								"    m_viewer.setInput(beansWritableList);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo beanObserve = provider.getObserves(ObserveType.BEANS).get(3);
		IObserveInfo beanInputCollectionProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(TreeViewerInputBindingInfo.class, bindings.get(0));
		//
		TreeViewerInputBindingInfo binding = (TreeViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(beanObserve, binding.getModel());
		assertSame(beanInputCollectionProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(WritableListBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(beanObserve, inputObservable.getBindableObject());
		assertSame(beanInputCollectionProperty, inputObservable.getBindableProperty());
		//
		assertEquals("WritableList(m_beans, TestBean.class)", inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableListTreeContentProvider[parent, beans, hasChildren], TreeObservableLabelProvider[name, image])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		assertNotNull(binding.getLabelProvider());
		assertInstanceOf(TreeObservableLabelProviderInfo.class, binding.getLabelProvider());
		//
		TreeObservableLabelProviderInfo labelProvider =
				(TreeObservableLabelProviderInfo) binding.getLabelProvider();
		assertEquals(
				"org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider",
				labelProvider.getClassName());
		assertSame(
				binding.getElementType(),
				ReflectionUtils.getFieldObject(labelProvider, "m_elementType"));
		assertEquals("name", labelProvider.getTextProperty());
		assertEquals("image", labelProvider.getImageProperty());
		//
		assertInstanceOf(
				KnownElementsObservableInfo.class,
				ReflectionUtils.getFieldObject(labelProvider, "m_allElementsObservable"));
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) ReflectionUtils.getFieldObject(
						labelProvider,
						"m_allElementsObservable");
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(knownElementsObservableInfo.getParent(), binding.getContentProvider());
		//
		assertInstanceOf(ObservableListTreeContentProviderInfo.class, binding.getContentProvider());
		//
		ObservableListTreeContentProviderInfo contentProvider =
				(ObservableListTreeContentProviderInfo) binding.getContentProvider();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider",
				contentProvider.getClassName());
		assertEquals(
				"ObservableListTreeContentProvider[parent, beans, hasChildren]",
				contentProvider.getPresentationText());
		//
		assertNotNull(contentProvider.getFactoryInfo());
		assertInstanceOf(BeansListObservableFactoryInfo.class, contentProvider.getFactoryInfo());
		//
		BeansListObservableFactoryInfo factory =
				(BeansListObservableFactoryInfo) contentProvider.getFactoryInfo();
		assertSame(factory.getElementType(), binding.getElementType());
		assertEquals("beans", ReflectionUtils.getFieldString(factory, "m_propertyName"));
		//
		assertNotNull(contentProvider.getAdvisorInfo());
		assertInstanceOf(TreeBeanAdvisorInfo.class, contentProvider.getAdvisorInfo());
		//
		TreeBeanAdvisorInfo advisor = (TreeBeanAdvisorInfo) contentProvider.getAdvisorInfo();
		assertEquals("org.eclipse.wb.rcp.databinding.TreeBeanAdvisor", advisor.getClassName());
		assertEquals("parent", advisor.getParentProperty());
		assertEquals("beans", advisor.getChildrenProperty());
		assertEquals("hasChildren", advisor.getHasChildrenProperty());
	}

	@Test
	public void test_Viewer_TreeInput_Set_InputCollection() throws Exception {
		DataBindingsCodeUtils.ensureDesignerResources(m_testProject.getJavaProject());
		//
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.List getBeans() {",
						"    return null;",
						"  }",
						"  public TestBean getParent() {",
						"    return null;",
						"  }",
						"  public boolean getHasChildren() {",
						"    return false;",
						"  }",
						"  public org.eclipse.swt.graphics.Image getImage() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"import org.eclipse.wb.rcp.databinding.BeansSetObservableFactory;",
								"import org.eclipse.wb.rcp.databinding.TreeBeanAdvisor;",
								"import org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider;",
								"public class Test {",
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TreeViewer m_viewer;",
								"  private java.util.Set m_beans;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TreeViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    BeansSetObservableFactory treeViewerFactorySet = new BeansSetObservableFactory(TestBean.class, \"beans\");",
								"    TreeBeanAdvisor treeViewerAdvisor = new TreeBeanAdvisor(TestBean.class, \"parent\", \"beans\", \"hasChildren\");",
								"    ObservableSetTreeContentProvider treeViewerContentProviderSet = new ObservableSetTreeContentProvider(treeViewerFactorySet, treeViewerAdvisor);",
								"    m_viewer.setContentProvider(treeViewerContentProviderSet);",
								"    //",
								"    m_viewer.setLabelProvider(new TreeObservableLabelProvider(treeViewerContentProviderSet.getKnownElements(), TestBean.class, \"name\", \"image\"));",
								"    //",
								"    WritableSet beansWritableSet = new WritableSet(m_beans, TestBean.class);",
								"    m_viewer.setInput(beansWritableSet);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo beanObserve = provider.getObserves(ObserveType.BEANS).get(3);
		IObserveInfo beanInputCollectionProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(TreeViewerInputBindingInfo.class, bindings.get(0));
		//
		TreeViewerInputBindingInfo binding = (TreeViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(beanObserve, binding.getModel());
		assertSame(beanInputCollectionProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(WritableSetBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(beanObserve, inputObservable.getBindableObject());
		assertSame(beanInputCollectionProperty, inputObservable.getBindableProperty());
		//
		assertEquals("WritableSet(m_beans, TestBean.class)", inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableSetTreeContentProvider[parent, beans, hasChildren], TreeObservableLabelProvider[name, image])",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		assertNotNull(binding.getLabelProvider());
		assertInstanceOf(TreeObservableLabelProviderInfo.class, binding.getLabelProvider());
		//
		TreeObservableLabelProviderInfo labelProvider =
				(TreeObservableLabelProviderInfo) binding.getLabelProvider();
		assertEquals(
				"org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider",
				labelProvider.getClassName());
		assertSame(
				binding.getElementType(),
				ReflectionUtils.getFieldObject(labelProvider, "m_elementType"));
		assertEquals("name", labelProvider.getTextProperty());
		assertEquals("image", labelProvider.getImageProperty());
		//
		assertInstanceOf(
				KnownElementsObservableInfo.class,
				ReflectionUtils.getFieldObject(labelProvider, "m_allElementsObservable"));
		//
		KnownElementsObservableInfo knownElementsObservableInfo =
				(KnownElementsObservableInfo) ReflectionUtils.getFieldObject(
						labelProvider,
						"m_allElementsObservable");
		assertNull(knownElementsObservableInfo.getBindableObject());
		assertNull(knownElementsObservableInfo.getBindableProperty());
		//
		assertNotNull(knownElementsObservableInfo.getParent());
		assertSame(knownElementsObservableInfo.getParent(), binding.getContentProvider());
		//
		assertInstanceOf(ObservableSetTreeContentProviderInfo.class, binding.getContentProvider());
		//
		ObservableSetTreeContentProviderInfo contentProvider =
				(ObservableSetTreeContentProviderInfo) binding.getContentProvider();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider",
				contentProvider.getClassName());
		assertEquals(
				"ObservableSetTreeContentProvider[parent, beans, hasChildren]",
				contentProvider.getPresentationText());
		//
		assertNotNull(contentProvider.getFactoryInfo());
		assertInstanceOf(BeansSetObservableFactoryInfo.class, contentProvider.getFactoryInfo());
		//
		BeansSetObservableFactoryInfo factory =
				(BeansSetObservableFactoryInfo) contentProvider.getFactoryInfo();
		assertSame(factory.getElementType(), binding.getElementType());
		assertEquals("beans", ReflectionUtils.getFieldString(factory, "m_propertyName"));
		assertEquals("org.eclipse.wb.rcp.databinding.BeansSetObservableFactory", factory.getClassName());
		//
		assertNotNull(contentProvider.getAdvisorInfo());
		assertInstanceOf(TreeBeanAdvisorInfo.class, contentProvider.getAdvisorInfo());
		//
		TreeBeanAdvisorInfo advisor = (TreeBeanAdvisorInfo) contentProvider.getAdvisorInfo();
		assertEquals("org.eclipse.wb.rcp.databinding.TreeBeanAdvisor", advisor.getClassName());
		assertEquals("parent", advisor.getParentProperty());
		assertEquals("beans", advisor.getChildrenProperty());
		assertEquals("hasChildren", advisor.getHasChildrenProperty());
	}

	@Test
	public void test_Viewer_TreeInput_List_JFace() throws Exception {
		DataBindingsCodeUtils.ensureDesignerResources(m_testProject.getJavaProject());
		//
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getName() {",
						"    return null;",
						"  }",
						"  public java.util.List getBeans() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		createModelCompilationUnit("test", "TestFactory.java", DatabindingTestUtils.getTestSource(
				"public class TestFactory implements IObservableFactory {",
				"  public IObservable createObservable(Object target) {",
				"    return null;",
				"  }",
				"}"));
		waitForAutoBuild();
		//
		createModelCompilationUnit("test", "TestAdvisor.java", DatabindingTestUtils.getTestSource(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public class TestAdvisor extends TreeStructureAdvisor {",
				"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"import org.eclipse.wb.rcp.databinding.BeansListObservableFactory;",
								"import org.eclipse.wb.rcp.databinding.TreeBeanAdvisor;",
								"import org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider;",
								"public class Test {",
								"  private DataBindingContext m_bindingContext;",
								"  protected Shell m_shell;",
								"  private TreeViewer m_viewer;",
								"  private java.util.List m_beans;",
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
								"    m_shell.setLayout(new GridLayout());",
								"    m_viewer = new TreeViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    //",
								"    TestFactory treeFactory = new TestFactory();",
								"    TestAdvisor treeAdvisor = new TestAdvisor();",
								"    ObservableListTreeContentProvider treeViewerContentProviderList = new ObservableListTreeContentProvider(treeFactory, treeAdvisor);",
								"    m_viewer.setContentProvider(treeViewerContentProviderList);",
								"    //",
								"    m_viewer.setLabelProvider(new LabelProvider());",
								"    //",
								"    WritableList beansWritableList = new WritableList(m_beans, TestBean.class);",
								"    m_viewer.setInput(beansWritableList);",
								"    //",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IObserveInfo shellObserve = provider.getObserves(ObserveType.WIDGETS).get(0);
		IObserveInfo viewerObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo beanObserve = provider.getObserves(ObserveType.BEANS).get(3);
		IObserveInfo beanInputCollectionProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		List<IBindingInfo> bindings = provider.getBindings();
		assertEquals(1, bindings.size());
		assertInstanceOf(TreeViewerInputBindingInfo.class, bindings.get(0));
		//
		TreeViewerInputBindingInfo binding = (TreeViewerInputBindingInfo) bindings.get(0);
		//
		assertSame(viewerObserve, binding.getViewer());
		assertSame(viewerObserve, binding.getTarget());
		assertSame(viewerInputProperty, binding.getTargetProperty());
		//
		assertSame(beanObserve, binding.getModel());
		assertSame(beanInputCollectionProperty, binding.getModelProperty());
		//
		assertNotNull(binding.getInputObservable());
		assertInstanceOf(WritableListBeanObservableInfo.class, binding.getInputObservable());
		//
		ObservableInfo inputObservable = binding.getInputObservable();
		assertSame(beanObserve, inputObservable.getBindableObject());
		assertSame(beanInputCollectionProperty, inputObservable.getBindableProperty());
		//
		assertEquals("WritableList(m_beans, TestBean.class)", inputObservable.getPresentationText());
		//
		assertNull(binding.getVariableIdentifier());
		assertEquals(
				"m_viewer.input(ObservableListTreeContentProvider, LabelProvider)",
				binding.getPresentationText());
		//
		assertNotNull(binding.getElementType());
		assertEquals("test.TestBean", binding.getElementType().getName());
		//
		assertNotNull(binding.getLabelProvider());
		//
		AbstractLabelProviderInfo labelProvider = binding.getLabelProvider();
		assertEquals("org.eclipse.jface.viewers.LabelProvider", labelProvider.getClassName());
		//
		assertInstanceOf(ObservableListTreeContentProviderInfo.class, binding.getContentProvider());
		//
		ObservableListTreeContentProviderInfo contentProvider =
				(ObservableListTreeContentProviderInfo) binding.getContentProvider();
		assertEquals(
				"org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider",
				contentProvider.getClassName());
		assertEquals("ObservableListTreeContentProvider", contentProvider.getPresentationText());
		//
		assertNotNull(contentProvider.getFactoryInfo());
		//
		ObservableFactoryInfo factory = contentProvider.getFactoryInfo();
		assertEquals("test.TestFactory", factory.getClassName());
		//
		assertNotNull(contentProvider.getAdvisorInfo());
		//
		TreeStructureAdvisorInfo advisor = contentProvider.getAdvisorInfo();
		assertEquals("test.TestAdvisor", advisor.getClassName());
	}
}