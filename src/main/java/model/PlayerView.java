package model;

import util.StreamUtil;

public class PlayerView {
    private int myId;
    private Game game;

    public PlayerView() {}

    public PlayerView(int myId, Game game) {
        this.myId = myId;
        this.game = game;
    }

    public static PlayerView readFrom(java.io.InputStream stream) throws java.io.IOException {
        PlayerView result = new PlayerView();
        result.myId = StreamUtil.readInt(stream);
        result.game = Game.readFrom(stream);
        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, myId);
        game.writeTo(stream);
    }

    public int getMyId() {
        return myId;
    }

    public void setMyId(int myId) {
        this.myId = myId;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public String toString() {
        return "PlayerView{" +
                "myId=" + myId +
                ", game=" + game +
                '}';
    }
}
