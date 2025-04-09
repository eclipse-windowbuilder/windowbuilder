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
package org.eclipse.wb.internal.core.nls.edit;

/**
 * Listener for changes in IEditableSource.
 *
 * For example when we externalize new property, we should update composite that displays this
 * source.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface IEditableSourceListener {
	/**
	 * Sent when new key was added.
	 */
	void keyAdded(String key, Object o);

	/**
	 * Sent when key was removed.
	 */
	void keyRemoved(String key);

	/**
	 * Sent when key was renamed.
	 */
	void keyRenamed(String oldKey, String newKey);
}
