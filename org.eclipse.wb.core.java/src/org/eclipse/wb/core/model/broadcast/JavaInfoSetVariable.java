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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

/**
 * Notifies that {@link VariableSupport} for {@link JavaInfo} was changed.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface JavaInfoSetVariable {
	void invoke(JavaInfo javaInfo, VariableSupport oldVariable, VariableSupport newVariable)
			throws Exception;
}