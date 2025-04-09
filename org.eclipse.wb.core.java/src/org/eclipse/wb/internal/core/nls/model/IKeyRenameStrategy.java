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
package org.eclipse.wb.internal.core.nls.model;


/**
 * This interface helps in renaming keys for externalized properties, when name of component is
 * changing.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface IKeyRenameStrategy {
	/**
	 * @return the new key. If it is same as any of existing keys, including returning "oldKey", then
	 *         no rename will be performed.
	 */
	String getNewKey(String oldName, String newName, String oldKey);
}
