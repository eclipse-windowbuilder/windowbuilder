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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import org.apache.commons.lang3.StringUtils;

/**
 * Utils for using in {@link VariableSupport} implementations.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class VariableUtils {
	private final JavaInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public VariableUtils(JavaInfo javaInfo) {
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Prefixes/suffixes
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the unique name of field by given local variable name.
	 */
	public String getUniqueFieldName(String localName, VariableDeclaration excludedVariable) {
		return convertName(
				-1,
				localName,
				NamingConventions.VK_LOCAL,
				NamingConventions.VK_INSTANCE_FIELD,
				excludedVariable);
	}

	/**
	 * Generates unique variable name by converting from local/field to field/local.
	 */
	public String convertName(int position,
			String name,
			int variableKind_source,
			int variableKind_target,
			VariableDeclaration excludedVariable) {
		// remove possible _NNN from base name
		{
			int index = name.lastIndexOf('_');
			if (index != -1) {
				String possibleNumber = name.substring(index + 1);
				if (StringUtils.isNumeric(possibleNumber)) {
					name = name.substring(0, index);
				}
			}
		}
		// remove source prefix/suffix
		name = stripPrefixSuffix(name, variableKind_source);
		// add target prefix/suffix
		name = addPrefixSuffix(name, variableKind_target);
		// generate unique name
		return m_javaInfo.getEditor().getUniqueVariableName(position, name, excludedVariable);
	}

	/**
	 * @return the name with added prefix/suffix.
	 *
	 * @param variableKind specifies what type the variable is:
	 *                     {@link NamingConventions#VK_LOCAL},
	 *                     {@link NamingConventions#VK_PARAMETER},
	 *                     {@link NamingConventions#VK_STATIC_FIELD},
	 *                     {@link NamingConventions#VK_INSTANCE_FIELD} or
	 *                     {@link NamingConventions#VK_STATIC_FINAL_FIELD}.
	 */
	public String addPrefixSuffix(String name, int variableKind) {
		Assert.isNotNull(name);
		IJavaProject javaProject = m_javaInfo.getEditor().getJavaProject();
		String[] variableNames = NamingConventions.suggestVariableNames(variableKind, NamingConventions.BK_NAME, name,
				javaProject, 0, null, true);
		// The first entry contains the combination prefix + name + suffix
		return variableNames[0];
	}

	/**
	 * @return the name with removed prefix/suffix.
	 *
	 * @param variableKind specifies what type the variable is:
	 *                     {@link NamingConventions#VK_LOCAL},
	 *                     {@link NamingConventions#VK_PARAMETER},
	 *                     {@link NamingConventions#VK_STATIC_FIELD},
	 *                     {@link NamingConventions#VK_INSTANCE_FIELD} or
	 *                     {@link NamingConventions#VK_STATIC_FINAL_FIELD}.
	 */
	public String stripPrefixSuffix(String name, int variableKind) {
		Assert.isNotNull(name);
		IJavaProject javaProject = m_javaInfo.getEditor().getJavaProject();
		String baseName = NamingConventions.getBaseName(variableKind, name, javaProject);
		return baseName;
	}
}
