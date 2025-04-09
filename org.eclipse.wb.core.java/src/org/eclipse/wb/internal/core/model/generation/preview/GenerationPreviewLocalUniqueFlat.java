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
package org.eclipse.wb.internal.core.model.generation.preview;

import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;

/**
 * Implementation of {@link GenerationPreview} for {@link LocalUniqueVariableSupport} and
 * {@link FlatStatementGenerator}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation.ui
 */
public final class GenerationPreviewLocalUniqueFlat extends GenerationPreview {
	public static final GenerationPreview INSTANCE = new GenerationPreviewLocalUniqueFlat();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private GenerationPreviewLocalUniqueFlat() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GenerationPreview
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPreview(GenerationPropertiesComposite variableComposite,
			GenerationPropertiesComposite statementComposite) {
		boolean v_useFinal = variableComposite.getBoolean(LocalUniqueVariableSupport.P_DECLARE_FINAL);
		boolean s_usePrefix = statementComposite.getBoolean(FlatStatementGenerator.P_USE_PREFIX);
		String s_thePrefix = statementComposite.getString(FlatStatementGenerator.P_PREFIX_TEXT);
		String source = "";
		// begin
		source += "\t...\n";
		// parent
		{
			source += "\t";
			if (v_useFinal) {
				source += "final ";
			}
			source += "JPanel panel = new JPanel();\n";
			// properties
			source += "\tpanel.setBorder(new TitledBorder(\"Management\"));\n";
		}
		// child
		{
			// optional prefix
			if (s_usePrefix) {
				source += "\t" + s_thePrefix + "\n";
			}
			// variable
			source += "\t";
			if (v_useFinal) {
				source += "final ";
			}
			source += "JButton button = new JButton();\n";
			// properties
			source += "\tbutton.setText(\"Add customer...\");\n";
			source += "\tpanel.add(button);\n";
		}
		// end
		source += "\t...\n";
		// final result
		return source;
	}
}
