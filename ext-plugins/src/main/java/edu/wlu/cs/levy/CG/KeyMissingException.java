// Key-size mismatch exception supporting KDTree class

package edu.wlu.cs.levy.CG;

class KeyMissingException extends Exception {

    public KeyMissingException() {
        super("Key not found");
    }

    // arbitrary; every serializable class has to have one of these
    public static final long serialVersionUID = 3L;

}
