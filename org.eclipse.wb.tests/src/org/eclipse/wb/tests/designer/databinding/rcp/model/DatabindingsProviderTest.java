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
package org.eclipse.wb.tests.designer.databinding.rcp.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.FieldBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ListBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.SetBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ValueBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateListStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateSetStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateValueStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.TreeViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ItemsSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.MultiSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.TextSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.property.BindingsProperty;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoRelatedTest;
import org.eclipse.wb.tests.designer.core.model.util.MorphingSupportTest;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class DatabindingsProviderTest extends AbstractBindingTest {
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
	public void test_common() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test {",
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
						"}");
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		assertNotNull(provider.getRootInfo());
		assertNull(provider.getRootInfo().getInitDataBindings());
		assertNotNull(provider.getRootInfo().getContextInfo());
		assertNotNull(provider.getRootInfo().getContextInfo().getBindings());
		assertTrue(provider.getRootInfo().getContextInfo().getBindings().isEmpty());
		assertNull(provider.getRootInfo().getContextInfo().getVariableIdentifier());
		//
		List<ObserveType> types = provider.getTypes();
		assertEquals(2, types.size());
		//
		assertSame(ObserveType.BEANS, types.get(0));
		assertSame(ObserveType.BEANS, provider.getModelStartType());
		//
		assertSame(ObserveType.WIDGETS, types.get(1));
		assertSame(ObserveType.WIDGETS, provider.getTargetStartType());
		//
		List<ObserveTypeContainer> containers = provider.getContainers();
		assertEquals(2, containers.size());
		//
		assertSame(ObserveType.BEANS, containers.get(0).getObserveType());
		assertSame(containers.get(0), provider.getContainer(ObserveType.BEANS));
		assertTrue(containers.get(0).isModelStartType());
		assertFalse(containers.get(0).isTargetStartType());
		//
		assertSame(ObserveType.WIDGETS, containers.get(1).getObserveType());
		assertSame(containers.get(1), provider.getContainer(ObserveType.WIDGETS));
		assertFalse(containers.get(1).isModelStartType());
		assertTrue(containers.get(1).isTargetStartType());
	}

	public void test_context_1() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
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
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  private DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    return bindingContext;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		assertNotNull(provider.getRootInfo());
		assertNotNull(provider.getRootInfo().getInitDataBindings());
		assertEquals(
				"initDataBindings",
				provider.getRootInfo().getInitDataBindings().getName().getIdentifier());
		assertNotNull(provider.getRootInfo().getContextInfo());
		assertNotNull(provider.getRootInfo().getContextInfo().getBindings());
		assertTrue(provider.getRootInfo().getContextInfo().getBindings().isEmpty());
		assertEquals("bindingContext", provider.getRootInfo().getContextInfo().getVariableIdentifier());
		assertFalse(provider.getRootInfo().getContextInfo().isAddInitializeContext());
		assertNull(provider.getRootInfo().getContextInfo().getUserTryCatchBlock());
	}

	public void test_context_2() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
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
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  private DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    initializeContext(bindingContext);",
						"    return bindingContext;",
						"  }",
						"  protected void initializeContext(DataBindingContext context) {",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		assertNotNull(provider.getRootInfo());
		assertNotNull(provider.getRootInfo().getInitDataBindings());
		assertEquals(
				"initDataBindings",
				provider.getRootInfo().getInitDataBindings().getName().getIdentifier());
		assertNotNull(provider.getRootInfo().getContextInfo());
		assertNotNull(provider.getRootInfo().getContextInfo().getBindings());
		assertTrue(provider.getRootInfo().getContextInfo().getBindings().isEmpty());
		assertEquals("bindingContext", provider.getRootInfo().getContextInfo().getVariableIdentifier());
		assertTrue(provider.getRootInfo().getContextInfo().isAddInitializeContext());
		assertNull(provider.getRootInfo().getContextInfo().getUserTryCatchBlock());
	}

	public void test_context_3() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
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
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  private DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    try {",
						"    } catch (Throwable e) {",
						"        System.out.println(e);",
						"    } finally {",
						"        System.out.println(bindingContext);",
						"    }",
						"    return bindingContext;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		assertNotNull(provider.getRootInfo());
		assertNotNull(provider.getRootInfo().getInitDataBindings());
		assertEquals(
				"initDataBindings",
				provider.getRootInfo().getInitDataBindings().getName().getIdentifier());
		assertNotNull(provider.getRootInfo().getContextInfo());
		assertNotNull(provider.getRootInfo().getContextInfo().getBindings());
		assertTrue(provider.getRootInfo().getContextInfo().getBindings().isEmpty());
		assertEquals("bindingContext", provider.getRootInfo().getContextInfo().getVariableIdentifier());
		assertFalse(provider.getRootInfo().getContextInfo().isAddInitializeContext());
		assertEquals(
				"} catch (Throwable e) {\n				System.out.println(e);\n		} finally {\n				System.out.println(bindingContext);\n		}",
				provider.getRootInfo().getContextInfo().getUserTryCatchBlock());
	}

	/**
	 * CASE-48323 Unable to remove a label from view in design tab. Tweaked
	 * {@link FieldBeanBindableInfo#update(BeansObserveTypeContainer)} .
	 */
	public void test_updateBindables() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private Object m_object;",
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
						"    m_object = new GridData();",
						"    m_shell.setLayoutData(m_object);",
						"    m_shell.setLayout(new FillLayout());",
						"    Button button = new Button(m_shell, SWT.NONE);",
						"    button.setText(\"New Button\");",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IObserveInfo> beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(3, beanObservables.size());
		//
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("m_object - Object", beanObservables.get(1).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(2).getPresentation().getText());
		//
		AbstractJavaInfoRelatedTest.<JavaInfo>getJavaInfoByName("button").delete();
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Object m_object;",
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
				"    m_object = new GridData();",
				"    m_shell.setLayoutData(m_object);",
				"    m_shell.setLayout(new FillLayout());",
				"  }",
				"}"), m_lastEditor);
	}

	public void test_synchronizedObserves() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
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
						"    m_shell.setLayout(new FillLayout());",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IObserveInfo> beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(2, beanObservables.size());
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(1).getPresentation().getText());
		//
		List<IObserveInfo> widgetObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		assertEquals(1, widgetObservables.size());
		//
		IObserveInfo shellObserve = widgetObservables.get(0);
		assertEquals("m_shell", shellObserve.getPresentation().getText());
		assertTrue(shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).isEmpty());
		//
		// ============================================================================
		//
		ControlInfo buttonInfo = createJavaInfo("org.eclipse.swt.widgets.Button");
		shell.getLayout().command_CREATE(buttonInfo, null);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
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
				"    m_shell.setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(m_shell, SWT.NONE);",
				"      button.setText(\"New Button\");",
				"    }",
				"  }",
				"}"), m_lastEditor);
		//
		beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(2, beanObservables.size());
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(1).getPresentation().getText());
		//
		widgetObservables = provider.getContainer(ObserveType.WIDGETS).getObservables();
		assertEquals(1, widgetObservables.size());
		//
		shellObserve = widgetObservables.get(0);
		assertEquals("m_shell", shellObserve.getPresentation().getText());
		//
		List<IObserveInfo> shellChildren =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, shellChildren.size());
		assertEquals("button - \"New Button\"", shellChildren.get(0).getPresentation().getText());
		//
		// ============================================================================
		//
		buttonInfo.getVariableSupport().convertLocalToField();
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Button button;",
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
				"    m_shell.setLayout(new FillLayout());",
				"    {",
				"      button = new Button(m_shell, SWT.NONE);",
				"      button.setText(\"New Button\");",
				"    }",
				"  }",
				"}"), m_lastEditor);
		//
		beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(3, beanObservables.size());
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("button - Button", beanObservables.get(1).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(2).getPresentation().getText());
		//
		widgetObservables = provider.getContainer(ObserveType.WIDGETS).getObservables();
		assertEquals(1, widgetObservables.size());
		//
		shellObserve = widgetObservables.get(0);
		assertEquals("m_shell", shellObserve.getPresentation().getText());
		//
		shellChildren = shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, shellChildren.size());
		assertEquals("button - \"New Button\"", shellChildren.get(0).getPresentation().getText());
		//
		// ============================================================================
		//
		final ControlInfo comboInfo = createJavaInfo("org.eclipse.swt.widgets.Combo");
		shell.getLayout().command_CREATE(comboInfo, null);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Button button;",
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
				"    m_shell.setLayout(new FillLayout());",
				"    {",
				"      button = new Button(m_shell, SWT.NONE);",
				"      button.setText(\"New Button\");",
				"    }",
				"    {",
				"      Combo combo = new Combo(m_shell, SWT.NONE);",
				"    }",
				"  }",
				"}"), m_lastEditor);
		//
		beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(3, beanObservables.size());
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("button - Button", beanObservables.get(1).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(2).getPresentation().getText());
		//
		widgetObservables = provider.getContainer(ObserveType.WIDGETS).getObservables();
		assertEquals(1, widgetObservables.size());
		//
		shellObserve = widgetObservables.get(0);
		assertEquals("m_shell", shellObserve.getPresentation().getText());
		//
		shellChildren = shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(2, shellChildren.size());
		assertEquals("button - \"New Button\"", shellChildren.get(0).getPresentation().getText());
		assertEquals("combo", shellChildren.get(1).getPresentation().getText());
		//
		// ============================================================================
		//
		shell.getLayout().command_MOVE(comboInfo, buttonInfo);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Button button;",
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
				"    m_shell.setLayout(new FillLayout());",
				"    {",
				"      Combo combo = new Combo(m_shell, SWT.NONE);",
				"    }",
				"    {",
				"      button = new Button(m_shell, SWT.NONE);",
				"      button.setText(\"New Button\");",
				"    }",
				"  }",
				"}"), m_lastEditor);
		//
		beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(3, beanObservables.size());
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("button - Button", beanObservables.get(1).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(2).getPresentation().getText());
		//
		widgetObservables = provider.getContainer(ObserveType.WIDGETS).getObservables();
		assertEquals(1, widgetObservables.size());
		//
		shellObserve = widgetObservables.get(0);
		assertEquals("m_shell", shellObserve.getPresentation().getText());
		//
		shellChildren = shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(2, shellChildren.size());
		assertEquals("combo", shellChildren.get(0).getPresentation().getText());
		assertEquals("button - \"New Button\"", shellChildren.get(1).getPresentation().getText());
		//
		// ============================================================================
		//
		comboInfo.getVariableSupport().convertLocalToField();
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Button button;",
				"  private Combo combo;",
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
				"    m_shell.setLayout(new FillLayout());",
				"    {",
				"      combo = new Combo(m_shell, SWT.NONE);",
				"    }",
				"    {",
				"      button = new Button(m_shell, SWT.NONE);",
				"      button.setText(\"New Button\");",
				"    }",
				"  }",
				"}"), m_lastEditor);
		//
		beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(4, beanObservables.size());
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("button - Button", beanObservables.get(1).getPresentation().getText());
		assertEquals("combo - Combo", beanObservables.get(2).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(3).getPresentation().getText());
		//
		widgetObservables = provider.getContainer(ObserveType.WIDGETS).getObservables();
		assertEquals(1, widgetObservables.size());
		//
		shellObserve = widgetObservables.get(0);
		assertEquals("m_shell", shellObserve.getPresentation().getText());
		//
		shellChildren = shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(2, shellChildren.size());
		assertEquals("combo", shellChildren.get(0).getPresentation().getText());
		assertEquals("button - \"New Button\"", shellChildren.get(1).getPresentation().getText());
		//
		// ============================================================================
		//
		buttonInfo.delete();
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Combo combo;",
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
				"    m_shell.setLayout(new FillLayout());",
				"    {",
				"      combo = new Combo(m_shell, SWT.NONE);",
				"    }",
				"  }",
				"}"), m_lastEditor);
		//
		beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(3, beanObservables.size());
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("combo - Combo", beanObservables.get(1).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(2).getPresentation().getText());
		//
		widgetObservables = provider.getContainer(ObserveType.WIDGETS).getObservables();
		assertEquals(1, widgetObservables.size());
		//
		shellObserve = widgetObservables.get(0);
		assertEquals("m_shell", shellObserve.getPresentation().getText());
		//
		shellChildren = shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, shellChildren.size());
		assertEquals("combo", shellChildren.get(0).getPresentation().getText());
		//
		// ============================================================================
		//
		ExecutionUtils.run(shell.getRootJava(), new RunnableEx() {
			@Override
			public void run() throws Exception {
				Class<?> textWidgetClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Text");
				MorphingTargetDescription target = new MorphingTargetDescription(textWidgetClass, null);
				MorphingSupportTest.morph(comboInfo, target);
			}
		});
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Text combo;",
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
				"    m_shell.setLayout(new FillLayout());",
				"    {",
				"      combo = new Text(m_shell, SWT.NONE);",
				"    }",
				"  }",
				"}"), m_lastEditor);
		//
		beanObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		assertEquals(3, beanObservables.size());
		assertEquals("m_shell - Shell", beanObservables.get(0).getPresentation().getText());
		assertEquals("combo - Text", beanObservables.get(1).getPresentation().getText());
		assertEquals("getClass()", beanObservables.get(2).getPresentation().getText());
		//
		widgetObservables = provider.getContainer(ObserveType.WIDGETS).getObservables();
		assertEquals(1, widgetObservables.size());
		//
		shellObserve = widgetObservables.get(0);
		assertEquals("m_shell", shellObserve.getPresentation().getText());
		//
		shellChildren = shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(1, shellChildren.size());
		assertEquals("combo", shellChildren.get(0).getPresentation().getText());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validate tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_validate_reference() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
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
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> observables = provider.getContainer(ObserveType.WIDGETS).getObservables();
		//
		IObserveInfo shellObserve = observables.get(0);
		List<IObserveInfo> properties =
				shellObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertFalse(provider.validate(shellObserve, properties.get(0), shellObserve, properties.get(0)));
	}

	public void test_validate_null() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo contextObserve = beansObservables.get(10);
		IObserveInfo contextValidateStatusMapProperty_nullFactory =
				contextObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		validateType(
				contextObserve,
				contextValidateStatusMapProperty_nullFactory,
				false,
				false,
				false,
				false,
				false,
				false,
				false);
	}

	public void test_validate_InputCollection() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getValue() {",
						"    return null;",
						"  }",
						"  public java.util.List getValues() {",
						"    return null;",
						"  }",
						"  public java.util.Set getNames() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private java.util.ArrayList m_list;",
						"  private TestBean m_bean;",
						"  private CheckboxTableViewer m_viewer;",
						"  private java.util.ArrayList m_otherList;",
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
						"    m_viewer = new CheckboxTableViewer(m_shell, SWT.BORDER);",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		//
		IObserveInfo listObserve = beansObservables.get(1);
		IObserveInfo listInputCollectionPropety =
				listObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		IObserveInfo beanObserve = beansObservables.get(2);
		IObserveInfo beanNamesProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		IObserveInfo beanValueProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		IObserveInfo beanValuesProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		assertFalse(provider.validate(
				listObserve,
				listInputCollectionPropety,
				beanObserve,
				beanNamesProperty));
		assertFalse(provider.validate(
				listObserve,
				listInputCollectionPropety,
				beanObserve,
				beanValueProperty));
		assertFalse(provider.validate(
				listObserve,
				listInputCollectionPropety,
				beanObserve,
				beanValuesProperty));
		assertFalse(provider.validate(
				beanObserve,
				beanNamesProperty,
				listObserve,
				listInputCollectionPropety));
		assertFalse(provider.validate(
				beanObserve,
				beanValueProperty,
				listObserve,
				listInputCollectionPropety));
		assertFalse(provider.validate(
				beanObserve,
				beanValuesProperty,
				listObserve,
				listInputCollectionPropety));
		//
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo tableObserve =
				widgetsObservables.get(0).getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerSelectionDetailProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		IObserveInfo viewerMultiSelectionProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		IObserveInfo viewerCheckedProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(5);
		//
		assertFalse(provider.validate(
				listObserve,
				listInputCollectionPropety,
				viewerObserve,
				viewerSelectionDetailProperty));
		assertFalse(provider.validate(
				viewerObserve,
				viewerSelectionDetailProperty,
				listObserve,
				listInputCollectionPropety));
		//
		assertFalse(provider.validate(
				listObserve,
				listInputCollectionPropety,
				viewerObserve,
				viewerMultiSelectionProperty));
		assertFalse(provider.validate(
				viewerObserve,
				viewerMultiSelectionProperty,
				listObserve,
				listInputCollectionPropety));
		//
		assertFalse(provider.validate(
				listObserve,
				listInputCollectionPropety,
				viewerObserve,
				viewerCheckedProperty));
		assertFalse(provider.validate(
				viewerObserve,
				viewerCheckedProperty,
				listObserve,
				listInputCollectionPropety));
		//
		assertTrue(provider.validate(
				listObserve,
				listInputCollectionPropety,
				viewerObserve,
				viewerInputProperty));
		assertTrue(provider.validate(
				viewerObserve,
				viewerInputProperty,
				listObserve,
				listInputCollectionPropety));
		//
		IObserveInfo otherListObserve = beansObservables.get(4);
		IObserveInfo otherListInputCollectionPropety =
				otherListObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		assertFalse(provider.validate(
				listObserve,
				listInputCollectionPropety,
				otherListObserve,
				otherListInputCollectionPropety));
		assertFalse(provider.validate(
				otherListObserve,
				otherListInputCollectionPropety,
				listObserve,
				listInputCollectionPropety));
	}

	public void test_validate_Input() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getValue() {",
						"    return null;",
						"  }",
						"  public java.util.List getValues() {",
						"    return null;",
						"  }",
						"  public java.util.Set getNames() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private java.util.ArrayList m_list;",
						"  private TestBean m_bean;",
						"  private TableViewer m_viewer;",
						"  private WritableValue m_direct;",
						"  private CheckboxTableViewer m_otherViewer;",
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
						"    m_viewer = new TableViewer(m_shell, SWT.BORDER);",
						"    m_otherViewer = new CheckboxTableViewer(m_shell, SWT.BORDER);",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		//
		IObserveInfo shellObserve = widgetsObservables.get(0);
		IObserveInfo shellBackgroundObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		assertFalse(provider.validate(
				shellObserve,
				shellBackgroundObserve,
				viewerObserve,
				viewerInputProperty));
		assertFalse(provider.validate(
				viewerObserve,
				viewerInputProperty,
				shellObserve,
				shellBackgroundObserve));
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		//
		IObserveInfo listObserve = beansObservables.get(1);
		IObserveInfo listInputCollectionPropety =
				listObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		assertTrue(provider.validate(
				listObserve,
				listInputCollectionPropety,
				viewerObserve,
				viewerInputProperty));
		assertTrue(provider.validate(
				viewerObserve,
				viewerInputProperty,
				listObserve,
				listInputCollectionPropety));
		//
		IObserveInfo beanObserve = beansObservables.get(2);
		IObserveInfo beanValueProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		IObserveInfo beanValuesProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		IObserveInfo beanNamesProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertFalse(provider.validate(
				beanObserve,
				beanValueProperty,
				viewerObserve,
				viewerInputProperty));
		assertFalse(provider.validate(
				viewerObserve,
				viewerInputProperty,
				beanObserve,
				beanValueProperty));
		//
		assertTrue(provider.validate(
				beanObserve,
				beanValuesProperty,
				viewerObserve,
				viewerInputProperty));
		assertTrue(provider.validate(
				viewerObserve,
				viewerInputProperty,
				beanObserve,
				beanValuesProperty));
		//
		assertTrue(provider.validate(beanObserve, beanNamesProperty, viewerObserve, viewerInputProperty));
		assertTrue(provider.validate(viewerObserve, viewerInputProperty, beanObserve, beanNamesProperty));
		//
		IObserveInfo directObserve = beansObservables.get(4);
		IObserveInfo directDetailPropety =
				directObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertTrue(provider.validate(
				directObserve,
				directDetailPropety,
				viewerObserve,
				viewerInputProperty));
		assertTrue(provider.validate(
				viewerObserve,
				viewerInputProperty,
				directObserve,
				directDetailPropety));
		//
		IObserveInfo otherTableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1);
		IObserveInfo otherViewerObserve =
				otherTableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo otherViewerMultiSelectionProperty =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		IObserveInfo otherViewerCheckedProperty =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		IObserveInfo otherViewerInputProperty =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(5);
		//
		assertTrue(provider.validate(
				otherViewerObserve,
				otherViewerMultiSelectionProperty,
				viewerObserve,
				viewerInputProperty));
		assertTrue(provider.validate(
				viewerObserve,
				viewerInputProperty,
				otherViewerObserve,
				otherViewerMultiSelectionProperty));
		//
		assertTrue(provider.validate(
				otherViewerObserve,
				otherViewerCheckedProperty,
				viewerObserve,
				viewerInputProperty));
		assertTrue(provider.validate(
				viewerObserve,
				viewerInputProperty,
				otherViewerObserve,
				otherViewerCheckedProperty));
		//
		assertFalse(provider.validate(
				otherViewerObserve,
				otherViewerInputProperty,
				viewerObserve,
				viewerInputProperty));
		assertFalse(provider.validate(
				viewerObserve,
				viewerInputProperty,
				otherViewerObserve,
				otherViewerInputProperty));
	}

	public void test_validate_OnlyList() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerMultiSelectionProperty_OnlyList =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		validateType(
				viewerObserve,
				viewerMultiSelectionProperty_OnlyList,
				true,
				false,
				false,
				true,
				false,
				false,
				true);
	}

	public void test_validate_OnlySet() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerCheckedProperty_OnlySet =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		validateType(
				viewerObserve,
				viewerCheckedProperty_OnlySet,
				false,
				true,
				false,
				false,
				true,
				false,
				true);
	}

	public void test_validate_OnlyValue() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		IObserveInfo shellForegroundProperty =
				shellObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		validateType(shellObserve, shellForegroundProperty, false, false, true, true, true, true, true);
	}

	public void test_validate_List() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanValuesProperty_List =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		validateType(beanObserve, beanValuesProperty_List, true, false, true, true, false, true, true);
	}

	public void test_validate_Set() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanBeanNamesProperty_Set =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		validateType(beanObserve, beanBeanNamesProperty_Set, false, true, true, false, true, true, true);
	}

	public void test_validate_Any() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanValueProperty_Any =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		validateType(beanObserve, beanValueProperty_Any, false, false, true, true, true, true, true);
	}

	public void test_validate_Detail() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerSelectionDetailProperty_Detail =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		validateType(
				viewerObserve,
				viewerSelectionDetailProperty_Detail,
				true,
				true,
				true,
				true,
				true,
				true,
				false);
	}

	private void createValidate() throws Exception {
		createValidate("CheckboxTableViewer");
	}

	private void createValidate(String viewerType) throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ(
						"package test;",
						"public class TestBean {",
						"  public String getValue() {",
						"    return null;",
						"  }",
						"  public java.util.List getValues() {",
						"    return null;",
						"  }",
						"  public java.util.Set getNames() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private TestBean m_bean;",
						"  private WritableValue m_direct;",
						"  private " + viewerType + " m_viewer;",
						"  private " + viewerType + " m_otherViewer;",
						"  private TestBean m_otherBean;",
						"  private java.util.ArrayList m_list;",
						"  private Text m_text;",
						"  private Combo m_combo;",
						"  private java.util.HashSet m_set;",
						"  private DataBindingContext m_context;",
						"  private TestBean m_bean1;",
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
						"    m_viewer = new " + viewerType + "(m_shell, SWT.BORDER);",
						"    m_otherViewer = new " + viewerType + "(m_shell, SWT.BORDER);",
						"    m_text = new Text(m_shell, SWT.BORDER);",
						"    m_combo = new Combo(m_shell, SWT.BORDER);",
						"  }",
				"}"});
		assertNotNull(shell);
	}

	private void validateType(IObserveInfo testObserve,
			IObserveInfo testProperty,
			boolean onlyList,
			boolean onlySet,
			boolean onlyValue,
			boolean list,
			boolean set,
			boolean any,
			boolean detail) throws Exception {
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		//
		IObserveInfo otherBeanObserve = beansObservables.get(5);
		IObserveInfo otherBeanValueProperty_Any =
				otherBeanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		IObserveInfo otherBeanValuesProperty_List =
				otherBeanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		IObserveInfo otherBeanNamesProperty_Set =
				otherBeanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		IObserveInfo directObserve = beansObservables.get(2);
		IObserveInfo directProperty_Detail =
				directObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		IObserveInfo shellObserve = widgetsObservables.get(0);
		IObserveInfo shellBackgroundObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		IObserveInfo otherTableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1);
		IObserveInfo otherViewerObserve =
				otherTableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo otherViewerMultiSelectionProperty_OnlyList =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		IObserveInfo otherViewerCheckedProperty_OnlySet =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		// OnlyList
		assertEquals(onlyList, provider.validate(
				testObserve,
				testProperty,
				otherViewerObserve,
				otherViewerMultiSelectionProperty_OnlyList));
		assertEquals(onlyList, provider.validate(
				otherViewerObserve,
				otherViewerMultiSelectionProperty_OnlyList,
				testObserve,
				testProperty));
		// OnlySet
		assertEquals(onlySet, provider.validate(
				testObserve,
				testProperty,
				otherViewerObserve,
				otherViewerCheckedProperty_OnlySet));
		assertEquals(onlySet, provider.validate(
				otherViewerObserve,
				otherViewerCheckedProperty_OnlySet,
				testObserve,
				testProperty));
		// OnlyValue
		assertEquals(
				onlyValue,
				provider.validate(testObserve, testProperty, shellObserve, shellBackgroundObserve));
		assertEquals(
				onlyValue,
				provider.validate(shellObserve, shellBackgroundObserve, testObserve, testProperty));
		// List
		assertEquals(
				list,
				provider.validate(testObserve, testProperty, otherBeanObserve, otherBeanValuesProperty_List));
		assertEquals(
				list,
				provider.validate(otherBeanObserve, otherBeanValuesProperty_List, testObserve, testProperty));
		// Set
		assertEquals(
				set,
				provider.validate(testObserve, testProperty, otherBeanObserve, otherBeanNamesProperty_Set));
		assertEquals(
				set,
				provider.validate(otherBeanObserve, otherBeanNamesProperty_Set, testObserve, testProperty));
		// Any
		assertEquals(
				any,
				provider.validate(testObserve, testProperty, otherBeanObserve, otherBeanValueProperty_Any));
		assertEquals(
				any,
				provider.validate(otherBeanObserve, otherBeanValueProperty_Any, testObserve, testProperty));
		// Detail
		assertEquals(
				detail,
				provider.validate(testObserve, testProperty, directObserve, directProperty_Detail));
		assertEquals(
				detail,
				provider.validate(directObserve, directProperty_Detail, testObserve, testProperty));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Create tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_createBinding_OnlyList_OnlyList() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerMultiSelectionProperty_OnlyList =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		IObserveInfo comboObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(3);
		IObserveInfo comboItemsProperty_OnlyList =
				comboObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(6);
		//
		assertCreateBinding(
				viewerObserve,
				viewerMultiSelectionProperty_OnlyList,
				comboObserve,
				comboItemsProperty_OnlyList,
				ListBindingInfo.class,
				MultiSelectionObservableInfo.class,
				ItemsSwtObservableInfo.class,
				UpdateListStrategyInfo.class);
	}

	public void test_createBinding_OnlyList_List() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerMultiSelectionProperty_OnlyList =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanValuesProperty_List =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		assertCreateBinding(
				viewerObserve,
				viewerMultiSelectionProperty_OnlyList,
				beanObserve,
				beanValuesProperty_List,
				ListBindingInfo.class,
				MultiSelectionObservableInfo.class,
				ListBeanObservableInfo.class,
				UpdateListStrategyInfo.class);
	}

	public void test_createBinding_List_List() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanValuesProperty_List =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		IObserveInfo bean1Observe = beansObservables.get(11);
		IObserveInfo bean1ValuesProperty_List =
				bean1Observe.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		assertCreateBinding(
				bean1Observe,
				bean1ValuesProperty_List,
				beanObserve,
				beanValuesProperty_List,
				ListBindingInfo.class,
				ListBeanObservableInfo.class,
				ListBeanObservableInfo.class,
				UpdateListStrategyInfo.class);
	}

	public void test_createBinding_OnlyList_Detail() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerMultiSelectionProperty_OnlyList =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo directObserve = beansObservables.get(2);
		IObserveInfo directProperty_Detail =
				directObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				viewerObserve,
				viewerMultiSelectionProperty_OnlyList,
				directObserve,
				directProperty_Detail,
				ListBindingInfo.class,
				MultiSelectionObservableInfo.class,
				DetailListBeanObservableInfo.class,
				UpdateListStrategyInfo.class);
		//
		IObserveInfo otherTableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1);
		IObserveInfo otherViewerObserve =
				otherTableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo otherViewerPartSelectionProperty_Detail =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				viewerObserve,
				viewerMultiSelectionProperty_OnlyList,
				otherViewerObserve,
				otherViewerPartSelectionProperty_Detail,
				ListBindingInfo.class,
				MultiSelectionObservableInfo.class,
				DetailListBeanObservableInfo.class,
				UpdateListStrategyInfo.class);
	}

	public void test_createBinding_OnlySet_OnlySet() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerCheckedProperty_OnlySet =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		IObserveInfo otherTableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1);
		IObserveInfo otherViewerObserve =
				otherTableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo otherViewerCheckedProperty_OnlySet =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		assertCreateBinding(
				viewerObserve,
				viewerCheckedProperty_OnlySet,
				otherViewerObserve,
				otherViewerCheckedProperty_OnlySet,
				SetBindingInfo.class,
				CheckedElementsObservableInfo.class,
				CheckedElementsObservableInfo.class,
				UpdateSetStrategyInfo.class);
	}

	public void test_createBinding_OnlySet_Set() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerCheckedProperty_OnlySet =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanNamesProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				viewerObserve,
				viewerCheckedProperty_OnlySet,
				beanObserve,
				beanNamesProperty,
				SetBindingInfo.class,
				CheckedElementsObservableInfo.class,
				SetBeanObservableInfo.class,
				UpdateSetStrategyInfo.class);
	}

	public void test_createBinding_Set_Set() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		//
		IObserveInfo bean1Observe = beansObservables.get(11);
		IObserveInfo bean1NamesProperty =
				bean1Observe.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanNamesProperty =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				bean1Observe,
				bean1NamesProperty,
				beanObserve,
				beanNamesProperty,
				SetBindingInfo.class,
				SetBeanObservableInfo.class,
				SetBeanObservableInfo.class,
				UpdateSetStrategyInfo.class);
	}

	public void test_createBinding_OnlySet_Detail() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerCheckedProperty_OnlySet =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo directObserve = beansObservables.get(2);
		IObserveInfo directProperty_Detail =
				directObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				viewerObserve,
				viewerCheckedProperty_OnlySet,
				directObserve,
				directProperty_Detail,
				SetBindingInfo.class,
				CheckedElementsObservableInfo.class,
				DetailSetBeanObservableInfo.class,
				UpdateSetStrategyInfo.class);
		//
		IObserveInfo otherTableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1);
		IObserveInfo otherViewerObserve =
				otherTableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo otherViewerPartSelectionProperty_Detail =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				viewerObserve,
				viewerCheckedProperty_OnlySet,
				otherViewerObserve,
				otherViewerPartSelectionProperty_Detail,
				SetBindingInfo.class,
				CheckedElementsObservableInfo.class,
				DetailSetBeanObservableInfo.class,
				UpdateSetStrategyInfo.class);
	}

	public void test_createBinding_OnlyValue_OnlyValue() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		IObserveInfo shellFontProperty =
				shellObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		IObserveInfo textObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(2);
		IObserveInfo textTextProperty =
				textObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(10);
		//
		assertCreateBinding(
				textObserve,
				textTextProperty,
				shellObserve,
				shellFontProperty,
				ValueBindingInfo.class,
				TextSwtObservableInfo.class,
				SwtObservableInfo.class,
				UpdateValueStrategyInfo.class);
	}

	public void test_createBinding_OnlyValue_Any() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanValueProperty_Any =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerSelectionProperty =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		assertCreateBinding(
				viewerObserve,
				viewerSelectionProperty,
				beanObserve,
				beanValueProperty_Any,
				ValueBindingInfo.class,
				SingleSelectionObservableInfo.class,
				ValueBeanObservableInfo.class,
				UpdateValueStrategyInfo.class);
	}

	public void test_createBinding_Any_Any() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		//
		IObserveInfo bean1Observe = beansObservables.get(11);
		IObserveInfo bean1ValueProperty_Any =
				bean1Observe.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanValueProperty_Any =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		assertCreateBinding(
				bean1Observe,
				bean1ValueProperty_Any,
				beanObserve,
				beanValueProperty_Any,
				ValueBindingInfo.class,
				ValueBeanObservableInfo.class,
				ValueBeanObservableInfo.class,
				UpdateValueStrategyInfo.class);
	}

	public void test_createBinding_OnlyValue_Detail() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		//
		IObserveInfo shellObserve = widgetsObservables.get(0);
		IObserveInfo shellTextProperty =
				shellObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(8);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerSelectionDetailProperty_Detail =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				shellObserve,
				shellTextProperty,
				viewerObserve,
				viewerSelectionDetailProperty_Detail,
				ValueBindingInfo.class,
				SwtObservableInfo.class,
				DetailValueBeanObservableInfo.class,
				UpdateValueStrategyInfo.class);
		//
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo directObserve = beansObservables.get(2);
		IObserveInfo directProperty_Detail =
				directObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				shellObserve,
				shellTextProperty,
				directObserve,
				directProperty_Detail,
				ValueBindingInfo.class,
				SwtObservableInfo.class,
				DetailValueBeanObservableInfo.class,
				UpdateValueStrategyInfo.class);
	}

	public void test_createBinding_List_Detail() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanValuesProperty_List =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		IObserveInfo directObserve = beansObservables.get(2);
		IObserveInfo directDetailProperty_Detail =
				directObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				beanObserve,
				beanValuesProperty_List,
				directObserve,
				directDetailProperty_Detail,
				ListBindingInfo.class,
				ListBeanObservableInfo.class,
				DetailListBeanObservableInfo.class,
				UpdateListStrategyInfo.class);
	}

	public void test_createBinding_Set_Detail() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanNamesProperty_Set =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		IObserveInfo directObserve = beansObservables.get(2);
		IObserveInfo directDetailProperty_Detail =
				directObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateBinding(
				beanObserve,
				beanNamesProperty_Set,
				directObserve,
				directDetailProperty_Detail,
				SetBindingInfo.class,
				SetBeanObservableInfo.class,
				DetailSetBeanObservableInfo.class,
				UpdateSetStrategyInfo.class);
	}

	private void assertCreateBinding(IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty,
			Class<?> bindingClass,
			Class<?> targetObservable,
			Class<?> modelObservable,
			Class<?> strategy) throws Exception {
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IBindingInfo binding0 = provider.createBinding(target, targetProperty, model, modelProperty);
		assertNotNull(binding0);
		assertInstanceOf(bindingClass, binding0);
		assertBinding(binding0, target, targetProperty, model, modelProperty);
		//
		assertBinding((BindingInfo) binding0, targetObservable, strategy, modelObservable, strategy);
		//
		IBindingInfo binding1 = provider.createBinding(model, modelProperty, target, targetProperty);
		assertNotNull(binding1);
		assertInstanceOf(bindingClass, binding1);
		assertBinding(binding1, model, modelProperty, target, targetProperty);
		//
		assertBinding((BindingInfo) binding1, modelObservable, strategy, targetObservable, strategy);
	}

	private static void assertBinding(IBindingInfo binding,
			IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty) throws Exception {
		assertSame(binding.getTarget(), target);
		assertSame(binding.getTargetProperty(), targetProperty);
		assertSame(binding.getModel(), model);
		assertSame(binding.getModelProperty(), modelProperty);
	}

	private static void assertBinding(BindingInfo binding,
			Class<?> targetObservable,
			Class<?> targetStrategy,
			Class<?> modelObservable,
			Class<?> modelStrategy) {
		assertInstanceOf(targetObservable, binding.getTargetObservable());
		assertInstanceOf(targetStrategy, binding.getTargetStrategy());
		assertInstanceOf(modelObservable, binding.getModelObservable());
		assertInstanceOf(modelStrategy, binding.getModelStrategy());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Viewer validate
	//
	////////////////////////////////////////////////////////////////////////////
	private void assertCreateViewerBinding(IObserveInfo viewerObserve,
			IObserveInfo viewerInputProperty,
			IObserveInfo inputObserve,
			IObserveInfo inputObserveProperty,
			Class<?> inputObserveClass) throws Exception {
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		IBindingInfo ibinding0 =
				provider.createBinding(
						viewerObserve,
						viewerInputProperty,
						inputObserve,
						inputObserveProperty);
		assertInstanceOf(ViewerInputBindingInfo.class, ibinding0);
		assertBinding(ibinding0, viewerObserve, viewerInputProperty, inputObserve, inputObserveProperty);
		//
		ViewerInputBindingInfo binding0 = (ViewerInputBindingInfo) ibinding0;
		assertNull(binding0.getElementType());
		assertSame(viewerObserve, binding0.getViewer());
		assertInstanceOf(inputObserveClass, binding0.getInputObservable());
		assertNotNull(binding0.getLabelProvider());
		//
		IBindingInfo ibinding1 =
				provider.createBinding(
						inputObserve,
						inputObserveProperty,
						viewerObserve,
						viewerInputProperty);
		assertInstanceOf(ViewerInputBindingInfo.class, ibinding1);
		assertBinding(ibinding1, viewerObserve, viewerInputProperty, inputObserve, inputObserveProperty);
		//
		ViewerInputBindingInfo binding1 = (ViewerInputBindingInfo) ibinding1;
		assertNull(binding1.getElementType());
		assertSame(viewerObserve, binding1.getViewer());
		assertInstanceOf(inputObserveClass, binding1.getInputObservable());
		assertNotNull(binding1.getLabelProvider());
	}

	public void test_createBinding_Viewer_InputCollection() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty_Input =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(5);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo listObserve = beansObservables.get(6);
		IObserveInfo listProperty_InputCollection =
				listObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		assertCreateViewerBinding(
				viewerObserve,
				viewerInputProperty_Input,
				listObserve,
				listProperty_InputCollection,
				WritableListBeanObservableInfo.class);
		//
		IObserveInfo setObserve = beansObservables.get(9);
		IObserveInfo setProperty_InputCollection =
				setObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		assertCreateViewerBinding(
				viewerObserve,
				viewerInputProperty_Input,
				setObserve,
				setProperty_InputCollection,
				WritableSetBeanObservableInfo.class);
	}

	public void test_createBinding_Viewer_OnlyList() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty_Input =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(5);
		//
		IObserveInfo otherTableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1);
		IObserveInfo otherViewerObserve =
				otherTableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo otherViewerMultiSelectionProperty_OnlyList =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		assertCreateViewerBinding(
				viewerObserve,
				viewerInputProperty_Input,
				otherViewerObserve,
				otherViewerMultiSelectionProperty_OnlyList,
				MultiSelectionObservableInfo.class);
	}

	public void test_createBinding_Viewer_OnlySet() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty_Input =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(5);
		//
		IObserveInfo otherTableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(1);
		IObserveInfo otherViewerObserve =
				otherTableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo otherViewerCheckedProperty_OnlySet =
				otherViewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		assertCreateViewerBinding(
				viewerObserve,
				viewerInputProperty_Input,
				otherViewerObserve,
				otherViewerCheckedProperty_OnlySet,
				CheckedElementsObservableInfo.class);
	}

	public void test_createBinding_Viewer_List() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty_Input =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(5);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanValuesProperty_List =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		assertCreateViewerBinding(
				viewerObserve,
				viewerInputProperty_Input,
				beanObserve,
				beanValuesProperty_List,
				ListBeanObservableInfo.class);
	}

	public void test_createBinding_Viewer_Set() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty_Input =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(5);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanNamesProperty_Set =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateViewerBinding(
				viewerObserve,
				viewerInputProperty_Input,
				beanObserve,
				beanNamesProperty_Set,
				SetBeanObservableInfo.class);
	}

	public void test_createBinding_Viewer_Detail() throws Exception {
		createValidate();
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo tableObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				tableObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty_Input =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(5);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo directObserve = beansObservables.get(2);
		IObserveInfo directDetailProperty_Detail =
				directObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertCreateViewerBinding(
				viewerObserve,
				viewerInputProperty_Input,
				directObserve,
				directDetailProperty_Detail,
				DetailListBeanObservableInfo.class);
	}

	public void test_createBinding_TreeViewer() throws Exception {
		createValidate("TreeViewer");
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> widgetsObservables =
				provider.getContainer(ObserveType.WIDGETS).getObservables();
		IObserveInfo shellObserve = widgetsObservables.get(0);
		//
		IObserveInfo treeObserve =
				shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerObserve =
				treeObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		IObserveInfo viewerInputProperty_Input =
				viewerObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(4);
		//
		List<IObserveInfo> beansObservables = provider.getContainer(ObserveType.BEANS).getObservables();
		IObserveInfo beanObserve = beansObservables.get(1);
		IObserveInfo beanNamesProperty_Set =
				beanObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		IBindingInfo ibinding0 =
				provider.createBinding(
						viewerObserve,
						viewerInputProperty_Input,
						beanObserve,
						beanNamesProperty_Set);
		assertInstanceOf(TreeViewerInputBindingInfo.class, ibinding0);
		assertBinding(
				ibinding0,
				viewerObserve,
				viewerInputProperty_Input,
				beanObserve,
				beanNamesProperty_Set);
		//
		TreeViewerInputBindingInfo binding0 = (TreeViewerInputBindingInfo) ibinding0;
		assertNull(binding0.getElementType());
		assertSame(viewerObserve, binding0.getViewer());
		assertInstanceOf(SetBeanObservableInfo.class, binding0.getInputObservable());
		assertNotNull(binding0.getContentProvider());
		assertNotNull(binding0.getLabelProvider());
		//
		IBindingInfo ibinding1 =
				provider.createBinding(
						beanObserve,
						beanNamesProperty_Set,
						viewerObserve,
						viewerInputProperty_Input);
		assertInstanceOf(TreeViewerInputBindingInfo.class, ibinding1);
		assertBinding(
				ibinding1,
				viewerObserve,
				viewerInputProperty_Input,
				beanObserve,
				beanNamesProperty_Set);
		//
		TreeViewerInputBindingInfo binding1 = (TreeViewerInputBindingInfo) ibinding1;
		assertNull(binding1.getElementType());
		assertSame(viewerObserve, binding1.getViewer());
		assertInstanceOf(SetBeanObservableInfo.class, binding1.getInputObservable());
		assertNotNull(binding1.getContentProvider());
		assertNotNull(binding1.getLabelProvider());
	}

	public void test_property() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super (parent, style);",
						"  }",
						"}");
		composite.refresh();
		BindingsProperty property = (BindingsProperty) composite.getPropertyByTitle("bindings");
		assertNotNull(property);
	}

	public void test_property_disabled() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  public MyComposite(Composite parent, int style) {",
						"    super (parent, style);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyComposite.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='databinding.disable'>true</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		CompositeInfo composite =
				parseComposite(
						"public class Test extends MyComposite {",
						"  public Test(Composite parent, int style) {",
						"    super (parent, style);",
						"  }",
						"}");
		composite.refresh();
		assertNull(composite.getPropertyByTitle("bindings"));
	}
}