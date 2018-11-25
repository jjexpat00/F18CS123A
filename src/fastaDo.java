
// &api_key=c0f2110548387a0d1b543dfd4594ddeb7e08

import java.io.IOException;

public class fastaDo {

    static Fasta seq = new Fasta();

    public static void main(String[] args) throws IOException {
        seq.getFasta();
        System.out.println(seq.printFasta());
    }
}
