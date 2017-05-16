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
package org.eclipse.wb.internal.core.model.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;

/**
 * Abstract base for {@link IMenuInfo} implementation, for {@link JavaInfo} models.
 *
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public abstract class JavaMenuMenuObject extends AbstractMenuMenuObject {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaMenuMenuObject(JavaInfo component) {
    super(component);
    m_component.addBroadcastListener(new ObjectInfoDelete() {
      private int m_level = 0;

      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (m_level == 0 && isRootFor0(child)) {
          fireDeleteListeners0(child);
        }
        m_level++;
      }

      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        m_level--;
      }
    });
  }

  /**
   * Access method for {@link #isRootFor(ObjectInfo)}.
   */
  private boolean isRootFor0(ObjectInfo child) {
    return isRootFor(child);
  }

  /**
   * Access method for {@link #fireDeleteListeners(Object).
   */
  private void fireDeleteListeners0(Object toolkitModel) {
    fireDeleteListeners(toolkitModel);
  }
}
