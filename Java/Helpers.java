package paralel_kmeans;

import java.util.ArrayList;
import java.util.Arrays;

class Helpers {

    static double miliSecondsToSeconds(long miliSeconds){
        return (double)miliSeconds/1000;
    }

    static double round(double val, int decimals){
        if (decimals <= 0)
            return val;

        var multiplier = 10;
        for (int i = 1; i < decimals; i++){
            multiplier *= 10;
        }

        return (double)Math.round(val * multiplier) / multiplier;
    }

    static ArrayList<Point> copyCentroids(ArrayList<Cluster> clusters)
    {
        ArrayList<Point> oldCentroids = new ArrayList<Point>();
        for (var cluster: clusters)
        {
            var coordinates = cluster.getCentroid().getCoordinates();
            var copiedCoordinates = Arrays.copyOf(coordinates, coordinates.length);
            oldCentroids.add(new Point(copiedCoordinates));
        }
        return oldCentroids;
    }
}
