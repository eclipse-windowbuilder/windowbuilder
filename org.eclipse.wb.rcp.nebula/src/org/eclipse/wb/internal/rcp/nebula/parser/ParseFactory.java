/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.nebula.parser;

import org.eclipse.wb.internal.core.parser.IParseFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.rcp.nebula.Activator;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * {@link IParseFactory} for Nebula.
 *
 * @author sablin_aa
 * @coverage core.model.parser
 */
public final class ParseFactory extends org.eclipse.wb.internal.core.parser.AbstractParseFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// IParseFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isToolkitObject(AstEditor editor, ITypeBinding typeBinding) throws Exception {
		return editor.getJavaProject().findType("org.eclipse.swt.custom.SashForm") != null // is_RCPContext
				&& isNebulaObject(typeBinding);
	}

	/**
	 * @return <code>true</code> if given type binding is RCP object.
	 */
	private static boolean isNebulaObject(ITypeBinding typeBinding) throws Exception {
		if (typeBinding == null) {
			return false;
		}
		// FormattedText
		if (AstNodeUtils.isSuccessorOf(
				typeBinding,
				"org.eclipse.nebula.widgets.formattedtext.AbstractFormatter")) {
			return true;
		}
		// GanttChart
		if (AstNodeUtils.isSuccessorOf(
				typeBinding,
				"org.eclipse.nebula.widgets.ganttchart.AbstractGanttEvent")) {
			return true;
		}
		//
		return false;
	}

	@Override
	protected String getToolkitId() {
		return Activator.PLUGIN_ID;
	}
}
