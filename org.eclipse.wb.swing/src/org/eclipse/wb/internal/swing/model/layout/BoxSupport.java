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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import javax.swing.Box;

/**
 * Helper for working with {@link Box} models.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class BoxSupport {
	/**
	 * Sets size for {@link Box#createHorizontalStrut(int)}, {@link Box#createVerticalStrut(int)} or
	 * {@link Box#createRigidArea(java.awt.Dimension)}.
	 */
	public static void setStrutSize(ComponentInfo strut, String source) throws Exception {
		StaticFactoryCreationSupport factoryCreationSupport =
				(StaticFactoryCreationSupport) strut.getCreationSupport();
		MethodInvocation factoryInvocation = factoryCreationSupport.getInvocation();
		Expression oldExpression = DomGenerics.arguments(factoryInvocation).get(0);
		strut.getEditor().replaceExpression(oldExpression, source);
	}
}
