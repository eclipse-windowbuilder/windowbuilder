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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard;

import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.TextSwtObservableInfo;

import java.util.Iterator;
import java.util.List;

/**
 * Model for observable object <code>SWTObservables.observeText(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class SwtObservableTextCodeSupport extends SwtObservableCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getSourceCode(ObservableInfo observable) throws Exception {
		TextSwtObservableInfo textObservable = (TextSwtObservableInfo) observable;
		List<String> updateEvents = textObservable.getUpdateEvents();
		int size = updateEvents.size();
		//
		if (size == 0) {
			return super.getSourceCode(observable);
		}
		StringBuffer events = new StringBuffer(", ");
		if (size == 1) {
			events.append("org.eclipse.swt." + updateEvents.get(0));
		} else {
			events.append("new int[]{");
			for (Iterator<String> I = updateEvents.iterator(); I.hasNext();) {
				events.append("org.eclipse.swt." + I.next());
				if (I.hasNext()) {
					events.append(", ");
				}
			}
			events.append("}");
		}
		return "org.eclipse.jface.databinding.swt.SWTObservables."
		+ observable.getBindableProperty().getReference()
		+ "("
		+ observable.getBindableObject().getReference()
		+ events.toString()
		+ ")";
	}
}