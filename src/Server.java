import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private static final int PORT = 7000;
    private static int availableCars = 2;
    private static final int WAIT_DURATION = 5000;
    private static final String OK_STATUS = "OK";
    private static final String WAIT_STATUS = "WAIT";
    private static final String END_STATUS = "END";
    private static List<Socket> connections = new ArrayList<Socket>();
    private static boolean DEV = true;


    public Server(Socket clientConnection) {
        this.connections.add(clientConnection);
    }

    public static void startServer() throws IOException {
        //INICIALIZA UM SERVICO DE ESCUTA POR CONEXOES NA PORTA ESPECIFICADA
        System.out.println("Sistema Uber online!");
        System.out.println("Aguardando por usuarios...\n");
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            //AGUARDA CONEXAO COM CLIENTE
            Socket clientConnection = serverSocket.accept(); //RECEBE CONEXAO E CRIA CANAL COM CLIENTE
            System.out.println("Solicitacao de viagem encontrada!");
            new Server(clientConnection).start();
        }
    }

    public static synchronized boolean hasAvailableCars() {
        if (availableCars > 0) {
            availableCars--;
            return true;
        }
        return false;
    }

    public static synchronized void incrementAvailableCars() {
        availableCars++;
    }

    public void run() {
        try {
            int lastConnection = connections.size() - 1;
            ObjectOutputStream serverOutput = new ObjectOutputStream(connections.get(lastConnection).getOutputStream());
            while (!hasAvailableCars()) {
                //ENVIA MENSAGEM DE ESPERA PARA O CLIENTE
                serverOutput.writeObject(WAIT_STATUS); //DEFINE STATUS DO PACOTE
                serverOutput.writeObject("Procurando motoristas parceiros..."); //DEFINE MENSAGEM DO PACOTE
                serverOutput.flush(); //ENVIA O PACOTE
                //AGUARDA 5 SEGUNDOS PARA TENTAR NOVAMENTE
                this.sleep(WAIT_DURATION);
            }

            //ENVIA MENSAGEM DE SUCESSO PARA O CLIENTE
            serverOutput.writeObject(OK_STATUS); //DEFINE STATUS DO PACOTE
            serverOutput.writeObject("Motorista encontrado!"); //DEFINE MENSAGEM DO PACOTE
            serverOutput.flush(); //ENVIA O PACOTE

            String rideId = Long.toString(this.getId());
            String clientId = Integer.toString(connections.get(lastConnection).getPort());
            System.out.println(String.format("Iniciando viagem numero: %s", rideId));
            System.out.println(String.format("Cliente: %s\n", clientId));

            //CRIA UM PACOTE DE ENTRADA PARA RECEBER MENSAGENS, ASSOCIADO A CONEXAO
            ObjectInputStream dataStream = new ObjectInputStream(connections.get(lastConnection).getInputStream());
            System.out.println("Obtendo destino da viagem...");
            String destination = dataStream.readObject().toString(); //RECEBE DESTINO DA VIAGEM
            System.out.println(String.format("Destino definido: %s", destination));
            System.out.println("Obtendo duracao da viagem...");
            String duration = dataStream.readObject().toString(); //RECEBE DURACAO DA VIAGEM
            System.out.println(String.format("Duracao definida: %s\n", duration));

            //AGUARDA O TEMPO DE DURACAO DA VIAGEM
            System.out.println(String.format("Iniciando viagem %s com cliente %s...", rideId, clientId));
            this.sleep(Integer.parseInt(duration));

            //ENVIA MENSAGEM DE ESPERA PARA O CLIENTE
            serverOutput.writeObject(END_STATUS); //DEFINE STATUS DO PACOTE
            serverOutput.writeObject("Voce chegou ao seu destino!"); //DEFINE MENSAGEM DO PACOTE
            serverOutput.flush(); //ENVIA O PACOTE

            //INCREMENTANDO NUMERO DE CARROS DISPONIVEIS
            incrementAvailableCars();

            //FINALIZA A CONEXAO
            connections.get(lastConnection).close();
            System.out.println(String.format("Fim da viagem %s com cliente %s...", rideId, clientId));
        }
        catch (Exception e) {
            if(DEV)
                e.printStackTrace(System.out);
            else
                System.out.println(e);
        }
    }

    public static void main(String[] args) throws IOException {
        startServer();
    }
}