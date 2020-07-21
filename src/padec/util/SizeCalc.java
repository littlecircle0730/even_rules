package padec.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class SizeCalc {

    public static long calculateSize(Serializable obj) {
        long size = -1;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            size = baos.toByteArray().length;
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }
}
