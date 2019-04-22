import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;

import java.io.IOException;

public class FastaDo {

    static Fasta seq = new Fasta();

    public static void main(String[] args) throws IOException {
        //seq.get20Fasta();
    	//seq.get16kFasta();
    	//seq.printFasta();
        // System.out.println(seq.printFasta());

       try {
            seq.getMSA();
        } catch (JobSubmissionException e) {
            e.printStackTrace();
        } catch (ResultNotAvailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
