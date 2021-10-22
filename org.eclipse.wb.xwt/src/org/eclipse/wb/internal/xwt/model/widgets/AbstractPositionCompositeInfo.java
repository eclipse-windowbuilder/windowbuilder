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
package org.eclipse.wb.internal.xwt.model.widgets;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Set;

/**
 * Model for {@link Composite} that has methods like
 * <code>setContent(org.eclipse.swt.widgets.Control)</code>.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public abstract class AbstractPositionCompositeInfo extends CompositeInfo {
  private final String[] m_properties;
  private final AbstractPositionInfo[] m_positions;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractPositionCompositeInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport,
      String[] properties) throws Exception {
    super(context, description, creationSupport);
    m_properties = properties;
    {
      m_positions = new AbstractPositionInfo[m_properties.length];
      for (int i = 0; i < m_properties.length; i++) {
        String method = m_properties[i];
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
          for (String method : m_properties) {
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
  public final ControlInfo getControl(String property) {
    for (ControlInfo control : getChildrenControls()) {
      DocumentElement controlElement = control.getCreationSupport().getElement();
      String propertyTag = controlElement.getParent().getTag();
      if (propertyTag.endsWith("." + property)) {
        return control;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new XmlObjectPresentation(this) {
    @Override
    public List<ObjectInfo> getChildrenTree() throws Exception {
      List<ObjectInfo> children = Lists.newArrayList(super.getChildrenTree());
      Set<ControlInfo> positionedControls = Sets.newHashSet();
      for (int i = 0; i < m_properties.length; i++) {
        String property = m_properties[i];
        ControlInfo control = getControl(property);
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
   * Creates new {@link ControlInfo} and associates it with given property.
   */
  public final void command_CREATE(ControlInfo control, String property) throws Exception {
    XmlObjectUtils.add(control, Associations.property(property), this, null);
  }

  /**
   * Moves existing {@link ControlInfo} and associates it with given property.
   */
  public final void command_MOVE(ControlInfo control, String property) throws Exception {
    ControlInfo nextControl = getNextControl(control, property);
    XmlObjectUtils.move(control, Associations.property(property), this, nextControl);
  }

  /**
   * @return the {@link ControlInfo} to use as reference when move into given position.
   */
  private ControlInfo getNextControl(ControlInfo movingControl, String property) {
    int index = ArrayUtils.indexOf(m_properties, property);
    Assert.isLegal(index >= 0, "Invalid position: " + property);
    for (int i = index + 1; i < m_properties.length; i++) {
      String method = m_properties[i];
      ControlInfo propertyControl = getControl(method);
      if (propertyControl != null && propertyControl != movingControl) {
        return propertyControl;
      }
    }
    return null;
  }
}
