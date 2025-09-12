/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.property.category;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang3.StringUtils;

/**
 * Describes category of {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public final class PropertyCategory {
	/**
	 * "Normal" category, used for properties that should be displayed without any effect.
	 */
	public static final PropertyCategory NORMAL = new PropertyCategory(0, "NORMAL");
	/**
	 * "Preferred" category, for properties that are most useful for component.
	 */
	public static final PropertyCategory PREFERRED = new PropertyCategory(-1, "PREFERRED");
	/**
	 * "Advanced" category, for properties that are rarely used, visible if modified, even if not
	 * enabled.
	 */
	public static final PropertyCategory ADVANCED = new PropertyCategory(1, "ADVANCED");
	/**
	 * "Advanced" category, for properties that are rarely used, visible only if enabled.
	 */
	public static final PropertyCategory ADVANCED_REALLY = new PropertyCategory(2, "ADVANCED_REALLY");
	/**
	 * "Hidden" category, for properties that should not be displayed.
	 */
	public static final PropertyCategory HIDDEN = new PropertyCategory(3, "HIDDEN");

	////////////////////////////////////////////////////////////////////////////
	//
	// System
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the system {@link PropertyCategory} with given priority.
	 */
	public static final PropertyCategory system(int priority) {
		return new PropertyCategory(SYSTEM_BASE + priority, "SYSTEM:" + priority);
	}

	/**
	 * @return the system {@link PropertyCategory} with priority
	 *         <code>system.getPriority() + additional</code>.
	 */
	public static final PropertyCategory system(PropertyCategory system, int additional) {
		Assert.isTrue(system.isSystem());
		return system(system.getPriority() - SYSTEM_BASE + additional);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parsing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Parses {@link PropertyCategory} from strings. Supported strings are:
	 *
	 * <ul>
	 * <li><b>normal</b></li>
	 * <li><b>preferred</b></li>
	 * <li><b>advanced</b></li>
	 * <li><b>advanced-really</b></li>
	 * <li><b>hidden</b></li>
	 * <li><b>system(number)</b></li>
	 * </ul>
	 *
	 * @param defaultCategory
	 *          the {@link PropertyCategory} to use if given string can not be parsed, or
	 *          <code>null</code> if exception should be thrown.
	 *
	 * @return the parsed {@link PropertyCategory}.
	 */
	public static PropertyCategory get(String text, PropertyCategory defaultCategory) {
		// simple
		if ("normal".equals(text)) {
			return NORMAL;
		}
		if ("preferred".equals(text)) {
			return PREFERRED;
		}
		if ("advanced".equals(text)) {
			return ADVANCED;
		}
		if ("advanced-really".equals(text)) {
			return ADVANCED_REALLY;
		}
		if ("hidden".equals(text)) {
			return HIDDEN;
		}
		// system
		if (StringUtils.startsWith(text, "system(")) {
			String systemText = text;
			systemText = StringUtils.removeStart(systemText, "system(");
			systemText = StringUtils.removeEnd(systemText, ")");
			try {
				int priority = Integer.parseInt(systemText);
				return system(priority);
			} catch (NumberFormatException e) {
			}
		}
		// can not parse
		if (defaultCategory != null) {
			return defaultCategory;
		}
		throw new IllegalArgumentException("Unknown category " + text);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private static final int SYSTEM_BASE = 1000;
	private final int m_priority;
	private final String m_string;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private PropertyCategory(int priority, String string) {
		m_priority = priority;
		m_string = string;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return m_string;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof PropertyCategory category) {
			return m_priority == category.m_priority;
		}
		// unknown class
		return false;
	}

	@Override
	public int hashCode() {
		return m_priority;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this property is preferred.
	 */
	public boolean isPreferred() {
		return this == PREFERRED;
	}

	/**
	 * @return <code>true</code> if this property is advanced.
	 */
	public boolean isAdvanced() {
		return this == ADVANCED;
	}

	/**
	 * @return <code>true</code> if this property is really advanced.
	 */
	public boolean isAdvancedReally() {
		return this == ADVANCED_REALLY;
	}

	/**
	 * @return <code>true</code> if this property is hidden.
	 */
	public boolean isHidden() {
		return this == HIDDEN;
	}

	/**
	 * @return <code>true</code> if this property is system.
	 */
	public boolean isSystem() {
		return m_priority >= 900;
	}

	/**
	 * @return the priority of this category.
	 */
	public int getPriority() {
		return m_priority;
	}
}
