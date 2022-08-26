package info.kgeorgiy.ja.zaripov.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {
    private final List<T> elements;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    @SuppressWarnings("unused")
    public ArraySet(final Collection<? extends T> collection) {
        this(collection, null);
    }

    public ArraySet(final Collection<? extends T> collection, final Comparator<? super T> comparator) {
        SortedSet<T> set = new TreeSet<>(comparator);
        set.addAll(collection);
        this.elements = new ArrayList<>(set);
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @SuppressWarnings("unchecked")
    private int compare(final T first, final T second) {
        return Objects.isNull(comparator)
                ? ((Comparable<T>) first).compareTo(second)
                : comparator.compare(first, second);
    }

    private int upperIndexOf(final T element) {
        int index = Collections.binarySearch(elements, Objects.requireNonNull(element), comparator);
        return index < 0 ? -index - 1 : index;
    }

    public SortedSet<T> subSet(final T fromElement, boolean fromInclusive,
                               final T toElement, boolean toInclusive) {
        if (compare(fromElement, toElement) > 0) {
            return new ArraySet<>(Collections.emptyList(), comparator);
        }

        int fromIndex = upperIndexOf(fromElement);
        int toIndex = upperIndexOf(toElement);

        fromIndex = fromInclusive ? fromIndex : fromIndex + 1;
        toIndex = toInclusive ? toIndex + 1 : toIndex;

        return new ArraySet<>(elements.subList(fromIndex, toIndex), comparator);
    }

    @Override
    public SortedSet<T> subSet(final T fromElement, final T toElement) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("fromKey > toKey");
        }

        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(final T toElement) {
        return isEmpty() ? this : subSet(first(), true, toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(final T fromElement) {
        return isEmpty() ? this : subSet(fromElement, true, last(), true);
    }

    private T getElement(int index) {
        if (isEmpty() || !isAccessible(index)) {
            throw new NoSuchElementException("Index out of bounds: " + index);
        }
        return elements.get(index);
    }

    private boolean isAccessible(int index) {
        return index >= 0 && index < size();
    }

    @Override
    public T first() {
        return getElement(0);
    }

    @Override
    public T last() {
        return getElement(size() - 1);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, Objects.requireNonNull((T) o), comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }
}