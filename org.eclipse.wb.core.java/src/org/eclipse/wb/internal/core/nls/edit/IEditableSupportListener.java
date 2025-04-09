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
 * Listener for changes in IEditableSupport.
 *
 * For example when we externalize new property to some existing source, we should update tree that
 * displays not yet externalized properties.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public abstract class IEditableSupportListener {
	/**
	 * Sent when new editable source was added.
	 */
	public void sourceAdded(IEditableSource source) {
	}

	/**
	 * Sent when set of externalized properties was changed - some property was externalized or
	 * internalized.
	 */
	public void externalizedPropertiesChanged() {
	}
}
