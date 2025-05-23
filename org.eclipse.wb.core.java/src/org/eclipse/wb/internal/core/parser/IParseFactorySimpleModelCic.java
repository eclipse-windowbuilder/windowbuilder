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
package org.eclipse.wb.internal.core.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * This interface is used to create {@link JavaInfo} for simple cases, when we know this it is just
 * simple component, not factory, etc.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public interface IParseFactorySimpleModelCic {
	/**
	 * @return <code>true</code> if this and only this factory should be used to create
	 *         {@link JavaInfo}. If no {@link JavaInfo} returned, then no other factories will be
	 *         checked.
	 */
	boolean accept(AstEditor editor, ClassInstanceCreation creation, ITypeBinding typeBinding)
			throws Exception;

	/**
	 * @return the {@link JavaInfo} for given {@link ClassInstanceCreation}, may be <code>null</code>.
	 */
	JavaInfo create(AstEditor editor, ClassInstanceCreation creation, ITypeBinding typeBinding)
			throws Exception;
}
