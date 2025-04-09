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
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

/**
 * Helper for working {@link ObjectPropertyEditor}.
 *
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public interface IObjectPropertyProcessor {
	/**
	 * @param property
	 *          {@link Property} to assign object value.
	 * @param componentValue
	 *          value {@link JavaInfo} for assign.
	 * @return the {@link StatementTarget} for assign invocation.
	 */
	public StatementTarget getObjectPropertyStatementTarget(GenericProperty property,
			JavaInfo componentValue) throws Exception;
}
