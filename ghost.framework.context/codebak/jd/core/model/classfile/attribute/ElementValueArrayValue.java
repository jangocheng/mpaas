/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package ghost.framework.context.asm.jd.core.model.classfile.attribute;

public class ElementValueArrayValue implements ElementValue {
    protected ElementValue[] values;

    public ElementValueArrayValue(ElementValue[] values) {
        this.values = values;
    }

    public ElementValue[] getValues() {
        return values;
    }

    @Override
    public void accept(ElementValueVisitor visitor) {
        visitor.visit(this);
    }
}
