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
package org.eclipse.wb.tests.designer.databinding.rcp.model.context;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.SetBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateSetStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;
import org.eclipse.wb.tests.designer.databinding.rcp.model.AbstractBindingTest;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class BindSetTest extends AbstractBindingTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_bindSet() throws Exception {
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
                "    IObservableSet observeSet = BeansObservables.observeSet(Realm.getDefault(), m_bean, \"names\");",
                "    IObservableSet observeWidget = ViewersObservables.observeCheckedElements(m_viewer, String.class);",
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
    assertInstanceOf(SetBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertNotNull(binding.getTarget());
    assertNotNull(binding.getTargetProperty());
    assertNotNull(binding.getTargetObservable());
    assertSame(binding.getTarget(), binding.getTargetObservable().getBindableObject());
    assertSame(binding.getTargetProperty(), binding.getTargetObservable().getBindableProperty());
    assertInstanceOf(CheckedElementsObservableInfo.class, binding.getTargetObservable());
    assertEquals("observeWidget", binding.getTargetObservable().getVariableIdentifier());
    //
    assertNotNull(binding.getModel());
    assertNotNull(binding.getModelProperty());
    assertNotNull(binding.getModelObservable());
    assertSame(binding.getModel(), binding.getModelObservable().getBindableObject());
    assertSame(binding.getModelProperty(), binding.getModelObservable().getBindableProperty());
    assertInstanceOf(SetBeanObservableInfo.class, binding.getModelObservable());
    assertEquals("observeSet", binding.getModelObservable().getVariableIdentifier());
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateSetStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateSetStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
  }

  public void test_strategy_constructors_1() throws Exception {
    strategy_constructors(
        "    bindingContext.bindSet(observeWidget, observeSet, new UpdateSetStrategy(), new UpdateSetStrategy(UpdateSetStrategy.POLICY_NEVER));",
        UpdateSetStrategyInfo.Value.POLICY_NEVER,
        "POLICY_NEVER|POLICY_NEVER");
  }

  public void test_strategy_constructors_2() throws Exception {
    strategy_constructors(
        "    bindingContext.bindSet(observeWidget, observeSet, new UpdateSetStrategy(), new UpdateSetStrategy(UpdateSetStrategy.POLICY_ON_REQUEST));",
        UpdateSetStrategyInfo.Value.POLICY_ON_REQUEST,
        "POLICY_ON_REQUEST|POLICY_ON_REQUEST");
  }

  public void test_strategy_constructors_3() throws Exception {
    strategy_constructors(
        "    bindingContext.bindSet(observeWidget, observeSet, new UpdateSetStrategy(), new UpdateSetStrategy(UpdateSetStrategy.POLICY_UPDATE));",
        UpdateSetStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
  }

  private void strategy_constructors(String line, Object value, String presentation)
      throws Exception {
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
                "    IObservableSet observeSet = BeansObservables.observeSet(Realm.getDefault(), m_bean, \"names\");",
                "    IObservableSet observeWidget = ViewersObservables.observeCheckedElements(m_viewer, String.class);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                line,
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
    assertInstanceOf(SetBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.DefaultConstructor,
        UpdateSetStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.IntConstructor,
        value,
        presentation);
  }

  public void test_strategy_variable() throws Exception {
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
                "    IObservableSet observeSet = BeansObservables.observeSet(Realm.getDefault(), m_bean, \"names\");",
                "    IObservableSet observeWidget = ViewersObservables.observeCheckedElements(m_viewer, String.class);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    UpdateSetStrategy strategy0 = new UpdateSetStrategy();",
                "    UpdateSetStrategy strategy1 = new UpdateSetStrategy(UpdateSetStrategy.POLICY_NEVER);",
                "    bindingContext.bindSet(observeWidget, observeSet, strategy0, strategy1);",
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
    assertInstanceOf(SetBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        "strategy0",
        UpdateStrategyInfo.StrategyType.DefaultConstructor,
        UpdateSetStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        "strategy1",
        UpdateStrategyInfo.StrategyType.IntConstructor,
        UpdateSetStrategyInfo.Value.POLICY_NEVER,
        "POLICY_NEVER|POLICY_NEVER");
  }

  public void test_strategy_extendet() throws Exception {
    createModelCompilationUnit("test", "TestStrategy.java", DatabindingTestUtils.getTestSource(
        "public class TestStrategy extends UpdateSetStrategy {",
        "  public TestStrategy() {",
        "  }",
        "}"));
    waitForAutoBuild();
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
                "    IObservableSet observeSet = BeansObservables.observeSet(Realm.getDefault(), m_bean, \"names\");",
                "    IObservableSet observeWidget = ViewersObservables.observeCheckedElements(m_viewer, String.class);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    bindingContext.bindSet(observeWidget, observeSet, null, new test.TestStrategy());",
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
    assertInstanceOf(SetBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateSetStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.ExtendetClass,
        "test.TestStrategy",
        "test.TestStrategy|test.TestStrategy");
  }

  public void test_strategy_converter_1() throws Exception {
    strategy_converter(
        "    strategy.setConverter(new TestConverter());",
        "    //",
        "null|test.TestConverter|TestConverter");
  }

  public void test_strategy_converter_2() throws Exception {
    strategy_converter(
        "    TestConverter converter = new TestConverter();",
        "    strategy.setConverter(converter);",
        "converter|test.TestConverter|TestConverter");
  }

  private void strategy_converter(String line0, String line1, String testString) throws Exception {
    createModelCompilationUnit("test", "TestConverter.java", DatabindingTestUtils.getTestSource(
        "public class TestConverter extends Converter {",
        "  public TestConverter() {",
        "    super(null, null);",
        "  }",
        "  public Object convert(Object fromObject) {",
        "    return null;",
        "  }",
        "}"));
    waitForAutoBuild();
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
                "    IObservableSet observeSet = BeansObservables.observeSet(Realm.getDefault(), m_bean, \"names\");",
                "    IObservableSet observeWidget = ViewersObservables.observeCheckedElements(m_viewer, String.class);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    UpdateSetStrategy strategy = new UpdateSetStrategy();",
                line0,
                line1,
                "    bindingContext.bindSet(observeWidget, observeSet, null, strategy);",
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
    assertInstanceOf(SetBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateSetStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        "strategy",
        UpdateStrategyInfo.StrategyType.DefaultConstructor,
        UpdateSetStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE",
        testString);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void assertStrategy(Object objectStrategy,
      String variable,
      Object type,
      Object value,
      String presentation) throws Exception {
    assertStrategy(objectStrategy, variable, type, value, presentation, null);
  }

  private void assertStrategy(Object objectStrategy,
      String variable,
      Object type,
      Object value,
      String presentation,
      String converter) throws Exception {
    assertNotNull(objectStrategy);
    assertInstanceOf(UpdateSetStrategyInfo.class, objectStrategy);
    //
    UpdateSetStrategyInfo strategy = (UpdateSetStrategyInfo) objectStrategy;
    //
    if (variable == null) {
      assertNull(strategy.getVariableIdentifier());
    } else {
      assertEquals(variable, strategy.getVariableIdentifier());
    }
    //
    assertEquals(type, strategy.getStrategyType());
    assertEquals(value, strategy.getStrategyValue());
    assertEquals(presentation, strategy.getStringValue() + "|" + strategy.getPresentationText());
    //
    if (converter == null) {
      assertNull(strategy.getConverter());
    } else {
      assertEquals(converter, strategy.getConverter().getVariableIdentifier()
          + "|"
          + strategy.getConverter().getClassName()
          + "|"
          + strategy.getConverter().getPresentationText());
    }
  }
}