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
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ValueBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateValueStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.ValidatorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SwtObservableInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;
import org.eclipse.wb.tests.designer.databinding.rcp.model.AbstractBindingTest;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class BindValueTest extends AbstractBindingTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_bindValue() throws Exception {
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
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
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
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
    assertInstanceOf(ValueBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertNull(binding.getVariableIdentifier());
    assertNotNull(binding.getTarget());
    assertNotNull(binding.getTargetProperty());
    assertNotNull(binding.getTargetObservable());
    assertSame(binding.getTarget(), binding.getTargetObservable().getBindableObject());
    assertSame(binding.getTargetProperty(), binding.getTargetObservable().getBindableProperty());
    assertInstanceOf(SwtObservableInfo.class, binding.getTargetObservable());
    assertEquals("observeWidget", binding.getTargetObservable().getVariableIdentifier());
    //
    assertNotNull(binding.getModel());
    assertNotNull(binding.getModelProperty());
    assertNotNull(binding.getModelObservable());
    assertSame(binding.getModel(), binding.getModelObservable().getBindableObject());
    assertSame(binding.getModelProperty(), binding.getModelObservable().getBindableProperty());
    assertInstanceOf(ValueBeanObservableInfo.class, binding.getModelObservable());
    assertEquals("observeValue", binding.getModelObservable().getVariableIdentifier());
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
  }

  public void test_bindValue_variable_1() throws Exception {
    bindValue_variable(
        "  //",
        "    Binding binding = bindingContext.bindValue(observeWidget, observeValue, null, null);",
        "binding");
  }

  public void test_bindValue_variable_2() throws Exception {
    bindValue_variable(
        "  Binding m_binding;",
        "    m_binding = bindingContext.bindValue(observeWidget, observeValue, null, null);",
        "m_binding");
  }

  private void bindValue_variable(String line0, String line1, String testString) throws Exception {
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
                "import org.eclipse.core.databinding.Binding;",
                "public class Test {",
                "  protected Shell m_shell;",
                "  private DataBindingContext m_bindingContext;",
                line0,
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
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                line1,
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
    assertInstanceOf(ValueBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertNotNull(binding.getVariableIdentifier());
    assertEquals(testString, binding.getVariableIdentifier());
  }

  public void test_strategy_constructors_1() throws Exception {
    strategy_constructors(
        "    bindingContext.bindValue(observeWidget, observeValue, new UpdateValueStrategy(), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER));",
        UpdateValueStrategyInfo.Value.POLICY_NEVER,
        "POLICY_NEVER|POLICY_NEVER");
  }

  public void test_strategy_constructors_2() throws Exception {
    strategy_constructors(
        "    bindingContext.bindValue(observeWidget, observeValue, new UpdateValueStrategy(), new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));",
        UpdateValueStrategyInfo.Value.POLICY_ON_REQUEST,
        "POLICY_ON_REQUEST|POLICY_ON_REQUEST");
  }

  public void test_strategy_constructors_3() throws Exception {
    strategy_constructors(
        "    bindingContext.bindValue(observeWidget, observeValue, new UpdateValueStrategy(), new UpdateValueStrategy(UpdateValueStrategy.POLICY_CONVERT));",
        UpdateValueStrategyInfo.Value.POLICY_CONVERT,
        "POLICY_CONVERT|POLICY_CONVERT");
  }

  public void test_strategy_constructors_4() throws Exception {
    strategy_constructors(
        "    bindingContext.bindValue(observeWidget, observeValue, new UpdateValueStrategy(), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));",
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
  }

  private void strategy_constructors(String line, Object value, String presentation)
      throws Exception {
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
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
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
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
    assertInstanceOf(ValueBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.DefaultConstructor,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
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
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
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
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    UpdateValueStrategy strategy0 = new UpdateValueStrategy();",
                "    UpdateValueStrategy strategy1 = new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER);",
                "    bindingContext.bindValue(observeWidget, observeValue, strategy0, strategy1);",
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
    assertInstanceOf(ValueBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        "strategy0",
        UpdateStrategyInfo.StrategyType.DefaultConstructor,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        "strategy1",
        UpdateStrategyInfo.StrategyType.IntConstructor,
        UpdateValueStrategyInfo.Value.POLICY_NEVER,
        "POLICY_NEVER|POLICY_NEVER");
  }

  public void test_strategy_policy_variable() throws Exception {
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
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
                "  int m_policy = UpdateValueStrategy.POLICY_NEVER;",
                "  private DataBindingContext initDataBindings() {",
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    int policy = UpdateValueStrategy.POLICY_UPDATE;",
                "    UpdateValueStrategy strategy0 = new UpdateValueStrategy(policy);",
                "    UpdateValueStrategy strategy1 = new UpdateValueStrategy(m_policy);",
                "    bindingContext.bindValue(observeWidget, observeValue, strategy0, strategy1);",
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
    assertInstanceOf(ValueBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        "strategy0",
        UpdateStrategyInfo.StrategyType.IntConstructor,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        "strategy1",
        UpdateStrategyInfo.StrategyType.IntConstructor,
        UpdateValueStrategyInfo.Value.POLICY_NEVER,
        "POLICY_NEVER|POLICY_NEVER");
  }

  public void test_strategy_z_extendet() throws Exception {
    createModelCompilationUnit("test", "TestStrategy.java", DatabindingTestUtils.getTestSource(
        "public class TestStrategy extends UpdateValueStrategy {",
        "  public TestStrategy() {",
        "  }",
        "}"));
    waitForAutoBuild();
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
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
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    bindingContext.bindValue(observeWidget, observeValue, null, new test.TestStrategy());",
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
    assertInstanceOf(ValueBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
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
    CompositeInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
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
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    UpdateValueStrategy strategy = new UpdateValueStrategy();",
                line0,
                line1,
                "    bindingContext.bindValue(observeWidget, observeValue, null, strategy);",
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
    assertInstanceOf(ValueBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        "strategy",
        UpdateStrategyInfo.StrategyType.DefaultConstructor,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE",
        testString,
        null,
        null,
        null);
  }

  public void test_strategy_AfterConvertValidator_1() throws Exception {
    strategy_validator(
        "    strategy.setAfterConvertValidator(new TestValidator());",
        "    //",
        "null|test.TestValidator|TestValidator",
        null,
        null);
  }

  public void test_strategy_AfterConvertValidator_2() throws Exception {
    strategy_validator(
        "    TestValidator validator = new TestValidator();",
        "    strategy.setAfterConvertValidator(validator);",
        "validator|test.TestValidator|TestValidator",
        null,
        null);
  }

  public void test_strategy_AfterGetValidator_1() throws Exception {
    strategy_validator(
        "    strategy.setAfterGetValidator(new TestValidator());",
        "    //",
        null,
        "null|test.TestValidator|TestValidator",
        null);
  }

  public void test_strategy_AfterGetValidator_2() throws Exception {
    strategy_validator(
        "    TestValidator validator = new TestValidator();",
        "    strategy.setAfterGetValidator(validator);",
        null,
        "validator|test.TestValidator|TestValidator",
        null);
  }

  public void test_strategy_BeforeSetValidator_1() throws Exception {
    strategy_validator(
        "    strategy.setBeforeSetValidator(new TestValidator());",
        "    //",
        null,
        null,
        "null|test.TestValidator|TestValidator");
  }

  public void test_strategy_BeforeSetValidator_2() throws Exception {
    strategy_validator(
        "    TestValidator validator = new TestValidator();",
        "    strategy.setBeforeSetValidator(validator);",
        null,
        null,
        "validator|test.TestValidator|TestValidator");
  }

  private void strategy_validator(String line0,
      String line1,
      String testString0,
      String testString1,
      String testString2) throws Exception {
    createModelCompilationUnit("test", "TestValidator.java", DatabindingTestUtils.getTestSource(
        "public class TestValidator implements IValidator {",
        "  public org.eclipse.core.runtime.IStatus validate(Object value) {",
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
                "    IObservableValue observeValue = BeansObservables.observeValue(getClass(), \"name\");",
                "    IObservableValue observeWidget = SWTObservables.observeText(m_shell);",
                "    DataBindingContext bindingContext = new DataBindingContext();",
                "    UpdateValueStrategy strategy = new UpdateValueStrategy();",
                line0,
                line1,
                "    bindingContext.bindValue(observeWidget, observeValue, null, strategy);",
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
    assertInstanceOf(ValueBindingInfo.class, bindings.get(0));
    BindingInfo binding = (BindingInfo) bindings.get(0);
    //
    assertStrategy(
        binding.getTargetStrategy(),
        null,
        UpdateStrategyInfo.StrategyType.Null,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE");
    //
    assertStrategy(
        binding.getModelStrategy(),
        "strategy",
        UpdateStrategyInfo.StrategyType.DefaultConstructor,
        UpdateValueStrategyInfo.Value.POLICY_UPDATE,
        "POLICY_UPDATE|POLICY_UPDATE",
        null,
        testString0,
        testString1,
        testString2);
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
    assertStrategy(objectStrategy, variable, type, value, presentation, null, null, null, null);
  }

  private void assertStrategy(Object objectStrategy,
      String variable,
      Object type,
      Object value,
      String presentation,
      String converter,
      String validator0,
      String validator1,
      String validator2) throws Exception {
    assertNotNull(objectStrategy);
    assertInstanceOf(UpdateValueStrategyInfo.class, objectStrategy);
    //
    UpdateValueStrategyInfo strategy = (UpdateValueStrategyInfo) objectStrategy;
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
    //
    if (validator0 == null) {
      assertNull(strategy.getAfterConvertValidator());
    } else {
      assertEquals(validator0, strategy.getAfterConvertValidator().getVariableIdentifier()
          + "|"
          + strategy.getAfterConvertValidator().getClassName()
          + "|"
          + strategy.getAfterConvertValidator().getPresentationText());
      //
      ValidatorInfo validator = strategy.getValidator("setAfterConvertValidator");
      assertNotNull(validator);
      assertEquals(validator0, validator.getVariableIdentifier()
          + "|"
          + validator.getClassName()
          + "|"
          + validator.getPresentationText());
    }
    //
    if (validator1 == null) {
      assertNull(strategy.getAfterGetValidator());
    } else {
      assertEquals(validator1, strategy.getAfterGetValidator().getVariableIdentifier()
          + "|"
          + strategy.getAfterGetValidator().getClassName()
          + "|"
          + strategy.getAfterGetValidator().getPresentationText());
      //
      ValidatorInfo validator = strategy.getValidator("setAfterGetValidator");
      assertNotNull(validator);
      assertEquals(validator1, validator.getVariableIdentifier()
          + "|"
          + validator.getClassName()
          + "|"
          + validator.getPresentationText());
    }
    //
    if (validator2 == null) {
      assertNull(strategy.getBeforeSetValidator());
    } else {
      assertEquals(validator2, strategy.getBeforeSetValidator().getVariableIdentifier()
          + "|"
          + strategy.getBeforeSetValidator().getClassName()
          + "|"
          + strategy.getBeforeSetValidator().getPresentationText());
      //
      ValidatorInfo validator = strategy.getValidator("setBeforeSetValidator");
      assertNotNull(validator);
      assertEquals(validator2, validator.getVariableIdentifier()
          + "|"
          + validator.getClassName()
          + "|"
          + validator.getPresentationText());
    }
  }
}