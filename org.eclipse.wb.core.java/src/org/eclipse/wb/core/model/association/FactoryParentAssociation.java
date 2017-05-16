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
import org.eclipse.wb.internal.core.model.creation.factory.AbstractFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Control;

/**
 * Implementation of {@link Association} for {@link MethodInvocation} as separate
 * {@link ExpressionStatement}, when <em>parent</em> passed as argument. Used for factories, i.e.
 * this is association on creation, for example <code>MyStaticFactory.createSWTButton(parent)</code>
 * . It is different than separate association using for example invocation of child method with
 * parent argument (like {@link Control#setParent(org.eclipse.swt.widgets.Composite)}).
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class FactoryParentAssociation extends InvocationAssociation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryParentAssociation() {
  }

  public FactoryParentAssociation(MethodInvocation invocation) {
    super(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    // get MethodInvocation from AbstractFactoryCreationSupport
    {
      AbstractFactoryCreationSupport creationSupport =
          (AbstractFactoryCreationSupport) javaInfo.getCreationSupport();
      m_invocation = creationSupport.getInvocation();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setParent(JavaInfo parent) throws Exception {
    // prepare description
    MethodDescription description;
    {
      AbstractFactoryCreationSupport creationSupport =
          (AbstractFactoryCreationSupport) m_javaInfo.getCreationSupport();
      description = creationSupport.getDescription();
    }
    // update association
    AssociationUtils.updateParentAssociation(
        description,
        DomGenerics.arguments(m_invocation),
        parent);
  }

  @Override
  public boolean remove() throws Exception {
    // parent argument can not be removed from factory, just do nothing
    return false;
  }

  @Override
  public Association getCopy() {
    // in MorphingSupport we use ConstructorCreationSupport
    return new ConstructorParentAssociation();
  }
}
