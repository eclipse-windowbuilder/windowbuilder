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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Description for creating (adding) new component.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class CreationDescription extends AbstractDescription {
	private final ComponentDescription m_componentDescription;
	private final Class<?> m_componentClass;
	private final String m_componentClassName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CreationDescription(ComponentDescription componentDescription,
			String creationId,
			String name) {
		m_componentDescription = componentDescription;
		m_componentClass = componentDescription.getComponentClass();
		m_componentClassName = ReflectionUtils.getCanonicalName(m_componentClass);
		m_id = creationId;
		m_name = name != null ? name : CodeUtils.getShortClass(m_componentClassName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// id
	//
	////////////////////////////////////////////////////////////////////////////
	private final String m_id;

	/**
	 * @return identifier of this creation.
	 */
	public String getId() {
		return m_id;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// icon
	//
	////////////////////////////////////////////////////////////////////////////
	private ImageDescriptor m_icon;

	/**
	 * @return the icon of this creation for displaying for user.
	 */
	public ImageDescriptor getIcon() {
		return m_icon != null ? m_icon : m_componentDescription.getIcon();
	}

	/**
	 * Sets the icon of this creation for displaying for user.
	 */
	public void setIcon(ImageDescriptor icon) {
		m_icon = icon;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// name
	//
	////////////////////////////////////////////////////////////////////////////
	private final String m_name;

	/**
	 * @return the name of this creation for displaying for user.
	 */
	public String getName() {
		return m_name;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// description
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_description;

	/**
	 * @return the description of this creation for displaying for user.
	 */
	public String getDescription() {
		return m_description != null ? m_description : m_componentDescription.getDescription();
	}

	/**
	 * Sets the description of this creation for displaying for user.
	 */
	public void setDescription(String description) {
		m_description = description != null ? StringUtilities.normalizeWhitespaces(description) : null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_source;

	/**
	 * @return the source for creating new component instance.
	 */
	public String getSource() {
		return m_source;
	}

	/**
	 * Sets that source for creating new component instance.
	 */
	public void setSource(String source) {
		m_source = evaluate(source);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Invocations
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<CreationInvocationDescription> m_invocations = new ArrayList<>();

	/**
	 * @return the {@link List} of {@link CreationInvocationDescription}.
	 */
	public List<CreationInvocationDescription> getInvocations() {
		return m_invocations;
	}

	/**
	 * Adds the {@link CreationInvocationDescription}.
	 */
	public void addInvocation(CreationInvocationDescription invocation) {
		m_invocations.add(invocation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parameters
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<String, String> m_parameters = new TreeMap<>();

	/**
	 * @return the {@link CreationDescription} specific parameters.
	 */
	public Map<String, String> getParameters() {
		return Collections.unmodifiableMap(m_parameters);
	}

	/**
	 * Adds new parameter.
	 */
	public void addParameter(String name, String value) {
		m_parameters.put(name, value);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Generics
	//
	////////////////////////////////////////////////////////////////////////////
	public static class TypeParameterDescription {
		private final String m_typeName;
		private final String m_title;

		public TypeParameterDescription(String typeName, String title) {
			m_typeName = typeName;
			m_title = title;
		}

		public String getTypeName() {
			return m_typeName;
		}

		public String getTitle() {
			return m_title;
		}
	}

	private Map<String, TypeParameterDescription> m_typeArguments;

	/**
	 * @return the {@link Map} of generic parameters.
	 */
	public Map<String, TypeParameterDescription> getTypeParameters() {
		return m_typeArguments != null
				? m_typeArguments
						: Collections.emptyMap();
	}

	/**
	 * Adds new generic parameter info.
	 */
	public void setTypeParameter(String name, String typeName, String title) {
		if (m_typeArguments == null) {
			m_typeArguments = new LinkedHashMap<>();
		}
		m_typeArguments.put(name, new TypeParameterDescription(typeName, title));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Do any replaces in given template (with <code>%pattern%</code>).
	 */
	private String evaluate(String s) {
		return StringUtils.replace(s, "%component.class%", m_componentClassName);
	}
}
