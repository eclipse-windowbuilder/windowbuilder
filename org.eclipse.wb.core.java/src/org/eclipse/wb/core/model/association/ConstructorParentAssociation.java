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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

/**
 * Implementation of {@link Association} for {@link ClassInstanceCreation} when <em>parent</em>
 * passed as argument.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class ConstructorParentAssociation extends ConstructorAssociation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ClassInstanceCreation getCreation() {
    return getConstructorCreationSupport().getCreation();
  }

  /**
   * @return the {@link ConstructorCreationSupport} for this {@link JavaInfo}.
   */
  private ConstructorCreationSupport getConstructorCreationSupport() {
    return (ConstructorCreationSupport) m_javaInfo.getCreationSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setParent(JavaInfo parent) throws Exception {
    // prepare description
    ConstructorDescription description = getConstructorCreationSupport().getDescription();
    // update association
    AssociationUtils.updateParentAssociation(
        description,
        DomGenerics.arguments(getCreation()),
        parent);
    // in general, replacement of constructor argument can cause binding (so constructor method) change,
    // for example "new TreeItem(Tree,style)" -> "new TreeItem(TreeItem,style)",
    // so replace binding to reflect such change
    m_editor.replaceInvocationBinding(getCreation());
  }

  @Override
  public boolean remove() throws Exception {
    // parent argument can not be removed from constructor, just do nothing
    return false;
  }

  @Override
  public Association getCopy() {
    return new ConstructorParentAssociation();
  }
}
