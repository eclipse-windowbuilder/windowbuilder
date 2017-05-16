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
package org.eclipse.wb.internal.core.model.creation.factory;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Implementation of {@link CreationSupport} for factory-like methods of some {@link JavaInfo}
 * component.
 * <p>
 * For example {@link JToolBar#add(javax.swing.Action)} internally creates {@link JButton} and sets
 * given {@link Action} for it. It also returns created {@link JButton}, so we can assign it to
 * variable and set more properties.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class ImplicitFactoryCreationSupport extends AbstractFactoryCreationSupport {
  private final String m_signature;
  private JavaInfo m_invocationTarget;
  private String m_invocationSource;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructor for existing {@link JavaInfo} creation using {@link ImplicitFactoryCreationSupport}
   * .
   */
  public ImplicitFactoryCreationSupport(MethodDescription description, MethodInvocation invocation) {
    super(description, invocation);
    m_signature = description.getSignature();
  }

  /**
   * Constructor for creating new {@link JavaInfo} using {@link ImplicitFactoryCreationSupport}.
   */
  public ImplicitFactoryCreationSupport(String signature, String invocationSource) {
    this(null, signature, invocationSource);
  }

  /**
   * Constructor for creating new {@link JavaInfo} using {@link ImplicitFactoryCreationSupport}.
   *
   * @param invocationTarget
   *          the {@link JavaInfo} which method should be invoked, may be <code>null</code> to use
   *          parent {@link JavaInfo}.
   */
  public ImplicitFactoryCreationSupport(JavaInfo invocationTarget,
      String signature,
      String invocationSource) {
    m_invocationTarget = invocationTarget;
    m_signature = signature;
    m_invocationSource = invocationSource;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "implicit-factory";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReorder() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    if (m_invocationTarget != null) {
      return TemplateUtils.format("{0}.{1}", m_invocationTarget, m_invocationSource);
    } else {
      return TemplateUtils.format("%parent%.{0}", m_invocationSource);
    }
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
    m_invocation = (MethodInvocation) expression;
    m_javaInfo.bindToExpression(m_invocation);
    {
      Expression targetExpression = m_invocation.getExpression();
      JavaInfo target = m_javaInfo.getRootJava().getChildRepresentedBy(targetExpression);
      m_description = target.getDescription().getMethod(m_signature);
    }
  }

  @Override
  public Association getAssociation() throws Exception {
    return new InvocationVoidAssociation();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IClipboardCreationSupport getClipboard() throws Exception {
    // support copy/paste only when target/parent are same
    if (!m_javaInfo.getParentJava().isRepresentedBy(m_invocation.getExpression())) {
      return null;
    }
    // OK, simple case
    final String signature = m_signature;
    final String source = getClipboardSource();
    return new IClipboardCreationSupport() {
      private static final long serialVersionUID = 0L;

      @Override
      public CreationSupport create(JavaInfo rootObject) throws Exception {
        return new ImplicitFactoryCreationSupport(null, signature, source);
      }
    };
  }

  private String getClipboardSource() throws Exception {
    String argumentsSource = m_utils.getClipboardArguments(m_description.getParameters());
    return m_description.getName() + "(" + argumentsSource + ")";
  }
}
