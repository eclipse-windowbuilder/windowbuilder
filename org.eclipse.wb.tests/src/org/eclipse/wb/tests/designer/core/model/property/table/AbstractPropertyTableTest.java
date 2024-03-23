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
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;
import org.eclipse.wb.tests.gef.EventSender;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractPropertyTableTest extends DesignerTestCase {
	protected Shell m_shell;
	protected PropertyTable m_propertyTable;
	protected EventSender m_sender;

	////////////////////////////////////////////////////////////////////////////
	//
	// Test life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// create GUI
		{
			m_shell = new Shell();
			m_shell.setText("PropertyTable test");
			m_shell.setLayout(new FillLayout());
			m_shell.setBounds(10000, 0, 300, 500);
			//
			m_propertyTable = new PropertyTable(m_shell, SWT.NONE);
			m_sender = new EventSender(m_propertyTable.getControl());
			//
			m_shell.setVisible(true);
			waitEventLoop(1);
		}
	}

	@Override
	@After
	public void tearDown() throws Exception {
		m_shell.dispose();
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TestProperty
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link Property} for testing {@link PropertyTable}.
	 */
	protected static class TestProperty extends Property {
		private final String m_title;
		private final boolean m_modified;
		private final Object m_value;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public TestProperty(String title, boolean modified, Object value, PropertyEditor propertyEditor) {
			super(propertyEditor);
			m_title = title;
			m_modified = modified;
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
			return m_modified;
		}

		@Override
		public Object getValue() throws Exception {
			return m_value;
		}

		@Override
		public void setValue(Object value) throws Exception {
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// ComplexEditor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Simple implementation of {@link IComplexPropertyEditor}.
	 */
	protected static class ComplexEditor extends TextDisplayPropertyEditor
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
