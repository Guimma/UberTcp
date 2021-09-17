import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class Client {
    private static final String serverIp = "192.168.18.5";
    private static final int serverPort = 7000;
    private static final int MIN_DURATION = 10000;
    private static final int MAX_DURATION = 30000;
    private static final String DESTINATION = "Sergipe";
    private static final Long DURATION = ThreadLocalRandom.current().nextLong(MIN_DURATION, MAX_DURATION + 1);
    private static final String OK_STATUS = "OK";
    private static final String WAIT_STATUS = "WAIT";
    private static final String END_STATUS = "END";
    private static String status;

    private static void run() {
        try
        {
            //ESTABELECE CONEXÃO COM SERVIDOR
            System.out.println("Conectando aos servidores Uber...");
            Socket clientSocket = new Socket (serverIp, serverPort);
            System.out.println(String.format("Conectado! (Servidor: %s)", clientSocket.getLocalAddress()));

            //CRIA UM PACOTE DE SAIDA PARA ENVIAR MENSAGENS, ASSOCIANDO-O A CONEXÃO
            ObjectOutputStream clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());
            clientOutput.writeObject(DESTINATION); //DEFINE DESTINO DO CLIENTE
            clientOutput.writeObject(DURATION); //DEFINE DURACAO DA VIAGEM
            clientOutput.flush(); //ENVIA O PACOTE

            //CRIA UM PACOTE DE ENTRADA PARA RECEBER MENSAGENS, ASSOCIADO A CONEXAO
            ObjectInputStream serverResponse = new ObjectInputStream (clientSocket.getInputStream());
            while (!END_STATUS.equals(status)) {
                status = serverResponse.readObject().toString(); //RECEBE STATUS DO SERVIDOR
                String message = serverResponse.readObject().toString(); //RECEBE MENSAGEM DO SERVIDOR
                //PROCESSA O PACOTE RECEBIDO
                System.out.println(message);
            }

            //FINALIZA A CONEXÃO
            clientSocket.close();
            System.out.println("Obrigado por viajar com a Uber!");
        }
        catch(Exception e) //SE OCORRER ALGUMA EXCECAO, ENTAO DEVE SER TRATADA
        {
            System.out.println("ERRO: \n" + e);
        }
    }

    public static void main (String args[])
    {
        run();
    }
}
