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
package org.eclipse.wb.internal.swing.java6.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.layout.group.model.GroupLayoutCodeSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.dom.Expression;

import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutInterval;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutCodeGenerator;

import java.awt.Component;
import java.awt.Dimension;
import java.io.StringWriter;
import java.util.List;

import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * Swing implementation.
 * 
 * @author mitin_aa
 */
public final class SwingGroupLayoutCodeSupport extends GroupLayoutCodeSupport
    implements
      LayoutConstants {
  private boolean m_isOldLayout;
  private GroupLayoutInfo2 m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingGroupLayoutCodeSupport(GroupLayoutInfo2 layout) {
    super(layout);
    m_layout = layout;
    CreationSupport creationSupport = layout.getCreationSupport();
    if (creationSupport instanceof IImplicitCreationSupport) {
      m_isOldLayout =
          "org.jdesktop.layout.GroupLayout".equals(layout.getDescription().getComponentClass().getName());
    } else if (creationSupport.getNode() instanceof Expression) {
      Expression node = (Expression) creationSupport.getNode();
      m_isOldLayout =
          node != null
              ? AstNodeUtils.isSuccessorOf(node, "org.jdesktop.layout.GroupLayout")
              : false;
    } else {
      m_isOldLayout = false;
    }
    // initialize names and signatures
    initialize(m_isOldLayout);
    if (m_isOldLayout) {
      m_layout.addBroadcastListener(new JavaEventListener() {
        @Override
        public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
            throws Exception {
          if (oldChild == m_layout) {
            m_layout = (GroupLayoutInfo2) newChild;
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  private void initialize(boolean old) {
    if (!old) {
      GROUP_LAYOUT_CLASS_NAME = "javax.swing.GroupLayout";
      ID_ADD_GAP = "addGap";
      ID_ADD_COMPONENT = "addComponent";
      ID_ADD_GROUP = "addGroup";
      SIGNATURE_LINK_SIZE = ID_LINK_SIZE + "(java.awt.Component[])";
      SIGNATURE_LINK_SIZE_AXIS = "linkSize(int,java.awt.Component[])";
    } else {
      GROUP_LAYOUT_CLASS_NAME = "org.jdesktop.layout.GroupLayout";
      ID_ADD_GAP = "add";
      ID_ADD_COMPONENT = "add";
      ID_ADD_GROUP = "add";
      SIGNATURE_LINK_SIZE = ID_LINK_SIZE + "(java.awt.Component[])";
      SIGNATURE_LINK_SIZE_AXIS = "linkSize(java.awt.Component[],int)";
    }
    GROUP_LAYOUT_GROUP_CLASS_NAME = GROUP_LAYOUT_CLASS_NAME + ".Group";
    SIGNATURE_SET_HORIZONTAL_GROUP =
        ID_SET_HORIZONTAL_GROUP + "(" + GROUP_LAYOUT_GROUP_CLASS_NAME + ")";
    SIGNATURE_SET_VERTICAL_GROUP =
        ID_SET_VERTICAL_GROUP + "(" + GROUP_LAYOUT_GROUP_CLASS_NAME + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String prepareLayoutCode(List<AbstractComponentInfo> components) throws Exception {
    // generate always new layout code
    if (m_isOldLayout) {
      initialize(!m_isOldLayout);
      convertLayout();
      m_isOldLayout = false;
    }
    // proceed with generation
    SwingLayoutCodeGenerator.ComponentInfo[] infos =
        new SwingLayoutCodeGenerator.ComponentInfo[components.size()];
    int i = 0;
    for (AbstractComponentInfo abstractComponent : components) {
      ComponentInfo component = (ComponentInfo) abstractComponent;
      SwingLayoutCodeGenerator.ComponentInfo info = new SwingLayoutCodeGenerator.ComponentInfo();
      info.id = ObjectInfoUtils.getId(component);
      info.variableName = TemplateUtils.ID_PREFIX + info.id;
      info.clazz = component.getDescription().getComponentClass();
      Property minProp = component.getPropertyByTitle("minimumSize");
      Property prefProp = component.getPropertyByTitle("preferredSize");
      Property maxProp = component.getPropertyByTitle("maximumSize");
      info.sizingChanged =
          !((minProp == null || !minProp.isModified())
              && (prefProp == null || !prefProp.isModified()) && (maxProp == null || !maxProp.isModified()));
      {
        Component componentObject = component.getComponent();
        info.minSize = componentObject != null ? componentObject.getMinimumSize() : new Dimension();
      }
      infos[i++] = info;
    }
    String contVarName = getLayoutReference();
    StringWriter stringWriter = new StringWriter();
    SwingLayoutCodeGenerator swingGenerator =
        new SwingLayoutCodeGenerator(getLayout().getLayoutModel());
    swingGenerator.generateContainerLayout(
        stringWriter,
        getRootComponent(),
        contVarName,
        infos,
        false);
    return stringWriter.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void checkComponent(AbstractComponentInfo component, int dimension) {
    if (JTextField.class.isAssignableFrom(component.getDescription().getComponentClass())
        && dimension == HORIZONTAL) {
      LayoutInterval interval =
          getLayoutModel().getLayoutComponent(ObjectInfoUtils.getId(component)).getLayoutInterval(
              dimension);
      interval.setSizes(NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED);
    }
  }

  @Override
  protected void setGroupAlignment(LayoutInterval group, Expression arg) {
    if (!m_isOldLayout) {
      Alignment alignment = (Alignment) JavaInfoEvaluationHelper.getValue(arg);
      group.setGroupAlignment(alignment.ordinal());
    } else {
      Number alignment = (Number) JavaInfoEvaluationHelper.getValue(arg);
      group.setGroupAlignment(convertOldAlignment(alignment.intValue()));
    }
  }

  @Override
  protected void setAlignment(LayoutInterval interval, Expression arg) {
    if (!m_isOldLayout) {
      Alignment alignment = (Alignment) JavaInfoEvaluationHelper.getValue(arg);
      interval.setAlignment(alignment.ordinal());
    } else {
      Number alignment = (Number) JavaInfoEvaluationHelper.getValue(arg);
      interval.setAlignment(convertOldAlignment(alignment.intValue()));
    }
  }

  @Override
  protected void setPaddingType(LayoutInterval gap, Expression arg) {
    if (!m_isOldLayout) {
      ComponentPlacement placement = (ComponentPlacement) JavaInfoEvaluationHelper.getValue(arg);
      gap.setPaddingType(convertPadding(placement));
    } else {
      Number placement = (Number) JavaInfoEvaluationHelper.getValue(arg);
      gap.setPaddingType(convertOldPadding(placement.intValue()));
    }
  }

  @Override
  protected boolean isComponent(Expression arg) {
    return AstNodeUtils.isSuccessorOf(arg, Component.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  private PaddingType convertPadding(ComponentPlacement value) {
    switch (value) {
      case RELATED :
        return PaddingType.RELATED;
      case UNRELATED :
        return PaddingType.UNRELATED;
      case INDENT :
        return PaddingType.INDENT;
    }
    return PaddingType.SEPARATE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Old layout
  //
  ////////////////////////////////////////////////////////////////////////////
  private void convertLayout() throws Exception {
    Class<?> componentClass =
        EditorState.get(m_layout.getEditor()).getEditorLoader().loadClass("javax.swing.GroupLayout");
    ComponentDescription layoutDescription =
        ComponentDescriptionHelper.getDescription(m_layout.getEditor(), componentClass);
    try {
      layoutDescription.putTag(GroupLayoutInfo2.FLAG_IS_MORPHING, "true");
      MorphingTargetDescription target = new MorphingTargetDescription(componentClass, null);
      MorphingSupport.morph("java.awt.Component", m_layout, target);
    } finally {
      layoutDescription.putTag(GroupLayoutInfo2.FLAG_IS_MORPHING, "false");
    }
  }

  @Override
  protected int convertDimension(int dimension) {
    if (m_isOldLayout) {
      if (dimension == 1 /*GroupLayout.HORIZONTAL*/) {
        return HORIZONTAL;
      } else if (dimension == 2 /*GroupLayout.VERTICAL*/) {
        return VERTICAL;
      }
    }
    return super.convertDimension(dimension);
  }

  private int convertOldAlignment(int value) {
    switch (value) {
      case 4 /*GroupLayout.CENTER*/:
        return CENTER;
      case 3 /*GroupLayout.BASELINE*/:
        return BASELINE;
      case 1 /*GroupLayout.LEADING*/:
        return LEADING;
      case 2 /*GroupLayout.TRAILING*/:
        return TRAILING;
      default :
        return DEFAULT;
    }
  }

  private PaddingType convertOldPadding(int value) {
    switch (value) {
      case 0 /*RELATED*/:
        return PaddingType.RELATED;
      case 1 /*UNRELATED*/:
        return PaddingType.UNRELATED;
      case 2 /*INDENT*/:
        return PaddingType.INDENT;
    }
    return PaddingType.SEPARATE;
  }
}
