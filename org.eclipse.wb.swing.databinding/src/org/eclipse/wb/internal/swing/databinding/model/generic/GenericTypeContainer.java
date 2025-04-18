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
package org.eclipse.wb.internal.swing.databinding.model.generic;

import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link IGenericType} container that contains generic parameters.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.generic
 */
public final class GenericTypeContainer implements IGenericType {
	private final Class<?> m_rawType;
	private final int m_dimension;
	private String m_fullName;
	private final String m_simpleName;
	private final List<IGenericType> m_subTypes = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public GenericTypeContainer(Class<?> rawType) {
		this(rawType, 0);
	}

	public GenericTypeContainer(Class<?> rawType, int dimension) {
		m_rawType = rawType;
		m_dimension = dimension;
		m_simpleName = m_rawType.getSimpleName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IGenericType
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Class<?> getRawType() {
		return m_rawType;
	}

	@Override
	public String getFullTypeName() {
		if (m_fullName == null) {
			// prepare class name
			StringBuffer fullName = new StringBuffer();
			if (m_rawType.isArray()) {
				Class<?> type = m_rawType;
				for (int i = 0; i < m_dimension; i++) {
					type = type.getComponentType();
				}
				fullName.append(CoreUtils.getClassName(type));
			} else {
				fullName.append(CoreUtils.getClassName(m_rawType));
			}
			// prepare generic parameters
			fullName.append("<");
			int count = m_subTypes.size();
			for (int i = 0; i < count; i++) {
				if (i > 0) {
					fullName.append(", ");
				}
				fullName.append(m_subTypes.get(i).getFullTypeName());
			}
			fullName.append(">");
			// handle array
			for (int i = 0; i < m_dimension; i++) {
				fullName.append("[]");
			}
			// result
			m_fullName = fullName.toString();
		}
		return m_fullName;
	}

	@Override
	public String getSimpleTypeName() {
		return m_simpleName;
	}

	@Override
	public List<IGenericType> getSubTypes() {
		return m_subTypes;
	}

	@Override
	public IGenericType getSubType(int index) {
		if (index >= m_subTypes.size()) {
			return ClassGenericType.OBJECT_CLASS;
		}
		return m_subTypes.get(index);
	}

	@Override
	public boolean isEmpty() {
		return m_subTypes.isEmpty();
	}
}