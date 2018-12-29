package rushydro.ru;

import org.junit.Test;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static org.junit.Assert.assertEquals;

public class QdataTest {

    @Test
    public void  testparseCurrentJson() throws IOException {
                Qdata qdata = new Qdata();
                String result = Qdata.parseCurrentJson(FRead( "parseurl.txt" ));
                String parse = FRead( "result.txt" );
                assertEquals( result, parse);
            }

    public String FRead(String namefile) throws IOException {

        InputStreamReader isr = new InputStreamReader( new FileInputStream( namefile ), "CP1251" );
        char[] cbuf = new char[8192];
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = isr.read( cbuf )) != -1) {
            sb.append( cbuf, 0, read );
        }

        String s = sb.toString();
        System.out.println( s );
        isr.close();

        return s;
    }
}
