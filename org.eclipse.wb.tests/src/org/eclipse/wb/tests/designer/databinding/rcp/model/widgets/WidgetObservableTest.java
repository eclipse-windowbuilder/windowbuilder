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
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ItemsSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.TextSwtObservableInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;
import org.eclipse.wb.tests.designer.databinding.rcp.model.AbstractBindingTest;

import org.eclipse.swt.SWT;

import org.junit.Test;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class WidgetObservableTest extends AbstractBindingTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_observeEnabled() throws Exception {
		observeControl(
				"    IObservableValue observeWidget = WidgetProperties.enabled().observe(m_shell);",
				"enabled|observeEnabled|boolean",
				"m_shell.enabled");
	}

	@Test
	public void test_observeVisible() throws Exception {
		observeControl(
				"    IObservableValue observeWidget = WidgetProperties.visible().observe(m_shell);",
				"visible|observeVisible|boolean",
				"m_shell.visible");
	}

	@Test
	public void test_observeText() throws Exception {
		observeControl(
				"    IObservableValue observeWidget = WidgetProperties.text().observe(m_shell);",
				"text|observeText|java.lang.String",
				"m_shell.text");
	}

	@Test
	public void test_observeTooltipText() throws Exception {
		observeControl(
				"    IObservableValue observeWidget = WidgetProperties.tooltipText().observe(m_shell);",
				"tooltipText|observeTooltipText|java.lang.String",
				"m_shell.tooltipText");
	}

	@Test
	public void test_observeForeground() throws Exception {
		observeControl(
				"    IObservableValue observeWidget = WidgetProperties.foreground().observe(m_shell);",
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				"m_shell.foreground");
	}

	@Test
	public void test_observeBackground() throws Exception {
		observeControl(
				"    IObservableValue observeWidget = WidgetProperties.background().observe(m_shell);",
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				"m_shell.background");
	}

	@Test
	public void test_observeFont() throws Exception {
		observeControl(
				"    IObservableValue observeWidget = WidgetProperties.font().observe(m_shell);",
				"font|observeFont|org.eclipse.swt.graphics.Font",
				"m_shell.font");
	}

	private void observeControl(String codeLine, String propertyString, String presentationString)
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
								"    IObservableValue observeValue = BeanProperties.value(\"name\").observe(getClass());",
								codeLine,
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
		assertInstanceOf(SwtObservableInfo.class, binding.getTargetObservable());
		SwtObservableInfo observable = (SwtObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeWidget", observable.getVariableIdentifier());
		assertEquals(presentationString, observable.getPresentationText());
		assertEquals(0, observable.getDelayValue());
		//
		WidgetBindableTest.assertBindable(
				shell,
				WidgetBindableInfo.class,
				null,
				false,
				"m_shell|m_shell|org.eclipse.swt.widgets.Shell",
				observable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				propertyString,
				observable.getBindableProperty());
	}

	@Test
	public void test_observeDelay() throws Exception {
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
								"    IObservableValue observeValue = BeanProperties.value(\"name\").observe(getClass());",
								"    IObservableValue observeWidget = WidgetProperties.font().observeDelayed(100, m_shell);",
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
		assertInstanceOf(SwtObservableInfo.class, binding.getTargetObservable());
		SwtObservableInfo observable = (SwtObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeWidget", observable.getVariableIdentifier());
		assertEquals("m_shell.font", observable.getPresentationText());
		assertEquals(100, observable.getDelayValue());
		//
		WidgetBindableTest.assertBindable(
				shell,
				WidgetBindableInfo.class,
				null,
				false,
				"m_shell|m_shell|org.eclipse.swt.widgets.Shell",
				observable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				observable.getBindableProperty());
	}

	@Test
	public void test_observeEditable() throws Exception {
		observeWidget(
				"  private Text m_text;",
				"    m_text = new Text(m_shell, SWT.BORDER);",
				"    IObservableValue observeWidget = WidgetProperties.editable().observe(m_text);",
				"m_text|m_text|org.eclipse.swt.widgets.Text",
				"editable|observeEditable|boolean",
				"m_text.editable");
	}

	@Test
	public void test_observeMin_Spinner() throws Exception {
		observeWidget(
				"  private Spinner m_spinner;",
				"    m_spinner = new Spinner(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.minimum().observe(m_spinner);",
				"m_spinner|m_spinner|org.eclipse.swt.widgets.Spinner",
				"minimum|observeMin|int",
				"m_spinner.minimum");
	}

	@Test
	public void test_observeMax_Spinner() throws Exception {
		observeWidget(
				"  private Spinner m_spinner;",
				"    m_spinner = new Spinner(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.maximum().observe(m_spinner);",
				"m_spinner|m_spinner|org.eclipse.swt.widgets.Spinner",
				"maximum|observeMax|int",
				"m_spinner.maximum");
	}

	@Test
	public void test_observeMin_Scale() throws Exception {
		observeWidget(
				"  private Scale m_scale;",
				"    m_scale = new Scale(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.minimum().observe(m_scale);",
				"m_scale|m_scale|org.eclipse.swt.widgets.Scale",
				"minimum|observeMin|int",
				"m_scale.minimum");
	}

	@Test
	public void test_observeMax_Scale() throws Exception {
		observeWidget(
				"  private Scale m_scale;",
				"    m_scale = new Scale(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.maximum().observe(m_scale);",
				"m_scale|m_scale|org.eclipse.swt.widgets.Scale",
				"maximum|observeMax|int",
				"m_scale.maximum");
	}

	@Test
	public void test_observeSelection_Spinner() throws Exception {
		observeWidget(
				"  private Spinner m_spinner;",
				"    m_spinner = new Spinner(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.spinnerSelection().observe(m_spinner);",
				"m_spinner|m_spinner|org.eclipse.swt.widgets.Spinner",
				"selection|observeSelection|int",
				"m_spinner.selection");
	}

	@Test
	public void test_observeSelection_Scale() throws Exception {
		observeWidget(
				"  private Scale m_scale;",
				"    m_scale = new Scale(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.scaleSelection().observe(m_scale);",
				"m_scale|m_scale|org.eclipse.swt.widgets.Scale",
				"selection|observeSelection|int",
				"m_scale.selection");
	}

	@Test
	public void test_observeSelection_Button() throws Exception {
		observeWidget(
				"  private Button m_button;",
				"    m_button = new Button(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.buttonSelection().observe(m_button);",
				"m_button|m_button|org.eclipse.swt.widgets.Button",
				"selection|observeSelection|boolean",
				"m_button.selection");
	}

	@Test
	public void test_observeSelection_Combo() throws Exception {
		observeWidget(
				"  private Combo m_combo;",
				"    m_combo = new Combo(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.comboSelection().observe(m_combo);",
				"m_combo|m_combo|org.eclipse.swt.widgets.Combo",
				"selection|observeSelection|java.lang.String",
				"m_combo.selection");
	}

	@Test
	public void test_observeSelection_CCombo() throws Exception {
		observeWidget(
				"  private CCombo m_combo;",
				"    m_combo = new CCombo(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.ccomboSelection().observe(m_combo);",
				"m_combo|m_combo|org.eclipse.swt.custom.CCombo",
				"selection|observeSelection|java.lang.String",
				"m_combo.selection");
	}

	@Test
	public void test_observeSelection_List() throws Exception {
		observeWidget(
				"  private List m_list;",
				"    m_list = new List(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.listSelection().observe(m_list);",
				"m_list|m_list|org.eclipse.swt.widgets.List",
				"selection|observeSelection|java.lang.String",
				"m_list.selection");
	}

	@Test
	public void test_observeSingleSelectionIndex_Combo() throws Exception {
		observeWidget(
				"  private Combo m_combo;",
				"    m_combo = new Combo(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.singleSelectionIndex().observe(m_combo);",
				"m_combo|m_combo|org.eclipse.swt.widgets.Combo",
				"singleSelectionIndex|observeSingleSelectionIndex|int",
				"m_combo.singleSelectionIndex");
	}

	@Test
	public void test_observeSingleSelectionIndex_CCombo() throws Exception {
		observeWidget(
				"  private CCombo m_combo;",
				"    m_combo = new CCombo(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.singleSelectionIndex().observe(m_combo);",
				"m_combo|m_combo|org.eclipse.swt.custom.CCombo",
				"singleSelectionIndex|observeSingleSelectionIndex|int",
				"m_combo.singleSelectionIndex");
	}

	@Test
	public void test_observeSingleSelectionIndex_List() throws Exception {
		observeWidget(
				"  private List m_list;",
				"    m_list = new List(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.singleSelectionIndex().observe(m_list);",
				"m_list|m_list|org.eclipse.swt.widgets.List",
				"singleSelectionIndex|observeSingleSelectionIndex|int",
				"m_list.singleSelectionIndex");
	}

	@Test
	public void test_observeSingleSelectionIndex_Table() throws Exception {
		observeWidget(
				"  private Table m_table;",
				"    m_table = new Table(m_shell, SWT.NONE);",
				"    IObservableValue observeWidget = WidgetProperties.singleSelectionIndex().observe(m_table);",
				"m_table|m_table|org.eclipse.swt.widgets.Table",
				"singleSelectionIndex|observeSingleSelectionIndex|int",
				"m_table.singleSelectionIndex");
	}

	private void observeWidget(String fieldLine,
			String creationLine,
			String observeLine,
			String widgetTest,
			String propertyTest,
			String presentationString) throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								fieldLine,
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
								creationLine,
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableValue observeValue = BeanProperties.value(\"name\").observe(getClass());",
								observeLine,
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
		assertInstanceOf(SwtObservableInfo.class, binding.getTargetObservable());
		SwtObservableInfo observable = (SwtObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeWidget", observable.getVariableIdentifier());
		assertEquals(presentationString, observable.getPresentationText());
		//
		WidgetBindableTest.assertBindable(
				shell.getChildrenControls().get(0),
				WidgetBindableInfo.class,
				provider.getObserves(ObserveType.WIDGETS).get(0),
				false,
				widgetTest,
				observable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				propertyTest,
				observable.getBindableProperty());
	}

	@Test
	public void test_observeText_Modify() throws Exception {
		observeText(
				"    IObservableValue observeWidget = WidgetProperties.text(SWT.Modify).observe(m_text);",
				SWT.Modify,
				0,
				"m_text.text(SWT.Modify)");
	}

	@Test
	public void test_observeText_FocusOut() throws Exception {
		observeText(
				"    IObservableValue observeWidget = WidgetProperties.text(SWT.FocusOut).observe(m_text);",
				SWT.FocusOut,
				1,
				"m_text.text(SWT.FocusOut)");
	}

	@Test
	public void test_observeText_NONE() throws Exception {
		observeText(
				"    IObservableValue observeWidget = WidgetProperties.text(SWT.NONE).observe(m_text);",
				SWT.NONE,
				2,
				"m_text.text(SWT.NONE)");
	}

	@Test
	public void test_observeText_None() throws Exception {
		observeText(
				"    IObservableValue observeWidget = WidgetProperties.text(SWT.None).observe(m_text);",
				SWT.None,
				2,
				"m_text.text(SWT.NONE)");
	}

	private void observeText(String codeLine,
			int updateEventType,
			int updateEventTypeIndex,
			String presentationString) throws Exception {
		CompositeInfo shell =
				DatabindingTestUtils.parseTestSource(
						this,
						new String[]{
								"public class Test {",
								"  protected Shell m_shell;",
								"  private Text m_text;",
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
								"    m_text = new Text(m_shell, SWT.BORDER);",
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableValue observeValue = BeanProperties.value(\"name\").observe(getClass());",
								codeLine,
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
		assertInstanceOf(TextSwtObservableInfo.class, binding.getTargetObservable());
		TextSwtObservableInfo observable = (TextSwtObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeWidget", observable.getVariableIdentifier());
		assertEquals(presentationString, observable.getPresentationText());
		//
		WidgetBindableTest.assertBindable(
				shell.getChildrenControls().get(0),
				WidgetBindableInfo.class,
				provider.getObserves(ObserveType.WIDGETS).get(0),
				false,
				"m_text|m_text|org.eclipse.swt.widgets.Text",
				observable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"text|observeText|java.lang.String",
				observable.getBindableProperty());
		//
		//assertEquals(updateEventType, observable.getUpdateEventType()); XXX
		//assertEquals(updateEventTypeIndex, observable.getUpdateEventTypeIndex()); XXX
	}

	@Test
	public void test_z_observeItems_Combo() throws Exception {
		observeItems(
				"  private Combo m_combo;",
				"    m_combo = new Combo(m_shell, SWT.NONE);",
				"    IObservableList observeWidget = WidgetProperties.items().observe(m_combo);",
				"m_combo|m_combo|org.eclipse.swt.widgets.Combo",
				"m_combo.items");
	}

	@Test
	public void test_z_observeItems_CCombo() throws Exception {
		observeItems(
				"  private CCombo m_combo;",
				"    m_combo = new CCombo(m_shell, SWT.NONE);",
				"    IObservableList observeWidget = WidgetProperties.items().observe(m_combo);",
				"m_combo|m_combo|org.eclipse.swt.custom.CCombo",
				"m_combo.items");
	}

	@Test
	public void test_z_observeItems_List() throws Exception {
		observeItems(
				"  private List m_list;",
				"    m_list = new List(m_shell, SWT.NONE);",
				"    IObservableList observeWidget = WidgetProperties.items().observe(m_list);",
				"m_list|m_list|org.eclipse.swt.widgets.List",
				"m_list.items");
	}

	private void observeItems(String fieldLine,
			String widgetLine,
			String observeLine,
			String widgetTest,
			String presentationString) throws Exception {
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
								fieldLine,
								"  private DataBindingContext m_bindingContext;",
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
								widgetLine,
								"    m_bindingContext = initDataBindings();",
								"  }",
								"  private DataBindingContext initDataBindings() {",
								"    IObservableList observeList = BeanProperties.list(\"names\").observe(Realm.getDefault(), m_bean);",
								observeLine,
								"    DataBindingContext bindingContext = new DataBindingContext();",
								"    bindingContext.bindList(observeWidget, observeList, null, null);",
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
		assertInstanceOf(ItemsSwtObservableInfo.class, binding.getTargetObservable());
		ItemsSwtObservableInfo observable = (ItemsSwtObservableInfo) binding.getTargetObservable();
		//
		assertEquals("observeWidget", observable.getVariableIdentifier());
		assertEquals(presentationString, observable.getPresentationText());
		//
		WidgetBindableTest.assertBindable(
				shell.getChildrenControls().get(0),
				WidgetBindableInfo.class,
				provider.getObserves(ObserveType.WIDGETS).get(0),
				false,
				widgetTest,
				observable.getBindableObject());
		//
		WidgetBindableTest.assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"items|observeItems|java.util.List",
				observable.getBindableProperty());
	}
}