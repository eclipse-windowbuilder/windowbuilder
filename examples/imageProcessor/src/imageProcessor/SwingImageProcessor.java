/*******************************************************************************
 * Copyright (c) 2024 DSA GmbH, Aachen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    DSA GmbH, Aachen - initial API and implementation
 *******************************************************************************/
package imageProcessor;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.wb.core.editor.icon.AbstractClasspathImageProcessor;
import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

/**
 * Custom image processor which uses {@link SwingImageIcon} instead of {@link ImageIcon} in the code generation.
 */
public class SwingImageProcessor extends AbstractClasspathImageProcessor {
	@Override
	public boolean process(IGenericProperty property, String[] value) {
		JavaInfo javaInfo = property.getJavaInfo();
		Expression expression = getFinalExpression(javaInfo, property.getExpression());
		if (expression instanceof ClassInstanceCreation creation) {
			if (AstNodeUtils.isCreation(creation, "imageProcessor.SwingImageIcon", "<init>(java.lang.String)" )) {
				StringLiteral resourceLiteral = (StringLiteral) creation.arguments().get(0);
				value[0] = prefix + resourceLiteral.getLiteralValue();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean postOpen(IGenericProperty property, String path, String[] value) {
		String pathSource = StringConverter.INSTANCE.toJavaSource(property.getJavaInfo(), path);
		value[0] = "new imageProcessor.SwingImageIcon(" + pathSource + ")";
		return true;
	}
}
