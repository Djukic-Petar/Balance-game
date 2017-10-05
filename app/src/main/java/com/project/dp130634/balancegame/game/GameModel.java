package com.project.dp130634.balancegame.game;

import com.project.dp130634.balancegame.GameField;
import com.project.dp130634.balancegame.newMap.NewMapModel;

/**
 * Created by John on 25-Aug-17.
 */

public class GameModel extends GameField {

    public enum Encounter {NOTHING, VICTORY, DEATH, WALL_LEFT, WALL_RIGHT, WALL_UP, WALL_DOWN}

    private boolean topBlocked = false;
    private boolean bottomBlocked = false;
    private boolean leftBlocked = false;
    private boolean rightBlocked = false;

    public GameModel(NewMapModel savedModel) {
        start = savedModel.getStartHole();
        end = savedModel.getEndHole();
        playingBall = new Hole(start.getCenter().x, start.getCenter().y);
        deathHoles = savedModel.getDeathHoles();
        walls = savedModel.getWalls();
        fieldWidth = savedModel.getFieldWidth();
        fieldHeight = savedModel.getFieldHeight();
        holeRadius = savedModel.getHoleRadius();
        wallWidth = savedModel.getWallWidth();

        Wall leftVertical = new Wall(0, 0, 0, fieldHeight);
        Wall rightVertical = new Wall(fieldWidth, 0, fieldWidth, fieldHeight);
        Wall topHorizontal = new Wall(0, 0, fieldWidth, 0);
        Wall bottomHorizontal = new Wall(0, fieldHeight, fieldWidth, fieldHeight);
        walls.add(leftVertical);
        walls.add(rightVertical);
        walls.add(topHorizontal);
        walls.add(bottomHorizontal);
    }

    public Encounter moveBall(float x, float y) {
        x *= getHoleRadius() / 20;
        y *= getHoleRadius() / 20;
        playingBall.getCenter().x += x;
        playingBall.getCenter().y += y;
        if(death()) {
            return Encounter.DEATH;
        }
        if(victory()) {
            return Encounter.VICTORY;
        }
        Encounter wallEncounter = hitWall();
        switch (wallEncounter) {
            case WALL_LEFT:
                leftBlocked = true;
                rightBlocked = false;
                break;

            case WALL_RIGHT:
                leftBlocked = false;
                rightBlocked = true;
                break;

            case WALL_UP:
                topBlocked = true;
                bottomBlocked = false;
                break;

            case WALL_DOWN:
                topBlocked = false;
                bottomBlocked = true;
                break;

            case NOTHING:
                leftBlocked = false;
                topBlocked = false;
                bottomBlocked = false;
                rightBlocked = false;
                break;
        }
        return wallEncounter;
    }

    public void restart() {
        playingBall = new Hole(start.getCenter().x, start.getCenter().y);
    }

    /**
     * Checks if ball has fallen into one of the death holes
     * */
    private boolean death() {
        for(int i = 0; i < deathHoles.size(); i++) {
            Hole curDeath = deathHoles.get(i);
            if(isPointInCircle(playingBall.getCenter(), curDeath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if ball has fallen into the victory hole
     * */
    private boolean victory() {
        return isPointInCircle(playingBall.getCenter(), getEndHole());
    }

    private Encounter hitWall() {
        Encounter retVal = Encounter.NOTHING;
        for(int i = 0; i < walls.size(); i++) {
            Wall curWall = walls.get(i);
            Encounter curEncounter = ballHitWall(playingBall, curWall);
            if(curEncounter != Encounter.NOTHING && retVal == Encounter.NOTHING) {
                retVal = curEncounter;
            }
        }
        return retVal;
    }

    private Encounter ballHitWall(Hole hole, Wall wall) {
        Point wallStartPoint = wall.getStartPoint();
        Point wallEndPoint = wall.getEndPoint();
        Point holeCenter = hole.getCenter();
        //find distance between hole center and line which goes through the center of the wall
        float distance = Math.abs((wallEndPoint.y - wallStartPoint.y) * holeCenter.x - (wallEndPoint.x - wallStartPoint.x) * holeCenter.y + wallEndPoint.x * wallStartPoint.y - wallEndPoint.y * wallStartPoint.x);
        distance /= pointDistance(wallStartPoint, wallEndPoint);

        //find the center's orthogonal projection onto the wall's line
        Point projection = findPointProjectionOntoLine(holeCenter, wallStartPoint, wallEndPoint);

        //calculate whether the center's projection onto the line is within the "wall" segment of the line
        float wallLength = pointDistance(wallStartPoint, wallEndPoint);
        boolean holeCenterProjectionInWall = pointDistance(projection, wallStartPoint) < wallLength && pointDistance(projection, wallEndPoint) < wallLength;

        //if yes, return (distance between hole center and line) < holeRadius + (wallWidth / 2)
        if(holeCenterProjectionInWall) {
            if(distance < holeRadius + wallWidth / 2) {
                //Hit side of wall, check if wall is horizontal or vertical
                if(wallStartPoint.x == wallEndPoint.x) {
                    //wall is vertical check if ball hit left or right side of wall
                    if(wallStartPoint.x > hole.getCenter().x) {
                        rightBlocked = true;
                        return Encounter.WALL_RIGHT;
                    } else {
                        leftBlocked = true;
                        return Encounter.WALL_LEFT;
                    }
                } else {
                    if(wallStartPoint.y > holeCenter.y) {
                        bottomBlocked = true;
                        return Encounter.WALL_DOWN;
                    } else {
                        topBlocked = true;
                        return Encounter.WALL_UP;
                    }
                }
            } else {
                return Encounter.NOTHING;
            }
        }
        //if not, find distance between the hole center and the starting point of the wall
        else {
            float wallStartDistance = pointDistance(holeCenter, wallStartPoint);
            float wallEndDistance = pointDistance(holeCenter, wallEndPoint);
            Point closerPoint = wallStartDistance < wallEndDistance ? wallStartPoint : wallEndPoint;
            if(pointDistance(holeCenter, closerPoint) < holeRadius) {
                //Hit top/bottom of wall, check if wall is horizontal or vertical
                if(wallStartPoint.x == wallEndPoint.x) {
                    //Vertical wall, hit horizontal side
                    if(wallStartPoint.y > holeCenter.y) {
                        return Encounter.WALL_DOWN;
                    } else {
                        return Encounter.WALL_UP;
                    }
                } else {
                    if(wallStartPoint.x > holeCenter.x) {
                        return Encounter.WALL_RIGHT;
                    } else {
                        return Encounter.WALL_LEFT;
                    }
                }
            } else {
                return Encounter.NOTHING;
            }
        }
    }

    public boolean isTopBlocked() {
        return topBlocked;
    }

    public boolean isBottomBlocked() {
        return bottomBlocked;
    }

    public boolean isLeftBlocked() {
        return leftBlocked;
    }

    public boolean isRightBlocked() {
        return rightBlocked;
    }
}
