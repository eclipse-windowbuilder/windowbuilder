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
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.swing.ToolkitProvider;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Utils for testing Swing related classes.
 *
 * @author scheglov_ke
 */
public class SwingTestUtils {
	private static final IPreferenceStore m_preferences =
			ToolkitProvider.DESCRIPTION.getPreferences();
	private static final GenerationSettings m_settings =
			ToolkitProvider.DESCRIPTION.getGenerationSettings();

	/**
	 * Sets the generation preferences.
	 */
	public static void setGenerations(VariableSupportDescription variable,
			StatementGeneratorDescription statement) {
		m_settings.setVariable(variable);
		m_settings.setStatement(statement);
	}

	/**
	 * Sets the {@link FieldUniqueVariableSupport#P_FIELD_MODIFIER} preference.
	 */
	public static void setFieldUniqueModifier(int modifierId) {
		m_preferences.setValue(FieldUniqueVariableSupport.P_FIELD_MODIFIER, modifierId);
	}

	/**
	 * Sets the {@link FieldInitializerVariableSupport#P_FIELD_MODIFIER} preference.
	 */
	public static void setFieldInitializerModifier(int modifierId) {
		m_preferences.setValue(FieldInitializerVariableSupport.P_FIELD_MODIFIER, modifierId);
	}

	/**
	 * Sets the {@link LazyVariableSupport#P_METHOD_MODIFIER} preference.
	 */
	public static void setLazyMethodModifier(int modifierId) {
		m_preferences.setValue(LazyVariableSupport.P_METHOD_MODIFIER, modifierId);
	}

	/**
	 * Sets the default generation preferences.
	 */
	public static void setGenerationDefaults() {
		m_settings.setVariable(m_settings.getDefaultVariable());
		m_settings.setStatement(m_settings.getDefaultStatement());
		m_preferences.setToDefault(LocalUniqueVariableSupport.P_DECLARE_FINAL);
		m_preferences.setToDefault(FieldUniqueVariableSupport.P_FIELD_MODIFIER);
		m_preferences.setToDefault(FieldUniqueVariableSupport.P_PREFIX_THIS);
		m_preferences.setToDefault(FieldInitializerVariableSupport.P_FIELD_MODIFIER);
		m_preferences.setToDefault(FieldInitializerVariableSupport.P_PREFIX_THIS);
		m_preferences.setToDefault(LazyVariableSupport.P_METHOD_MODIFIER);
	}
}
