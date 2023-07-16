package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class AnnotationSet extends AbstractSet<AnnotationItem>
        implements PublicCloneable {

    public static final int ALIGNMENT = 4;

    private final Set<AnnotationItem> annotations;

    public AnnotationSet(AnnotationItem... annotations) {
        if (annotations == null) {
            annotations = new AnnotationItem[0];
        }
        this.annotations = new HashSet<>(annotations.length);
        addAll(Arrays.asList(annotations));
    }

    public static AnnotationSet read(RandomInput in, ReadContext context) {
        int size = in.readInt();
        AnnotationItem[] out = new AnnotationItem[size];
        for (int i = 0; i < size; i++) {
            out[i] = AnnotationItem.read(in.duplicate(in.readInt()), context);
        }
        return new AnnotationSet(out);
    }

    public static AnnotationSet empty() {
        return new AnnotationSet();
    }

    public void collectData(DataCollector data) {
        for (AnnotationItem tmp : annotations) {
            data.add(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        AnnotationItem[] sorted_annotations = annotations.toArray(new AnnotationItem[0]);
        Arrays.sort(sorted_annotations, AnnotationItem.COMPARATOR);
        out.writeInt(size());
        for (AnnotationItem tmp : sorted_annotations) {
            out.writeInt(context.getAnnotationOffset(tmp));
        }
    }

    @Override
    public void clear() {
        annotations.clear();
    }

    @Override
    public boolean isEmpty() {
        return annotations.isEmpty();
    }

    @Override
    public boolean contains(Object obj) {
        return annotations.contains(obj);
    }

    private AnnotationItem check(AnnotationItem annotation) {
        return Objects.requireNonNull(annotation,
                "annotation set can`t contain null annotation");
    }

    @Override
    public boolean add(AnnotationItem annotation) {
        return annotations.add(check(annotation).clone());
    }

    @Override
    public boolean remove(Object obj) {
        return annotations.remove(obj);
    }

    @Override
    public Iterator<AnnotationItem> iterator() {
        return annotations.iterator();
    }

    @Override
    public int size() {
        return annotations.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AnnotationSet) {
            AnnotationSet asobj = (AnnotationSet) obj;
            return Objects.equals(annotations, asobj.annotations);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(annotations);
    }

    @Override
    public AnnotationSet clone() {
        AnnotationSet out = new AnnotationSet();
        out.addAll(annotations);
        return out;
    }
}
