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
package org.eclipse.wb.gef.core;

/**
 * Pattern Command.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class Command {
  /**
   * {@link Command} that does nothing.
   */
  public static final Command EMPTY = new Command() {
    @Override
    public void execute() {
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Executes this {@link Command}.
   */
  public abstract void execute() throws Exception;
}