package net.bytebuddy.implementation.bytecode.constant;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Field;

/**
 * Represents a {@link Field} constant for a given type.
 */
public class FieldConstant implements StackManipulation {

    /**
     * The field to be represent as a {@link Field}.
     */
    private final FieldDescription.InDefinedShape fieldDescription;

    /**
     * Creates a new field constant.
     *
     * @param fieldDescription The field to be represent as a {@link Field}.
     */
    public FieldConstant(FieldDescription.InDefinedShape fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    /**
     * Returns a cached version of this field constant.
     *
     * @return A cached version of this field constant.
     */
    public StackManipulation cached() {
        return new Cached(this);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
        try {
            return new Compound(
                    ClassConstant.of(fieldDescription.getDeclaringType()),
                    new TextConstant(fieldDescription.getInternalName()),
                    MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(Class.class.getMethod("getDeclaredField", String.class)))
            ).apply(methodVisitor, implementationContext);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Cannot locate Class::getDeclaredField", exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other == null || getClass() != other.getClass()) {
            return false;
        }
        FieldConstant fieldConstant = (FieldConstant) other;
        return fieldDescription.equals(fieldConstant.fieldDescription);
    }

    @Override
    public int hashCode() {
        return fieldDescription.hashCode();
    }

    /**
     * A cached version of a {@link FieldConstant}.
     */
    protected static class Cached implements StackManipulation {

        /**
         * The field constant stack manipulation.
         */
        private final StackManipulation fieldConstant;

        /**
         * Creates a new cached version of a field constant.
         *
         * @param fieldConstant The field constant stack manipulation.
         */
        public Cached(StackManipulation fieldConstant) {
            this.fieldConstant = fieldConstant;
        }

        @Override
        public boolean isValid() {
            return fieldConstant.isValid();
        }

        @Override
        public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
            return FieldAccess.forField(implementationContext.cache(fieldConstant, TypeDescription.ForLoadedType.of(Field.class)))
                    .read()
                    .apply(methodVisitor, implementationContext);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            } else if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Cached cached = (Cached) other;
            return fieldConstant.equals(cached.fieldConstant);
        }

        @Override
        public int hashCode() {
            return fieldConstant.hashCode();
        }
    }
}
