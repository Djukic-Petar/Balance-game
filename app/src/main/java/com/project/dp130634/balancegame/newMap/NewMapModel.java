package com.project.dp130634.balancegame.newMap;

import com.project.dp130634.balancegame.GameField;

import java.io.Serializable;

/**
 * Created by John on 23-Aug-17.
 */

public class NewMapModel extends GameField implements Serializable {

    public boolean putStartPosition(float x, float y) {
        if(!holeOverlaps(x, y)) {
            start = new Hole(x, y);
            return true;
        } else {
            return false;
        }
    }

    public boolean replaceStartPosition(float x, float y) {
        Hole oldStart = start;
        start = null;
        if(!holeOverlaps(x, y)) {
            start = new Hole(x, y);
            return true;
        } else {
            start = oldStart;
            return false;
        }
    }

    public boolean hasStartPosition() {
        return start != null;
    }

    public boolean hasVictoryHole() {
        return end != null;
    }

    public boolean replaceVictoryHole(float x, float y) {
        Hole oldEnd = start;
        end = null;
        if(!holeOverlaps(x, y)) {
            end = new Hole(x, y);
            return true;
        } else {
            end = oldEnd;
            return false;
        }
    }

    public boolean putVictoryHole(float x, float y) {
        if(!holeOverlaps(x, y)) {
            end = new Hole(x, y);
            return true;
        } else {
            return false;
        }
    }

    public boolean addDeathHole(float x, float y) {
        if(!holeOverlaps(x, y)) {
            deathHoles.add(new Hole(x, y));
            return true;
        } else {
            return false;
        }
    }

    public boolean createTempWall(float x, float y) {
        tempWallHorizontal = new Wall(x - wallWidth/2, y, x + wallWidth/2, y);
        tempWallVertical = new Wall(x, y - wallWidth/2, x, y + wallWidth/2);
        wallDirection = WallDirection.UNKNOWN;
        if(wallOverlaps(tempWallHorizontal)) {
            tempWallHorizontal = tempWallVertical = null;
            return false;
        } else {
            tempWallCenter = new Point(x, y);
            return true;
        }
    }

    public boolean hasTempWall() {
        return tempWallHorizontal != null || tempWallVertical != null;
    }

    public boolean extendTempWall(float x, float y) {
        if(!hasTempWall()) {
            return false;
        }

        if(wallDirection == WallDirection.UNKNOWN) {
            if(x > tempWallCenter.x + wallWidth/2) {
                wallDirection = WallDirection.RIGHT;
                tempWallVertical = null;
            } else if(x < tempWallCenter.x - wallWidth/2) {
                wallDirection = WallDirection.LEFT;
                tempWallVertical = null;
                Point temp = tempWallHorizontal.getStartPoint();
                tempWallHorizontal.setStartPoint(tempWallHorizontal.getEndPoint());
                tempWallHorizontal.setEndPoint(temp);
            } else if(y > tempWallCenter.y + wallWidth/2) {
                wallDirection = WallDirection.DOWN;
                tempWallHorizontal = null;
            } else if(y < tempWallCenter.y - wallWidth/2) {
                wallDirection = WallDirection.UP;
                tempWallHorizontal = null;
                Point temp = tempWallVertical.getStartPoint();
                tempWallVertical.setStartPoint(tempWallVertical.getEndPoint());
                tempWallVertical.setEndPoint(temp);
            }
        }

        switch (wallDirection) {
            case DOWN:
                if(y > tempWallVertical.getEndPoint().y) {
                    Wall tempTempWall = new Wall(tempWallVertical.getStartPoint().x, tempWallVertical.getStartPoint().y, tempWallVertical.getEndPoint().x, y);
                    if(!wallOverlaps(tempTempWall)) {
                        tempWallVertical = tempTempWall;
                    } else {
                        return false;
                    }
                }
                break;

            case UP:
                if(y < tempWallVertical.getEndPoint().y) {
                    Wall tempTempWall = new Wall(tempWallVertical.getStartPoint().x, tempWallVertical.getStartPoint().y, tempWallVertical.getEndPoint().x, y);
                    if(!wallOverlaps(tempTempWall)) {
                        tempWallVertical = tempTempWall;
                    } else {
                        return false;
                    }
                }
                break;

            case LEFT:
                if(x < tempWallHorizontal.getEndPoint().x) {
                    Wall tempTempWall = new Wall(tempWallHorizontal.getStartPoint().x, tempWallHorizontal.getStartPoint().y, x, tempWallHorizontal.getEndPoint().y);
                    if(!wallOverlaps(tempTempWall)) {
                        tempWallHorizontal = tempTempWall;
                    } else {
                        return false;
                    }
                }
                break;

            case RIGHT:
                if(x > tempWallHorizontal.getEndPoint().x) {
                    Wall tempTempWall = new Wall(tempWallHorizontal.getStartPoint().x, tempWallHorizontal.getStartPoint().y, x, tempWallHorizontal.getEndPoint().y);
                    if(!wallOverlaps(tempTempWall)) {
                        tempWallHorizontal = tempTempWall;
                    } else {
                        return false;
                    }
                }
                break;
        }
        return true;
    }

    public boolean addWall() {
        boolean retVal = false;
        if(tempWallHorizontal != null) {
            walls.add(tempWallHorizontal);
            retVal = true;
        } else if (tempWallVertical != null) {
            walls.add(tempWallVertical);
            retVal = true;
        }
        tempWallCenter = null;
        tempWallHorizontal = tempWallVertical = null;
        wallDirection = WallDirection.UNKNOWN;
        return retVal;
    }

    private boolean holeOverlaps(float x, float y) {
        boolean overlap = false;

        if(start != null) {
            overlap = isOverlapping(start, new Hole(x, y));
        }
        if(overlap) {
            return true;
        }

        if(end != null) {
            overlap = isOverlapping(end, new Hole(x, y));
        }
        if(overlap) {
            return true;
        }

        for(int i = 0; i < walls.size(); i++) {
            overlap = isOverlapping(new Hole(x, y), walls.get(i));
            if(overlap) {
                return true;
            }
        }

        for(int i = 0; i < deathHoles.size(); i++) {
            overlap = isOverlapping(new Hole(x,y), deathHoles.get(i));
            if(overlap) {
                return true;
            }
        }
        return overlap;
    }

    private boolean wallOverlaps(Wall wall) {
        boolean overlap = false;
        if(start != null) {
            overlap = isOverlapping(start, wall);
        }
        if(overlap) {
            return true;
        }

        if(end != null) {
            overlap = isOverlapping(end, wall);
        }
        if(overlap) {
            return true;
        }
        for(int i = 0; i < deathHoles.size(); i++) {
            overlap = isOverlapping(deathHoles.get(i), wall);
            if(overlap) {
                return true;
            }
        }
        /*  Uncommenting this disables wall overlapping
        for(int i = 0; i < walls.size(); i++) {
            overlap = isOverlapping(wall, walls.get(i));
            if(overlap) {
                return true;
            }
        }
        */
        return overlap;
    }

    private enum WallDirection {LEFT, RIGHT, UP, DOWN, UNKNOWN};
    private transient WallDirection wallDirection = WallDirection.UNKNOWN;
    private transient Point tempWallCenter = null;
}
