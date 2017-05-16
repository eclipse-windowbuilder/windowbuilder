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
package org.eclipse.wb.core.editor.actions.assistant;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.utils.ui.TabFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import java.util.List;

/**
 * Abstract provider for layout assistant pages for this/parent layout and constraints.
 *
 * @author lobas_av
 * @coverage core.editor.action.assistant
 */
public abstract class LayoutAssistantSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutAssistantSupport(ObjectInfo layout) {
    layout.addBroadcastListener(new LayoutAssistantListener() {
      @Override
      public void createAssistantPages(List<ObjectInfo> objects,
          TabFolder folder,
          List<ILayoutAssistantPage> pages) throws Exception {
        LayoutAssistantSupport.this.createAssistantPages(objects, folder, pages);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutAssistantListener
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createAssistantPages(List<ObjectInfo> objects,
      TabFolder folder,
      List<ILayoutAssistantPage> pages) throws Exception {
    // prepare first selection object
    ObjectInfo firstObject = objects.get(0);
    // prepare container
    ObjectInfo container = getContainer();
    // check selection is this container
    if (objects.size() == 1 && firstObject == container) {
      addPage(folder, pages, Messages.LayoutAssistantSupport_layoutPage, createLayoutPage(folder));
      return;
    }
    // check selection is children of this container
    if (firstObject.getParent() == container) {
      addPage(
          folder,
          pages,
          Messages.LayoutAssistantSupport_parentLayoutPage,
          createLayoutPage(folder));
      addPage(folder, pages, getConstraintsPageTitle(), createConstraintsPage(folder, objects));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return container {@link ObjectInfo} for handled layout.
   */
  protected abstract ObjectInfo getContainer();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void addPage(TabFolder folder,
      List<ILayoutAssistantPage> pages,
      String name,
      AbstractAssistantPage page) {
    if (page != null) {
      TabFactory.item(folder).text(name).control(page);
      pages.add(page);
    }
  }

  /**
   * Create page for editing handled layout.
   */
  protected AbstractAssistantPage createLayoutPage(Composite parent) {
    return null;
  }

  /**
   * Create page for editing layout constraints.
   */
  protected AbstractAssistantPage createConstraintsPage(Composite parent, List<ObjectInfo> objects) {
    return null;
  }

  /**
   * @return the toolkit specific title for constraints page.
   */
  protected abstract String getConstraintsPageTitle();
}