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
package org.eclipse.wb.internal.core.model.order;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Description for location of {@link JavaInfo} in list of parent children and statements.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class ComponentOrder {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ComponentOrder} parsed from given string specification.
   */
  public static ComponentOrder parse(String specification) {
    if (specification.equals("default")) {
      return ComponentOrderDefault.INSTANCE;
    } else if (specification.equals("last")) {
      return ComponentOrderLast.INSTANCE;
    } else if (specification.equals("first")) {
      return ComponentOrderFirst.INSTANCE;
    } else if (specification.startsWith("beforeSibling ")) {
      String nextComponentClass = specification.substring("beforeSibling ".length());
      return new ComponentOrderBeforeSibling(nextComponentClass);
    } else {
      throw new IllegalArgumentException("Unsupported order specification: " + specification);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is used when requested to add component with this {@link ComponentOrder} as "last".
   * Usually this means - after all existing {@link JavaInfo} children. However sometimes we want to
   * group children by class and "last" means "after last child of same class".
   *
   * @return the {@link JavaInfo} before which component should be added, may be <code>null</code>,
   *         so component will be added as last child of parent.
   */
  public JavaInfo getNextComponent_whenLast(JavaInfo component, JavaInfo container)
      throws Exception {
    return null;
  }

  /**
   * @return <code>true</code> if "otherComponent" can be added after component with this
   *         {@link ComponentOrder}.
   */
  public boolean canBeBefore(JavaInfo otherComponent) {
    return true;
  }
}
