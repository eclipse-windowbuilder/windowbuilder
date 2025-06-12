/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.gef;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author lobas_av
 *
 */
public class CommandsTest extends Assertions {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CompoundCommand() throws Exception {
		CompoundCommand command = new CompoundCommand();
		//
		// check new CompoundCommand
		assertNotNull(command.getCommands());
		assertTrue(command.getCommands().isEmpty());
		assertTrue(command.isEmpty());
		assertEquals(0, command.size());
		assertEquals(command.unwrap(), UnexecutableCommand.INSTANCE);
		//
		// check add 'null' command
		command.add(null);
		assertEquals(0, command.size());
		assertTrue(command.isEmpty());
		//
		// check add command
		Command dummy = new Command() {
		};
		command.add(dummy);
		assertEquals(1, command.size());
		assertFalse(command.isEmpty());
		assertSame(dummy, command.unwrap());
		//
		// again check add command
		command.add(new Command(){});
		assertEquals(2, command.size());
		assertFalse(command.isEmpty());
		assertSame(command, command.unwrap());
		//
		// check execute commands
		command = new CompoundCommand();
		final int[] counter = new int[1];
		command.add(new Command() {
			@Override
			public void execute() {
				counter[0]++;
			}
		});
		command.add(new Command() {
			@Override
			public void execute() {
				counter[0]++;
			}
		});
		command.execute();
		assertEquals(2, counter[0]);
	}
}