import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.io.InputStream;
import java.util.*;
import java.io.BufferedOutputStream;

import model.*;
import util.Logger;
import util.StreamUtil;

public class Runner {
    private final InputStream inputStream;
    private final OutputStream outputStream;

    Runner(String host, int port, String token) throws IOException {
        Socket socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        inputStream = new BufferedInputStream(socket.getInputStream());
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        StreamUtil.writeString(outputStream, token);
        outputStream.flush();
    }

    void run() throws IOException {
        MyStrategy myStrategy = new MyStrategy();
        while (true) {
            ServerMessageGame message = ServerMessageGame.readFrom(inputStream);
            Logger.log(message.toString());
            PlayerView playerView = message.getPlayerView();
            if (playerView == null) {
                break;
            }
            Map<Integer, UnitAction> actions = new HashMap<>();
            for (Unit unit : playerView.getGame().getUnits()) {
                if (unit.getPlayerId() == playerView.getMyId()) {
                    Debug debug = new Debug(outputStream);
                    myStrategy.update(playerView.getGame(), unit, debug);
                    UnitAction action = myStrategy.getAction();
                    Logger.log(action.toString());
                    actions.put(unit.getId(), action);
                }
            }
            new PlayerMessageGame.ActionMessage(actions).writeTo(outputStream);
            outputStream.flush();
        }
    }

    public static void main(String[] args) throws IOException {
        String host = args.length < 1 ? "127.0.0.1" : args[0];
        int port = args.length < 2 ? 31001 : Integer.parseInt(args[1]);
        String token = args.length < 3 ? "0000000000000000" : args[2];
        Logger.isLocalRun =  args.length >= 4 && args[3].equals("local");
        new Runner(host, port, token).run();
    }
}
