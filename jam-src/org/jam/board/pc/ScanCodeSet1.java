package org.jam.board.pc;

public enum ScanCodeSet1 {
    KEY_RESERVED(0),
    KEY_ESC(1, '`'),
    KEY_1(2, '1'),
    KEY_2(3, '2'),
    KEY_3(4, '3'),
    KEY_4(5, '4'),
    KEY_5(6, '5'),
    KEY_6(7, '6'),
    KEY_7(8, '7'),
    KEY_8(9, '8'),
    KEY_9(0xa, '9'),
    KEY_0(0xb, '0'),
    KEY_MINUS(0xc, '-'),
    KEY_EQUAL(0xd, '='),
    KEY_BACKSPACE(0xe),
    KEY_TAB(0xf),
    KEY_Q(0x10, 'q', 'Q'),
    KEY_W(0x11, 'w', 'W'),
    KEY_E(0x12, 'e', 'E'),
    KEY_R(0x13, 'r', 'R'),
    KEY_T(0x14, 't', 'T'),
    KEY_Y(0x15, 'y', 'Y'),
    KEY_U(0x16, 'u', 'U'),
    KEY_I(0x17, 'i', 'I'),
    KEY_O(0x18, 'o'),
    KEY_P(0x19, 'p'),
    KEY_LEFTBRACKET(0x1a),
    KEY_RIGHTBRACKET(0x1b),
    KEY_CAPSLOCK(0x3a),
    KEY_A(0x1e, 'a'),
    KEY_S(0x1f, 's'),
    KEY_D(0x20, 'd'),
    KEY_F(0x21, 'f'),
    KEY_G(0x22, 'g'),
    KEY_H(0x23, 'h'),
    KEY_J(0x24, 'j'),
    KEY_K(0x25, 'k'),
    KEY_L(0x26, 'l'),
    KEY_SEMICOLON(0x27, ';'),
    KEY_APOSTROPHE(0x28, '\''),
    KEY_ENTER(0x1c),
    KEY_BACKTICK(0x29, '`'),
    KEY_LEFTSHIFT(0x2a),
    KEY_BACKSLASH(0x2b, '\\'),
    KEY_Z(0x2c, 'z'),
    KEY_X(0x2d, 'x'),
    KEY_C(0x2e, 'c'),
    KEY_V(0x2f, 'v'),
    KEY_B(0x30, 'b'),
    KEY_N(0x31, 'n'),
    KEY_M(0x32, 'm'),
    KEY_COMMA(0x33, ','),
    KEY_PERIOD(0x34, '.'),
    KEY_SLASH(0x35, '/'),
    KEY_RIGHTSHIFT(0x36),
    KEY_LEFTCTRL(0x1d),
    KEY_LEFTALT(0x38),
    KEY_SPACE(0x39, ' '),
    KEY_F1(0x3b),
    KEY_F2(0x3c),
    KEY_F3(0x3d),
    KEY_F4(0x3e),
    KEY_F5(0x3f),
    KEY_F6(0x40),
    KEY_F7(0x41),
    KEY_F8(0x42),
    KEY_F9(0x43),
    KEY_F10(0x44),
    KEY_NUMLOCK(0x45),
    KEY_SCROLLLOCK(0x46),
    KEY_KP7(0x47, '7'),
    KEY_KP8(0x48, '8'),
    KEY_KP9(0x49, '9'),
    KEY_KPMINUS(0x4a, '-'),
    KEY_KP4(0x4b, '4'),
    KEY_KP5(0x4c, '5'),
    KEY_KP6(0x4d, '6'),
    KEY_KPPLUS(0x4e, '+'),
    KEY_KP1(0x4f, '1'),
    KEY_KP2(0x50, '2'),
    KEY_KP3(0x51, '3'),
    KEY_KP0(0x52, '0'),
    KEY_KPDOT(0x53, '.'),
    KEY_F11(0x57),
    KEY_F12(0x58),
    KEY_CMD(0x5b),
    KEY_EXTENDED(0xe0);

    int makeCode;
    char base;
    char shifted;
    private static int RELEASED = 0x80;
    
    ScanCodeSet1(int code, char ch)
    {
        makeCode = code;
        this.base = ch;
    }
    
    ScanCodeSet1(int code, char ch, char shifted)
    {
        makeCode = code;
        this.base = ch;
        this.shifted = shifted;
    }
    
    ScanCodeSet1(int code)
    {
        makeCode = code;
        base = 0;
    }
    
    final public boolean code(int code)
    {
        return hasCode(code);
    }
    final public boolean hasCode(int code)
    {
        return code == makeCode;
    }
    
    public boolean released(int code)
    {
        return code == (makeCode | RELEASED);
    }
}
