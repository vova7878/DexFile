package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.PCList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class EncodedAnnotation implements PublicCloneable {

    private static final Comparator<PCList<AnnotationElement>> AE_LIST_COMPARATOR
            = PCList.getComparator(AnnotationElement.COMPARATOR);

    public static final Comparator<EncodedAnnotation> COMPARATOR = (a, b) -> {
        int out = TypeId.COMPARATOR.compare(a.type, b.type);
        if (out != 0) {
            return out;
        }

        return AE_LIST_COMPARATOR.compare(a.elements, b.elements);
    };

    private TypeId type;

    private PCList<AnnotationElement> elements;

    public EncodedAnnotation(TypeId type, PCList<AnnotationElement> elements) {
        setType(type);
        setElements(elements);
    }

    public EncodedAnnotation(TypeId type, AnnotationElement... elements) {
        this(type, new PCList<>(elements));
    }

    public final void setType(TypeId type) {
        this.type = Objects.requireNonNull(type,
                "type can`t be null").clone();
    }

    public final TypeId getType() {
        return type;
    }

    public final void setElements(PCList<AnnotationElement> elements) {
        this.elements = elements == null
                ? PCList.empty() : elements.clone();
    }

    public final PCList<AnnotationElement> getElements() {
        return elements;
    }

    public static EncodedAnnotation read(RandomInput in, ReadContext context) {
        TypeId type = context.type(in.readULeb128());
        int size = in.readULeb128();
        PCList<AnnotationElement> elements = PCList.empty();
        for (int i = 0; i < size; i++) {
            elements.add(AnnotationElement.read(in, context));
        }
        return new EncodedAnnotation(type, elements);
    }

    public void collectData(DataCollector data) {
        data.add(type);
        for (AnnotationElement tmp : elements) {
            data.fill(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(context.getTypeIndex(type));
        out.writeULeb128(elements.size());
        AnnotationElement[] sorted_elements = elements.toArray(new AnnotationElement[0]);
        Arrays.sort(sorted_elements, AnnotationElement.COMPARATOR);
        for (AnnotationElement tmp : sorted_elements) {
            tmp.write(context, out);
        }
    }

    @Override
    public String toString() {
        String elems = elements.stream()
                .map(AnnotationElement::toString)
                .collect(Collectors.joining(", "));
        return "EncodedAnnotation{" + type + " " + elems + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EncodedAnnotation) {
            EncodedAnnotation eaobj = (EncodedAnnotation) obj;
            return Objects.equals(type, eaobj.type)
                    && Objects.equals(elements, eaobj.elements);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, elements);
    }

    @Override
    public EncodedAnnotation clone() {
        return new EncodedAnnotation(type, elements);
    }
}
