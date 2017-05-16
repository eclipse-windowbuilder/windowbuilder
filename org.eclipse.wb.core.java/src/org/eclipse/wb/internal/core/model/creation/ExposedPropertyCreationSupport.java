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
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoMethodAssociationOnParse;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateIcon;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.graphics.Image;

import java.lang.reflect.Method;

/**
 * Implementation of {@link CreationSupport} for object exposed using property (
 * <code>getXXX()</code> method).
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class ExposedPropertyCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport,
      IExposedCreationSupport {
  private static final String IS_SET_REPLACED = "ExposedPropertyCreationSupport.isSetReplaced";
  private final JavaInfo m_hostJavaInfo;
  private final Method m_getMethod;
  private final String m_getMethodSignature;
  private final String m_setMethodSignature;
  private final boolean m_direct;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExposedPropertyCreationSupport(JavaInfo hostJavaInfo,
      Method getMethod,
      Method setMethod,
      boolean direct) {
    m_hostJavaInfo = hostJavaInfo;
    m_getMethod = getMethod;
    m_direct = direct;
    m_getMethodSignature = ReflectionUtils.getMethodSignature(m_getMethod);
    m_setMethodSignature = setMethod != null ? ReflectionUtils.getMethodSignature(setMethod) : null;
    // add listeners to remove/add exposed component when "setXXX()" added/removed
    if (setMethod != null) {
      m_hostJavaInfo.addBroadcastListener(new JavaInfoMethodAssociationOnParse() {
        public void invoke(JavaInfo parent, JavaInfo child, MethodDescription methodDescription)
            throws Exception {
          if (parent == m_hostJavaInfo) {
            if (m_setMethodSignature.equals(methodDescription.getSignature())) {
              parent.removeChild(m_javaInfo);
              child.putArbitraryValue(IS_SET_REPLACED, Boolean.TRUE);
            }
          }
        }
      });
      m_hostJavaInfo.addBroadcastListener(new ObjectInfoDelete() {
        @Override
        public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
          // when delete component, associated using "setXXX()", restore original exposed component
          if (parent == m_hostJavaInfo && child != m_javaInfo) {
            MethodInvocation invocation = m_hostJavaInfo.getMethodInvocation(m_setMethodSignature);
            if (invocation != null) {
              Expression argument = (Expression) invocation.arguments().get(0);
              JavaInfo removingChild = m_hostJavaInfo.getChildRepresentedBy(argument);
              if (removingChild != null) {
                parent.addChild(m_javaInfo);
              }
            }
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "method: " + ReflectionUtils.toString(m_getMethod);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    // evaluation
    m_hostJavaInfo.addBroadcastListener(new JavaInfoSetObjectAfter() {
      public void invoke(JavaInfo target, Object o) throws Exception {
        if (target == m_hostJavaInfo) {
          if (m_javaInfo.getObject() == null) {
            // get object, may fail if not right time, case 47105
            Object object;
            try {
              object = m_getMethod.invoke(o);
            } catch (Throwable e) {
              object = null;
            }
            // set object
            if (object != null) {
              m_javaInfo.setObject(object);
            }
          }
        }
      }
    });
    // icon decorator
    m_javaInfo.addBroadcastListener(new ObjectInfoPresentationDecorateIcon() {
      public void invoke(ObjectInfo object, Image[] icon) throws Exception {
        if (object == m_javaInfo) {
          Image decorator = DesignerPlugin.getImage("exposed/decorator.gif");
          icon[0] =
              SwtResourceManager.decorateImage(icon[0], decorator, SwtResourceManager.BOTTOM_RIGHT);
        }
      }
    });
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      return invocation.arguments().isEmpty()
          && invocation.getName().getIdentifier().equals(m_getMethod.getName())
          && m_hostJavaInfo.isRepresentedBy(invocation.getExpression());
    }
    return false;
  }

  @Override
  public ASTNode getNode() {
    return m_hostJavaInfo.getCreationSupport().getNode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link JavaInfo} that exposes this {@link JavaInfo}.
   */
  public JavaInfo getHostJavaInfo() {
    return m_hostJavaInfo;
  }

  /**
   * @return the {@link Method} used to expose this {@link JavaInfo}.
   */
  public Method getMethod() {
    return m_getMethod;
  }

  /**
   * @return <code>true</code> if this {@link JavaInfo} is direct child of host {@link JavaInfo}.
   */
  public boolean isDirect() {
    return m_direct;
  }

  /**
   * @return <code>true</code> if given {@link JavaInfo} was set using "setXXX()" during parsing
   *         instead of some exposed child.
   */
  public static boolean isReplacementForExposed(JavaInfo javaInfo) {
    return javaInfo.getArbitraryValue(IS_SET_REPLACED) != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Components with {@link ExposedPropertyCreationSupport} can be "deleted", but for them this
   * means that they delete their children and related nodes, but keep themselves in parent.
   */
  @Override
  public boolean canDelete() {
    return true;
  }

  @Override
  public void delete() throws Exception {
    JavaInfoUtils.deleteJavaInfo(m_javaInfo, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardImplicitCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public IClipboardImplicitCreationSupport getImplicitClipboard() {
    final String getMethodSignature = m_getMethodSignature;
    return new IClipboardImplicitCreationSupport() {
      private static final long serialVersionUID = 0L;

      public JavaInfo find(JavaInfo host) throws Exception {
        for (JavaInfo child : host.getChildrenJava()) {
          if (child.getCreationSupport() instanceof ExposedPropertyCreationSupport) {
            ExposedPropertyCreationSupport exposedCreation =
                (ExposedPropertyCreationSupport) child.getCreationSupport();
            if (exposedCreation.m_getMethodSignature.equals(getMethodSignature)) {
              return child;
            }
          }
        }
        return null;
      }
    };
  }
}
