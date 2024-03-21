/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.property.table;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author lobas_av
 *
 */
public class TableTest1 {
	////////////////////////////////////////////////////////////////////////////
	//
	// MAIN
	//
	////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell();
		shell.setLayout(new GridLayout());
		//
		PropertyEditor booleanEditor = BooleanPropertyEditor.INSTANCE;
		PropertyEditor intEditor = IntegerPropertyEditor.INSTANCE;
		PropertyEditor stringEditor = StringPropertyEditor.INSTANCE;
		//
		ComplexEditor keyEditor =
				new ComplexEditor(new Property[]{
						new TestProperty("passwd", false, "qwerty", stringEditor),
						new TestProperty("stars", false, "****", stringEditor),
						new TestProperty("TesT", false, "###", new ComplexEditor(new Property[]{}))});
		//
		ComplexEditor styleEditor =
				new ComplexEditor(new Property[]{new TestProperty("border",
						false,
						Boolean.FALSE,
						booleanEditor)});
		ComplexEditor accessEditor =
				new ComplexEditor(new Property[]{
						new TestProperty("name", false, "", stringEditor),
						new TestProperty("help", false, "", stringEditor),
						new TestProperty("keyboardShortcut", false, "", keyEditor),
						new TestProperty("description", false, "", stringEditor)});
		ComplexEditor dataEditor = new ComplexEditor(new Property[]{});
		ComplexEditor layoutDataEditor =
				new ComplexEditor(new Property[]{
						new TestProperty("class", false, "org.eclipse.swt.layout.GridData", stringEditor),
						new TestProperty("exclude", false, Boolean.FALSE, booleanEditor),
						new TestProperty("hAlign", true, "fill", stringEditor),
						new TestProperty("hGrab", true, Boolean.TRUE, booleanEditor),
						new TestProperty("hHint", false, -1, intEditor),
						new TestProperty("hIndent", false, -1, intEditor),
						new TestProperty("hSpan", false, -1, intEditor),
						new TestProperty("minHeight", false, 0, intEditor),
						new TestProperty("minWidth", false, 0, intEditor),
						new TestProperty("vAlign", true, "fill", stringEditor),
						new TestProperty("vGrab", true, Boolean.TRUE, booleanEditor),
						new TestProperty("vHint", false, -1, intEditor),
						new TestProperty("vIndent", false, -1, intEditor),
						new TestProperty("vSpan", false, -1, intEditor),
						new TestProperty("variable", false, "", stringEditor)});
		//
		PropertyTable table = new PropertyTable(shell, SWT.NONE);
		//PropertyTable2 table = new PropertyTable2(shell, SWT.NONE);
		//table.setFont(new Font(null, "", 12, SWT.NONE));
		table.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		//table.setInput(null);
		if (true) {
			table.setInput(new Property[]{
					new TestProperty("style", false, "[]", styleEditor),
					new TestPropertyWithTooltip("text",
							false,
							"",
							stringEditor,
							"text - Sets the receiver\'s text. This method sets the widget label.  The label may include the mnemonic character and line delimiters."),
					new TestProperty("variable", true, "browser", stringEditor),
					new TestProperty("accessibility", false, "(accessibility)", accessEditor),
					new TestProperty("background", false, "", stringEditor),
					new TestProperty("backgroundImage", false, "", stringEditor),
					new TestProperty("backgroundModel", false, "inherit_none", stringEditor),
					new TestProperty("capture", false, Boolean.FALSE, booleanEditor),
					new TestProperty("class", false, "org.eclipse.swt.browser.Browser", stringEditor),
					new TestProperty("data", false, "(values)", dataEditor),
					new TestProperty("enabled", false, Boolean.TRUE, booleanEditor),
					new TestProperty("font", false, "", stringEditor),
					new TestProperty("foreground", false, "", stringEditor),
					new TestProperty("layoutData", true, "(GridData)", layoutDataEditor),
					new TestProperty("layoutDeferred", false, Boolean.FALSE, booleanEditor),
					new TestProperty("redraw", false, Boolean.FALSE, booleanEditor),
					new TestProperty("toolTipText", false, 123, intEditor),
					new TestProperty("url", true, "http://www.google.com", stringEditor),
					new TestProperty("visible0123456789", false, Boolean.TRUE, booleanEditor)});
		}
		//
		shell.setText("Properties");
		shell.setLocation(1150, 450);
		shell.setSize(300, 500);
		shell.open();
		//
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TestPropertyWithTooltip
	//
	////////////////////////////////////////////////////////////////////////////
	private static class TestPropertyWithTooltip extends TestProperty {
		private final String m_tooltipText;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public TestPropertyWithTooltip(String title,
				boolean modify,
				Object value,
				PropertyEditor propertyEditor,
				String tooltipText) {
			super(title, modify, value, propertyEditor);
			m_tooltipText = tooltipText;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IAdaptable
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter == PropertyTooltipProvider.class && m_tooltipText != null) {
				return adapter.cast(new PropertyTooltipTextProvider() {
					@Override
					protected String getText(Property property) throws Exception {
						return m_tooltipText;
					}
				});
			}
			return super.getAdapter(adapter);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// TestProperty
	//
	////////////////////////////////////////////////////////////////////////////
	private static class TestProperty extends Property {
		private final String m_title;
		private final boolean m_modify;
		private final Object m_value;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public TestProperty(String title, boolean modify, Object value, PropertyEditor propertyEditor) {
			super(propertyEditor);
			m_title = title;
			m_modify = modify;
			m_value = value;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Property
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String getTitle() {
			return m_title;
		}

		@Override
		public boolean isModified() throws Exception {
			return m_modify;
		}

		@Override
		public Object getValue() throws Exception {
			return m_value;
		}

		@Override
		public void setValue(Object value) throws Exception {
			throw new Exception("aaaaaa");
		}
	}
	private static class ComplexEditor extends TextDisplayPropertyEditor
	implements
	IComplexPropertyEditor {
		private final Property[] m_properties;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ComplexEditor(Property[] properties) {
			m_properties = properties;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected String getText(Property property) throws Exception {
			return null;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IComplexPropertyEditor
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Property[] getProperties(Property property) throws Exception {
			return m_properties;
		}
	}
}