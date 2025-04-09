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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetVariable;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.nonvisual.CollectorObjectInfo;
import org.eclipse.wb.internal.core.model.variable.FieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for auto synchronize {@link JavaInfo}'s and observe, binding infos.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class SynchronizeManager {
	private final IDatabindingsProvider m_provider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SynchronizeManager(IDatabindingsProvider provider, JavaInfo javaInfoRoot) {
		m_provider = provider;
		javaInfoRoot.addBroadcastListener(new ObjectInfoDelete() {
			@Override
			public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
				synchronizeObserves();
			}
		});
		javaInfoRoot.addBroadcastListener(new JavaEventListener() {
			@Override
			public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
				if (EditorState.get(child.getEditor()).isLiveComponent()) {
					return;
				}
				handleCreate(child);
			}

			@Override
			public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				synchronizeObserves();
			}

			@Override
			public void replaceChildBefore(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
					throws Exception {
				m_provider.deleteBindings(oldChild);
			}

			@Override
			public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
					throws Exception {
				handleCreate(newChild);
			}

			private void handleCreate(final JavaInfo javaInfo) {
				// all JavaInfo children (include exposed) done after sets Object to JavaInfo
				javaInfo.addBroadcastListener(new JavaInfoSetObjectAfter() {
					@Override
					public void invoke(JavaInfo target, Object o) throws Exception {
						if (javaInfo == target) {
							target.removeBroadcastListener(this);
							synchronizeObserves();
						}
					}
				});
			}
		});
		javaInfoRoot.addBroadcastListener(new JavaInfoSetVariable() {
			@Override
			public void invoke(JavaInfo javaInfo, VariableSupport oldVariable, VariableSupport newVariable)
					throws Exception {
				if (oldVariable != null) {
					if (isField(oldVariable) ^ isField(newVariable)) {
						synchronizeObserves();
					}
				}
			}

			private boolean isField(VariableSupport variable) {
				return variable instanceof FieldVariableSupport;
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle
	//
	////////////////////////////////////////////////////////////////////////////
	private void synchronizeObserves() throws Exception {
		m_provider.synchronizeObserves();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * XXX
	 */
	public static <T, V> void synchronizeObjects(List<V> objects,
			List<T> newKeyObjects,
			ISynchronizeProcessor<T, V> processor) throws Exception {
		// prepare map[key, object]
		int beanCount = 0;
		Map<T, V> keyObjectToObject = new HashMap<>();
		for (V object : objects) {
			if (processor.handleObject(object)) {
				keyObjectToObject.put(processor.getKeyObject(object), object);
				beanCount++;
			}
		}
		//
		int keyCount = newKeyObjects.size();
		int index = 0;
		for (; index < keyCount; index++) {
			T keyObject = newKeyObjects.get(index);
			// quick check over index
			if (index < beanCount) {
				V object = objects.get(index);
				if (processor.equals(processor.getKeyObject(object), keyObject)) {
					processor.update(object);
					continue;
				}
			}
			//
			V object = keyObjectToObject.get(keyObject);
			if (object == null) {
				object = processor.findObject(keyObjectToObject, keyObject);
			}
			if (object == null) {
				object = processor.createObject(keyObject);
				if (object == null) {
					newKeyObjects.remove(index--);
					keyCount--;
				} else {
					objects.add(index, object);
				}
			} else {
				// reorder child
				objects.remove(object);
				objects.add(index, object);
				processor.update(object);
			}
		}
		// remove old child
		for (int i = index; i < beanCount; i++) {
			objects.remove(index);
		}
	}

	/**
	 * @return the {@link List<T extends ObjectInfo>} children for processing.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ObjectInfo> List<T> getChildren(ObjectInfo objectInfo, Class<T> clazz)
			throws Exception {
		List<T> childrenInfos = new ArrayList<>();
		for (ObjectInfo childObjectInfo : objectInfo.getChildren()) {
			boolean[] visible = new boolean[]{childObjectInfo.getPresentation().isVisible()};
			objectInfo.getBroadcast(ObjectInfoChildTree.class).invoke(childObjectInfo, visible);
			if (visible[0]) {
				if (childObjectInfo instanceof CollectorObjectInfo collectorObjectInfo) {
					for (ObjectInfo itemObjectInfo : collectorObjectInfo.getItems()) {
						if (clazz.isAssignableFrom(itemObjectInfo.getClass())) {
							childrenInfos.add((T) itemObjectInfo);
						}
					}
				} else if (clazz.isAssignableFrom(childObjectInfo.getClass())) {
					childrenInfos.add((T) childObjectInfo);
				}
			}
		}
		return childrenInfos;
	}
}