/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import org.eclipse.wb.internal.core.databinding.model.ObserveComparator;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Properties container for <code>SWT</code> widgets and <code>JFace</code> viewers.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class SwtProperties {
	static final List<WidgetPropertyBindableInfo> CONTROL = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> BUTTON = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> DATE_TIME = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> TEXT = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> STYLED_TEXT = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> TOOL_TIP = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> SPINNER = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> SCALE = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> LIST = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> TABLE = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> LINK = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> SHELL = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> FORM = new ArrayList<>();
	//
	static final List<WidgetPropertyBindableInfo> LABEL = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> CLABEL = new ArrayList<>();
	//
	static final List<WidgetPropertyBindableInfo> COMBO = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> CCOMBO = new ArrayList<>();
	//
	static final List<WidgetPropertyBindableInfo> ITEM = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> TABLE_COLUMN = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> TREE_COLUMN = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> TOOL_ITEM = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> TRAY_ITEM = new ArrayList<>();
	//
	static final List<WidgetPropertyBindableInfo> TAB_ITEM = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> CTAB_ITEM = new ArrayList<>();
	//
	static final List<WidgetPropertyBindableInfo> VIEWER = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> STRUCTURED_VIEWER = new ArrayList<>();
	static final List<WidgetPropertyBindableInfo> CHECKABLE_VIEWER = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Control
	//
	////////////////////////////////////////////////////////////////////////////
	static void createControlProperties(WidgetPropertyBindableInfo tooltipText) {
		CONTROL.add(new WidgetPropertyBindableInfo("enabled",
				boolean.class,
				"observeEnabled",
				IObserveDecorator.DEFAULT));
		CONTROL.add(new WidgetPropertyBindableInfo("visible",
				boolean.class,
				"observeVisible",
				IObserveDecorator.DEFAULT));
		CONTROL.add(tooltipText);
		CONTROL.add(new WidgetPropertyBindableInfo("foreground",
				Color.class,
				"observeForeground",
				IObserveDecorator.ITALIC));
		CONTROL.add(new WidgetPropertyBindableInfo("background",
				Color.class,
				"observeBackground",
				IObserveDecorator.ITALIC));
		CONTROL.add(new WidgetPropertyBindableInfo("font",
				Font.class,
				"observeFont",
				IObserveDecorator.ITALIC));
		CONTROL.add(new WidgetPropertyBindableInfo("size",
				Point.class,
				"observeSize",
				IObserveDecorator.ITALIC));
		CONTROL.add(new WidgetPropertyBindableInfo("location",
				Point.class,
				"observeLocation",
				IObserveDecorator.ITALIC));
		CONTROL.add(new WidgetPropertyBindableInfo("focused",
				boolean.class,
				"observeFocus",
				IObserveDecorator.DEFAULT));
		CONTROL.add(new WidgetPropertyBindableInfo("bounds",
				Rectangle.class,
				"observeBounds",
				IObserveDecorator.ITALIC));
		//
		Collections.sort(CONTROL, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Button
	//
	////////////////////////////////////////////////////////////////////////////
	static void createButtonProperties(WidgetPropertyBindableInfo text,
			WidgetPropertyBindableInfo image) {
		BUTTON.addAll(CONTROL);
		BUTTON.add(new WidgetPropertyBindableInfo("selection",
				boolean.class,
				"observeSelection",
				IObserveDecorator.BOLD));
		BUTTON.add(text);
		BUTTON.add(image);
		//
		Collections.sort(BUTTON, ObserveComparator.INSTANCE);
	}

	static void createDateTimeProperties() {
		DATE_TIME.addAll(CONTROL);
		DATE_TIME.add(new WidgetPropertyBindableInfo("selection",
				java.util.Date.class,
				"observeSelection",
				IObserveDecorator.BOLD));
		//
		Collections.sort(DATE_TIME, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Text
	//
	////////////////////////////////////////////////////////////////////////////
	static void createTextProperties(WidgetPropertyBindableInfo text,
			WidgetPropertyBindableInfo message) {
		TEXT.addAll(CONTROL);
		TEXT.add(new WidgetPropertyBindableInfo("editable",
				boolean.class,
				"observeEditable",
				IObserveDecorator.BOLD));
		TEXT.add(text);
		TEXT.add(message);
		//
		Collections.sort(TEXT, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// StyledText
	//
	////////////////////////////////////////////////////////////////////////////
	static void createStyledTextProperties(WidgetPropertyBindableInfo text) {
		STYLED_TEXT.addAll(CONTROL);
		STYLED_TEXT.add(text);
		//
		Collections.sort(STYLED_TEXT, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ToolTip
	//
	////////////////////////////////////////////////////////////////////////////
	static void createToolTipProperties(WidgetPropertyBindableInfo message) {
		TOOL_TIP.add(message);
		//
		Collections.sort(TOOL_TIP, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Spinner
	//
	////////////////////////////////////////////////////////////////////////////
	static void createSpinnerProperties(WidgetPropertyBindableInfo selection,
			WidgetPropertyBindableInfo min,
			WidgetPropertyBindableInfo max) {
		SPINNER.addAll(CONTROL);
		SPINNER.add(selection);
		SPINNER.add(min);
		SPINNER.add(max);
		//
		Collections.sort(SPINNER, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Scale
	//
	////////////////////////////////////////////////////////////////////////////
	static void createScaleProperties(WidgetPropertyBindableInfo selection,
			WidgetPropertyBindableInfo min,
			WidgetPropertyBindableInfo max) {
		SCALE.addAll(CONTROL);
		SCALE.add(selection);
		SCALE.add(min);
		SCALE.add(max);
		//
		Collections.sort(SCALE, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Label
	//
	////////////////////////////////////////////////////////////////////////////
	static void createLabelProperties(WidgetPropertyBindableInfo text,
			WidgetPropertyBindableInfo image) {
		LABEL.addAll(CONTROL);
		LABEL.add(text);
		LABEL.add(image);
		//
		Collections.sort(LABEL, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Label
	//
	////////////////////////////////////////////////////////////////////////////
	static void createCLabelProperties(WidgetPropertyBindableInfo text,
			WidgetPropertyBindableInfo image) {
		CLABEL.addAll(CONTROL);
		CLABEL.add(text);
		CLABEL.add(image);
		//
		Collections.sort(CLABEL, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Link
	//
	////////////////////////////////////////////////////////////////////////////
	static void createLinkProperties(WidgetPropertyBindableInfo text) {
		LINK.addAll(CONTROL);
		LINK.add(text);
		//
		Collections.sort(LINK, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Shell
	//
	////////////////////////////////////////////////////////////////////////////
	static void createShellProperties(WidgetPropertyBindableInfo text) {
		SHELL.addAll(CONTROL);
		SHELL.add(text);
		//
		Collections.sort(SHELL, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Form
	//
	////////////////////////////////////////////////////////////////////////////
	static void createFormProperties(WidgetPropertyBindableInfo text) {
		FORM.addAll(CONTROL);
		FORM.add(text);
		//
		Collections.sort(FORM, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Combo
	//
	////////////////////////////////////////////////////////////////////////////
	static void createComboProperties(WidgetPropertyBindableInfo text,
			WidgetPropertyBindableInfo selection,
			WidgetPropertyBindableInfo items,
			WidgetPropertyBindableInfo selectionIndex) {
		COMBO.addAll(CONTROL);
		COMBO.add(text);
		COMBO.add(selection);
		COMBO.add(items);
		COMBO.add(selectionIndex);
		//
		Collections.sort(COMBO, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CCombo
	//
	////////////////////////////////////////////////////////////////////////////
	static void createCComboProperties(WidgetPropertyBindableInfo text,
			WidgetPropertyBindableInfo selection,
			WidgetPropertyBindableInfo items,
			WidgetPropertyBindableInfo selectionIndex) {
		CCOMBO.addAll(CONTROL);
		CCOMBO.add(text);
		CCOMBO.add(selection);
		CCOMBO.add(items);
		CCOMBO.add(selectionIndex);
		//
		Collections.sort(CCOMBO, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// List
	//
	////////////////////////////////////////////////////////////////////////////
	static void createListProperties(WidgetPropertyBindableInfo selection,
			WidgetPropertyBindableInfo items,
			WidgetPropertyBindableInfo selectionIndex) {
		LIST.addAll(CONTROL);
		LIST.add(selection);
		LIST.add(items);
		LIST.add(selectionIndex);
		//
		Collections.sort(LIST, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Table
	//
	////////////////////////////////////////////////////////////////////////////
	static void createTableProperties(WidgetPropertyBindableInfo selectionIndex) {
		TABLE.addAll(CONTROL);
		TABLE.add(selectionIndex);
		//
		Collections.sort(TABLE, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Item
	//
	////////////////////////////////////////////////////////////////////////////
	static void createItemProperties(WidgetPropertyBindableInfo text, WidgetPropertyBindableInfo image) {
		ITEM.add(text);
		ITEM.add(image);
		//
		Collections.sort(ITEM, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TableColumn
	//
	////////////////////////////////////////////////////////////////////////////
	static void createTableColumnProperties(WidgetPropertyBindableInfo tooltipText) {
		TABLE_COLUMN.addAll(ITEM);
		TABLE_COLUMN.add(tooltipText);
		//
		Collections.sort(TABLE_COLUMN, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TreeColumn
	//
	////////////////////////////////////////////////////////////////////////////
	static void createTreeColumnProperties(WidgetPropertyBindableInfo tooltipText) {
		TREE_COLUMN.addAll(ITEM);
		TREE_COLUMN.add(tooltipText);
		//
		Collections.sort(TREE_COLUMN, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ToolItem
	//
	////////////////////////////////////////////////////////////////////////////
	static void createToolItemProperties(WidgetPropertyBindableInfo tooltipText) {
		TOOL_ITEM.addAll(ITEM);
		TOOL_ITEM.add(tooltipText);
		//
		Collections.sort(TOOL_ITEM, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TrayItem
	//
	////////////////////////////////////////////////////////////////////////////
	static void createTrayItemProperties(WidgetPropertyBindableInfo tooltipText) {
		TRAY_ITEM.addAll(ITEM);
		TRAY_ITEM.add(tooltipText);
		//
		Collections.sort(TRAY_ITEM, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TabItem
	//
	////////////////////////////////////////////////////////////////////////////
	static void createTabItemProperties(WidgetPropertyBindableInfo tooltipText) {
		TAB_ITEM.addAll(ITEM);
		TAB_ITEM.add(tooltipText);
		//
		Collections.sort(TAB_ITEM, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CTabItem
	//
	////////////////////////////////////////////////////////////////////////////
	static void createCTabItemProperties(WidgetPropertyBindableInfo tooltipText) {
		CTAB_ITEM.addAll(ITEM);
		CTAB_ITEM.add(tooltipText);
		//
		Collections.sort(CTAB_ITEM, ObserveComparator.INSTANCE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Viewer
	//
	////////////////////////////////////////////////////////////////////////////
	static void createViewerProperties() {
		VIEWER.add(new WidgetPropertyBindableInfo("single selection",
				TypeImageProvider.OBJECT_IMAGE,
				Object.class,
				"observeSingleSelection",
				ViewerObservableFactory.SINGLE_SELECTION,
				IObserveDecorator.DEFAULT));
		VIEWER.add(new WidgetPropertyBindableInfo(PropertiesSupport.DETAIL_SINGLE_SELECTION_NAME,
				TypeImageProvider.OBJECT_IMAGE,
				Object.class,
				"observeSingleSelection",
				ViewerObservableFactory.DETAIL_SINGLE_SELECTION,
				IObserveDecorator.DEFAULT));
		VIEWER.add(new WidgetPropertyBindableInfo("multi selection",
				TypeImageProvider.COLLECTION_IMAGE,
				Object.class,
				"observeMultiSelection",
				ViewerObservableFactory.MULTI_SELECTION,
				IObserveDecorator.DEFAULT));
		VIEWER.add(new WidgetPropertyBindableInfo("input",
				TypeImageProvider.VIEWER_IMAGE,
				Object.class,
				"setInput",
				ViewerInputObservableFactory.INSTANCE,
				IObserveDecorator.BOLD));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// StructuredViewer
	//
	////////////////////////////////////////////////////////////////////////////
	static void createStructuredViewerProperties() {
		STRUCTURED_VIEWER.addAll(VIEWER);
		STRUCTURED_VIEWER.add(3, new WidgetPropertyBindableInfo("filters",
				TypeImageProvider.COLLECTION_IMAGE,
				ViewerFilter.class,
				"observeFilters",
				ViewerObservableFactory.FILTERS,
				IObserveDecorator.DEFAULT));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CheckableViewer
	//
	////////////////////////////////////////////////////////////////////////////
	static void createCheckableViewerProperties() {
		CHECKABLE_VIEWER.addAll(STRUCTURED_VIEWER);
		CHECKABLE_VIEWER.add(3, new WidgetPropertyBindableInfo("checked elements",
				TypeImageProvider.COLLECTION_IMAGE,
				Object.class,
				"observeCheckedElements",
				ViewerObservableFactory.CHECKED_ELEMENTS,
				IObserveDecorator.DEFAULT));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Map<String, String> SWT_OBSERVABLES_TO_WIDGET_PROPERTIES = new HashMap<>();
	static {
		String[] keys =
			{
					"observeEnabled",
					"observeVisible",
					"observeTooltipText",
					"observeSelection",
					"observeMin",
					"observeMax",
					"observeText",
					"observeMessage",
					"observeImage",
					"observeItems",
					"observeSingleSelectionIndex",
					"observeForeground",
					"observeBackground",
					"observeFont",
					"observeSize",
					"observeLocation",
					"observeFocus",
					"observeBounds",
					"observeEditable",
					"observeSingleSelection",
					"observeMultiSelection",
					"observeInput",
					"observeCheckedElements",
					"observeFilters",
					// observeSelection method is overloaded...
					"observeSelection",
					"observeSelection",
					"observeSelection",
					"observeSelection",
					"observeSelection",
					"observeSelection",
					"observeSelection",
					"observeSelection",
					"observeSelection",
					"observeSelection",
					"observeSelection",
			"observeSelection"};
		String[] values =
			{
					"enabled",
					"visible",
					"tooltipText",
					"selection",
					"minimum",
					"maximum",
					"text",
					"message",
					"image",
					"items",
					"singleSelectionIndex",
					"foreground",
					"background",
					"font",
					"size",
					"location",
					"focused",
					"bounds",
					"editable",
					"singleSelection",
					"multipleSelection",
					"input",
					"checkedElements",
					"filters",
					"dateTimeSelection",
					"localDateSelection",
					"localTimeSelection",
					"buttonSelection",
					"comboSelection",
					"ccomboSelection",
					"listSelection",
					"menuItemSelection",
					"scaleSelection",
					"sliderSelection",
					"spinnerSelection",
					// widgetSelection -> observeSelection
			"widgetSelection"};
		//
		for (int i = 0; i < keys.length; i++) {
			SWT_OBSERVABLES_TO_WIDGET_PROPERTIES.put(keys[i], values[i]);
			SWT_OBSERVABLES_TO_WIDGET_PROPERTIES.put(values[i], keys[i]);
		}
	}
}