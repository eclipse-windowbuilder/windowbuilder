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
package org.eclipse.wb.core.gef.policy;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.DirectTextEditPolicy;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;

/**
 * {@link EditPolicy} that support direct editing for text {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class DirectTextPropertyEditPolicy extends DirectTextEditPolicy {
  public static final Object KEY = DirectTextPropertyEditPolicy.class;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Installation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If given {@link AbstractComponentInfo} has path to the property in parameter
   * <code>directEdit.property</code>, installs {@link DirectTextPropertyEditPolicy} for this
   * property.
   */
  public static void install(AbstractComponentEditPart editPart) {
    AbstractComponentInfo component = editPart.getComponent();
    install(editPart, component);
  }

  /**
   * If given {@link AbstractComponentInfo} has path to the property in parameter
   * <code>directEdit.property</code>, installs {@link DirectTextPropertyEditPolicy} for this
   * property.
   */
  public static void install(final EditPart editPart, final AbstractComponentInfo component) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        installEx(editPart, component);
      }
    });
  }

  /**
   * Implementation for {@link #install(EditPart, AbstractComponentInfo)}.
   */
  private static void installEx(EditPart editPart, AbstractComponentInfo component)
      throws Exception {
    // try to find property with "isText" tag
    for (Property property : component.getProperties()) {
      if (property instanceof GenericPropertyImpl) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
        GenericPropertyDescription description = genericProperty.getDescription();
        if (genericProperty.getJavaInfo() == component
            && description != null
            && description.hasTrueTag("isText")) {
          EditPolicy policy = new DirectTextPropertyEditPolicy(component, property);
          editPart.installEditPolicy(KEY, policy);
          return;
        }
      }
    }
    // try to find property using full path
    {
      String propertyPath = component.getDescription().getParameter("directEdit.property");
      if (propertyPath != null) {
        Property property = PropertyUtils.getByPath(component, propertyPath);
        if (property != null) {
          EditPolicy policy = new DirectTextPropertyEditPolicy(component, property);
          editPart.installEditPolicy(policy);
          return;
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AbstractComponentInfo m_component;
  private final Property m_property;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DirectTextPropertyEditPolicy(AbstractComponentInfo component, Property property) {
    m_component = component;
    m_property = property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return (String) m_property.getValue();
      }
    }, StringUtils.EMPTY);
  }

  @Override
  protected void setText(final String text) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        m_property.setValue(text);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Point getTextWidgetLocation(Rectangle hostBounds, Dimension textSize) {
    try {
      // apply insets
      {
        String insetsString = JavaInfoUtils.getParameter(m_component, "directEdit.location.insets");
        if (insetsString != null) {
          String insetsElements[] = StringUtils.split(insetsString);
          Assert.equals(4, insetsElements.length);
          hostBounds.y += Integer.parseInt(insetsElements[0]);
          hostBounds.x += Integer.parseInt(insetsElements[1]);
          hostBounds.height -= Integer.parseInt(insetsElements[2]);
          hostBounds.width -= Integer.parseInt(insetsElements[3]);
        }
      }
      // prepare "x"
      int x;
      {
        String horizontalAlignment =
            JavaInfoUtils.getParameter(m_component, "directEdit.location.horizontalAlignment");
        if (horizontalAlignment == null || "center".equals(horizontalAlignment)) {
          x = hostBounds.getCenter().x - textSize.width / 2;
        } else if ("left".equals(horizontalAlignment)) {
          x = hostBounds.left();
        } else {
          throw new IllegalArgumentException("Unknown horizontal alignment: " + horizontalAlignment);
        }
      }
      // prepare "y"
      int y;
      {
        String verticalAlignment =
            JavaInfoUtils.getParameter(m_component, "directEdit.location.verticalAlignment");
        if (verticalAlignment == null || "center".equals(verticalAlignment)) {
          y = hostBounds.getCenter().y - textSize.height / 2;
        } else if ("top".equals(verticalAlignment)) {
          y = hostBounds.top();
        } else {
          throw new IllegalArgumentException("Unknown vertical alignment: " + verticalAlignment);
        }
      }
      // final location
      return new Point(x, y);
    } catch (Throwable e) {
      throw new Error("Direct edit location for " + m_component, e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_ALREADY_EDITED = "alreadyDirectEdited";

  @Override
  public void activate() {
    super.activate();
    // try automatically activate direct edit
    IPreferenceStore preferences = m_component.getDescription().getToolkit().getPreferences();
    if (preferences.getBoolean(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD)
        && m_component.getArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT) == Boolean.TRUE
        && m_component.getArbitraryValue(KEY_ALREADY_EDITED) == null) {
      m_component.putArbitraryValue(KEY_ALREADY_EDITED, Boolean.TRUE);
      beginEdit();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      if (keyRequest.isPressed() && keyRequest.getCharacter() == ' ') {
        beginEdit();
      }
    }
    super.performRequest(request);
  }
}
