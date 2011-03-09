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
package org.eclipse.wb.internal.ercp.model.widgets.mobile;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.ercp.support.ListBoxSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Model for eSWT {@link org.eclipse.ercp.swt.mobile.ListBox}.
 * 
 * @author lobas_av
 * @author scheglov_ke
 * @coverage ercp.model.widgets.mobile
 */
public final class ListBoxInfo extends CompositeInfo {
  public static final String SET_DATA_MODEL_ITEM = "org.eclipse.ercp.swt.mobile.ListBoxItem";
  public static final String SET_DATA_MODEL_SIGNATURE = "setDataModel("
      + SET_DATA_MODEL_ITEM
      + "[])";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListBoxInfo(final AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // add items with create new listBox
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == ListBoxInfo.this) {
          // remove listener
          removeBroadcastListener(this);
          // prepare component class
          Class<?> itemType = get_setDataModel_ItemType();
          // remove existing "setDataModel()"
          removeMethodInvocations(SET_DATA_MODEL_SIGNATURE);
          // add new items
          for (int i = 0; i < 3; i++) {
            ListBoxItemInfo item =
                (ListBoxItemInfo) JavaInfoUtils.createJavaInfo(
                    editor,
                    itemType,
                    new ConstructorCreationSupport());
            add(item, null);
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    // continue in super()
    super.refresh_fetch();
    // prepare ListBox and items
    Object listBox = getObject();
    List<ListBoxItemInfo> items = getItems();
    int count = items.size();
    // fetch all items bounds
    for (int i = 0; i < count; i++) {
      ListBoxItemInfo item = items.get(i);
      item.setModelBounds(ListBoxSupport.getItemBounds(listBox, i));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ListBoxItemInfo} children.
   */
  public final List<ListBoxItemInfo> getItems() {
    return getChildren(ListBoxItemInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link ListBoxItemInfo} to this list.
   */
  public void add(ListBoxItemInfo item, ListBoxItemInfo nextItem) throws Exception {
    ArrayObjectInfo objectInfo = init_setDataModel_ArrayObjectInfo();
    objectInfo.command_CREATE(item, nextItem);
  }

  /**
   * Moves {@link ListBoxItemInfo} in this list.
   */
  public void move(ListBoxItemInfo item, ListBoxItemInfo nextItem) throws Exception {
    AbstractArrayObjectInfo objectInfo = null;
    if (nextItem != null) {
      objectInfo = AbstractArrayObjectInfo.getArrayObjectInfo(nextItem);
    }
    if (objectInfo == null) {
      objectInfo = init_setDataModel_ArrayObjectInfo();
    }
    objectInfo.command_MOVE(item, nextItem);
  }

  /**
   * @return {@link ArrayObjectInfo} for method <code>setDataModel()</code>.
   */
  private ArrayObjectInfo init_setDataModel_ArrayObjectInfo() throws Exception {
    ArrayObjectInfo arrayInfo = get_setDataModel_ArrayObjectInfo();
    if (arrayInfo == null) {
      MethodInvocation invocation = get_setDataModel_Invocation();
      if (invocation == null) {
        invocation =
            addMethodInvocation(SET_DATA_MODEL_SIGNATURE, "new " + SET_DATA_MODEL_ITEM + "[]{}");
      }
      // prepare array creation
      ArrayCreation creation = (ArrayCreation) invocation.arguments().get(0);
      // add ArrayObjectInfo item
      arrayInfo =
          new ArrayObjectInfo(getEditor(),
              invocation.getName().getIdentifier(),
              get_setDataModel_ItemType(),
              creation);
      addChild(arrayInfo);
      arrayInfo.setRemoveOnEmpty(true);
      arrayInfo.setHideInTree(true);
    }
    return arrayInfo;
  }

  private ArrayObjectInfo get_setDataModel_ArrayObjectInfo() {
    MethodInvocation invocation = get_setDataModel_Invocation();
    if (invocation != null) {
      List<ArrayObjectInfo> arrays = getChildren(ArrayObjectInfo.class);
      for (ArrayObjectInfo array : arrays) {
        if (array.getCreation().equals(invocation.arguments().get(0))) {
          return array;
        }
      }
    }
    return null;
  }

  /**
   * @return {@link MethodInvocation} for method <code>setDataModel()</code>.
   */
  private MethodInvocation get_setDataModel_Invocation() {
    return getMethodInvocation(SET_DATA_MODEL_SIGNATURE);
  }

  /**
   * @return {@link Class} for <code>item</code>.
   */
  private Class<?> get_setDataModel_ItemType() throws ClassNotFoundException {
    return EditorState.get(getEditor()).getEditorLoader().loadClass(SET_DATA_MODEL_ITEM);
  }
}