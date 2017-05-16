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
package org.eclipse.wb.internal.core.nls.commands;

/**
 * Command queue for NLS editing commands.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface ICommandQueue {
  /**
   * Adds given {@link AbstractCommand} to the command queue.
   */
  void addCommand(AbstractCommand command);
}
