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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model for {@link ActionContributionItem}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ActionContributionItemInfo extends ContributionItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionContributionItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    deleteWhenActionDeleting();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When underlying {@link ActionInfo} is deleting, we should delete items which use it.
   */
  private void deleteWhenActionDeleting() {
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (getAction() == child) {
          delete();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public Image getIcon() throws Exception {
      return getAction().getPresentation().getIcon();
    }

    @Override
    public String getText() throws Exception {
      if (isAddAction()) {
        return getAction().getPresentation().getText();
      }
      return super.getText();
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    if (isAddAction()) {
      Property[] actionProperties = getAction().getProperties();
      List<Property> list = new ArrayList<>();
      Collections.addAll(list, actionProperties);
      return list;
    }
    return super.getPropertyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link ActionContributionItemInfo} created using
   *         {@link IContributionManager#add(IAction)}.
   */
  private boolean isAddAction() {
    return getCreationSupport() instanceof ContributionManagerActionCreationSupport;
  }

  /**
   * @return the underlying {@link ActionInfo}.
   */
  public ActionInfo getAction() {
    if (isAddAction()) {
      ContributionManagerActionCreationSupport creationSupport =
          (ContributionManagerActionCreationSupport) getCreationSupport();
      return creationSupport.getAction();
    } else {
      ClassInstanceCreation creation = (ClassInstanceCreation) getCreationSupport().getNode();
      Expression actionExpression = DomGenerics.arguments(creation).get(0);
      return (ActionInfo) getRootJava().getChildRepresentedBy(actionExpression);
    }
  }
}
