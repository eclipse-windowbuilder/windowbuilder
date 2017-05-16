/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
