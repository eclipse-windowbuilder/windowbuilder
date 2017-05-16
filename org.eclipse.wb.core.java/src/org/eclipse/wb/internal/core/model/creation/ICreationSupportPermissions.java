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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;

import org.apache.commons.lang.NotImplementedException;

/**
 * Provides for {@link CreationSupport} delete/reorder/reparent permissions.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public interface ICreationSupportPermissions {
  /**
   * Implementation of {@link ICreationSupportPermissions} that does not allow any operation.
   */
  ICreationSupportPermissions FALSE = new ICreationSupportPermissions() {
    public boolean canDelete(JavaInfo javaInfo) {
      return false;
    }

    public void delete(JavaInfo javaInfo) throws Exception {
      throw new NotImplementedException();
    }

    public boolean canReorder(JavaInfo javaInfo) {
      return false;
    }

    public boolean canReparent(JavaInfo javaInfo) {
      return false;
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  boolean canDelete(JavaInfo javaInfo);

  void delete(JavaInfo javaInfo) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Permissions
  //
  ////////////////////////////////////////////////////////////////////////////
  boolean canReorder(JavaInfo javaInfo);

  boolean canReparent(JavaInfo javaInfo);
}
