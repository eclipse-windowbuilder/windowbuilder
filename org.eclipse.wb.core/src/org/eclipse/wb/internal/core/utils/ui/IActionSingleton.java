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
package org.eclipse.wb.internal.core.utils.ui;

import org.eclipse.jface.action.IAction;

/**
 * Marker for {@link IAction} that tells {@link MenuIntersector} that wrapper for such
 * {@link IAction}'s should execute only first {@link IAction}, for example because it performs
 * operation based on selection, not on single object.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public interface IActionSingleton {
}
