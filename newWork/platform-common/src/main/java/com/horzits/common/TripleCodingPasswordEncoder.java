package com.horzits.common;

import com.horzits.common.utils.AOSCodec;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TripleCodingPasswordEncoder implements PasswordEncoder {


    @Override
    public String encode(CharSequence rawPassword) {
        AOSCodec aosCodec = new AOSCodec();
        return aosCodec.password(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        AOSCodec aosCodec = new AOSCodec();
        return aosCodec.password(rawPassword.toString()).equals(encodedPassword);
    }
}
