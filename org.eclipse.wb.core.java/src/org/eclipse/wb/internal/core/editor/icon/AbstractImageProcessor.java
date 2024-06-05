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
package org.eclipse.wb.internal.core.editor.icon;

import org.eclipse.wb.core.editor.icon.AbstractClasspathImageProcessor;
import org.eclipse.wb.core.editor.icon.AbstractFileImageProcessor;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.IImageProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Abstract base class for all processors that need to extract the image from
 * the AST.
 *
 * @noextend This class is not intended to be subclassed by clients. Use either
 *           {@link AbstractClasspathImageProcessor} or
 *           {@link AbstractFileImageProcessor}.
 */
public abstract class AbstractImageProcessor implements IImageProcessor {
	protected final String prefix;

	/**
	 * @param prefix An arbitrary but fixed prefix used for the human-readable name.
	 */
	public AbstractImageProcessor(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public final boolean preOpen(IGenericProperty property, String text, Object[] value) {
		if (text != null && text.startsWith(prefix)) {
			value[0] = text.substring(prefix.length());
			return true;
		}
		return false;
	}

	/**
	 * @return the final {@link Expression} for given one. This method will traverse
	 *         {@link SimpleName}'s until last assignment of "real"
	 *         {@link Expression} will be found.
	 */
	protected static Expression getFinalExpression(JavaInfo javaInfo, Expression expression) {
		ExecutionFlowDescription flowDescription = JavaInfoUtils.getState(javaInfo).getFlowDescription();
		return ExecutionFlowUtils.getFinalExpression(flowDescription, expression);
	}
}
