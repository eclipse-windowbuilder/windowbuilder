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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link Association} that consists of several other {@link Association}.
 * <p>
 * For example in SWT {@link Menu} is associated with its {@link MenuItem} using <em>two</em> AST
 * nodes: {@link Menu} constructor and {@link MenuItem#setMenu(Menu)} invocation. So, we need also
 * two {@link Association}'s - for creation and for {@link MenuItem#setMenu(Menu)}. Note, that
 * instead of constructor we may have also some factory, so we can not use just
 * {@link ConstructorParentAssociation} for creation.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class CompoundAssociation extends Association {
  private final List<Association> m_associations = Lists.newArrayList();
  private final List<Association> m_newAssociations = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompoundAssociation(Association... associations) {
    for (Association association : associations) {
      Assert.isNotNull(association);
      if (association instanceof CompoundAssociation) {
        CompoundAssociation compoundAssociation = (CompoundAssociation) association;
        m_associations.addAll(compoundAssociation.m_associations);
      } else {
        m_associations.add(association);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compound access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Association} that form this {@link CompoundAssociation}.
   */
  public List<Association> getAssociations() {
    return m_associations;
  }

  /**
   * Adds new {@link Association}, to add when {@link #setParent(JavaInfo)} is used.
   */
  public void add(Association association) {
    Assert.isNotNull(association);
    m_newAssociations.add(association);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    // set JavaInfo for sub-associations
    for (Association association : m_associations) {
      // when CompoundAssociation is mutated from single Association, it may already have JavaInfo
      if (association.getJavaInfo() != null) {
        Assert.isTrue(association.getJavaInfo() == javaInfo);
      } else {
        association.setJavaInfo(javaInfo);
      }
    }
  }

  @Override
  public boolean canDelete() {
    for (Association association : m_associations) {
      if (!association.canDelete()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Statement getStatement() {
    Assert.isTrue(!m_associations.isEmpty());
    return m_associations.get(0).getStatement();
  }

  @Override
  public String getSource() {
    Assert.isTrue(!m_associations.isEmpty());
    return m_associations.get(0).getSource();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(JavaInfo javaInfo, StatementTarget target, String[] leadingComments)
      throws Exception {
    // add individual associations
    for (Association association : m_associations) {
      association.add(javaInfo, target, leadingComments);
      leadingComments = null;
    }
    // set association
    setInModelNoCompound(javaInfo);
  }

  @Override
  public boolean remove() throws Exception {
    // exclude associations that are removed
    for (Iterator<Association> I = m_associations.iterator(); I.hasNext();) {
      Association association = I.next();
      if (association.remove()) {
        I.remove();
      }
    }
    // compound is removed if all its children are removed
    if (m_associations.isEmpty()) {
      removeFromModelIfPrimary();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void setParent(JavaInfo parent) throws Exception {
    // update existing associations
    for (Association association : m_associations) {
      association.setParent(parent);
    }
    // add new associations
    {
      StatementTarget target = new StatementTarget(getStatement(), false);
      for (Association association : m_newAssociations) {
        association.add(m_javaInfo, target, null);
        m_associations.add(association);
      }
      // new associations are accepted
      m_newAssociations.clear();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morph
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Association getCopy() {
    Association[] newAssociations = new Association[m_associations.size()];
    for (int i = 0; i < m_associations.size(); ++i) {
      newAssociations[i] = m_associations.get(i).getCopy();
    }
    return new CompoundAssociation(newAssociations);
  }
}
