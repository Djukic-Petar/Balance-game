package com.project.dp130634.balancegame;

import android.content.res.Resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 21-Aug-17.
 */

public abstract class GameField extends Model implements Serializable{
    public class Point implements Serializable {
        public float x;
        public float y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Point() {

        }
    }

    public class Hole implements Serializable{
        private Point center;
        public Hole(){}
        public Hole(float x, float y) {
            center = new Point(x, y);
        }

        public Point getCenter() {
            return center;
        }

        public void setCenter(Point center) {
            this.center = center;
        }
    }

    public class Wall implements Serializable {
        private Point startPoint;
        private Point endPoint;

        public Wall() {
        }

        public Wall(float startX, float startY, float endX, float endY) {
            startPoint = new Point(startX, startY);
            endPoint = new Point(endX, endY);
        }

        public Point getStartPoint() {
            return startPoint;
        }

        public void setStartPoint(Point startPoint) {
            this.startPoint = startPoint;
        }

        public Point getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(Point endPoint) {
            this.endPoint = endPoint;
        }
    }

    protected Hole start, end;
    protected Hole playingBall;
    protected List<Hole> deathHoles;
    protected List<Wall> walls;
    protected float fieldWidth;
    protected float fieldHeight;
    protected Wall tempWallHorizontal = null;
    protected Wall tempWallVertical = null;

    protected float holeRadius;
    protected float wallWidth;

    public GameField() {
        start = end = null;
        deathHoles = new ArrayList<>();
        walls = new ArrayList<>();
        fieldHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        fieldWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        float smallerDimension = fieldHeight > fieldWidth ? fieldWidth : fieldHeight;
        holeRadius = smallerDimension / 25;
        wallWidth = smallerDimension / 40;
    }

    public boolean hasRequiredElements() {
        return start != null && end != null;
    }

    public Wall getTempWallHorizontal() {
        return tempWallHorizontal;
    }

    public Wall getTempWallVertical() {
        return tempWallVertical;
    }

    public Hole getStartHole() {
        return this.start;
    }

    public Hole getEndHole() {
        return this.end;
    }

    public float getHoleRadius() {
        return this.holeRadius;
    }

    public float getWallWidth() {
        return this.wallWidth;
    }

    public List<Hole> getDeathHoles() {
        return  this.deathHoles;
    }

    public Hole getPlayingBall() {
        return this.playingBall;
    }

    public List<Wall> getWalls() {
        return this.walls;
    }

    public float getFieldWidth() {
        return fieldWidth;
    }

    public float getFieldHeight() {
        return fieldHeight;
    }

    public void setPlayingBall(Hole playingBall) {
        this.playingBall = playingBall;
    }

    public boolean isOverlapping(Hole hole, Wall wall) {
        //find distance between hole center and line which goes through the center of the wall
        float distance = Math.abs((wall.endPoint.y - wall.startPoint.y) * hole.center.x - (wall.endPoint.x - wall.startPoint.x) * hole.center.y + wall.endPoint.x * wall.startPoint.y - wall.endPoint.y * wall.startPoint.x);
        distance /= pointDistance(wall.startPoint, wall.endPoint);

        //find the center's orthogonal projection onto the wall's line
        Point projection = findPointProjectionOntoLine(hole.center, wall.startPoint, wall.endPoint);

        //calculate whether the center's projection onto the line is within the "wall" segment of the line
        float wallLength = pointDistance(wall.startPoint, wall.endPoint);
        boolean holeCenterProjectionInWall = pointDistance(projection, wall.startPoint) < wallLength && pointDistance(projection, wall.endPoint) < wallLength;

        //if yes, return (distance between hole center and line) < holeRadius + (wallWidth / 2)
        if(holeCenterProjectionInWall) {
            return distance < holeRadius + wallWidth / 2;
        }
        //if not, find distance between the hole center and the starting point of the wall
        else {
            float wallStartDistance = pointDistance(hole.center, wall.startPoint);
            float wallEndDistance = pointDistance(hole.center, wall.endPoint);
            Point closerPoint = wallStartDistance < wallEndDistance ? wall.startPoint : wall.endPoint;
            return pointDistance(hole.center, closerPoint) < holeRadius;
        }
    }

    public boolean isOverlapping(Hole hole1, Hole hole2) {
        return pointDistance(hole1.center, hole2.center) < holeRadius * 2;
    }

    public boolean isOverlapping(Wall wall1, Wall wall2) {
        if(wall1.startPoint.x == wall1.endPoint.x) {
            if(wall2.startPoint.x == wall2.endPoint.x) {
                return Math.abs(wall1.startPoint.x - wall2.startPoint.x) < wallWidth;
            } else {
                Point lineOverlap = new Point(wall1.startPoint.x, wall2.startPoint.y);
                return isPointInLineSegment(lineOverlap, wall1.startPoint, wall1.endPoint) && isPointInLineSegment(lineOverlap, wall2.startPoint, wall2.endPoint);
            }
        } else {
            if(wall2.startPoint.y == wall2.endPoint.y) {
                return Math.abs(wall1.startPoint.y - wall2.startPoint.y) < wallWidth;
            } else {
                Point lineOverlap = new Point(wall2.startPoint.x, wall1.startPoint.y);
                return isPointInLineSegment(lineOverlap, wall1.startPoint, wall1.endPoint) && isPointInLineSegment(lineOverlap, wall2.startPoint, wall2.endPoint);
            }
        }
    }

    protected boolean isPointInLineSegment(Point point, Point segmentPointA, Point segmentPointB) {
        double segmentLength = pointDistance(segmentPointA, segmentPointB);
        return pointDistance(point, segmentPointA) < segmentLength && pointDistance(point, segmentPointB) < segmentLength;
    }

    protected boolean isPointInCircle(Point point, Hole circle) {
        if(pointDistance(point, circle.getCenter()) < holeRadius) {
            return true;
        } else {
            return false;
        }
    }

    protected float pointDistance(Point point1, Point point2) {
        return (float)Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }

    protected Point findPointProjectionOntoLine(Point point, Point linePointA, Point linePointB) {
        Point projection = new Point();
        if(linePointA.x == linePointB.x) { //Horizontal line
            projection.x = linePointA.x;
            projection.y = point.y;
        } else { //Vertical line
            projection.x = point.x;
            projection.y = linePointA.y;
        }
        return projection;
    }
}
