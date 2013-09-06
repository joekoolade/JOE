package joeq.Runtime;

/*
* @author V.Benjamin Livshits <livshits@stanford.edu>
* @version $Id: SilentDebugImpl.java,v 1.1 2005/02/17 22:17:09 livshits Exp $
*/
public class SilentDebugImpl implements Debug.Delegate {
    public void write(byte[] msg, int size) {
        //for (int i=0; i<size; ++i)
            //System.err.print((char) msg[i]);
    }

    public void write(String msg) {
        //System.err.print(msg);
    }

    public void writeln(byte[] msg, int size) {
//        write(msg, size);
//        System.err.println();
    }

    public void writeln(String msg) {
//        System.err.println(msg);
    }

    public void die(int code) {
        new InternalError().printStackTrace();
        System.exit(code);
    }

}
