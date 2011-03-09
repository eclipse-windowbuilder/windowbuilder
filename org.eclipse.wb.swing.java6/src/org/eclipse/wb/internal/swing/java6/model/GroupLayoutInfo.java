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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.swing.java6.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.graphics.Image;

import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;

/**
 * Model for {@link GroupLayout}.
 * 
 * @author mitin_aa
 * @coverage swing.model.layout.group
 */
public final class GroupLayoutInfo extends LayoutInfo implements IAbsoluteLayoutCommands {
  // constants
  private static final String GROUP_LAYOUT_CLASS_NAME = "javax.swing.GroupLayout";
  static final String GROUP_LAYOUT_GROUP_CLASS_NAME = GROUP_LAYOUT_CLASS_NAME + ".Group";
  static final String PROPERTY_NAME_GROUP = "group";
  public static final String IDENTIFIER_CREATE_PARALLEL_GROUP = "createParallelGroup";
  static final String IDENTIFIER_CREATE_SEQUENTIAL_GROUP = "createSequentialGroup";
  public static final String IDENTIFIER_CREATE_BASELINE_GROUP = "createBaselineGroup";
  static final String IDENTIFIER_ADD_CONTAINER_GAP = "addContainerGap";
  static final String IDENTIFIER_ADD_PREFERRED_GAP = "addPreferredGap";
  static final String IDENTIFIER_ADD_GAP = "addGap";
  static final String IDENTIFIER_ADD_COMPONENT = "addComponent";
  static final String IDENTIFIER_ADD_GROUP = "addGroup";
  private static final String SIGNATURE_SET_HORIZONTAL_GROUP =
      "setHorizontalGroup(javax.swing.GroupLayout.Group)";
  private static final String SIGNATURE_SET_VERTICAL_GROUP =
      "setVerticalGroup(javax.swing.GroupLayout.Group)";
  // fields
  private final SpringInfo[] m_rootGroups = new SpringInfo[2];
  @SuppressWarnings("unchecked")
  private final Map[] m_widgetMaps = new Map[2];

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    m_widgetMaps[0] = Maps.newHashMap();
    m_widgetMaps[1] = Maps.newHashMap();
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void bindComponents(List<JavaInfo> components) throws Exception {
        parse();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout manipulation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void adjustAttachmentOffset(IAbstractComponentInfo widget, int side, int moveDelta)
      throws Exception {
  }

  @Override
  public void attachAbsolute(IAbstractComponentInfo widget, int side, int distance)
      throws Exception {
    // TODO: for testing
    save();
  }

  @Override
  public void attachWidgetParallelly(IAbstractComponentInfo widget,
      IAbstractComponentInfo attachToWidget,
      int side,
      int distance) throws Exception {
  }

  @Override
  public void attachWidgetSequientially(IAbstractComponentInfo widget,
      IAbstractComponentInfo attachToWidget,
      int side,
      int distance) throws Exception {
  }

  @Override
  public void detach(IAbstractComponentInfo widget, int side) throws Exception {
  }

  @Override
  public IAbstractComponentInfo getAttachedToWidget(IAbstractComponentInfo widget, int side)
      throws Exception {
    return null;
  }

  @Override
  public boolean isAttached(IAbstractComponentInfo widget, int side) throws Exception {
    return false;
  }

  @Override
  public void performAction(int actionId) {
  }

  @Override
  public void setExplicitSize(IAbstractComponentInfo widget,
      int side,
      int draggingSide,
      int resizeDelta) throws Exception {
  }

  public ComponentAttachmentInfo getComponentAttachmentInfo(IAbstractComponentInfo widget, int side) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void command_CREATE(ComponentInfo component, ComponentInfo nextComponent)
      throws Exception {
    add(component, null, nextComponent);
  }

  public final void command_MOVE(ComponentInfo component, ComponentInfo nextComponent)
      throws Exception {
    move(component, null, nextComponent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Image getImage(String imageName) {
    return Activator.getImage("info/layout/groupLayout/" + imageName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save layout code
  //
  ////////////////////////////////////////////////////////////////////////////
  private void save() throws Exception {
    saveGroup(SIGNATURE_SET_HORIZONTAL_GROUP, m_rootGroups[0]);
    saveGroup(SIGNATURE_SET_VERTICAL_GROUP, m_rootGroups[1]);
    // TODO: link size
  }

  private void saveGroup(String methodName, SpringInfo rootGroup) throws Exception {
    /*List<ComponentInfo> childrenComponents = getContainer().getChildrenComponents();
    StatementTarget statementTarget =
    		JavaInfoUtils.getStatementTarget_whenAllCreated(ImmutableList.copyOf(childrenComponents));
    String referenceExpression =
    		getVariableSupport().getReferenceExpression(new NodeTarget(statementTarget));*/
    MethodInvocation invocation = getMethodInvocation(methodName);
    String groupCode = rootGroup.getCode();
    getEditor().replaceInvocationArguments(invocation, ImmutableList.of(groupCode));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  protected void parse() {
    try {
      m_rootGroups[0] = parseGroup(SIGNATURE_SET_HORIZONTAL_GROUP, m_widgetMaps[0]);
      m_rootGroups[1] = parseGroup(SIGNATURE_SET_VERTICAL_GROUP, m_widgetMaps[1]);
    } catch (Throwable e) {
      throw new DesignerException(1100, e);
    }
    if (m_rootGroups[0] == null || m_rootGroups[1] == null) {
      throw new DesignerException(1100);
    }
  }

  protected SpringInfo parseGroup(String methodName,
      Map<IAbstractComponentInfo, WidgetSpringInfo> widgetMaps) {
    MethodInvocation invocation = getMethodInvocation(methodName);
    GroupLayoutParserVisitor parser =
        new GroupLayoutParserVisitor(widgetMaps, methodName, getContainer());
    invocation.accept(parser);
    return parser.getRootGroup();
  }

  @Override
  public void attachWidgetBaseline(IAbstractComponentInfo widget,
      IAbstractComponentInfo attachedToWidget) throws Exception {
  }
}
