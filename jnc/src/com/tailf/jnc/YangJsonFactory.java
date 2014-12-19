package com.tailf.jnc;

import com.fasterxml.jackson.core.JsonFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Json Factory wrapper to encapsulate json factory
 * Created by jalandip on 12/18/14.
 */
public class YangJsonFactory {

    private static final JsonFactory jsonFactory = new JsonFactory();


    public static JsonFactory jsonFactory() {
        return jsonFactory;
    }
}
