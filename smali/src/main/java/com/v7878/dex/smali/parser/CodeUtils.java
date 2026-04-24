package com.v7878.dex.smali.parser;

import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.collections.IntMap;
import com.v7878.dex.Opcode;
import com.v7878.dex.builder.CodeBuilder;
import com.v7878.dex.smali.parser.SmaliParser.Args_array_dataContext;
import com.v7878.dex.smali.parser.SmaliParser.Args_format31tContext;
import com.v7878.dex.smali.parser.SmaliParser.Args_packed_switchContext;
import com.v7878.dex.smali.parser.SmaliParser.Args_sparse_switchContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CodeUtils {
    public static int parseRegister(CodeBuilder ib, String text) {
        char kind = text.charAt(0);
        int reg = Integer.parseUnsignedInt(text, 1, text.length(), 10);
        return switch (kind) {
            case 'v' -> ib.v(reg);
            case 'p' -> ib.p(reg);
            default -> throw shouldNotReachHere();
        };
    }

    public static List<Number> parseArrayData(int width, long[] data) {
        long min, max;
        switch (width) {
            case 1 -> {
                min = Byte.MIN_VALUE;
                // unsigned max
                max = 0xffL;
            }
            case 2 -> {
                min = Short.MIN_VALUE;
                // unsigned max
                max = 0xffffL;
            }
            case 4 -> {
                min = Integer.MIN_VALUE;
                // unsigned max
                max = 0xffffffffL;
            }
            case 8 -> {
                min = Long.MIN_VALUE;
                max = Long.MAX_VALUE;
            }
            default -> throw new IllegalArgumentException("Not a valid element width: " + width);
        }

        var table = new ArrayList<Number>(data.length);
        for (long value : data) {
            if (value < min || value > max) {
                throw new IllegalArgumentException(
                        String.format("%d does not fit into a %d-byte integer", value, width));
            }
            table.add(switch (width) {
                case 1 -> (byte) value;
                case 2 -> (short) value;
                case 4 -> (int) value;
                default -> value; // width == 8
            });
        }
        return Collections.unmodifiableList(table);
    }

    public static IntMap<Object> parsePackedSwitch(int first_key, Object[] labels) {
        var length = labels.length;
        var table = new IntMap<>(length);
        for (int i = 0; i < length; i++) {
            table.append(first_key + i, labels[i]);
        }
        return table.freeze();
    }

    public static IntMap<Object> parseSparseSwitch(int[] keys, Object[] labels) {
        var length = labels.length;
        assert keys.length == length;
        var table = new IntMap<>(length);
        for (int i = 0; i < length; i++) {
            table.append(keys[i], labels[i]);
        }
        return table.freeze();
    }

    public static boolean instruction(
            CodeBuilder ib, Opcode op, Object raw_args, List<Runnable> actions) {
        switch (op) {
            case PACKED_SWITCH -> {
                var args = (Args_format31tContext) raw_args;
                var current = ib.current_label();
                ib.nop();

                actions.add(() -> {
                    var metadata = ib.get_metadata(args.target);
                    if (!(metadata instanceof Args_packed_switchContext switch_args)) {
                        // TODO: msg
                        throw new IllegalArgumentException();
                    }

                    var backup = ib.current_label();
                    ib.append_position(current);

                    ib.switch_(args.reg, switch_args.table);

                    ib.append_position(backup);
                });
                return true;
            }
            case SPARSE_SWITCH -> {
                var args = (Args_format31tContext) raw_args;
                var current = ib.current_label();
                ib.nop();

                actions.add(() -> {
                    var metadata = ib.get_metadata(args.target);
                    if (!(metadata instanceof Args_sparse_switchContext switch_args)) {
                        // TODO: msg
                        throw new IllegalArgumentException();
                    }

                    var backup = ib.current_label();
                    ib.append_position(current);

                    ib.switch_(args.reg, switch_args.table);

                    ib.append_position(backup);
                });
                return true;
            }
            case FILL_ARRAY_DATA -> {
                var args = (Args_format31tContext) raw_args;
                var current = ib.current_label();
                ib.nop();

                actions.add(() -> {
                    var metadata = ib.get_metadata(args.target);
                    if (!(metadata instanceof Args_array_dataContext array_args)) {
                        // TODO: msg
                        throw new IllegalArgumentException();
                    }

                    var backup = ib.current_label();
                    ib.append_position(current);

                    ib.fill_array_data_raw(args.reg, array_args.width, array_args.table);

                    ib.append_position(backup);
                });
                return true;
            }
        }
        return false;
    }
}
