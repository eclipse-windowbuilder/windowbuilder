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
package org.eclipse.wb.internal.swt.model.generation.preview;

import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldVariableSupport;

/**
 * Implementation of {@link GenerationPreview} for {@link FieldUniqueVariableSupport} and
 * {@link FlatStatementGenerator}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation.ui
 */
public final class GenerationPreviewFieldUniqueFlat extends GenerationPreview {
	public static final GenerationPreview INSTANCE = new GenerationPreviewFieldUniqueFlat();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private GenerationPreviewFieldUniqueFlat() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GenerationPreview
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPreview(GenerationPropertiesComposite variableComposite,
			GenerationPropertiesComposite statementComposite) {
		boolean v_useThis = variableComposite.getBoolean(FieldUniqueVariableSupport.P_PREFIX_THIS);
		int v_modifierIndex = variableComposite.getInteger(FieldUniqueVariableSupport.P_FIELD_MODIFIER);
		String v_modifierSource = FieldVariableSupport.V_MODIFIER_CODE[v_modifierIndex];
		boolean s_usePrefix = statementComposite.getBoolean(FlatStatementGenerator.P_USE_PREFIX);
		String s_thePrefix = statementComposite.getString(FlatStatementGenerator.P_PREFIX_TEXT);
		//
		String source = "";
		String contentsRef = v_useThis ? "this.content" : "contents";
		String buttonRef = v_useThis ? "this.button" : "button";
		// declare fields
		source += "\t" + v_modifierSource + "Composite contents;\n";
		source += "\t" + v_modifierSource + "Button button;\n";
		// begin
		source += "\t...\n";
		// parent
		{
			// assign field
			source += "\t" + contentsRef + " = new Composite(parent, SWT.NONE);\n";
			// properties
			source += "\t" + contentsRef + ".setLayout(new GridLayout(1, false));\n";
		}
		// child
		{
			// optional prefix
			if (s_usePrefix) {
				source += "\t" + s_thePrefix + "\n";
			}
			// assign field
			source += "\t" + buttonRef + " = new Button(" + contentsRef + ", SWT.NONE);\n";
			// properties
			source += "\t" + buttonRef + ".setText(\"New button\");\n";
		}
		// end
		source += "\t...\n";
		// final result
		return source;
	}
}
