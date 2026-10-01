/*
 * Copyright (c) 2016. Nordea Bank AB
 * Licensed under the MIT license (LICENSE.txt)
 */

package com.nordea.oss.copybook.golden;

import com.nordea.oss.copybook.CopyBookSerializer;
import com.nordea.oss.copybook.annotations.CopyBook;
import com.nordea.oss.copybook.annotations.CopyBookFieldFormat;
import com.nordea.oss.copybook.annotations.CopyBookLine;
import com.nordea.oss.copybook.converters.SignedDecimalToBigDecimal;
import com.nordea.oss.copybook.converters.SignedIntegerToBigInteger;
import com.nordea.oss.copybook.converters.SignedIntegerToInteger;
import com.nordea.oss.copybook.converters.SignedIntegerToLong;
import com.nordea.oss.copybook.converters.StringToString;
import com.nordea.oss.copybook.converters.TypeConverterIntEnum;
import com.nordea.oss.copybook.converters.TypeConverterStringEnum;
import com.nordea.oss.copybook.serializers.CopyBookFieldSigningType;
import com.nordea.oss.copybook.serializers.FullLastArrayShortMapper;
import com.nordea.oss.copybook.serializers.FullMapper;
import com.nordea.oss.copybook.serializers.PackedFirstLevelMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Golden-byte regression tests: serialized bytes and deserialized values are compared against
 * fixtures in src/test/resources/golden/ that were captured from the JDK 8 baseline build.
 * Run with -Dgolden.write.dir=DIR to (re)write the fixtures instead of asserting.
 */
@RunWith(Parameterized.class)
public class GoldenBytesTest {

