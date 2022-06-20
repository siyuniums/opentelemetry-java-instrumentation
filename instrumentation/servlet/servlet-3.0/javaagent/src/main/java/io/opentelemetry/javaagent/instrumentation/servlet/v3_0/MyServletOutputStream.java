/* SPDX-License-Identifier: Apache-2.0 */
package com.github.siyuniums;

import javax.servlet.ServletOutputStream;
import java.io.IOException;



public class MyServletOutputStream extends ServletOutputStream {
    ServletOutputStream sp;
    int headTag = -1; // record how many bits go so far for <head>

    private final String SNIPPET = new ReadInjection().readFile();
    public MyServletOutputStream(ServletOutputStream outputStream) {
        this.sp = outputStream;
    }


    @Override
    public void write(int b) throws IOException {
        if (headTag == -1 && b == 60){ // ASCII for <
            headTag = 0;
        } else if (headTag == 0 && b == 104){ // ASCII for h
            headTag = 1;
        } else if (headTag == 1 && b == 101){ // ASCII for e
            headTag = 2;
        } else if (headTag == 2 && b == 97){ // ASCII for a
            headTag = 3;
        } else if (headTag == 3 && b == 100) { // ASCII for d
            headTag = 4;
        } else if (headTag == 4 && b == 62) { // ASCII for >
            headTag = 5;
        } else {
            headTag = -1;
        }
        sp.write(b);
        if (headTag == 5){
            // begin to insert
            for (int i = 0; i < SNIPPET.length(); i++){
                sp.write((int)SNIPPET.charAt(i));
            }

        }

    }
}
