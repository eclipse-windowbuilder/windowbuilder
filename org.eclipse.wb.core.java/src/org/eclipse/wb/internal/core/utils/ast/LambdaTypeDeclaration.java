/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.lang.reflect.Constructor;

/**
 * Wrapper class for creating instances of {@code LambdaTypeStub} without
 * explicitly referencing the class. Doing so may cause problems because this
 * class is contributed by our JDT fragment.
 */
public final class LambdaTypeDeclaration {
	private static Class<?> m_class;

	private LambdaTypeDeclaration() {
	}

	public static TypeDeclaration create(Expression expression, IMethodBinding methodBinding) {
		try {
			if (m_class == null) {
				m_class = TypeDeclaration.class.getClassLoader().loadClass("org.eclipse.jdt.core.dom.LambdaTypeStub");
			}
			Constructor<?> constructor = m_class.getConstructor(Expression.class, IMethodBinding.class);
			return (TypeDeclaration) constructor.newInstance(expression, methodBinding);
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}
}
