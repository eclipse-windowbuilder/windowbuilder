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
package org.eclipse.wb.internal.core.model.property.table;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * Handler for any {@link Exception} that happens in {@link PropertyTable}, i.e. exceptions during
 * {@link Property} modifications using {@link PropertyEditor}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property.table
 */
public interface IPropertyExceptionHandler {
	void handle(Throwable e);
}
