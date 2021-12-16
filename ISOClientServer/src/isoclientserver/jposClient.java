package isoclientserver;

import java.io.IOException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.ThreadPool;

public class jposClient {

    public static void main(String[] args) throws IOException, ISOException {
        
        Logger l = new Logger();
        l.addListener(new SimpleLogListener());
        GenericPackager serverPkg = new GenericPackager("E:\\ISO\\fields.xml");
        serverPkg.setLogger(l, "Server"); // Para que a saída possa ser diferenciada com base no domínio

        GenericPackager clientPkg = new GenericPackager("E:\\ISO\\fields.xml");
        clientPkg.setLogger(l, "Client");// Para que a saída possa ser diferenciada com base no domínio
        
        // Simular um servidor e escutar em uma porta
        ISOChannel serverChannel = new ASCIIChannel(serverPkg);
        ((ASCIIChannel) serverChannel).setHeader("ISO70100000");
        ((ASCIIChannel) serverChannel).setLogger(l, "server");
        ISOServer server = new ISOServer(7654, (ServerChannel) serverChannel, new ThreadPool(10, 100, "serverListeningThread"));

        
        ASCIIChannel clientChannel = new ASCIIChannel("127.0.0.1", 7654, clientPkg);
        clientChannel.setHeader("ISO70100000");
        clientChannel.setLogger(l, "client");
        clientChannel.connect(); // Conectar ao servidor, será visto no console de saída
        ISOChannel connectChannel = server.getLastConnectedISOChannel();// Uma vez que o servidor pode ter várias conexões, temos o último que está conectado a ele.

        ISOMsg serverInitiatedRequest = new ISOMsg();
        serverInitiatedRequest.set(0, "1804");
        serverInitiatedRequest.set(7, "1607161705");
        serverInitiatedRequest.set(11, "888402");
        serverInitiatedRequest.set(12, "160716170549");
        serverInitiatedRequest.set(24, "803");
        serverInitiatedRequest.set(25, "0000");
        serverInitiatedRequest.set(33, "101010");
        serverInitiatedRequest.set(37, "619817888402");
        
        clientChannel.send(serverInitiatedRequest);
        ISOMsg receivedRequest = clientChannel.receive();// Receber a mensagem de solicitação do servidor no cliente

        ISOMsg clientResponse = (ISOMsg) receivedRequest.clone();
        clientResponse.setResponseMTI();
        clientResponse.set(39, "000");
        clientChannel.send(clientResponse); // Enviar a resposta para o servidor
    }

}
