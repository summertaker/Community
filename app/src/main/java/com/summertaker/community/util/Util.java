package com.summertaker.community.util;

import android.util.Log;

import com.summertaker.community.common.BaseApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Util {

    public static String convertedString(String text, String encoding) {
        if (encoding == null) {
            encoding = "ISO-8859-1"; // // JIS, SJIS, 8859_1, SHIFT-JIS
        }
        try {
            return new String(text.getBytes(encoding), Charset.forName("UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }
}
