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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Implementations of this interface can process {@link ComponentDescription}'s after loading them.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public interface IDescriptionProcessor {
	/**
	 * This method allows configure given {@link ComponentDescription} after loading.
	 * <p>
	 * For example in SWT we can mark first <code>Composite</code> parameter of constructor as parent.
	 *
	 * @param editor
	 *          the {@link AstEditor} in which context {@link ComponentDescription} is loading.
	 * @param componentDescription
	 *          the {@link ComponentDescription} to process.
	 */
	void process(AstEditor editor, ComponentDescription componentDescription) throws Exception;
}
