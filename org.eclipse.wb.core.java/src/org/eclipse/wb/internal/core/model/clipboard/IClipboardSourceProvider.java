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
package org.eclipse.wb.internal.core.model.clipboard;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * Extension for {@link PropertyEditor} that can provide Java source for copy/paste operation.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public interface IClipboardSourceProvider {
	/**
	 * @return the Java source that that has same value as current value of given
	 *         {@link GenericProperty}, or <code>null</code> if no such source can be provided.
	 */
	String getClipboardSource(GenericProperty property) throws Exception;
}
