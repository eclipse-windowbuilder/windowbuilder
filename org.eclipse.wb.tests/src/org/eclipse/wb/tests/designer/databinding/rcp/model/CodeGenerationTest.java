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
package org.eclipse.wb.tests.designer.databinding.rcp.model;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.CollectionPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectPropertyObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.WritableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.WritableSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ListBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.SetBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ValueBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.ConverterInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateListStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateSetStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateValueStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.ValidatorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ItemsSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.MultiSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.TextSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertiesCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertyTextCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.CheckedElementsObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.MultiSelectionObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SingleSelectionObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableItemsCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableTextCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.SWT;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 *
 */
public class CodeGenerationTest extends AbstractBindingTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ConverterInfo() throws Exception {
		ConverterInfo converter =
				new ConverterInfo("org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter");
		//
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, converter);
		//
		assertEquals(
				"new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter()",
				converter.getSourceCode(lines, generationSupport));
		assertTrue(lines.isEmpty());
		//
		converter.setVariableIdentifier("m_converter");
		assertEquals("m_converter", converter.getSourceCode(lines, generationSupport));
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.conversion.IConverter m_converter = new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter();");
	}

	@Test
	public void test_ValidatorInfo() throws Exception {
		ValidatorInfo validator =
				new ValidatorInfo("org.eclipse.core.internal.databinding.validation.StringToDateValidator");
		//
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, validator);
		//
		assertEquals(
				"new org.eclipse.core.internal.databinding.validation.StringToDateValidator()",
				validator.getSourceCode(lines, generationSupport));
		assertTrue(lines.isEmpty());
		//
		validator.setVariableIdentifier("validator");
		assertEquals("validator", validator.getSourceCode(lines, generationSupport));
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.validation.IValidator validator = new org.eclipse.core.internal.databinding.validation.StringToDateValidator();");
	}

	@Test
	public void test_UpdateListStrategyInfo_Null() throws Exception {
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.Null,
						UpdateListStrategyInfo.Value.POLICY_UPDATE,
						null,
						null),
				"null");
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.Null,
						UpdateListStrategyInfo.Value.POLICY_UPDATE,
						null,
						"strategy"),
				"strategy",
				"org.eclipse.core.databinding.UpdateListStrategy strategy = new org.eclipse.core.databinding.UpdateListStrategy();");
	}

	@Test
	public void test_UpdateListStrategyInfo_DefaultConstructor() throws Exception {
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.DefaultConstructor,
						UpdateListStrategyInfo.Value.POLICY_UPDATE,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateListStrategy()");
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.DefaultConstructor,
						UpdateListStrategyInfo.Value.POLICY_UPDATE,
						null,
						"strategy"),
				"strategy",
				"org.eclipse.core.databinding.UpdateListStrategy strategy = new org.eclipse.core.databinding.UpdateListStrategy();");
	}

	@Test
	public void test_UpdateListStrategyInfo_IntConstructor() throws Exception {
		UpdateListStrategyInfo listStrategyInfo =
				createListStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateListStrategyInfo.Value.POLICY_UPDATE,
						null,
						null);
		assertStrategy(
				listStrategyInfo,
				"new org.eclipse.core.databinding.UpdateListStrategy(org.eclipse.core.databinding.UpdateListStrategy.POLICY_UPDATE)");
		//
		listStrategyInfo.setStringValue("POLICY_ON_REQUEST");
		assertStrategy(
				listStrategyInfo,
				"new org.eclipse.core.databinding.UpdateListStrategy(org.eclipse.core.databinding.UpdateListStrategy.POLICY_ON_REQUEST)");
		//
		listStrategyInfo.setStringValue("POLICY_NEVER");
		assertStrategy(
				listStrategyInfo,
				"new org.eclipse.core.databinding.UpdateListStrategy(org.eclipse.core.databinding.UpdateListStrategy.POLICY_NEVER)");
		//
		listStrategyInfo.setStringValue("POLICY_UPDATE");
		assertStrategy(listStrategyInfo, "null");
		//
		listStrategyInfo.setStringValue("com.company.project.Strategy");
		assertStrategy(listStrategyInfo, "new com.company.project.Strategy()");
		//
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateListStrategyInfo.Value.POLICY_ON_REQUEST,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateListStrategy(org.eclipse.core.databinding.UpdateListStrategy.POLICY_ON_REQUEST)");
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateListStrategyInfo.Value.POLICY_NEVER,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateListStrategy(org.eclipse.core.databinding.UpdateListStrategy.POLICY_NEVER)");
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateListStrategyInfo.Value.POLICY_NEVER,
						null,
						"m_strategy"),
				"m_strategy",
				"org.eclipse.core.databinding.UpdateListStrategy m_strategy = new org.eclipse.core.databinding.UpdateListStrategy(org.eclipse.core.databinding.UpdateListStrategy.POLICY_NEVER);");
	}

	@Test
	public void test_UpdateListStrategyInfo_ExtendetClass() throws Exception {
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						null,
						null),
				"new com.company.project.Strategy()");
		assertStrategy(
				createListStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						null,
						"_strategy"),
				"_strategy",
				"org.eclipse.core.databinding.UpdateListStrategy _strategy = new com.company.project.Strategy();");
	}

	@Test
	public void test_UpdateListStrategyInfo_Converter() throws Exception {
		UpdateListStrategyInfo strategy =
				createListStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						new ConverterInfo("org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter"),
						"_strategy");
		//
		assertStrategy(
				strategy,
				"_strategy",
				"org.eclipse.core.databinding.UpdateListStrategy _strategy = new com.company.project.Strategy();",
				"_strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter());");
		//
		strategy.setVariableIdentifier(null);
		assertStrategy(
				strategy,
				"strategy",
				"org.eclipse.core.databinding.UpdateListStrategy strategy = new com.company.project.Strategy();",
				"strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter());");
		//
		strategy.setConverter(new ConverterInfo("org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter"));
		assertStrategy(
				strategy,
				"strategy",
				"org.eclipse.core.databinding.UpdateListStrategy strategy = new com.company.project.Strategy();",
				"strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter());");
	}

	@Test
	public void test_UpdateSetStrategyInfo_Null() throws Exception {
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.Null,
						UpdateSetStrategyInfo.Value.POLICY_UPDATE,
						null,
						null),
				"null");
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.Null,
						UpdateSetStrategyInfo.Value.POLICY_UPDATE,
						null,
						"strategy"),
				"strategy",
				"org.eclipse.core.databinding.UpdateSetStrategy strategy = new org.eclipse.core.databinding.UpdateSetStrategy();");
	}

	@Test
	public void test_UpdateSetStrategyInfo_DefaultConstructor() throws Exception {
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.DefaultConstructor,
						UpdateSetStrategyInfo.Value.POLICY_UPDATE,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateSetStrategy()");
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.DefaultConstructor,
						UpdateSetStrategyInfo.Value.POLICY_UPDATE,
						null,
						"strategy"),
				"strategy",
				"org.eclipse.core.databinding.UpdateSetStrategy strategy = new org.eclipse.core.databinding.UpdateSetStrategy();");
	}

	@Test
	public void test_UpdateSetStrategyInfo_IntConstructor() throws Exception {
		UpdateSetStrategyInfo setStrategyInfo =
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateSetStrategyInfo.Value.POLICY_UPDATE,
						null,
						null);
		assertStrategy(
				setStrategyInfo,
				"new org.eclipse.core.databinding.UpdateSetStrategy(org.eclipse.core.databinding.UpdateSetStrategy.POLICY_UPDATE)");
		//
		setStrategyInfo.setStringValue("POLICY_ON_REQUEST");
		assertStrategy(
				setStrategyInfo,
				"new org.eclipse.core.databinding.UpdateSetStrategy(org.eclipse.core.databinding.UpdateSetStrategy.POLICY_ON_REQUEST)");
		//
		setStrategyInfo.setStringValue("POLICY_NEVER");
		assertStrategy(
				setStrategyInfo,
				"new org.eclipse.core.databinding.UpdateSetStrategy(org.eclipse.core.databinding.UpdateSetStrategy.POLICY_NEVER)");
		//
		setStrategyInfo.setStringValue("POLICY_UPDATE");
		assertStrategy(setStrategyInfo, "null");
		//
		setStrategyInfo.setStringValue("com.company.project.Strategy");
		assertStrategy(setStrategyInfo, "new com.company.project.Strategy()");
		//
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateSetStrategyInfo.Value.POLICY_ON_REQUEST,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateSetStrategy(org.eclipse.core.databinding.UpdateSetStrategy.POLICY_ON_REQUEST)");
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateSetStrategyInfo.Value.POLICY_NEVER,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateSetStrategy(org.eclipse.core.databinding.UpdateSetStrategy.POLICY_NEVER)");
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateSetStrategyInfo.Value.POLICY_NEVER,
						null,
						"m_strategy"),
				"m_strategy",
				"org.eclipse.core.databinding.UpdateSetStrategy m_strategy = new org.eclipse.core.databinding.UpdateSetStrategy(org.eclipse.core.databinding.UpdateSetStrategy.POLICY_NEVER);");
	}

	@Test
	public void test_UpdateSetStrategyInfo_ExtendetClass() throws Exception {
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						null,
						null),
				"new com.company.project.Strategy()");
		assertStrategy(
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						null,
						"_strategy"),
				"_strategy",
				"org.eclipse.core.databinding.UpdateSetStrategy _strategy = new com.company.project.Strategy();");
	}

	@Test
	public void test_UpdateSetStrategyInfo_Converter() throws Exception {
		UpdateSetStrategyInfo strategy =
				createSetStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						new ConverterInfo("org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter"),
						"_strategy");
		//
		assertStrategy(
				strategy,
				"_strategy",
				"org.eclipse.core.databinding.UpdateSetStrategy _strategy = new com.company.project.Strategy();",
				"_strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter());");
		//
		strategy.setVariableIdentifier(null);
		assertStrategy(
				strategy,
				"strategy",
				"org.eclipse.core.databinding.UpdateSetStrategy strategy = new com.company.project.Strategy();",
				"strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter());");
		//
		strategy.setConverter(new ConverterInfo("org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter"));
		assertStrategy(
				strategy,
				"strategy",
				"org.eclipse.core.databinding.UpdateSetStrategy strategy = new com.company.project.Strategy();",
				"strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter());");
	}

	@Test
	public void test_UpdateValueStrategyInfo_Null() throws Exception {
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.Null,
						UpdateValueStrategyInfo.Value.POLICY_UPDATE,
						null,
						null),
				"null");
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.Null,
						UpdateValueStrategyInfo.Value.POLICY_UPDATE,
						null,
						"strategy"),
				"strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy strategy = new org.eclipse.core.databinding.UpdateValueStrategy();");
	}

	@Test
	public void test_UpdateValueStrategyInfo_DefaultConstructor() throws Exception {
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.DefaultConstructor,
						UpdateValueStrategyInfo.Value.POLICY_UPDATE,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateValueStrategy()");
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.DefaultConstructor,
						UpdateValueStrategyInfo.Value.POLICY_UPDATE,
						null,
						"strategy"),
				"strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy strategy = new org.eclipse.core.databinding.UpdateValueStrategy();");
	}

	@Test
	public void test_UpdateValueStrategyInfo_IntConstructor() throws Exception {
		UpdateValueStrategyInfo valueStrategyInfo =
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateValueStrategyInfo.Value.POLICY_UPDATE,
						null,
						null);
		assertStrategy(
				valueStrategyInfo,
				"new org.eclipse.core.databinding.UpdateValueStrategy(org.eclipse.core.databinding.UpdateValueStrategy.POLICY_UPDATE)");
		//
		valueStrategyInfo.setStringValue("POLICY_ON_REQUEST");
		assertStrategy(
				valueStrategyInfo,
				"new org.eclipse.core.databinding.UpdateValueStrategy(org.eclipse.core.databinding.UpdateValueStrategy.POLICY_ON_REQUEST)");
		//
		valueStrategyInfo.setStringValue("POLICY_NEVER");
		assertStrategy(
				valueStrategyInfo,
				"new org.eclipse.core.databinding.UpdateValueStrategy(org.eclipse.core.databinding.UpdateValueStrategy.POLICY_NEVER)");
		//
		valueStrategyInfo.setStringValue("POLICY_CONVERT");
		assertStrategy(
				valueStrategyInfo,
				"new org.eclipse.core.databinding.UpdateValueStrategy(org.eclipse.core.databinding.UpdateValueStrategy.POLICY_CONVERT)");
		//
		valueStrategyInfo.setStringValue("POLICY_UPDATE");
		assertStrategy(valueStrategyInfo, "null");
		//
		valueStrategyInfo.setStringValue("com.company.project.Strategy");
		assertStrategy(valueStrategyInfo, "new com.company.project.Strategy()");
		//
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateValueStrategyInfo.Value.POLICY_ON_REQUEST,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateValueStrategy(org.eclipse.core.databinding.UpdateValueStrategy.POLICY_ON_REQUEST)");
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateValueStrategyInfo.Value.POLICY_NEVER,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateValueStrategy(org.eclipse.core.databinding.UpdateValueStrategy.POLICY_NEVER)");
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateValueStrategyInfo.Value.POLICY_CONVERT,
						null,
						null),
				"new org.eclipse.core.databinding.UpdateValueStrategy(org.eclipse.core.databinding.UpdateValueStrategy.POLICY_CONVERT)");
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.IntConstructor,
						UpdateValueStrategyInfo.Value.POLICY_NEVER,
						null,
						"m_strategy"),
				"m_strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy m_strategy = new org.eclipse.core.databinding.UpdateValueStrategy(org.eclipse.core.databinding.UpdateValueStrategy.POLICY_NEVER);");
	}

	@Test
	public void test_UpdateValueStrategyInfo_ExtendetClass() throws Exception {
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						null,
						null),
				"new com.company.project.Strategy()");
		assertStrategy(
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						null,
						"_strategy"),
				"_strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy _strategy = new com.company.project.Strategy();");
	}

	@Test
	public void test_UpdateValueStrategyInfo_Converter_Validators() throws Exception {
		UpdateValueStrategyInfo strategy =
				createValueStrategy(
						UpdateStrategyInfo.StrategyType.ExtendetClass,
						"com.company.project.Strategy",
						new ConverterInfo("org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter"),
						"_strategy");
		strategy.setValidator(
				"setAfterConvertValidator",
				new ValidatorInfo("org.eclipse.core.internal.databinding.validation.StringToIntegerValidator"));
		//
		ValidatorInfo validator =
				new ValidatorInfo("org.eclipse.core.internal.databinding.validation.StringToIntegerValidator");
		validator.setVariableIdentifier("validator");
		strategy.setValidator("setAfterGetValidator", validator);
		//
		assertStrategy(
				strategy,
				"_strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy _strategy = new com.company.project.Strategy();",
				"_strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter());",
				"_strategy.setAfterConvertValidator(new org.eclipse.core.internal.databinding.validation.StringToIntegerValidator());",
				"org.eclipse.core.databinding.validation.IValidator validator = new org.eclipse.core.internal.databinding.validation.StringToIntegerValidator();",
				"_strategy.setAfterGetValidator(validator);");
		//
		validator.setClassName("org.eclipse.core.internal.databinding.validation.StringToByteValidator");
		//
		assertStrategy(
				strategy,
				"_strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy _strategy = new com.company.project.Strategy();",
				"_strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter());",
				"_strategy.setAfterConvertValidator(new org.eclipse.core.internal.databinding.validation.StringToIntegerValidator());",
				"org.eclipse.core.databinding.validation.IValidator validator = new org.eclipse.core.internal.databinding.validation.StringToByteValidator();",
				"_strategy.setAfterGetValidator(validator);");
		//
		strategy.setVariableIdentifier(null);
		assertStrategy(
				strategy,
				"strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy strategy = new com.company.project.Strategy();",
				"strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter());",
				"strategy.setAfterConvertValidator(new org.eclipse.core.internal.databinding.validation.StringToIntegerValidator());",
				"org.eclipse.core.databinding.validation.IValidator validator = new org.eclipse.core.internal.databinding.validation.StringToByteValidator();",
				"strategy.setAfterGetValidator(validator);");
		//
		strategy.setConverter(new ConverterInfo("org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter"));
		assertStrategy(
				strategy,
				"strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy strategy = new com.company.project.Strategy();",
				"strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter());",
				"strategy.setAfterConvertValidator(new org.eclipse.core.internal.databinding.validation.StringToIntegerValidator());",
				"org.eclipse.core.databinding.validation.IValidator validator = new org.eclipse.core.internal.databinding.validation.StringToByteValidator();",
				"strategy.setAfterGetValidator(validator);");
		//
		strategy.setValidator("setAfterConvertValidator", null);
		assertStrategy(
				strategy,
				"strategy",
				"org.eclipse.core.databinding.UpdateValueStrategy strategy = new com.company.project.Strategy();",
				"strategy.setConverter(new org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter());",
				"org.eclipse.core.databinding.validation.IValidator validator = new org.eclipse.core.internal.databinding.validation.StringToByteValidator();",
				"strategy.setAfterGetValidator(validator);");
	}

	@Test
	public void test_BeanObservebleInfo() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test {",
						"  private String m_bean;",
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
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		//
		BeanBindableInfo bindableObject = (BeanBindableInfo) observes.get(0);
		BeanPropertyBindableInfo bindableProperty =
				(BeanPropertyBindableInfo) bindableObject.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		assertObservableInfo(
				new ListBeanObservableInfo(bindableObject, bindableProperty),
				new BeanObservableListCodeSupport(),
				"observeList",
				"org.eclipse.core.databinding.observable.list.IObservableList observeList = org.eclipse.core.databinding.beans.typed.PojoProperties.list(\"bytes\").observe(m_bean);",
				"org.eclipse.core.databinding.observable.list.IObservableList beanBytesObserveList = org.eclipse.core.databinding.beans.typed.PojoProperties.list(\"bytes\").observe(m_bean);");
		//
		assertObservableInfo(
				new SetBeanObservableInfo(bindableObject, bindableProperty),
				new BeanObservableSetCodeSupport(),
				"observeSet",
				"org.eclipse.core.databinding.observable.set.IObservableSet observeSet = org.eclipse.core.databinding.beans.typed.PojoProperties.set(\"bytes\").observe(m_bean);",
				"org.eclipse.core.databinding.observable.set.IObservableSet beanBytesObserveSet = org.eclipse.core.databinding.beans.typed.PojoProperties.set(\"bytes\").observe(m_bean);");
		//
		assertObservableInfo(
				new ValueBeanObservableInfo(bindableObject, bindableProperty),
				new BeanObservableValueCodeSupport(),
				"observeValue",
				"org.eclipse.core.databinding.observable.value.IObservableValue observeValue = org.eclipse.core.databinding.beans.typed.PojoProperties.value(\"bytes\").observe(m_bean);",
				"org.eclipse.core.databinding.observable.value.IObservableValue beanBytesObserveValue = org.eclipse.core.databinding.beans.typed.PojoProperties.value(\"bytes\").observe(m_bean);");
	}

	@Test
	public void test_CollectionObservableInfo() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test {",
						"  private java.util.List m_bean0;",
						"  private java.util.Set m_bean1;",
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
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		//
		BeanBindableInfo bindableObjectList = (BeanBindableInfo) observes.get(0);
		CollectionPropertyBindableInfo bindablePropertyList =
				(CollectionPropertyBindableInfo) bindableObjectList.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		assertObservableInfo(
				new WritableListBeanObservableInfo(bindableObjectList, bindablePropertyList, String.class),
				new WritableListCodeSupport(),
				"writableList",
				"org.eclipse.core.databinding.observable.list.WritableList writableList = new org.eclipse.core.databinding.observable.list.WritableList(m_bean0, java.lang.String.class);",
				"org.eclipse.core.databinding.observable.list.WritableList writableList = new org.eclipse.core.databinding.observable.list.WritableList(m_bean0, java.lang.String.class);");
		//
		WritableListBeanObservableInfo listObservable =
				new WritableListBeanObservableInfo(bindableObjectList, bindablePropertyList, null);
		listObservable.setElementType(String.class);
		assertObservableInfo(
				listObservable,
				new WritableListCodeSupport(),
				"writableList",
				"org.eclipse.core.databinding.observable.list.WritableList writableList = new org.eclipse.core.databinding.observable.list.WritableList(m_bean0, java.lang.String.class);",
				"org.eclipse.core.databinding.observable.list.WritableList writableList = new org.eclipse.core.databinding.observable.list.WritableList(m_bean0, java.lang.String.class);");
		//
		BeanBindableInfo bindableObjectSet = (BeanBindableInfo) observes.get(1);
		CollectionPropertyBindableInfo bindablePropertySet =
				(CollectionPropertyBindableInfo) bindableObjectSet.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		WritableSetBeanObservableInfo setObservable =
				new WritableSetBeanObservableInfo(bindableObjectSet, bindablePropertySet, null);
		setObservable.setElementType(String.class);
		assertObservableInfo(
				setObservable,
				new WritableSetCodeSupport(),
				"writableSet",
				"org.eclipse.core.databinding.observable.set.WritableSet writableSet = new org.eclipse.core.databinding.observable.set.WritableSet(m_bean1, java.lang.String.class);",
				"org.eclipse.core.databinding.observable.set.WritableSet writableSet = new org.eclipse.core.databinding.observable.set.WritableSet(m_bean1, java.lang.String.class);");
	}

	@Test
	public void test_BeanObservableInfo_observeDetail() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test {",
						"  private String m_bean;",
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
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		//
		BeanBindableInfo bindableObject = (BeanBindableInfo) observes.get(0);
		BeanPropertyBindableInfo bindableProperty =
				(BeanPropertyBindableInfo) bindableObject.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(1);
		//
		ValueBeanObservableInfo masterObservable =
				new ValueBeanObservableInfo(bindableObject, bindableProperty);
		masterObservable.setCodeSupport(new BeanObservableValueCodeSupport());
		//
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, masterObservable);
		// ----------
		DetailValueBeanObservableInfo observableDetailValue =
				new DetailValueBeanObservableInfo(masterObservable, null, "\"abc\"", int.class);
		observableDetailValue.setCodeSupport(new BeanObservableDetailValueCodeSupport());
		//
		observableDetailValue.setVariableIdentifier("detail");
		observableDetailValue.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.value.IObservableValue beanBytesObserveValue = org.eclipse.core.databinding.beans.typed.PojoProperties.value(\"bytes\").observe(m_bean);",
				"org.eclipse.core.databinding.observable.value.IObservableValue detail = org.eclipse.core.databinding.beans.typed.BeanProperties.value(\"abc\", int.class).observeDetail(beanBytesObserveValue);");
		//
		lines.clear();
		observableDetailValue.setVariableIdentifier(null);
		observableDetailValue.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.value.IObservableValue beanAbcObserveDetailValue = org.eclipse.core.databinding.beans.typed.BeanProperties.value(\"abc\", int.class).observeDetail(beanBytesObserveValue);");
		//
		lines.clear();
		observableDetailValue.setDetailPropertyReference(java.awt.Component.class, "\"property\"");
		observableDetailValue.setDetailPropertyType(String.class);
		observableDetailValue.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.value.IObservableValue beanAbcObserveDetailValue = org.eclipse.core.databinding.beans.typed.BeanProperties.value(java.awt.Component.class, \"property\", java.lang.String.class).observeDetail(beanBytesObserveValue);");
		// ----------
		DetailListBeanObservableInfo observableDetailList =
				new DetailListBeanObservableInfo(masterObservable, null, "\"abc\"", int.class);
		observableDetailList.setCodeSupport(new BeanObservableDetailListCodeSupport());
		//
		generationSupport = new CodeGenerationSupport(false, masterObservable);
		lines.clear();
		observableDetailList.setVariableIdentifier("detail");
		observableDetailList.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.value.IObservableValue beanBytesObserveValue = org.eclipse.core.databinding.beans.typed.PojoProperties.value(\"bytes\").observe(m_bean);",
				"org.eclipse.core.databinding.observable.list.IObservableList detail = org.eclipse.core.databinding.beans.typed.BeanProperties.list(\"abc\", int.class).observeDetail(beanBytesObserveValue);");
		//
		lines.clear();
		observableDetailList.setVariableIdentifier(null);
		observableDetailList.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.list.IObservableList beanAbcObserveDetailList = org.eclipse.core.databinding.beans.typed.BeanProperties.list(\"abc\", int.class).observeDetail(beanBytesObserveValue);");
		// ----------
		DetailSetBeanObservableInfo observableDetailSet =
				new DetailSetBeanObservableInfo(masterObservable, null, "\"abc\"", int.class);
		observableDetailSet.setCodeSupport(new BeanObservableDetailSetCodeSupport());
		//
		generationSupport = new CodeGenerationSupport(false, masterObservable);
		lines.clear();
		observableDetailSet.setVariableIdentifier("detail");
		observableDetailSet.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.value.IObservableValue beanBytesObserveValue = org.eclipse.core.databinding.beans.typed.PojoProperties.value(\"bytes\").observe(m_bean);",
				"org.eclipse.core.databinding.observable.set.IObservableSet detail = org.eclipse.core.databinding.beans.typed.BeanProperties.set(\"abc\", int.class).observeDetail(beanBytesObserveValue);");
		//
		lines.clear();
		observableDetailSet.setVariableIdentifier(null);
		observableDetailSet.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.set.IObservableSet beanAbcObserveDetailSet = org.eclipse.core.databinding.beans.typed.BeanProperties.set(\"abc\", int.class).observeDetail(beanBytesObserveValue);");
	}

	@Test
	public void test_DirectPropertyObservableInfo() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  private DataBindingContext m_context;",
						"  private String m_bean;",
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
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		//
		BeanBindableInfo bindableObject = (BeanBindableInfo) observes.get(0);
		DirectPropertyBindableInfo bindableProperty =
				(DirectPropertyBindableInfo) bindableObject.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(3);
		//
		DirectPropertyObservableInfo observable =
				new DirectPropertyObservableInfo(bindableObject, bindableProperty);
		//
		assertEquals("m_context.getValidationStatusProviders()", observable.getVariableIdentifier());
		//
		try {
			observable.setVariableIdentifier("variable");
			fail();
		} catch (Throwable e) {
			assertInstanceOf(UnsupportedOperationException.class, e);
		}
		//
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, observable);
		//
		observable.addSourceCode(lines, generationSupport);
		//
		assertTrue(lines.isEmpty());
		assertEquals("m_context.getValidationStatusProviders()", observable.getVariableIdentifier());
	}

	@Test
	public void test_DirectObservableInfo() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  private WritableValue m_value;",
						"  private String m_bean;",
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
		List<IObserveInfo> observes = provider.getObserves(ObserveType.BEANS);
		//
		BeanBindableInfo bindableObject = (BeanBindableInfo) observes.get(0);
		DirectPropertyBindableInfo bindableProperty =
				(DirectPropertyBindableInfo) bindableObject.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(0);
		//
		DirectObservableInfo observable = new DirectObservableInfo(bindableObject, bindableProperty);
		//
		assertEquals("m_value", observable.getVariableIdentifier());
		//
		try {
			observable.setVariableIdentifier("variable");
			fail();
		} catch (Throwable e) {
			assertInstanceOf(UnsupportedOperationException.class, e);
		}
		//
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, observable);
		//
		observable.addSourceCode(lines, generationSupport);
		//
		assertTrue(lines.isEmpty());
		assertEquals("m_value", observable.getVariableIdentifier());
	}

	@Test
	public void test_SWTObservableInfo() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private Spinner m_spinner;",
						"  private Text m_text;",
						"  private Combo m_combo;",
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
						"    m_spinner = new Spinner(m_shell, SWT.NONE);",
						"    m_text = new Text(m_shell, SWT.NONE);",
						"    m_combo = new Combo(m_shell, SWT.NONE);",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> observes = provider.getObserves(ObserveType.WIDGETS);
		//
		WidgetBindableInfo shellBindable = (WidgetBindableInfo) observes.get(0);
		List<IObserveInfo> shellProperties =
				shellBindable.getChildren(ChildrenContext.ChildrenForPropertiesTable);
		//
		WidgetPropertyBindableInfo shellEnabled = (WidgetPropertyBindableInfo) shellProperties.get(2);
		assertSWTObservableInfo(
				shellBindable,
				shellEnabled,
				new SwtObservableCodeSupport(),
				"shellObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveEnabledObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveEnabledObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(m_shell));");
		//
		WidgetPropertyBindableInfo shellVisible = (WidgetPropertyBindableInfo) shellProperties.get(10);
		assertSWTObservableInfo(
				shellBindable,
				shellVisible,
				new SwtObservableCodeSupport(),
				"shellObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeVisible(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveVisibleObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeVisible(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveVisibleObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeVisible(m_shell));");
		//
		WidgetPropertyBindableInfo shellTooltip = (WidgetPropertyBindableInfo) shellProperties.get(9);
		assertSWTObservableInfo(
				shellBindable,
				shellTooltip,
				new SwtObservableCodeSupport(),
				"shellObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeTooltipText(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveTooltipTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeTooltipText(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveTooltipTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeTooltipText(m_shell));");
		//
		WidgetPropertyBindableInfo shellText = (WidgetPropertyBindableInfo) shellProperties.get(8);
		assertSWTObservableInfo(
				shellBindable,
				shellText,
				new SwtObservableCodeSupport(),
				"shellObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_shell));");
		//
		WidgetPropertyBindableInfo shellForeground =
				(WidgetPropertyBindableInfo) shellProperties.get(5);
		assertSWTObservableInfo(
				shellBindable,
				shellForeground,
				new SwtObservableCodeSupport(),
				"shellObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeForeground(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveForegroundObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeForeground(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveForegroundObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeForeground(m_shell));");
		//
		WidgetPropertyBindableInfo shellBackground =
				(WidgetPropertyBindableInfo) shellProperties.get(0);
		assertSWTObservableInfo(
				shellBindable,
				shellBackground,
				new SwtObservableCodeSupport(),
				"shellObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeBackground(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveBackgroundObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeBackground(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveBackgroundObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeBackground(m_shell));");
		//
		WidgetPropertyBindableInfo shellFont = (WidgetPropertyBindableInfo) shellProperties.get(4);
		assertSWTObservableInfo(
				shellBindable,
				shellFont,
				new SwtObservableCodeSupport(),
				"shellObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeFont(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveFontObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeFont(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveFontObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeFont(m_shell));");
		// ----------------------
		List<IObserveInfo> shellChildren =
				shellBindable.getChildren(ChildrenContext.ChildrenForMasterTable);
		//
		WidgetBindableInfo spinnerBindable = (WidgetBindableInfo) shellChildren.get(0);
		//
		WidgetPropertyBindableInfo spinnerMax =
				(WidgetPropertyBindableInfo) spinnerBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(7);
		assertSWTObservableInfo(
				spinnerBindable,
				spinnerMax,
				new SwtObservableCodeSupport(),
				"spinnerObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue spinnerObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeMax(m_spinner);",
				"org.eclipse.core.databinding.observable.value.IObservableValue spinnerObserveMaxObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeMax(m_spinner);",
				"org.eclipse.core.databinding.observable.value.IObservableValue spinnerObserveMaxObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeMax(m_spinner));");
		//
		WidgetPropertyBindableInfo spinnerMin =
				(WidgetPropertyBindableInfo) spinnerBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(8);
		assertSWTObservableInfo(
				spinnerBindable,
				spinnerMin,
				new SwtObservableCodeSupport(),
				"spinnerObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue spinnerObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeMin(m_spinner);",
				"org.eclipse.core.databinding.observable.value.IObservableValue spinnerObserveMinObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeMin(m_spinner);",
				"org.eclipse.core.databinding.observable.value.IObservableValue spinnerObserveMinObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeMin(m_spinner));");
		// -----------------
		WidgetBindableInfo textBindable = (WidgetBindableInfo) shellChildren.get(1);
		//
		WidgetPropertyBindableInfo textEditable =
				(WidgetPropertyBindableInfo) textBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(2);
		assertSWTObservableInfo(
				textBindable,
				textEditable,
				new SwtObservableCodeSupport(),
				"editableObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue editableObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeEditable(m_text);",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserveEditableObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeEditable(m_text);",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserveEditableObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeEditable(m_text));");
		//
		WidgetPropertyBindableInfo textText =
				(WidgetPropertyBindableInfo) textBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(10);
		assertSWTObservableInfo(
				new TextSwtObservableInfo(textBindable, textText, SWT.Modify),
				new SwtObservableTextCodeSupport(),
				"textObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.Modify);",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserveTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.Modify);",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserveTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.Modify));");
		assertSWTObservableInfo(
				new TextSwtObservableInfo(textBindable, textText, SWT.FocusOut),
				new SwtObservableTextCodeSupport(),
				"textObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.FocusOut);",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserveTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.FocusOut);",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserveTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.FocusOut));");
		assertSWTObservableInfo(
				new TextSwtObservableInfo(textBindable, textText, SWT.NONE),
				new SwtObservableTextCodeSupport(),
				"textObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.NONE);",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserveTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.NONE);",
				"org.eclipse.core.databinding.observable.value.IObservableValue textObserveTextObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeText(m_text, org.eclipse.swt.SWT.NONE));");
		// -----------------
		WidgetBindableInfo comboBindable = (WidgetBindableInfo) shellChildren.get(2);
		//
		WidgetPropertyBindableInfo comboItems =
				(WidgetPropertyBindableInfo) comboBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(6);
		assertObservableInfo(
				new ItemsSwtObservableInfo(comboBindable, comboItems),
				new SwtObservableItemsCodeSupport(),
				"itemsObserve",
				"org.eclipse.core.databinding.observable.list.IObservableList itemsObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeItems(m_combo);",
				"org.eclipse.core.databinding.observable.list.IObservableList comboObserveItemsObserveListWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeItems(m_combo);");
		//
		WidgetPropertyBindableInfo comboSelection =
				(WidgetPropertyBindableInfo) comboBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(8);
		assertSWTObservableInfo(
				comboBindable,
				comboSelection,
				new SwtObservableCodeSupport(),
				"selectionObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue selectionObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeSelection(m_combo);",
				"org.eclipse.core.databinding.observable.value.IObservableValue comboObserveSelectionObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeSelection(m_combo);",
				"org.eclipse.core.databinding.observable.value.IObservableValue comboObserveSelectionObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeSelection(m_combo));");
		//
		WidgetPropertyBindableInfo comboSelectionIndex =
				(WidgetPropertyBindableInfo) comboBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(9);
		assertSWTObservableInfo(
				comboBindable,
				comboSelectionIndex,
				new SwtObservableCodeSupport(),
				"selectionObserve",
				"org.eclipse.core.databinding.observable.value.IObservableValue selectionObserve = org.eclipse.jface.databinding.swt.SWTObservables.observeSingleSelectionIndex(m_combo);",
				"org.eclipse.core.databinding.observable.value.IObservableValue comboObserveSingleSelectionIndexObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeSingleSelectionIndex(m_combo);",
				"org.eclipse.core.databinding.observable.value.IObservableValue comboObserveSingleSelectionIndexObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(100, org.eclipse.jface.databinding.swt.SWTObservables.observeSingleSelectionIndex(m_combo));");
	}

	@Test
	public void test_ViewerObservableInfo() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private CheckboxTableViewer m_viewer;",
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
						"    m_viewer = new CheckboxTableViewer(m_shell, SWT.NONE);",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> observes = provider.getObserves(ObserveType.WIDGETS);
		//
		WidgetBindableInfo viewerBindable =
				(WidgetBindableInfo) observes.get(0).getChildren(ChildrenContext.ChildrenForMasterTable).get(
						0).getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		//
		WidgetPropertyBindableInfo singleSelection =
				(WidgetPropertyBindableInfo) viewerBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(0);
		assertObservableInfo(
				new SingleSelectionObservableInfo(viewerBindable, singleSelection),
				new SingleSelectionObservableCodeSupport(),
				"viewerObservable",
				"org.eclipse.core.databinding.observable.value.IObservableValue viewerObservable = org.eclipse.jface.databinding.viewers.ViewersObservables.observeSingleSelection(m_viewer);",
				"org.eclipse.core.databinding.observable.value.IObservableValue viewerObserveSingleSelection = org.eclipse.jface.databinding.viewers.ViewersObservables.observeSingleSelection(m_viewer);");
		//
		WidgetPropertyBindableInfo multiSelection =
				(WidgetPropertyBindableInfo) viewerBindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(2);
		assertObservableInfo(
				new MultiSelectionObservableInfo(viewerBindable, multiSelection),
				new MultiSelectionObservableCodeSupport(),
				"viewerObservable",
				"org.eclipse.core.databinding.observable.list.IObservableList viewerObservable = org.eclipse.jface.databinding.viewers.ViewersObservables.observeMultiSelection(m_viewer);",
				"org.eclipse.core.databinding.observable.list.IObservableList viewerObserveMultiSelection = org.eclipse.jface.databinding.viewers.ViewersObservables.observeMultiSelection(m_viewer);");
		//
		CheckedElementsObservableInfo checkedObservable =
				new CheckedElementsObservableInfo(viewerBindable);
		checkedObservable.setElementType(String.class);
		assertObservableInfo(
				checkedObservable,
				new CheckedElementsObservableCodeSupport(),
				"viewerObservable",
				"org.eclipse.core.databinding.observable.set.IObservableSet viewerObservable = org.eclipse.jface.databinding.viewers.ViewersObservables.observeCheckedElements(m_viewer, java.lang.String.class);",
				"org.eclipse.core.databinding.observable.set.IObservableSet viewerObserveCheckedElements = org.eclipse.jface.databinding.viewers.ViewersObservables.observeCheckedElements(m_viewer, java.lang.String.class);");
	}

	@Test
	public void test_DataBindingContextInfo() throws Exception {
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
		List<IObserveInfo> observes = provider.getObserves(ObserveType.WIDGETS);
		//
		WidgetBindableInfo shellBindable = (WidgetBindableInfo) observes.get(0);
		//
		List<IObserveInfo> shellProperties =
				shellBindable.getChildren(ChildrenContext.ChildrenForPropertiesTable);
		WidgetPropertyBindableInfo shellEnabled = (WidgetPropertyBindableInfo) shellProperties.get(2);
		WidgetPropertyBindableInfo shellVisible = (WidgetPropertyBindableInfo) shellProperties.get(10);
		//
		DataBindingContextInfo contextInfo = new DataBindingContextInfo();
		//
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, contextInfo);
		//
		contextInfo.addSourceCode(m_lastEditor, lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.DataBindingContext bindingContext = new org.eclipse.core.databinding.DataBindingContext();",
				"//",
				"return bindingContext;");
		//
		lines.clear();
		contextInfo.setVariableIdentifier("context");
		contextInfo.addInitializeContext(true);
		contextInfo.addSourceCode(m_lastEditor, lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.DataBindingContext context = new org.eclipse.core.databinding.DataBindingContext();",
				"initializeContext(context);",
				"//",
				"return context;");
		//
		SwtObservableInfo target = new SwtObservableInfo(shellBindable, shellEnabled);
		target.setCodeSupport(new SwtObservableCodeSupport());
		SwtObservableInfo model = new SwtObservableInfo(shellBindable, shellVisible);
		model.setCodeSupport(new SwtObservableCodeSupport());
		contextInfo.getBindings().add(new ValueBindingInfo(target, model));
		//
		lines.clear();
		contextInfo.setVariableIdentifier(null);
		contextInfo.addInitializeContext(false);
		contextInfo.addSourceCode(m_lastEditor, lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.DataBindingContext bindingContext = new org.eclipse.core.databinding.DataBindingContext();",
				"//",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveEnabledObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveVisibleObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeVisible(m_shell);",
				"bindingContext.bindValue(shellObserveEnabledObserveWidget, shellObserveVisibleObserveWidget, null, null);",
				"//",
				"return bindingContext;");
		//
		lines.clear();
		generationSupport = new CodeGenerationSupport(false, contextInfo);
		org.eclipse.wb.internal.rcp.databinding.Activator.getStore().setValue(
				IPreferenceConstants.INITDB_TRY_CATCH,
				true);
		try {
			contextInfo.addSourceCode(m_lastEditor, lines, generationSupport);
			//
			assertEqualsLines(
					lines,
					"org.eclipse.core.databinding.DataBindingContext bindingContext = new org.eclipse.core.databinding.DataBindingContext();",
					"//",
					"try {",
					"	org.eclipse.core.databinding.observable.value.IObservableValue shellObserveEnabledObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(m_shell);",
					"	org.eclipse.core.databinding.observable.value.IObservableValue shellObserveVisibleObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeVisible(m_shell);",
					"	bindingContext.bindValue(shellObserveEnabledObserveWidget, shellObserveVisibleObserveWidget, null, null);",
					"} catch(Throwable e) {}",
					"//",
					"return bindingContext;");
		} finally {
			org.eclipse.wb.internal.rcp.databinding.Activator.getStore().setValue(
					IPreferenceConstants.INITDB_TRY_CATCH,
					false);
		}
	}

	@Test
	public void test_BindingInfo() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private Combo m_combo0;",
						"  private Combo m_combo1;",
						"  private CheckboxTableViewer m_viewer0;",
						"  private CheckboxTableViewer m_viewer1;",
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
						"    m_combo0 = new Combo(m_shell, SWT.BORDER);",
						"    m_combo1 = new Combo(m_shell, SWT.BORDER);",
						"    m_viewer0 = new CheckboxTableViewer(m_shell, SWT.NONE);",
						"    m_viewer1 = new CheckboxTableViewer(m_shell, SWT.NONE);",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> observes = provider.getObserves(ObserveType.WIDGETS);
		//
		WidgetBindableInfo shellBindable = (WidgetBindableInfo) observes.get(0);
		// ----------------
		List<IObserveInfo> shellProperties =
				shellBindable.getChildren(ChildrenContext.ChildrenForPropertiesTable);
		WidgetPropertyBindableInfo shellEnabled = (WidgetPropertyBindableInfo) shellProperties.get(2);
		WidgetPropertyBindableInfo shellVisible = (WidgetPropertyBindableInfo) shellProperties.get(10);
		//
		SwtObservableInfo target0 = new SwtObservableInfo(shellBindable, shellEnabled);
		target0.setCodeSupport(new SwtObservableCodeSupport());
		SwtObservableInfo model0 = new SwtObservableInfo(shellBindable, shellVisible);
		model0.setCodeSupport(new SwtObservableCodeSupport());
		ValueBindingInfo binding0 = new ValueBindingInfo(target0, model0);
		//
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, binding0);
		DataBindingContextInfo context = new DataBindingContextInfo();
		context.setVariableIdentifier("context");
		//
		binding0.addSourceCode(context, lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveEnabledObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveVisibleObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeVisible(m_shell);",
				"context.bindValue(shellObserveEnabledObserveWidget, shellObserveVisibleObserveWidget, null, null);");
		//
		binding0.setVariableIdentifier("binding");
		//
		lines.clear();
		generationSupport = new CodeGenerationSupport(false, binding0);
		//
		binding0.addSourceCode(context, lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveEnabledObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(m_shell);",
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveVisibleObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeVisible(m_shell);",
				"binding = context.bindValue(shellObserveEnabledObserveWidget, shellObserveVisibleObserveWidget, null, null);");
		//
		ValueBindingInfo binding1 = new ValueBindingInfo(target0, target0);
		//
		lines.clear();
		generationSupport = new CodeGenerationSupport(false, binding1);
		binding1.addSourceCode(context, lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.value.IObservableValue shellObserveEnabledObserveWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(m_shell);",
				"context.bindValue(shellObserveEnabledObserveWidget, shellObserveEnabledObserveWidget, null, null);");
		// ----------------
		List<IObserveInfo> shellChildren =
				shellBindable.getChildren(ChildrenContext.ChildrenForMasterTable);
		//
		WidgetBindableInfo combo0Bindable = (WidgetBindableInfo) shellChildren.get(0);
		WidgetPropertyBindableInfo combo0Items =
				(WidgetPropertyBindableInfo) combo0Bindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(6);
		//
		WidgetBindableInfo combo1Bindable = (WidgetBindableInfo) shellChildren.get(1);
		WidgetPropertyBindableInfo combo1Items =
				(WidgetPropertyBindableInfo) combo1Bindable.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(6);
		//
		ItemsSwtObservableInfo target1 = new ItemsSwtObservableInfo(combo0Bindable, combo0Items);
		target1.setCodeSupport(new SwtObservableItemsCodeSupport());
		ItemsSwtObservableInfo model1 = new ItemsSwtObservableInfo(combo1Bindable, combo1Items);
		model1.setCodeSupport(new SwtObservableItemsCodeSupport());
		ListBindingInfo binding2 = new ListBindingInfo(target1, model1);
		//
		lines.clear();
		generationSupport = new CodeGenerationSupport(false, binding2);
		binding2.addSourceCode(context, lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.list.IObservableList combo0ObserveItemsObserveListWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeItems(m_combo0);",
				"org.eclipse.core.databinding.observable.list.IObservableList combo1ObserveItemsObserveListWidget = org.eclipse.jface.databinding.swt.SWTObservables.observeItems(m_combo1);",
				"context.bindList(combo0ObserveItemsObserveListWidget, combo1ObserveItemsObserveListWidget, null, null);");
		// ----------------
		WidgetBindableInfo viewerBindable0 =
				(WidgetBindableInfo) shellChildren.get(2).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		//
		WidgetBindableInfo viewerBindable1 =
				(WidgetBindableInfo) shellChildren.get(3).getChildren(
						ChildrenContext.ChildrenForMasterTable).get(0);
		//
		CheckedElementsObservableInfo checkedObservable0 =
				new CheckedElementsObservableInfo(viewerBindable0, String.class);
		checkedObservable0.setCodeSupport(new CheckedElementsObservableCodeSupport());
		CheckedElementsObservableInfo checkedObservable1 =
				new CheckedElementsObservableInfo(viewerBindable1, String.class);
		checkedObservable1.setCodeSupport(new CheckedElementsObservableCodeSupport());
		SetBindingInfo binding3 = new SetBindingInfo(checkedObservable0, checkedObservable1);
		//
		lines.clear();
		generationSupport = new CodeGenerationSupport(false, binding3);
		binding3.addSourceCode(context, lines, generationSupport);
		//
		assertEqualsLines(
				lines,
				"org.eclipse.core.databinding.observable.set.IObservableSet viewer0ObserveCheckedElements = org.eclipse.jface.databinding.viewers.ViewersObservables.observeCheckedElements(m_viewer0, java.lang.String.class);",
				"org.eclipse.core.databinding.observable.set.IObservableSet viewer1ObserveCheckedElements = org.eclipse.jface.databinding.viewers.ViewersObservables.observeCheckedElements(m_viewer1, java.lang.String.class);",
				"context.bindSet(viewer0ObserveCheckedElements, viewer1ObserveCheckedElements, null, null);");
	}

	@Test
	public void test_BindingInfo_setVariableIdentifier() throws Exception {
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
						"    IObservableValue observeWidget0 = WidgetProperties.visible().observe(m_shell);",
						"    IObservableValue observeWidget1 = WidgetProperties.text().observe(m_shell);",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    bindingContext.bindValue(observeWidget0, observeWidget1, null, null);",
						"    return bindingContext;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertEquals(1, provider.getBindings().size());
		assertInstanceOf(BindingInfo.class, provider.getBindings().get(0));
		//
		BindingInfo binding = (BindingInfo) provider.getBindings().get(0);
		assertNull(binding.getVariableIdentifier());
		//
		binding.setVariableIdentifier(shell.getRootJava(), "m_binding", true);
		assertEquals("m_binding", binding.getVariableIdentifier());
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private Binding m_binding;",
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
				"    IObservableValue observeWidget0 = WidgetProperties.visible().observe(m_shell);",
				"    IObservableValue observeWidget1 = WidgetProperties.text().observe(m_shell);",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    bindingContext.bindValue(observeWidget0, observeWidget1, null, null);",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
		//
		binding.setVariableIdentifier(shell.getRootJava(), "_binding", true);
		assertEquals("_binding", binding.getVariableIdentifier());
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private Binding _binding;",
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
				"    IObservableValue observeWidget0 = WidgetProperties.visible().observe(m_shell);",
				"    IObservableValue observeWidget1 = WidgetProperties.text().observe(m_shell);",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    bindingContext.bindValue(observeWidget0, observeWidget1, null, null);",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
		//
		binding.setVariableIdentifier(shell.getRootJava(), null, false);
		assertNull(binding.getVariableIdentifier());
		//
		assertEditor(DatabindingTestUtils.getTestSource(
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
				"    IObservableValue observeWidget0 = WidgetProperties.visible().observe(m_shell);",
				"    IObservableValue observeWidget1 = WidgetProperties.text().observe(m_shell);",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    bindingContext.bindValue(observeWidget0, observeWidget1, null, null);",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_DataBindingsCodeUtils_ensureDBLibraries() throws Exception {
		TestProject project = new TestProject("OtherProject");
		try {
			BTestUtils.configure(project);
			//
			IJavaProject javaProject = project.getJavaProject();
			//
			assertFalse(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.observable.IObservable"));
			assertFalse(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.beans.IBeanObservable"));
			assertFalse(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.jface.databinding.swt.ISWTObservable"));
			//
			DataBindingsCodeUtils.ensureDBLibraries(javaProject);
			//
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.observable.IObservable"));
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.beans.IBeanObservable"));
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.jface.databinding.swt.ISWTObservable"));
			//
			DataBindingsCodeUtils.ensureDBLibraries(javaProject);
			//
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.observable.IObservable"));
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.beans.IBeanObservable"));
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.jface.databinding.swt.ISWTObservable"));
		} finally {
			project.dispose();
			waitForAutoBuild();
		}
	}

	@Test
	public void test_DataBindingsCodeUtils_ensureDBLibraries_ForPlugin() throws Exception {
		TestProject project = new TestProject("OtherProject");
		try {
			BTestUtils.configure(project);
			PdeProjectConversionUtils.convertToPDE(project.getProject(), null);
			waitForAutoBuild();
			//
			IJavaProject javaProject = project.getJavaProject();
			//
			assertFalse(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.observable.IObservable"));
			assertFalse(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.beans.IBeanObservable"));
			assertFalse(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.jface.databinding.swt.ISWTObservable"));
			//
			DataBindingsCodeUtils.ensureDBLibraries(javaProject);
			//
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.observable.IObservable"));
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.beans.IBeanObservable"));
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.jface.databinding.swt.ISWTObservable"));
			//
			DataBindingsCodeUtils.ensureDBLibraries(javaProject);
			//
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.observable.IObservable"));
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.core.databinding.beans.IBeanObservable"));
			assertTrue(ProjectUtils.hasType(
					javaProject,
					"org.eclipse.jface.databinding.swt.ISWTObservable"));
		} finally {
			project.dispose();
			waitForAutoBuild();
		}
	}

	@Test
	public void test_DataBindingsCodeUtils_ensureEnclosingRealmOfMain_1() throws Exception {
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
		DataBindingsCodeUtils.ensureEnclosingRealmOfMain(m_lastEditor);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  public static void main(String[] args) {",
				"    Display display = Display.getDefault();",
				"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
				"      public void run() {",
				"        Test test = new Test();",
				"        test.open();",
				"      }",
				"    });",
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
				"}"), m_lastEditor);
		//
		DataBindingsCodeUtils.ensureEnclosingRealmOfMain(m_lastEditor);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  public static void main(String[] args) {",
				"    Display display = Display.getDefault();",
				"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
				"      public void run() {",
				"        Test test = new Test();",
				"        test.open();",
				"      }",
				"    });",
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
				"}"), m_lastEditor);
	}

	@Test
	public void test_DataBindingsCodeUtils_ensureEnclosingRealmOfMain_2() throws Exception {
		CompositeInfo composite =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
				"}"});
		assertNotNull(composite);
		//
		DataBindingsCodeUtils.ensureEnclosingRealmOfMain(m_lastEditor);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_DataBindingsCodeUtils_getLastInfoDeclaration_1() throws Exception {
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
		TypeDeclaration type = JavaInfoUtils.getTypeDeclaration(shell);
		MethodDeclaration createContentsMethod =
				AstNodeUtils.getMethodBySignature(type, "createContents()");
		assertNotNull(createContentsMethod);
		//
		assertSame(createContentsMethod, DataBindingsCodeUtils.getLastInfoDeclaration(null, shell));
	}

	@Test
	public void test_DataBindingsCodeUtils_getLastInfoDeclaration_2() throws Exception {
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
						"    m_shell = new Shell();",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		TypeDeclaration type = JavaInfoUtils.getTypeDeclaration(shell);
		MethodDeclaration openMethod = AstNodeUtils.getMethodBySignature(type, "open()");
		assertNotNull(openMethod);
		//
		assertSame(openMethod, DataBindingsCodeUtils.getLastInfoDeclaration(null, shell));
	}

	@Test
	public void test_DataBindingsCodeUtils_getLastInfoDeclaration_3() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  public static void main(String[] args) {",
						"    Display display = new Display();",
						"    Shell shell = new Shell();",
						"    shell.open();",
						"    shell.layout();",
						"    while (!shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		TypeDeclaration type = JavaInfoUtils.getTypeDeclaration(shell);
		MethodDeclaration mainMethod =
				AstNodeUtils.getMethodBySignature(type, "main(java.lang.String[])");
		assertNotNull(mainMethod);
		//
		assertSame(mainMethod, DataBindingsCodeUtils.getLastInfoDeclaration(null, shell));
	}

	@Test
	public void test_ensureInvokeInitDataBindings_1() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  public static void main(String[] args) {",
						"    Display display = new Display();",
						"    Shell shell = new Shell();",
						"    shell.open();",
						"    shell.layout();",
						"    while (!shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
						"  protected static DataBindingContext initDataBindings() {",
						"    return null;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		TypeDeclaration type = JavaInfoUtils.getTypeDeclaration(shell);
		MethodDeclaration lastInfoDeclaration =
				DataBindingsCodeUtils.getLastInfoDeclaration(
						AstNodeUtils.getMethodBySignature(type, "initDataBindings()"),
						shell);
		//
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				shell,
				m_lastEditor,
				type,
				lastInfoDeclaration);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  public static void main(String[] args) {",
				"    Display display = new Display();",
				"    Shell shell = new Shell();",
				"    initDataBindings();",
				"    shell.open();",
				"    shell.layout();",
				"    while (!shell.isDisposed()) {",
				"      if (!display.readAndDispatch()) {",
				"        display.sleep();",
				"      }",
				"    }",
				"  }",
				"  protected static DataBindingContext initDataBindings() {",
				"    return null;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_ensureInvokeInitDataBindings_2() throws Exception {
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
						"    m_shell = new Shell();",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
						"  protected DataBindingContext initDataBindings() {",
						"    return null;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		TypeDeclaration type = JavaInfoUtils.getTypeDeclaration(shell);
		MethodDeclaration lastInfoDeclaration =
				DataBindingsCodeUtils.getLastInfoDeclaration(
						AstNodeUtils.getMethodBySignature(type, "initDataBindings()"),
						shell);
		//
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				shell,
				m_lastEditor,
				type,
				lastInfoDeclaration);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
				"  protected Shell m_shell;",
				"  public static void main(String[] args) {",
				"    Test test = new Test();",
				"    test.open();",
				"  }",
				"  public void open() {",
				"    Display display = new Display();",
				"    m_shell = new Shell();",
				"    m_bindingContext = initDataBindings();",
				"    m_shell.open();",
				"    m_shell.layout();",
				"    while (!m_shell.isDisposed()) {",
				"      if (!display.readAndDispatch()) {",
				"        display.sleep();",
				"      }",
				"    }",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    return null;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_ensureInvokeInitDataBindings_3() throws Exception {
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
						"  protected DataBindingContext initDataBindings() {",
						"    return null;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		TypeDeclaration type = JavaInfoUtils.getTypeDeclaration(shell);
		MethodDeclaration lastInfoDeclaration =
				DataBindingsCodeUtils.getLastInfoDeclaration(
						AstNodeUtils.getMethodBySignature(type, "initDataBindings()"),
						shell);
		//
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				shell,
				m_lastEditor,
				type,
				lastInfoDeclaration);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
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
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    return null;",
				"  }",
				"}"), m_lastEditor);
		// -------------
		type = JavaInfoUtils.getTypeDeclaration(shell);
		lastInfoDeclaration =
				DataBindingsCodeUtils.getLastInfoDeclaration(
						AstNodeUtils.getMethodBySignature(type, "initDataBindings()"),
						shell);
		//
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				shell,
				m_lastEditor,
				type,
				lastInfoDeclaration);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
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
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    return null;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_ensureInvokeInitDataBindings_4() throws Exception {
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
						"  protected boolean createContents() {",
						"    m_shell = new Shell();",
						"    return true;",
						"  }",
						"  protected DataBindingContext initDataBindings() {",
						"    return null;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		TypeDeclaration type = JavaInfoUtils.getTypeDeclaration(shell);
		MethodDeclaration lastInfoDeclaration =
				DataBindingsCodeUtils.getLastInfoDeclaration(
						AstNodeUtils.getMethodBySignature(type, "initDataBindings()"),
						shell);
		//
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				shell,
				m_lastEditor,
				type,
				lastInfoDeclaration);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
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
				"  protected boolean createContents() {",
				"    m_shell = new Shell();",
				"    m_bindingContext = initDataBindings();",
				"    return true;",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    return null;",
				"  }",
				"}"), m_lastEditor);
		// -------------
		type = JavaInfoUtils.getTypeDeclaration(shell);
		lastInfoDeclaration =
				DataBindingsCodeUtils.getLastInfoDeclaration(
						AstNodeUtils.getMethodBySignature(type, "initDataBindings()"),
						shell);
		//
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				shell,
				m_lastEditor,
				type,
				lastInfoDeclaration);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
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
				"  protected boolean createContents() {",
				"    m_shell = new Shell();",
				"    m_bindingContext = initDataBindings();",
				"    return true;",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    return null;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_ensureInvokeInitDataBindings_5() throws Exception {
		CompositeInfo composite =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  protected DataBindingContext initDataBindings() {",
						"    return null;",
						"  }",
				"}"});
		assertNotNull(composite);
		//
		TypeDeclaration type = JavaInfoUtils.getTypeDeclaration(composite);
		MethodDeclaration lastInfoDeclaration =
				DataBindingsCodeUtils.getLastInfoDeclaration(
						AstNodeUtils.getMethodBySignature(type, "initDataBindings()"),
						composite);
		//
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				composite,
				m_lastEditor,
				type,
				lastInfoDeclaration);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test extends Composite {",
				"  private DataBindingContext m_bindingContext;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    return null;",
				"  }",
				"}"), m_lastEditor);
		// -------------
		type = JavaInfoUtils.getTypeDeclaration(composite);
		lastInfoDeclaration =
				DataBindingsCodeUtils.getLastInfoDeclaration(
						AstNodeUtils.getMethodBySignature(type, "initDataBindings()"),
						composite);
		//
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				composite,
				m_lastEditor,
				type,
				lastInfoDeclaration);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test extends Composite {",
				"  private DataBindingContext m_bindingContext;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    return null;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_JavaInfoDeleteManager() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private Spinner m_spinner;",
						"  private Text m_text;",
						"  private Combo m_combo;",
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
						"    m_shell.setLayout(new FillLayout());",
						"    m_spinner = new Spinner(m_shell, SWT.NONE);",
						"    m_text = new Text(m_shell, SWT.NONE);",
						"    m_combo = new Combo(m_shell, SWT.NONE);",
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  protected DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    //",
						"    IObservableValue widget0 = WidgetProperties.enabled().observe(m_text);",
						"    IObservableValue widget1 = WidgetProperties.enabled().observe(m_combo);",
						"    bindingContext.bindValue(widget0, widget1, null, null);",
						"    //",
						"    return bindingContext;",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		shell.getChildrenControls().get(0).delete();
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Text m_text;",
				"  private Combo m_combo;",
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
				"    m_shell.setLayout(new FillLayout());",
				"    m_text = new Text(m_shell, SWT.NONE);",
				"    m_combo = new Combo(m_shell, SWT.NONE);",
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    //",
				"    IObservableValue widget0 = WidgetProperties.enabled().observe(m_text);",
				"    IObservableValue widget1 = WidgetProperties.enabled().observe(m_combo);",
				"    bindingContext.bindValue(widget0, widget1, null, null);",
				"    //",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
		//
		shell.getChildrenControls().get(0).delete();
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  protected Shell m_shell;",
				"  private Combo m_combo;",
				"  private DataBindingContext m_bindingContext;",
				"  public static void main(String[] args) {",
				"    Display display = Display.getDefault();",
				"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
				"      public void run() {",
				"        Test test = new Test();",
				"        test.open();",
				"      }",
				"    });",
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
				"    m_combo = new Combo(m_shell, SWT.NONE);",
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    //",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_totalCodeGeneration_1() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private Text m_text;",
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
						"    m_text = new Text(m_shell, SWT.NONE);",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		WidgetBindableInfo shellObserve =
				(WidgetBindableInfo) provider.getObserves(ObserveType.WIDGETS).get(0);
		WidgetPropertyBindableInfo shellTextProperty =
				(WidgetPropertyBindableInfo) shellObserve.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(8);
		//
		WidgetBindableInfo textObserve =
				(WidgetBindableInfo) shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		WidgetPropertyBindableInfo textTextProperty =
				(WidgetPropertyBindableInfo) textObserve.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(10);
		//
		SwtObservableInfo target = new SwtObservableInfo(shellObserve, shellTextProperty);
		target.setCodeSupport(new WidgetPropertiesCodeSupport("observeText"));
		TextSwtObservableInfo model = new TextSwtObservableInfo(textObserve, textTextProperty);
		model.setCodeSupport(new WidgetPropertyTextCodeSupport(new int[] {SWT.Modify}));
		ValueBindingInfo binding = new ValueBindingInfo(target, model);
		provider.addBinding(binding);
		//
		assertEditor(
				DatabindingTestUtils.getTestSource(
						"public class Test {",
						"  private DataBindingContext m_bindingContext;",
						"  protected Shell m_shell;",
						"  private Text m_text;",
						"  public static void main(String[] args) {",
						"    Display display = Display.getDefault();",
						"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
						"      public void run() {",
						"        Test test = new Test();",
						"        test.open();",
						"      }",
						"    });",
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
						"    m_text = new Text(m_shell, SWT.NONE);",
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  protected DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    //",
						"    IObservableValue observeTextShellObserveWidget = WidgetProperties.text().observe(m_shell);",
						"    IObservableValue observeTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_text);",
						"    bindingContext.bindValue(observeTextShellObserveWidget, observeTextTextObserveWidget, null, null);",
						"    //",
						"    return bindingContext;",
						"  }",
						"}"),
				m_lastEditor);
		//
		target.setVariableIdentifier("target");
		model.setVariableIdentifier("model");
		//
		provider.editBinding(binding);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
				"  protected Shell m_shell;",
				"  private Text m_text;",
				"  public static void main(String[] args) {",
				"    Display display = Display.getDefault();",
				"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
				"      public void run() {",
				"        Test test = new Test();",
				"        test.open();",
				"      }",
				"    });",
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
				"    m_text = new Text(m_shell, SWT.NONE);",
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    //",
				"    IObservableValue target = WidgetProperties.text().observe(m_shell);",
				"    IObservableValue model = WidgetProperties.text(SWT.Modify).observe(m_text);",
				"    bindingContext.bindValue(target, model, null, null);",
				"    //",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
		//
		provider.deleteBinding(binding);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
				"  protected Shell m_shell;",
				"  private Text m_text;",
				"  public static void main(String[] args) {",
				"    Display display = Display.getDefault();",
				"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
				"      public void run() {",
				"        Test test = new Test();",
				"        test.open();",
				"      }",
				"    });",
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
				"    m_text = new Text(m_shell, SWT.NONE);",
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    //",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_totalCodeGeneration_2() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  protected Shell m_shell;",
						"  private Text m_text;",
						"  public static void main(String[] args) {",
						"    Test test = new Test();",
						"    test.open();",
						"  }",
						"  public void open() {",
						"    Display display = new Display();",
						"    m_shell = new Shell();",
						"    m_shell.setLayout(new FillLayout());",
						"    m_text = new Text(m_shell, SWT.NONE);",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		WidgetBindableInfo shellObserve =
				(WidgetBindableInfo) provider.getObserves(ObserveType.WIDGETS).get(0);
		WidgetPropertyBindableInfo shellTextProperty =
				(WidgetPropertyBindableInfo) shellObserve.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(8);
		//
		WidgetBindableInfo textObserve =
				(WidgetBindableInfo) shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		WidgetPropertyBindableInfo textTextProperty =
				(WidgetPropertyBindableInfo) textObserve.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(10);
		//
		SwtObservableInfo target = new SwtObservableInfo(shellObserve, shellTextProperty);
		target.setCodeSupport(new WidgetPropertiesCodeSupport("observeText"));
		TextSwtObservableInfo model = new TextSwtObservableInfo(textObserve, textTextProperty);
		model.setCodeSupport(new WidgetPropertyTextCodeSupport(new int[] { SWT.Modify }));
		ValueBindingInfo binding = new ValueBindingInfo(target, model);
		provider.addBinding(binding);
		//
		assertEditor(
				DatabindingTestUtils.getTestSource(
						"public class Test {",
						"  private DataBindingContext m_bindingContext;",
						"  protected Shell m_shell;",
						"  private Text m_text;",
						"  public static void main(String[] args) {",
						"    Display display = Display.getDefault();",
						"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
						"      public void run() {",
						"        Test test = new Test();",
						"        test.open();",
						"      }",
						"    });",
						"  }",
						"  public void open() {",
						"    Display display = new Display();",
						"    m_shell = new Shell();",
						"    m_shell.setLayout(new FillLayout());",
						"    m_text = new Text(m_shell, SWT.NONE);",
						"    m_bindingContext = initDataBindings();",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
						"  protected DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    //",
						"    IObservableValue observeTextShellObserveWidget = WidgetProperties.text().observe(m_shell);",
						"    IObservableValue observeTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_text);",
						"    bindingContext.bindValue(observeTextShellObserveWidget, observeTextTextObserveWidget, null, null);",
						"    //",
						"    return bindingContext;",
						"  }",
						"}"),
				m_lastEditor);
		//
		target.setVariableIdentifier("target");
		model.setVariableIdentifier("model");
		//
		provider.editBinding(binding);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
				"  protected Shell m_shell;",
				"  private Text m_text;",
				"  public static void main(String[] args) {",
				"    Display display = Display.getDefault();",
				"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
				"      public void run() {",
				"        Test test = new Test();",
				"        test.open();",
				"      }",
				"    });",
				"  }",
				"  public void open() {",
				"    Display display = new Display();",
				"    m_shell = new Shell();",
				"    m_shell.setLayout(new FillLayout());",
				"    m_text = new Text(m_shell, SWT.NONE);",
				"    m_bindingContext = initDataBindings();",
				"    m_shell.open();",
				"    m_shell.layout();",
				"    while (!m_shell.isDisposed()) {",
				"      if (!display.readAndDispatch()) {",
				"        display.sleep();",
				"      }",
				"    }",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    //",
				"    IObservableValue target = WidgetProperties.text().observe(m_shell);",
				"    IObservableValue model = WidgetProperties.text(SWT.Modify).observe(m_text);",
				"    bindingContext.bindValue(target, model, null, null);",
				"    //",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
		//
		provider.deleteAllBindings();
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private DataBindingContext m_bindingContext;",
				"  protected Shell m_shell;",
				"  private Text m_text;",
				"  public static void main(String[] args) {",
				"    Display display = Display.getDefault();",
				"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
				"      public void run() {",
				"        Test test = new Test();",
				"        test.open();",
				"      }",
				"    });",
				"  }",
				"  public void open() {",
				"    Display display = new Display();",
				"    m_shell = new Shell();",
				"    m_shell.setLayout(new FillLayout());",
				"    m_text = new Text(m_shell, SWT.NONE);",
				"    m_bindingContext = initDataBindings();",
				"    m_shell.open();",
				"    m_shell.layout();",
				"    while (!m_shell.isDisposed()) {",
				"      if (!display.readAndDispatch()) {",
				"        display.sleep();",
				"      }",
				"    }",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    //",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_totalCodeGeneration_3() throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test {",
						"  private static Shell m_shell;",
						"  private static Text m_text;",
						"  public static void main(String[] args) {",
						"    Display display = new Display();",
						"    m_shell = new Shell();",
						"    m_shell.setLayout(new FillLayout());",
						"    m_text = new Text(m_shell, SWT.NONE);",
						"    m_shell.open();",
						"    m_shell.layout();",
						"    while (!m_shell.isDisposed()) {",
						"      if (!display.readAndDispatch()) {",
						"        display.sleep();",
						"      }",
						"    }",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		WidgetBindableInfo shellObserve =
				(WidgetBindableInfo) provider.getObserves(ObserveType.WIDGETS).get(0);
		WidgetPropertyBindableInfo shellTextProperty =
				(WidgetPropertyBindableInfo) shellObserve.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(8);
		//
		WidgetBindableInfo textObserve =
				(WidgetBindableInfo) shellObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(0);
		WidgetPropertyBindableInfo textTextProperty =
				(WidgetPropertyBindableInfo) textObserve.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(10);
		//
		SwtObservableInfo target = new SwtObservableInfo(shellObserve, shellTextProperty);
		target.setCodeSupport(new WidgetPropertiesCodeSupport("observeText"));
		TextSwtObservableInfo model = new TextSwtObservableInfo(textObserve, textTextProperty);
		model.setCodeSupport(new WidgetPropertyTextCodeSupport(new int[] { SWT.Modify }));
		ValueBindingInfo binding = new ValueBindingInfo(target, model);
		provider.addBinding(binding);
		//
		assertEditor(
				DatabindingTestUtils.getTestSource(
						"public class Test {",
						"  private static Shell m_shell;",
						"  private static Text m_text;",
						"  public static void main(String[] args) {",
						"    Display display = Display.getDefault();",
						"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
						"      public void run() {",
						"        Display display = new Display();",
						"        m_shell = new Shell();",
						"        m_shell.setLayout(new FillLayout());",
						"        m_text = new Text(m_shell, SWT.NONE);",
						"        initDataBindings();",
						"        m_shell.open();",
						"        m_shell.layout();",
						"        while (!m_shell.isDisposed()) {",
						"          if (!display.readAndDispatch()) {",
						"            display.sleep();",
						"          }",
						"        }",
						"      }",
						"    });",
						"  }",
						"  protected static DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    //",
						"    IObservableValue observeTextShellObserveWidget = WidgetProperties.text().observe(m_shell);",
						"    IObservableValue observeTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_text);",
						"    bindingContext.bindValue(observeTextShellObserveWidget, observeTextTextObserveWidget, null, null);",
						"    //",
						"    return bindingContext;",
						"  }",
						"}"),
				m_lastEditor);
		//
		target.setVariableIdentifier("target");
		model.setVariableIdentifier("model");
		//
		provider.editBinding(binding);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test {",
				"  private static Shell m_shell;",
				"  private static Text m_text;",
				"  public static void main(String[] args) {",
				"    Display display = Display.getDefault();",
				"    Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {",
				"      public void run() {",
				"        Display display = new Display();",
				"        m_shell = new Shell();",
				"        m_shell.setLayout(new FillLayout());",
				"        m_text = new Text(m_shell, SWT.NONE);",
				"        initDataBindings();",
				"        m_shell.open();",
				"        m_shell.layout();",
				"        while (!m_shell.isDisposed()) {",
				"          if (!display.readAndDispatch()) {",
				"            display.sleep();",
				"          }",
				"        }",
				"      }",
				"    });",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    //",
				"    IObservableValue target = WidgetProperties.text().observe(m_shell);",
				"    IObservableValue model = WidgetProperties.text(SWT.Modify).observe(m_text);",
				"    bindingContext.bindValue(target, model, null, null);",
				"    //",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
	}

	@Test
	public void test_totalCodeGeneration_4() throws Exception {
		CompositeInfo composite =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"public class Test extends Composite {",
						"  private Text m_text;",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new FillLayout());",
						"    m_text = new Text(this, SWT.NONE);",
						"  }",
				"}"});
		assertNotNull(composite);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		//
		WidgetBindableInfo compositeObserve =
				(WidgetBindableInfo) provider.getObserves(ObserveType.WIDGETS).get(0);
		WidgetPropertyBindableInfo compositeEnabledProperty =
				(WidgetPropertyBindableInfo) compositeObserve.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(2);
		//
		WidgetBindableInfo textObserve =
				(WidgetBindableInfo) compositeObserve.getChildren(ChildrenContext.ChildrenForMasterTable).get(
						0);
		WidgetPropertyBindableInfo textTextProperty =
				(WidgetPropertyBindableInfo) textObserve.getChildren(
						ChildrenContext.ChildrenForPropertiesTable).get(10);
		//
		SwtObservableInfo target = new SwtObservableInfo(compositeObserve, compositeEnabledProperty);
		target.setCodeSupport(new WidgetPropertiesCodeSupport("observeText"));
		TextSwtObservableInfo model = new TextSwtObservableInfo(textObserve, textTextProperty);
		model.setCodeSupport(new WidgetPropertyTextCodeSupport(new int[] { SWT.Modify }));
		ValueBindingInfo binding = new ValueBindingInfo(target, model);
		provider.addBinding(binding);
		//
		assertEditor(
				DatabindingTestUtils.getTestSource(
						"public class Test extends Composite {",
						"  private DataBindingContext m_bindingContext;",
						"  private Text m_text;",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new FillLayout());",
						"    m_text = new Text(this, SWT.NONE);",
						"    m_bindingContext = initDataBindings();",
						"  }",
						"  protected DataBindingContext initDataBindings() {",
						"    DataBindingContext bindingContext = new DataBindingContext();",
						"    //",
						"    IObservableValue observeEnabledThisObserveWidget = WidgetProperties.enabled().observe(this);",
						"    IObservableValue observeTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_text);",
						"    bindingContext.bindValue(observeEnabledThisObserveWidget, observeTextTextObserveWidget, null, null);",
						"    //",
						"    return bindingContext;",
						"  }",
						"}"),
				m_lastEditor);
		//
		target.setVariableIdentifier("target");
		model.setVariableIdentifier("model");
		//
		provider.editBinding(binding);
		//
		assertEditor(DatabindingTestUtils.getTestSource(
				"public class Test extends Composite {",
				"  private DataBindingContext m_bindingContext;",
				"  private Text m_text;",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    m_text = new Text(this, SWT.NONE);",
				"    m_bindingContext = initDataBindings();",
				"  }",
				"  protected DataBindingContext initDataBindings() {",
				"    DataBindingContext bindingContext = new DataBindingContext();",
				"    //",
				"    IObservableValue target = WidgetProperties.enabled().observe(this);",
				"    IObservableValue model = WidgetProperties.text(SWT.Modify).observe(m_text);",
				"    bindingContext.bindValue(target, model, null, null);",
				"    //",
				"    return bindingContext;",
				"  }",
				"}"), m_lastEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static void assertSWTObservableInfo(WidgetBindableInfo widget,
			WidgetPropertyBindableInfo property,
			ObservableCodeSupport codeSupport,
			String variable,
			String line1,
			String line2,
			String line3) throws Exception {
		assertSWTObservableInfo(
				new SwtObservableInfo(widget, property),
				codeSupport,
				variable,
				line1,
				line2,
				line3);
	}

	private static void assertSWTObservableInfo(SwtObservableInfo observable,
			ObservableCodeSupport codeSupport,
			String variable,
			String line1,
			String line2,
			String line3) throws Exception {
		//
		assertObservableInfo(observable, codeSupport, variable, line1, line2);
		//
		observable.setDelayValue(100);
		//
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, observable);
		observable.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(lines, line3);
	}

	private static void assertObservableInfo(ObservableInfo observable,
			ObservableCodeSupport codeSupport,
			String variable,
			String line1,
			String line2) throws Exception {
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, observable);
		//
		observable.setCodeSupport(codeSupport);
		observable.setVariableIdentifier(variable);
		observable.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(lines, line1);
		//
		lines.clear();
		observable.setVariableIdentifier(null);
		observable.addSourceCode(lines, generationSupport);
		//
		assertEqualsLines(lines, line2);
	}

	private static void assertStrategy(UpdateStrategyInfo strategy,
			String expectedSourceCode,
			String... expectedLines) throws Exception {
		List<String> lines = new ArrayList<>();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, strategy);
		//
		assertEquals(expectedSourceCode, strategy.getSourceCode(lines, generationSupport));
		assertEquals(expectedLines.length, lines.size());
		for (int i = 0; i < expectedLines.length; i++) {
			assertEquals("line:" + i, expectedLines[i], lines.get(i));
		}
	}

	private static UpdateValueStrategyInfo createValueStrategy(UpdateStrategyInfo.StrategyType strategyType,
			Object strategyValue,
			ConverterInfo converter,
			String variable) {
		UpdateValueStrategyInfo strategy =
				new UpdateValueStrategyInfo(strategyType, strategyValue, converter);
		strategy.setVariableIdentifier(variable);
		return strategy;
	}

	private static UpdateListStrategyInfo createListStrategy(UpdateStrategyInfo.StrategyType strategyType,
			Object strategyValue,
			ConverterInfo converter,
			String variable) {
		UpdateListStrategyInfo strategy =
				new UpdateListStrategyInfo(strategyType, strategyValue, converter);
		strategy.setVariableIdentifier(variable);
		return strategy;
	}

	private static UpdateSetStrategyInfo createSetStrategy(UpdateStrategyInfo.StrategyType strategyType,
			Object strategyValue,
			ConverterInfo converter,
			String variable) {
		UpdateSetStrategyInfo strategy =
				new UpdateSetStrategyInfo(strategyType, strategyValue, converter);
		strategy.setVariableIdentifier(variable);
		return strategy;
	}

	private static void assertEqualsLines(List<String> actual, String... expected) throws Exception {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual.get(i));
		}
	}
}