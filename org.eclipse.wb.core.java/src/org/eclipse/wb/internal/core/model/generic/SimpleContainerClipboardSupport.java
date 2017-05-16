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
package org.eclipse.wb.internal.core.model.generic;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;

import java.util.List;

/**
 * Support for copying {@link SimpleContainer} child.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public final class SimpleContainerClipboardSupport implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new SimpleContainerClipboardSupport();

  private SimpleContainerClipboardSupport() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
    root.addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        List<SimpleContainer> containers = getSimpleContainers(javaInfo);
        for (int i = 0; i < containers.size(); i++) {
          SimpleContainer container = containers.get(i);
          Object child_ = container.getChild();
          if (child_ instanceof JavaInfo) {
            ClipboardCommand command = createCommand(i, (JavaInfo) child_);
            commands.add(command);
          }
        }
      }
    });
  }

  private static List<SimpleContainer> getSimpleContainers(JavaInfo javaInfo) {
    return new SimpleContainerFactory(javaInfo, true).get();
  }

  /**
   * @return the {@link ClipboardCommand} for creating given child on simple container, during
   *         paste.
   */
  private static ClipboardCommand createCommand(final int containerIndex, JavaInfo child)
      throws Exception {
    final JavaInfoMemento memento = JavaInfoMemento.createMemento(child);
    ClipboardCommand command = new ClipboardCommand() {
      private static final long serialVersionUID = 0L;

      @Override
      public void execute(JavaInfo newContainer) throws Exception {
        JavaInfo newChild = memento.create(newContainer);
        getSimpleContainers(newContainer).get(containerIndex).command_CREATE(newChild);
        memento.apply();
      }
    };
    return command;
  }
}
