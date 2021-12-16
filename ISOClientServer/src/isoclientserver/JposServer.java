package isoclientserver;

import java.io.IOException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.ThreadPool;

/**
 *
 * @author ESRICCI
 */
public class JposServer {

    public static void main(String[] args) throws ISOException, IOException {
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
        ISOServer server = new ISOServer(7654, (ServerChannel) serverChannel,new ThreadPool(10, 100, "serverListeningThread"));

            server.addISORequestListener(new ISORequestListener() {
            // Se o cliente enviar uma mensagem, o servidor responderá e aprovará se for uma mensagem de solicitação
            @Override
            public boolean process(ISOSource source, ISOMsg msg) {
                try {
                    if (!msg.isRequest()) {
                        msg.setResponseMTI();
                        msg.set(39, "000");
                        source.send(msg);
                    }
                }
                catch (ISOException | IOException ex) {

                }

                return true;
            }
        });
        
                
        Thread serverThread = new Thread(server);
        serverThread.start(); // Além deste ponto, o servidor está ouvindo uma conexão do cliente
    }
        
}