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
package org.eclipse.wb.core.editor;

/**
 * Designer parsing state.
 *
 * @author lobas_av
 * @coverage core.editor
 */
public enum DesignerState {
	/**
	 * State is undefined.
	 */
	Undefined,
	/**
	 * Parsing time.
	 */
	Parsing,
	/**
	 * Parsing completed successfully.
	 */
	Successful,
	/**
	 * Parsing completed with error.
	 */
	Error
}