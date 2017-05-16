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
package org.eclipse.wb.core.gef;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.core.gef.EditPartFactory;

/**
 * Implementations of {@link IEditPartConfigurator} are used by {@link EditPartFactory} to configure
 * any created {@link EditPart}.
 * <p>
 * Now {@link IEditPartConfigurator}'s are used to add some {@link EditPolicy} on {@link EditPart},
 * even if {@link EditPart} itself does not know anything about these {@link EditPolicy}.
 * <p>
 * For example we have SWT and <code>ControlEditPart</code>, that is used in both RCP and eRCP. But
 * in eRCP there is <code>Command</code> that can be dropped on <code>Control</code>. However we can
 * not install corresponding {@link EditPolicy} in <code>ControlEditPart</code> because at "shared"
 * SWT level we don't know anything about eRCP and <code>Command</code>.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public interface IEditPartConfigurator {
  /**
   * Configures given {@link EditPart}.
   */
  void configure(EditPart context, EditPart editPart);
}
