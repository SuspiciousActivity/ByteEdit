package me.ByteEdit.decompiler;

public enum EnumDecompiler {
    BYTEEDIT("ByteEdit"), PROCYON("Procyon"), FERNFLOWER("FernFlower"), JD_GUI("JD-GUI"), CFR("CFR");

    final String name;

    EnumDecompiler(String name){
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
