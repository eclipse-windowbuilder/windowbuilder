/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Override
	public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
		if (root instanceof final ComponentInfo rootComponent) {
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
						@Override
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
