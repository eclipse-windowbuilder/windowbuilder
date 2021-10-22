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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Implementations of this interface are used to provide presentation icon for {@link ActionInfo},
 * even when {@link Action} itself has no icon.
 * <p>
 * We use this for example for {@link ActionFactory} actions.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public interface IActionIconProvider {
  Image getActionIcon();
}
