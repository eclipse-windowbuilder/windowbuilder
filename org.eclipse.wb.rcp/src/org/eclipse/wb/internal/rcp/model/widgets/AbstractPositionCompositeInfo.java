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
package org.eclipse.wb.internal.rcp.model.widgets;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Set;

/**
 * Model for {@link Composite} that has methods like
 * <code>setContent(org.eclipse.swt.widgets.Control)</code>.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public abstract class AbstractPositionCompositeInfo extends CompositeInfo {
  private final String[] m_methods;
  private final AbstractPositionInfo[] m_positions;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractPositionCompositeInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport,
      String[] methods) throws Exception {
    super(editor, description, creationSupport);
    m_methods = methods;
    {
      m_positions = new AbstractPositionInfo[m_methods.length];
      for (int i = 0; i < m_methods.length; i++) {
        String method = m_methods[i];
        m_positions[i] = new AbstractPositionInfo(this, method);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    // listener that adds prefix with name of position
    addBroadcastListener(new ObjectInfoPresentationDecorateText() {
      public void invoke(ObjectInfo object, String[] text) throws Exception {
        if (object instanceof ControlInfo
            && object.getParent() == AbstractPositionCompositeInfo.this) {
          for (String method : m_methods) {
            if (getControl(method) == object) {
              text[0] = method + " - " + text[0];
              break;
            }
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ControlInfo} set using given <code>setXXX()</code> method.
   */
  public final ControlInfo getControl(String methodName) {
    MethodInvocation invocation =
        getMethodInvocation(methodName + "(org.eclipse.swt.widgets.Control)");
    if (invocation != null) {
      return (ControlInfo) getChildRepresentedBy(DomGenerics.arguments(invocation).get(0));
    } else {
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public List<ObjectInfo> getChildrenTree() throws Exception {
      List<ObjectInfo> children = Lists.newArrayList(super.getChildrenTree());
      Set<ControlInfo> positionedControls = Sets.newHashSet();
      for (int i = 0; i < m_methods.length; i++) {
        String method = m_methods[i];
        ControlInfo control = getControl(method);
        if (control != null && !positionedControls.contains(control)) {
          positionedControls.add(control);
          children.remove(control);
          children.add(i, control);
        } else {
          children.add(i, m_positions[i]);
        }
      }
      return children;
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ControlInfo} and associates it with this
   * {@link AbstractPositionCompositeInfo} using method with given name.
   */
  public final void command_CREATE(ControlInfo control, String methodName) throws Exception {
    AssociationObject association = getAssociation_(methodName);
    JavaInfoUtils.add(control, association, this, null);
  }

  /**
   * Moves existing {@link ControlInfo} and associates it with this
   * {@link AbstractPositionCompositeInfo} using method with given name.
   */
  public final void command_MOVE(ControlInfo control, String methodName) throws Exception {
    // prepare "nextControl", to add before it
    ControlInfo nextControl = null;
    {
      int index = ArrayUtils.indexOf(m_methods, methodName);
      Assert.isLegal(index >= 0, "Invalid method: " + methodName);
      for (int i = index + 1; i < m_methods.length; i++) {
        String method = m_methods[i];
        ControlInfo methodControl = getControl(method);
        if (methodControl != null && methodControl != control) {
          nextControl = methodControl;
          break;
        }
      }
    }
    // do move
    AssociationObject association = getAssociation_(methodName);
    JavaInfoUtils.move(control, association, this, nextControl);
  }

  private static AssociationObject getAssociation_(String methodName) {
    return AssociationObjects.invocationChild("%parent%." + methodName + "(%child%)", true);
  }
}
