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
package org.eclipse.wb.internal.core.model.generation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;

/**
 * Provides access to {@link VariableSupport}'s and {@link StatementGenerator}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public final class GenerationUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private GenerationUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link VariableSupport} for given {@link JavaInfo}.
	 */
	public static VariableSupport getVariableSupport(JavaInfo javaInfo) throws Exception {
		VariableSupportDescription description = getVariableDescription(javaInfo);
		return description.createSupport(javaInfo);
	}

	/**
	 * @return the {@link VariableSupportDescription} for given {@link JavaInfo}.
	 */
	public static VariableSupportDescription getVariableDescription(JavaInfo javaInfo) {
		GenerationSettings generationSettings = getToolkit(javaInfo).getGenerationSettings();
		return generationSettings.getVariable(javaInfo);
	}

	/**
	 * @return the {@link StatementGenerator} for given {@link JavaInfo}.
	 */
	public static StatementGenerator getStatementGenerator(JavaInfo javaInfo) throws Exception {
		GenerationSettings generationSettings = getToolkit(javaInfo).getGenerationSettings();
		StatementGeneratorDescription description = generationSettings.getStatement(javaInfo);
		return description.get();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ToolkitDescriptionJava} for given {@link JavaInfo}.
	 */
	private static ToolkitDescriptionJava getToolkit(JavaInfo javaInfo) {
		return javaInfo.getDescription().getToolkit();
	}
}
