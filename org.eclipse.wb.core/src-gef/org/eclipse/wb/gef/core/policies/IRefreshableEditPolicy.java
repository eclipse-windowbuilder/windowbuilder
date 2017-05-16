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
package org.eclipse.wb.gef.core.policies;

/**
 * Optional interface for {@link EditPolicy} that may be used to notify {@link EditPolicy} that
 * something was changed, and even if it is still active, some changes may be should be performed.
 * <p>
 * For example layout {@link EditPolicy} may update selection {@link EditPolicy} of managed objects.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public interface IRefreshableEditPolicy {
  void refreshEditPolicy();
}