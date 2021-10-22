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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.widgets.Form;

import java.util.Collections;
import java.util.List;

/**
 * Properties provider for <code>SWT</code> widgets and <code>JFace</code> viewers.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class PropertiesSupport {
  public static final String DETAIL_SINGLE_SELECTION_NAME = "part of selection";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  static {
    WidgetPropertyBindableInfo tooltipText =
        new WidgetPropertyBindableInfo("tooltipText",
            String.class,
            "observeTooltipText",
            IObserveDecorator.DEFAULT);
    //
    SwtProperties.createControlProperties(tooltipText);
    //
    SwtProperties.createDateTimeProperties();
    //
    WidgetPropertyBindableInfo text_Text_StyledText =
        new WidgetPropertyBindableInfo("text",
            String.class,
            "observeText",
            SwtObservableFactory.SWT_TEXT,
            IObserveDecorator.BOLD);
    //
    WidgetPropertyBindableInfo message =
        new WidgetPropertyBindableInfo("message",
            String.class,
            "observeMessage",
            IObserveDecorator.BOLD);
    //
    SwtProperties.createTextProperties(text_Text_StyledText, message);
    SwtProperties.createStyledTextProperties(text_Text_StyledText);
    SwtProperties.createToolTipProperties(message);
    //
    WidgetPropertyBindableInfo selectionInt =
        new WidgetPropertyBindableInfo("selection",
            int.class,
            "observeSelection",
            IObserveDecorator.BOLD);
    //
    WidgetPropertyBindableInfo min =
        new WidgetPropertyBindableInfo("minimum", int.class, "observeMin", IObserveDecorator.BOLD);
    //
    WidgetPropertyBindableInfo max =
        new WidgetPropertyBindableInfo("maximum", int.class, "observeMax", IObserveDecorator.BOLD);
    //
    SwtProperties.createSpinnerProperties(selectionInt, min, max);
    SwtProperties.createScaleProperties(selectionInt, min, max);
    //
    WidgetPropertyBindableInfo text =
        new WidgetPropertyBindableInfo("text", String.class, "observeText", IObserveDecorator.BOLD);
    //
    WidgetPropertyBindableInfo image =
        new WidgetPropertyBindableInfo("image", Image.class, "observeImage", IObserveDecorator.BOLD);
    //
    SwtProperties.createButtonProperties(text, image);
    SwtProperties.createLabelProperties(text, image);
    SwtProperties.createCLabelProperties(text, image);
    SwtProperties.createLinkProperties(text);
    SwtProperties.createShellProperties(text);
    SwtProperties.createFormProperties(text);
    //
    WidgetPropertyBindableInfo selectionString =
        new WidgetPropertyBindableInfo("selection",
            String.class,
            "observeSelection",
            IObserveDecorator.BOLD);
    //
    WidgetPropertyBindableInfo items =
        new WidgetPropertyBindableInfo("items",
            List.class,
            "observeItems",
            SwtObservableFactory.SWT_ITEMS,
            IObserveDecorator.BOLD);
    //
    WidgetPropertyBindableInfo selectionIndex =
        new WidgetPropertyBindableInfo("singleSelectionIndex",
            int.class,
            "observeSingleSelectionIndex",
            IObserveDecorator.BOLD);
    //
    SwtProperties.createComboProperties(text, selectionString, items, selectionIndex);
    SwtProperties.createCComboProperties(text, selectionString, items, selectionIndex);
    SwtProperties.createListProperties(selectionString, items, selectionIndex);
    SwtProperties.createTableProperties(selectionIndex);
    SwtProperties.createItemProperties(text, image);
    SwtProperties.createTableColumnProperties(tooltipText);
    SwtProperties.createTreeColumnProperties(tooltipText);
    SwtProperties.createToolItemProperties(tooltipText);
    SwtProperties.createTrayItemProperties(tooltipText);
    SwtProperties.createTabItemProperties(tooltipText);
    SwtProperties.createCTabItemProperties(tooltipText);
    SwtProperties.createViewerProperties();
    SwtProperties.createStructuredViewerProperties();
    SwtProperties.createCheckableViewerProperties();
  }

  /**
   * @return {@link WidgetPropertyBindableInfo} properties for given widget {@link Class}.
   */
  public static List<WidgetPropertyBindableInfo> getProperties(ClassLoader classLoader,
      Class<?> widgetClass) throws Exception {
    return createBindables(getProperties0(classLoader, widgetClass));
  }

  /**
   * @return {@link WidgetPropertyBindableInfo} properties for given widget {@link Class}.
   */
  private static List<WidgetPropertyBindableInfo> getProperties0(ClassLoader classLoader,
      Class<?> widgetClass) throws Exception {
    // TableColumn
    if (TableColumn.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.TABLE_COLUMN;
    }
    // TreeColumn
    if (TreeColumn.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.TREE_COLUMN;
    }
    // ToolItem
    if (ToolItem.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.TOOL_ITEM;
    }
    // TrayItem
    if (TrayItem.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.TRAY_ITEM;
    }
    // TabItem
    if (TabItem.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.TAB_ITEM;
    }
    // CTabItem
    if (CTabItem.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.CTAB_ITEM;
    }
    // ToolTip
    if (ToolTip.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.TOOL_TIP;
    }
    // Item
    if (Item.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.ITEM;
    }
    // StyledText
    if (StyledText.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.STYLED_TEXT;
    }
    // Button
    if (Button.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.BUTTON;
    }
    // Text
    if (Text.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.TEXT;
    }
    // Spinner
    if (Spinner.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.SPINNER;
    }
    // Scale
    if (Scale.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.SCALE;
    }
    // Label
    if (Label.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.LABEL;
    }
    // CLabel
    if (CLabel.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.CLABEL;
    }
    // Link
    if (Link.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.LINK;
    }
    // Shell
    if (Shell.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.SHELL;
    }
    // Form
    if (Form.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.FORM;
    }
    // Combo
    if (Combo.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.COMBO;
    }
    // CCombo
    if (CCombo.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.CCOMBO;
    }
    // List
    if (org.eclipse.swt.widgets.List.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.LIST;
    }
    // Table
    if (Table.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.TABLE;
    }
    // DateTime (since 3.3)
    if (CoreUtils.isAssignableFrom(classLoader, "org.eclipse.swt.widgets.DateTime", widgetClass)) {
      return SwtProperties.DATE_TIME;
    }
    // Control
    if (Control.class.isAssignableFrom(widgetClass)) {
      return SwtProperties.CONTROL;
    }
    // CheckboxTableViewer
    // CheckboxTreeViewer
    if (CoreUtils.isAssignableFrom(
        classLoader,
        "org.eclipse.jface.viewers.CheckboxTableViewer",
        widgetClass)
        || CoreUtils.isAssignableFrom(
            classLoader,
            "org.eclipse.jface.viewers.CheckboxTreeViewer",
            widgetClass)) {
      return SwtProperties.CHECKABLE_VIEWER;
    }
    // StructuredViewer
    if (CoreUtils.isAssignableFrom(
        classLoader,
        "org.eclipse.jface.viewers.StructuredViewer",
        widgetClass)) {
      return SwtProperties.STRUCTURED_VIEWER;
    }
    // Viewer
    if (CoreUtils.isAssignableFrom(classLoader, "org.eclipse.jface.viewers.Viewer", widgetClass)) {
      return SwtProperties.VIEWER;
    }
    return Collections.emptyList();
  }

  private static List<WidgetPropertyBindableInfo> createBindables(List<WidgetPropertyBindableInfo> staticBindables) {
    List<WidgetPropertyBindableInfo> bindables = Lists.newArrayList();
    for (WidgetPropertyBindableInfo bindable : staticBindables) {
      bindables.add(new WidgetPropertyBindableInfo(bindable));
    }
    return bindables;
  }

  /**
   * @return {@code true} if given {@link JavaInfo} should observe over SWTObservables.
   */
  public static boolean isObservableInfo(JavaInfo javaInfo) throws Exception {
    Class<?> widgetClass = javaInfo.getDescription().getComponentClass();
    //
    if (Control.class.isAssignableFrom(widgetClass)
        || ToolTip.class.isAssignableFrom(widgetClass)
        || Item.class.isAssignableFrom(widgetClass)) {
      return true;
    }
    //
    ClassLoader classLoader = EditorState.get(javaInfo.getEditor()).getEditorLoader();
    return CoreUtils.isAssignableFrom(classLoader, "org.eclipse.jface.viewers.Viewer", widgetClass);
  }
}