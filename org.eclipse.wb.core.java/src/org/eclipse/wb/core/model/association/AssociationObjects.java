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

import org.eclipse.wb.internal.core.model.nonvisual.NonVisualAssociation;

/**
 * Helper for creating {@link AssociationObject}.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class AssociationObjects {
  public static AssociationObject no() {
    return new AssociationObject("NO", null, false);
  }

  public static AssociationObject empty() {
    return new AssociationObject("empty", new EmptyAssociation(), false);
  }

  public static AssociationObject nonVisual() {
    return new AssociationObject("nonVisual", new NonVisualAssociation(), false);
  }

  public static AssociationObject constructorChild() {
    return new AssociationObject("constructorChild", new ConstructorChildAssociation(), false);
  }

  public static AssociationObject factoryParent() {
    return new AssociationObject("factoryParent", new FactoryParentAssociation(), false);
  }

  public static AssociationObject invocationVoid() {
    return new AssociationObject("invocationVoid", new InvocationVoidAssociation(), false);
  }

  public static AssociationObject invocationChildNull() {
    return new AssociationObject("invocationChildNull",
        new InvocationChildAssociation((String) null),
        false);
  }

  public static AssociationObject invocationChild(String source, boolean required) {
    return new AssociationObject(source, new InvocationChildAssociation(source), required);
  }
}
