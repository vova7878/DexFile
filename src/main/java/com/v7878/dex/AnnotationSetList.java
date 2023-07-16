package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.PCList;

public class AnnotationSetList extends PCList<AnnotationSet> {

    public AnnotationSetList(AnnotationSet... annotation_sets) {
        super(annotation_sets);
    }

    public static AnnotationSetList read(RandomInput in, ReadContext context) {
        AnnotationSetList out = new AnnotationSetList();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            int annotations_off = in.readInt();
            if (annotations_off != 0) {
                out.add(AnnotationSet.read(
                        in.duplicate(annotations_off), context));
            } else {
                out.add(AnnotationSet.empty());
            }
        }
        return out;
    }

    public static AnnotationSetList empty() {
        return new AnnotationSetList();
    }

    public void collectData(DataCollector data) {
        for (AnnotationSet tmp : this) {
            if (!tmp.isEmpty()) {
                data.add(tmp);
            }
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        int max_size = size();
        while (get(max_size - 1).isEmpty()) {
            max_size--;
        }
        out.writeInt(max_size);
        for (int i = 0; i < max_size; i++) {
            AnnotationSet tmp = get(i);
            if (tmp.isEmpty()) {
                out.writeInt(0);
            } else {
                out.writeInt(context.getAnnotationSetOffset(tmp));
            }
        }
    }

    @Override
    public boolean isEmpty() {
        if (super.isEmpty()) {
            return true;
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected AnnotationSet check(AnnotationSet annotation_set) {
        return annotation_set == null ? AnnotationSet.empty() : annotation_set;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AnnotationSetList) {
            return super.equals(obj);
        }
        return false;
    }

    @Override
    public AnnotationSetList clone() {
        AnnotationSetList out = new AnnotationSetList();
        out.addAll(this);
        return out;
    }
}
