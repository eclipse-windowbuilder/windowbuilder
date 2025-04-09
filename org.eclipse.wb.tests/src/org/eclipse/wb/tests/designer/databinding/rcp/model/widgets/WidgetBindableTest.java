/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.databinding.rcp.model.widgets;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory.Type;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.WidgetsObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.model.jface.DialogInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.databinding.rcp.model.AbstractBindingTest;

import org.junit.Test;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class WidgetBindableTest extends AbstractBindingTest {
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
	@Test
	public void test_widgets() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import java.io.File;",
						"public class Test {",
						"  protected Shell m_shell;",
						"  private Label m_label;",
						"  private Button m_button;",
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
						"    m_shell.setLayout(new GridLayout());",
						"    m_label = new Label(m_shell, SWT.NONE);",
						"    m_button = new Button(m_shell, SWT.NONE);",
						"    m_text = new Text(m_shell, SWT.BORDER);",
						"  }",
						"}");
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(WidgetsObserveTypeContainer.class, provider.getContainers().get(1));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.WIDGETS);
		assertNotNull(observes);
		assertEquals(1, observes.size());
		//
		assertBindable(
				shell,
				WidgetBindableInfo.class,
				null,
				true,
				"m_shell|m_shell|org.eclipse.swt.widgets.Shell",
				observes.get(0));
		//
		List<ControlInfo> widgetChildren = shell.getChildrenControls();
		//
		List<IObserveInfo> children =
				observes.get(0).getChildren(ChildrenContext.ChildrenForMasterTable);
		assertEquals(3, children.size());
		//
		assertBindable(
				widgetChildren.get(0),
				WidgetBindableInfo.class,
				observes.get(0),
				false,
				"m_label|m_label|org.eclipse.swt.widgets.Label",
				children.get(0));
		//
		assertBindable(
				widgetChildren.get(1),
				WidgetBindableInfo.class,
				observes.get(0),
				false,
				"m_button|m_button|org.eclipse.swt.widgets.Button",
				children.get(1));
		//
		assertBindable(
				widgetChildren.get(2),
				WidgetBindableInfo.class,
				observes.get(0),
				false,
				"m_text|m_text|org.eclipse.swt.widgets.Text",
				children.get(2));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_widget_Shell_properties() throws Exception {
		WidgetBindableInfo root = parseBindings("  //", "    //");
		List<IObserveInfo> properties = root.getChildren(ChildrenContext.ChildrenForPropertiesTable);
		//
		widget_Link_Shell_properties(properties);
	}

	@Test
	public void test_widget_Label_properties() throws Exception {
		widget_Label_CLabel_properties(
				"  private Label m_label;",
				"    m_label = new Label(m_shell, SWT.NONE);");
	}

	@Test
	public void test_widget_CLabel_properties() throws Exception {
		widget_Label_CLabel_properties(
				"  private CLabel m_clabel;",
				"    m_clabel = new CLabel(m_shell, SWT.NONE);");
	}

	@Test
	public void test_widget_Link_properties() throws Exception {
		WidgetBindableInfo root =
				parseBindings("  private Link m_link;", "    m_link = new Link(m_shell, SWT.NONE);");
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		//
		widget_Link_Shell_properties(properties);
	}

	private void widget_Label_CLabel_properties(String fieldLine, String createLine) throws Exception {
		WidgetBindableInfo root = parseBindings(fieldLine, createLine);
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		//
		assertEquals(12, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"image|observeImage|org.eclipse.swt.graphics.Image",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"text|observeText|java.lang.String",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(10));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(11));
	}

	private void widget_Link_Shell_properties(List<IObserveInfo> properties) throws Exception {
		assertEquals(11, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"text|observeText|java.lang.String",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(10));
	}

	@Test
	public void test_widget_Composite_properties() throws Exception {
		WidgetBindableInfo root =
				parseBindings(
						"  private Composite m_composite;",
						"    m_composite = new Composite(m_shell, SWT.NONE);");
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(10, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(9));
	}

	@Test
	public void test_widget_Form_properties() throws Exception {
		WidgetBindableInfo root =
				parseBindings("  private Form m_form;", "    m_form = new Form(m_shell, SWT.NONE);");
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(11, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"text|observeText|java.lang.String",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(10));
	}

	@Test
	public void test_widget_Button_properties() throws Exception {
		WidgetBindableInfo root =
				parseBindings("  private Button m_button;", "    m_button = new Button(m_shell, SWT.NONE);");
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(13, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"image|observeImage|org.eclipse.swt.graphics.Image",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"selection|observeSelection|boolean",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"text|observeText|java.lang.String",
				properties.get(10));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(11));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(12));
	}

	@Test
	public void test_widget_Text_properties() throws Exception {
		WidgetBindableInfo root =
				parseBindings("  private Text m_text;", "    m_text = new Text(m_shell, SWT.NONE);");
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(13, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"editable|observeEditable|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"message|observeMessage|java.lang.String",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"text|observeText|java.lang.String",
				properties.get(10));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(11));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(12));
	}

	@Test
	public void test_widget_Spinner_properties() throws Exception {
		widget_Spinner_Scale_properties(
				"  private Spinner m_spinner;",
				"    m_spinner = new Spinner(m_shell, SWT.NONE);");
	}

	@Test
	public void test_widget_Scale_properties() throws Exception {
		widget_Spinner_Scale_properties(
				"  private Scale m_scale;",
				"    m_scale = new Scale(m_shell, SWT.NONE);");
	}

	private void widget_Spinner_Scale_properties(String fieldLine, String createLine)
			throws Exception {
		WidgetBindableInfo root = parseBindings(fieldLine, createLine);
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(13, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"maximum|observeMax|int",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"minimum|observeMin|int",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"selection|observeSelection|int",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(10));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(11));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(12));
	}

	@Test
	public void test_widget_Combo_properties() throws Exception {
		widget_Combo_CCombo_properties(
				"  private Combo m_combo;",
				"    m_combo = new Combo(m_shell, SWT.NONE);");
	}

	@Test
	public void test_widget_CCombo_properties() throws Exception {
		widget_Combo_CCombo_properties(
				"  private CCombo m_ccombo;",
				"    m_ccombo = new CCombo(m_shell, SWT.NONE);");
	}

	private void widget_Combo_CCombo_properties(String fieldLine, String createLine) throws Exception {
		WidgetBindableInfo root = parseBindings(fieldLine, createLine);
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(14, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"items|observeItems|java.util.List",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"selection|observeSelection|java.lang.String",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"singleSelectionIndex|observeSingleSelectionIndex|int",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(10));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"text|observeText|java.lang.String",
				properties.get(11));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(12));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(13));
	}

	@Test
	public void test_widget_List_properties() throws Exception {
		WidgetBindableInfo root =
				parseBindings("  private List m_list;", "    m_list = new List(m_shell, SWT.NONE);");
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(13, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"items|observeItems|java.util.List",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"selection|observeSelection|java.lang.String",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"singleSelectionIndex|observeSingleSelectionIndex|int",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(10));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(11));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(12));
	}

	@Test
	public void test_widget_Table_properties() throws Exception {
		WidgetBindableInfo root =
				parseBindings("  private Table m_table;", "    m_table = new Table(m_shell, SWT.NONE);");
		List<IObserveInfo> children = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(11, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"background|observeBackground|org.eclipse.swt.graphics.Color",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"bounds|observeBounds|org.eclipse.swt.graphics.Rectangle",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"enabled|observeEnabled|boolean",
				properties.get(2));
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"focused|observeFocus|boolean",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"font|observeFont|org.eclipse.swt.graphics.Font",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"foreground|observeForeground|org.eclipse.swt.graphics.Color",
				properties.get(5));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"location|observeLocation|org.eclipse.swt.graphics.Point",
				properties.get(6));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"singleSelectionIndex|observeSingleSelectionIndex|int",
				properties.get(7));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"size|observeSize|org.eclipse.swt.graphics.Point",
				properties.get(8));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"tooltipText|observeTooltipText|java.lang.String",
				properties.get(9));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"visible|observeVisible|boolean",
				properties.get(10));
	}

	@Test
	public void test_widget_Viewer_properties() throws Exception {
		WidgetBindableInfo root =
				parseBindings(
						"  private TableViewer m_viewer;",
						"    m_viewer = new TableViewer(m_shell, SWT.NONE);");
		List<IObserveInfo> controls = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> children =
				controls.get(0).getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(5, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"single selection|observeSingleSelection|java.lang.Object",
				properties.get(0));
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"part of selection|observeSingleSelection|java.lang.Object",
				properties.get(1));
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"multi selection|observeMultiSelection|java.lang.Object",
				properties.get(2));
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"filters|observeFilters|org.eclipse.jface.viewers.ViewerFilter",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"input|setInput|java.lang.Object",
				properties.get(4));
		//
		WidgetPropertyBindableInfo inputProperty = (WidgetPropertyBindableInfo) properties.get(4);
		assertNotNull(inputProperty.getObservableFactory());
		assertNull(inputProperty.getObservableFactory().createObservable(null, null, null, false));
		assertNull(inputProperty.getObservableFactory().createObservable(
				(BindableInfo) children.get(0),
				inputProperty,
				Type.Input,
				false));
	}

	@Test
	public void test_widget_CheckboxTableViewer_properties() throws Exception {
		widget_Checkable_properties(
				"  private CheckboxTableViewer m_viewer;",
				"    m_viewer = new CheckboxTableViewer(m_shell, SWT.NONE);");
	}

	@Test
	public void test_widget_CheckboxTreeViewer_properties() throws Exception {
		widget_Checkable_properties(
				"  private CheckboxTreeViewer m_viewer;",
				"    m_viewer = new CheckboxTreeViewer(m_shell, SWT.NONE);");
	}

	private void widget_Checkable_properties(String fieldLine, String createLine) throws Exception {
		WidgetBindableInfo root = parseBindings(fieldLine, createLine);
		List<IObserveInfo> controls = root.getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> children =
				controls.get(0).getChildren(ChildrenContext.ChildrenForMasterTable);
		List<IObserveInfo> properties =
				children.get(0).getChildren(ChildrenContext.ChildrenForPropertiesTable);
		assertEquals(6, properties.size());
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"single selection|observeSingleSelection|java.lang.Object",
				properties.get(0));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"part of selection|observeSingleSelection|java.lang.Object",
				properties.get(1));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"multi selection|observeMultiSelection|java.lang.Object",
				properties.get(2));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"checked elements|observeCheckedElements|java.lang.Object",
				properties.get(3));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"filters|observeFilters|org.eclipse.jface.viewers.ViewerFilter",
				properties.get(4));
		//
		assertBindableProperty(
				WidgetPropertyBindableInfo.class,
				"input|setInput|java.lang.Object",
				properties.get(5));
	}

	private WidgetBindableInfo parseBindings(String fieldLine, String createLine) throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import java.io.File;",
						"public class Test {",
						"  protected Shell m_shell;",
						fieldLine,
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
						createLine,
						"  }",
						"}");
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		assertInstanceOf(WidgetsObserveTypeContainer.class, provider.getContainers().get(1));
		//
		List<IObserveInfo> observes = provider.getObserves(ObserveType.WIDGETS);
		assertNotNull(observes);
		assertEquals(1, observes.size());
		//
		return (WidgetBindableInfo) observes.get(0);
	}

	@Test
	public void test_widget_noProperties() throws Exception {
		DialogInfo dialog =
				parseJavaInfo(
						"import java.io.File;",
						"public class Test extends org.eclipse.jface.dialogs.Dialog {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"  protected Control createDialogArea(Composite parent) {",
						"    Composite container = (Composite) super.createDialogArea(parent);",
						"    return container;",
						"  }",
						"}");
		assertNotNull(dialog);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> observes = provider.getObserves(ObserveType.WIDGETS);
		assertNotNull(observes);
		assertEquals(1, observes.size());
		//
		WidgetBindableInfo dialogBindable = (WidgetBindableInfo) observes.get(0);
		assertEquals("(org.eclipse.jface.dialogs.Dialog)", dialogBindable.getPresentation().getText());
		assertTrue(dialogBindable.getChildren(ChildrenContext.ChildrenForPropertiesTable).isEmpty());
		//
		WidgetBindableInfo parentInCreateDialogArea =
				(WidgetBindableInfo) dialogBindable.getChildren(ChildrenContext.ChildrenForMasterTable).get(
						0);
		assertEquals(
				"parent in createDialogArea(...)",
				parentInCreateDialogArea.getPresentation().getText());
		assertTrue(parentInCreateDialogArea.getChildren(ChildrenContext.ChildrenForPropertiesTable).isEmpty());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static void assertBindable(Object javaInfo,
			Class<?> testClass,
			Object testParent,
			boolean childrenState,
			String testString,
			Object object) throws Exception {
		assertInstanceOf(testClass, object);
		assertSame(javaInfo, ReflectionUtils.getFieldObject(object, "m_javaInfo"));
		BindableInfo bindable = (BindableInfo) object;
		assertSame(ObserveType.WIDGETS, bindable.getType());
		assertNotNull(bindable.getObjectType());
		assertEquals(testString, bindable.getPresentation().getText()
				+ "|"
				+ bindable.getReference()
				+ "|"
				+ bindable.getObjectType().getName());
		assertSame(testParent, bindable.getParent());
		assertNotNull(bindable.getPresentation());
		//
		assertNotNull(bindable.getChildren(ChildrenContext.ChildrenForMasterTable));
		assertEquals(
				childrenState,
				!bindable.getChildren(ChildrenContext.ChildrenForMasterTable).isEmpty());
		//
		assertNotNull(bindable.getChildren(ChildrenContext.ChildrenForPropertiesTable));
		assertFalse(bindable.getChildren(ChildrenContext.ChildrenForPropertiesTable).isEmpty());
	}

	public static void assertBindableProperty(Class<?> testClass, String testString, Object object)
			throws Exception {
		assertInstanceOf(testClass, object);
		BindableInfo bindable = (BindableInfo) object;
		assertSame(ObserveType.WIDGETS, bindable.getType());
		assertNotNull(bindable.getPresentation());
		assertNotNull(bindable.getObjectType());
		assertEquals(testString, bindable.getPresentation().getText()
				+ "|"
				+ bindable.getReference()
				+ "|"
				+ bindable.getObjectType().getName());
		assertNull(bindable.getParent());
		//
		assertNotNull(bindable.getChildren(ChildrenContext.ChildrenForMasterTable));
		assertTrue(bindable.getChildren(ChildrenContext.ChildrenForMasterTable).isEmpty());
		//
		assertNotNull(bindable.getChildren(ChildrenContext.ChildrenForPropertiesTable));
		assertTrue(bindable.getChildren(ChildrenContext.ChildrenForPropertiesTable).isEmpty());
	}
}