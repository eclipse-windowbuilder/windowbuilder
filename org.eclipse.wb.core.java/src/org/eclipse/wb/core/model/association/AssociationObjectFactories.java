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
package org.eclipse.wb.core.model.association;

/**
 * Helper for creating {@link AssociationObjectFactory}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class AssociationObjectFactories {
  public static AssociationObjectFactory no() {
    return new AssociationObjectFactory() {
      @Override
      public String toString() {
        return "NO";
      }

      public AssociationObject create() {
        return AssociationObjects.no();
      }
    };
  }

  public static AssociationObjectFactory invocationChild(final String source, final boolean required) {
    return new AssociationObjectFactory() {
      @Override
      public String toString() {
        return source;
      }

      public AssociationObject create() {
        return new AssociationObject(source, new InvocationChildAssociation(source), required);
      }
    };
  }
}
