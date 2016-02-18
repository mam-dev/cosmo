package org.unitedinternet.cosmo.ext;

public class Main {

    public static void main(String[] args) {
        String a = "localhost";
        String[] pieces = a.split("\\|");
        System.out.println(pieces.length);
    }

}
