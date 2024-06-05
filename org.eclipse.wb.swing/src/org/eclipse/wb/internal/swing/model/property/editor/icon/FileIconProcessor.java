/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.editor.icon;

import org.eclipse.wb.core.editor.icon.AbstractFileImageProcessor;
import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Default implementation for handling Swing {@link Icon}s via the file system.
 */
public final class FileIconProcessor extends AbstractFileImageProcessor {
	@Override
	public boolean process(IGenericProperty property, String[] value) {
		JavaInfo javaInfo = property.getJavaInfo();
		Expression expression = getFinalExpression(javaInfo, property.getExpression());
		if (expression instanceof ClassInstanceCreation creation) {
			// absolute path
			if (AstNodeUtils.isCreation(creation, "javax.swing.ImageIcon",
					new String[] { "<init>(java.lang.String)", "<init>(java.lang.String,java.lang.String)" })
					&& creation.arguments().get(0) instanceof StringLiteral stringLiteral) {
				String path = stringLiteral.getLiteralValue();
				value[0] = prefix + path;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean postOpen(IGenericProperty property, String path, String[] value) {
		String pathSource = StringConverter.INSTANCE.toJavaSource(property.getJavaInfo(), path);
		value[0] = "new javax.swing.ImageIcon(" + pathSource + ")";
		return true;
	}
}
