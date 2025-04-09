/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * The {@link ExpressionConverter} for {@link Number} types.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public abstract class AbstractNumberConverter extends ExpressionConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if context {@link IJavaProject} supports auto-boxing.
	 */
	protected final boolean isBoxingEnabled(JavaInfo javaInfo) {
		if (javaInfo == null) {
			return false;
		}
		IJavaProject javaProject = javaInfo.getEditor().getJavaProject();
		// ...auto-boxing enabled?
		String option = javaProject.getOption(JavaCore.COMPILER_PB_AUTOBOXING, true);
		if (!"ignore".equals(option)) {
			return false;
		}
		// OK
		return true;
	}
}
