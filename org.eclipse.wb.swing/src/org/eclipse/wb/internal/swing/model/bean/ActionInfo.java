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
package org.eclipse.wb.internal.swing.model.bean;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.palette.ActionUseEntryInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.graphics.Image;

import java.io.IOException;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;

/**
 * Model for {@link Action}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public class ActionInfo extends JavaInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds broadcast listeners.
   */
  private void addListeners() {
    // add to palette
    addBroadcastListener(new PaletteEventListener() {
      @Override
      public void entries(CategoryInfo category, List<EntryInfo> entries) throws Exception {
        if (category.getId().equals("org.eclipse.wb.internal.swing.actions")) {
          entries.add(new ActionUseEntryInfo(ActionInfo.this));
        }
      }
    });
    // update SMALL_ICON image
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        refreshIconImage();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The SWT {@link Image} created from {@link Action#SMALL_ICON}, may be <code>null</code>.
   */
  private Image m_smallIconImage;
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public Image getIcon() throws Exception {
      if (m_smallIconImage != null) {
        return m_smallIconImage;
      }
      return super.getIcon();
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  /**
   * Converts {@link Action#SMALL_ICON} into {@link #m_smallIconImage}.
   */
  private void refreshIconImage() throws IOException {
    m_smallIconImage = null;
    // if Action has icon, convert it into SWT image
    if (getObject() instanceof Action) {
      javax.swing.Icon smallIcon = (Icon) ((Action) getObject()).getValue(Action.SMALL_ICON);
      if (smallIcon != null) {
        m_smallIconImage = ImageUtils.convertToSWT(smallIcon);
        // schedule SWT image for disposing
        JavaInfoUtils.getState(this).addDisposableImage(m_smallIconImage);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets {@link ActionInfo} for given {@link ComponentInfo} using
   * {@link AbstractButton#setAction(javax.swing.Action)}.
   * 
   * @param component
   *          the {@link ComponentInfo} to set action for.
   * @param action
   *          the {@link ActionInfo} to set, may be <code>null</code>, then any existing action will
   *          be unset.
   */
  public static void setAction(ComponentInfo component, ActionInfo action) throws Exception {
    if (action == null) {
      component.removeMethodInvocations("setAction(javax.swing.Action)");
    } else {
      // ensure that ActionInfo is already added
      if (action.getParent() == null) {
        ActionContainerInfo.add(component.getRootJava(), action);
      }
      // do set using setAction()
      String actionSource = TemplateUtils.getExpression(action);
      MethodInvocation invocation = component.getMethodInvocation("setAction(javax.swing.Action)");
      if (invocation != null) {
        component.replaceExpression(DomGenerics.arguments(invocation).get(0), actionSource);
      } else {
        invocation = component.addMethodInvocation("setAction(javax.swing.Action)", actionSource);
      }
      action.addRelatedNodes(invocation);
      // select component
      component.getBroadcastObject().select(ImmutableList.of(component));
    }
  }

  /**
   * @return the new {@link ActionInfo} instance for adding as inner class.
   */
  public static ActionInfo createInner(AstEditor editor) throws Exception {
    return (ActionInfo) JavaInfoUtils.createJavaInfo(
        editor,
        Action.class,
        new ActionInnerCreationSupport());
  }
}
