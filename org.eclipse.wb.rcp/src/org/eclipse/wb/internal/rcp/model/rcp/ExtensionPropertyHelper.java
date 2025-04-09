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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;
import java.util.function.Function;

/**
 * Helper for creating property with name "Extension" for attributes in <code>plugin.xml</code>
 * file.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public abstract class ExtensionPropertyHelper {
	private final JavaInfo m_javaInfo;
	private final IProject m_project;
	private final String m_pointID;
	private final String m_elementName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExtensionPropertyHelper(JavaInfo javaInfo, String pointID, String elementName)
			throws Exception {
		m_javaInfo = javaInfo;
		m_project = javaInfo.getEditor().getJavaProject().getProject();
		m_pointID = pointID;
		m_elementName = elementName;
		m_javaInfo.addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				appendExtensionProperty(properties);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private ComplexProperty m_extensionProperty;
	private final RunnableEx m_refreshListener = new RunnableEx() {
		@Override
		public void run() throws Exception {
			m_javaInfo.refresh();
		}
	};

	/**
	 * This method is invoked one time after reparse to create {@link Property}'s using method
	 * {@link #createAttributeProperty(PropertyEditor, String)}.
	 */
	protected abstract Property[] createProperties();

	/**
	 * Appends {@link ComplexProperty} for properties from <code>plugin.xml</code>.
	 */
	private void appendExtensionProperty(List<Property> properties) throws Exception {
		// ensure ComplexProperty
		if (m_extensionProperty == null) {
			Property[] extensionProperties = createProperties();
			m_extensionProperty =
					new ComplexProperty("Extension", "(Extension properties)", extensionProperties);
			m_extensionProperty.setCategory(PropertyCategory.system(9));
		}
		// OK, add ComplexProperty
		if (((ExtensionElementProperty<?>) m_extensionProperty.getProperties()[0]).hasElement()) {
			properties.add(m_extensionProperty);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Extension property creation utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected final Property createBooleanProperty(String attributeName, boolean defaultValue) {
		return createAttributeProperty(
				BooleanPropertyEditor.INSTANCE,
				attributeName,
				ExtensionElementProperty.FROM_BOOLEAN,
				ExtensionElementProperty.TO_BOOLEAN,
				defaultValue);
	}

	protected final Property createStringProperty(String attributeName) {
		return createAttributeProperty(StringPropertyEditor.INSTANCE, attributeName);
	}

	protected final Property createIconProperty(String attributeName) {
		return createAttributeProperty(ExtensionElementIconPropertyEditor.INSTANCE, attributeName);
	}

	/**
	 * @return the {@link ExtensionElementProperty} for given attribute of extension element.
	 */
	protected final <T> Property createAttributeProperty(PropertyEditor editor, String attributeName) {
		return createAttributeProperty(
				editor,
				attributeName,
				ExtensionElementProperty.IDENTITY,
				ExtensionElementProperty.IDENTITY,
				Property.UNKNOWN_VALUE);
	}

	/**
	 * @return the {@link ExtensionElementProperty} for given attribute of extension element.
	 */
	protected final <T> Property createAttributeProperty(PropertyEditor editor,
			String attributeName,
			Function<T, String> fromValueConverter,
			Function<String, T> toValueConverter,
			Object defaultValue) {
		TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_javaInfo);
		String className = AstNodeUtils.getFullyQualifiedName(typeDeclaration, true);
		return new ExtensionElementProperty<>(m_refreshListener,
				editor,
				attributeName,
				m_project,
				m_pointID,
				m_elementName,
				className,
				attributeName,
				fromValueConverter,
				toValueConverter,
				defaultValue);
	}
}
