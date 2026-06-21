![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)
[![License](https://img.shields.io/github/license/vova7878/DexFile)](https://github.com/vova7878/DexFile/blob/main/LICENSE)

# About

This library is designed for reading, generating, modifying, and writing dex files. It was primarily inspired by the [Class-File API](https://openjdk.org/jeps/484), which was introduced in JDK 22.

### Installation

```kotlin
dependencies {
    implementation("io.github.vova7878:DexFile:<version>")
}
```

### Supported DEX Versions

| Version        | Supported API |      Read      |      Write      | Instruction Set                                       |
|:---------------|:-------------:|:--------------:|:---------------:|:------------------------------------------------------|
| **041**        |      35+      | ✅ <sup>1</sup> | ✅ <sup>1</sup>  | Standard <sup>2</sup>                                 |
| **040**        |      30+      |       ✅        |        ✅        | Standard + ODEX                                       |
| **039**        |      28+      |       ✅        |        ✅        | Standard + ODEX                                       |
| **038**        |      26+      |       ✅        |        ✅        | Standard + ODEX                                       |
| **037**        |      26+      |       ✅        |        ✅        | Standard + ODEX                                       |
| **037**        |    24 - 25    |       ✅        |        ✅        | Standard + ODEX + ⏳lambda opcodes <sup>3</sup>        |
| **036**        |    16 - 20    |       ✅        |        ✅        | Standard + ODEX <sup>4</sup>                          |
| **036**        |    14 - 15    |       ✅        |        ✅        | Standard + ODEX + expanded jumbo opcodes <sup>5</sup> |
| **035**        |      1+       |       ✅        |        ✅        | Standard + ODEX                                       |
| **013**        |      M5       |       ✅        |        ⏳        | Early Dalvik + ODEX <sup>6</sup>                      |
| **009**        |      M3       |       ✅        |        ⏳        | Early Dalvik + ODEX <sup>6</sup>                      |
| **cdex (001)** |    28 - 35    | ✅ <sup>7</sup> | ⚠️ <sup>8</sup> | Standard + ODEX                                       |

* <sup>1</sup> Supports reading and writing as a **container** that can combine multiple logical DEX files into a single physical file to save space and share data (e.g., strings).
* <sup>2</sup> Unlike all other DEX versions, version 041 does not contain any special `odex` instructions. This is because `odex` instructions were completely removed from the Android runtime starting in API 32, while the `dex041` container format was only introduced later in API 35.
* <sup>3</sup> An early experimental feature for Android 7.x that never made it to a stable release. However, samples exist and the opcodes are present in the ART source code. Support for these instructions is planned for a future release.
* <sup>4</sup> This format was rolled back to the `dex035` state in these Android versions, thus it has full read and write support in the library.
* <sup>5</sup> The format was never officially finalized by Google and was abandoned before a stable release. However, it fully existed, and this library provides complete read and write support for it, including all unusual instruction families (like expanded jumbo opcodes), based on full documentation and real samples.
* <sup>6</sup> Historical pre-release versions (Milestone 3 and 5). Reading is fully supported, including `odex` instructions. Writing is planned for a future release.
* <sup>7</sup> Full support. The peculiarity is that `cdex` is normally always packaged inside a `vdex` container.
* <sup>8</sup> Writing is only supported as a single file outside of `vdex`. Such a written file can be loaded using special techniques via native code in API 28–35, or the standard way via `InMemoryClassLoader` in API 34 and 35 (but **not** via `PathClassLoader`).

### Example

<details>
<summary><b>BrainFuck to DEX Compiler</b></summary>

This is a simple compiler for the [BrainFuck](https://en.wikipedia.org/wiki/Brainfuck) language to dex format.

```java
public static byte[] compile(int tape_length, String bf) {
    var impl_name = "com.v7878.bf.Main";
    var impl_id = TypeId.ofName(impl_name);

    var system_id = TypeId.of(System.class);

    var print_stream_id = TypeId.of(PrintStream.class);
    var system_out_id = FieldId.of(system_id, "out", print_stream_id);
    var system_write_id = MethodId.of(print_stream_id, "print", TypeId.V, TypeId.C);

    var input_stream_id = TypeId.of(InputStream.class);
    var system_in_id = FieldId.of(system_id, "in", input_stream_id);
    var system_read_id = MethodId.of(input_stream_id, "read", TypeId.I);

    var write_byte_id = MethodId.of(impl_id, "write", TypeId.V, TypeId.B);
    var read_byte_id = MethodId.of(impl_id, "read", TypeId.B);

    var impl_def = ClassBuilder.build(impl_id, cb -> cb
            .withSuperClass(TypeId.OBJECT)
            .withFlags(ACC_PUBLIC | ACC_FINAL)
            .withMethod(mb -> mb
                    .of(write_byte_id)
                    .withFlags(ACC_PRIVATE | ACC_STATIC)
                    .withCode(/* locals */ 1, ib -> {
                        ib.generate_lines();

                        int field_reg = ib.l(0);
                        ib.sget(field_reg, system_out_id);
                        ib.invoke(VIRTUAL, system_write_id, field_reg, ib.p(0));

                        ib.return_void();
                    })
            )
            .withMethod(mb -> mb
                    .of(read_byte_id)
                    .withFlags(ACC_PRIVATE | ACC_STATIC)
                    .withCode(/* locals */ 2, ib -> {
                        ib.generate_lines();

                        int field_reg = ib.l(0);
                        ib.sget(field_reg, system_in_id);
                        ib.invoke(VIRTUAL, system_read_id, field_reg);

                        int data_reg = ib.l(1);
                        ib.move_result(data_reg);

                        ib.unop(INT_TO_BYTE, data_reg, data_reg);

                        ib.return_(data_reg);
                    })
            )
            .withMethod(mb -> mb
                    .withFlags(ACC_PUBLIC | ACC_STATIC)
                    .withName("main")
                    .withReturnType(TypeId.V)
                    .withParameterTypes(TypeId.of(String[].class))
                    .withCode(/* locals */ 3, ib -> {
                        ib.generate_lines();

                        int tape_reg = ib.l(0);
                        ib.const_(tape_reg, tape_length);
                        ib.new_array(tape_reg, tape_reg, TypeId.B.array());
                        ib.local(tape_reg, "tape", TypeId.B.array());

                        int index_reg = ib.l(1);
                        ib.const_(index_reg, /* start */ 0);
                        ib.local(index_reg, "index", TypeId.I);

                        int tmp_reg = ib.l(2);
                        ib.local(tmp_reg, "tmp", TypeId.I);

                        int depth = 0;
                        var labels = new LinkedList<Integer>();

                        for (char op : bf.toCharArray()) {
                            switch (op) {
                                case '>' -> ib.binop_lit(ADD_INT, index_reg, index_reg, 1);
                                case '<' -> ib.binop_lit(ADD_INT, index_reg, index_reg, -1);
                                case '+' -> {
                                    ib.aget('B', tmp_reg, tape_reg, index_reg);
                                    ib.binop_lit(ADD_INT, tmp_reg, tmp_reg, 1);
                                    ib.aput('B', tmp_reg, tape_reg, index_reg);
                                }
                                case '-' -> {
                                    ib.aget('B', tmp_reg, tape_reg, index_reg);
                                    ib.binop_lit(ADD_INT, tmp_reg, tmp_reg, -1);
                                    ib.aput('B', tmp_reg, tape_reg, index_reg);
                                }
                                case '.' -> {
                                    ib.aget('B', tmp_reg, tape_reg, index_reg);
                                    ib.invoke(STATIC, write_byte_id, tmp_reg);
                                }
                                case ',' -> {
                                    ib.invoke(STATIC, read_byte_id);
                                    ib.move_result(tmp_reg);
                                    ib.aput('B', tmp_reg, tape_reg, index_reg);
                                }
                                case '[' -> {
                                    labels.add(depth);
                                    int open_depth = depth++;
                                    ib.label("label_open_" + open_depth);
                                    ib.aget('B', tmp_reg, tape_reg, index_reg);
                                    ib.if_testz(EQ, tmp_reg, "label_close_" + open_depth);
                                }
                                case ']' -> {
                                    int close_depth = labels.pollLast();
                                    ib.goto_("label_open_" + close_depth);
                                    ib.label("label_close_" + close_depth);
                                }
                                default -> { /* nop */ }
                            }
                        }

                        ib.return_void();
                    })
            )
    );

    return DexIO.write(Dex.of(impl_def));
}
```

</details>
