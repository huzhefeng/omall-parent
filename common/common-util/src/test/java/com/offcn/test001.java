package com.offcn;

public class test001 {

    public static void main(String[] args) {
        Integer i1 = new Integer(12);
        Integer i2 = new Integer(12);

        String a = new String("aaaaaaaaaa");

        System.out.println(a.hashCode());
        System.out.println(i1 == i2); //不等

        Integer i3 = 126;
        Integer i4 = 126;
        int i5 = 126;
        System.out.println(i3 == i4); //等
        System.out.println(i3 == i5);//等

        Integer i6 = 128;
        Integer i7 = 128;
        int i8 = 128;
        System.out.println(i6 == i7);//不等
        System.out.println(i6 == i8);//不等
    }
}
