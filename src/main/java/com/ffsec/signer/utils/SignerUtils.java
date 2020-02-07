package com.ffsec.signer.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class SignerUtils {

    public static byte[] convertRequestParameters(Map<String,String[]> params) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(());

        params.forEach((k,v) -> {
            byte[] byteArray;
            for(int i = 0; i < v.length; i++) {
                byteArray = v[i].getBytes();
                try {
                    byteArrayOutputStream.write(byteArray);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        byteArrayOutputStream.toByteArray();

    }
}
