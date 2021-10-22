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
package org.eclipse.wb.internal.rcp.model.jface.action;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.util.List;

/**
 * Model for any {@link IContributionManager}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public abstract class ContributionManagerInfo extends AbstractComponentInfo {
  private Object m_componentObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContributionManagerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IContributionItem} model children.
   */
  public final List<AbstractComponentInfo> getItems() {
    List<AbstractComponentInfo> contributionItems = Lists.newArrayList();
    for (ObjectInfo child : getChildren()) {
      if (child instanceof ContributionItemInfo || child instanceof MenuManagerInfo) {
        contributionItems.add((AbstractComponentInfo) child);
      }
    }
    return contributionItems;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Component
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link Object} for this {@link ContributionManagerInfo}.<br>
   * This method is invoked from top-level RCP container, such as ApplicationWindow, ViewPart, etc.
   */
  public void setComponentObject(Object componentObject) {
    m_componentObject = componentObject;
  }

  @Override
  public Object getComponentObject() {
    return m_componentObject != null ? m_componentObject : getObject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate0() throws Exception {
    super.refresh_afterCreate0();
    refresh_forceSeparators();
  }

  /**
   * {@link IContributionManager} perform optimization during filling, by not creating
   * {@link Separator}'s if there are no other/real {@link ContributionItem}'s before/after. But
   * during design time we want to see all {@link Separator}'s, so this method adds artificial
   * {@link ContributionItem}'s to force {@link Separator}'s creation.
   */
  private void refresh_forceSeparators() throws ClassNotFoundException, Exception {
    // in any case insert ContributionItem as first item, just to make this ContributionManager "visible"
    insertArtificialContributionItem(0);
    // insert ContributionItem after each Separator
    Object manager = getObject();
    Object[] items = (Object[]) ReflectionUtils.invokeMethod2(manager, "getItems");
    for (Object item : items) {
      if (item.getClass().getName().equals("org.eclipse.jface.action.Separator")) {
        int index = low_getIndex(item);
        insertArtificialContributionItem(index + 1);
      }
    }
    // do update
    low_update();
  }

  /**
   * Inserts new {@link ContributionItem} that does not perform any <code>fill</code>.
   */
  private void insertArtificialContributionItem(int index) throws Exception {
    Object item;
    {
      ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
      String itemClassName = getArtificialContributionItem_className();
      Class<?> itemClass = editorLoader.loadClass(itemClassName);
      Enhancer enhancer = new Enhancer();
      enhancer.setClassLoader(editorLoader);
      enhancer.setSuperclass(itemClass);
      enhancer.setCallback(NoOp.INSTANCE);
      item = enhancer.create();
    }
    // do insert
    low_insertContributionItem(index, item);
  }

  /**
   * Usually just {@link ContributionItem}, but because of
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=260685 we need tweak for {@link CoolBarManager}.
   * <p>
   * TODO remove this after fixing Eclipse bug.
   */
  private String getArtificialContributionItem_className() {
    if (this instanceof CoolBarManagerInfo) {
      return "org.eclipse.jface.action.ToolBarContributionItem";
    }
    return "org.eclipse.jface.action.ContributionItem";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Low level ContributionItem
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of given {@link ContributionItem} in this {@link ContributionManager}.
   */
  int low_getIndex(Object item) throws Exception {
    return (Integer) ReflectionUtils.invokeMethod(
        getObject(),
        "indexOf(org.eclipse.jface.action.IContributionItem)",
        item);
  }

  /**
   * Calls {@link ContributionManager#insert(int, IContributionItem)}.
   */
  void low_insertContributionItem(int index, Object item) throws Exception {
    ReflectionUtils.invokeMethod(
        getObject(),
        "insert(int,org.eclipse.jface.action.IContributionItem)",
        index,
        item);
  }

  /**
   * Calls {@link ContributionManager#update(boolean)}.
   */
  void low_update() throws Exception {
    ReflectionUtils.invokeMethod(getObject(), "update(boolean)", false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ContributionItemInfo} using given {@link ActionInfo}.
   *
   * @return the created {@link ActionContributionItemInfo}.
   */
  public ActionContributionItemInfo command_CREATE(ActionInfo action, AbstractComponentInfo nextItem)
      throws Exception {
    ActionContainerInfo.ensureInstance(getRootJava(), action);
    // prepare "item" for "action"
    ActionContributionItemInfo item;
    {
      AstEditor editor = getEditor();
      CreationSupport creationSupport = new ContributionManagerActionCreationSupport(this, action);
      item =
          (ActionContributionItemInfo) JavaInfoUtils.createJavaInfo(
              editor,
              "org.eclipse.jface.action.ActionContributionItem",
              creationSupport);
    }
    // add "item" for "action"
    JavaInfoUtils.add(
        item,
        new VoidInvocationVariableSupport(item),
        PureFlatStatementGenerator.INSTANCE,
        AssociationObjects.invocationVoid(),
        this,
        nextItem);
    return item;
  }

  /**
   * Creates new {@link ContributionItemInfo}.
   */
  public void command_CREATE(AbstractComponentInfo item, AbstractComponentInfo nextItem)
      throws Exception {
    if (item instanceof ContributionManagerInfo) {
      JavaInfoUtils.add(item, getAssociation_(), this, nextItem);
    } else {
      JavaInfoUtils.add(
          item,
          new LocalUniqueVariableSupport(item),
          PureFlatStatementGenerator.INSTANCE,
          getAssociation_(),
          this,
          nextItem);
      if (item.getVariableSupport() instanceof LocalUniqueVariableSupport) {
        ((LocalUniqueVariableSupport) item.getVariableSupport()).inline();
      }
    }
  }

  /**
   * Moves {@link ContributionItemInfo}.
   */
  public void command_MOVE(AbstractComponentInfo item, AbstractComponentInfo nextItem)
      throws Exception {
    ObjectInfo oldParent = item.getParent();
    if (item.getCreationSupport() instanceof ContributionManagerActionCreationSupport
        && oldParent != this) {
      getBroadcastJava().moveBefore0(item, oldParent, this);
      getBroadcastJava().moveBefore(item, oldParent, this);
      // update source/AST
      {
        AstEditor editor = getEditor();
        // prepare current state
        ContributionManagerActionCreationSupport oldCreationSupport =
            (ContributionManagerActionCreationSupport) item.getCreationSupport();
        ActionInfo action = oldCreationSupport.getAction();
        // remove old add() invocation
        editor.removeEnclosingStatement(oldCreationSupport.getNode());
        // add new add() invocation
        MethodInvocation addInvocation;
        {
          StatementTarget target = JavaInfoUtils.getTarget(this, item, nextItem);
          String addSource = TemplateUtils.format("{0}.add({1})", this, action);
          addInvocation = (MethodInvocation) addExpressionStatement(target, addSource);
        }
        // use new CreationSupport and Association
        MethodDescription description =
            getDescription().getMethod("add(org.eclipse.jface.action.IAction)");
        item.setCreationSupport(new ContributionManagerActionCreationSupport(this,
            description,
            addInvocation,
            new JavaInfo[]{action}));
        item.setAssociation(new InvocationVoidAssociation(addInvocation));
      }
      // move model
      oldParent.removeChild(item);
      addChild(item, nextItem);
      getBroadcastJava().moveAfter(item, oldParent, this);
    } else {
      JavaInfoUtils.move(item, getAssociation_(), this, nextItem);
      if (item.getVariableSupport() instanceof LocalUniqueVariableSupport) {
        LocalUniqueVariableSupport variableSupport =
            (LocalUniqueVariableSupport) item.getVariableSupport();
        if (variableSupport.canInline()) {
          variableSupport.inline();
        }
      }
    }
  }

  /**
   * @return the standard {@link AssociationObject} for {@link IContributionManager}.
   */
  private static AssociationObject getAssociation_() throws Exception {
    return AssociationObjects.invocationChild("%parent%.add(%child%)", false);
  }
}
