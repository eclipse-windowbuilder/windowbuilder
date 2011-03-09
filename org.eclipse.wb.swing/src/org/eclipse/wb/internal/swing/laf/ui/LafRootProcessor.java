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
package org.eclipse.wb.internal.swing.laf.ui;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.util.List;

/**
 * {@link IRootProcessor} that contributes LAF selection action into context menu.
 * 
 * @author mitin_aa
 * @coverage swing.laf.ui
 */
public final class LafRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    if (root instanceof ComponentInfo) {
      final ComponentInfo rootComponent = (ComponentInfo) root;
      // TODO
      /*rootComponent.addBroadcastListener(new BroadcastGroup() {
        LafSelectionItem lafItem;
        {
          add(new ObjectEventListener() {
            @Override
            public void addHierarchyActions(List<Object> actions) throws Exception {
              if (lafItem == null) {
                lafItem = new LafSelectionItem(rootComponent);
              }
              actions.add(lafItem);
            }
          }, new ObjectEventListener() {
            @Override
            public void refreshBeforeCreate() throws Exception {
              LafInfo lafInfo = LafSupport.getSelectedLAF(rootComponent);
              LafSupport.applySelectedLAF(lafInfo);
            }
          }, new EditorActivatedListener() {
            public void invoke(EditorActivatedRequest request) throws Exception {
              if (lafItem != null) {
                lafItem.update();
              }
            }
          });
        }
      });*/
      new Object() {
        LafSelectionItem lafItem;
        {
          rootComponent.addBroadcastListener(new ObjectEventListener() {
            @Override
            public void addHierarchyActions(List<Object> actions) throws Exception {
              if (lafItem == null) {
                lafItem = new LafSelectionItem(rootComponent);
              }
              actions.add(lafItem);
            }

            @Override
            public void refreshBeforeCreate() throws Exception {
              LafInfo lafInfo = LafSupport.getSelectedLAF(rootComponent);
              LafSupport.applySelectedLAF(lafInfo);
            }
          });
          rootComponent.addBroadcastListener(new EditorActivatedListener() {
            public void invoke(EditorActivatedRequest request) throws Exception {
              if (lafItem != null) {
                lafItem.update();
              }
            }
          });
        }
      };
    }
  }
}
