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

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.action.GroupMarker;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

/**
 * Model for {@link GroupMarker}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class GroupMarkerInfo extends ContributionItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupMarkerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate0() throws Exception {
    super.refresh_afterCreate0();
    {
      String id = (String) ReflectionUtils.invokeMethod(getObject(), "getId()");
      Object item = createContributionItem_withText(id);
      // insert item
      ContributionManagerInfo manager = (ContributionManagerInfo) getParent();
      manager.low_insertContributionItem(manager.low_getIndex(getObject()), item);
      manager.low_update();
      // use this item instead
      setObject(item);
    }
  }

  private Object createContributionItem_withText(String text) throws Exception {
    ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> classAction = classLoader.loadClass("org.eclipse.jface.action.Action");
    Class<?> classItem = classLoader.loadClass("org.eclipse.jface.action.ActionContributionItem");
    // create Action
    Object action;
    {
      Enhancer enhancer = new Enhancer();
      enhancer.setClassLoader(classLoader);
      enhancer.setSuperclass(classAction);
      enhancer.setCallback(NoOp.INSTANCE);
      action = enhancer.create(new Class<?>[]{String.class}, new Object[]{text});
    }
    // wrap Action with item
    return ReflectionUtils.getConstructorBySignature(
        classItem,
        "<init>(org.eclipse.jface.action.IAction)").newInstance(action);
  }
}
