package com.sean.backpackininventory.compat.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class StorageBackpackMenuRegistrationTest {
    @Test
    void backpackSlotsUseSophisticatedCoreExtraSlotAccounting() throws IOException {
        String resource = "/com/sean/backpackininventory/compat/storage/StorageBackpackMenu.class";
        var bytecode = StorageBackpackMenuRegistrationTest.class.getResourceAsStream(resource);
        assertNotNull(bytecode);

        int[] calls = {0};
        new ClassReader(bytecode).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (!name.equals("initSlotsAndContainers")) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name,
                            String descriptor, boolean isInterface) {
                        if (name.equals("addExtraSlot")) calls[0]++;
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        assertEquals(2, calls[0],
                "Both backpack slot branches must update Sophisticated Core's extra-slot count");
    }
}
