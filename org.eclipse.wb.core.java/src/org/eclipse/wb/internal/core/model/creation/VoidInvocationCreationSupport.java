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

import com.google.common.base.Predicate;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfosetObjectBefore;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAllProperties;
import org.eclipse.wb.internal.core.model.creation.factory.AbstractFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Abstract {@link CreationSupport} for components created by {@link MethodInvocation} that return
 * <code>void</code>.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public abstract class VoidInvocationCreationSupport extends AbstractFactoryCreationSupport {
  private final JavaInfo m_hostJavaInfo;

  //private MethodInvocation m_invocation;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VoidInvocationCreationSupport(JavaInfo hostJavaInfo,
      MethodDescription description,
      MethodInvocation invocation) {
    super(description, invocation);
    m_hostJavaInfo = hostJavaInfo;
    //m_invocation = invocation;
  }

  /**
   * Creates new {@link VoidInvocationCreationSupport} for adding new component.
   *
   * @param hostJavaInfo
   *          the {@link JavaInfo} for which {@link MethodInvocation} should be added.
   */
  public VoidInvocationCreationSupport(JavaInfo hostJavaInfo, MethodDescription description) {
    super(description);
    m_hostJavaInfo = hostJavaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "void";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canUseParent(JavaInfo parent) throws Exception {
    // TODO consider to implement
    return super.canUseParent(parent);
  }

  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    m_javaInfo.addBroadcastListener(new JavaInfosetObjectBefore() {
      public void invoke(JavaInfo target, Object[] objectRef) throws Exception {
        if (target == m_javaInfo) {
          if (m_javaInfo.getCreationSupport() == VoidInvocationCreationSupport.this) {
            Object hostObject = m_hostJavaInfo.getObject();
            objectRef[0] = getObject(hostObject);
          } else {
            m_javaInfo.removeBroadcastListener(this);
          }
        }
      }
    });
    m_javaInfo.addBroadcastListener(new ObjectInfoAllProperties() {
      public void invoke(ObjectInfo object, List<Property> properties) throws Exception {
        if (object == m_javaInfo) {
          if (!m_description.hasTrueTag("voidFactory.dontFilterProperties")) {
            Predicate<Property> predicate = PropertyUtils.getIncludeByTitlePredicate("Factory");
            PropertyUtils.filterProperties(properties, predicate);
          }
        }
      }
    });
  }

  /**
   * Void methods don't return any value, so we need so special way to access created object.
   *
   * @param hostObject
   *          the {@link Object} or host {@link JavaInfo}.
   * @return the object of this {@link JavaInfo}.
   */
  protected abstract Object getObject(Object hostObject) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets underlying {@link MethodInvocation}, useful to perform morphing into different method.
   * <p>
   * TODO may be move into {@link AbstractFactoryCreationSupport}.
   */
  public final void setInvocation(MethodInvocation invocation) {
    m_invocation = invocation;
    m_javaInfo.bindToExpression(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source of {@link MethodInvocation} for {@link #add_getSource(NodeTarget)}, starting
   *         from method name, without "expression" part, for example <code>addSeparator()</code>.
   */
  protected abstract String add_getMethodSource() throws Exception;

  @Override
  public final String add_getSource(NodeTarget target) throws Exception {
    return TemplateUtils.format("{0}.{1}", m_hostJavaInfo, add_getMethodSource());
  }

  @Override
  public final void add_setSourceExpression(Expression expression) throws Exception {
    setInvocation((MethodInvocation) expression);
  }
}
