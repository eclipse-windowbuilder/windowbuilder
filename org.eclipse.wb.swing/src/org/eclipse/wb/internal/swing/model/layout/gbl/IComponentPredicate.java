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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

/**
 * Predicate for {@link ComponentInfo} and its {@link AbstractGridBagConstraintsInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public interface IComponentPredicate {
  /**
   * @return <code>true</code> if {@link ComponentInfo} should be visited.
   */
  boolean apply(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // TRUE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IComponentPredicate} that accepts all {@link ComponentInfo}'s.
   */
  IComponentPredicate TRUE = new IComponentPredicate() {
    public boolean apply(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
        throws Exception {
      return true;
    }
  };
}
