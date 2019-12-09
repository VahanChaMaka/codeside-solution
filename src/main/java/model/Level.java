package model;

import util.Logger;
import util.StreamUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Level {
    private Tile[][] tiles; //i for column, j for row. i -> x, j -> y
    private List<Wall> walls = new ArrayList<>();
    
    public Level() {}
    
    public Level(Tile[][] tiles) {
        this.tiles = tiles;
    }
    
    public static Level readFrom(java.io.InputStream stream) throws java.io.IOException {
        Level result = new Level();
        result.tiles = new Tile[StreamUtil.readInt(stream)][];
        for (int i = 0; i < result.tiles.length; i++) {
            result.tiles[i] = new Tile[StreamUtil.readInt(stream)];
            for (int j = 0; j < result.tiles[i].length; j++) {
                switch (StreamUtil.readInt(stream)) {
                case 0:
                    result.tiles[i][j] = Tile.EMPTY;
                    break;
                case 1:
                    result.tiles[i][j] = Tile.WALL;
                    break;
                case 2:
                    result.tiles[i][j] = Tile.PLATFORM;
                    break;
                case 3:
                    result.tiles[i][j] = Tile.LADDER;
                    break;
                case 4:
                    result.tiles[i][j] = Tile.JUMP_PAD;
                    break;
                default:
                    throw new java.io.IOException("Unexpected discriminant value");
                }
            }
        }

        result.buildWalls();

        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, tiles.length);
        for (Tile[] tilesElement : tiles) {
            StreamUtil.writeInt(stream, tilesElement.length);
            for (Tile tilesElementElement : tilesElement) {
                StreamUtil.writeInt(stream, tilesElementElement.discriminant);
            }
        }
    }

    private void buildWalls(){
        walls.clear();
        //vertical ones
        for (int i = 0; i < tiles.length; i++) {
            Point startPoint = null;
            for (int j = 0; j < tiles[i].length - 1; j++) {
                if(startPoint == null
                        && (tiles[i][j] != Tile.WALL || i == 0 || j == 0)
                        && tiles[i][j+1] == Tile.WALL){
                    startPoint = new Point(i, j+1);
                }
                if((tiles[i][j] == Tile.WALL && tiles[i][j+1] != Tile.WALL && j != 0) || (j == tiles[0].length-2 && startPoint != null)){
                    Point endPoint = new Point(i, j+1);
                    Wall leftWall = new Wall(startPoint,  endPoint);
                    if(startPoint == null){
                        Logger.log("Error while building walls: " + i + ", " + j);
                        Logger.log(this.toString());
                    }
                    Wall rightWall = new Wall(new Point(startPoint.x+1, startPoint.y),
                            new Point(endPoint.x+1, endPoint.y));

                    if(i != 0 && tiles[i-1][j] != Tile.WALL) {
                        walls.add(leftWall);
                    }
                    if(i < tiles.length-1 && tiles[i+1][j] != Tile.WALL) {
                        walls.add(rightWall);
                    }
                    startPoint = null;
                }
            }
        }

        for (int j = 0; j < tiles[0].length ; j++) { //for every row
            Point startPoint = null;
            for (int i = 0; i < tiles.length - 1; i++) {
                if(startPoint == null
                        && (tiles[i][j] != Tile.WALL || i == 0 || j == 0)
                        && tiles[i+1][j] == Tile.WALL){
                    startPoint = new Point(i+1, j);
                }
                if((tiles[i][j] == Tile.WALL && tiles[i+1][j] != Tile.WALL && i != 0) || (i == tiles.length-2 && startPoint != null && j != tiles[0].length-1)){
                    if(startPoint == null){
                        Logger.log("Error while building walls: " + i + ", " + j);
                        Logger.log(this.toString());
                    }

                    Point endPoint = new Point(i+1, j);
                    Wall bottom = new Wall(startPoint,  endPoint);
                    Wall top = new Wall(new Point(startPoint.x, startPoint.y+1),
                            new Point(endPoint.x, endPoint.y+1));

                    walls.add(top);
                    startPoint = null;
                }
            }
        }

        //ceiling
        walls.add(new Wall(new Point(0, tiles[0].length - 1), new Point(tiles.length-1, tiles[0].length - 1)));
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = tiles[0].length-1; i >= 0 ; i--) {
            for (int j = 0; j < tiles.length; j++) {
                builder.append(tiles[j][i].discriminant)
                        .append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
