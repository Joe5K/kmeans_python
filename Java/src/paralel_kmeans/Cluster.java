package paralel_kmeans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

class Cluster
{
    private Point centroid;
    private ArrayList<Point> points;

    Cluster(Point Centroid)
    {
        this.centroid = Centroid;
        this.points = new ArrayList<Point>();
    }

    ArrayList<Point> getPoints(){
        return this.points;
    }

    public void setPoints(ArrayList<Point> points){
        this.points = points;
    }

    Point getCentroid(){
        return this.centroid;
    }

    void countNewCoordinates()
    {
        if (this.getPoints().size() == 0)
            return;

        var sumVector = new BigDecimal[this.getPoints().get(0).getLength()];
        Arrays.fill(sumVector, BigDecimal.ZERO);

        for (var Point: this.getPoints())
        {
            for (int i = 0; i < Point.getLength(); i++)
            {
                sumVector[i] = sumVector[i].add(BigDecimal.valueOf(Point.getOnIndex(i)));
            }
        }
        for (int i = 0; i< sumVector.length; i++)
        {
            this.centroid.setOnIndex(i, sumVector[i].doubleValue() / this.getPoints().size());
        }
    }

    boolean centroidEquals(Point otherCentroid){
        return this.getCentroid().equals(otherCentroid);
    }
}
