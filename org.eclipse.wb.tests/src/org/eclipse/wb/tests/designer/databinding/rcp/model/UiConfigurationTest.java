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
package org.eclipse.wb.tests.designer.databinding.rcp.model;

import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.BindingContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.SeparatorUiContentProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.CheckedElementsUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ConverterUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ObservableDetailUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SwtDelayUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SwtTextEventsUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.UpdateStrategyPropertiesUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.UpdateStrategyUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ValidatorUiContentProvider;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author lobas_av
 *
 */
@SuppressWarnings("unchecked")
public class UiConfigurationTest extends AbstractBindingTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ObservableInfo() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"import org.eclipse.core.databinding.Binding;",
								"public class Test {",
								"  protected Shell m_shell;",
								"  private Text m_text;",
								"  private CheckboxTableViewer m_viewer;",
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
								"    m_text = new Text(m_shell, SWT.SINGLE);",
								"    m_viewer = new CheckboxTableViewer(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableValue observeValue = BeanProperties.value(\"name\").observe(getClass());",
								"    IObservableValue observeDetailValue = BeanProperties.value(\"empty\", boolean.class).observeDetail(observeValue);",
								"    IObservableValue observeWidget = WidgetProperties.font().observe(m_shell);",
								"    IObservableValue observeText = WidgetProperties.text(SWT.Modify).observe(m_text);",
								"    IObservableSet observeSet = BeanProperties.set(\"name\").observe(getClass());",
								"    IObservableSet observeViewerSet = ViewerProperties.checkedElements(String.class).observe((Viewer)m_viewer);",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindValue(observeWidget, observeValue, null, null);",
								"    bindingContext.bindValue(observeText, observeDetailValue, null, null);",
								"    bindingContext.bindSet(observeViewerSet, observeSet, null, null);",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		List<IUiContentProvider> providers = new ArrayList<>();
		//
		BindingUiContentProviderContext context = new BindingUiContentProviderContext();
		context.setDirection("Target");
		assertEquals("Target", context.getDirection());
		assertNull(context.getValue("MyValueKey"));
		context.setValue("MyValueKey", "MyValue");
		assertEquals("MyValue", context.getValue("MyValueKey"));
		context.setValue("MyValueKey", null);
		assertNull(context.getValue("MyValueKey"));
		// ------------------------------------------------------------------------
		assertEquals(3, provider.getBindings().size());
		assertInstanceOf(BindingInfo.class, provider.getBindings().get(0));
		BindingInfo binding0 = (BindingInfo) provider.getBindings().get(0);
		//
		binding0.getModelObservable().createContentProviders(providers, context, provider);
		assertTrue(providers.isEmpty());
		//
		binding0.getTargetObservable().createContentProviders(providers, context, provider);
		//
		assertEquals(1, providers.size());
		assertInstanceOf(SwtDelayUiContentProvider.class, providers.get(0));
		// ------------------------------------------------------------------------
		assertInstanceOf(BindingInfo.class, provider.getBindings().get(1));
		BindingInfo binding1 = (BindingInfo) provider.getBindings().get(1);
		//
		providers.clear();
		binding1.getTargetObservable().createContentProviders(providers, context, provider);
		//
		assertEquals(2, providers.size());
		assertInstanceOf(SwtTextEventsUiContentProvider.class, providers.get(0));
		assertInstanceOf(SwtDelayUiContentProvider.class, providers.get(1));
		//
		providers.clear();
		binding1.getModelObservable().createContentProviders(providers, context, provider);
		//
		assertEquals(1, providers.size());
		assertInstanceOf(ObservableDetailUiContentProvider.class, providers.get(0));
		//
		ChooseClassAndPropertiesConfiguration detailActual =
				(ChooseClassAndPropertiesConfiguration) getConfiguration(providers.get(0));
		//
		ChooseClassAndPropertiesConfiguration detailExpected =
				new ChooseClassAndPropertiesConfiguration();
		detailExpected.setDialogFieldLabel("Master bean class:");
		detailExpected.setValueScope("beans");
		detailExpected.setChooseInterfaces(true);
		detailExpected.setEmptyClassErrorMessage("Choose a master bean class that contains properties.");
		detailExpected.setErrorMessagePrefix("Master bean class");
		detailExpected.setPropertiesLabel("Properties (for detail):");
		detailExpected.setPropertiesErrorMessage("Choose a detail property.");
		detailExpected.addDefaultStart("detail(");
		//
		assertEquals(detailExpected, detailActual);
		// --------------------------------------------------------------------------
		assertInstanceOf(BindingInfo.class, provider.getBindings().get(2));
		BindingInfo binding2 = (BindingInfo) provider.getBindings().get(2);
		//
		providers.clear();
		binding2.getTargetObservable().createContentProviders(providers, context, provider);
		//
		assertEquals(1, providers.size());
		assertInstanceOf(CheckedElementsUiContentProvider.class, providers.get(0));
		//
		ChooseClassConfiguration actual = getConfiguration(providers.get(0));
		//
		ChooseClassConfiguration expected = new ChooseClassConfiguration();
		expected.setDialogFieldLabel("Checked bean class:");
		expected.setValueScope("beans");
		expected.setChooseInterfaces(true);
		expected.setEmptyClassErrorMessage("Choose Target checked bean class.");
		expected.setErrorMessagePrefix("Target checked bean");
		//
		assertEquals(expected, actual);
	}

	@Test
	public void test_UpdateValueStrategy() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"import org.eclipse.core.databinding.Binding;",
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
								"    IObservableValue observeValue = BeanProperties.value(\"name\").observe(getClass());",
								"    IObservableValue observeWidget = WidgetProperties.text().observe(m_shell);",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindValue(observeWidget, observeValue, null, null);",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		assertEquals(1, provider.getBindings().size());
		assertInstanceOf(BindingInfo.class, provider.getBindings().get(0));
		BindingInfo binding = (BindingInfo) provider.getBindings().get(0);
		//
		assertNotNull(binding.getTargetStrategy());
		//
		List<IUiContentProvider> providers = new ArrayList<>();
		BindingUiContentProviderContext context = new BindingUiContentProviderContext();
		context.setDirection("Target");
		//
		binding.getTargetStrategy().createContentProviders(providers, context);
		assertEquals(2, providers.size());
		// ---------------------------------------------------------------------------
		assertInstanceOf(UpdateStrategyUiContentProvider.class, providers.get(0));
		assertInstanceOf(
				ChooseClassConfiguration.class,
				ReflectionUtils.getFieldObject(providers.get(0), "m_configuration"));
		ChooseClassConfiguration actual = getConfiguration(providers.get(0));
		//
		ChooseClassConfiguration expected = new ChooseClassConfiguration();
		expected.setDialogFieldLabel("UpdateValueStrategy:");
		expected.setDefaultValues(new String[]{
				"POLICY_UPDATE",
				"POLICY_NEVER",
				"POLICY_ON_REQUEST",
		"POLICY_CONVERT"});
		expected.setValueScope("org.eclipse.core.databinding.UpdateValueStrategy");
		expected.setRetargetClassName(
				"org.eclipse.core.databinding.UpdateValueStrategy",
				"POLICY_UPDATE");
		expected.setBaseClassName("org.eclipse.core.databinding.UpdateValueStrategy");
		expected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		expected.setEmptyClassErrorMessage("Target strategy class is empty.");
		expected.setErrorMessagePrefix("Target strategy");
		//
		assertEquals(expected, actual);
		// ---------------------------------------------------------------------------
		assertInstanceOf(UpdateStrategyPropertiesUiContentProvider.class, providers.get(1));
		//
		List<IUiContentProvider> subProviders =
				(List<IUiContentProvider>) ReflectionUtils.getFieldObject(providers.get(1), "m_providers");
		assertEquals(4, subProviders.size());
		assertInstanceOf(ValidatorUiContentProvider.class, subProviders.get(0));
		assertInstanceOf(ValidatorUiContentProvider.class, subProviders.get(1));
		assertInstanceOf(ValidatorUiContentProvider.class, subProviders.get(2));
		assertInstanceOf(ConverterUiContentProvider.class, subProviders.get(3));
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration afterConvertValidatorActual = getConfiguration(subProviders.get(0));
		//
		ChooseClassConfiguration afterConvertValidatorExpected = new ChooseClassConfiguration();
		afterConvertValidatorExpected.setDialogFieldLabel("AfterConvertValidator:");
		afterConvertValidatorExpected.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		afterConvertValidatorExpected.setClearValue("N/S");
		afterConvertValidatorExpected.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		afterConvertValidatorExpected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		afterConvertValidatorExpected.setEmptyClassErrorMessage(context.getDirection()
				+ " \"AfterConvertValidator\" class is empty.");
		afterConvertValidatorExpected.setErrorMessagePrefix(context.getDirection()
				+ " \"AfterConvertValidator\"");
		//
		assertEquals(afterConvertValidatorExpected, afterConvertValidatorActual);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration afterGetValidatorActual = getConfiguration(subProviders.get(1));
		//
		ChooseClassConfiguration afterGetValidatorExpected = new ChooseClassConfiguration();
		afterGetValidatorExpected.setDialogFieldLabel("AfterGetValidator:");
		afterGetValidatorExpected.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		afterGetValidatorExpected.setClearValue("N/S");
		afterGetValidatorExpected.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		afterGetValidatorExpected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		afterGetValidatorExpected.setEmptyClassErrorMessage(context.getDirection()
				+ " \"AfterGetValidator\" class is empty.");
		afterGetValidatorExpected.setErrorMessagePrefix(context.getDirection()
				+ " \"AfterGetValidator\"");
		//
		assertEquals(afterGetValidatorExpected, afterGetValidatorActual);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration beforeSetValidatorActual = getConfiguration(subProviders.get(2));
		//
		ChooseClassConfiguration beforeSetValidatorExpected = new ChooseClassConfiguration();
		beforeSetValidatorExpected.setDialogFieldLabel("BeforeSetValidator:");
		beforeSetValidatorExpected.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		beforeSetValidatorExpected.setClearValue("N/S");
		beforeSetValidatorExpected.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		beforeSetValidatorExpected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		beforeSetValidatorExpected.setEmptyClassErrorMessage(context.getDirection()
				+ " \"BeforeSetValidator\" class is empty.");
		beforeSetValidatorExpected.setErrorMessagePrefix(context.getDirection()
				+ " \"BeforeSetValidator\"");
		//
		assertEquals(beforeSetValidatorExpected, beforeSetValidatorActual);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration converterActual = getConfiguration(subProviders.get(3));
		//
		ChooseClassConfiguration converterExpected = new ChooseClassConfiguration();
		converterExpected.setDialogFieldLabel("Converter:");
		converterExpected.setValueScope("org.eclipse.core.databinding.conversion.IConverter");
		converterExpected.setClearValue("N/S");
		converterExpected.setBaseClassName("org.eclipse.core.databinding.conversion.IConverter");
		converterExpected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		converterExpected.setEmptyClassErrorMessage("Target converter class is empty.");
		converterExpected.setErrorMessagePrefix("Target converter");
		//
		assertEquals(converterExpected, converterActual);
	}

	@Test
	public void test_BindingInfo() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"import org.eclipse.core.databinding.Binding;",
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
								"    IObservableValue observeValue = BeanProperties.value(\"name\").observe(getClass());",
								"    IObservableValue observeWidget = WidgetProperties.text().observe(m_shell);",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindValue(observeWidget, observeValue, null, null);",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		assertEquals(1, provider.getBindings().size());
		assertInstanceOf(BindingInfo.class, provider.getBindings().get(0));
		BindingInfo binding = (BindingInfo) provider.getBindings().get(0);
		//
		assertNotNull(binding.getTargetStrategy());
		//
		IPageListener listener = new IPageListener() {
			@Override
			public void setTitleImage(Image image) {
			}

			@Override
			public void setTitle(String title) {
			}

			@Override
			public void setPageComplete(boolean complete) {
			}

			@Override
			public void setMessage(String newMessage) {
			}

			@Override
			public void setErrorMessage(String newMessage) {
			}
		};
		//
		List<IUiContentProvider> providers = provider.getContentProviders(binding, listener);
		//
		assertEquals(11, providers.size());
		// ---------------------------------------------------------------------------
		assertInstanceOf(LabelUiContentProvider.class, providers.get(0));
		assertEquals("Target:", ReflectionUtils.getFieldObject(providers.get(0), "m_title"));
		assertEquals("m_shell.text", ReflectionUtils.getFieldObject(providers.get(0), "m_value"));
		// ---------------------------------------------------------------------------
		assertInstanceOf(SwtTextEventsUiContentProvider.class, providers.get(1));
		// ---------------------------------------------------------------------------
		assertInstanceOf(SwtDelayUiContentProvider.class, providers.get(2));
		// ---------------------------------------------------------------------------
		assertInstanceOf(UpdateStrategyUiContentProvider.class, providers.get(3));
		assertInstanceOf(
				ChooseClassConfiguration.class,
				ReflectionUtils.getFieldObject(providers.get(3), "m_configuration"));
		ChooseClassConfiguration actualTargetStrategy = getConfiguration(providers.get(3));
		//
		ChooseClassConfiguration expectedTargetStrategy = new ChooseClassConfiguration();
		expectedTargetStrategy.setDialogFieldLabel("UpdateValueStrategy:");
		expectedTargetStrategy.setDefaultValues(new String[]{
				"POLICY_UPDATE",
				"POLICY_NEVER",
				"POLICY_ON_REQUEST",
		"POLICY_CONVERT"});
		expectedTargetStrategy.setValueScope("org.eclipse.core.databinding.UpdateValueStrategy");
		expectedTargetStrategy.setRetargetClassName(
				"org.eclipse.core.databinding.UpdateValueStrategy",
				"POLICY_UPDATE");
		expectedTargetStrategy.setBaseClassName("org.eclipse.core.databinding.UpdateValueStrategy");
		expectedTargetStrategy.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		expectedTargetStrategy.setEmptyClassErrorMessage("Target strategy class is empty.");
		expectedTargetStrategy.setErrorMessagePrefix("Target strategy");
		//
		assertEquals(expectedTargetStrategy, actualTargetStrategy);
		// ---------------------------------------------------------------------------
		assertInstanceOf(UpdateStrategyPropertiesUiContentProvider.class, providers.get(4));
		//
		List<IUiContentProvider> subProvidersTargetStrategy =
				(List<IUiContentProvider>) ReflectionUtils.getFieldObject(providers.get(4), "m_providers");
		assertEquals(4, subProvidersTargetStrategy.size());
		assertInstanceOf(ValidatorUiContentProvider.class, subProvidersTargetStrategy.get(0));
		assertInstanceOf(ValidatorUiContentProvider.class, subProvidersTargetStrategy.get(1));
		assertInstanceOf(ValidatorUiContentProvider.class, subProvidersTargetStrategy.get(2));
		assertInstanceOf(ConverterUiContentProvider.class, subProvidersTargetStrategy.get(3));
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration afterConvertValidatorActualTarget =
				getConfiguration(subProvidersTargetStrategy.get(0));
		//
		ChooseClassConfiguration afterConvertValidatorExpectedTarget = new ChooseClassConfiguration();
		afterConvertValidatorExpectedTarget.setDialogFieldLabel("AfterConvertValidator:");
		afterConvertValidatorExpectedTarget.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		afterConvertValidatorExpectedTarget.setClearValue("N/S");
		afterConvertValidatorExpectedTarget.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		afterConvertValidatorExpectedTarget.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		afterConvertValidatorExpectedTarget.setEmptyClassErrorMessage("Target \"AfterConvertValidator\" class is empty.");
		afterConvertValidatorExpectedTarget.setErrorMessagePrefix("Target \"AfterConvertValidator\"");
		//
		assertEquals(afterConvertValidatorExpectedTarget, afterConvertValidatorActualTarget);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration afterGetValidatorActualTarget =
				getConfiguration(subProvidersTargetStrategy.get(1));
		//
		ChooseClassConfiguration afterGetValidatorExpectedTarget = new ChooseClassConfiguration();
		afterGetValidatorExpectedTarget.setDialogFieldLabel("AfterGetValidator:");
		afterGetValidatorExpectedTarget.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		afterGetValidatorExpectedTarget.setClearValue("N/S");
		afterGetValidatorExpectedTarget.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		afterGetValidatorExpectedTarget.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		afterGetValidatorExpectedTarget.setEmptyClassErrorMessage("Target \"AfterGetValidator\" class is empty.");
		afterGetValidatorExpectedTarget.setErrorMessagePrefix("Target \"AfterGetValidator\"");
		//
		assertEquals(afterGetValidatorExpectedTarget, afterGetValidatorActualTarget);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration beforeSetValidatorActualTarget =
				getConfiguration(subProvidersTargetStrategy.get(2));
		//
		ChooseClassConfiguration beforeSetValidatorExpectedTarget = new ChooseClassConfiguration();
		beforeSetValidatorExpectedTarget.setDialogFieldLabel("BeforeSetValidator:");
		beforeSetValidatorExpectedTarget.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		beforeSetValidatorExpectedTarget.setClearValue("N/S");
		beforeSetValidatorExpectedTarget.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		beforeSetValidatorExpectedTarget.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		beforeSetValidatorExpectedTarget.setEmptyClassErrorMessage("Target \"BeforeSetValidator\" class is empty.");
		beforeSetValidatorExpectedTarget.setErrorMessagePrefix("Target \"BeforeSetValidator\"");
		//
		assertEquals(beforeSetValidatorExpectedTarget, beforeSetValidatorActualTarget);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration converterActualTarget =
				getConfiguration(subProvidersTargetStrategy.get(3));
		//
		ChooseClassConfiguration converterExpectedTarget = new ChooseClassConfiguration();
		converterExpectedTarget.setDialogFieldLabel("Converter:");
		converterExpectedTarget.setValueScope("org.eclipse.core.databinding.conversion.IConverter");
		converterExpectedTarget.setClearValue("N/S");
		converterExpectedTarget.setBaseClassName("org.eclipse.core.databinding.conversion.IConverter");
		converterExpectedTarget.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		converterExpectedTarget.setEmptyClassErrorMessage("Target converter class is empty.");
		converterExpectedTarget.setErrorMessagePrefix("Target converter");
		//
		assertEquals(converterExpectedTarget, converterActualTarget);
		// ---------------------------------------------------------------------------
		assertInstanceOf(SeparatorUiContentProvider.class, providers.get(5));
		// ---------------------------------------------------------------------------
		assertInstanceOf(LabelUiContentProvider.class, providers.get(6));
		assertEquals("Model:", ReflectionUtils.getFieldObject(providers.get(6), "m_title"));
		assertEquals("getClass().name", ReflectionUtils.getFieldObject(providers.get(6), "m_value"));
		// ---------------------------------------------------------------------------
		assertInstanceOf(UpdateStrategyUiContentProvider.class, providers.get(7));
		assertInstanceOf(
				ChooseClassConfiguration.class,
				ReflectionUtils.getFieldObject(providers.get(7), "m_configuration"));
		ChooseClassConfiguration actualModelStrategy = getConfiguration(providers.get(7));
		//
		ChooseClassConfiguration expectedModelStrategy = new ChooseClassConfiguration();
		expectedModelStrategy.setDialogFieldLabel("UpdateValueStrategy:");
		expectedModelStrategy.setDefaultValues(new String[]{
				"POLICY_UPDATE",
				"POLICY_NEVER",
				"POLICY_ON_REQUEST",
		"POLICY_CONVERT"});
		expectedModelStrategy.setValueScope("org.eclipse.core.databinding.UpdateValueStrategy");
		expectedModelStrategy.setRetargetClassName(
				"org.eclipse.core.databinding.UpdateValueStrategy",
				"POLICY_UPDATE");
		expectedModelStrategy.setBaseClassName("org.eclipse.core.databinding.UpdateValueStrategy");
		expectedModelStrategy.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		expectedModelStrategy.setEmptyClassErrorMessage("Model strategy class is empty.");
		expectedModelStrategy.setErrorMessagePrefix("Model strategy");
		//
		assertEquals(expectedModelStrategy, actualModelStrategy);
		// ---------------------------------------------------------------------------
		assertInstanceOf(UpdateStrategyPropertiesUiContentProvider.class, providers.get(8));
		//
		List<IUiContentProvider> subProvidersModelStrategy =
				(List<IUiContentProvider>) ReflectionUtils.getFieldObject(providers.get(8), "m_providers");
		assertEquals(4, subProvidersModelStrategy.size());
		assertInstanceOf(ValidatorUiContentProvider.class, subProvidersModelStrategy.get(0));
		assertInstanceOf(ValidatorUiContentProvider.class, subProvidersModelStrategy.get(1));
		assertInstanceOf(ValidatorUiContentProvider.class, subProvidersModelStrategy.get(2));
		assertInstanceOf(ConverterUiContentProvider.class, subProvidersModelStrategy.get(3));
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration afterConvertValidatorActualModel =
				getConfiguration(subProvidersModelStrategy.get(0));
		//
		ChooseClassConfiguration afterConvertValidatorExpectedModel = new ChooseClassConfiguration();
		afterConvertValidatorExpectedModel.setDialogFieldLabel("AfterConvertValidator:");
		afterConvertValidatorExpectedModel.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		afterConvertValidatorExpectedModel.setClearValue("N/S");
		afterConvertValidatorExpectedModel.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		afterConvertValidatorExpectedModel.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		afterConvertValidatorExpectedModel.setEmptyClassErrorMessage("Model \"AfterConvertValidator\" class is empty.");
		afterConvertValidatorExpectedModel.setErrorMessagePrefix("Model \"AfterConvertValidator\"");
		//
		assertEquals(afterConvertValidatorExpectedModel, afterConvertValidatorActualModel);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration afterGetValidatorActualModel =
				getConfiguration(subProvidersModelStrategy.get(1));
		//
		ChooseClassConfiguration afterGetValidatorExpectedModel = new ChooseClassConfiguration();
		afterGetValidatorExpectedModel.setDialogFieldLabel("AfterGetValidator:");
		afterGetValidatorExpectedModel.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		afterGetValidatorExpectedModel.setClearValue("N/S");
		afterGetValidatorExpectedModel.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		afterGetValidatorExpectedModel.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		afterGetValidatorExpectedModel.setEmptyClassErrorMessage("Model \"AfterGetValidator\" class is empty.");
		afterGetValidatorExpectedModel.setErrorMessagePrefix("Model \"AfterGetValidator\"");
		//
		assertEquals(afterGetValidatorExpectedModel, afterGetValidatorActualModel);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration beforeSetValidatorActualModel =
				getConfiguration(subProvidersModelStrategy.get(2));
		//
		ChooseClassConfiguration beforeSetValidatorExpectedModel = new ChooseClassConfiguration();
		beforeSetValidatorExpectedModel.setDialogFieldLabel("BeforeSetValidator:");
		beforeSetValidatorExpectedModel.setValueScope("org.eclipse.core.databinding.validation.IValidator");
		beforeSetValidatorExpectedModel.setClearValue("N/S");
		beforeSetValidatorExpectedModel.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
		beforeSetValidatorExpectedModel.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		beforeSetValidatorExpectedModel.setEmptyClassErrorMessage("Model \"BeforeSetValidator\" class is empty.");
		beforeSetValidatorExpectedModel.setErrorMessagePrefix("Model \"BeforeSetValidator\"");
		//
		assertEquals(beforeSetValidatorExpectedModel, beforeSetValidatorActualModel);
		// ---------------------------------------------------------------------------
		ChooseClassConfiguration converterActualModel =
				getConfiguration(subProvidersModelStrategy.get(3));
		//
		ChooseClassConfiguration converterExpectedModel = new ChooseClassConfiguration();
		converterExpectedModel.setDialogFieldLabel("Converter:");
		converterExpectedModel.setValueScope("org.eclipse.core.databinding.conversion.IConverter");
		converterExpectedModel.setClearValue("N/S");
		converterExpectedModel.setBaseClassName("org.eclipse.core.databinding.conversion.IConverter");
		converterExpectedModel.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		converterExpectedModel.setEmptyClassErrorMessage("Model converter class is empty.");
		converterExpectedModel.setErrorMessagePrefix("Model converter");
		//
		assertEquals(converterExpectedModel, converterActualModel);
		// ---------------------------------------------------------------------------
		assertInstanceOf(SeparatorUiContentProvider.class, providers.get(9));
		// ---------------------------------------------------------------------------
		assertInstanceOf(BindingContentProvider.class, providers.get(10));
	}

	@Test
	public void test_UpdateListStrategy() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"import org.eclipse.core.databinding.Binding;",
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
								"    IObservableList observeList1 = BeanProperties.list(\"name\").observe(getClass());",
								"    IObservableList observeList2 = BeanProperties.list(\"modifiers\").observe(getClass());",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindList(observeList1, observeList2, null, null);",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		assertEquals(1, provider.getBindings().size());
		assertInstanceOf(BindingInfo.class, provider.getBindings().get(0));
		BindingInfo binding = (BindingInfo) provider.getBindings().get(0);
		//
		assertNotNull(binding.getTargetStrategy());
		//
		List<IUiContentProvider> providers = new ArrayList<>();
		BindingUiContentProviderContext context = new BindingUiContentProviderContext();
		context.setDirection("Target");
		binding.getTargetStrategy().createContentProviders(providers, context);
		assertEquals(2, providers.size());
		//
		assertInstanceOf(UpdateStrategyUiContentProvider.class, providers.get(0));
		assertInstanceOf(
				ChooseClassConfiguration.class,
				ReflectionUtils.getFieldObject(providers.get(0), "m_configuration"));
		//
		ChooseClassConfiguration actual = getConfiguration(providers.get(0));
		//
		ChooseClassConfiguration expected = new ChooseClassConfiguration();
		expected.setDialogFieldLabel("UpdateListStrategy:");
		expected.setDefaultValues(new String[]{"POLICY_UPDATE", "POLICY_NEVER", "POLICY_ON_REQUEST"});
		expected.setValueScope("org.eclipse.core.databinding.UpdateListStrategy");
		expected.setRetargetClassName(
				"org.eclipse.core.databinding.UpdateListStrategy",
				"POLICY_UPDATE");
		expected.setBaseClassName("org.eclipse.core.databinding.UpdateListStrategy");
		expected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		expected.setEmptyClassErrorMessage("Target strategy class is empty.");
		expected.setErrorMessagePrefix("Target strategy");
		//
		assertEquals(expected, actual);
		//
		assertInstanceOf(UpdateStrategyPropertiesUiContentProvider.class, providers.get(1));
		//
		List<IUiContentProvider> subProviders =
				(List<IUiContentProvider>) ReflectionUtils.getFieldObject(providers.get(1), "m_providers");
		assertEquals(1, subProviders.size());
		assertInstanceOf(ConverterUiContentProvider.class, subProviders.get(0));
		//
		ChooseClassConfiguration converterActual = getConfiguration(subProviders.get(0));
		//
		ChooseClassConfiguration converterExpected = new ChooseClassConfiguration();
		converterExpected.setDialogFieldLabel("Converter:");
		converterExpected.setValueScope("org.eclipse.core.databinding.conversion.IConverter");
		converterExpected.setClearValue("N/S");
		converterExpected.setBaseClassName("org.eclipse.core.databinding.conversion.IConverter");
		converterExpected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		converterExpected.setEmptyClassErrorMessage("Target converter class is empty.");
		converterExpected.setErrorMessagePrefix("Target converter");
		//
		assertEquals(converterExpected, converterActual);
	}

	@Test
	public void test_UpdateSetStrategy() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"import org.eclipse.core.databinding.Binding;",
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
								"    IObservableSet observeSet1 = BeanProperties.set(\"name\").observe(getClass());",
								"    IObservableSet observeSet2 = BeanProperties.set(\"modifiers\").observe(getClass());",
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindSet(observeSet1, observeSet2, null, null);",
								"    return bindingContext;",
								"  }",
						"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		assertEquals(1, provider.getBindings().size());
		assertInstanceOf(BindingInfo.class, provider.getBindings().get(0));
		BindingInfo binding = (BindingInfo) provider.getBindings().get(0);
		//
		assertNotNull(binding.getTargetStrategy());
		//
		List<IUiContentProvider> providers = new ArrayList<>();
		BindingUiContentProviderContext context = new BindingUiContentProviderContext();
		context.setDirection("Target");
		binding.getTargetStrategy().createContentProviders(providers, context);
		assertEquals(2, providers.size());
		//
		assertInstanceOf(UpdateStrategyUiContentProvider.class, providers.get(0));
		assertInstanceOf(
				ChooseClassConfiguration.class,
				ReflectionUtils.getFieldObject(providers.get(0), "m_configuration"));
		//
		ChooseClassConfiguration actual = getConfiguration(providers.get(0));
		//
		ChooseClassConfiguration expected = new ChooseClassConfiguration();
		expected.setDialogFieldLabel("UpdateSetStrategy:");
		expected.setDefaultValues(new String[]{"POLICY_UPDATE", "POLICY_NEVER", "POLICY_ON_REQUEST"});
		expected.setValueScope("org.eclipse.core.databinding.UpdateSetStrategy");
		expected.setRetargetClassName("org.eclipse.core.databinding.UpdateSetStrategy", "POLICY_UPDATE");
		expected.setBaseClassName("org.eclipse.core.databinding.UpdateSetStrategy");
		expected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		expected.setEmptyClassErrorMessage("Target strategy class is empty.");
		expected.setErrorMessagePrefix("Target strategy");
		//
		assertEquals(expected, actual);
		//
		assertInstanceOf(UpdateStrategyPropertiesUiContentProvider.class, providers.get(1));
		//
		List<IUiContentProvider> subProviders =
				(List<IUiContentProvider>) ReflectionUtils.getFieldObject(providers.get(1), "m_providers");
		assertEquals(1, subProviders.size());
		assertInstanceOf(ConverterUiContentProvider.class, subProviders.get(0));
		//
		ChooseClassConfiguration converterActual = getConfiguration(subProviders.get(0));
		//
		ChooseClassConfiguration converterExpected = new ChooseClassConfiguration();
		converterExpected.setDialogFieldLabel("Converter:");
		converterExpected.setValueScope("org.eclipse.core.databinding.conversion.IConverter");
		converterExpected.setClearValue("N/S");
		converterExpected.setBaseClassName("org.eclipse.core.databinding.conversion.IConverter");
		converterExpected.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
		converterExpected.setEmptyClassErrorMessage("Target converter class is empty.");
		converterExpected.setErrorMessagePrefix("Target converter");
		//
		assertEquals(converterExpected, converterActual);
	}

	private static void assertEquals(ChooseClassConfiguration expected,
			ChooseClassConfiguration actual) throws Exception {
		assertEquals(expected.getDialogFieldLabel(), actual.getDialogFieldLabel());
		assertEquals(expected.isDialogFieldEnabled(), actual.isDialogFieldEnabled());
		assertEquals(expected.isUseClearButton(), actual.isUseClearButton());
		assertEquals(expected.getClearValue(), actual.getClearValue());
		assertEquals(expected.getValuesScope(), actual.getValuesScope());
		assertTrue(Arrays.equals(expected.getDefaultValues(), actual.getDefaultValues()));
		assertEquals(
				Arrays.toString(expected.getBaseClassNames()),
				Arrays.toString(actual.getBaseClassNames()));
		assertEquals(expected.isChooseInterfaces(), actual.isChooseInterfaces());
		assertTrue(Arrays.equals(
				expected.getConstructorsParameters(),
				actual.getConstructorsParameters()));
		assertEquals(expected.getEmptyClassErrorMessage(), actual.getEmptyClassErrorMessage());
		assertEquals(expected.getErrorMessagePrefix(), actual.getErrorMessagePrefix());
		assertEquals(
				ReflectionUtils.getFieldObject(expected, "m_defaultStarts"),
				ReflectionUtils.getFieldObject(actual, "m_defaultStarts"));
		assertEquals(
				ReflectionUtils.getFieldObject(expected, "m_targetClassName"),
				ReflectionUtils.getFieldObject(actual, "m_targetClassName"));
		assertEquals(
				ReflectionUtils.getFieldObject(expected, "m_retargetClassName"),
				ReflectionUtils.getFieldObject(actual, "m_retargetClassName"));
	}

	private static void assertEquals(ChooseClassAndPropertiesConfiguration expected,
			ChooseClassAndPropertiesConfiguration actual) throws Exception {
		assertEquals((ChooseClassConfiguration) expected, (ChooseClassConfiguration) actual);
		assertEquals(expected.getPropertiesLabel(), actual.getPropertiesLabel());
		assertEquals(expected.isPropertiesMultiChecked(), actual.isPropertiesMultiChecked());
		assertEquals(expected.isReorderMode(), actual.isReorderMode());
		assertEquals(
				expected.getLoadedPropertiesCheckedStrategy(),
				actual.getLoadedPropertiesCheckedStrategy());
		assertEquals(expected.getPropertiesErrorMessage(), actual.getPropertiesErrorMessage());
		if (expected.getPropertiesLabelProvider() == null) {
			assertNull(actual.getPropertiesLabelProvider());
		} else {
			assertNotNull(actual.getPropertiesLabelProvider());
			assertSame(
					expected.getPropertiesLabelProvider().getClass(),
					actual.getPropertiesLabelProvider().getClass());
		}
	}

	private static ChooseClassConfiguration getConfiguration(Object object) {
		return (ChooseClassConfiguration) ReflectionUtils.getFieldObject(object, "m_configuration");
	}
}