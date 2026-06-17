package org.eclipse.wb.internal.os.linux;

import java.lang.foreign.MemorySegment;

/**
 * The base type system and object class
 */
public class GObject {
	private final MemorySegment segment;

	protected GObject(long handle) {
		this(handle == 0L ? MemorySegment.NULL : MemorySegment.ofAddress(handle));
	}

	protected GObject(MemorySegment segment) {
		this.segment = segment;
	}

	public MemorySegment segment() {
		return segment;
	}
}
