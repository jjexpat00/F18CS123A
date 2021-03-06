import compbio.data.msa.MsaWS;
import compbio.data.sequence.Alignment;
import compbio.data.sequence.ClustalAlignmentUtil;
import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.SequenceUtil;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.ws.client.Services;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Scanner;

public class Fasta {

    /**
     * Method makes Entrez API call to get FASTA file from query, creating or replacing "f.fasta".
     * @throws IOException
     */
    public void getFasta() throws IOException {

        String base = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
        String query = "1516214787,1516461622,1516461432,1516461375,1516461514,1516461413,1516461451,1516461584,1516461704,1516461565,1516461470,1516461394,1516460539,1516461337,1516461546";
        String apikey = APIKey.getKey(); // API Key redacted

        URL url = new URL(base + "efetch.fcgi?db=nuccore&id=" + query + "&rettype=fasta&retmode=text&api_key=" + apikey);
        InputStream in = url.openStream();
        Files.copy(in, Paths.get("f.fasta"), StandardCopyOption.REPLACE_EXISTING);
        in.close();

    }

    /**
     * Method prints the entire FASTA query to verify its existence
     * @return FASTA query
     * @throws IOException
     */
    public String printFasta() throws IOException {

        String newline = System.getProperty("line.separator");
        BufferedReader in = new BufferedReader(new FileReader("f.fasta"));
        String seq = "";
        String line;

        while( (line = in.readLine() ) != null) {
            seq += line + newline;

        }
        return seq;

    }

    /**
     * Method makes call to JABAWS to perform MSA on previously fetched sequence. Gives user a set of options of MSA algorithms.
     * @throws JobSubmissionException
     * @throws ResultNotAvailableException
     * @throws InterruptedException
     * @throws IOException
     */
    public void getMSA() throws JobSubmissionException, ResultNotAvailableException, InterruptedException, IOException {

        if (Paths.get("f.fast") == null) {
             System.out.println("No FASTA found.");
             return;
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("Muscle: mu");
        System.out.println("Mafft: ma");
        System.out.println("ClustalO: co");
        System.out.println("ClustalW: cw");
        System.out.println("Select MSA option: ");
        String input = sc.nextLine();

        getMSAHelper(input);

    }

    /**
     * Helper method to send user input selection to JABAWS for MSA. Future implementation will run MSA locally.
     * @param serviceName User input selection
     * @throws IOException
     * @throws JobSubmissionException
     * @throws InterruptedException
     * @throws ResultNotAvailableException
     */
    private void getMSAHelper(String serviceName) throws IOException, JobSubmissionException, InterruptedException, ResultNotAvailableException {

        String qualifiedServiceName = "http://msa.data.compbio/01/01/2010/";
        String host = "http://www.compbio.dundee.ac.uk/jabaws";

        // Default service is Clustal Omega
        Services clustal = Services.ClustalWS;

        if (serviceName.equalsIgnoreCase("co")) {
            clustal = Services.ClustalOWS;
            qualifiedServiceName = "http://msa.data.compbio/01/12/2010/";   // Later implementation on their end requires different service name
            System.out.println("Now aligning with ClustalOmega...");
        }
        else if (serviceName.equalsIgnoreCase("mu")){
            clustal = Services.MuscleWS;
            System.out.println("Now aligning with Muscle...");
        }
        else if (serviceName.equalsIgnoreCase("cw")){
            clustal = Services.ClustalWS;
            System.out.println("Now aligning with ClustalW...");
        }
        else if (serviceName.equalsIgnoreCase("ma")){
            clustal = Services.MafftWS;
            System.out.println("Now aligning with Mafft...");
        }

        URL url = new URL(host + "/" + clustal.toString() + "?wsdl");
        QName qname = new QName(qualifiedServiceName, clustal.toString());
        Service serv = Service.create(url, qname);
        MsaWS msaws = serv.getPort(new QName(qualifiedServiceName, clustal + "Port"), MsaWS.class);

        List<FastaSequence> fastalist = SequenceUtil.readFasta(new FileInputStream("f.fasta"));
        String jobId = msaws.align(fastalist);

        while (msaws.getJobStatus(jobId) != JobStatus.FINISHED) {
            Thread.sleep(1000);                 // wait one second, then recheck status
            System.out.println("Waiting...");
        }

        File file = new File("out.msa");    // Removes older MSA outputs
        file.delete();
        Alignment alignment = msaws.getResult(jobId);
        ClustalAlignmentUtil.writeClustalAlignment(new FileWriter("out.msa"), alignment);
        System.out.println("Alignment completed. See \"out.msa\" file.");
    }

}

