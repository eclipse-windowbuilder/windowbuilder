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
package org.eclipse.wb.internal.layout.group.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.parser.JavaInfoResolver;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutInterval;
import org.netbeans.modules.form.layoutdesign.LayoutModel;

import java.util.List;

/**
 * Responsible to parse/save GroupLayout code.
 * 
 * @author mitin_aa
 */
public abstract class GroupLayoutCodeSupport implements LayoutConstants {
  public final static Object ASSOCIATION_EXPRESSION_KEY = GroupLayoutCodeSupport.class;
  protected static final String ID_CREATE_PARALLEL_GROUP = "createParallelGroup";
  protected static final String ID_CREATE_BASELINE_GROUP = "createBaselineGroup";
  final static String ID_CREATE_SEQUENTIAL_GROUP = "createSequentialGroup";
  final static String PROPERTY_NAME_GROUP = "group";
  protected static final String ID_SET_HORIZONTAL_GROUP = "setHorizontalGroup";
  protected static final String ID_SET_VERTICAL_GROUP = "setVerticalGroup";
  protected static final String ID_LINK_SIZE = "linkSize";
  protected static final String ID_ADD_CONTAINER_GAP = "addContainerGap";
  protected static final String ID_ADD_PREFERRED_GAP = "addPreferredGap";
  // 
  protected String GROUP_LAYOUT_CLASS_NAME;
  protected String GROUP_LAYOUT_GROUP_CLASS_NAME;
  protected String ID_ADD_GAP;
  protected String ID_ADD_COMPONENT;
  protected String ID_ADD_GROUP;
  protected String SIGNATURE_SET_HORIZONTAL_GROUP;
  protected String SIGNATURE_SET_VERTICAL_GROUP;
  protected String SIGNATURE_LINK_SIZE;
  protected String SIGNATURE_LINK_SIZE_AXIS;
  //
  protected IAdaptable m_layoutAdaptable;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutCodeSupport(IAdaptable layout) {
    m_layoutAdaptable = layout;
    // support changing 'main' layout JavaInfo instance, used by Swing while converting from old-style layout.
    getJavaInfo().addBroadcastListener(new JavaEventListener() {
      @Override
      public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
          throws Exception {
        if (oldChild == m_layoutAdaptable) {
          m_layoutAdaptable = (IAdaptable) newChild;
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public void parse() {
    LayoutInterval rootGroupH = null;
    LayoutInterval rootGroupV = null;
    {
      try {
        rootGroupH = parseGroup(SIGNATURE_SET_HORIZONTAL_GROUP, HORIZONTAL);
        rootGroupV = parseGroup(SIGNATURE_SET_VERTICAL_GROUP, VERTICAL);
      } catch (Throwable e) {
        throw new DesignerException(1100, e);
      }
      if (rootGroupH == null || rootGroupV == null) {
        throw new DesignerException(1100);
      }
    }
    // root component should already exist!
    LayoutInterval[] rootIntervals = getRootComponent().getLayoutRoots().get(0);
    rootIntervals[HORIZONTAL] = rootGroupH;
    rootIntervals[VERTICAL] = rootGroupV;
    // parse linked sizes
    parseLinkedSizes();
  }

  private LayoutInterval parseGroup(String methodName, int dimension) {
    MethodInvocation invocation = getJavaInfo().getMethodInvocation(methodName);
    if (invocation != null) {
      GroupLayoutParserVisitor2 parser =
          new GroupLayoutParserVisitor2(getLayout().getLayoutContainer(), dimension, this);
      invocation.accept(parser);
      return checkRootGroup(parser.getRootGroup());
    } else {
      // no method invocation
      return new LayoutInterval(PARALLEL);
    }
  }

  /**
   * Netbeans model requires root group always as PARALLEL type. Hand-coded UI or other UI builders
   * may skip this type of group if it has the only one child. The workaround is go move root group
   * inside new parallel group.
   */
  private LayoutInterval checkRootGroup(LayoutInterval root) {
    if (!root.isParallel()) {
      LayoutInterval newRoot = new LayoutInterval(PARALLEL);
      newRoot.add(root, -1);
      return newRoot;
    }
    return root;
  }

  private void parseLinkedSizes() {
    int linkID = 0;
    for (MethodInvocation invocation : getJavaInfo().getMethodInvocations(SIGNATURE_LINK_SIZE)) {
      List<Expression> arguments = DomGenerics.arguments(invocation);
      Expression arg = arguments.get(0);
      // old way
      if (arg instanceof ArrayCreation) {
        ArrayCreation arrayCreation = (ArrayCreation) arg;
        ArrayInitializer arrayInitializer = arrayCreation.getInitializer();
        List<Expression> expressions = DomGenerics.expressions(arrayInitializer);
        for (Expression expression : expressions) {
          addLinkedSize(linkID, expression);
        }
      } else {
        // list of components as args should be here
        for (int i = 0; i < arguments.size(); ++i) {
          addLinkedSize(linkID, arguments.get(i));
        }
      }
      linkID++;
    }
    for (MethodInvocation invocation : getJavaInfo().getMethodInvocations(SIGNATURE_LINK_SIZE_AXIS)) {
      List<Expression> arguments = DomGenerics.arguments(invocation);
      Expression axisArg = arguments.get(0);
      int componentsArgIndex = 1;
      if (arguments.size() == 2 && axisArg instanceof ArrayCreation) {
        // standalone lib and SWT
        axisArg = arguments.get(1);
        componentsArgIndex = 0;
      }
      int axis = convertDimension((Integer) JavaInfoEvaluationHelper.getValue(axisArg));
      Expression arg = arguments.get(componentsArgIndex);
      // old way
      if (arg instanceof ArrayCreation) {
        ArrayCreation arrayCreation = (ArrayCreation) arg;
        ArrayInitializer arrayInitializer = arrayCreation.getInitializer();
        List<Expression> expressions = DomGenerics.expressions(arrayInitializer);
        for (Expression expression : expressions) {
          addLinkedSize(axis, linkID, expression);
        }
      } else {
        // list of components as args should be here
        for (int i = 1; i < arguments.size(); ++i) {
          addLinkedSize(axis, linkID, arguments.get(i));
        }
      }
      linkID++;
    }
  }

  private void addLinkedSize(int linkID, Expression expression) {
    JavaInfo component = getJavaInfo(expression);
    String id = ObjectInfoUtils.getId(component);
    getLayout().getLayoutModel().addComponentToLinkSizedGroup(linkID, id, HORIZONTAL);
    getLayout().getLayoutModel().addComponentToLinkSizedGroup(linkID, id, VERTICAL);
  }

  private void addLinkedSize(int axis, int linkID, Expression expression) {
    JavaInfo component = getJavaInfo(expression);
    String id = ObjectInfoUtils.getId(component);
    getLayout().getLayoutModel().addComponentToLinkSizedGroup(linkID, id, axis);
  }

  protected int convertDimension(int dimension) {
    return dimension;
  }

  protected abstract void checkComponent(AbstractComponentInfo component, int dimension);

  protected abstract void setGroupAlignment(LayoutInterval group, Expression arg);

  protected abstract void setAlignment(LayoutInterval interval, Expression arg);

  protected abstract void setPaddingType(LayoutInterval gap, Expression arg);

  protected abstract boolean isComponent(Expression arg);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  public void saveLayout(List<AbstractComponentInfo> components) throws Exception {
    // remove all previous layout code
    JavaInfo layoutJavaInfo = getJavaInfo();
    layoutJavaInfo.removeMethodInvocations(SIGNATURE_SET_HORIZONTAL_GROUP);
    layoutJavaInfo.removeMethodInvocations(SIGNATURE_SET_VERTICAL_GROUP);
    layoutJavaInfo.removeMethodInvocations(SIGNATURE_LINK_SIZE);
    layoutJavaInfo.removeMethodInvocations(SIGNATURE_LINK_SIZE_AXIS);
    // add generated code
    // prepare
    String layoutCode = prepareLayoutCode(components);
    // fill layout
    String horizontalGroupCode = extractGroupCode(layoutCode, ID_SET_HORIZONTAL_GROUP);
    String verticalGroupCode = extractGroupCode(layoutCode, ID_SET_VERTICAL_GROUP);
    {
      layoutJavaInfo.addMethodInvocation(SIGNATURE_SET_HORIZONTAL_GROUP, horizontalGroupCode);
      layoutJavaInfo.addMethodInvocation(SIGNATURE_SET_VERTICAL_GROUP, verticalGroupCode);
    }
    // linked size components
    List<String> linkSizesCode = extractLinkSizeCode(layoutCode);
    for (String linkSizeCode : linkSizesCode) {
      layoutJavaInfo.addMethodInvocation(SIGNATURE_LINK_SIZE_AXIS, linkSizeCode);
    }
    // TODO: add 'pack()' method invocation if needed
  }

  private String extractGroupCode(String layoutCode, String invocationString) {
    int beginIndex = layoutCode.indexOf(invocationString);
    if (beginIndex == -1) {
      throw new DesignerException(1200);
    }
    beginIndex += invocationString.length();
    int endIndex = layoutCode.indexOf(";", beginIndex);
    if (endIndex == -1) {
      throw new DesignerException(1200);
    }
    String result = layoutCode.substring(beginIndex + 1, endIndex - 1);
    return result;
  }

  private List<String> extractLinkSizeCode(String layoutCode) {
    List<String> result = Lists.newArrayList();
    int beginIndex = 0;
    while (true) {
      beginIndex = layoutCode.indexOf(ID_LINK_SIZE, beginIndex);
      if (beginIndex == -1) {
        // nothing (more) found
        break;
      }
      beginIndex += ID_LINK_SIZE.length();
      int endIndex = layoutCode.indexOf(";", beginIndex);
      if (endIndex == -1) {
        // probably should throw an exception?
        break;
      }
      String code = layoutCode.substring(beginIndex + 1, endIndex - 1);
      result.add(code);
      beginIndex = endIndex;
    }
    return result;
  }

  protected final String getLayoutReference() throws Exception {
    StatementTarget target = getJavaInfo().getVariableSupport().getStatementTarget();
    return getJavaInfo().getVariableSupport().getReferenceExpression(new NodeTarget(target));
  }

  protected abstract String prepareLayoutCode(List<AbstractComponentInfo> components)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final IGroupLayoutInfo getLayout() {
    return m_layoutAdaptable.getAdapter(IGroupLayoutInfo.class);
  }

  private JavaInfo getJavaInfo() {
    return m_layoutAdaptable.getAdapter(JavaInfo.class);
  }

  private JavaInfo getJavaInfo(Expression expression) {
    return JavaInfoResolver.getJavaInfo(getJavaInfo(), expression);
  }

  protected final LayoutComponent getRootComponent() {
    return getLayout().getLayoutModel().getLayoutComponent(
        ObjectInfoUtils.getId(getLayout().getLayoutContainer()));
  }

  public LayoutModel getLayoutModel() {
    return getLayout().getLayoutModel();
  }
}