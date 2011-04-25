package mongrel2;

import java.util.Enumeration;
import java.util.Iterator;

@SuppressWarnings("rawtypes")
class IteratorEnumeration implements Enumeration {

	private final Iterator i;

	public IteratorEnumeration(final Iterator i) {
		this.i = i;
	}

	@Override
	public boolean hasMoreElements() {
		return this.i.hasNext();
	}

	@Override
	public Object nextElement() {
		return this.i.next();
	}

}