    public enum Level implements TypeConverterIntEnum {
        LOW(10), HIGH(30);
        private final int value;
        Level(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    public enum Code implements TypeConverterStringEnum {
        AA("AA"), ZZ("ZZ");
        private final String value;
        Code(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    // PIC X
    @CopyBook(type = FullMapper.class)
    public static class PicX {
        @CopyBookLine("01 SHORT PIC X(2).")
        public String shortField;
        @CopyBookLine("01 PADDED PIC X(10).")
        public String padded;
        @CopyBookLine("01 EMPTY PIC X(4).")
        public String empty;
    }

    // PIC 9 unsigned integers
    @CopyBook(type = FullMapper.class)
    public static class Pic9 {
        @CopyBookLine("01 I PIC 9(5).")
        public int i;
        @CopyBookLine("01 L PIC 9(12).")
        public long l;
        @CopyBookLine("01 BI PIC 9(20).")
        public BigInteger bi;
        @CopyBookLine("01 ZERO PIC 9(3).")
        public int zero;
        @CopyBookLine("01 FLAG PIC 9(1).")
        public boolean flag;
        @CopyBookLine("01 LEVEL PIC 9(2).")
        public Level level;
    }

    // PIC S9 signed integers, default PREFIX signing
    @CopyBook(type = FullMapper.class)
    public static class PicS9Prefix {
        @CopyBookLine("01 NEG PIC S9(5).")
        public int neg;
        @CopyBookLine("01 POS PIC S9(5).")
        public int pos;
        @CopyBookLine("01 L PIC S9(15).")
        public long l;
        @CopyBookLine("01 BI PIC S9(20).")
        public BigInteger bi;
    }

    @CopyBook(type = FullMapper.class)
    @CopyBookFieldFormat(type = SignedIntegerToInteger.class, paddingChar = '0', nullFillerChar = (byte)0, signingType = CopyBookFieldSigningType.POSTFIX, rightPadding = false)
    @CopyBookFieldFormat(type = SignedDecimalToBigDecimal.class, paddingChar = '0', nullFillerChar = (byte)0, signingType = CopyBookFieldSigningType.POSTFIX, rightPadding = false)
    public static class PicS9Postfix {
        @CopyBookLine("01 NEG PIC S9(5).")
        public int neg;
        @CopyBookLine("01 POS PIC S9(5).")
        public int pos;
        @CopyBookLine("01 DEC PIC S9(3)V9(2).")
        public BigDecimal dec;
    }

    @CopyBook(type = FullMapper.class)
    @CopyBookFieldFormat(type = SignedIntegerToInteger.class, paddingChar = '0', nullFillerChar = (byte)0, signingType = CopyBookFieldSigningType.LAST_BYTE_BIT8, rightPadding = false)
    public static class PicS9LastByteBit8 {
        @CopyBookLine("01 NEG PIC S9(4).")
        public int neg;
        @CopyBookLine("01 POS PIC S9(4).")
        public int pos;
    }

    // PIC 9V9 / S9V9 BigDecimals
    @CopyBook(type = FullMapper.class)
    public static class PicDecimal {
        @CopyBookLine("01 U PIC 9(3)V9(2).")
        public BigDecimal u;
        @CopyBookLine("01 S PIC S9(3)V9(2).")
        public BigDecimal s;
        @CopyBookLine("01 SPOS PIC S9(5)V9(3).")
        public BigDecimal spos;
        @CopyBookLine("01 SMALL PIC 9V9.")
        public BigDecimal small;
    }

    @CopyBook()
    public static class Item {
        @CopyBookLine("02 NAME PIC X(4).")
        public String name;
        @CopyBookLine("02 QTY PIC S9(3).")
        public int qty;

        public Item() {
        }

        public Item(String name, int qty) {
            this.name = name;
            this.qty = qty;
        }
    }

    // OCCURS arrays
    @CopyBook(type = FullMapper.class)
    public static class Occurs {
        @CopyBookLine("01 TAGS OCCURS 3 TIMES PIC X(4).")
        public String[] tags;
        @CopyBookLine("01 NUMS OCCURS 4 TIMES PIC 9(3).")
        public int[] nums;
        @CopyBookLine("01 ITEMS OCCURS 2 TIMES.")
        public Item[] items;
    }

    // OCCURS ... DEPENDING ON
    @CopyBook(type = FullMapper.class)
    public static class DependingOn {
        @CopyBookLine("01 TAGCOUNT PIC 9(2).")
        public int tagCount;
        @CopyBookLine("01 ITEMCOUNT PIC 9(2).")
        public int itemCount;
        @CopyBookLine("01 TAGS OCCURS 0 TO 5 TIMES PIC X(4) DEPENDING ON TAGCOUNT.")
        public String[] tags;
        @CopyBookLine("01 ITEMS OCCURS 0 TO 3 TIMES DEPENDING ON ITEMCOUNT.")
        public Item[] items;
    }

    // Misc types
    @CopyBook(type = FullMapper.class)
    public static class MiscTypes {
        @CopyBookLine("01 YN PIC X(1).")
        public boolean yn;
        @CopyBookLine("01 CODE PIC X(2).")
        public Code code;
        @CopyBookLine("01 TS PIC X(14).")
        public LocalDateTime ts;
    }

    // EBCDIC cp037 strings and unsigned numbers
    @CopyBook(charset = "cp037", type = FullMapper.class)
    public static class Cp037Strings {
        @CopyBookLine("01 TEXT PIC X(12).")
        public String text;
        @CopyBookLine("01 SYMBOLS PIC X(8).")
        public String symbols;
        @CopyBookLine("01 NUM PIC 9(4).")
        public int num;
        @CopyBookLine("01 DEC PIC 9(3)V9(2).")
        public BigDecimal dec;
    }

    @CopyBook(charset = "cp037", type = FullMapper.class)
    @CopyBookFieldFormat(type = StringToString.class, paddingChar = ' ', nullFillerChar = (byte)0, signingType = CopyBookFieldSigningType.PREFIX, rightPadding = false)
    public static class Cp037LeftPadded {
        @CopyBookLine("01 TEXT PIC X(8).")
        public String text;
    }

    // EBCDIC cp037 signed zoned decimal, PREFIX signing
    @CopyBook(charset = "cp037", type = FullMapper.class)
    public static class Cp037SignedPrefix {
        @CopyBookLine("01 NEG PIC S9(4).")
        public int neg;
        @CopyBookLine("01 POS PIC S9(4).")
        public int pos;
        @CopyBookLine("01 DEC PIC S9(3)V9(2).")
        public BigDecimal dec;
    }

    // EBCDIC cp037 signed zoned decimal, overpunched last byte
    @CopyBook(charset = "cp037", type = FullMapper.class)
    @CopyBookFieldFormat(type = SignedIntegerToInteger.class, paddingChar = '0', nullFillerChar = (byte)0, signingType = CopyBookFieldSigningType.LAST_BYTE_EBCDIC_BIT5, rightPadding = false)
    @CopyBookFieldFormat(type = SignedIntegerToLong.class, paddingChar = '0', nullFillerChar = (byte)0, signingType = CopyBookFieldSigningType.LAST_BYTE_EBCDIC_BIT5, rightPadding = false)
    @CopyBookFieldFormat(type = SignedIntegerToBigInteger.class, paddingChar = '0', nullFillerChar = (byte)0, signingType = CopyBookFieldSigningType.LAST_BYTE_EBCDIC_BIT5, rightPadding = false)
    @CopyBookFieldFormat(type = SignedDecimalToBigDecimal.class, paddingChar = '0', nullFillerChar = (byte)0, signingType = CopyBookFieldSigningType.LAST_BYTE_EBCDIC_BIT5, rightPadding = false)
    public static class Cp037SignedEbcdicBit5 {
        @CopyBookLine("01 NEG PIC S9(4).")
        public int neg;
        @CopyBookLine("01 POS PIC S9(4).")
        public int pos;
        @CopyBookLine("01 ZERO PIC S9(3).")
        public int zero;
        @CopyBookLine("01 L PIC S9(10).")
        public long l;
        @CopyBookLine("01 BI PIC S9(12).")
        public BigInteger bi;
        @CopyBookLine("01 DECNEG PIC S9(3)V9(2).")
        public BigDecimal decNeg;
        @CopyBookLine("01 DECPOS PIC S9(3)V9(2).")
        public BigDecimal decPos;
    }

    // README style request record in EBCDIC
    @CopyBook(charset = "cp037", type = FullMapper.class)
    public static class Cp037Request {
        @CopyBookLine("02 ID PIC 9(2).")
        public int id;
        @CopyBookLine("02 CMD PIC X(10).")
        public String command;
        @CopyBookLine("01 ARGS OCCURS 10 TIMES.")
        @CopyBookLine("02 ARG PIC X(8).")
        public String[] args;
    }

    @CopyBook(type = FullLastArrayShortMapper.class)
    public static class LastArrayShort {
        @CopyBookLine("01 ID PIC 9(3).")
        public int id;
        @CopyBookLine("01 COUNT PIC 9.")
        public int fields_count;
        @CopyBookLine("01 FIELDS OCCURS 10 TIMES PIC S9(4).")
        public int[] fields;
    }

    @CopyBook(charset = "UTF-8", type = PackedFirstLevelMapper.class)
    public static class PackedFirstLevel {
        @CopyBookLine("01 ID PIC 9(8).")
        public int id;
        @CopyBookLine("01 CMD PIC X(10).")
        public String command;
        @CopyBookLine("01 ITEM.")
        public Item item;
        @CopyBookLine("01 ARGCNT PIC 9(2).")
        public int argsCount;
        @CopyBookLine("01 ARGS OCCURS 4 TIMES.")
        @CopyBookLine("02 ARG PIC X(8).")
        public String[] args;
    }

    private static final class Case {
        final String name;
        final Class<?> type;
        final Supplier<Object> fixture;

        Case(String name, Class<?> type, Supplier<Object> fixture) {
            this.name = name;
            this.type = type;
            this.fixture = fixture;
        }
    }

    private static List<Case> cases() {
        List<Case> cases = new ArrayList<>();
        cases.add(new Case("pic_x", PicX.class, () -> {
            PicX o = new PicX();
            o.shortField = "ok";
            o.padded = "hello";
            o.empty = "";
            return o;
        }));
        cases.add(new Case("pic_9", Pic9.class, () -> {
            Pic9 o = new Pic9();
            o.i = 42;
            o.l = 123456789012L;
            o.bi = new BigInteger("12345678901234567890");
            o.zero = 0;
            o.flag = true;
            o.level = Level.HIGH;
            return o;
        }));
        cases.add(new Case("pic_s9_prefix", PicS9Prefix.class, () -> {
            PicS9Prefix o = new PicS9Prefix();
            o.neg = -123;
            o.pos = 123;
            o.l = -12345678901234L;
            o.bi = new BigInteger("-1234567890123456789");
            return o;
        }));
        cases.add(new Case("pic_s9_postfix", PicS9Postfix.class, () -> {
            PicS9Postfix o = new PicS9Postfix();
            o.neg = -123;
            o.pos = 123;
            o.dec = new BigDecimal("-10.01");
            return o;
        }));
        cases.add(new Case("pic_s9_last_byte_bit8", PicS9LastByteBit8.class, () -> {
            PicS9LastByteBit8 o = new PicS9LastByteBit8();
            o.neg = -987;
            o.pos = 987;
            return o;
        }));
        cases.add(new Case("pic_9v9_decimal", PicDecimal.class, () -> {
            PicDecimal o = new PicDecimal();
            o.u = new BigDecimal("12.34");
            o.s = new BigDecimal("-12.34");
            o.spos = new BigDecimal("1234.567");
            o.small = new BigDecimal("1.5");
            return o;
        }));
        cases.add(new Case("occurs", Occurs.class, () -> {
            Occurs o = new Occurs();
            o.tags = new String[] { "ab", "cdef" };
            o.nums = new int[] { 1, 22, 333 };
            o.items = new Item[] { new Item("x", -1), new Item("yz", 42) };
            return o;
        }));
        cases.add(new Case("depending_on", DependingOn.class, () -> {
            DependingOn o = new DependingOn();
            o.tags = new String[] { "t1", "t2", "t3" };
            o.tagCount = o.tags.length;
            o.items = new Item[] { new Item("one", 1), new Item("two", -2) };
            o.itemCount = o.items.length;
            return o;
        }));
        cases.add(new Case("misc_types", MiscTypes.class, () -> {
            MiscTypes o = new MiscTypes();
            o.yn = true;
            o.code = Code.ZZ;
            o.ts = LocalDateTime.parse("2016-02-29T23:59:58");
            return o;
        }));
        cases.add(new Case("cp037_strings", Cp037Strings.class, () -> {
            Cp037Strings o = new Cp037Strings();
            o.text = "Hello World";
            o.symbols = "a-Z_0.@$";
            o.num = 1234;
            o.dec = new BigDecimal("987.65");
            return o;
        }));
        cases.add(new Case("cp037_left_padded", Cp037LeftPadded.class, () -> {
            Cp037LeftPadded o = new Cp037LeftPadded();
            o.text = "ok";
            return o;
        }));
        cases.add(new Case("cp037_signed_prefix", Cp037SignedPrefix.class, () -> {
            Cp037SignedPrefix o = new Cp037SignedPrefix();
            o.neg = -42;
            o.pos = 42;
            o.dec = new BigDecimal("-1.23");
            return o;
        }));
        cases.add(new Case("cp037_signed_ebcdic_bit5", Cp037SignedEbcdicBit5.class, () -> {
            Cp037SignedEbcdicBit5 o = new Cp037SignedEbcdicBit5();
            o.neg = -1234;
            o.pos = 1234;
            o.zero = 0;
            o.l = -123456789L;
            o.bi = new BigInteger("98765432109");
            o.decNeg = new BigDecimal("-12.34");
            o.decPos = new BigDecimal("56.78");
            return o;
        }));
        cases.add(new Case("cp037_request", Cp037Request.class, () -> {
            Cp037Request o = new Cp037Request();
            o.id = 1;
            o.command = "cmd";
            o.args = new String[] { "arg1", "arg2" };
            return o;
        }));
        cases.add(new Case("last_array_short", LastArrayShort.class, () -> {
            LastArrayShort o = new LastArrayShort();
            o.id = 7;
            o.fields = new int[] { -1, 2, -3 };
            o.fields_count = o.fields.length;
            return o;
        }));
        cases.add(new Case("packed_first_level", PackedFirstLevel.class, () -> {
            PackedFirstLevel o = new PackedFirstLevel();
            o.id = 1;
            o.command = "cmd1234";
            o.item = new Item("abc", -5);
            o.args = new String[] { "do", "stuff" };
            o.argsCount = o.args.length;
            return o;
        }));
        return cases;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        List<Object[]> params = new ArrayList<>();
        for (Case c : cases()) {
            params.add(new Object[] { c.name, c });
        }
        return params;
    }

    private final String name;
    private final Case testCase;

    public GoldenBytesTest(String name, Object testCase) {
        this.name = name;
        this.testCase = (Case) testCase;
    }

    @Test
    public void serializeMatchesGoldenBytes() throws Exception {
        CopyBookSerializer serializer = new CopyBookSerializer(testCase.type);
        byte[] bytes = serializer.serialize(testCase.fixture.get());
        String writeDir = System.getProperty("golden.write.dir");
        if (writeDir != null) {
            Path dir = Paths.get(writeDir);
            Files.createDirectories(dir);
            Files.write(dir.resolve(name + ".bin"), bytes);
            Files.write(dir.resolve(name + ".hex"), (toHex(bytes) + "\n").getBytes(StandardCharsets.US_ASCII));
            Object roundTrip = serializer.deserialize(bytes, testCase.type);
            Files.write(dir.resolve(name + ".txt"), (dump(roundTrip) + "\n").getBytes(StandardCharsets.UTF_8));
            return;
        }
        assertEquals(readResource(name + ".hex").trim(), toHex(bytes));
    }

    @Test
    public void deserializeGoldenBytesMatchesGoldenValues() throws Exception {
        if (System.getProperty("golden.write.dir") != null) {
            return;
        }
        byte[] golden = fromHex(readResource(name + ".hex").trim());
        CopyBookSerializer serializer = new CopyBookSerializer(testCase.type);
        Object obj = serializer.deserialize(golden, testCase.type);
        assertEquals(readResource(name + ".txt").trim(), dump(obj));
        assertArrayEquals(golden, serializer.serialize(obj));
    }

    private static String readResource(String fileName) throws IOException {
        try (InputStream in = GoldenBytesTest.class.getResourceAsStream("/golden/" + fileName)) {
            assertNotNull("Missing golden resource /golden/" + fileName, in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private static String dump(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return "null";
        }
        Class<?> type = obj.getClass();
        if (type.isArray()) {
            List<String> items = new ArrayList<>();
            for (int i = 0; i < Array.getLength(obj); i++) {
                items.add(dump(Array.get(obj, i)));
            }
            return "[" + String.join(", ", items) + "]";
        }
        if (type.isPrimitive() || obj instanceof Number || obj instanceof Boolean || obj instanceof Enum
                || obj instanceof LocalDateTime) {
            return obj.toString();
        }
        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }
        Field[] fields = type.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        List<String> parts = new ArrayList<>();
        for (Field field : fields) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            parts.add(field.getName() + "=" + dump(field.get(obj)));
        }
        return type.getSimpleName() + "{" + String.join(", ", parts) + "}";
    }
}
