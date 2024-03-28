package org.eclipse.wb.internal.core.model.property.event;

import java.util.Optional;

public interface ListenerDeclaration {

	void removeListener();

	void openStubMethod();

	Optional<Integer> getStartPosition();

	boolean isModified() throws Exception;

	void setValue(Object object) throws Exception;
}
