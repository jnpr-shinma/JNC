package com.tailf.jnc;

/**
 * The Tagpath class is used to represent the name of individual schema nodes.
 * Each SchemaNode is identified by a Tagpath.
 */
public class Tagpath {
    public String[] p;

    public Tagpath(int size) {
        p = new String[size];
    }

    public Tagpath(String[] tp) {
        p = tp;
    }

    public Tagpath(String s) {
        final String[] tags = s.split("/");
        if(tags.length > 0 && tags[0].trim().equals("")==true) {
            p = new String[tags.length-1];
            System.arraycopy(tags, 1, p, 0, tags.length-1);
        } else {
            p = new String[tags.length];
            System.arraycopy(tags, 0, p, 0, tags.length);
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for (int i = 0; i < p.length; i++) {
            ret += p[i];
            if (i != p.length - 1) {
                ret += "/";
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < p.length; i++) {
            h += p[i].hashCode();
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof Tagpath)) {
            final Tagpath tp = (Tagpath) o;
            if (tp.p.length == p.length) {
                for (int i = 0; i < tp.p.length; i++) {
                    if (!tp.p[i].equals(p[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}
