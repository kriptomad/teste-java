import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class ElectionServer {

    // HashMap thread-safe para armazenar os votos para cada candidato
    private static ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = 12345; // Porta do servidor

        // Inicializando votos para os candidatos
        votes.put("Candidato1", 0);
        votes.put("Candidato2", 0);
        votes.put("Candidato3", 0);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor de eleição iniciado na porta " + port);

            // Loop infinito para aceitar conexões de clientes
            while (true) {
                // Aceitar uma nova conexão de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado");

                // Criar uma nova thread para cada cliente conectado
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe que trata as conexões com os clientes
    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    // Buffer de entrada e saída para comunicação com o cliente
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                // Enviar mensagem de boas-vindas e instruções para o cliente
                out.println(
                        "Bem-vindo à eleição! Vote digitando o nome do candidato (Candidato1, Candidato2 ou Candidato3):");

                // Ler o voto do cliente
                String vote = in.readLine();

                // Validar o voto e processar
                if (votes.containsKey(vote)) {
                    // Atualizar o número de votos do candidato de forma thread-safe
                    votes.put(vote, votes.get(vote) + 1);
                    out.println("Seu voto para " + vote + " foi registrado.");
                } else {
                    out.println("Candidato inválido. Por favor, tente novamente.");
                }

                // Enviar os resultados parciais para o cliente
                out.println("Resultados parciais da eleição:");
                for (String candidate : votes.keySet()) {
                    out.println(candidate + ": " + votes.get(candidate) + " votos");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Fechar a conexão com o cliente
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}