package org.example;

public class A {

    public void foo() {
        new B().foo();
    }

    public void bar() {
        new B().bar();
    }
}
