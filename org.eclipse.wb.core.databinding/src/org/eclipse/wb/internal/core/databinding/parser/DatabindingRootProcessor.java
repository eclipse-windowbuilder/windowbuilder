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
package org.eclipse.wb.internal.core.databinding.parser;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingFactory;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.ICompilationUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link IRootProcessor} for bindings.
 *
 * @author lobas_av
 * @coverage bindings.parser
 */
public class DatabindingRootProcessor implements IRootProcessor {
	public static final IRootProcessor INSTANCE = new DatabindingRootProcessor();
	public static final Map<ICompilationUnit, ParseState> STATES = new HashMap<>();
	private List<IDatabindingFactory> m_factories;

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
		if (EditorState.get(root.getEditor()).isLiveComponent()) {
			return;
		}
		// prepare factories
		if (m_factories == null) {
			m_factories =
					ExternalFactoriesHelper.getElementsInstances(
							IDatabindingFactory.class,
							"org.eclipse.wb.core.databinding.databindingFactories",
							"factory");
		}
		// handle providers
		for (IDatabindingFactory factory : m_factories) {
			IDatabindingsProvider databindingsProvider = factory.createProvider(root);
			if (databindingsProvider != null) {
				// store current provider
				STATES.put(
						root.getEditor().getModelUnit(),
						new ParseState(databindingsProvider, factory.getPlugin()));
				// add remove listener
				root.addBroadcastListener(new ObjectEventListener() {
					@Override
					public void dispose() throws Exception {
						STATES.remove(root.getEditor().getModelUnit());
					}
				});
				return;
			}
		}
	}
}
